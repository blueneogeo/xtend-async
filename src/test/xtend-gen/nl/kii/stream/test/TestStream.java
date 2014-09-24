package nl.kii.stream.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamObserver;
import nl.kii.stream.StreamResponder;
import nl.kii.stream.Value;
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
  public void testObservingAStream() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    s.monitor(new StreamMonitor() {
      public void onNext() {
        InputOutput.<String>println("next!");
      }
      
      public void onSkip() {
        InputOutput.<String>println("skip!");
      }
      
      public void onClose() {
        InputOutput.<String>println("close!");
      }
      
      public void onOverflow(final Entry<?> entry) {
        InputOutput.<String>println(("overflow! of " + entry));
      }
    });
    s.observe(new StreamObserver<Integer>() {
      public void onValue(final Integer value) {
        try {
          InputOutput.<String>println(("value: " + value));
          if (((value).intValue() == 2)) {
            throw new Exception("boo!");
          }
          s.next();
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
      
      public boolean onError(final Throwable t) {
        boolean _xblockexpression = false;
        {
          InputOutput.<String>println(("error:" + t));
          s.next();
          _xblockexpression = true;
        }
        return _xblockexpression;
      }
      
      public void onFinish(final int level) {
        InputOutput.<String>println("finished");
        s.next();
      }
      
      public void onClosed() {
        InputOutput.<String>println("closed");
      }
    });
    s.next();
  }
  
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
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Test
  public void testControlledStream() {
    Stream<Integer> _stream = new Stream<Integer>();
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Procedure1<StreamHandlerBuilder<Integer>> _function = new Procedure1<StreamHandlerBuilder<Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            TestStream.this.incCounter(it);
          }
        };
        it.each(_function);
        final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
          public void apply(final Integer it) {
          }
        };
        it.finish(_function_1);
      }
    };
    StreamExtensions.<Integer>on(s, _function);
    s.next();
    Integer _counter = this.getCounter();
    Assert.assertEquals(1, (_counter).intValue());
    s.skip();
    Integer _counter_1 = this.getCounter();
    Assert.assertEquals(1, (_counter_1).intValue());
    s.next();
    Integer _counter_2 = this.getCounter();
    Assert.assertEquals(1, (_counter_2).intValue());
    s.next();
    Integer _counter_3 = this.getCounter();
    Assert.assertEquals(5, (_counter_3).intValue());
    s.next();
    Integer _counter_4 = this.getCounter();
    Assert.assertEquals(10, (_counter_4).intValue());
    s.next();
    Integer _counter_5 = this.getCounter();
    Assert.assertEquals(10, (_counter_5).intValue());
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(4));
    Integer _counter_6 = this.getCounter();
    Assert.assertEquals(11, (_counter_6).intValue());
    s.next();
    Integer _counter_7 = this.getCounter();
    Assert.assertEquals(15, (_counter_7).intValue());
  }
  
  @Atomic
  private final AtomicInteger _result = new AtomicInteger();
  
  @Test
  public void testControlledChainedBufferedStream() {
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
    final Procedure1<StreamHandlerBuilder<Integer>> _function_1 = new Procedure1<StreamHandlerBuilder<Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            TestStream.this.setResult(it);
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s2, _function_1);
    s2.next();
    Integer _result = this.getResult();
    Assert.assertEquals(4, (_result).intValue());
    s2.next();
    Integer _result_1 = this.getResult();
    Assert.assertEquals(5, (_result_1).intValue());
    s2.next();
    Integer _result_2 = this.getResult();
    Assert.assertEquals(6, (_result_2).intValue());
    s2.next();
    Integer _result_3 = this.getResult();
    Assert.assertEquals(1, (_result_3).intValue());
    s2.next();
    Integer _result_4 = this.getResult();
    Assert.assertEquals(2, (_result_4).intValue());
    s2.next();
    Integer _result_5 = this.getResult();
    Assert.assertEquals(3, (_result_5).intValue());
  }
  
  @Atomic
  private final AtomicReference<Throwable> _error = new AtomicReference<Throwable>();
  
  @Test
  public void testStreamErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(Integer.valueOf((1 / (it).intValue())));
      }
    };
    Task _onEach = StreamExtensions.<Integer>onEach(s, _function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStream.this.setError(it);
      }
    };
    _onEach.onError(_function_1);
    StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(0));
    Throwable _error = this.getError();
    Assert.assertNotNull(_error);
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
    final Procedure1<StreamHandlerBuilder<Integer>> _function_1 = new Procedure1<StreamHandlerBuilder<Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            result.set((it).intValue());
          }
        };
        it.each(_function);
        final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
          public void apply(final Integer it) {
          }
        };
        it.finish(_function_1);
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
  
  @Atomic
  private final AtomicInteger _overflowCount = new AtomicInteger();
  
  @Test
  public void testStreamBufferOverflow() {
    final Stream<Integer> stream = new Stream<Integer>(3);
    final Procedure1<StreamResponder> _function = new Procedure1<StreamResponder>() {
      public void apply(final StreamResponder it) {
        final Procedure1<Entry<?>> _function = new Procedure1<Entry<?>>() {
          public void apply(final Entry<?> it) {
            TestStream.this.incOverflowCount();
          }
        };
        it.overflow(_function);
      }
    };
    StreamExtensions.<Integer>monitor(stream, _function);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    StreamExtensions.<Integer>operator_doubleLessThan(stream, Integer.valueOf(4));
    StreamExtensions.<Integer>operator_doubleLessThan(stream, Integer.valueOf(5));
    Integer _overflowCount = this.getOverflowCount();
    Assert.assertEquals(2, (_overflowCount).intValue());
  }
  
  private Integer setCounter(final Integer value) {
    return this._counter.getAndSet(value);
  }
  
  private Integer getCounter() {
    return this._counter.get();
  }
  
  private Integer incCounter() {
    return this._counter.incrementAndGet();
  }
  
  private Integer decCounter() {
    return this._counter.decrementAndGet();
  }
  
  private Integer incCounter(final Integer value) {
    return this._counter.addAndGet(value);
  }
  
  private Integer setResult(final Integer value) {
    return this._result.getAndSet(value);
  }
  
  private Integer getResult() {
    return this._result.get();
  }
  
  private Integer incResult() {
    return this._result.incrementAndGet();
  }
  
  private Integer decResult() {
    return this._result.decrementAndGet();
  }
  
  private Integer incResult(final Integer value) {
    return this._result.addAndGet(value);
  }
  
  private Throwable setError(final Throwable value) {
    return this._error.getAndSet(value);
  }
  
  private Throwable getError() {
    return this._error.get();
  }
  
  private Integer setOverflowCount(final Integer value) {
    return this._overflowCount.getAndSet(value);
  }
  
  private Integer getOverflowCount() {
    return this._overflowCount.get();
  }
  
  private Integer incOverflowCount() {
    return this._overflowCount.incrementAndGet();
  }
  
  private Integer decOverflowCount() {
    return this._overflowCount.decrementAndGet();
  }
  
  private Integer incOverflowCount(final Integer value) {
    return this._overflowCount.addAndGet(value);
  }
}
