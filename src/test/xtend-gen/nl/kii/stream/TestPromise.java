package nl.kii.stream;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromise {
  @Test
  public void testPromisedAfter() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    p.then(_function);
    p.apply(Integer.valueOf(10));
  }
  
  @Test
  public void testPromisedBefore() {
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.class);
    p.apply(Integer.valueOf(10));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    p.then(_function);
  }
}
