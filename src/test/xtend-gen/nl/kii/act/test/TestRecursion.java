package nl.kii.act.test;

import nl.kii.act.test.Call2;
import nl.kii.act.test.Result;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.junit.Test;

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
@SuppressWarnings("all")
public class TestRecursion {
  @Test
  public void testRecursion() {
    Integer _loop = this.loop(1000000);
    InputOutput.<Integer>println(_loop);
  }
  
  public Integer loop(final int loops) {
    try {
      int input = loops;
      int result = 0;
      while ((loops > 0)) {
        try {
          this.count(input, result);
        } catch (final Throwable _t) {
          if (_t instanceof Call2) {
            final Call2 call = (Call2)_t;
            input = call.input;
            result = call.result;
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
    } catch (final Throwable _t) {
      if (_t instanceof Result) {
        final Result result_1 = (Result)_t;
        return Integer.valueOf(result_1.result);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return null;
  }
  
  public void count(final int left, final int sum) throws Call2, Result {
    if ((left == 0)) {
      throw new Result(sum);
    } else {
      throw new Call2((left - 1), (sum + 1));
    }
  }
}
