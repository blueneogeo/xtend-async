package nl.kii.stream.internal;

import nl.kii.stream.internal.StreamEventHandler;
import nl.kii.stream.message.Entry;

public class StreamEventResponder implements StreamEventHandler {
  /* @Atomic
   */private /* Procedure1<Void> */Object nextFn;
  
  /* @Atomic
   */private /* Procedure1<Void> */Object skipFn;
  
  /* @Atomic
   */private /* Procedure1<Void> */Object closeFn;
  
  /* @Atomic
   */private /* Procedure1<Entry<? extends  */Object overflowFn;
  
  public Procedure1 next(final /*  */Object handler) {
    return this.nextFn = handler;
  }
  
  public Procedure1 skip(final /*  */Object handler) {
    return this.skipFn = handler;
  }
  
  public Procedure1 close(final /*  */Object handler) {
    return this.closeFn = handler;
  }
  
  public Procedure1 overflow(final /*  */Object handler) {
    return this.overflowFn = handler;
  }
  
  public void onNext() {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onSkip() {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onClose() {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public void onOverflow(final /* Entry<? extends  */Object entry) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
}
