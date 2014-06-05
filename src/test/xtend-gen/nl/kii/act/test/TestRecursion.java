package nl.kii.act.test;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
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
 */
@SuppressWarnings("all")
public class TestRecursion {
  @Test
  public void testRecursion() {
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.println();
      }
    };
    this.perform(((int[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))), int.class)), _function);
  }
  
  public void perform(final int[] inbox, final Procedure1<? super Integer> processor) {
    boolean _isEmpty = IterableExtensions.isEmpty(((Iterable<?>)Conversions.doWrapArray(inbox)));
    if (_isEmpty) {
      return;
    }
    int[] _clone = inbox.clone();
    int _length = inbox.length;
    int _minus = (_length - 1);
    List<Integer> _subList = ((List<Integer>)Conversions.doWrapArray(_clone)).subList(1, _minus);
    this.perform(((int[])Conversions.unwrapArray(_subList, int.class)), processor);
  }
}
