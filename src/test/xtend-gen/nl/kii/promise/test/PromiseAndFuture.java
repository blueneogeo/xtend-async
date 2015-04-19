package nl.kii.promise.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import nl.kii.async.ExecutorExtensions;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class PromiseAndFuture {
  private final ExecutorService exec = Executors.newCachedThreadPool();
  
  @Test
  public void testFuture() {
    try {
      final Callable<String> _function = new Callable<String>() {
        @Override
        public String call() throws Exception {
          return "hi";
        }
      };
      final FutureTask<String> task = new FutureTask<String>(_function);
      this.exec.submit(task);
      final String result = task.get();
      InputOutput.<String>println(result);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testPromise() {
    try {
      final Callable<Integer> _function = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return Integer.valueOf(10);
        }
      };
      final Promise<Integer> promise = ExecutorExtensions.<Integer>promise(this.exec, _function);
      final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() + 10));
        }
      };
      SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(promise, _function_1);
      final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
        @Override
        public void apply(final Integer it) {
          InputOutput.<Integer>println(it);
        }
      };
      Task _then = _map.then(_function_2);
      final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
        @Override
        public void apply(final Throwable it) {
          InputOutput.<Throwable>println(it);
        }
      };
      PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_3);
      Thread.sleep(100);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
