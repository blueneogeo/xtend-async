package nl.kii.async;

@SuppressWarnings("all")
public class AsyncException extends Exception {
  public final Object value;
  
  public AsyncException(final String message, final Object value) {
    super(((message + ": ") + value));
    this.value = value;
  }
  
  public AsyncException(final String message, final Object value, final Throwable cause) {
    super(((message + ": ") + value), cause);
    this.value = value;
  }
}
