package nl.kii.stream.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.ExecutorExtensions;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Subscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStream {
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Test
  public void testUnbufferedStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() != 2));
      }
    };
    Stream<Integer> _filter = StreamExtensions.<Integer>filter(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(_filter, _function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(_map, _function_2);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    int _get = counter.get();
    Assert.assertEquals(6, _get);
    StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    int _get_1 = counter.get();
    Assert.assertEquals(8, _get_1);
  }
  
  @Test
  public void testCount() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4))));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    Stream<Integer> _split = StreamExtensions.<Integer>split(_stream, _function);
    Stream<List<Integer>> _collect = StreamExtensions.<Integer>collect(_split);
    Stream<List<List<Integer>>> _collect_1 = StreamExtensions.<List<Integer>>collect(_collect);
    final Procedure1<List<List<Integer>>> _function_1 = new Procedure1<List<List<Integer>>>() {
      public void apply(final List<List<Integer>> it) {
        InputOutput.<List<List<Integer>>>println(it);
      }
    };
    StreamExtensions.<List<List<Integer>>>onEach(_collect_1, _function_1);
  }
  
  @Test
  public void testBufferedStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    Stream<Integer> _stream = new Stream<Integer>();
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(s, _function);
    int _get = counter.get();
    Assert.assertEquals(6, _get);
  }
  
  @Test
  public void testControlledStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    Stream<Integer> _stream = new Stream<Integer>();
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Procedure1<Subscription<Integer>> _function = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            counter.addAndGet((it).intValue());
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s, _function);
    s.next();
    int _get = counter.get();
    Assert.assertEquals(1, _get);
    s.skip();
    int _get_1 = counter.get();
    Assert.assertEquals(1, _get_1);
    s.next();
    int _get_2 = counter.get();
    Assert.assertEquals(1, _get_2);
    s.next();
    int _get_3 = counter.get();
    Assert.assertEquals(5, _get_3);
    s.next();
    int _get_4 = counter.get();
    Assert.assertEquals(10, _get_4);
    s.next();
    int _get_5 = counter.get();
    Assert.assertEquals(10, _get_5);
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(4));
    int _get_6 = counter.get();
    Assert.assertEquals(11, _get_6);
    s.next();
    int _get_7 = counter.get();
    Assert.assertEquals(15, _get_7);
  }
  
  @Test
  public void testControlledChainedBufferedStream() {
    final AtomicInteger result = new AtomicInteger(0);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final Stream<Integer> s1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s1, _function);
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_map, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(5));
    final Stream<Integer> s2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(6));
    final Procedure1<Subscription<Integer>> _function_1 = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            result.set((it).intValue());
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    s2.next();
    int _get = result.get();
    Assert.assertEquals(4, _get);
    s2.next();
    int _get_1 = result.get();
    Assert.assertEquals(5, _get_1);
    s2.next();
    int _get_2 = result.get();
    Assert.assertEquals(6, _get_2);
    s2.next();
    int _get_3 = result.get();
    Assert.assertEquals(1, _get_3);
    s2.next();
    int _get_4 = result.get();
    Assert.assertEquals(2, _get_4);
    s2.next();
    int _get_5 = result.get();
    Assert.assertEquals(3, _get_5);
  }
  
  @Test
  public void testStreamErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final AtomicReference<Throwable> e = new AtomicReference<Throwable>();
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        e.set(it);
      }
    };
    Subscription<Integer> _onError = StreamExtensions.<Integer>onError(s, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(Integer.valueOf((1 / (it).intValue())));
      }
    };
    StreamExtensions.<Integer>onEach(_onError, _function_1);
    StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(0));
    Throwable _get = e.get();
    Assert.assertNotNull(_get);
  }
  
  @Test
  public void testChainedBufferedSkippingStream() {
    final AtomicInteger result = new AtomicInteger(0);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    final Stream<Integer> s1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s1, _function);
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_map, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish_1);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(6));
    final Stream<Integer> s2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(7));
    final Procedure1<Subscription<Integer>> _function_1 = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            result.set((it).intValue());
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    s2.next();
    int _get = result.get();
    Assert.assertEquals(4, _get);
    s2.skip();
    s2.next();
    s2.next();
    int _get_1 = result.get();
    Assert.assertEquals(6, _get_1);
    s2.skip();
    s2.next();
    s2.next();
    int _get_2 = result.get();
    Assert.assertEquals(3, _get_2);
  }
  
  @Test
  public void testParallelHighThroughputStreaming() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(Integer.class);
      final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() * 2));
        }
      };
      final Stream<Integer> s2 = StreamExtensions.<Integer, Integer>map(s, _function);
      final Runnable _function_1 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(0, 999);
          for (final Integer i : _upTo) {
            Value<Integer> _value = new Value<Integer>(Integer.valueOf(1));
            s.apply(_value);
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_1);
      final Runnable _function_2 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(1000, 1999);
          for (final Integer i : _upTo) {
            Value<Integer> _value = new Value<Integer>(Integer.valueOf(2));
            s.apply(_value);
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_2);
      final Runnable _function_3 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(2000, 2999);
          for (final Integer i : _upTo) {
            Value<Integer> _value = new Value<Integer>(Integer.valueOf(3));
            s.apply(_value);
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_3);
      final AtomicInteger sum = new AtomicInteger();
      final Procedure1<Entry<Integer>> _function_4 = new Procedure1<Entry<Integer>>() {
        public void apply(final Entry<Integer> it) {
          boolean _matched = false;
          if (!_matched) {
            if (it instanceof nl.kii.stream.Error) {
              _matched=true;
              InputOutput.<nl.kii.stream.Error<?>>println(((nl.kii.stream.Error<?>)it));
            }
          }
          if (!_matched) {
            if (it instanceof Value) {
              _matched=true;
              sum.addAndGet((((Value<Integer>)it).value).intValue());
            }
          }
          s2.next();
        }
      };
      s2.onChange(_function_4);
      s2.next();
      Thread.sleep(100);
      int _get = sum.get();
      Assert.assertEquals(12000, _get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
