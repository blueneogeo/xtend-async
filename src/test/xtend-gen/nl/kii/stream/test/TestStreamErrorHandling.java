package nl.kii.stream.test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.Task;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamErrorHandling {
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _errors = new AtomicInteger();
  
  @Atomic
  private final AtomicBoolean _complete = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _failed = new AtomicBoolean();
  
  @Test
  public void testStreamsSwallowExceptions() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() == 2) || ((it).intValue() == 4))) {
              throw new Exception();
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    SubStream<Integer, Integer> _map_2 = StreamExtensions.<Integer, Integer, Integer>map(_map_1, _function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamErrorHandling.this.incCounter();
      }
    };
    Task _onEach = StreamExtensions.<Integer, Integer>onEach(_map_2, _function_3);
    final Procedure1<Boolean> _function_4 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestStreamErrorHandling.this.setComplete(Boolean.valueOf(true));
      }
    };
    _onEach.then(_function_4);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    StreamExtensions.<Object, Object>finish();
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
    Boolean _complete = this.getComplete();
    Assert.assertFalse((_complete).booleanValue());
  }
  
  @Test
  public void testStreamsErrorHandlersSwallowExceptions() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() == 2) || ((it).intValue() == 4))) {
              throw new Exception();
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamErrorHandling.this.incErrors();
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_map_1, _function_2);
    final Function1<Integer, Integer> _function_3 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    SubStream<Integer, Integer> _map_2 = StreamExtensions.<Integer, Integer, Integer>map(_onError, _function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamErrorHandling.this.incCounter();
      }
    };
    Task _onEach = StreamExtensions.<Integer, Integer>onEach(_map_2, _function_4);
    final Procedure1<Boolean> _function_5 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestStreamErrorHandling.this.setComplete(Boolean.valueOf(true));
      }
    };
    Task _then = _onEach.then(_function_5);
    final Procedure1<Throwable> _function_6 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamErrorHandling.this.setFailed(Boolean.valueOf(true));
      }
    };
    _then.onError(_function_6);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Integer _errors = this.getErrors();
    Assert.assertEquals(2, (_errors).intValue());
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
    Boolean _complete = this.getComplete();
    Assert.assertTrue((_complete).booleanValue());
    Boolean _failed = this.getFailed();
    Assert.assertFalse((_failed).booleanValue());
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream() {
    IntegerRange _upTo = new IntegerRange(1, 20);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(_stream, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() % 6) == 0)) {
              throw new Exception();
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_split, _function_1);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println(("error " + it));
      }
    };
    SubStream<Integer, List<Integer>> _onError = StreamExtensions.<Integer, List<Integer>>onError(_collect, _function_2);
    final Procedure1<List<Integer>> _function_3 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<String>println(("result : " + it));
      }
    };
    Task _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_onError, _function_3);
    final Procedure1<Boolean> _function_4 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<String>println("done");
      }
    };
    Task _then = _onEach.then(_function_4);
    final Procedure1<Throwable> _function_5 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        String _message = it.getMessage();
        Assert.fail(_message);
      }
    };
    _then.onError(_function_5);
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream2() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(6));
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(9));
    IStream<Integer, Integer> _doubleLessThan_9 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, Integer.valueOf(10));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_10 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_9, _finish);
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish(1);
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_10, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() % 6) == 0)) {
              throw new Exception();
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_split, _function_1);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println(("error " + it));
      }
    };
    SubStream<Integer, List<Integer>> _onError = StreamExtensions.<Integer, List<Integer>>onError(_collect, _function_2);
    final Procedure1<List<Integer>> _function_3 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<String>println(("result : " + it));
      }
    };
    Task _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_onError, _function_3);
    final Procedure1<Boolean> _function_4 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<String>println("done");
      }
    };
    Task _then = _onEach.then(_function_4);
    final Procedure1<Throwable> _function_5 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        String _message = it.getMessage();
        Assert.fail(_message);
      }
    };
    _then.onError(_function_5);
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream3() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, _finish);
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(6));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(8));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_9 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    IStream<Integer, Integer> _doubleLessThan_10 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_9, Integer.valueOf(9));
    IStream<Integer, Integer> _doubleLessThan_11 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_10, Integer.valueOf(10));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_12 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_11, _finish_2);
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish(1);
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_12, _finish_3);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() % 6) == 0)) {
              throw new Exception();
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println(("error " + it));
      }
    };
    SubStream<Integer, List<Integer>> _onError = StreamExtensions.<Integer, List<Integer>>onError(_collect, _function_1);
    final Procedure1<List<Integer>> _function_2 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<String>println(("result : " + it));
      }
    };
    Task _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_onError, _function_2);
    final Procedure1<Boolean> _function_3 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<String>println("done");
      }
    };
    Task _then = _onEach.then(_function_3);
    final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println("error");
        String _message = it.getMessage();
        Assert.fail(_message);
      }
    };
    _then.onError(_function_4);
  }
  
  @Test
  public void testSplit() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(_stream, _function);
    final Procedure1<StreamHandlerBuilder<Integer, Integer>> _function_1 = new Procedure1<StreamHandlerBuilder<Integer, Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            InputOutput.<Integer>println($1);
            it.stream.next();
          }
        };
        it.each(_function);
        final Procedure2<Integer, Integer> _function_1 = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            InputOutput.<String>println(("finish " + $1));
            it.stream.next();
          }
        };
        it.finish(_function_1);
        it.stream.next();
      }
    };
    StreamExtensions.<Integer, Integer>on(_split, _function_1);
  }
  
  @Atomic
  private final AtomicReference<Throwable> _caught = new AtomicReference<Throwable>();
  
  @Test
  public void testHandlingBelowErrorShouldFilterTheException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      SubStream<Integer, Integer> _filter = StreamExtensions.<Integer, Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_filter, _function_2);
      final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          TestStreamErrorHandling.this.setCaught(it);
        }
      };
      SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_map_1, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      StreamExtensions.<Integer, Integer>onEach(_onError, _function_4);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
      StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Assert.fail("error should be handled");
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    Throwable _caught = this.getCaught();
    Assert.assertNotNull(_caught);
  }
  
  @Test
  public void testHandlingAtTaskShouldFilterTheException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      SubStream<Integer, Integer> _filter = StreamExtensions.<Integer, Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_filter, _function_2);
      final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      Task _onEach = StreamExtensions.<Integer, Integer>onEach(_map_1, _function_3);
      final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          TestStreamErrorHandling.this.setCaught(it);
        }
      };
      _onEach.onError(_function_4);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
      StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Assert.fail("error should be handled");
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    Throwable _caught = this.getCaught();
    Assert.assertNotNull(_caught);
  }
  
  @Atomic
  private final AtomicBoolean _finished = new AtomicBoolean();
  
  @Atomic
  private final AtomicInteger _errorCount = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _count = new AtomicInteger();
  
  @Test
  public void testErrorHandlingBeforeCollect() {
    this.setFinished(Boolean.valueOf(false));
    this.setErrorCount(Integer.valueOf(0));
    this.setCount(Integer.valueOf(0));
    IntegerRange _upTo = new IntegerRange(1, 10);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() % 3));
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((100 / (it).intValue()));
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamErrorHandling.this.incErrorCount();
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_map_1, _function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamErrorHandling.this.incCount();
      }
    };
    Task _onEach = StreamExtensions.<Integer, Integer>onEach(_onError, _function_3);
    final Procedure1<Boolean> _function_4 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestStreamErrorHandling.this.setFinished(Boolean.valueOf(true));
      }
    };
    _onEach.then(_function_4);
    Integer _count = this.getCount();
    Assert.assertEquals(7, (_count).intValue());
    Integer _errorCount = this.getErrorCount();
    Assert.assertEquals(3, (_errorCount).intValue());
    Boolean _finished = this.getFinished();
    Assert.assertTrue((_finished).booleanValue());
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
  
  private Integer setErrors(final Integer value) {
    return this._errors.getAndSet(value);
  }
  
  private Integer getErrors() {
    return this._errors.get();
  }
  
  private Integer incErrors() {
    return this._errors.incrementAndGet();
  }
  
  private Integer decErrors() {
    return this._errors.decrementAndGet();
  }
  
  private Integer incErrors(final Integer value) {
    return this._errors.addAndGet(value);
  }
  
  private Boolean setComplete(final Boolean value) {
    return this._complete.getAndSet(value);
  }
  
  private Boolean getComplete() {
    return this._complete.get();
  }
  
  private Boolean setFailed(final Boolean value) {
    return this._failed.getAndSet(value);
  }
  
  private Boolean getFailed() {
    return this._failed.get();
  }
  
  private Throwable setCaught(final Throwable value) {
    return this._caught.getAndSet(value);
  }
  
  private Throwable getCaught() {
    return this._caught.get();
  }
  
  private Boolean setFinished(final Boolean value) {
    return this._finished.getAndSet(value);
  }
  
  private Boolean getFinished() {
    return this._finished.get();
  }
  
  private Integer setErrorCount(final Integer value) {
    return this._errorCount.getAndSet(value);
  }
  
  private Integer getErrorCount() {
    return this._errorCount.get();
  }
  
  private Integer incErrorCount() {
    return this._errorCount.incrementAndGet();
  }
  
  private Integer decErrorCount() {
    return this._errorCount.decrementAndGet();
  }
  
  private Integer incErrorCount(final Integer value) {
    return this._errorCount.addAndGet(value);
  }
  
  private Integer setCount(final Integer value) {
    return this._count.getAndSet(value);
  }
  
  private Integer getCount() {
    return this._count.get();
  }
  
  private Integer incCount() {
    return this._count.incrementAndGet();
  }
  
  private Integer decCount() {
    return this._count.decrementAndGet();
  }
  
  private Integer incCount(final Integer value) {
    return this._count.addAndGet(value);
  }
}
