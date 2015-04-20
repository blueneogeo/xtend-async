package nl.kii.observe;

public interface Observable<T extends java.lang.Object> {
  /**
   * Observe changes on the observable.
   * @return a function that can be called to stop observing
   */
  public abstract /*  */Object onChange(final /*  */Object observeFn);
}
