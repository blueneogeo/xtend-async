package nl.kii.stream.test;

import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamSource;
import nl.kii.stream.Subscription;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestSplitter {
  @Atomic
  private final AtomicBoolean _did1 = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _did2 = new AtomicBoolean();
  
  @Test
  public void testCopySplitter() {
    final Stream<Integer> source = StreamExtensions.<Integer>stream(int.class);
    final Stream<Integer> s1 = StreamExtensions.<Integer>stream(int.class);
    final Stream<Integer> s2 = StreamExtensions.<Integer>stream(int.class);
    StreamSource<Integer> _split = StreamExtensions.<Integer>split(source);
    StreamSource<Integer> _pipe = _split.pipe(s1);
    _pipe.pipe(s2);
    final Procedure1<Subscription<Integer>> _function = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            TestSplitter.this.setDid1(Boolean.valueOf(true));
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
            TestSplitter.this.setDid2(Boolean.valueOf(true));
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    StreamExtensions.<Integer>operator_doubleLessThan(source, Integer.valueOf(1));
    Boolean _did1 = this.getDid1();
    Assert.assertFalse((_did1).booleanValue());
    Boolean _did2 = this.getDid2();
    Assert.assertFalse((_did2).booleanValue());
    s1.next();
    Boolean _did1_1 = this.getDid1();
    Assert.assertFalse((_did1_1).booleanValue());
    Boolean _did2_1 = this.getDid2();
    Assert.assertFalse((_did2_1).booleanValue());
    s2.next();
    Boolean _did1_2 = this.getDid1();
    Assert.assertTrue((_did1_2).booleanValue());
    Boolean _did2_2 = this.getDid2();
    Assert.assertTrue((_did2_2).booleanValue());
    this.setDid1(Boolean.valueOf(false));
    this.setDid2(Boolean.valueOf(false));
    StreamExtensions.<Integer>operator_doubleLessThan(source, Integer.valueOf(2));
    Boolean _did1_3 = this.getDid1();
    Assert.assertFalse((_did1_3).booleanValue());
    Boolean _did2_3 = this.getDid2();
    Assert.assertFalse((_did2_3).booleanValue());
    s1.next();
    Boolean _did1_4 = this.getDid1();
    Assert.assertFalse((_did1_4).booleanValue());
    Boolean _did2_4 = this.getDid2();
    Assert.assertFalse((_did2_4).booleanValue());
    s2.next();
    Boolean _did1_5 = this.getDid1();
    Assert.assertTrue((_did1_5).booleanValue());
    Boolean _did2_5 = this.getDid2();
    Assert.assertTrue((_did2_5).booleanValue());
  }
  
  @Test
  public void testBalancer() {
    final Stream<Integer> source = StreamExtensions.<Integer>stream(int.class);
    final Stream<Integer> s1 = StreamExtensions.<Integer>stream(int.class);
    final Stream<Integer> s2 = StreamExtensions.<Integer>stream(int.class);
    StreamSource<Integer> _balance = StreamExtensions.<Integer>balance(source);
    StreamSource<Integer> _pipe = _balance.pipe(s1);
    _pipe.pipe(s2);
    final Procedure1<Subscription<Integer>> _function = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            TestSplitter.this.setDid1(Boolean.valueOf(true));
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
            TestSplitter.this.setDid2(Boolean.valueOf(true));
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    this.setDid1(Boolean.valueOf(false));
    this.setDid2(Boolean.valueOf(false));
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(source, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Boolean _did1 = this.getDid1();
    Assert.assertFalse((_did1).booleanValue());
    Boolean _did2 = this.getDid2();
    Assert.assertFalse((_did2).booleanValue());
    s1.next();
    Boolean _did1_1 = this.getDid1();
    Assert.assertTrue((_did1_1).booleanValue());
    Boolean _did2_1 = this.getDid2();
    Assert.assertFalse((_did2_1).booleanValue());
    s2.next();
    Boolean _did1_2 = this.getDid1();
    Assert.assertTrue((_did1_2).booleanValue());
    Boolean _did2_2 = this.getDid2();
    Assert.assertTrue((_did2_2).booleanValue());
    this.setDid1(Boolean.valueOf(false));
    this.setDid2(Boolean.valueOf(false));
    s2.next();
    Boolean _did1_3 = this.getDid1();
    Assert.assertFalse((_did1_3).booleanValue());
    Boolean _did2_3 = this.getDid2();
    Assert.assertTrue((_did2_3).booleanValue());
    s2.next();
  }
  
  private Boolean setDid1(final Boolean value) {
    return this._did1.getAndSet(value);
  }
  
  private Boolean getDid1() {
    return this._did1.get();
  }
  
  private Boolean setDid2(final Boolean value) {
    return this._did2.getAndSet(value);
  }
  
  private Boolean getDid2() {
    return this._did2.get();
  }
}
