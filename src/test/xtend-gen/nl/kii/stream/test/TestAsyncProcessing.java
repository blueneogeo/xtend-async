package nl.kii.stream.test;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExt;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAsyncProcessing {
  @Test
  public void testSimpleAsyncPromise() {
    try {
      final AtomicInteger result = new AtomicInteger();
      Promise<Integer> _power2 = this.power2(2);
      final Procedure1<Integer> _function = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          result.set((it).intValue());
        }
      };
      _power2.then(_function);
      int _get = result.get();
      Assert.assertEquals(0, _get);
      Thread.sleep(210);
      int _get_1 = result.get();
      Assert.assertEquals(4, _get_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testTripleAsyncPromise() {
    try {
      final AtomicInteger result = new AtomicInteger();
      Promise<Integer> _power2 = this.power2(2);
      final Function1<Integer,Promise<Integer>> _function = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Promise<Integer> _mapAsync = PromiseExt.<Integer, Integer>mapAsync(_power2, _function);
      final Function1<Integer,Promise<Integer>> _function_1 = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Promise<Integer> _mapAsync_1 = PromiseExt.<Integer, Integer>mapAsync(_mapAsync, _function_1);
      final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          result.set((it).intValue());
        }
      };
      _mapAsync_1.then(_function_2);
      int _get = result.get();
      Assert.assertEquals(0, _get);
      Thread.sleep(500);
      int _get_1 = result.get();
      Assert.assertEquals(256, _get_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAsyncMapping() {
    try {
      LinkedList<Integer> _linkedList = new LinkedList<Integer>();
      final AtomicReference<LinkedList<Integer>> result = new AtomicReference<LinkedList<Integer>>(_linkedList);
      Stream<Integer> _stream = StreamExt.<Integer>stream(int.class);
      Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
      final Function1<Integer,Promise<Integer>> _function = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Stream<Integer> _mapAsync = StreamExt.<Integer, Integer>mapAsync(s, _function);
      final Function1<Integer,Integer> _function_1 = new Function1<Integer,Integer>() {
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() + 1));
        }
      };
      Stream<Integer> _map = StreamExt.<Integer, Integer>map(_mapAsync, _function_1);
      final Function1<Integer,Promise<Integer>> _function_2 = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Stream<Integer> _mapAsync_1 = StreamExt.<Integer, Integer>mapAsync(_map, _function_2);
      final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          LinkedList<Integer> _get = result.get();
          _get.add(it);
        }
      };
      StreamExt.<Integer>each(_mapAsync_1, _function_3);
      LinkedList<Integer> _get = result.get();
      int _size = _get.size();
      Assert.assertEquals(0, _size);
      Thread.sleep(700);
      LinkedList<Integer> _get_1 = result.get();
      int _size_1 = _get_1.size();
      Assert.assertEquals(3, _size_1);
      LinkedList<Integer> _get_2 = result.get();
      Integer _get_3 = _get_2.get(0);
      Assert.assertEquals(4, (_get_3).intValue());
      LinkedList<Integer> _get_4 = result.get();
      Integer _get_5 = _get_4.get(1);
      Assert.assertEquals(25, (_get_5).intValue());
      LinkedList<Integer> _get_6 = result.get();
      Integer _get_7 = _get_6.get(2);
      Assert.assertEquals(100, (_get_7).intValue());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAsyncErrorCatching() {
    try {
      final AtomicInteger result = new AtomicInteger();
      Stream<Integer> _stream = StreamExt.<Integer>stream(int.class);
      Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
      Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
      final Function1<Integer,Promise<Integer>> _function = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.throwsError((it).intValue());
        }
      };
      Stream<Integer> _mapAsync = StreamExt.<Integer, Integer>mapAsync(s, _function);
      final Function1<Integer,Integer> _function_1 = new Function1<Integer,Integer>() {
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() + 1));
        }
      };
      Stream<Integer> _map = StreamExt.<Integer, Integer>map(_mapAsync, _function_1);
      final Function1<Integer,Promise<Integer>> _function_2 = new Function1<Integer,Promise<Integer>>() {
        public Promise<Integer> apply(final Integer it) {
          return TestAsyncProcessing.this.power2((it).intValue());
        }
      };
      Stream<Integer> _mapAsync_1 = StreamExt.<Integer, Integer>mapAsync(_map, _function_2);
      final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          result.incrementAndGet();
        }
      };
      Stream<Integer> _error = StreamExt.<Integer>error(_mapAsync_1, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          Assert.fail("we should not end up here, since an error should be caught instead");
        }
      };
      StreamExt.<Integer>each(_error, _function_4);
      Thread.sleep(700);
      int _get = result.get();
      Assert.assertEquals(3, _get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<Integer> power2(final int i) {
    final Function0<Integer> _function = new Function0<Integer>() {
      public Integer apply() {
        try {
          Thread.sleep(100);
          return Integer.valueOf((i * i));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return PromiseExt.<Integer>promise(_function);
  }
  
  public Promise<Integer> throwsError(final int i) {
    final Function0<Integer> _function = new Function0<Integer>() {
      public Integer apply() {
        try {
          Thread.sleep(100);
          if (true) {
            throw new Exception("something went wrong");
          }
          return Integer.valueOf((i * i));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return PromiseExt.<Integer>promise(_function);
  }
}
