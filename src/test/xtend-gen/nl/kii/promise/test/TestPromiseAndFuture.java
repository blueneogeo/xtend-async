package nl.kii.promise.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import nl.kii.async.ExecutorExtensions;
import nl.kii.promise.Promise;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromiseAndFuture {
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
      Assert.assertEquals("hi", result);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testPromise() {
    try {
      final Callable<String> _function = new Callable<String>() {
        @Override
        public String call() throws Exception {
          return "hi";
        }
      };
      final Promise<String> promise = ExecutorExtensions.<String>promise(this.exec, _function);
      Future<String> _future = ExecutorExtensions.<String, String>future(promise);
      final String result = _future.get();
      Assert.assertEquals("hi", result);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
