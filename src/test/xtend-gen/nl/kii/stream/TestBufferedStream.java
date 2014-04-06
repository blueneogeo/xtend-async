package nl.kii.stream;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.BufferedStream;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.PromisePairExt;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExt;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestBufferedStream {
  @Test
  public void testBufferedStreaming() {
    BufferedStream<String> _stream = StreamExt.<String>stream(String.class);
    Stream<String> _doubleLessThan = StreamExt.<String>operator_doubleLessThan(_stream, "a");
    Stream<String> _doubleLessThan_1 = StreamExt.<String>operator_doubleLessThan(_doubleLessThan, "b");
    Stream<String> _doubleLessThan_2 = StreamExt.<String>operator_doubleLessThan(_doubleLessThan_1, "c");
    Finish<String> _finish = StreamExt.<String>finish();
    final Stream<String> s = StreamExt.<String>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Value<String> _value = this.<String>value("a");
    Value<String> _value_1 = this.<String>value("b");
    Value<String> _value_2 = this.<String>value("c");
    Finish<String> _finish_1 = StreamExt.<String>finish();
    this.<String>assertStream(Collections.<Entry<String>>unmodifiableList(Lists.<Entry<String>>newArrayList(_value, _value_1, _value_2, _finish_1)), s);
  }
  
  @Test
  public void testDeferredStreaming() {
    final BufferedStream<String> s2 = StreamExt.<String>stream(String.class);
    final BufferedStream<String> s = StreamExt.<String>stream(String.class);
    final Procedure1<String> _function = new Procedure1<String>() {
      public void apply(final String it) {
        StreamExt.<String>operator_doubleGreaterThan(it, s2);
      }
    };
    s.each(_function);
    Value<String> _value = this.<String>value("a");
    Stream<String> _doubleLessThan = StreamExt.<String>operator_doubleLessThan(s, _value);
    Value<String> _value_1 = this.<String>value("b");
    StreamExt.<String>operator_doubleLessThan(_doubleLessThan, _value_1);
    Value<String> _value_2 = this.<String>value("a");
    Value<String> _value_3 = this.<String>value("b");
    this.<String>assertStream(Collections.<Value<String>>unmodifiableList(Lists.<Value<String>>newArrayList(_value_2, _value_3)), s2);
  }
  
  @Test
  public void testAutomaticEach() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final BufferedStream<Integer> s2 = StreamExt.<Integer>stream(Integer.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        StreamExt.<Integer>operator_doubleGreaterThan(it, s2);
      }
    };
    s.each(_function);
    Value<Integer> _value = this.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = this.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_2 = this.<Integer>value(Integer.valueOf(3));
    this.<Integer>assertStream(Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value, _value_1, _value_2)), s2);
  }
  
  @Test
  public void testCatchErrorsOff() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method start is undefined for the type TestBufferedStream");
  }
  
  @Test
  public void testCatchErrorsOn() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method start is undefined for the type TestBufferedStream");
  }
  
  @Test
  public void testCatchErrorsOnWithChaining() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method start is undefined for the type TestBufferedStream");
  }
  
  @Test
  public void testMap() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 3));
      }
    };
    final Stream<Integer> mapped = StreamExt.<Integer, Integer>map(s, _function);
    Value<Integer> _value = this.<Integer>value(Integer.valueOf(4));
    Value<Integer> _value_1 = this.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_2 = this.<Integer>value(Integer.valueOf(6));
    this.<Integer>assertStream(Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value, _value_1, _value_2)), mapped);
  }
  
  @Test
  public void testFilter() {
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5)), Integer.class)));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> filtered = StreamExt.<Integer>filter(s, _function);
    Value<Integer> _value = this.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = this.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    this.<Integer>assertStream(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _finish)), filtered);
  }
  
  @Test
  public void testFlatten() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method flatten is undefined for the type TestBufferedStream");
  }
  
  @Test
  public void testSplit() {
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5)), Integer.class)));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> split = StreamExt.<Integer>split(s, _function);
    Value<Integer> _value = this.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = this.<Integer>value(Integer.valueOf(2));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Value<Integer> _value_2 = this.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_3 = this.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    Value<Integer> _value_4 = this.<Integer>value(Integer.valueOf(5));
    Finish<Integer> _finish_2 = StreamExt.<Integer>finish();
    this.<Integer>assertStream(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _finish, _value_2, _value_3, _finish_1, _value_4, _finish_2)), split);
  }
  
  @Test
  public void testCollectAndFirst() {
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5)), Integer.class)));
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(s);
    final Promise<List<Integer>> collect = StreamExt.<List<Integer>>first(_collect);
    this.<Integer>assertPromiseEquals(collect, Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5))));
  }
  
  @Test
  public void testRepeatedCollect() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish_1);
    Finish<Integer> _finish_2 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_2);
    final Stream<List<Integer>> collect = StreamExt.<Integer>collect(s);
    Value<List<Integer>> _value = this.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2)));
    Value<List<Integer>> _value_1 = this.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(3, 4)));
    Value<List<Integer>> _value_2 = this.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList()));
    this.<List<Integer>>assertStream(Collections.<Value<List<Integer>>>unmodifiableList(Lists.<Value<List<Integer>>>newArrayList(_value, _value_1, _value_2)), collect);
  }
  
  private Promise<Integer> mockAddAsync(final int value) {
    Promise<Integer> _promise = PromiseExt.<Integer>promise(Integer.class);
    final Procedure1<Promise<Integer>> _function = new Procedure1<Promise<Integer>>() {
      public void apply(final Promise<Integer> it) {
        it.apply(Integer.valueOf((value + 1)));
      }
    };
    return ObjectExtensions.<Promise<Integer>>operator_doubleArrow(_promise, _function);
  }
  
  @Test
  public void testAsync() {
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3)), Integer.class)));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestBufferedStream.this.mockAddAsync((it).intValue());
      }
    };
    Stream<Integer> _async = StreamExt.<Integer, Integer>async(s, _function);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(_async);
    final Promise<List<Integer>> result = StreamExt.<List<Integer>>first(_collect);
    this.<Integer>assertPromiseEquals(result, Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4))));
  }
  
  @Test
  public void testReduce() {
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3)), Integer.class)));
    final Function3<Integer, Integer, Long, Integer> _function = new Function3<Integer, Integer, Long, Integer>() {
      public Integer apply(final Integer a, final Integer b, final Long c) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    Stream<Integer> _reduce = StreamExt.<Integer>reduce(s, Integer.valueOf(1), _function);
    final Promise<Integer> reduced = StreamExt.<Integer>first(_reduce);
    this.<Integer>assertPromiseEquals(reduced, Integer.valueOf(7));
  }
  
  @Test
  public void testLimitAndShortcutting() {
    final AtomicInteger counter = new AtomicInteger();
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5, 6, 7, 8)), Integer.class)));
    Stream<Integer> _limit = StreamExt.<Integer>limit(s, 3);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    _limit.each(_function);
    int _get = counter.get();
    Assert.assertEquals(_get, 3);
  }
  
  @Test
  public void testShortcuttingMultipleListeners() {
    final AtomicInteger counter = new AtomicInteger();
    final BufferedStream<Integer> s = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5, 6, 7, 8)), Integer.class)));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        Integer _xblockexpression = null;
        {
          counter.incrementAndGet();
          _xblockexpression = it;
        }
        return _xblockexpression;
      }
    };
    Stream<Integer> _map = StreamExt.<Integer, Integer>map(s, _function);
    final Stream<Integer> limited = StreamExt.<Integer>limit(_map, 3);
    Value<Integer> _value = this.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = this.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_2 = this.<Integer>value(Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    this.<Integer>assertStream(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)), limited);
  }
  
  @Test
  public void testRepeatedStreaming() {
    final BufferedStream<Integer> stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(stream, Integer.valueOf(5));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(9));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(23));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    Stream<Integer> _filter = StreamExt.<Integer>filter(stream, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() * 2));
      }
    };
    Stream<Integer> _map = StreamExt.<Integer, Integer>map(_filter, _function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>print(it);
      }
    };
    _map.each(_function_2);
    Stream<Integer> _doubleLessThan_6 = StreamExt.<Integer>operator_doubleLessThan(stream, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_7 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_7, _finish_1);
  }
  
  @Test
  public void testCollectOLD() {
    final BufferedStream<Integer> stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(stream, Integer.valueOf(5));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(9));
    Stream<Integer> _doubleLessThan_6 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(23));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_6, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    Stream<Integer> _filter = StreamExt.<Integer>filter(stream, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() * 2));
      }
    };
    Stream<Integer> _map = StreamExt.<Integer, Integer>map(_filter, _function_1);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(_map);
    final Procedure1<List<Integer>> _function_2 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<List<Integer>>println(it);
      }
    };
    _collect.each(_function_2);
  }
  
  @Test
  public void testReduceOLD() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    final Stream<Integer> stream = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    Stream<Integer> _reduce = StreamExt.<Integer>reduce(stream, Integer.valueOf(0), _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    StreamExt.<Integer>then(_reduce, _function_1);
  }
  
  @Test
  public void testErrorCatching() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    final Procedure1<BufferedStream<Integer>> _function = new Procedure1<BufferedStream<Integer>>() {
      public void apply(final BufferedStream<Integer> it) {
        it.catchErrors = true;
      }
    };
    final BufferedStream<Integer> stream = ObjectExtensions.<BufferedStream<Integer>>operator_doubleArrow(_stream, _function);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(stream, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(6));
    Exception _exception = new Exception("throw this");
    StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, _exception);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println(("error:" + it));
      }
    };
    Stream<Integer> _onError = stream.onError(_function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    _onError.each(_function_2);
  }
  
  @Test
  public void testManyListeners() {
    final BufferedStream<Integer> stream = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5, 6, 7)), Integer.class)));
    Stream<Integer> _limit = StreamExt.<Integer>limit(stream, 2);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(_limit);
    final Procedure1<List<Integer>> _function = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        String _join = IterableExtensions.join(it, ",");
        InputOutput.<String>println(_join);
      }
    };
    StreamExt.<List<Integer>>then(_collect, _function);
  }
  
  @Test
  public void testSplitFn() {
    final BufferedStream<Integer> stream = StreamExt.<Integer>stream(((Integer[])Conversions.unwrapArray(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3, 4, 5, 6, 7, 8)), Integer.class)));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    Stream<Integer> _split = StreamExt.<Integer>split(stream, _function);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(_split);
    final Procedure1<List<Integer>> _function_1 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<List<Integer>>println(it);
      }
    };
    _collect.each(_function_1);
  }
  
  @Test
  public void testSimpleStreaming() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(9));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(10));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    final Stream<Integer> stream = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 4));
      }
    };
    Stream<Integer> _map = StreamExt.<Integer, Integer>map(stream, _function);
    final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    Stream<Integer> _filter = StreamExt.<Integer>filter(_map, _function_1);
    final Procedure1<Void> _function_2 = new Procedure1<Void>() {
      public void apply(final Void it) {
        InputOutput.<String>println("done!");
      }
    };
    Stream<Integer> _onFinish = _filter.onFinish(_function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<String>println(("got: " + it));
      }
    };
    _onFinish.each(_function_3);
  }
  
  @Test
  public void testBigStream() {
    BufferedStream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(5));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(22));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    final Stream<Integer> stream = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 4));
      }
    };
    Stream<Integer> _map = StreamExt.<Integer, Integer>map(stream, _function);
    final Function1<Integer, String> _function_1 = new Function1<Integer, String>() {
      public String apply(final Integer it) {
        return it.toString();
      }
    };
    Stream<String> _map_1 = StreamExt.<Integer, String>map(_map, _function_1);
    final Function1<String, Promise<String>> _function_2 = new Function1<String, Promise<String>>() {
      public Promise<String> apply(final String it) {
        return TestBufferedStream.this.getMessageAsync(it);
      }
    };
    Stream<String> _async = StreamExt.<String, String>async(_map_1, _function_2);
    final Function1<String, Integer> _function_3 = new Function1<String, Integer>() {
      public Integer apply(final String it) {
        return Integer.valueOf(it.length());
      }
    };
    Stream<Integer> _map_2 = StreamExt.<String, Integer>map(_async, _function_3);
    Stream<Double> _sum = StreamExt.<Integer>sum(_map_2);
    Promise<Double> _first = StreamExt.<Double>first(_sum);
    final Procedure1<Double> _function_4 = new Procedure1<Double>() {
      public void apply(final Double it) {
        int _intValue = it.intValue();
        String _plus = ("sum: " + Integer.valueOf(_intValue));
        InputOutput.<String>println(_plus);
      }
    };
    _first.then(_function_4);
  }
  
  @Test
  public void testError() {
    BufferedStream<Long> _stream = StreamExt.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExt.<Long>operator_doubleLessThan(_stream, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_1 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(0L));
    Finish<Long> _finish = StreamExt.<Long>finish();
    final Stream<Long> stream = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_1, _finish);
    stream.catchErrors = true;
    final Function1<Long, Long> _function = new Function1<Long, Long>() {
      public Long apply(final Long it) {
        return Long.valueOf((10 / (it).longValue()));
      }
    };
    Stream<Long> _map = StreamExt.<Long, Long>map(stream, _function);
    Stream<Double> _sum = StreamExt.<Long>sum(_map);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<String>println(("error: " + it));
      }
    };
    Stream<Double> _onError = _sum.onError(_function_1);
    final Procedure1<Double> _function_2 = new Procedure1<Double>() {
      public void apply(final Double it) {
        InputOutput.<String>println(("sum: " + it));
      }
    };
    StreamExt.<Double>then(_onError, _function_2);
  }
  
  @Test
  public void testPromise() {
    final Promise<Integer> promise = PromiseExt.<Integer>promise(Integer.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<String>println(("got" + it));
      }
    };
    promise.then(_function);
    PromiseExt.<Integer>operator_doubleGreaterThan(
      Integer.valueOf(10), promise);
  }
  
  @Test
  public void testBufferedPromise() {
    final Promise<Integer> promise = PromiseExt.<Integer>promise(Integer.valueOf(10));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<String>println(("got " + it));
      }
    };
    promise.then(_function);
  }
  
  @Test
  public void testPromiseChain() {
    final Promise<String> promise = PromiseExt.<String>promise(String.class);
    PromiseExt.<String>operator_doubleGreaterThan(
      "hello", promise);
    final Function1<String, Promise<String>> _function = new Function1<String, Promise<String>>() {
      public Promise<String> apply(final String it) {
        return TestBufferedStream.this.getMessageAsync(it);
      }
    };
    Promise<String> _async = PromiseExt.<String, String>async(promise, _function);
    final Function1<String, Promise<String>> _function_1 = new Function1<String, Promise<String>>() {
      public Promise<String> apply(final String it) {
        return TestBufferedStream.this.getMessageAsync(it);
      }
    };
    Promise<String> _async_1 = PromiseExt.<String, String>async(_async, _function_1);
    final Procedure1<String> _function_2 = new Procedure1<String>() {
      public void apply(final String it) {
        InputOutput.<String>println(it);
      }
    };
    _async_1.then(_function_2);
  }
  
  @Test
  public void testPromiseChainPair() {
    final Promise<String> promise = PromiseExt.<String>promise(String.class);
    PromiseExt.<String>operator_doubleGreaterThan(
      "hello", promise);
    final Function1<String, Pair<Pair<Integer, Integer>, Promise<String>>> _function = new Function1<String, Pair<Pair<Integer, Integer>, Promise<String>>>() {
      public Pair<Pair<Integer, Integer>, Promise<String>> apply(final String it) {
        Pair<Integer, Integer> _mappedTo = Pair.<Integer, Integer>of(Integer.valueOf(2), Integer.valueOf(3));
        Promise<String> _messageAsync = TestBufferedStream.this.getMessageAsync(it);
        return Pair.<Pair<Integer, Integer>, Promise<String>>of(_mappedTo, _messageAsync);
      }
    };
    Promise<Pair<Pair<Integer, Integer>, String>> _async2 = PromisePairExt.<String, String, Pair<Integer, Integer>>async2(promise, _function);
    final Procedure2<Pair<Integer, Integer>, String> _function_1 = new Procedure2<Pair<Integer, Integer>, String>() {
      public void apply(final Pair<Integer, Integer> p, final String v) {
        String _plus = (p + v);
        InputOutput.<String>println(_plus);
      }
    };
    PromisePairExt.<Pair<Integer, Integer>, String>then(_async2, _function_1);
  }
  
  public Promise<String> getMessageAsync(final String value) {
    Promise<String> _promise = PromiseExt.<String>promise(String.class);
    final Procedure1<Promise<String>> _function = new Procedure1<Promise<String>>() {
      public void apply(final Promise<String> it) {
        it.apply(("got value " + value));
      }
    };
    return ObjectExtensions.<Promise<String>>operator_doubleArrow(_promise, _function);
  }
  
  private <T extends Object> void assertStream(final List<? extends Entry<T>> entries, final Stream<T> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method start is undefined for the type TestBufferedStream");
  }
  
  private void assertPromiseFinished(final Promise<Boolean> promise) {
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    promise.then(_function);
    boolean _isFinished = promise.isFinished();
    Assert.assertTrue(_isFinished);
  }
  
  private <T extends Object> void assertPromiseEquals(final Promise<T> promise, final T value) {
    final AtomicReference<T> ref = new AtomicReference<T>();
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        ref.set(it);
      }
    };
    promise.then(_function);
    boolean _isFinished = promise.isFinished();
    Assert.assertTrue(_isFinished);
    T _get = ref.get();
    Assert.assertEquals(_get, value);
  }
  
  private <T extends Object> void assertPromiseEquals(final Promise<List<T>> promise, final List<T> value) {
    final AtomicReference<List<T>> ref = new AtomicReference<List<T>>();
    final Procedure1<List<T>> _function = new Procedure1<List<T>>() {
      public void apply(final List<T> it) {
        ref.set(it);
      }
    };
    promise.then(_function);
    boolean _isFinished = promise.isFinished();
    Assert.assertTrue(_isFinished);
    List<T> _get = ref.get();
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(_get, Object.class)), ((Object[])Conversions.unwrapArray(value, Object.class)));
  }
  
  private <T extends Object> Value<T> value(final T value) {
    return new Value<T>(value);
  }
}
