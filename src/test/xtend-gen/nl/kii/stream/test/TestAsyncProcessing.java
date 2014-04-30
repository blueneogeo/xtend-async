package nl.kii.stream.test;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExt;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAsyncProcessing {
  @Test
  public void testSimpleAsyncPromise() {
    try {
      Promise<Function0<Integer>> _power2 = this.power2(2);
      final Procedure1<Function0<Integer>> _function = new Procedure1<Function0<Integer>>() {
        public void apply(final Function0<Integer> it) {
          InputOutput.<String>println(("result: " + it));
        }
      };
      _power2.then(_function);
      InputOutput.<String>println("we are done immediately, and then the result of 4 comes in a second later");
      Thread.sleep(2100);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testDoubleAsyncPromise() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from Function0<Integer> to int");
  }
  
  @Test
  public void testAsyncMapping() {
    try {
      Stream<Integer> _stream = StreamExt.<Integer>stream(int.class);
      Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
      final Function1<Integer, Promise<Function0<Integer>>> _function = new Function1<Integer, Promise<Function0<Integer>>>() {
        public Promise<Function0<Integer>> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Stream<Function0<Integer>> _async = StreamExt.<Integer, Function0<Integer>>async(s, _function);
      final Procedure1<Function0<Integer>> _function_1 = new Procedure1<Function0<Integer>>() {
        public void apply(final Function0<Integer> it) {
          InputOutput.<String>println(("result: " + it));
        }
      };
      StreamExt.<Function0<Integer>>each(_async, _function_1);
      Thread.sleep(4000);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<Function0<Integer>> power2(final int i) {
    final Function0<Integer> _function = new Function0<Integer>() {
      public Integer apply() {
        try {
          Thread.sleep(1000);
          return (i * i);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return PromiseExt.<Function0<Integer>>promise(_function);
  }
}
