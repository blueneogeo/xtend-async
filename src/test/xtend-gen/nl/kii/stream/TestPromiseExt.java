package nl.kii.stream;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.StreamAssert;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromiseExt {
  @Test
  public void testMap() {
    Promise<Integer> _promise = PromiseExt.<Integer>promise(Integer.class);
    final Promise<Integer> p = PromiseExt.<Integer>operator_doubleLessThan(_promise, Integer.valueOf(4));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 10));
      }
    };
    final Promise<Integer> mapped = PromiseExt.<Integer, Integer>map(p, _function);
    StreamAssert.<Integer>assertPromiseEquals(mapped, Integer.valueOf(14));
  }
  
  @Test
  public void testFlatten() {
    Promise<Integer> _promise = PromiseExt.<Integer>promise(Integer.class);
    final Promise<Integer> p1 = PromiseExt.<Integer>operator_doubleLessThan(_promise, Integer.valueOf(3));
    Promise<Promise<Integer>> _promise_1 = new Promise<Promise<Integer>>();
    final Promise<Promise<Integer>> p2 = PromiseExt.<Promise<Integer>>operator_doubleLessThan(_promise_1, p1);
    final Promise<Integer> flattened = PromiseExt.<Integer>flatten(p2);
    StreamAssert.<Integer>assertPromiseEquals(flattened, Integer.valueOf(3));
  }
  
  @Test
  public void testAsync() {
    Promise<Integer> _promise = PromiseExt.<Integer>promise(Integer.class);
    final Promise<Integer> s = PromiseExt.<Integer>operator_doubleLessThan(_promise, Integer.valueOf(2));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromiseExt.this.power2((it).intValue());
      }
    };
    final Promise<Integer> asynced = PromiseExt.<Integer, Integer>async(s, _function);
    StreamAssert.<Integer>assertPromiseEquals(asynced, Integer.valueOf(4));
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExt.<Integer>promise(Integer.valueOf((i * i)));
  }
}
