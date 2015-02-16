package nl.kii.async.test;

import java.util.concurrent.Future;
import nl.kii.async.ExecutorExtensions;
import nl.kii.promise.BasePromise;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.SubPromise;
import nl.kii.promise.Task;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Test;

@SuppressWarnings("all")
public class FancyAsyncTests {
  @Test
  public void testFancyClosures() {
    try {
      Task _complete = PromiseExtensions.complete();
      final Function1<Boolean, String> _function = new Function1<Boolean, String>() {
        public String apply(final Boolean it) {
          return "hello world";
        }
      };
      SubPromise<Boolean, String> _map = PromiseExtensions.<Boolean, Boolean, String>map(_complete, _function);
      final Function1<SubPromise<Boolean, String>, SubPromise<Boolean, String>> _function_1 = new Function1<SubPromise<Boolean, String>, SubPromise<Boolean, String>>() {
        public SubPromise<Boolean, String> apply(final SubPromise<Boolean, String> it) {
          return it;
        }
      };
      final Function1<SubPromise<Boolean, String>, Promise<String>> _function_2 = new Function1<SubPromise<Boolean, String>, Promise<String>>() {
        public Promise<String> apply(final SubPromise<Boolean, String> it) {
          return PromiseExtensions.<String>error(String.class, "test");
        }
      };
      SubPromise<Boolean, String> _if_ = FancyAsyncTests.<Boolean, String, String, SubPromise<Boolean, String>, BasePromise<? extends Object, String>>if_(_map, true, _function_1, _function_2);
      Future<String> _future = ExecutorExtensions.<Boolean, String>future(_if_);
      final String x = _future.get();
      InputOutput.<String>println(x);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<I, O>, P2 extends IPromise<?, R>> Object if_(final P promise, final Function1<? super O, ? extends Boolean> testFn, final Function1<? super P, ? extends P2> thenFn) {
    return null;
  }
  
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<I, O>> Pair<P, Boolean> if_(final P promise, final Function1<? super O, ? extends Boolean> testFn) {
    return null;
  }
  
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<I, O>, P2 extends IPromise<?, R>> SubPromise<I, R> if_(final P promise, final Function1<? super O, ? extends Boolean> testFn, final Function1<? super P, ? extends P2> thenFn, final Function1<? super P, ? extends P2> elseFn) {
    SubPromise<I, R> _xblockexpression = null;
    {
      final SubPromise<I, R> newPromise = new SubPromise<I, R>(promise);
      final Procedure2<I, Throwable> _function = new Procedure2<I, Throwable>() {
        public void apply(final I i, final Throwable e) {
          newPromise.error(i, e);
        }
      };
      IPromise<I, O> _onError = promise.onError(_function);
      final Procedure2<I, O> _function_1 = new Procedure2<I, O>() {
        public void apply(final I i, final O o) {
          try {
            P2 _xifexpression = null;
            Boolean _apply = testFn.apply(o);
            if ((_apply).booleanValue()) {
              _xifexpression = thenFn.apply(promise);
            } else {
              _xifexpression = elseFn.apply(promise);
            }
            final P2 result = _xifexpression;
            final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
              public void apply(final Throwable it) {
                newPromise.error(it);
              }
            };
            IPromise<?, R> _onError = result.onError(_function);
            final Procedure1<R> _function_1 = new Procedure1<R>() {
              public void apply(final R r) {
                newPromise.set(i, r);
              }
            };
            _onError.then(_function_1);
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable e = (Throwable)_t;
              newPromise.error(e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      _onError.then(_function_1);
      final Procedure1<SubPromise<I, R>> _function_2 = new Procedure1<SubPromise<I, R>>() {
        public void apply(final SubPromise<I, R> it) {
          it.setOperation("when");
        }
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I, R>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<I, O>, P2 extends IPromise<?, R>> SubPromise<I, R> if_(final P promise, final boolean condition, final Function1<? super P, ? extends P2> thenFn, final Function1<? super P, ? extends P2> elseFn) {
    final Function1<O, Boolean> _function = new Function1<O, Boolean>() {
      public Boolean apply(final O it) {
        return Boolean.valueOf(condition);
      }
    };
    return FancyAsyncTests.<I, O, R, P, P2>if_(promise, _function, thenFn, elseFn);
  }
}
