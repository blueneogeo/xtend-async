package nl.kii.act;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class LoopingActor<T extends Object> implements Procedure1<T> {
  private final Queue<T> inbox;
  
  private final ReentrantLock processLock = new ReentrantLock();
  
  /**
   * Create a new actor with a concurrentlinkedqueue as inbox
   */
  public LoopingActor() {
    this(Queues.<T>newConcurrentLinkedQueue());
  }
  
  /**
   * Create an actor with the given queue as inbox.
   * Queue implementation must be threadsafe and non-blocking.
   */
  public LoopingActor(final Queue<T> queue) {
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
  protected abstract void act(final T message);
  
  /**
   * Start the process loop that takes input from the inbox queue one by one and calls the
   * act method for each input.
   */
  protected void process() {
    try {
      boolean _tryLock = this.processLock.tryLock(1, TimeUnit.MILLISECONDS);
      if (_tryLock) {
        try {
          boolean _isEmpty = this.inbox.isEmpty();
          boolean _not = (!_isEmpty);
          boolean _while = _not;
          while (_while) {
            {
              final T message = this.inbox.poll();
              this.act(message);
            }
            boolean _isEmpty_1 = this.inbox.isEmpty();
            boolean _not_1 = (!_isEmpty_1);
            _while = _not_1;
          }
        } finally {
          this.processLock.unlock();
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Collection<T> getInbox() {
    return Collections.<T>unmodifiableCollection(this.inbox);
  }
}
