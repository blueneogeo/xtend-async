package nl.kii.stream;

@SuppressWarnings("all")
public interface StreamObserver<I extends Object, O extends Object> {
  /**
   * handle an incoming value
   */
  public abstract void onValue(final I from, final O value);
  
  /**
   * handle an incoming error
   */
  public abstract void onError(final I from, final Throwable t);
  
  /**
   * handle an imcoming finish of a given level
   */
  public abstract void onFinish(final I from, final int level);
  
  /**
   * handle the stream being closed
   */
  public abstract void onClosed();
}
