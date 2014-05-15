package nl.kii.stream.test;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAsyncProcessing {
  @Test
  public void testSimpleAsyncPromise() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Function0<Integer> to int");
  }
  
  @Test
  public void testTripleAsyncPromise() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from Function0<Integer> to int"
      + "\nType mismatch: cannot convert from Function0<Integer> to int"
      + "\nType mismatch: cannot convert implicit first argument from Function0<Integer> to int");
  }
  
  @Test
  public void testAsyncMapping() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Function0<Integer> to Integer"
      + "\nType mismatch: cannot convert from int to String"
      + "\nType mismatch: cannot convert implicit first argument from String to int");
  }
  
  @Test
  public void testAsyncErrorCatching() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from int to String"
      + "\nType mismatch: cannot convert implicit first argument from String to int");
  }
  
  public Promise<Function0<Integer>> power2(final int i) {
    final Function0<Integer> _function = new Function0<Integer>() {
      public Integer apply() {
        try {
          Thread.sleep(100);
          return (i * i);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return PromiseExtensions.<Function0<Integer>>promise(_function);
  }
  
  public Promise<Function0<Integer>> throwsError(final int i) {
    final Function0<Integer> _function = new Function0<Integer>() {
      public Integer apply() {
        try {
          Thread.sleep(100);
          if (true) {
            throw new Exception("something went wrong");
          }
          return (i * i);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return PromiseExtensions.<Function0<Integer>>promise(_function);
  }
}
