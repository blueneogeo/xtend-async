package nl.kii.act.test;

import nl.kii.act.test.Call2;
import nl.kii.act.test.Result;

/**
 * There are ways in which you can exchange stack for heap.
 * 
 * Instead of making a recursive call within a function,
 * have it return a lazy datastructure that makes the call when evaluated.
 * 
 * You can then unwind the "stack" with Java's for-construct.
 * 
 * http://stackoverflow.com/questions/860550/stack-overflows-from-deep-recursion-in-java/861385#861385
 * 
 * Other way: throw an Exception!!!
 */
public class TestRecursion {
  /* @Test
   */public void testRecursion() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method println is undefined for the type TestRecursion");
  }
  
  public int loop(final int loops) {
    throw new Error("Unresolved compilation problems:"
      + "\n> cannot be resolved."
      + "\nNo exception of type Call2 can be thrown; an exception type must be a subclass of Throwable"
      + "\nNo exception of type Result can be thrown; an exception type must be a subclass of Throwable");
  }
  
  public void count(final int left, final int sum) throws Call2, Result {
    throw new Error("Unresolved compilation problems:"
      + "\n== cannot be resolved."
      + "\n- cannot be resolved."
      + "\n+ cannot be resolved.");
  }
}
