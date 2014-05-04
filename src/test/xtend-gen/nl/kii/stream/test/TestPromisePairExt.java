package nl.kii.stream.test;

import com.google.common.collect.Lists;
import java.util.Collections;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExt;
import nl.kii.stream.StreamPairExt;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromisePairExt {
  @Test
  public void testEachWithPairParams() {
    Pair<Integer,Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Stream<Pair<Integer,Integer>> p = StreamExt.<Pair<Integer,Integer>>stream(_mappedTo);
    final Stream<Integer> p2 = StreamExt.<Integer>stream(int.class);
    final Procedure2<Integer,Integer> _function = new Procedure2<Integer,Integer>() {
      public void apply(final Integer k, final Integer v) {
        StreamExt.<Integer>operator_doubleLessThan(p2, Integer.valueOf(((k).intValue() + (v).intValue())));
      }
    };
    StreamPairExt.<Integer, Integer>each(p, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(3));
    StreamAssert.<Integer>assertStreamEquals(p2, Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value)));
  }
  
  @Test
  public void testAsyncWithPairParams() {
    Pair<Integer,Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Stream<Pair<Integer,Integer>> p = StreamExt.<Pair<Integer,Integer>>stream(_mappedTo);
    final Function2<Integer,Integer,Promise<Integer>> _function = new Function2<Integer,Integer,Promise<Integer>>() {
      public Promise<Integer> apply(final Integer a, final Integer b) {
        return TestPromisePairExt.this.power2(((a).intValue() + (b).intValue()));
      }
    };
    final Stream<Integer> asynced = StreamPairExt.<Integer, Integer, Integer>async(p, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(9));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    StreamAssert.<Integer>assertStreamEquals(asynced, Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _finish)));
  }
  
  @Test
  public void testMapWithPairs() {
    final Stream<Integer> p = StreamExt.<Integer>stream(Integer.valueOf(2));
    final Function1<Integer,Pair<Integer,Integer>> _function = new Function1<Integer,Pair<Integer,Integer>>() {
      public Pair<Integer,Integer> apply(final Integer it) {
        return Pair.<Integer, Integer>of(it, Integer.valueOf(((it).intValue() * (it).intValue())));
      }
    };
    Stream<Pair<Integer,Integer>> _map = StreamExt.<Integer, Pair<Integer,Integer>>map(p, _function);
    final Function2<Integer,Integer,Pair<Integer,Integer>> _function_1 = new Function2<Integer,Integer,Pair<Integer,Integer>>() {
      public Pair<Integer,Integer> apply(final Integer key, final Integer value) {
        return Pair.<Integer, Integer>of(key, Integer.valueOf((((key).intValue() + (value).intValue()) * ((key).intValue() + (value).intValue()))));
      }
    };
    final Stream<Pair<Integer,Integer>> asynced = StreamPairExt.<Integer, Integer, Integer, Integer>mapToPair(_map, _function_1);
    Pair<Integer,Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(2), Integer.valueOf(36));
    Value<Pair<Integer,Integer>> _value = StreamAssert.<Pair<Integer,Integer>>value(_mappedTo);
    StreamAssert.<Pair<Integer,Integer>>assertStreamEquals(asynced, Collections.<Value<Pair<Integer, Integer>>>unmodifiableList(Lists.<Value<Pair<Integer, Integer>>>newArrayList(_value)));
  }
  
  @Test
  public void testAsyncPair() {
    final Stream<Integer> p = StreamExt.<Integer>stream(Integer.valueOf(2));
    final Function1<Integer,Pair<Integer,Promise<Integer>>> _function = new Function1<Integer,Pair<Integer,Promise<Integer>>>() {
      public Pair<Integer,Promise<Integer>> apply(final Integer it) {
        Promise<Integer> _promise = PromiseExt.<Integer>promise(it);
        return Pair.<Integer, Promise<Integer>>of(it, _promise);
      }
    };
    Stream<Pair<Integer,Integer>> _asyncToPair = StreamPairExt.<Integer, Integer, Integer>asyncToPair(p, _function);
    final Function2<Integer,Integer,Pair<Integer,Promise<Integer>>> _function_1 = new Function2<Integer,Integer,Pair<Integer,Promise<Integer>>>() {
      public Pair<Integer,Promise<Integer>> apply(final Integer key, final Integer value) {
        Promise<Integer> _power2 = TestPromisePairExt.this.power2((value).intValue());
        return Pair.<Integer, Promise<Integer>>of(key, _power2);
      }
    };
    final Stream<Pair<Integer,Integer>> asynced = StreamPairExt.<Integer, Integer, Integer, Integer>asyncToPair(_asyncToPair, _function_1);
    Pair<Integer,Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(2), Integer.valueOf(4));
    Value<Pair<Integer,Integer>> _value = StreamAssert.<Pair<Integer,Integer>>value(_mappedTo);
    Finish<Pair<Integer,Integer>> _finish = StreamExt.<Pair<Integer,Integer>>finish();
    StreamAssert.<Pair<Integer,Integer>>assertStreamEquals(asynced, Collections.<Entry<Pair<Integer, Integer>>>unmodifiableList(Lists.<Entry<Pair<Integer, Integer>>>newArrayList(_value, _finish)));
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExt.<Integer>promise(Integer.valueOf((i * i)));
  }
}
