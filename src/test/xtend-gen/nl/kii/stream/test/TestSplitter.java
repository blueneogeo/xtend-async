package nl.kii.stream.test;

import nl.kii.stream.CopySplitter;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Subscription;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestSplitter {
  @Test
  public void testCopySplitter() {
    final Stream<Integer> source = StreamExtensions.<Integer>stream(int.class);
    final CopySplitter<Integer> splitter = new CopySplitter<Integer>(source);
    final Stream<Integer> s1 = splitter.stream();
    final Stream<Integer> s2 = splitter.stream();
    final Procedure1<Subscription<Integer>> _function = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            InputOutput.<String>println(("s1: " + it));
            s1.next();
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s1, _function);
    final Procedure1<Subscription<Integer>> _function_1 = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            InputOutput.<String>println(("s2: " + it));
            s2.next();
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    s1.next();
    s2.next();
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(source, Integer.valueOf(1));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
  }
}
