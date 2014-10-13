package nl.kii.promise.test;

import nl.kii.promise.Promise;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromisePairExtensions {
  @Test
  public void testThenWithPairParams() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Pair<Integer, Integer> to String"
      + "\nType mismatch: cannot convert from String to Integer");
  }
  
  @Test
  public void testAsyncWithPairParams() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from String to int"
      + "\nType mismatch: cannot convert from (Object, String)=>Promise<Integer> to (Pair<Integer, Integer>)=>IPromise<Pair<Integer, Integer>, Integer>");
  }
  
  @Test
  public void testMapWithPairs() {
    throw new Error("Unresolved compilation problems:"
      + "\n* cannot be resolved."
      + "\nType mismatch: cannot convert from Pair<Integer, Integer> to String"
      + "\nType mismatch: cannot convert from Pair<Integer, Integer> to String");
  }
  
  @Test
  public void testAsyncPair() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method call2 is undefined for the type TestPromisePairExtensions"
      + "\nThere is no context to infer the closure\'s argument types from. Consider typing the arguments or put the closures into a typed context."
      + "\ncall2 cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  @Test
  public void testAsyncPairUsingFlatmap() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method call2 is undefined for the type TestPromisePairExtensions"
      + "\nThere is no context to infer the closure\'s argument types from. Consider typing the arguments or put the closures into a typed context."
      + "\ncall2 cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  private Promise<Integer> power2(final int i) {
    return new Promise<Integer>(Integer.valueOf((i * i)));
  }
}
