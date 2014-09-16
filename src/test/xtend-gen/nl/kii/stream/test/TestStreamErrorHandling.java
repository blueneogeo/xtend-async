package nl.kii.stream.test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
import nl.kii.promise.Task;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamErrorHandling {
  @Test
  public void testNoHandlingShouldTriggerException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      Stream<Integer> _filter = StreamExtensions.<Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_filter, _function_2);
      final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      StreamExtensions.<Integer>onEach(_map_1, _function_3);
      Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer> _finish = StreamExtensions.<Integer>finish();
      StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
      Assert.fail("we expected an error for /0");
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testIteratorErrorHandlingShouldCatchException() {
    try {
      IntegerRange _upTo = new IntegerRange(1, 20);
      final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
      final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 3)) == 0));
        }
      };
      Stream<Integer> _filter = StreamExtensions.<Integer>filter(s, _function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
        }
      };
      Stream<Integer> _onError = StreamExtensions.<Integer>onError(_filter, _function_1);
      final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      StreamExtensions.<Integer>onEach(_onError, _function_2);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Assert.fail(("onError should have caught " + e));
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testHandlingAboveErrorShouldTriggerException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          Assert.fail("should not trigger");
        }
      };
      Stream<Integer> _onError = StreamExtensions.<Integer>onError(_map, _function_1);
      final Function1<Integer, Boolean> _function_2 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      Stream<Integer> _filter = StreamExtensions.<Integer>filter(_onError, _function_2);
      final Function1<Integer, Integer> _function_3 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_filter, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      StreamExtensions.<Integer>onEach(_map_1, _function_4);
      Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer> _finish = StreamExtensions.<Integer>finish();
      StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
      Assert.fail("we expected an error for /0");
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
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
      Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      Stream<Integer> _filter = StreamExtensions.<Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_filter, _function_2);
      final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          TestStreamErrorHandling.this.setCaught(it);
        }
      };
      Stream<Integer> _onError = StreamExtensions.<Integer>onError(_map_1, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      StreamExtensions.<Integer>onEach(_onError, _function_4);
      Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer> _finish = StreamExtensions.<Integer>finish();
      StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
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
      Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
        public Boolean apply(final Integer it) {
          return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
        }
      };
      Stream<Integer> _filter = StreamExtensions.<Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
        public Integer apply(final Integer it) {
          return it;
        }
      };
      Stream<Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_filter, _function_2);
      final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
        }
      };
      Task _onEach = StreamExtensions.<Integer>onEach(_map_1, _function_3);
      final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          TestStreamErrorHandling.this.setCaught(it);
        }
      };
      _onEach.onError(_function_4);
      Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer> _finish = StreamExtensions.<Integer>finish();
      StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
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
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((100 / (it).intValue()));
      }
    };
    Stream<Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamErrorHandling.this.incErrorCount();
      }
    };
    Stream<Integer> _onError = StreamExtensions.<Integer>onError(_map_1, _function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamErrorHandling.this.incCount();
      }
    };
    Task _onEach = StreamExtensions.<Integer>onEach(_onError, _function_3);
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
  
  @Test
  public void testErrorHandlingAfterCollect() {
    this.setFinished(Boolean.valueOf(false));
    this.setErrorCount(Integer.valueOf(0));
    this.setCount(Integer.valueOf(0));
    final Stream<Integer> s = StreamExtensions.<Integer>stream(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10))));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() % 3));
      }
    };
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((100 / (it).intValue()));
      }
    };
    final Stream<Integer> s2 = StreamExtensions.<Integer, Integer>map(_map, _function_1);
    final Procedure2<Stream<Integer>, StreamHandlerBuilder<Integer>> _function_2 = new Procedure2<Stream<Integer>, StreamHandlerBuilder<Integer>>() {
      public void apply(final Stream<Integer> stream, final StreamHandlerBuilder<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            TestStreamErrorHandling.this.incCount();
            stream.next();
          }
        };
        it.each(_function);
      }
    };
    Task _on = StreamExtensions.<Integer>on(s2, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamErrorHandling.this.incErrorCount();
      }
    };
    IPromise<Boolean> _onError = _on.onError(_function_3);
    final Procedure1<Boolean> _function_4 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestStreamErrorHandling.this.setFinished(Boolean.valueOf(true));
      }
    };
    _onError.then(_function_4);
    s2.next();
    Integer _count = this.getCount();
    Assert.assertEquals(7, (_count).intValue());
    Boolean _finished = this.getFinished();
    Assert.assertFalse((_finished).booleanValue());
    Integer _errorCount = this.getErrorCount();
    Assert.assertEquals(1, (_errorCount).intValue());
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
