package nl.kii.promise;

@SuppressWarnings("all")
public class PromiseException extends Exception {
  public final Object value;
  
  public PromiseException(final String message, final Object value) {
    super(((message + ": ") + value));
    this.value = value;
  }
  
  public PromiseException(final String message, final Object value, final Throwable cause) {
    super(((message + ": ") + value), cause);
    this.value = value;
  }
}
