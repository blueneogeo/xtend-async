package nl.kii.promise.test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Async;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
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
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.class);
    final Promise<Integer> p2 = PromiseExtensions.<Integer>promise(Integer.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        PromiseExtensions.<Integer>operator_doubleGreaterThan(it, p2);
      }
    };
    p.then(_function);
    p.set(Integer.valueOf(10));
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(10));
  }
  
  @Test
  public void testPromisedBefore() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.class);
    final Promise<Integer> p2 = PromiseExtensions.<Integer>promise(Integer.class);
    p.set(Integer.valueOf(10));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        PromiseExtensions.<Integer>operator_doubleGreaterThan(it, p2);
      }
    };
    p.then(_function);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(10));
  }
  
  @Test
  public void testPromiseErrorHandling() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(0));
    final Promise<Boolean> p2 = PromiseExtensions.<Boolean>promise(boolean.class);
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        PromiseExtensions.<Boolean>operator_doubleGreaterThan(Boolean.valueOf(true), p2);
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
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(0));
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
  public void testPromiseChaining() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(1));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return PromiseExtensions.<Integer>promise(Integer.valueOf(2));
      }
    };
    final Promise<Integer> p2 = PromiseExtensions.<Integer, Integer>then(p, _function);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(2));
  }
  
  @Test
  public void testLongChain() {
    final AtomicBoolean alwaysDone = new AtomicBoolean();
    final AtomicReference<Throwable> caughtError = new AtomicReference<Throwable>();
    Promise<Integer> _addOne = this.addOne(1);
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then = PromiseExtensions.<Integer, Integer>then(_addOne, _function);
    final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_1 = PromiseExtensions.<Integer, Integer>then(_then, _function_1);
    final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_2 = PromiseExtensions.<Integer, Integer>then(_then_1, _function_2);
    final Function1<Integer, Promise<Integer>> _function_3 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_3 = PromiseExtensions.<Integer, Integer>then(_then_2, _function_3);
    final Function1<Integer, Promise<Integer>> _function_4 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_4 = PromiseExtensions.<Integer, Integer>then(_then_3, _function_4);
    final Function1<Integer, Promise<Integer>> _function_5 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_5 = PromiseExtensions.<Integer, Integer>then(_then_4, _function_5);
    final Function1<Integer, Promise<Integer>> _function_6 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_6 = PromiseExtensions.<Integer, Integer>then(_then_5, _function_6);
    final Function1<Integer, Promise<Integer>> _function_7 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_7 = PromiseExtensions.<Integer, Integer>then(_then_6, _function_7);
    final Procedure1<Throwable> _function_8 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        caughtError.set(it);
      }
    };
    Promise<Integer> _onError = _then_7.onError(_function_8);
    final Procedure1<Promise<Integer>> _function_9 = new Procedure1<Promise<Integer>>() {
      public void apply(final Promise<Integer> it) {
        alwaysDone.set(true);
      }
    };
    Promise<Integer> _always = _onError.always(_function_9);
    StreamAssert.<Integer>assertPromiseEquals(_always, Integer.valueOf(10));
    boolean _get = alwaysDone.get();
    Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(_get));
    Throwable _get_1 = caughtError.get();
    Assert.assertNull(_get_1);
  }
  
  @Test
  public void testLongChainWithError() {
    final AtomicBoolean alwaysDone = new AtomicBoolean();
    final AtomicReference<Throwable> caughtError = new AtomicReference<Throwable>();
    Promise<Integer> _addOne = this.addOne(1);
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then = PromiseExtensions.<Integer, Integer>then(_addOne, _function);
    final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_1 = PromiseExtensions.<Integer, Integer>then(_then, _function_1);
    final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_2 = PromiseExtensions.<Integer, Integer>then(_then_1, _function_2);
    final Function1<Integer, Promise<Integer>> _function_3 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        try {
          if (true) {
            throw new Exception("help!");
          }
          return TestPromise.this.addOne((it).intValue());
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    Promise<Integer> _then_3 = PromiseExtensions.<Integer, Integer>then(_then_2, _function_3);
    final Function1<Integer, Promise<Integer>> _function_4 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_4 = PromiseExtensions.<Integer, Integer>then(_then_3, _function_4);
    final Function1<Integer, Promise<Integer>> _function_5 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_5 = PromiseExtensions.<Integer, Integer>then(_then_4, _function_5);
    final Function1<Integer, Promise<Integer>> _function_6 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_6 = PromiseExtensions.<Integer, Integer>then(_then_5, _function_6);
    final Function1<Integer, Promise<Integer>> _function_7 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    Promise<Integer> _then_7 = PromiseExtensions.<Integer, Integer>then(_then_6, _function_7);
    final Procedure1<Throwable> _function_8 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        caughtError.set(it);
      }
    };
    Promise<Integer> _onError = _then_7.onError(_function_8);
    final Procedure1<Promise<Integer>> _function_9 = new Procedure1<Promise<Integer>>() {
      public void apply(final Promise<Integer> it) {
        alwaysDone.set(true);
      }
    };
    Promise<Integer> _always = _onError.always(_function_9);
    final Procedure1<Integer> _function_10 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        Assert.fail(("should not get here" + it));
      }
    };
    _always.then(_function_10);
    boolean _get = alwaysDone.get();
    Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(_get));
    Throwable _get_1 = caughtError.get();
    Assert.assertNotNull(_get_1);
  }
  
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Async
  private Task addOne(final int n, final Promise<Integer> promise) {
    final Runnable _function = new Runnable() {
      public void run() {
        PromiseExtensions.<Integer>operator_doubleLessThan(promise, Integer.valueOf((n + 1)));
      }
    };
    return PromiseExtensions.run(this.threads, _function);
  }
  
  @Test
  public void testPromiseErrorChaining() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(1));
    final Promise<Boolean> p2 = PromiseExtensions.<Boolean>promise(boolean.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() - 1));
      }
    };
    Promise<Integer> _map = PromiseExtensions.<Integer, Integer>map(p, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((1 / (it).intValue()));
      }
    };
    Promise<Integer> _map_1 = PromiseExtensions.<Integer, Integer>map(_map, _function_1);
    final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    Promise<Integer> _map_2 = PromiseExtensions.<Integer, Integer>map(_map_1, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        PromiseExtensions.<Boolean>operator_doubleGreaterThan(Boolean.valueOf(true), p2);
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
  
  public Promise<Integer> addOne(final int n) {
    final Promise<Integer> promise = new Promise<Integer>();
    try {
    	addOne(n,promise);
    } catch(Throwable t) {
    	promise.error(t);
    } finally {
    	return promise;
    }
  }
  
  public Promise<Integer> addOne(final Executor executor, final int n) {
    final Promise<Integer> promise = new Promise<Integer>();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			addOne(n,promise);
    		} catch(Throwable t) {
    			promise.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return promise;
  }
}
