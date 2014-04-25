package nl.kii.stream.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExt;
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
public class TestStreamExt {
  @Test
  public void testRangeStream() {
    IntegerRange _upTo = new IntegerRange(5, 7);
    final Stream<Integer> s = StreamExt.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return it;
      }
    };
    final Stream<Integer> s2 = StreamExt.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(6));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(7));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    StreamAssert.<Integer>assertStreamEquals(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)), s2);
  }
  
  @Test
  public void testListStream() {
    final Stream<Integer> s = StreamExt.<Integer>stream(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2, 3)));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final Stream<Integer> s2 = StreamExt.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    StreamAssert.<Integer>assertStreamEquals(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish)), s2);
  }
  
  @Test
  public void testMapStream() {
    Map<Integer, String> _xsetliteral = null;
    Map<Integer, String> _tempMap = Maps.<Integer, String>newHashMap();
    _tempMap.put(Integer.valueOf(1), "a");
    _tempMap.put(Integer.valueOf(2), "b");
    _xsetliteral = Collections.<Integer, String>unmodifiableMap(_tempMap);
    final Map<Integer, String> map = _xsetliteral;
    final Stream<Pair<Integer, String>> s = StreamExt.<Integer, String>stream(map);
    final Function1<Pair<Integer, String>, Pair<Integer, String>> _function = new Function1<Pair<Integer, String>, Pair<Integer, String>>() {
      public Pair<Integer, String> apply(final Pair<Integer, String> it) {
        Integer _key = it.getKey();
        int _plus = ((_key).intValue() + 1);
        String _value = it.getValue();
        return Pair.<Integer, String>of(Integer.valueOf(_plus), _value);
      }
    };
    final Stream<Pair<Integer, String>> s2 = StreamExt.<Pair<Integer, String>, Pair<Integer, String>>map(s, _function);
    Pair<Integer, String> _mappedTo = Pair.<Integer, String>of(Integer.valueOf(2), "a");
    Value<Pair<Integer, String>> _value = StreamAssert.<Pair<Integer, String>>value(_mappedTo);
    Pair<Integer, String> _mappedTo_1 = Pair.<Integer, String>of(Integer.valueOf(3), "b");
    Value<Pair<Integer, String>> _value_1 = StreamAssert.<Pair<Integer, String>>value(_mappedTo_1);
    Finish<Pair<Integer, String>> _finish = StreamExt.<Pair<Integer, String>>finish();
    StreamAssert.<Pair<Integer, String>>assertStreamEquals(Collections.<Entry<Pair<Integer, String>>>unmodifiableList(Lists.<Entry<Pair<Integer, String>>>newArrayList(_value, _value_1, _finish)), s2);
  }
  
  @Test
  public void testMap() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final Stream<Integer> mapped = StreamExt.<Integer, Integer>map(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(5));
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(6));
    StreamAssert.<Integer>assertStreamEquals(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _value_2, _finish_1, _value_3, _value_4)), mapped);
  }
  
  @Test
  public void testFilter() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> filtered = StreamExt.<Integer>filter(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(2));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(4));
    StreamAssert.<Integer>assertStreamEquals(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _finish_1, _value_1)), filtered);
  }
  
  @Test
  public void testSplit() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final Stream<Integer> split = StreamExt.<Integer>split(s, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(1));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(2));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(3));
    Finish<Integer> _finish_2 = StreamExt.<Integer>finish();
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(4));
    Finish<Integer> _finish_3 = StreamExt.<Integer>finish();
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(5));
    StreamAssert.<Integer>assertStreamEquals(Collections.<Entry<Integer>>unmodifiableList(Lists.<Entry<Integer>>newArrayList(_value, _value_1, _finish_1, _value_2, _finish_2, _value_3, _finish_3, _value_4)), split);
  }
  
  @Test
  public void testCollect() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    Stream<Integer> _split = StreamExt.<Integer>split(s, _function);
    final Stream<List<Integer>> collected = StreamExt.<Integer>collect(_split);
    Value<List<Integer>> _value = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(1, 2)));
    Value<List<Integer>> _value_1 = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(3)));
    Value<List<Integer>> _value_2 = StreamAssert.<List<Integer>>value(Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(4)));
    StreamAssert.<List<Integer>>assertStreamEquals(Collections.<Value<List<Integer>>>unmodifiableList(Lists.<Value<List<Integer>>>newArrayList(_value, _value_1, _value_2)), collected);
  }
  
  @Test
  public void testSum() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Double> summed = StreamExt.<Integer>sum(s);
    Value<Double> _value = StreamAssert.<Double>value(Double.valueOf(6D));
    Value<Double> _value_1 = StreamAssert.<Double>value(Double.valueOf(9D));
    StreamAssert.<Double>assertStreamEquals(Collections.<Value<Double>>unmodifiableList(Lists.<Value<Double>>newArrayList(_value, _value_1)), summed);
  }
  
  @Test
  public void testAvg() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Double> avg = StreamExt.<Integer>avg(s);
    Value<Double> _value = StreamAssert.<Double>value(Double.valueOf(2D));
    Value<Double> _value_1 = StreamAssert.<Double>value(Double.valueOf(4.5D));
    StreamAssert.<Double>assertStreamEquals(Collections.<Value<Double>>unmodifiableList(Lists.<Value<Double>>newArrayList(_value, _value_1)), avg);
  }
  
  @Test
  public void testCount() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Long> counted = StreamExt.<Integer>count(s);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(3L));
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(2L));
    StreamAssert.<Long>assertStreamEquals(Collections.<Value<Long>>unmodifiableList(Lists.<Value<Long>>newArrayList(_value, _value_1)), counted);
  }
  
  @Test
  public void testReduce() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_5 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    final Stream<Integer> summed = StreamExt.<Integer>reduce(s, Integer.valueOf(1), _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(7));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer>assertStreamEquals(Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value, _value_1)), summed);
  }
  
  @Test
  public void testReduceWithCounter() {
    Stream<Long> _stream = StreamExt.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExt.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExt.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExt.<Long>finish();
    final Stream<Long> s = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function3<Long, Long, Long, Long> _function = new Function3<Long, Long, Long, Long>() {
      public Long apply(final Long a, final Long b, final Long c) {
        return Long.valueOf(((a).longValue() + (c).longValue()));
      }
    };
    final Stream<Long> summed = StreamExt.<Long>reduce(s, Long.valueOf(0L), _function);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(3L));
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(1L));
    StreamAssert.<Long>assertStreamEquals(Collections.<Value<Long>>unmodifiableList(Lists.<Value<Long>>newArrayList(_value, _value_1)), summed);
  }
  
  @Test
  public void testLimit() {
    Stream<Long> _stream = StreamExt.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExt.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExt.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExt.<Long>finish();
    final Stream<Long> s = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Stream<Long> limited = StreamExt.<Long>limit(s, 1);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(1L));
    Finish<Long> _finish_2 = StreamExt.<Long>finish();
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(4L));
    Finish<Long> _finish_3 = StreamExt.<Long>finish();
    StreamAssert.<Long>assertStreamEquals(Collections.<Entry<Long>>unmodifiableList(Lists.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _finish_3)), limited);
  }
  
  @Test
  public void testLimitBeforeCollect() {
    Stream<Long> _stream = StreamExt.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExt.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExt.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExt.<Long>finish();
    final Stream<Long> s = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    Stream<Long> _limit = StreamExt.<Long>limit(s, 1);
    final Stream<List<Long>> limited = StreamExt.<Long>collect(_limit);
    Value<List<Long>> _value = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(Lists.<Long>newArrayList(1L)));
    Value<List<Long>> _value_1 = StreamAssert.<List<Long>>value(Collections.<Long>unmodifiableList(Lists.<Long>newArrayList(4L)));
    StreamAssert.<List<Long>>assertStreamEquals(Collections.<Value<List<Long>>>unmodifiableList(Lists.<Value<List<Long>>>newArrayList(_value, _value_1)), limited);
  }
  
  @Test
  public void testUntil() {
    Stream<Long> _stream = StreamExt.<Long>stream(Long.class);
    Stream<Long> _doubleLessThan = StreamExt.<Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    Stream<Long> _doubleLessThan_1 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    Stream<Long> _doubleLessThan_2 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long> _finish = StreamExt.<Long>finish();
    Stream<Long> _doubleLessThan_3 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    Stream<Long> _doubleLessThan_4 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    Stream<Long> _doubleLessThan_5 = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long> _finish_1 = StreamExt.<Long>finish();
    final Stream<Long> s = StreamExt.<Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    final Stream<Long> untilled = StreamExt.<Long>until(s, _function);
    Value<Long> _value = StreamAssert.<Long>value(Long.valueOf(1L));
    Finish<Long> _finish_2 = StreamExt.<Long>finish();
    Value<Long> _value_1 = StreamAssert.<Long>value(Long.valueOf(4L));
    Value<Long> _value_2 = StreamAssert.<Long>value(Long.valueOf(5L));
    Finish<Long> _finish_3 = StreamExt.<Long>finish();
    StreamAssert.<Long>assertStreamEquals(Collections.<Entry<Long>>unmodifiableList(Lists.<Entry<Long>>newArrayList(_value, _finish_2, _value_1, _value_2, _finish_3)), untilled);
  }
  
  @Test
  public void testAnyMatchNoFinish() {
    Stream<Boolean> _stream = StreamExt.<Boolean>stream(Boolean.class);
    Stream<Boolean> _doubleLessThan = StreamExt.<Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_1 = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_2 = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(true));
    final Stream<Boolean> s = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan_2, Boolean.valueOf(false));
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    Stream<Boolean> _anyMatch = StreamExt.<Boolean>anyMatch(s, _function);
    final Promise<Boolean> matches = StreamExt.<Boolean>first(_anyMatch);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(true));
  }
  
  @Test
  public void testAnyMatchWithFinish() {
    Stream<Boolean> _stream = StreamExt.<Boolean>stream(Boolean.class);
    Stream<Boolean> _doubleLessThan = StreamExt.<Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_1 = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    Stream<Boolean> _doubleLessThan_2 = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(false));
    Finish<Boolean> _finish = StreamExt.<Boolean>finish();
    final Stream<Boolean> s = StreamExt.<Boolean>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    Stream<Boolean> _anyMatch = StreamExt.<Boolean>anyMatch(s, _function);
    final Promise<Boolean> matches = StreamExt.<Boolean>first(_anyMatch);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(false));
  }
  
  @Test
  public void testAsync() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, _finish);
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, _finish_1);
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestStreamExt.this.power2((it).intValue());
      }
    };
    Stream<Integer> _async = StreamExt.<Integer, Integer>async(s, _function);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(_async);
    final Procedure1<List<Integer>> _function_1 = new Procedure1<List<Integer>>() {
      public void apply(final List<Integer> it) {
        InputOutput.<List<Integer>>println(it);
      }
    };
    StreamExt.<List<Integer>>then(_collect, _function_1);
  }
  
  @Test
  public void testAsync3() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(4));
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(5));
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(6));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(7));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestStreamExt.this.power2((it).intValue());
      }
    };
    final Stream<Integer> asynced = StreamExt.<Integer, Integer>async(s, 3, _function);
    Value<Integer> _value = StreamAssert.<Integer>value(Integer.valueOf(4));
    Value<Integer> _value_1 = StreamAssert.<Integer>value(Integer.valueOf(9));
    Value<Integer> _value_2 = StreamAssert.<Integer>value(Integer.valueOf(16));
    Value<Integer> _value_3 = StreamAssert.<Integer>value(Integer.valueOf(25));
    Value<Integer> _value_4 = StreamAssert.<Integer>value(Integer.valueOf(36));
    Value<Integer> _value_5 = StreamAssert.<Integer>value(Integer.valueOf(49));
    StreamAssert.<Integer>assertStreamEquals(Collections.<Value<Integer>>unmodifiableList(Lists.<Value<Integer>>newArrayList(_value, _value_1, _value_2, _value_3, _value_4, _value_5)), asynced);
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExt.<Integer>promise(Integer.valueOf((i * i)));
  }
  
  @Test
  public void testFirst() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(2));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(3));
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(4));
    Promise<Integer> _first = StreamExt.<Integer>first(s);
    StreamAssert.<Integer>assertPromiseEquals(_first, Integer.valueOf(2));
  }
  
  @Test
  public void testFirstAfterCollect() {
    Stream<Integer> _stream = StreamExt.<Integer>stream(Integer.class);
    Stream<Integer> _doubleLessThan = StreamExt.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer> _finish = StreamExt.<Integer>finish();
    Stream<Integer> _doubleLessThan_2 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    Stream<Integer> _doubleLessThan_3 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    Stream<Integer> _doubleLessThan_4 = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Finish<Integer> _finish_1 = StreamExt.<Integer>finish();
    final Stream<Integer> s = StreamExt.<Integer>operator_doubleLessThan(_doubleLessThan_4, _finish_1);
    Stream<List<Integer>> _collect = StreamExt.<Integer>collect(s);
    Promise<List<Integer>> _first = StreamExt.<List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(Lists.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
  }
}
