package nl.kii.stream.internal;

import nl.kii.stream.IStream;
import nl.kii.stream.internal.StreamObserver;

/**
 * A basic builder for asynchronous stream listening.
 * Combine with StreamExtensions.on like this:
 * <p>
 * <pre>
 * stream.on [
 *    each [ ... stream.next ]
 *    finish [ ... ]
 *    error [ ... ]
 * ]
 * stream.next
 * </pre>
 * <p>
 * Remember to call stream.next to start the stream!
 */
public class StreamResponder<I extends java.lang.Object, O extends java.lang.Object> implements StreamObserver<I, O> {
  /* @Atomic
   */public final IStream<I, O> stream;
  
  /* @Atomic
   */private /* Procedure2<I, O> */Object valueFn;
  
  /* @Atomic
   */private /* Procedure2<I, Throwable> */Object errorFn;
  
  /* @Atomic
   */private /* Procedure2<I, Integer> */Object finishFn;
  
  /* @Atomic
   */private /* Procedure1<Void> */Object closedFn;
  
  /**
   * listen for each incoming value
   */
  public Procedure2 each(final /*  */Object handler) {
    return this.valueFn = handler;
  }
  
  /**
   * listen for any finish
   */
  public Procedure2 finish(final /*  */Object handler) {
    return this.finishFn = handler;
  }
  
  /**
   * listen for any uncaught errors
   */
  public Procedure2 error(final /*  */Object handler) {
    return this.errorFn = handler;
  }
  
  /**
   * listen for when the stream closes
   */
  public Procedure1 closed(final /*  */Object handler) {
    return this.closedFn = handler;
  }
  
  public void onValue(final I from, final O value) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onError(final I from, final /* Throwable */Object t) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onFinish(final I from, final int level) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onClosed() {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
}
