package nl.kii.stream.test;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Subscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamExtensions {
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Test
  public void testRangeStream() {
    IntegerRange _upTo = new IntegerRange(5, 7);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    final Stream<Integer> s2 = StreamExtensions.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(6));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(7));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    StreamAssert.<Integer>assertStreamContains(s2, _value, _value_1, _value_2, _finish);
  }
  
  @Test
  public void testListStream() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Collection<Entry<Integer>> _queue = s.getQueue();
    InputOutput.<Collection<Entry<Integer>>>println(_queue);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final Stream<Integer> s2 = StreamExtensions.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    StreamAssert.<Integer>assertStreamContains(s2, _value, _value_1, _value_2, _finish);
  }
  
  @Test
  public void testMapStream() {
    Pair<Integer, String> _mappedTo = Pair.<Integer, String>of(Integer.valueOf(1), "a");
    Pair<Integer, String> _mappedTo_1 = Pair.<Integer, String>of(Integer.valueOf(2), "b");
    final Map<Integer, String> map = Collections.<Integer, String>unmodifiableMap(CollectionLiterals.<Integer, String>newHashMap(_mappedTo, _mappedTo_1));
    final Stream<Pair<Integer, String>> s = StreamExtensions.<Integer, String>stream(map);
    final Function1<Pair<Integer, String>, Pair<Integer, String>> _function = new Function1<Pair<Integer, String>, Pair<Integer, String>>() {
      public Pair<Integer, String> apply(final Pair<Integer, String> it) {
        Integer _key = it.getKey();
        int _plus = ((_key).intValue() + 1);
        String _value = it.getValue();
        return Pair.<Integer, String>of(Integer.valueOf(_plus), _value);
      }
    };
    final Stream<Pair<Integer, String>> s2 = StreamExtensions.<Pair<Integer, String>, Pair<Integer, String>>map(s, _function);
    Pair<Integer, String> _mappedTo_2 = Pair.<Integer, String>of(Integer.valueOf(2), "a");
    Value<Pair<Integer, String>> _value = StreamAssert.<Pair<Integer, String>>value(_mappedTo_2);
    Pair<Integer, String> _mappedTo_3 = Pair.<Integer, String>of(Integer.valueOf(3), "b");
    Value<Pair<Integer, String>> _value_1 = StreamAssert.<Pair<Integer, String>>value(_mappedTo_3);
    Finish<Pair<Integer, String>> _finish = StreamExtensions.<Pair<Integer, String>>finish();
    StreamAssert.<Pair<Integer, String>>assertStreamContains(s2, _value, _value_1, _finish);
  }
  
  @Test
  public void testRandomStream() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    final Stream<Integer> s = StreamExtensions.streamRandom(_upTo);
    final Procedure1<Subscription<Integer>> _function = new Procedure1<Subscription<Integer>>() {
      public void apply(final Subscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            Assert.assertTrue((((it).intValue() >= 1) && ((it).intValue() <= 3)));
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer>on(s, _function);
    IntegerRange _upTo_1 = new IntegerRange(1, 1000);
    for (final Integer i : _upTo_1) {
      s.next();
    }
  }
  
  @Test
  public void testSubscriptionBuilding() {
    final AtomicBoolean finished = new AtomicBoolean();
    final AtomicBoolean errored = new AtomicBoolean();
    final AtomicInteger count = new AtomicInteger();
    IntegerRange _upTo = new IntegerRange(1, 10);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() - 10));
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
        errored.set(true);
      }
    };
    Subscription<Integer> _onError = StreamExtensions.<Integer>onError(_map_1, _function_2);
    final Procedure1<Finish<Integer>> _function_3 = new Procedure1<Finish<Integer>>() {
      public void apply(final Finish<Integer> it) {
        finished.set(true);
      }
    };
    Subscription<Integer> _onFinish = StreamExtensions.<Integer>onFinish(_onError, _function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        count.incrementAndGet();
      }
    };
    StreamExtensions.<Integer>onEach(_onFinish, _function_4);
    boolean _get = finished.get();
    Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(_get));
    boolean _get_1 = errored.get();
    Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(_get_1));
    int _get_2 = count.get();
    Assert.assertEquals(9, _get_2);
  }
  
  @Atomic
  private final AtomicBoolean _calledThen = new AtomicBoolean();
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Test
  public void testTaskReturning() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Procedure1<Finish<Integer>> _function = new Procedure1<Finish<Integer>>() {
      public void apply(final Finish<Integer> it) {
      }
    };
    Subscription<Integer> _onFinish = StreamExtensions.<Integer>onFinish(_stream, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamExtensions.this.incCounter();
      }
    };
    Task _onEach = StreamExtensions.<Integer>onEach(_onFinish, _function_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestStreamExtensions.this.setCalledThen(Boolean.valueOf(true));
      }
    };
    _onEach.then(_function_2);
    Boolean _calledThen = this.getCalledThen();
    Assert.assertTrue((_calledThen).booleanValue());
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
  }
  
  @Test
  public void testObservable() {
    final AtomicInteger count1 = new AtomicInteger(0);
    final AtomicInteger count2 = new AtomicInteger(0);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Publisher<Integer> publisher = StreamExtensions.<Integer>publish(s);
    final Stream<Integer> s1 = StreamExtensions.<Integer>stream(publisher);
    final Stream<Integer> s2 = StreamExtensions.<Integer>stream(publisher);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        count1.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(s1, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        count2.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(s2, _function_1);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    int _get = count1.get();
    Assert.assertEquals(6, _get);
    int _get_1 = count2.get();
    Assert.assertEquals(6, _get_1);
    s1.close();
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(4));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(5));
    int _get_2 = count1.get();
    Assert.assertEquals(6, _get_2);
    int _get_3 = count2.get();
    Assert.assertEquals(15, _get_3);
  }
  
  @Test
  public void testMap() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final Stream<Integer> mapped = StreamExtensions.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(6));
    StreamAssert.<Integer>assertStreamContains(mapped, _value, _value_1, _value_2, _finish_1, _value_3, _value_4);
  }
  
  @Test
  public void testFilter() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> filtered = StreamExtensions.<Integer>filter(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(4));
    StreamAssert.<Integer>assertStreamContains(filtered, _value, _finish_1, _value_1);
  }
  
  @Test
  public void testSplit() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> split = StreamExtensions.<Integer>split(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(2));
    Finish<Integer> _finish_2 = StreamExtensions.<Integer>finish(0);
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Finish<Integer> _finish_3 = StreamExtensions.<Integer>finish(0);
    Finish<Integer> _finish_4 = StreamExtensions.<Integer>finish(1);
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish_5 = StreamExtensions.<Integer>finish(0);
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(5));
    Finish<Integer> _finish_6 = StreamExtensions.<Integer>finish(0);
    Finish<Integer> _finish_7 = StreamExtensions.<Integer>finish(1);
    StreamAssert.<Integer>assertStreamContains(split, _value, _value_1, _finish_2, _value_2, _finish_3, _finish_4, _value_3, _finish_5, _value_4, _finish_6, _finish_7);
  }
  
  @Test
  public void testMerge() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish(0);
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish(1);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, _finish_1);
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(4));
    Finish<Integer> _finish_2 = StreamExtensions.<Integer>finish(0);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_2);
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(5));
    final Stream<Integer> merged = StreamExtensions.<Integer>merge(s);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Finish<Integer> _finish_3 = StreamExtensions.<Integer>finish();
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(5));
    StreamAssert.<Integer>assertStreamContains(merged, _value, _value_1, _value_2, _finish_3, _value_3, _value_4);
  }
  
  @Test
  public void testCollect() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(6));
    final Stream<List<Integer>> collected = StreamExtensions.<Integer>collect(s);
    Value<List<Integer>> _value = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Value<List<Integer>> _value_1 = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(4), Integer.valueOf(5))));
    StreamAssert.<List<Integer>>assertStreamContains(collected, _value, _value_1);
  }
  
  @Test
  public void testDoubleCollect() {
    IntegerRange _upTo = new IntegerRange(1, 11);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    final Stream<Integer> split = StreamExtensions.<Integer>split(s, _function);
    final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> split2 = StreamExtensions.<Integer>split(split, _function_1);
    final Stream<List<Integer>> collect = StreamExtensions.<Integer>collect(split2);
    final Stream<List<List<Integer>>> collect2 = StreamExtensions.<List<Integer>>collect(collect);
    final Stream<List<List<List<Integer>>>> collect3 = StreamExtensions.<List<List<Integer>>>collect(collect2);
    IPromise<List<List<List<Integer>>>> _first = StreamExtensions.<List<List<List<Integer>>>>first(collect3);
    final Procedure1<List<List<List<Integer>>>> _function_2 = new Procedure1<List<List<List<Integer>>>>() {
      public void apply(final List<List<List<Integer>>> it) {
        Assert.assertEquals(it, 
          Collections.<List<List<Integer>>>unmodifiableList(CollectionLiterals.<List<List<Integer>>>newArrayList(Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))), Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(3), Integer.valueOf(4))))), Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(5), Integer.valueOf(6))), Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(7), Integer.valueOf(8))))), Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(9), Integer.valueOf(10))), Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(11))))))));
      }
    };
    _first.then(_function_2);
  }
  
  @Test
  public void testGuardDoubleSplits() {
    IntegerRange _upTo = new IntegerRange(1, 11);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    final Stream<Integer> split = StreamExtensions.<Integer>split(s, _function);
    final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    final Stream<Integer> split2 = StreamExtensions.<Integer>split(split, _function_1);
    final Function1<Integer, Boolean> _function_2 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> split3 = StreamExtensions.<Integer>split(split2, _function_2);
    final Stream<List<Integer>> collect = StreamExtensions.<Integer>collect(split3);
    final Stream<List<List<Integer>>> collect2 = StreamExtensions.<List<Integer>>collect(collect);
    final Stream<List<List<List<Integer>>>> collect3 = StreamExtensions.<List<List<Integer>>>collect(collect2);
    final Stream<List<List<List<List<Integer>>>>> collect4 = StreamExtensions.<List<List<List<Integer>>>>collect(collect3);
    IPromise<List<List<List<List<Integer>>>>> _first = StreamExtensions.<List<List<List<List<Integer>>>>>first(collect4);
    final Procedure1<List<List<List<List<Integer>>>>> _function_3 = new Procedure1<List<List<List<List<Integer>>>>>() {
      public void apply(final List<List<List<List<Integer>>>> it) {
        Assert.assertEquals(it, 
          Collections.<List<List<List<Integer>>>>unmodifiableList(CollectionLiterals.<List<List<List<Integer>>>>newArrayList(Collections.<List<List<Integer>>>unmodifiableList(CollectionLiterals.<List<List<Integer>>>newArrayList(Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))), Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(3))))), Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(4))))))), Collections.<List<List<Integer>>>unmodifiableList(CollectionLiterals.<List<List<Integer>>>newArrayList(Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(5), Integer.valueOf(6))))), Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(7), Integer.valueOf(8))))))), Collections.<List<List<Integer>>>unmodifiableList(CollectionLiterals.<List<List<Integer>>>newArrayList(Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(9))))), Collections.<List<Integer>>unmodifiableList(CollectionLiterals.<List<Integer>>newArrayList(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(10))), Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(11))))))))));
      }
    };
    _first.then(_function_3);
  }
  
  @Test
  public void testSum() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Double> summed = StreamExtensions.<Integer>sum(s);
    Value<Double> _value = StreamAssert.<Double>value(Double.valueOf(6D));
    Value<Double> _value_1 = StreamAssert.<Double>value(Double.valueOf(9D));
    StreamAssert.<Double>assertStreamContains(summed, _value, _value_1);
  }
  
  @Test
  public void testAvg() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Double> avg = StreamExtensions.<Integer>average(s);
    Value<Double> _value = StreamAssert.<Double>value(Double.valueOf(2D));
    Value<Double> _value_1 = StreamAssert.<Double>value(Double.valueOf(4.5D));
    StreamAssert.<Double>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testMax() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    Stream<Integer> _doubleLessThan_7 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_8 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Stream<Integer> avg = StreamExtensions.<Integer>max(s);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(8));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(7));
    StreamAssert.<Integer>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testMin() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    Stream<Integer> _doubleLessThan_7 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_8 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Stream<Integer> avg = StreamExtensions.<Integer>min(s);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(4));
    StreamAssert.<Integer>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testAll() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    Stream<Integer> _doubleLessThan_7 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_8 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() > 3));
      }
    };
    final Stream<Boolean> avg = StreamExtensions.<Integer>all(s, _function);
    Value<Boolean> _value = StreamAssert.<Boolean>value(Boolean.valueOf(false));
    Value<Boolean> _value_1 = StreamAssert.<Boolean>value(Boolean.valueOf(true));
    StreamAssert.<Boolean>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testNone() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    Stream<Integer> _doubleLessThan_7 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_8 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() < 3));
      }
    };
    final Stream<Boolean> avg = StreamExtensions.<Integer>none(s, _function);
    Value<Boolean> _value = StreamAssert.<Boolean>value(Boolean.valueOf(false));
    Value<Boolean> _value_1 = StreamAssert.<Boolean>value(Boolean.valueOf(true));
    StreamAssert.<Boolean>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testFirstMatch() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Stream<Integer> _doubleLessThan_6 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    Stream<Integer> _doubleLessThan_7 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_8 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_9 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    Stream<Integer> _doubleLessThan_10 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_9, Integer.valueOf(1));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_10, Integer.valueOf(10));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> first = StreamExtensions.<Integer>first(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(8));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer>assertStreamContains(first, _value, _value_1, _value_2);
  }
  
  @Test
  public void testCount() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Integer> counted = StreamExtensions.<Integer>count(s);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(2));
    StreamAssert.<Integer>assertStreamContains(counted, _value, _value_1);
  }
  
  @Test
  public void testReduce() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    final Stream<Integer> summed = StreamExtensions.<Integer, Integer>reduce(s, Integer.valueOf(1), _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(7));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer>assertStreamContains(summed, _value, _value_1);
  }
  
  @Test
  public void testScan() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    final Stream<Integer> summed = StreamExtensions.<Integer, Integer>scan(s, Integer.valueOf(1), _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(7));
    Finish<Integer> _finish_2 = StreamExtensions.<Integer>finish();
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(10));
    Finish<Integer> _finish_3 = StreamExtensions.<Integer>finish();
    StreamAssert.<Integer>assertStreamContains(summed, _value, _value_1, _value_2, _finish_2, _value_3, _value_4, _finish_3);
  }
  
  @Test
  public void testFlatten() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    IntegerRange _upTo_1 = new IntegerRange(11, 20);
    IntegerRange _upTo_2 = new IntegerRange(21, 30);
    final Function1<IntegerRange, Stream<Integer>> _function = new Function1<IntegerRange, Stream<Integer>>() {
      public Stream<Integer> apply(final IntegerRange it) {
        return StreamExtensions.<Integer>stream(it);
      }
    };
    List<Stream<Integer>> _map = ListExtensions.<IntegerRange, Stream<Integer>>map(Collections.<IntegerRange>unmodifiableList(CollectionLiterals.<IntegerRange>newArrayList(_upTo, _upTo_1, _upTo_2)), _function);
    Stream<Stream<Integer>> _stream = StreamExtensions.<Stream<Integer>>stream(_map);
    Stream<Integer> _flatten = StreamExtensions.<Integer>flatten(_stream);
    IntegerRange _upTo_3 = new IntegerRange(1, 30);
    final Function1<Integer, Value<Integer>> _function_1 = new Function1<Integer, Value<Integer>>() {
      public Value<Integer> apply(final Integer it) {
        return StreamAssert.<Integer>value(it);
      }
    };
    Iterable<Value<Integer>> _map_1 = IterableExtensions.<Integer, Value<Integer>>map(_upTo_3, _function_1);
    StreamAssert.<Integer>assertStreamContains(_flatten, ((Entry<Integer>[])Conversions.unwrapArray(_map_1, Entry.class)));
  }
  
  @Test
  public void testFlatMap() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    IntegerRange _upTo_1 = new IntegerRange(11, 20);
    IntegerRange _upTo_2 = new IntegerRange(21, 30);
    Stream<IntegerRange> _stream = StreamExtensions.<IntegerRange>stream(Collections.<IntegerRange>unmodifiableList(CollectionLiterals.<IntegerRange>newArrayList(_upTo, _upTo_1, _upTo_2)));
    final Function1<IntegerRange, Stream<Integer>> _function = new Function1<IntegerRange, Stream<Integer>>() {
      public Stream<Integer> apply(final IntegerRange it) {
        return StreamExtensions.<Integer>stream(it);
      }
    };
    Stream<Integer> _flatMap = StreamExtensions.<IntegerRange, Integer>flatMap(_stream, _function);
    IntegerRange _upTo_3 = new IntegerRange(1, 30);
    final Function1<Integer, Value<Integer>> _function_1 = new Function1<Integer, Value<Integer>>() {
      public Value<Integer> apply(final Integer it) {
        return StreamAssert.<Integer>value(it);
      }
    };
    Iterable<Value<Integer>> _map = IterableExtensions.<Integer, Value<Integer>>map(_upTo_3, _function_1);
    StreamAssert.<Integer>assertStreamContains(_flatMap, ((Entry<Integer>[])Conversions.unwrapArray(_map, Entry.class)));
  }
  
  @Test
  public void testLimit() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExtensions.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExtensions.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExtensions.<Long>finish();
    final Stream<Long> s = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Long> limited = StreamExtensions.<Long>limit(s, 1);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(1L));
    Finish<Long> _finish_2 = StreamExtensions.<Long>finish();
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(4L));
    Finish<Long> _finish_3 = StreamExtensions.<Long>finish();
    StreamAssert.<Long>assertStreamContains(limited, _value, _finish_2, _value_1, _finish_3);
  }
  
  @Test
  public void testLimitBeforeCollect() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExtensions.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExtensions.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExtensions.<Long>finish();
    final Stream<Long> s = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    Stream<Long> _limit = StreamExtensions.<Long>limit(s, 1);
    final Stream<List<Long>> limited = StreamExtensions.<Long>collect(_limit);
    Value<List<Long>> _value = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(1L))));
    Value<List<Long>> _value_1 = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(4L))));
    StreamAssert.<List<Long>>assertStreamContains(limited, _value, _value_1);
  }
  
  @Test
  public void testUntil() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExtensions.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Stream<Long> _doubleLessThan_3 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_2, Long.valueOf(4L));
    Finish<Long> _finish = StreamExtensions.<Long>finish();
    Stream<Long> _doubleLessThan_4 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_3, _finish);
    Stream<Long> _doubleLessThan_5 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_6 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_5, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_7 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_6, Long.valueOf(5L));
    Stream<Long> _doubleLessThan_8 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_7, Long.valueOf(6L));
    Finish<Long> _finish_1 = StreamExtensions.<Long>finish();
    final Stream<Long> s = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    final Stream<Long> untilled = StreamExtensions.<Long>until(s, _function);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(1L));
    Finish<Long> _finish_2 = StreamExtensions.<Long>finish();
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(4L));
    Finish<Long> _finish_3 = StreamExtensions.<Long>finish();
    StreamAssert.<Long>assertStreamContains(untilled, _value, _finish_2, _value_1, _finish_3);
  }
  
  @Test
  public void testUntil2() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExtensions.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Stream<Long> _doubleLessThan_3 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_2, Long.valueOf(4L));
    Finish<Long> _finish = StreamExtensions.<Long>finish();
    Stream<Long> _doubleLessThan_4 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_3, _finish);
    Stream<Long> _doubleLessThan_5 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_6 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_5, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_7 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_6, Long.valueOf(5L));
    Stream<Long> _doubleLessThan_8 = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_7, Long.valueOf(6L));
    Finish<Long> _finish_1 = StreamExtensions.<Long>finish();
    final Stream<Long> s = StreamExtensions.<Long>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    Stream<Long> _until = StreamExtensions.<Long>until(s, _function);
    final Stream<List<Long>> untilled = StreamExtensions.<Long>collect(_until);
    Value<List<Long>> _value = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(1L))));
    Value<List<Long>> _value_1 = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(4L))));
    StreamAssert.<List<Long>>assertStreamContains(untilled, _value, _value_1);
  }
  
  @Test
  public void testAnyMatchNoFinish() {
    Stream<Boolean> _stream = StreamExtensions.<Boolean>stream(Boolean.class);
    Stream<Boolean> _doubleLessThan = StreamExtensions.<Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_1 = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_2 = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(true));
    final Stream<Boolean> s = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan_2, Boolean.valueOf(false));
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    Stream<Boolean> _any = StreamExtensions.<Boolean>any(s, _function);
    final IPromise<Boolean> matches = StreamExtensions.<Boolean>first(_any);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(true));
  }
  
  @Test
  public void testAnyMatchWithFinish() {
    Stream<Boolean> _stream = StreamExtensions.<Boolean>stream(Boolean.class);
    Stream<Boolean> _doubleLessThan = StreamExtensions.<Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_1 = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_2 = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(false));
    Finish<Boolean> _finish = StreamExtensions.<Boolean>finish();
    final Stream<Boolean> s = StreamExtensions.<Boolean>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    Stream<Boolean> _any = StreamExtensions.<Boolean>any(s, _function);
    final IPromise<Boolean> matches = StreamExtensions.<Boolean>first(_any);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(false));
  }
  
  @Test
  public void testFragment() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    Stream<Integer> _split = StreamExtensions.<Integer>split(_stream, _function);
    Stream<List<Integer>> _collect = StreamExtensions.<Integer>collect(_split);
    Stream<Integer> _separate = StreamExtensions.<Integer>separate(_collect);
    Stream<List<Integer>> _collect_1 = StreamExtensions.<Integer>collect(_separate);
    IPromise<List<Integer>> _first = StreamExtensions.<List<Integer>>first(_collect_1);
    IntegerRange _upTo_1 = new IntegerRange(1, 10);
    List<Integer> _list = IterableExtensions.<Integer>toList(_upTo_1);
    StreamAssert.<Integer>assertPromiseEquals(_first, _list);
  }
  
  @Test
  public void testResolve() {
    final Promise<Integer> t1 = PromiseExtensions.<Integer>promise(int.class);
    final Promise<Integer> t2 = PromiseExtensions.<Integer>promise(int.class);
    Stream<Promise<Integer>> _stream = StreamExtensions.<Promise<Integer>>stream(Collections.<Promise<Integer>>unmodifiableList(CollectionLiterals.<Promise<Integer>>newArrayList(t1, t2)));
    final Stream<Integer> s = StreamExtensions.<Integer, Object>resolve(_stream);
    final Procedure1<Entry<Integer>> _function = new Procedure1<Entry<Integer>>() {
      public void apply(final Entry<Integer> it) {
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            InputOutput.<Integer>println(((Value<Integer>)it).value);
            s.next();
          }
        }
      }
    };
    s.onChange(_function);
    InputOutput.<String>println("start");
    s.next();
    InputOutput.<String>println("A");
    t1.set(Integer.valueOf(1));
    InputOutput.<String>println("B");
    InputOutput.<String>println("C");
    t2.set(Integer.valueOf(2));
    InputOutput.<String>println("D");
    InputOutput.<String>println("E");
  }
  
  @Test
  public void testResolving() {
    try {
      final Function1<String, IPromise<String>> _function = new Function1<String, IPromise<String>>() {
        public IPromise<String> apply(final String x) {
          final Callable<String> _function = new Callable<String>() {
            public String call() throws Exception {
              String _xblockexpression = null;
              {
                IntegerRange _upTo = new IntegerRange(1, 5);
                for (final Integer i : _upTo) {
                  {
                    Thread.sleep(10);
                    InputOutput.<String>println((x + i));
                  }
                }
                _xblockexpression = x;
              }
              return _xblockexpression;
            }
          };
          return ExecutorExtensions.<String>promise(TestStreamExtensions.this.threads, _function);
        }
      };
      final Function1<String, IPromise<String>> doSomethingAsync = _function;
      final Stream<String> s = StreamExtensions.<String>stream(String.class);
      Stream<String> _doubleLessThan = StreamExtensions.<String>operator_doubleLessThan(s, "a");
      Stream<String> _doubleLessThan_1 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan, "b");
      Stream<String> _doubleLessThan_2 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_1, "c");
      Finish<String> _finish = StreamExtensions.<String>finish();
      Stream<String> _doubleLessThan_3 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_2, _finish);
      Stream<String> _doubleLessThan_4 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_3, "d");
      Stream<String> _doubleLessThan_5 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_4, "e");
      Finish<String> _finish_1 = StreamExtensions.<String>finish();
      Stream<String> _doubleLessThan_6 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
      Stream<String> _doubleLessThan_7 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_6, "f");
      Finish<String> _finish_2 = StreamExtensions.<String>finish();
      StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_7, _finish_2);
      Collection<Entry<String>> _queue = s.getQueue();
      InputOutput.<Collection<Entry<String>>>println(_queue);
      final Function1<String, String> _function_1 = new Function1<String, String>() {
        public String apply(final String it) {
          return it;
        }
      };
      Stream<String> _map = StreamExtensions.<String, String>map(s, _function_1);
      Stream<IPromise<String>> _map_1 = StreamExtensions.<String, IPromise<String>>map(_map, doSomethingAsync);
      Stream<String> _resolve = StreamExtensions.<String, Object>resolve(_map_1, 3);
      Stream<List<String>> _collect = StreamExtensions.<String>collect(_resolve);
      final Procedure1<List<String>> _function_2 = new Procedure1<List<String>>() {
        public void apply(final List<String> it) {
          InputOutput.<String>println(("got: " + it));
        }
      };
      StreamExtensions.<List<String>>onEach(_collect, _function_2);
      Stream<String> _doubleLessThan_8 = StreamExtensions.<String>operator_doubleLessThan(s, "f");
      Stream<String> _doubleLessThan_9 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_8, "g");
      Finish<String> _finish_3 = StreamExtensions.<String>finish();
      Stream<String> _doubleLessThan_10 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_9, _finish_3);
      Stream<String> _doubleLessThan_11 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_10, "h");
      Finish<String> _finish_4 = StreamExtensions.<String>finish();
      StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_11, _finish_4);
      Stream<String> _doubleLessThan_12 = StreamExtensions.<String>operator_doubleLessThan(s, "d");
      Stream<String> _doubleLessThan_13 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_12, "e");
      Finish<String> _finish_5 = StreamExtensions.<String>finish();
      StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_13, _finish_5);
      Stream<String> _doubleLessThan_14 = StreamExtensions.<String>operator_doubleLessThan(s, "a");
      Stream<String> _doubleLessThan_15 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_14, "b");
      Stream<String> _doubleLessThan_16 = StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_15, "c");
      Finish<String> _finish_6 = StreamExtensions.<String>finish();
      StreamExtensions.<String>operator_doubleLessThan(_doubleLessThan_16, _finish_6);
      Thread.sleep(100);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testFirst() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(3));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(4));
    IPromise<Integer> _first = StreamExtensions.<Integer>first(s);
    StreamAssert.<Integer>assertPromiseEquals(_first, Integer.valueOf(2));
  }
  
  @Test
  public void testLast() {
    IntegerRange _upTo = new IntegerRange(1, 1000000);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    IPromise<Integer> _last = StreamExtensions.<Integer>last(s);
    StreamAssert.<Integer>assertPromiseEquals(_last, Integer.valueOf(1000000));
  }
  
  @Test
  public void testSkipAndTake() {
    IntegerRange _upTo = new IntegerRange(1, 1000000000);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    Stream<Integer> _skip = StreamExtensions.<Integer>skip(s, 3);
    Stream<Integer> _take = StreamExtensions.<Integer>take(_skip, 3);
    Stream<List<Integer>> _collect = StreamExtensions.<Integer>collect(_take);
    IPromise<List<Integer>> _first = StreamExtensions.<List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6))));
  }
  
  @Test
  public void testFirstAfterCollect() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    Stream<Integer> _doubleLessThan_2 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    Stream<Integer> _doubleLessThan_3 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_4 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExtensions.<Integer>finish();
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish_1);
    Stream<List<Integer>> _collect = StreamExtensions.<Integer>collect(s);
    IPromise<List<Integer>> _first = StreamExtensions.<List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
  }
  
  @Test
  public void testStreamForwardTo() {
    IntegerRange _upTo = new IntegerRange(1, 1000000);
    final Stream<Integer> s1 = StreamExtensions.<Integer>stream(_upTo);
    final Stream<Integer> s2 = StreamExtensions.<Integer>stream(int.class);
    StreamExtensions.<Integer>forwardTo(s1, s2);
    Stream<Integer> _count = StreamExtensions.<Integer>count(s2);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        Assert.assertEquals(1000000, (it).intValue(), 0);
      }
    };
    StreamExtensions.<Integer>then(_count, _function);
  }
  
  @Test
  public void testThrottle() {
    IntegerRange _upTo = new IntegerRange(1, 1000);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    Stream<Integer> _throttle = StreamExtensions.<Integer>throttle(_stream, 1);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    StreamExtensions.<Integer>onEach(_throttle, _function);
  }
  
  public void testLatest() {
  }
  
  @Test
  public void testFileStreaming() {
    final File file = new File("gradle.properties");
    Stream<List<Byte>> _stream = StreamExtensions.stream(file);
    Stream<String> _text = StreamExtensions.toText(_stream);
    final Function1<String, String> _function = new Function1<String, String>() {
      public String apply(final String it) {
        return ("- " + it);
      }
    };
    Stream<String> _map = StreamExtensions.<String, String>map(_text, _function);
    final Procedure1<Finish<String>> _function_1 = new Procedure1<Finish<String>>() {
      public void apply(final Finish<String> it) {
        InputOutput.<String>println("finish");
      }
    };
    Subscription<String> _onFinish = StreamExtensions.<String>onFinish(_map, _function_1);
    final Procedure1<String> _function_2 = new Procedure1<String>() {
      public void apply(final String it) {
        InputOutput.<String>println(it);
      }
    };
    StreamExtensions.<String>onEach(_onFinish, _function_2);
  }
  
  @Test
  public void testStreamToFileAndFileCopy() {
    final List<String> data = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Hello,", "This is some text", "Please make this into a nice file!"));
    Stream<String> _stream = StreamExtensions.<String>stream(data);
    Stream<List<Byte>> _bytes = StreamExtensions.toBytes(_stream);
    File _file = new File("test.txt");
    StreamExtensions.writeTo(_bytes, _file);
    final File source = new File("test.txt");
    final File destination = new File("text2.txt");
    Stream<List<Byte>> _stream_1 = StreamExtensions.stream(source);
    Task _writeTo = StreamExtensions.writeTo(_stream_1, destination);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        source.delete();
        destination.delete();
      }
    };
    _writeTo.then(_function);
  }
  
  private Boolean setCalledThen(final Boolean value) {
    return this._calledThen.getAndSet(value);
  }
  
  private Boolean getCalledThen() {
    return this._calledThen.get();
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
}
