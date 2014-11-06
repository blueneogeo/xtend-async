package nl.kii.stream.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.SubTask;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamEventHandler;
import nl.kii.stream.StreamEventResponder;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamObserver;
import nl.kii.stream.StreamResponder;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStream {
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Test
  public void testObservingAStream() {
    final Stream<Integer> s = new Stream<Integer>();
    StreamExtensions.<Integer, Integer>handle(s, new StreamEventHandler() {
      public void onNext() {
        InputOutput.<String>println("next!");
      }
      
      public void onSkip() {
        InputOutput.<String>println("skip!");
      }
      
      public void onClose() {
        InputOutput.<String>println("close!");
      }
      
      public void onOverflow(final Entry<?, ?> entry) {
        InputOutput.<String>println(("overflow! of " + entry));
      }
    });
    StreamExtensions.<Integer, Integer>observe(s, new StreamObserver<Integer, Integer>() {
      public void onValue(final Integer from, final Integer value) {
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
      
      public void onError(final Integer from, final Throwable t) {
        InputOutput.<String>println(("error:" + t));
        s.next();
      }
      
      public void onFinish(final Integer from, final int level) {
        InputOutput.<String>println("finished");
        s.next();
      }
      
      public void onClosed() {
        InputOutput.<String>println("closed");
      }
    });
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
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
    SubStream<Integer, Integer> _filter = StreamExtensions.<Integer, Integer>filter(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_filter, _function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer, Integer>onEach(_map, _function_2);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    int _get = counter.get();
    Assert.assertEquals(6, _get);
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    int _get_1 = counter.get();
    Assert.assertEquals(8, _get_1);
  }
  
  @Test
  public void testBufferedStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    Stream<Integer> _stream = new Stream<Integer>();
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer, Integer>onEach(s, _function);
    int _get = counter.get();
    Assert.assertEquals(6, _get);
  }
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Test
  public void testControlledStream() {
    Stream<Integer> _stream = new Stream<Integer>();
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Procedure1<StreamResponder<Integer, Integer>> _function = new Procedure1<StreamResponder<Integer, Integer>>() {
      public void apply(final StreamResponder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            TestStream.this.incCounter($1);
          }
        };
        it.each(_function);
        final Procedure2<Integer, Integer> _function_1 = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
          }
        };
        it.finish(_function_1);
      }
    };
    StreamExtensions.<Integer, Integer>on(s, _function);
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
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(4));
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
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    final SubStream<Integer, Integer> s = StreamExtensions.<Integer, Integer, Integer>map(_stream, _function);
    final Procedure1<StreamResponder<Integer, Integer>> _function_1 = new Procedure1<StreamResponder<Integer, Integer>>() {
      public void apply(final StreamResponder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            TestStream.this.setResult($1);
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer, Integer>on(s, _function_1);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    s.next();
    Integer _result = this.getResult();
    Assert.assertEquals(1, (_result).intValue());
    s.next();
    Integer _result_1 = this.getResult();
    Assert.assertEquals(2, (_result_1).intValue());
    s.next();
    Integer _result_2 = this.getResult();
    Assert.assertEquals(3, (_result_2).intValue());
    s.next();
    Integer _result_3 = this.getResult();
    Assert.assertEquals(3, (_result_3).intValue());
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
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(s, _function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println("!!");
        TestStream.this.setError(it);
      }
    };
    _onEach.onError(_function_1);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(0));
    Throwable _error = this.getError();
    Assert.assertNotNull(_error);
  }
  
  @Atomic
  private final AtomicInteger _sum = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _overflow = new AtomicInteger();
  
  @Test
  public void testParallelHighThroughputStreaming() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Procedure1<Entry<?, ?>> _function = new Procedure1<Entry<?, ?>>() {
        public void apply(final Entry<?, ?> it) {
          TestStream.this.incOverflow();
        }
      };
      final SubStream<Integer, Integer> s2 = StreamExtensions.<Integer, Integer>buffer(s, 3000, _function);
      final Runnable _function_1 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(0, 999);
          for (final Integer i : _upTo) {
            StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_1);
      final Runnable _function_2 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(1000, 1999);
          for (final Integer i : _upTo) {
            StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(2));
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_2);
      final Runnable _function_3 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(2000, 2999);
          for (final Integer i : _upTo) {
            StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(3));
          }
        }
      };
      ExecutorExtensions.task(this.threads, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          TestStream.this.incSum();
        }
      };
      StreamExtensions.<Integer, Integer>onEach(s2, _function_4);
      Thread.sleep(1000);
      Integer _overflow = this.getOverflow();
      Assert.assertEquals(0, (_overflow).intValue());
      Integer _sum = this.getSum();
      Assert.assertEquals(3000, (_sum).intValue());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Atomic
  private final AtomicInteger _overflowCount = new AtomicInteger();
  
  @Test
  public void testStreamBufferOverflow() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    final Procedure1<Stream<Integer>> _function = new Procedure1<Stream<Integer>>() {
      public void apply(final Stream<Integer> it) {
        it.setMaxBufferSize(Integer.valueOf(3));
      }
    };
    final Stream<Integer> stream = ObjectExtensions.<Stream<Integer>>operator_doubleArrow(_stream, _function);
    final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
      public void apply(final StreamEventResponder it) {
        final Procedure1<Entry<?, ?>> _function = new Procedure1<Entry<?, ?>>() {
          public void apply(final Entry<?, ?> it) {
            TestStream.this.incOverflowCount();
          }
        };
        it.overflow(_function);
      }
    };
    StreamExtensions.<Integer, Integer>when(stream, _function_1);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(stream, Integer.valueOf(4));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(stream, Integer.valueOf(5));
    Integer _overflowCount = this.getOverflowCount();
    Assert.assertEquals(2, (_overflowCount).intValue());
  }
  
  private void setCounter(final Integer value) {
    this._counter.set(value);
  }
  
  private Integer getCounter() {
    return this._counter.get();
  }
  
  private Integer getAndSetCounter(final Integer value) {
    return this._counter.getAndSet(value);
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
  
  private void setResult(final Integer value) {
    this._result.set(value);
  }
  
  private Integer getResult() {
    return this._result.get();
  }
  
  private Integer getAndSetResult(final Integer value) {
    return this._result.getAndSet(value);
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
  
  private void setError(final Throwable value) {
    this._error.set(value);
  }
  
  private Throwable getError() {
    return this._error.get();
  }
  
  private Throwable getAndSetError(final Throwable value) {
    return this._error.getAndSet(value);
  }
  
  private void setSum(final Integer value) {
    this._sum.set(value);
  }
  
  private Integer getSum() {
    return this._sum.get();
  }
  
  private Integer getAndSetSum(final Integer value) {
    return this._sum.getAndSet(value);
  }
  
  private Integer incSum() {
    return this._sum.incrementAndGet();
  }
  
  private Integer decSum() {
    return this._sum.decrementAndGet();
  }
  
  private Integer incSum(final Integer value) {
    return this._sum.addAndGet(value);
  }
  
  private void setOverflow(final Integer value) {
    this._overflow.set(value);
  }
  
  private Integer getOverflow() {
    return this._overflow.get();
  }
  
  private Integer getAndSetOverflow(final Integer value) {
    return this._overflow.getAndSet(value);
  }
  
  private Integer incOverflow() {
    return this._overflow.incrementAndGet();
  }
  
  private Integer decOverflow() {
    return this._overflow.decrementAndGet();
  }
  
  private Integer incOverflow(final Integer value) {
    return this._overflow.addAndGet(value);
  }
  
  private void setOverflowCount(final Integer value) {
    this._overflowCount.set(value);
  }
  
  private Integer getOverflowCount() {
    return this._overflowCount.get();
  }
  
  private Integer getAndSetOverflowCount(final Integer value) {
    return this._overflowCount.getAndSet(value);
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
