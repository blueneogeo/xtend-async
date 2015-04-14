package nl.kii.stream.test;

import com.google.common.base.Objects;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.ExecutorExtensions;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestMultiThreadedProcessing {
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Test
  public void testSimpleAsyncPromise() {
    try {
      final AtomicInteger result = new AtomicInteger();
      Promise<Integer> _power2 = this.power2(2);
      final Procedure1<Integer> _function = new Procedure1<Integer>() {
        @Override
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
      final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.power2((it).intValue());
        }
      };
      SubPromise<Integer, Promise<Integer>> _map = PromiseExtensions.<Integer, Integer, Promise<Integer>>map(_power2, _function);
      SubPromise<Integer, Integer> _flatten = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>flatten(_map);
      final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.power2((it).intValue());
        }
      };
      SubPromise<Integer, Promise<Integer>> _map_1 = PromiseExtensions.<Integer, Integer, Promise<Integer>>map(_flatten, _function_1);
      SubPromise<Integer, Integer> _flatten_1 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>flatten(_map_1);
      final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
        @Override
        public void apply(final Integer it) {
          result.set((it).intValue());
        }
      };
      _flatten_1.then(_function_2);
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
      Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
      final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.power2((it).intValue());
        }
      };
      SubStream<Integer, Promise<Integer>> _map = StreamExtensions.<Integer, Integer, Promise<Integer>>map(s, _function);
      SubStream<Integer, Integer> _resolve = StreamExtensions.<Integer, Integer>resolve(_map, 1);
      final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() + 1));
        }
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_resolve, _function_1);
      final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.power2((it).intValue());
        }
      };
      SubStream<Integer, Promise<Integer>> _map_2 = StreamExtensions.<Integer, Integer, Promise<Integer>>map(_map_1, _function_2);
      SubStream<Integer, Integer> _resolve_1 = StreamExtensions.<Integer, Integer>resolve(_map_2, 1);
      final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
        @Override
        public void apply(final Integer it) {
          LinkedList<Integer> _get = result.get();
          _get.add(it);
        }
      };
      StreamExtensions.<Integer, Integer>onEach(_resolve_1, _function_3);
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
      Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
      final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.throwsError((it).intValue());
        }
      };
      SubStream<Integer, Promise<Integer>> _map = StreamExtensions.<Integer, Integer, Promise<Integer>>map(s, _function);
      SubStream<Integer, Integer> _resolve = StreamExtensions.<Integer, Integer>resolve(_map, 1);
      final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(final Integer it) {
          return Integer.valueOf(((it).intValue() + 1));
        }
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_resolve, _function_1);
      final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
        @Override
        public Promise<Integer> apply(final Integer it) {
          return TestMultiThreadedProcessing.this.power2((it).intValue());
        }
      };
      SubStream<Integer, Promise<Integer>> _map_2 = StreamExtensions.<Integer, Integer, Promise<Integer>>map(_map_1, _function_2);
      SubStream<Integer, Integer> _resolve_1 = StreamExtensions.<Integer, Integer, Integer>resolve(_map_2);
      final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
        @Override
        public void apply(final Throwable it) {
          result.incrementAndGet();
        }
      };
      SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_resolve_1, _function_3);
      final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
        @Override
        public void apply(final Integer it) {
          InputOutput.<String>println(("result " + it));
        }
      };
      StreamExtensions.<Integer, Integer>onEach(_onError, _function_4);
      Thread.sleep(500);
      int _get = result.get();
      Assert.assertEquals(3, _get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<Integer> power2(final int i) {
    final Callable<Integer> _function = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        Thread.sleep(100);
        return Integer.valueOf((i * i));
      }
    };
    return ExecutorExtensions.<Integer>promise(this.threads, _function);
  }
  
  public Promise<Integer> throwsError(final int i) {
    final Callable<Integer> _function = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        Thread.sleep(100);
        boolean _notEquals = (!Objects.equal(TestMultiThreadedProcessing.this.threads, null));
        if (_notEquals) {
          throw new Exception("something went wrong");
        }
        return Integer.valueOf((i * i));
      }
    };
    return ExecutorExtensions.<Integer>promise(this.threads, _function);
  }
}
