package nl.kii.stream.test;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExtensions;
import nl.kii.stream.PromisePairExtensions;
import nl.kii.stream.StreamAssert;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromisePairExtensions {
  @Test
  public void testThenWithPairParams() {
    Pair<Class<Integer>, Class<Integer>> _mappedTo = Pair.<Class<Integer>, Class<Integer>>of(int.class, int.class);
    Promise<Pair<Integer, Integer>> _promisePair = PromisePairExtensions.<Integer, Integer>promisePair(_mappedTo);
    Pair<Integer, Integer> _mappedTo_1 = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Promise<Pair<Integer, Integer>> p = PromiseExtensions.<Pair<Integer, Integer>>operator_doubleLessThan(_promisePair, _mappedTo_1);
    final Promise<Integer> p2 = PromiseExtensions.<Integer>promise(Integer.class);
    final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
      public void apply(final Integer k, final Integer v) {
        PromiseExtensions.<Integer>operator_doubleLessThan(p2, Integer.valueOf(((k).intValue() + (v).intValue())));
      }
    };
    PromisePairExtensions.<Integer, Integer>then(p, _function);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(3));
  }
  
  @Test
  public void testAsyncWithPairParams() {
    Pair<Class<Integer>, Class<Integer>> _mappedTo = Pair.<Class<Integer>, Class<Integer>>of(int.class, int.class);
    Promise<Pair<Integer, Integer>> _promisePair = PromisePairExtensions.<Integer, Integer>promisePair(_mappedTo);
    Pair<Integer, Integer> _mappedTo_1 = Pair.<Integer, Integer>of(Integer.valueOf(1), Integer.valueOf(2));
    final Promise<Pair<Integer, Integer>> p = PromiseExtensions.<Pair<Integer, Integer>>operator_doubleLessThan(_promisePair, _mappedTo_1);
    final Function2<Integer, Integer, Promise<Integer>> _function = new Function2<Integer, Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer a, final Integer b) {
        return TestPromisePairExtensions.this.power2(((a).intValue() + (b).intValue()));
      }
    };
    Promise<Promise<Integer>> _map = PromisePairExtensions.<Integer, Integer, Promise<Integer>>map(p, _function);
    final Promise<Integer> asynced = PromiseExtensions.<Integer>resolve(_map);
    StreamAssert.<Integer>assertPromiseEquals(asynced, Integer.valueOf(9));
  }
  
  @Test
  public void testMapWithPairs() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(2));
    final Function1<Integer, Pair<Integer, Integer>> _function = new Function1<Integer, Pair<Integer, Integer>>() {
      public Pair<Integer, Integer> apply(final Integer it) {
        return Pair.<Integer, Integer>of(it, Integer.valueOf(((it).intValue() * (it).intValue())));
      }
    };
    Promise<Pair<Integer, Integer>> _map = PromiseExtensions.<Integer, Pair<Integer, Integer>>map(p, _function);
    final Function2<Integer, Integer, Pair<Integer, Integer>> _function_1 = new Function2<Integer, Integer, Pair<Integer, Integer>>() {
      public Pair<Integer, Integer> apply(final Integer key, final Integer value) {
        return Pair.<Integer, Integer>of(key, Integer.valueOf((((key).intValue() + (value).intValue()) * ((key).intValue() + (value).intValue()))));
      }
    };
    final Promise<Pair<Integer, Integer>> asynced = PromisePairExtensions.<Integer, Integer, Pair<Integer, Integer>>map(_map, _function_1);
    Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(2), Integer.valueOf(36));
    StreamAssert.<Pair<Integer, Integer>>assertPromiseEquals(asynced, _mappedTo);
  }
  
  @Test
  public void testAsyncPair() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(2));
    final Function1<Integer, Pair<Integer, Promise<Integer>>> _function = new Function1<Integer, Pair<Integer, Promise<Integer>>>() {
      public Pair<Integer, Promise<Integer>> apply(final Integer it) {
        Promise<Integer> _promise = PromiseExtensions.<Integer>promise(it);
        return Pair.<Integer, Promise<Integer>>of(it, _promise);
      }
    };
    Promise<Pair<Integer, Promise<Integer>>> _map = PromiseExtensions.<Integer, Pair<Integer, Promise<Integer>>>map(p, _function);
    Promise<Pair<Integer, Integer>> _resolvePair = PromisePairExtensions.<Integer, Integer>resolvePair(_map);
    final Function2<Integer, Integer, Pair<Integer, Promise<Integer>>> _function_1 = new Function2<Integer, Integer, Pair<Integer, Promise<Integer>>>() {
      public Pair<Integer, Promise<Integer>> apply(final Integer key, final Integer value) {
        Promise<Integer> _power2 = TestPromisePairExtensions.this.power2((value).intValue());
        return Pair.<Integer, Promise<Integer>>of(key, _power2);
      }
    };
    Promise<Pair<Integer, Promise<Integer>>> _map_1 = PromisePairExtensions.<Integer, Integer, Pair<Integer, Promise<Integer>>>map(_resolvePair, _function_1);
    final Promise<Pair<Integer, Integer>> asynced = PromisePairExtensions.<Integer, Integer>resolvePair(_map_1);
    Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(2), Integer.valueOf(4));
    StreamAssert.<Pair<Integer, Integer>>assertPromiseEquals(asynced, _mappedTo);
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExtensions.<Integer>promise(Integer.valueOf((i * i)));
  }
}
