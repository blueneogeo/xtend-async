package nl.kii.act;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.act.AtMaxProcessDepth;
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
 * you are done processing. This tells the actor that the asynchronous work has been completed.
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
 * For this actor to perform well, the code it 'acts' upon should be non-blocking and relatively lightweight.
 * If you do need to perform heavy work, make an asynchronous call, and call the passed done function when
 * the processing is complete. The done method is used to tell the actor that the acting is finished and the next
 * item can be processed from its queue.
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
public abstract class Actor<T extends Object> implements Procedure1<T> {
  /**
   * Since the actor supports asynchronous callbacks, its main loop must be implemented via
   * recursion. However since it has no seperate process loop (as it steals from the threads
   * that call it), this can mean that the recursion can go deeper than Java allows. The correct
   * way of dealing with this would be via tail optimization, however this is unsupported by
   * the JVM. Another good way of dealing with the growing stacktrace is by using continuations.
   * That way, you can have a looping continuation instead of recursion. However, this is only
   * possible using ASM bytecode enhancement, which I tried to avoid. Because of this, this
   * actor uses a dirty trick to escape too long recursion stacktraces: it throws an exception
   * when the stack is too deep, breaking out of the call-stack back to the loop. However since
   * recursion is more performant than constantly throwing exceptions, it will only do so after
   * an X amount of process depth. The MAX_PROCESS_DEPTH setting below sets how many time the
   * processNextAsync call can be called recursively before it breaks out through an exception.
   */
  private final static int MAX_PROCESS_DEPTH = 50;
  
  private final Queue<T> inbox;
  
  private final AtomicBoolean processing = new AtomicBoolean(false);
  
  /**
   * Create a new actor with a concurrentlinkedqueue as inbox
   */
  public Actor() {
    this(Queues.<T>newConcurrentLinkedQueue());
  }
  
  /**
   * Create an actor with the given queue as inbox.
   * Queue implementation must be threadsafe and non-blocking.
   */
  public Actor(final Queue<T> queue) {
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
  
  protected void process() {
    boolean _and = false;
    boolean _get = this.processing.get();
    boolean _not = (!_get);
    if (!_not) {
      _and = false;
    } else {
      boolean _isEmpty = this.inbox.isEmpty();
      boolean _not_1 = (!_isEmpty);
      _and = _not_1;
    }
    boolean _while = _and;
    while (_while) {
      try {
        this.processNextAsync(Actor.MAX_PROCESS_DEPTH);
        this.processing.set(false);
        return;
      } catch (final Throwable _t) {
        if (_t instanceof AtMaxProcessDepth) {
          final AtMaxProcessDepth e = (AtMaxProcessDepth)_t;
          this.processing.set(false);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      boolean _and_1 = false;
      boolean _get_1 = this.processing.get();
      boolean _not_2 = (!_get_1);
      if (!_not_2) {
        _and_1 = false;
      } else {
        boolean _isEmpty_1 = this.inbox.isEmpty();
        boolean _not_3 = (!_isEmpty_1);
        _and_1 = _not_3;
      }
      _while = _and_1;
    }
  }
  
  protected void processNextAsync(final int depth) {
    try {
      if ((depth == 0)) {
        throw new AtMaxProcessDepth();
      }
      boolean _get = this.processing.get();
      if (_get) {
        return;
      }
      final T message = this.inbox.poll();
      boolean _equals = Objects.equal(message, null);
      if (_equals) {
        return;
      }
      this.processing.set(true);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          Actor.this.processing.set(false);
          boolean _isEmpty = Actor.this.inbox.isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            Actor.this.processNextAsync((depth - 1));
          }
        }
      };
      this.act(message, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Collection<T> getInbox() {
    return Collections.<T>unmodifiableCollection(this.inbox);
  }
}