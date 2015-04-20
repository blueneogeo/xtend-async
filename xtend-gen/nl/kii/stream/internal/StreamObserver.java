package nl.kii.stream.internal;

public interface StreamObserver<I extends java.lang.Object, O extends java.lang.Object> {
  /**
   * handle an incoming value
   */
  public abstract void onValue(final I from, final O value);
  
  /**
   * handle an incoming error
   */
  public abstract void onError(final I from, final /* Throwable */Object t);
  
  /**
   * handle an imcoming finish of a given level
   */
  public abstract void onFinish(final I from, final int level);
  
  /**
   * handle the stream being closed
   */
  public abstract void onClosed();
}
