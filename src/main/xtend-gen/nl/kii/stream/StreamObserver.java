package nl.kii.stream;

@SuppressWarnings("all")
public interface StreamObserver<R extends Object, T extends Object> {
  /**
   * handle an incoming value
   */
  public abstract void onValue(final R from, final T value);
  
  /**
   * handle an incoming error
   * @return if the error should be escalated/thrown
   */
  public abstract boolean onError(final R from, final Throwable t);
  
  /**
   * handle an imcoming finish of a given level
   */
  public abstract void onFinish(final int level);
  
  /**
   * handle the stream being closed
   */
  public abstract void onClosed();
}
