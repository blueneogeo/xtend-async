package nl.kii.stream;

import nl.kii.stream.Entry;

@SuppressWarnings("all")
public interface StreamMonitor {
  public abstract void onNext();
  
  public abstract void onSkip();
  
  public abstract void onClose();
  
  public abstract void onOverflow(final Entry<?> entry);
}
