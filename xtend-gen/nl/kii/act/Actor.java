package nl.kii.act;

/**
 * An Actor<T> is a computational unit that processes incoming messages, spawn other actors,
 * send messages to other actors and code, and that can contain its own state.
 * <p>
 * http://en.wikipedia.org/wiki/Actor_model
 * <p>
 * In Java an Actor<T> is a threadsafe procedure that guarantees that the execution of the act method is
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
public abstract class Actor<T extends java.lang.Object> /* implements Procedure1<T>  */{
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
  private final static int MAX_PROCESS_DEPTH = 10;
  
  /**
   * The queue of messages waiting to be processed by this actor.
   * Must be a threadsafe queue.
   */
  private final /* Queue<T> */Object inbox;
  
  /**
   * Indicates if this actor is busy processing the queue.
   */
  /* @Atomic
   */private final boolean processing = false;
  
  /**
   * Create a new actor with a concurrentlinkedqueue as inbox
   */
  public Actor() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field newConcurrentLinkedQueue is undefined for the type Actor");
  }
  
  /**
   * Create an actor with the given queue as inbox.
   * Queue implementation must be threadsafe and non-blocking.
   */
  public Actor(final /* Queue<T> */Object queue) {
    this.inbox = queue;
  }
  
  /**
   * Give the actor something to process
   */
  public void apply(final T message) {
    throw new Error("Unresolved compilation problems:"
      + "\nadd cannot be resolved");
  }
  
  /**
   * Perform the actor on the next message in the inbox.
   * You must call done.apply when you have completed processing!
   */
  protected abstract void act(final T message, final /*  */Object done);
  
  /**
   * Start processing as many messages as possible before releasing the thread.
   */
  protected void process() {
    throw new Error("Unresolved compilation problems:"
      + "\nNo exception of type AtMaxProcessDepth can be thrown; an exception type must be a subclass of Throwable"
      + "\nempty cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  /**
   * Process a single message recursively by calling itself until either the inbox is empty,
   * or the maximum depth is achieved, in which case an AtMaxProcessDepth exception is thrown.
   */
  protected boolean processNextAsync(final int depth) {
    throw new Error("Unresolved compilation problems:"
      + "\n== cannot be resolved."
      + "\nThe method or field _processing is undefined for the type Actor"
      + "\n- cannot be resolved."
      + "\nAssignment to final field"
      + "\ncompareAndSet cannot be resolved"
      + "\n! cannot be resolved"
      + "\npoll cannot be resolved"
      + "\n== cannot be resolved"
      + "\nempty cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  /**
   * Get the current inbox. The returned collection is unmodifiable.
   */
  public Object getInbox() {
    throw new Error("Unresolved compilation problems:"
      + "\nunmodifiableView cannot be resolved");
  }
  
  private final static int MAX_INBOX_TO_PRINT = 10;
  
  public java.lang.CharSequence toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nsize cannot be resolved"
      + "\nsize cannot be resolved"
      + "\n< cannot be resolved"
      + "\nMAX_INBOX_TO_PRINT cannot be resolved");
  }
}
