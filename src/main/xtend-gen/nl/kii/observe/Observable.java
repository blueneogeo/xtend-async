package nl.kii.observe;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface Observable<T extends Object> {
  /**
   * Observe changes on the observable.
   * @return a function that can be called to stop observing
   */
  public abstract Procedure0 onChange(final Procedure1<? super T> observeFn);
}
