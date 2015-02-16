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
      public void apply(final Throwable it) {
        PromiseFuture.this.setException(it);
      }
    };
    IPromise<?, T> _onError = promise.onError(_function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      public void apply(final T it) {
        PromiseFuture.this.set(it);
      }
    };
    _onError.then(_function_1);
  }
}
