package nl.kii.stream.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.observe.Publisher;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.stream.AsyncSubscription;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
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
    StreamAssert.<Integer>assertStreamEquals(s2, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)));
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
    StreamAssert.<Integer>assertStreamEquals(s2, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)));
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
    StreamAssert.<Pair<Integer, String>>assertStreamEquals(s2, Collections.<Entry<Pair<Integer, String>>>unmodifiableList(CollectionLiterals.<Entry<Pair<Integer, String>>>newArrayList(_value, _value_1, _finish)));
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
    AsyncSubscription<Integer> _onError = StreamExtensions.<Integer>onError(_map_1, _function_2);
    final Procedure1<Finish<Integer>> _function_3 = new Procedure1<Finish<Integer>>() {
      public void apply(final Finish<Integer> it) {
        finished.set(true);
      }
    };
    AsyncSubscription<Integer> _onFinish = StreamExtensions.<Integer>onFinish(_onError, _function_3);
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
    StreamAssert.<Integer>assertStreamEquals(mapped, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish_1, _value_3, _value_4)));
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
    StreamAssert.<Integer>assertStreamEquals(filtered, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _finish_1, _value_1)));
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
    StreamAssert.<Integer>assertStreamEquals(split, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _value_1, _finish_2, _value_2, _finish_3, _finish_4, _value_3, _finish_5, _value_4, _finish_6, _finish_7)));
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
    StreamAssert.<Integer>assertStreamEquals(merged, Collections.<Entry<Integer>>unmodifiableList(CollectionLiterals.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish_3, _value_3, _value_4)));
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
    StreamAssert.<List<Integer>>assertStreamEquals(collected, Collections.<Value<List<Integer>>>unmodifiableList(CollectionLiterals.<Value<List<Integer>>>newArrayList(_value, _value_1)));
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
    Promise<List<List<List<Integer>>>> _first = StreamExtensions.<List<List<List<Integer>>>>first(collect3);
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
    Promise<List<List<List<List<Integer>>>>> _first = StreamExtensions.<List<List<List<List<Integer>>>>>first(collect4);
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
    StreamAssert.<Double>assertStreamEquals(summed, Collections.<Value<Double>>unmodifiableList(CollectionLiterals.<Value<Double>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Double>assertStreamEquals(avg, Collections.<Value<Double>>unmodifiableList(CollectionLiterals.<Value<Double>>newArrayList(_value, _value_1)));
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
    final Stream<Long> counted = StreamExtensions.<Integer>count(s);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(3L));
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(2L));
    StreamAssert.<Long>assertStreamEquals(counted, Collections.<Value<Long>>unmodifiableList(CollectionLiterals.<Value<Long>>newArrayList(_value, _value_1)));
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
    final Stream<Integer> summed = StreamExtensions.<Integer>reduce(s, Integer.valueOf(1), _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(7));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer>assertStreamEquals(summed, Collections.<Value<Integer>>unmodifiableList(CollectionLiterals.<Value<Integer>>newArrayList(_value, _value_1)));
  }
  
  @Test
  public void testReduceWithCounter() {
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
    final Function3<Long, Long, Long, Long> _function = new Function3<Long, Long, Long, Long>() {
      public Long apply(final Long a, final Long b, final Long c) {
        return Long.valueOf(((a).longValue() + (c).longValue()));
      }
    };
    final Stream<Long> summed = StreamExtensions.<Long>reduce(s, Long.valueOf(0L), _function);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(3L));
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(1L));
    StreamAssert.<Long>assertStreamEquals(summed, Collections.<Value<Long>>unmodifiableList(CollectionLiterals.<Value<Long>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Long>assertStreamEquals(limited, Collections.<Entry<Long>>unmodifiableList(CollectionLiterals.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _finish_3)));
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
    StreamAssert.<List<Long>>assertStreamEquals(limited, Collections.<Value<List<Long>>>unmodifiableList(CollectionLiterals.<Value<List<Long>>>newArrayList(_value, _value_1)));
  }
  
  @Test
  public void testUntil() {
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
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    final Stream<Long> untilled = StreamExtensions.<Long>until(s, _function);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(1L));
    Finish<Long> _finish_2 = StreamExtensions.<Long>finish();
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(4L));
    Value<Long> _value_2 = StreamAssert.<Long>value(Long.valueOf(5L));
    Finish<Long> _finish_3 = StreamExtensions.<Long>finish();
    StreamAssert.<Long>assertStreamEquals(untilled, Collections.<Entry<Long>>unmodifiableList(CollectionLiterals.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _value_2, _finish_3)));
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
    Stream<Boolean> _anyMatch = StreamExtensions.<Boolean>anyMatch(s, _function);
    final Promise<Boolean> matches = StreamExtensions.<Boolean>first(_anyMatch);
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
    Stream<Boolean> _anyMatch = StreamExtensions.<Boolean>anyMatch(s, _function);
    final Promise<Boolean> matches = StreamExtensions.<Boolean>first(_anyMatch);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(false));
  }
  
  @Test
  public void testResolving() {
    try {
      final Function1<String, Promise<String>> _function = new Function1<String, Promise<String>>() {
        public Promise<String> apply(final String x) {
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
          return PromiseExtensions.<String>async(TestStreamExtensions.this.threads, _function);
        }
      };
      final Function1<String, Promise<String>> doSomethingAsync = _function;
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
      Stream<Promise<String>> _map_1 = StreamExtensions.<String, Promise<String>>map(_map, doSomethingAsync);
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
      Thread.sleep(1000);
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
    Promise<Integer> _first = StreamExtensions.<Integer>first(s);
    StreamAssert.<Integer>assertPromiseEquals(_first, Integer.valueOf(2));
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
    Promise<List<Integer>> _first = StreamExtensions.<List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
  }
}
