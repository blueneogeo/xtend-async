package nl.kii.stream;

@SuppressWarnings("all")
public interface StreamMonitor {
  public abstract void onNext();
  
  public abstract void onSkip();
  
  public abstract void onClose();
}
