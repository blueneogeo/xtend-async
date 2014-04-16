package nl.kii.stream;

import com.google.common.util.concurrent.AbstractFuture;
import nl.kii.stream.Promise;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class PromiseFuture<T extends Object> extends AbstractFuture<T> {
  public PromiseFuture(final Promise<T> promise) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        PromiseFuture.this.setException(it);
      }
    };
    Promise<T> _onError = promise.onError(_function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      public void apply(final T it) {
        PromiseFuture.this.set(it);
      }
    };
    _onError.then(_function_1);
  }
}
