package nl.kii.act;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * An Actor<T> is a threadsafe procedure that guarantees that the execution of the act method is
 * single threaded. To accomplish this, it has an inbox (which is a queue) which gathers all
 * incoming messages of type T. Calls from one or more threads to the apply function simply add to
 * this queue. The actor then uses a singlethreaded process loop to process these messages one by one.
 * The acting is implemented by extending this actor and implementing the act method.
 * 
 * <h3>Asynchronous act method</h3>
 * 
 * The act method of this actor is asynchronous. This means that you have to call done.apply to indicate
 * you are done processing. This allows the actor to signal when asynchronous work has been completed.
 * 
 * <h3>Thread borrowing</h3>
 * 
 * This actor is built to work well in asynchronous, single threaded, non blocking environments and is
 * built to be lightweight. As such it has no threadpool of its own for the process loop.
 * Instead, this actor implementation borrows the processing power of the tread that applies something
 * to perform its act loop. Once the loop is empty, it releases the thread. If while one thread is being
 * borrowed and processing, another applies, that other thread is let go. This way, only one thread is ever
 * processing the act loop. This allows you to use the actor as a single-threaded yet threadsafe worker.
 * 
 * <h3>Usage:</h3>
 * 
 * <pre>
 * // anonymous class version
 * val Actor<String> greeter = new Actor() {
 *     def act(String message, =>void done) {
 *         println('hello ' + message + '!')
 *         done.apply // signal we are done processing the message
 *     }
 * }
 * greeter.apply('world')
 * <p>
 * // lambda version
 * val Actor<String> greeter = [ String message, done |
 *     println('hello ' + message + '!')
 *     done.apply
 * ]
 * greeter.apply('world')
 * <p>
 * // using import static extension nl.kii.actor.ActorExtensions
 * val greeter = actor [ println('hello' + it) ]
 * 'world' >> greeter
 * </pre>
 */
@SuppressWarnings("all")
public abstract class AsyncActor<T extends Object> implements Procedure1<T> {
  private final Queue<T> inbox;
  
  private final ReentrantLock processLock = new ReentrantLock();
  
  /**
   * Create a new actor with a concurrentlinkedqueue as inbox
   */
  public AsyncActor() {
    this(Queues.<T>newConcurrentLinkedQueue());
  }
  
  /**
   * Create an actor with the given queue as inbox.
   * Queue implementation must be threadsafe and non-blocking.
   */
  public AsyncActor(final Queue<T> queue) {
    this.inbox = queue;
  }
  
  /**
   * Give the actor something to process
   */
  public void apply(final T message) {
    this.inbox.add(message);
    this.process();
  }
  
  /**
   * Perform the actor on the next message in the inbox.
   * You must call done.apply when you have completed processing!
   */
  protected abstract void act(final T message, final Procedure0 done);
  
  /**
   * Start the process loop that takes input from the inbox queue one by one and calls the
   * act method for each input.
   */
  protected void process() {
    try {
      boolean _tryLock = this.processLock.tryLock(1, TimeUnit.MILLISECONDS);
      if (_tryLock) {
        final T message = this.inbox.poll();
        boolean _notEquals = (!Objects.equal(message, null));
        if (_notEquals) {
          try {
            this.act(message, this.onProcessDone);
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable t = (Throwable)_t;
              this.onProcessDone.apply();
              throw t;
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        } else {
          this.processLock.unlock();
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private final Procedure0 onProcessDone = new Procedure0() {
    public void apply() {
      AsyncActor.this.processLock.unlock();
      boolean _isEmpty = AsyncActor.this.inbox.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        AsyncActor.this.process();
      }
    }
  };
  
  public Collection<T> getInbox() {
    return Collections.<T>unmodifiableCollection(this.inbox);
  }
}
