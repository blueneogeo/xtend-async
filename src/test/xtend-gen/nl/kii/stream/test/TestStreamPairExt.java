package nl.kii.stream.test;

import nl.kii.promise.Promise;
import nl.kii.promise.Task;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamPairExt {
  @Test
  public void testMapWithRoot() {
    final Stream<Integer> p = StreamExtensions.<Integer>datastream(Integer.valueOf(2));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() * (it).intValue()));
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(p, _function);
    final Function2<Integer, Integer, Integer> _function_1 = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer key, final Integer value) {
        return Integer.valueOf((((key).intValue() + (value).intValue()) * ((key).intValue() + (value).intValue())));
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure2<Integer, Integer> _function_2 = new Procedure2<Integer, Integer>() {
      public void apply(final Integer r, final Integer it) {
        Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(r, it);
        InputOutput.<Pair<Integer, Integer>>println(_mappedTo);
      }
    };
    final Task asynced = StreamExtensions.<Integer, Integer>onEach(_map_1, _function_2);
  }
  
  @Test
  public void testAsyncPair() {
    final Stream<Integer> p = StreamExtensions.<Integer>datastream(Integer.valueOf(2));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return new Promise<Integer>(it);
      }
    };
    SubStream<Integer, Promise<Integer>> _map = StreamExtensions.<Integer, Integer, Promise<Integer>>map(p, _function);
    SubStream<Integer, Integer> _resolve = StreamExtensions.<Integer, Integer, Integer>resolve(_map);
    final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestStreamPairExt.this.power2((it).intValue());
      }
    };
    SubStream<Integer, Promise<Integer>> _map_1 = StreamExtensions.<Integer, Integer, Promise<Integer>>map(_resolve, _function_1);
    SubStream<Integer, Integer> _resolve_1 = StreamExtensions.<Integer, Integer, Integer>resolve(_map_1);
    final Procedure2<Integer, Integer> _function_2 = new Procedure2<Integer, Integer>() {
      public void apply(final Integer r, final Integer it) {
        Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(r, it);
        InputOutput.<Pair<Integer, Integer>>println(_mappedTo);
      }
    };
    final Task asynced = StreamExtensions.<Integer, Integer>onEach(_resolve_1, _function_2);
  }
  
  private Promise<Integer> power2(final int i) {
    return new Promise<Integer>(Integer.valueOf((i * i)));
  }
}
