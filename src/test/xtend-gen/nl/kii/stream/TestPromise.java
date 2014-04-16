package nl.kii.stream;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.StreamAssert;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromise {
  @Test
  public void testPromisedAfter() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.class);
    final Promise<Integer> p2 = PromiseExt.<Integer>promise(Integer.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        PromiseExt.<Integer>operator_doubleGreaterThan(it, p2);
      }
    };
    p.then(_function);
    p.set(Integer.valueOf(10));
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(10));
  }
  
  @Test
  public void testPromisedBefore() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.class);
    final Promise<Integer> p2 = PromiseExt.<Integer>promise(Integer.class);
    p.set(Integer.valueOf(10));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        PromiseExt.<Integer>operator_doubleGreaterThan(it, p2);
      }
    };
    p.then(_function);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(10));
  }
  
  @Test
  public void testPromiseErrorHandling() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.valueOf(0));
    final Promise<Boolean> p2 = PromiseExt.<Boolean>promise(boolean.class);
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        PromiseExt.<Boolean>operator_doubleGreaterThan(Boolean.valueOf(true), p2);
      }
    };
    p.onError(_function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(Integer.valueOf((1 / (it).intValue())));
      }
    };
    p.then(_function_1);
    StreamAssert.<Boolean>assertPromiseEquals(p2, Boolean.valueOf(true));
  }
  
  @Test
  public void testPromiseNoHandling() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.valueOf(0));
    try {
      final Procedure1<Integer> _function = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          InputOutput.<Integer>println(Integer.valueOf((1 / (it).intValue())));
        }
      };
      p.then(_function);
      Assert.fail("we should have gotten an error");
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        final Throwable t = (Throwable)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testPromiseErrorChaining() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.valueOf(1));
    final Promise<Boolean> p2 = PromiseExt.<Boolean>promise(boolean.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() - 1));
      }
    };
    Promise<Integer> _map = PromiseExt.<Integer, Integer>map(p, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((1 / (it).intValue()));
      }
    };
    Promise<Integer> _map_1 = PromiseExt.<Integer, Integer>map(_map, _function_1);
    final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    Promise<Integer> _map_2 = PromiseExt.<Integer, Integer>map(_map_1, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        PromiseExt.<Boolean>operator_doubleGreaterThan(Boolean.valueOf(true), p2);
      }
    };
    Promise<Integer> _onError = _map_2.onError(_function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    _onError.then(_function_4);
    StreamAssert.<Boolean>assertPromiseEquals(p2, Boolean.valueOf(true));
  }
}
