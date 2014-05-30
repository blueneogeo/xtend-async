package nl.kii.stream.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SyncSubscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamExtensions {
  @Test
  public void testPrint() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExtensions.<Integer>finish();
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    final Procedure1<SyncSubscription<Integer>> _function = new Procedure1<SyncSubscription<Integer>>() {
      public void apply(final SyncSubscription<Integer> it) {
        final Procedure1<Integer> _function = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            InputOutput.<Integer>println(it);
          }
        };
        it.forEach(_function);
        final Procedure1<Void> _function_1 = new Procedure1<Void>() {
          public void apply(final Void it) {
            InputOutput.<String>println("finished!");
          }
        };
        it.onFinish(_function_1);
      }
    };
    StreamExtensions.<Integer>listen(s, _function);
  }
  
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
    StreamAssert.<Integer>assertStreamEquals(s2, Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)));
  }
  
  @Test
  public void testListStream() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
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
    StreamAssert.<Integer>assertStreamEquals(s2, Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)));
  }
  
  @Test
  public void testMapStream() {
    Map<Integer, String> _xsetliteral = null;
    Map<Integer, String> _tempMap = Maps.<Integer, String>newHashMap();
    _tempMap.put(Integer.valueOf(1), "a");
    _tempMap.put(Integer.valueOf(2), "b");
    _xsetliteral = Collections.<Integer, String>unmodifiableMap(_tempMap);
    final Map<Integer, String> map = _xsetliteral;
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
    Pair<Integer, String> _mappedTo = Pair.<Integer, String>of(Integer.valueOf(2), "a");
    Value<Pair<Integer, String>> _value = StreamAssert.<Pair<Integer, String>>value(_mappedTo);
    Pair<Integer, String> _mappedTo_1 = Pair.<Integer, String>of(Integer.valueOf(3), "b");
    Value<Pair<Integer, String>> _value_1 = StreamAssert.<Pair<Integer, String>>value(_mappedTo_1);
    Finish<Pair<Integer, String>> _finish = StreamExtensions.<Pair<Integer, String>>finish();
    StreamAssert.<Pair<Integer, String>>assertStreamEquals(s2, Collections.<Entry<Pair<Integer, String>>>unmodifiableList(Lists.<Entry<Pair<Integer, String>>>newArrayList(_value, _value_1, _finish)));
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
    StreamAssert.<Integer>assertStreamEquals(mapped, Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish_1, _value_3, _value_4)));
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
    StreamAssert.<Integer>assertStreamEquals(filtered, Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _finish_1, _value_1)));
  }
  
  @Test
  public void testSplit() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method split is undefined for the type TestStreamExtensions"
      + "\nType mismatch: cannot convert from Object to byte"
      + "\nassertStreamEquals cannot be resolved");
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
    Value<List<Integer>> _value = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Value<List<Integer>> _value_1 = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(4), Integer.valueOf(5))));
    StreamAssert.<List<Integer>>assertStreamEquals(collected, Collections.<Value<List<Integer>>>unmodifiableList(Lists.<Value<List<Integer>>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Double>assertStreamEquals(summed, Collections.<Value<Double>>unmodifiableList(Lists.<Value<Double>>newArrayList(_value, _value_1)));
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
    final Stream<Double> avg = StreamExtensions.<Integer>avg(s);
    Value<Double> _value = StreamAssert.<Double>value(Double.valueOf(2D));
    Value<Double> _value_1 = StreamAssert.<Double>value(Double.valueOf(4.5D));
    StreamAssert.<Double>assertStreamEquals(avg, Collections.<Value<Double>>unmodifiableList(Lists.<Value<Double>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Long>assertStreamEquals(counted, Collections.<Value<Long>>unmodifiableList(Lists.<Value<Long>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Integer>assertStreamEquals(summed, Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Long>assertStreamEquals(summed, Collections.<Value<Long>>unmodifiableList(Lists.<Value<Long>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Long>assertStreamEquals(limited, Collections.<Entry<Long>>unmodifiableList(Lists.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _finish_3)));
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
    Value<List<Long>> _value = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(Lists.<Long>newArrayList(Long.valueOf(1L))));
    Value<List<Long>> _value_1 = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(Lists.<Long>newArrayList(Long.valueOf(4L))));
    StreamAssert.<List<Long>>assertStreamEquals(limited, Collections.<Value<List<Long>>>unmodifiableList(Lists.<Value<List<Long>>>newArrayList(_value, _value_1)));
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
    StreamAssert.<Long>assertStreamEquals(untilled, Collections.<Entry<Long>>unmodifiableList(Lists.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _value_2, _finish_3)));
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
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
  }
}
