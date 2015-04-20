package nl.kii.stream.internal;

import nl.kii.stream.message.Entry;

public interface StreamEventHandler {
  public abstract void onNext();
  
  public abstract void onSkip();
  
  public abstract void onClose();
  
  public abstract void onOverflow(final /* Entry<? extends  */Object entry);
}
