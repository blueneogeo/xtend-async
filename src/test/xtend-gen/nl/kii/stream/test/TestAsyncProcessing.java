package nl.kii.stream.test;

import nl.kii.stream.Promise;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAsyncProcessing {
  @Test
  public void testSimpleAsyncPromise() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Object to int");
  }
  
  @Test
  public void testTripleAsyncPromise() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from Object to int"
      + "\nType mismatch: cannot convert from Object to int"
      + "\nType mismatch: cannot convert implicit first argument from Object to int");
  }
  
  @Test
  public void testAsyncMapping() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from int to String"
      + "\nType mismatch: cannot convert implicit first argument from String to int"
      + "\nType mismatch: cannot convert from Object to Integer");
  }
  
  @Test
  public void testAsyncErrorCatching() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from String to int"
      + "\nType mismatch: cannot convert from int to String");
  }
  
  public Promise<Object> power2(final int i) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from ()=>int to Promise<Promise<Object>>");
  }
  
  public Promise<Object> throwsError(final int i) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from ()=>int to Promise<Promise<Object>>");
  }
}
