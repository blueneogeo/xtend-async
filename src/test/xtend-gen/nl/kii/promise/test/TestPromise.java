package nl.kii.promise.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Async;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
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
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(0));
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
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(0));
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
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(1));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return new Promise<Integer>(Integer.valueOf(2));
      }
    };
    Promise<Promise<Integer>> _map = PromiseExtensions.<Integer, Promise<Integer>>map(p, _function);
    final Promise<Integer> p2 = PromiseExtensions.<Integer, Promise<Integer>>resolve(_map);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(2));
  }
  
  @Test
  public void testTaskChain() {
    Task _sayHello = this.sayHello();
    final Function1<Boolean, Task> _function = new Function1<Boolean, Task>() {
      public Task apply(final Boolean it) {
        return TestPromise.this.sayHello();
      }
    };
    Promise<Task> _map = PromiseExtensions.<Boolean, Task>map(_sayHello, _function);
    Promise<Boolean> _resolve = PromiseExtensions.<Boolean, Task>resolve(_map);
    final Function1<Boolean, Task> _function_1 = new Function1<Boolean, Task>() {
      public Task apply(final Boolean it) {
        return TestPromise.this.sayHello();
      }
    };
    Promise<Task> _map_1 = PromiseExtensions.<Boolean, Task>map(_resolve, _function_1);
    Promise<Boolean> _resolve_1 = PromiseExtensions.<Boolean, Task>resolve(_map_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromise.this.sayHello();
      }
    };
    _resolve_1.then(_function_2);
  }
  
  @Test
  public void testLongChain() {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <Integer>");
  }
  
  @Test
  public void testLongChainWithError() {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <Integer>");
  }
  
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Async
  public IPromise<IPromise<Integer>> addOne(final int n, final Promise<Integer> promise) {
    final Callable<IPromise<Integer>> _function = new Callable<IPromise<Integer>>() {
      public IPromise<Integer> call() throws Exception {
        return PromiseExtensions.<Integer>operator_doubleLessThan(promise, Integer.valueOf((n + 1)));
      }
    };
    return ExecutorExtensions.<IPromise<Integer>>promise(this.threads, _function);
  }
  
  @Async
  public IPromise<Task> sayHello(final Task task) {
    final Callable<Task> _function = new Callable<Task>() {
      public Task call() throws Exception {
        Task _xblockexpression = null;
        {
          InputOutput.<String>println("hello");
          _xblockexpression = task.complete();
        }
        return _xblockexpression;
      }
    };
    return ExecutorExtensions.<Task>promise(this.threads, _function);
  }
  
  @Test
  public void testPromiseErrorChaining() {
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(1));
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
    IPromise<Integer> _onError = _map_2.onError(_function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    _onError.then(_function_4);
    StreamAssert.<Boolean>assertPromiseEquals(p2, Boolean.valueOf(true));
  }
  
  @Test
  public void testPromiseChain() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    Promise<Integer> _map = PromiseExtensions.<Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<Boolean>println(it);
      }
    };
    Task _then_1 = _then.then(_function_2);
    final Procedure1<Boolean> _function_3 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<Boolean>println(it);
      }
    };
    _then_1.then(_function_3);
    p.set(Integer.valueOf(1));
  }
  
  @Atomic
  private final AtomicBoolean _foundError = new AtomicBoolean();
  
  @Test
  public void testPromiseWithLaterError2() {
    this.setFoundError(Boolean.valueOf(false));
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    Promise<Integer> _map = PromiseExtensions.<Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        Assert.fail("it/0 should not succeed");
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestPromise.this.setFoundError(Boolean.valueOf(true));
      }
    };
    _then.onError(_function_2);
    p.set(Integer.valueOf(1));
    Boolean _foundError = this.getFoundError();
    Assert.assertTrue((_foundError).booleanValue());
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
  
  public Task sayHello() {
    final Task task = new Task();
    try {
    	sayHello(task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  private Boolean setFoundError(final Boolean value) {
    return this._foundError.getAndSet(value);
  }
  
  private Boolean getFoundError() {
    return this._foundError.get();
  }
}
