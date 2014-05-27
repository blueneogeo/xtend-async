package nl.kii.stream.test;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Next;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExtensions;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure4;
import org.junit.Test;

@SuppressWarnings("all")
public class TestCollector {
  @Test
  public void testStreaming() {
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
      PromiseExtensions.run(_function_1);
      final Runnable _function_2 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(1000, 1999);
          for (final Integer i : _upTo) {
            Value<Integer> _value = new Value<Integer>(Integer.valueOf(2));
            s.apply(_value);
          }
        }
      };
      PromiseExtensions.run(_function_2);
      final Runnable _function_3 = new Runnable() {
        public void run() {
          IntegerRange _upTo = new IntegerRange(2000, 2999);
          for (final Integer i : _upTo) {
            Value<Integer> _value = new Value<Integer>(Integer.valueOf(3));
            s.apply(_value);
          }
        }
      };
      PromiseExtensions.run(_function_3);
      final AtomicInteger sum = new AtomicInteger();
      final Procedure4<Entry<Integer>, Procedure0, Procedure0, Procedure0> _function_4 = new Procedure4<Entry<Integer>, Procedure0, Procedure0, Procedure0>() {
        public void apply(final Entry<Integer> it, final Procedure0 next, final Procedure0 skip, final Procedure0 close) {
          InputOutput.<String>println(("got : " + it));
          boolean _matched = false;
          if (!_matched) {
            if (it instanceof Value) {
              _matched=true;
              sum.addAndGet((((Value<Integer>)it).value).intValue());
            }
          }
          next.apply();
        }
      };
      s2.setListener(_function_4);
      Next _next = new Next();
      s2.perform(_next);
      Thread.sleep(500);
      int _get = sum.get();
      InputOutput.<Integer>println(Integer.valueOf(_get));
      List<Queue<Entry<Integer>>> _queue = s.getQueue();
      InputOutput.<List<Queue<Entry<Integer>>>>println(_queue);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public <T extends Object> void onEach(final Stream<T> stream, final Procedure1<? super T> handler) {
    final Procedure4<Entry<T>, Procedure0, Procedure0, Procedure0> _function = new Procedure4<Entry<T>, Procedure0, Procedure0, Procedure0>() {
      public void apply(final Entry<T> it, final Procedure0 next, final Procedure0 skip, final Procedure0 close) {
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            handler.apply(((Value<T>)it).value);
            next.apply();
          }
        }
      }
    };
    stream.setListener(_function);
  }
  
  @Test
  public void testResolving() {
    try {
      final Stream<String> s = StreamExtensions.<String>stream(Collections.<String>unmodifiableList(Lists.<String>newArrayList("a", "b", "c", "d", "e")));
      final Function1<String, String> _function = new Function1<String, String>() {
        public String apply(final String it) {
          String _xblockexpression = null;
          {
            InputOutput.<String>println(("pushing " + it));
            _xblockexpression = it;
          }
          return _xblockexpression;
        }
      };
      Stream<String> _map = StreamExtensions.<String, String>map(s, _function);
      final Function1<String, Promise<String>> _function_1 = new Function1<String, Promise<String>>() {
        public Promise<String> apply(final String it) {
          return TestCollector.this.doSomethingAsync(it);
        }
      };
      Stream<Promise<String>> _map_1 = StreamExtensions.<String, Promise<String>>map(_map, _function_1);
      Stream<String> _resolve = StreamExtensions.<String, Object>resolve(_map_1, 2);
      Stream<List<String>> _collect = StreamExtensions.<String>collect(_resolve);
      final Procedure1<List<String>> _function_2 = new Procedure1<List<String>>() {
        public void apply(final List<String> it) {
          InputOutput.<String>println(("got: " + it));
        }
      };
      StreamExtensions.<List<String>>forEach(_collect, _function_2);
      Stream<String> _doubleLessThan = StreamExtensions.<String>operator_doubleLessThan(s, "f");
      Stream<String> _doubleLessThan_1 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan, "g");
      Finish<String> _finish = StreamExtensions.<String>finish();
      Stream<String> _doubleLessThan_2 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_1, _finish);
      Stream<String> _doubleLessThan_3 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_2, "h");
      Finish<String> _finish_1 = StreamExtensions.<String>finish();
      StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_3, _finish_1);
      Thread.sleep(3000);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<String> doSomethingAsync(final String x) {
    final Callable<String> _function = new Callable<String>() {
      public String call() throws Exception {
        String _xblockexpression = null;
        {
          IntegerRange _upTo = new IntegerRange(1, 5);
          for (final Integer i : _upTo) {
            System.out.print("");
          }
          _xblockexpression = x;
        }
        return _xblockexpression;
      }
    };
    return PromiseExtensions.<String>async(_function);
  }
}
