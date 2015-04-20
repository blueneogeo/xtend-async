package nl.kii.observe;

import nl.kii.act.Actor;
import nl.kii.observe.Observable;

/**
 * A threadsafe non-blocking distributor of events to its registered listeners.
 * <p>
 * A Publisher is more lightweight than a stream with a streamobserver.
 * It does not have any flow control or async support, and has only
 * a single queue. Contrary to a stream, it allows for multiple
 * subscriptions, and each subscription can be unsubscribed by calling
 * the returned method.
 * <p>
 * For it to work correctly, the listeners should be non-blocking.
 */
public class Publisher<T extends java.lang.Object> extends Actor<T> implements /* Procedure1<T> */Observable<T> {
  /* @Atomic
   */public final boolean publishing = true;
  
  /* @Atomic
   */private final transient /* List<Procedure1<T>> */Object observers;
  
  public Publisher() {
  }
  
  public Publisher(final boolean isPublishing) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe final field publishing may already have been assigned");
  }
  
  /**
   * Listen for publications from the publisher
   */
  public /*  */Object onChange(final /*  */Object observeFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nCopyOnWriteArrayList cannot be resolved."
      + "\nAssignment to final field"
      + "\n== cannot be resolved"
      + "\nadd cannot be resolved"
      + "\nremove cannot be resolved");
  }
  
  public void act(final T message, final /*  */Object done) {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\napply cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  public Object getSubscriptionCount() {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved"
      + "\nsize cannot be resolved");
  }
  
  public java.lang.CharSequence toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nsize cannot be resolved"
      + "\nsize cannot be resolved");
  }
}
