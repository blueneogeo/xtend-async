package nl.kii.promise;

import com.google.common.util.concurrent.AbstractFuture;
import nl.kii.promise.IPromise;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Converts a promise into a future for legacy code.
 */
@SuppressWarnings("all")
public class PromiseFuture<T extends Object> extends AbstractFuture<T> {
  public PromiseFuture(final IPromise<?, T> promise) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        PromiseFuture.this.setException(it);
      }
    };
    IPromise<?, T> _on = promise.on(Throwable.class, _function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      @Override
      public void apply(final T it) {
        PromiseFuture.this.set(it);
      }
    };
    _on.then(_function_1);
  }
}
