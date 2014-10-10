package nl.kii.stream.test;

import nl.kii.promise.Promise;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamPairExtensions;
import nl.kii.stream.SubStream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamPairExt {
  @Test
  public void testEachWithPairParams() {
    Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Stream<Pair<Integer, Integer>> p = StreamExtensions.<Pair<Integer, Integer>>datastream(_mappedTo);
    final Stream<Integer> p2 = StreamExtensions.<Integer>stream(int.class);
    final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
      public void apply(final Integer k, final Integer v) {
        StreamExtensions.<Integer, Integer>operator_doubleLessThan(p2, Integer.valueOf(((k).intValue() + (v).intValue())));
      }
    };
    StreamPairExtensions.<Integer, Integer>onEach(p, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    StreamAssert.<Integer, Integer>assertStreamContains(p2, _value);
  }
  
  @Test
  public void testAsyncWithPairParams() {
    Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Stream<Pair<Integer, Integer>> p = StreamExtensions.<Pair<Integer, Integer>>datastream(_mappedTo);
    final Function2<Integer, Integer, Promise<Integer>> _function = new Function2<Integer, Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer a, final Integer b) {
        return TestStreamPairExt.this.power2(((a).intValue() + (b).intValue()));
      }
    };
    final SubStream<Promise<Integer>, Integer> asynced = StreamPairExtensions.<Integer, Integer, Integer, Promise<Integer>>call(p, _function);
    Value<Promise<Integer>, Integer> _value = StreamAssert.<Promise<Integer>, Integer>value(Integer.valueOf(9));
    Finish<Promise<Integer>, Integer> _finish = StreamExtensions.<Promise<Integer>, Integer>finish();
    StreamAssert.<Promise<Integer>, Integer>assertStreamContains(asynced, _value, _finish);
  }
  
  @Test
  public void testMapWithPairs() {
    throw new Error("Unresolved compilation problems:"
      + "\n* cannot be resolved."
      + "\nType mismatch: cannot convert from (Object, String)=>Pair<Object, Object> to (Pair<Integer, Integer>)=>Pair<Integer, Integer>");
  }
  
  @Test
  public void testAsyncPair() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method resolveValue is undefined for the type TestStreamPairExt"
      + "\nThere is no context to infer the closure\'s argument types from. Consider typing the arguments or put the closures into a typed context."
      + "\nmap cannot be resolved"
      + "\nresolveValue cannot be resolved"
      + "\nassertStreamContains cannot be resolved");
  }
  
  private Promise<Integer> power2(final int i) {
    return new Promise<Integer>(Integer.valueOf((i * i)));
  }
}
