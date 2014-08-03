package nl.kii.async.annotation.test;

import com.google.common.base.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.async.annotation.Async;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAsyncAnnotation {
  @Test
  public void testAsyncPromise() {
    final AtomicInteger result = new AtomicInteger();
    Promise<Integer> _increment = this.increment(5);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        result.set((it).intValue());
      }
    };
    _increment.then(_function);
    int _get = result.get();
    Assert.assertEquals(6, _get);
  }
  
  @Test
  public void testAsyncTask() {
    final AtomicBoolean result = new AtomicBoolean();
    Task _printHello = this.printHello("world");
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        result.set((it).booleanValue());
      }
    };
    _printHello.then(_function);
    boolean _get = result.get();
    Assert.assertTrue(_get);
  }
  
  @Test
  public void testAsyncErrorHandling() {
    final AtomicBoolean isError = new AtomicBoolean();
    Task _printHello = this.printHello(null);
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        isError.set(true);
      }
    };
    Promise<Boolean> _onError = _printHello.onError(_function);
    final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        isError.set(false);
      }
    };
    _onError.then(_function_1);
    boolean _get = isError.get();
    Assert.assertTrue(_get);
  }
  
  @Test
  public void testAsyncTaskOnExecutor() {
    try {
      final AtomicBoolean success = new AtomicBoolean();
      final ExecutorService exec = Executors.newCachedThreadPool();
      Task _printHello = this.printHello(exec, "christian");
      final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
        public void apply(final Boolean it) {
          success.set(true);
        }
      };
      _printHello.then(_function);
      Thread.sleep(10);
      boolean _get = success.get();
      Assert.assertTrue(_get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Async
  public Promise<Integer> increment(final int number, final Promise<Integer> promise) {
    return PromiseExtensions.<Integer>operator_doubleLessThan(promise, Integer.valueOf((number + 1)));
  }
  
  @Async
  public Task printHello(final Task task, final String name) {
    try {
      Task _xblockexpression = null;
      {
        boolean _equals = Objects.equal(name, null);
        if (_equals) {
          throw new Exception("name cannot be empty");
        }
        InputOutput.<String>println(("hello " + name));
        _xblockexpression = task.complete();
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<Integer> increment(final int number) {
    final Promise<Integer> promise = new Promise<Integer>();
    try {
    	increment(number,promise);
    } catch(Throwable t) {
    	promise.error(t);
    } finally {
    	return promise;
    }
  }
  
  public Promise<Integer> increment(final Executor executor, final int number) {
    final Promise<Integer> promise = new Promise<Integer>();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			increment(number,promise);
    		} catch(Throwable t) {
    			promise.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return promise;
  }
  
  public Task printHello(final String name) {
    final Task task = new Task();
    try {
    	printHello(task,name);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  public Task printHello(final Executor executor, final String name) {
    final Task task = new Task();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			printHello(task,name);
    		} catch(Throwable t) {
    			task.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return task;
  }
}