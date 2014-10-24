package nl.kii.stream.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import nl.kii.promise.SubPromise;
import nl.kii.promise.SubTask;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.SubStream;
import nl.kii.stream.Value;
import nl.kii.stream.source.StreamCopySplitter;
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
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
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
    final SubStream<Integer, Integer> s2 = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(6));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(7));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamAssert.<Integer, Integer>assertStreamContains(s2, _value, _value_1, _value_2, _finish);
  }
  
  @Test
  public void testListStream() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Collection<Entry<Integer, Integer>> _queue = s.getQueue();
    InputOutput.<Collection<Entry<Integer, Integer>>>println(_queue);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final SubStream<Integer, Integer> s2 = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamAssert.<Integer, Integer>assertStreamContains(s2, _value, _value_1, _value_2, _finish);
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
    final SubStream<Pair<Integer, String>, Pair<Integer, String>> s2 = StreamExtensions.<Pair<Integer, String>, Pair<Integer, String>, Pair<Integer, String>>map(s, _function);
    Pair<Integer, String> _mappedTo_2 = Pair.<Integer, String>of(Integer.valueOf(2), "a");
    Value<Pair<Integer, String>, Pair<Integer, String>> _value = StreamAssert.<Pair<Integer, String>, Pair<Integer, String>>value(_mappedTo_2);
    Pair<Integer, String> _mappedTo_3 = Pair.<Integer, String>of(Integer.valueOf(3), "b");
    Value<Pair<Integer, String>, Pair<Integer, String>> _value_1 = StreamAssert.<Pair<Integer, String>, Pair<Integer, String>>value(_mappedTo_3);
    Finish<Pair<Integer, String>, Pair<Integer, String>> _finish = StreamExtensions.<Pair<Integer, String>, Pair<Integer, String>>finish();
    StreamAssert.<Pair<Integer, String>, Pair<Integer, String>>assertStreamContains(s2, _value, _value_1, _finish);
  }
  
  @Test
  public void testRandomStream() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    final Stream<Integer> s = StreamExtensions.streamRandom(_upTo);
    final Procedure1<StreamHandlerBuilder<Integer, Integer>> _function = new Procedure1<StreamHandlerBuilder<Integer, Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            Assert.assertTrue(((($1).intValue() >= 1) && (($1).intValue() <= 3)));
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer, Integer>on(s, _function);
    IntegerRange _upTo_1 = new IntegerRange(1, 1000);
    for (final Integer i : _upTo_1) {
      s.next();
    }
  }
  
  @Atomic
  private final AtomicBoolean _calledThen = new AtomicBoolean();
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Test
  public void testTaskReturning() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<String>println("x");
        TestStreamExtensions.this.incCounter();
      }
    };
    StreamExtensions.<Integer, Integer>onEach(_stream, _function);
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
  }
  
  @Test
  public void testObservable() {
    final AtomicInteger count1 = new AtomicInteger(0);
    final AtomicInteger count2 = new AtomicInteger(0);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Publisher<Integer> publisher = StreamExtensions.<Integer, Integer>publish(s);
    final Stream<Integer> s1 = StreamExtensions.<Integer>stream(publisher);
    final Stream<Integer> s2 = StreamExtensions.<Integer>stream(publisher);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        count1.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer, Integer>onEach(s1, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        count2.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer, Integer>onEach(s2, _function_1);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    int _get = count1.get();
    Assert.assertEquals(6, _get);
    int _get_1 = count2.get();
    Assert.assertEquals(6, _get_1);
    s1.close();
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(4));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(5));
    int _get_2 = count1.get();
    Assert.assertEquals(6, _get_2);
    int _get_3 = count2.get();
    Assert.assertEquals(15, _get_3);
  }
  
  @Test
  public void testMap() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    final SubStream<Integer, Integer> mapped = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    Value<Integer, Integer> _value_3 = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    Value<Integer, Integer> _value_4 = StreamAssert.<Integer, Integer>value(Integer.valueOf(6));
    StreamAssert.<Integer, Integer>assertStreamContains(mapped, _value, _value_1, _value_2, _finish_1, _value_3, _value_4);
  }
  
  @Test
  public void testFilter() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final SubStream<Integer, Integer> filtered = StreamExtensions.<Integer, Integer>filter(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    StreamAssert.<Integer, Integer>assertStreamContains(filtered, _value, _finish_1, _value_1);
  }
  
  @Test
  public void testSplit() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final SubStream<Integer, Integer> split = StreamExtensions.<Integer, Integer>split(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(1));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish(0);
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish(0);
    Finish<Integer, Integer> _finish_4 = StreamExtensions.<Integer, Integer>finish(1);
    Value<Integer, Integer> _value_3 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Finish<Integer, Integer> _finish_5 = StreamExtensions.<Integer, Integer>finish(0);
    Value<Integer, Integer> _value_4 = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    Finish<Integer, Integer> _finish_6 = StreamExtensions.<Integer, Integer>finish(0);
    Finish<Integer, Integer> _finish_7 = StreamExtensions.<Integer, Integer>finish(1);
    StreamAssert.<Integer, Integer>assertStreamContains(split, _value, _value_1, _finish_2, _value_2, _finish_3, _finish_4, _value_3, _finish_5, _value_4, _finish_6, _finish_7);
  }
  
  @Test
  public void testSplitWithSkip() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 4) == 0));
      }
    };
    final SubStream<Integer, Integer> split = StreamExtensions.<Integer, Integer>split(s, _function);
    final Stream<Integer> collect = StreamExtensions.<Integer>stream(int.class);
    final Procedure1<StreamHandlerBuilder<Integer, Integer>> _function_1 = new Procedure1<StreamHandlerBuilder<Integer, Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            if ((($1).intValue() == 1)) {
              it.stream.skip();
            }
            StreamExtensions.<Integer, Integer>operator_doubleLessThan(collect, $1);
          }
        };
        it.each(_function);
        final Procedure2<Integer, Integer> _function_1 = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(($1).intValue());
            StreamExtensions.<Integer, Integer>operator_doubleLessThan(collect, _finish);
          }
        };
        it.finish(_function_1);
      }
    };
    StreamExtensions.<Integer, Integer>on(split, _function_1);
    split.next();
    split.next();
    split.next();
    split.next();
    split.next();
    split.next();
    split.next();
    split.next();
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(1));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish(0);
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish(1);
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Finish<Integer, Integer> _finish_4 = StreamExtensions.<Integer, Integer>finish(0);
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    Finish<Integer, Integer> _finish_5 = StreamExtensions.<Integer, Integer>finish(0);
    Finish<Integer, Integer> _finish_6 = StreamExtensions.<Integer, Integer>finish(1);
    StreamAssert.<Integer, Integer>assertStreamContains(collect, _value, _finish_2, _finish_3, _value_1, _finish_4, _value_2, _finish_5, _finish_6);
  }
  
  @Test
  public void testMerge() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish(1);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, _finish_1);
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(4));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_2);
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(5));
    final SubStream<Integer, Integer> merged = StreamExtensions.<Integer, Integer>merge(s);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(1));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish();
    Value<Integer, Integer> _value_3 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Value<Integer, Integer> _value_4 = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    StreamAssert.<Integer, Integer>assertStreamContains(merged, _value, _value_1, _value_2, _finish_3, _value_3, _value_4);
  }
  
  @Test
  public void testCollect() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(Integer.class);
    final SubStream<Integer, List<Integer>> collected = StreamExtensions.<Integer, Integer>collect(s);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(6));
    Value<Integer, List<Integer>> _value = StreamAssert.<Integer, List<Integer>>value(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Value<Integer, List<Integer>> _value_1 = StreamAssert.<Integer, List<Integer>>value(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(4), Integer.valueOf(5))));
    StreamAssert.<Integer, List<Integer>>assertStreamContains(collected, _value, _value_1);
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
    final SubStream<Integer, Integer> split = StreamExtensions.<Integer, Integer>split(s, _function);
    final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final SubStream<Integer, Integer> split2 = StreamExtensions.<Integer, Integer>split(split, _function_1);
    final SubStream<Integer, List<Integer>> collect = StreamExtensions.<Integer, Integer>collect(split2);
    final SubStream<Integer, List<List<Integer>>> collect2 = StreamExtensions.<Integer, List<Integer>>collect(collect);
    final SubStream<Integer, List<List<List<Integer>>>> collect3 = StreamExtensions.<Integer, List<List<Integer>>>collect(collect2);
    IPromise<Integer, List<List<List<Integer>>>> _first = StreamExtensions.<Integer, List<List<List<Integer>>>>first(collect3);
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
    final SubStream<Integer, Integer> split = StreamExtensions.<Integer, Integer>split(s, _function);
    final Function1<Integer, Boolean> _function_1 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 3) == 0));
      }
    };
    final SubStream<Integer, Integer> split2 = StreamExtensions.<Integer, Integer>split(split, _function_1);
    final Function1<Integer, Boolean> _function_2 = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final SubStream<Integer, Integer> split3 = StreamExtensions.<Integer, Integer>split(split2, _function_2);
    final SubStream<Integer, List<Integer>> collect = StreamExtensions.<Integer, Integer>collect(split3);
    final SubStream<Integer, List<List<Integer>>> collect2 = StreamExtensions.<Integer, List<Integer>>collect(collect);
    final SubStream<Integer, List<List<List<Integer>>>> collect3 = StreamExtensions.<Integer, List<List<Integer>>>collect(collect2);
    final SubStream<Integer, List<List<List<List<Integer>>>>> collect4 = StreamExtensions.<Integer, List<List<List<Integer>>>>collect(collect3);
    IPromise<Integer, List<List<List<List<Integer>>>>> _first = StreamExtensions.<Integer, List<List<List<List<Integer>>>>>first(collect4);
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
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final SubStream<Integer, Double> summed = StreamExtensions.<Integer, Integer>sum(s);
    Value<Integer, Double> _value = StreamAssert.<Integer, Double>value(Double.valueOf(6D));
    Value<Integer, Double> _value_1 = StreamAssert.<Integer, Double>value(Double.valueOf(9D));
    StreamAssert.<Integer, Double>assertStreamContains(summed, _value, _value_1);
  }
  
  @Test
  public void testAvg() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final SubStream<Integer, Double> avg = StreamExtensions.<Integer, Integer>average(s);
    Value<Integer, Double> _value = StreamAssert.<Integer, Double>value(Double.valueOf(2D));
    Value<Integer, Double> _value_1 = StreamAssert.<Integer, Double>value(Double.valueOf(4.5D));
    StreamAssert.<Integer, Double>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testMax() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final SubStream<Integer, Integer> avg = StreamExtensions.<Integer, Integer>max(s);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(8));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(7));
    StreamAssert.<Integer, Integer>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testMin() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final SubStream<Integer, Integer> avg = StreamExtensions.<Integer, Integer>min(s);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(1));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    StreamAssert.<Integer, Integer>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testAll() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() > 3));
      }
    };
    final SubStream<Integer, Boolean> avg = StreamExtensions.<Integer, Integer>all(s, _function);
    Value<Integer, Boolean> _value = StreamAssert.<Integer, Boolean>value(Boolean.valueOf(false));
    Value<Integer, Boolean> _value_1 = StreamAssert.<Integer, Boolean>value(Boolean.valueOf(true));
    StreamAssert.<Integer, Boolean>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testNone() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() < 3));
      }
    };
    final SubStream<Integer, Boolean> avg = StreamExtensions.<Integer, Integer>none(s, _function);
    Value<Integer, Boolean> _value = StreamAssert.<Integer, Boolean>value(Boolean.valueOf(false));
    Value<Integer, Boolean> _value_1 = StreamAssert.<Integer, Boolean>value(Boolean.valueOf(true));
    StreamAssert.<Integer, Boolean>assertStreamContains(avg, _value, _value_1);
  }
  
  @Test
  public void testFirstMatch() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_9 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    IStream<Integer, Integer> _doubleLessThan_10 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_9, Integer.valueOf(1));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_10, Integer.valueOf(10));
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf((((it).intValue() % 2) == 0));
      }
    };
    final SubStream<Integer, Integer> first = StreamExtensions.<Integer, Integer>first(s, _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(8));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer, Integer>assertStreamContains(first, _value, _value_1, _value_2);
  }
  
  @Test
  public void testCount() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final SubStream<Integer, Integer> counted = StreamExtensions.<Integer, Integer>count(s);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(3));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    StreamAssert.<Integer, Integer>assertStreamContains(counted, _value, _value_1);
  }
  
  @Test
  public void testReduce() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    SubStream<Integer, Integer> _reduce = StreamExtensions.<Integer, Integer, Integer>reduce(s, Integer.valueOf(1), _function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        InputOutput.<Throwable>println(it);
      }
    };
    final SubStream<Integer, Integer> summed = StreamExtensions.<Integer, Integer>onError(_reduce, _function_1);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(7));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(10));
    StreamAssert.<Integer, Integer>assertStreamContains(summed, _value, _value_1);
  }
  
  @Test
  public void testScan() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final Function2<Integer, Integer, Integer> _function = new Function2<Integer, Integer, Integer>() {
      public Integer apply(final Integer a, final Integer b) {
        return Integer.valueOf(((a).intValue() + (b).intValue()));
      }
    };
    final SubStream<Integer, Integer> summed = StreamExtensions.<Integer, Integer, Integer>scan(s, Integer.valueOf(1), _function);
    Value<Integer, Integer> _value = StreamAssert.<Integer, Integer>value(Integer.valueOf(2));
    Value<Integer, Integer> _value_1 = StreamAssert.<Integer, Integer>value(Integer.valueOf(4));
    Value<Integer, Integer> _value_2 = StreamAssert.<Integer, Integer>value(Integer.valueOf(7));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish();
    Value<Integer, Integer> _value_3 = StreamAssert.<Integer, Integer>value(Integer.valueOf(5));
    Value<Integer, Integer> _value_4 = StreamAssert.<Integer, Integer>value(Integer.valueOf(10));
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish();
    StreamAssert.<Integer, Integer>assertStreamContains(summed, _value, _value_1, _value_2, _finish_2, _value_3, _value_4, _finish_3);
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
    Stream<Stream<Integer>> _datastream = StreamExtensions.<Stream<Integer>>datastream(((Stream<Integer>[])Conversions.unwrapArray(_map, Stream.class)));
    SubStream<Stream<Integer>, Integer> _flatten = StreamExtensions.<Stream<Integer>, Integer, Integer, Stream<Integer>>flatten(_datastream);
    IntegerRange _upTo_3 = new IntegerRange(1, 30);
    final Function1<Integer, Value<Stream<Integer>, Integer>> _function_1 = new Function1<Integer, Value<Stream<Integer>, Integer>>() {
      public Value<Stream<Integer>, Integer> apply(final Integer it) {
        return StreamAssert.<Stream<Integer>, Integer>value(it);
      }
    };
    Iterable<Value<Stream<Integer>, Integer>> _map_1 = IterableExtensions.<Integer, Value<Stream<Integer>, Integer>>map(_upTo_3, _function_1);
    StreamAssert.<Stream<Integer>, Integer>assertStreamContains(_flatten, ((Entry<Stream<Integer>, Integer>[])Conversions.unwrapArray(_map_1, Entry.class)));
  }
  
  @Test
  public void testFlatMap() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    IntegerRange _upTo_1 = new IntegerRange(11, 20);
    IntegerRange _upTo_2 = new IntegerRange(21, 30);
    Stream<IntegerRange> _stream = StreamExtensions.<IntegerRange>stream(Collections.<IntegerRange>unmodifiableList(CollectionLiterals.<IntegerRange>newArrayList(_upTo, _upTo_1, _upTo_2)));
    final Function1<IntegerRange, Stream<IntegerRange>> _function = new Function1<IntegerRange, Stream<IntegerRange>>() {
      public Stream<IntegerRange> apply(final IntegerRange it) {
        return StreamExtensions.<IntegerRange>datastream(it, it);
      }
    };
    SubStream<IntegerRange, IntegerRange> _flatMap = StreamExtensions.<IntegerRange, IntegerRange, IntegerRange>flatMap(_stream, _function);
    final Procedure1<IntegerRange> _function_1 = new Procedure1<IntegerRange>() {
      public void apply(final IntegerRange it) {
        InputOutput.<IntegerRange>println(it);
      }
    };
    StreamExtensions.<IntegerRange, IntegerRange>onEach(_flatMap, _function_1);
  }
  
  @Test
  public void testLimit() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    IStream<Long, Long> _doubleLessThan = StreamExtensions.<Long, Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    IStream<Long, Long> _doubleLessThan_1 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_2 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long, Long> _finish = StreamExtensions.<Long, Long>finish();
    IStream<Long, Long> _doubleLessThan_3 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Long, Long> _doubleLessThan_4 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    IStream<Long, Long> _doubleLessThan_5 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long, Long> _finish_1 = StreamExtensions.<Long, Long>finish();
    final IStream<Long, Long> s = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    final SubStream<Long, Long> limited = StreamExtensions.<Long, Long>limit(s, 1);
    Value<Long, Long> _value = StreamAssert.<Long, Long>value(Long.valueOf(1L));
    Finish<Long, Long> _finish_2 = StreamExtensions.<Long, Long>finish();
    Value<Long, Long> _value_1 = StreamAssert.<Long, Long>value(Long.valueOf(4L));
    Finish<Long, Long> _finish_3 = StreamExtensions.<Long, Long>finish();
    StreamAssert.<Long, Long>assertStreamContains(limited, _value, _finish_2, _value_1, _finish_3);
  }
  
  @Test
  public void testLimitBeforeCollect() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    IStream<Long, Long> _doubleLessThan = StreamExtensions.<Long, Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    IStream<Long, Long> _doubleLessThan_1 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_2 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    Finish<Long, Long> _finish = StreamExtensions.<Long, Long>finish();
    IStream<Long, Long> _doubleLessThan_3 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_2, _finish);
    IStream<Long, Long> _doubleLessThan_4 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_3, Long.valueOf(4L));
    IStream<Long, Long> _doubleLessThan_5 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(5L));
    Finish<Long, Long> _finish_1 = StreamExtensions.<Long, Long>finish();
    final IStream<Long, Long> s = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
    SubStream<Long, Long> _limit = StreamExtensions.<Long, Long>limit(s, 1);
    final SubStream<Long, List<Long>> limited = StreamExtensions.<Long, Long>collect(_limit);
    Value<Long, List<Long>> _value = StreamAssert.<Long, List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(1L))));
    Value<Long, List<Long>> _value_1 = StreamAssert.<Long, List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(4L))));
    StreamAssert.<Long, List<Long>>assertStreamContains(limited, _value, _value_1);
  }
  
  @Test
  public void testUntil() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    IStream<Long, Long> _doubleLessThan = StreamExtensions.<Long, Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    IStream<Long, Long> _doubleLessThan_1 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_2 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    IStream<Long, Long> _doubleLessThan_3 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_2, Long.valueOf(4L));
    Finish<Long, Long> _finish = StreamExtensions.<Long, Long>finish();
    IStream<Long, Long> _doubleLessThan_4 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_3, _finish);
    IStream<Long, Long> _doubleLessThan_5 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(4L));
    IStream<Long, Long> _doubleLessThan_6 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_5, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_7 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_6, Long.valueOf(5L));
    IStream<Long, Long> _doubleLessThan_8 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_7, Long.valueOf(6L));
    Finish<Long, Long> _finish_1 = StreamExtensions.<Long, Long>finish();
    final IStream<Long, Long> s = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    final SubStream<Long, Long> untilled = StreamExtensions.<Long, Long>until(s, _function);
    Value<Long, Long> _value = StreamAssert.<Long, Long>value(Long.valueOf(1L));
    Finish<Long, Long> _finish_2 = StreamExtensions.<Long, Long>finish();
    Value<Long, Long> _value_1 = StreamAssert.<Long, Long>value(Long.valueOf(4L));
    Finish<Long, Long> _finish_3 = StreamExtensions.<Long, Long>finish();
    StreamAssert.<Long, Long>assertStreamContains(untilled, _value, _finish_2, _value_1, _finish_3);
  }
  
  @Test
  public void testUntil2() {
    Stream<Long> _stream = StreamExtensions.<Long>stream(Long.class);
    IStream<Long, Long> _doubleLessThan = StreamExtensions.<Long, Long>operator_doubleLessThan(_stream, Long.valueOf(1L));
    IStream<Long, Long> _doubleLessThan_1 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_2 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_1, Long.valueOf(3L));
    IStream<Long, Long> _doubleLessThan_3 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_2, Long.valueOf(4L));
    Finish<Long, Long> _finish = StreamExtensions.<Long, Long>finish();
    IStream<Long, Long> _doubleLessThan_4 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_3, _finish);
    IStream<Long, Long> _doubleLessThan_5 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_4, Long.valueOf(4L));
    IStream<Long, Long> _doubleLessThan_6 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_5, Long.valueOf(2L));
    IStream<Long, Long> _doubleLessThan_7 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_6, Long.valueOf(5L));
    IStream<Long, Long> _doubleLessThan_8 = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_7, Long.valueOf(6L));
    Finish<Long, Long> _finish_1 = StreamExtensions.<Long, Long>finish();
    final IStream<Long, Long> s = StreamExtensions.<Long, Long>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    final Function1<Long, Boolean> _function = new Function1<Long, Boolean>() {
      public Boolean apply(final Long it) {
        return Boolean.valueOf(((it).longValue() == 2L));
      }
    };
    SubStream<Long, Long> _until = StreamExtensions.<Long, Long>until(s, _function);
    final SubStream<Long, List<Long>> untilled = StreamExtensions.<Long, Long>collect(_until);
    Value<Long, List<Long>> _value = StreamAssert.<Long, List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(1L))));
    Value<Long, List<Long>> _value_1 = StreamAssert.<Long, List<Long>>value(Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(4L))));
    StreamAssert.<Long, List<Long>>assertStreamContains(untilled, _value, _value_1);
  }
  
  @Test
  public void testAnyMatchNoFinish() {
    Stream<Boolean> _stream = StreamExtensions.<Boolean>stream(Boolean.class);
    IStream<Boolean, Boolean> _doubleLessThan = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    IStream<Boolean, Boolean> _doubleLessThan_1 = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    IStream<Boolean, Boolean> _doubleLessThan_2 = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(true));
    final IStream<Boolean, Boolean> s = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan_2, Boolean.valueOf(false));
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    SubStream<Boolean, Boolean> _any = StreamExtensions.<Boolean, Boolean>any(s, _function);
    final IPromise<Boolean, Boolean> matches = StreamExtensions.<Boolean, Boolean>first(_any);
    StreamAssert.<Boolean>assertPromiseEquals(matches, Boolean.valueOf(true));
  }
  
  @Test
  public void testAnyMatchWithFinish() {
    Stream<Boolean> _stream = StreamExtensions.<Boolean>stream(Boolean.class);
    IStream<Boolean, Boolean> _doubleLessThan = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_stream, Boolean.valueOf(false));
    IStream<Boolean, Boolean> _doubleLessThan_1 = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan, Boolean.valueOf(false));
    IStream<Boolean, Boolean> _doubleLessThan_2 = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan_1, Boolean.valueOf(false));
    Finish<Boolean, Boolean> _finish = StreamExtensions.<Boolean, Boolean>finish();
    final IStream<Boolean, Boolean> s = StreamExtensions.<Boolean, Boolean>operator_doubleLessThan(_doubleLessThan_2, _finish);
    final Function1<Boolean, Boolean> _function = new Function1<Boolean, Boolean>() {
      public Boolean apply(final Boolean it) {
        return it;
      }
    };
    SubStream<Boolean, Boolean> _any = StreamExtensions.<Boolean, Boolean>any(s, _function);
    final IPromise<Boolean, Boolean> matches = StreamExtensions.<Boolean, Boolean>first(_any);
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
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(_stream, _function);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_split);
    SubStream<Integer, Integer> _separate = StreamExtensions.<Integer, Integer>separate(_collect);
    SubStream<Integer, List<Integer>> _collect_1 = StreamExtensions.<Integer, Integer>collect(_separate);
    IPromise<Integer, List<Integer>> _first = StreamExtensions.<Integer, List<Integer>>first(_collect_1);
    IntegerRange _upTo_1 = new IntegerRange(1, 10);
    List<Integer> _list = IterableExtensions.<Integer>toList(_upTo_1);
    StreamAssert.<Integer>assertPromiseEquals(_first, _list);
  }
  
  @Test
  public void testErrorsDontStopStream() {
    final Stream<String> errors = StreamExtensions.<String>stream(String.class);
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((((1 / ((it).intValue() - 5)) * 0) + (it).intValue()));
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_stream, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf((((1 / ((it).intValue() - 7)) * 0) + (it).intValue()));
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        String _message = it.getMessage();
        StreamExtensions.<String, String>operator_doubleGreaterThan(_message, errors);
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_map_1, _function_2);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_onError);
    IPromise<Integer, List<Integer>> _first = StreamExtensions.<Integer, List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10))));
    Collection<Entry<String, String>> _queue = errors.getQueue();
    int _size = _queue.size();
    Assert.assertEquals(2, _size);
  }
  
  @Atomic
  private final AtomicInteger _valueCount = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _errorCount = new AtomicInteger();
  
  @Test
  public void testErrorsDontStopStream2() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        try {
          if ((((it).intValue() == 3) || ((it).intValue() == 5))) {
            throw new Exception("should not break the stream");
          } else {
            TestStreamExtensions.this.incValueCount();
          }
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(s, _function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamExtensions.this.incErrorCount();
      }
    };
    _onEach.onError(_function_1);
    IntegerRange _upTo = new IntegerRange(1, 10);
    for (final Integer i : _upTo) {
      StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, i);
    }
    Integer _valueCount = this.getValueCount();
    Assert.assertEquals((10 - 2), (_valueCount).intValue());
    Integer _errorCount = this.getErrorCount();
    Assert.assertEquals(1, (_errorCount).intValue());
  }
  
  @Test
  public void testErrorsDontStopStream3() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        try {
          Integer _xblockexpression = null;
          {
            if ((((it).intValue() == 3) || ((it).intValue() == 5))) {
              throw new Exception("should not break the stream");
            }
            _xblockexpression = it;
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_stream, _function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        TestStreamExtensions.this.incErrorCount();
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_map, _function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        TestStreamExtensions.this.incValueCount();
      }
    };
    StreamExtensions.<Integer, Integer>onEach(_onError, _function_2);
    Integer _valueCount = this.getValueCount();
    Assert.assertEquals((10 - 2), (_valueCount).intValue());
    Integer _errorCount = this.getErrorCount();
    Assert.assertEquals(2, (_errorCount).intValue());
  }
  
  @Atomic
  private final AtomicInteger _overflowCount = new AtomicInteger();
  
  @Test
  public void testBufferOverflow() {
    final Stream<Integer> stream = StreamExtensions.<Integer>stream(int.class);
    final Procedure1<Entry<?, ?>> _function = new Procedure1<Entry<?, ?>>() {
      public void apply(final Entry<?, ?> it) {
        TestStreamExtensions.this.incOverflowCount();
      }
    };
    StreamExtensions.<Integer, Integer>buffer(stream, 3, _function);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(stream, Integer.valueOf(4));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(5));
    Integer _overflowCount = this.getOverflowCount();
    Assert.assertEquals(2, (_overflowCount).intValue());
  }
  
  @Test
  public void testResolve() {
    final Promise<Integer> t1 = PromiseExtensions.<Integer>promise(int.class);
    final Promise<Integer> t2 = PromiseExtensions.<Integer>promise(int.class);
    Stream<Promise<Integer>> _stream = StreamExtensions.<Promise<Integer>>stream(Collections.<Promise<Integer>>unmodifiableList(CollectionLiterals.<Promise<Integer>>newArrayList(t1, t2)));
    final SubStream<Promise<Integer>, Integer> s = StreamExtensions.<Promise<Integer>, Integer, Integer>resolve(_stream);
    final Procedure1<Entry<Promise<Integer>, Integer>> _function = new Procedure1<Entry<Promise<Integer>, Integer>>() {
      public void apply(final Entry<Promise<Integer>, Integer> it) {
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            InputOutput.<Integer>println(((Value<?, Integer>)it).value);
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
          return ExecutorExtensions.<String>promise(TestStreamExtensions.this.threads, _function);
        }
      };
      final Function1<String, Promise<String>> doSomethingAsync = _function;
      final Stream<String> s = StreamExtensions.<String>stream(String.class);
      IStream<String, String> _doubleLessThan = StreamExtensions.<String, String>operator_doubleLessThan(s, "a");
      IStream<String, String> _doubleLessThan_1 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan, "b");
      IStream<String, String> _doubleLessThan_2 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_1, "c");
      Finish<String, String> _finish = StreamExtensions.<String, String>finish();
      IStream<String, String> _doubleLessThan_3 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_2, _finish);
      IStream<String, String> _doubleLessThan_4 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_3, "d");
      IStream<String, String> _doubleLessThan_5 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_4, "e");
      Finish<String, String> _finish_1 = StreamExtensions.<String, String>finish();
      IStream<String, String> _doubleLessThan_6 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_5, _finish_1);
      IStream<String, String> _doubleLessThan_7 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_6, "f");
      Finish<String, String> _finish_2 = StreamExtensions.<String, String>finish();
      StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_7, _finish_2);
      Collection<Entry<String, String>> _queue = s.getQueue();
      InputOutput.<Collection<Entry<String, String>>>println(_queue);
      final Function1<String, String> _function_1 = new Function1<String, String>() {
        public String apply(final String it) {
          return it;
        }
      };
      SubStream<String, String> _map = StreamExtensions.<String, String, String>map(s, _function_1);
      SubStream<String, Promise<String>> _map_1 = StreamExtensions.<String, String, Promise<String>>map(_map, doSomethingAsync);
      SubStream<String, String> _resolve = StreamExtensions.<String, String>resolve(_map_1, 3);
      SubStream<String, List<String>> _collect = StreamExtensions.<String, String>collect(_resolve);
      final Procedure1<List<String>> _function_2 = new Procedure1<List<String>>() {
        public void apply(final List<String> it) {
          InputOutput.<String>println(("got: " + it));
        }
      };
      StreamExtensions.<String, List<String>>onEach(_collect, _function_2);
      IStream<String, String> _doubleLessThan_8 = StreamExtensions.<String, String>operator_doubleLessThan(s, "f");
      IStream<String, String> _doubleLessThan_9 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_8, "g");
      Finish<String, String> _finish_3 = StreamExtensions.<String, String>finish();
      IStream<String, String> _doubleLessThan_10 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_9, _finish_3);
      IStream<String, String> _doubleLessThan_11 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_10, "h");
      Finish<String, String> _finish_4 = StreamExtensions.<String, String>finish();
      StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_11, _finish_4);
      IStream<String, String> _doubleLessThan_12 = StreamExtensions.<String, String>operator_doubleLessThan(s, "d");
      IStream<String, String> _doubleLessThan_13 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_12, "e");
      Finish<String, String> _finish_5 = StreamExtensions.<String, String>finish();
      StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_13, _finish_5);
      IStream<String, String> _doubleLessThan_14 = StreamExtensions.<String, String>operator_doubleLessThan(s, "a");
      IStream<String, String> _doubleLessThan_15 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_14, "b");
      IStream<String, String> _doubleLessThan_16 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_15, "c");
      Finish<String, String> _finish_6 = StreamExtensions.<String, String>finish();
      StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_16, _finish_6);
      Thread.sleep(100);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testFirst() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(3));
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(4));
    IPromise<Integer, Integer> _first = StreamExtensions.<Integer, Integer>first(s);
    StreamAssert.<Integer>assertPromiseEquals(_first, Integer.valueOf(2));
  }
  
  @Test
  public void testLast() {
    IntegerRange _upTo = new IntegerRange(1, 1000000);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    SubPromise<Integer, Integer> _last = StreamExtensions.<Integer, Integer>last(s);
    StreamAssert.<Integer>assertPromiseEquals(_last, Integer.valueOf(1000000));
  }
  
  @Test
  public void testSkipAndTake() {
    IntegerRange _upTo = new IntegerRange(1, 1000000000);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    SubStream<Integer, Integer> _skip = StreamExtensions.<Integer, Integer>skip(s, 3);
    SubStream<Integer, Integer> _take = StreamExtensions.<Integer, Integer>take(_skip, 3);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_take);
    IPromise<Integer, List<Integer>> _first = StreamExtensions.<Integer, List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6))));
  }
  
  @Test
  public void testFirstAfterCollect() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(Integer.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(4));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish();
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish_1);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(s);
    IPromise<Integer, List<Integer>> _first = StreamExtensions.<Integer, List<Integer>>first(_collect);
    StreamAssert.<Integer>assertPromiseEquals(_first, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
  }
  
  @Test
  public void testPipe() {
    IntegerRange _upTo = new IntegerRange(1, 3);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    StreamCopySplitter<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(s);
    final IStream<Integer, Integer> s2 = _split.stream();
    final Procedure1<StreamHandlerBuilder<Integer, Integer>> _function = new Procedure1<StreamHandlerBuilder<Integer, Integer>>() {
      public void apply(final StreamHandlerBuilder<Integer, Integer> it) {
        final Procedure2<Integer, Integer> _function = new Procedure2<Integer, Integer>() {
          public void apply(final Integer $0, final Integer $1) {
            InputOutput.<String>println(("x" + $1));
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Integer, Integer>on(s2, _function);
    s2.next();
  }
  
  @Test
  public void testStreamForwardTo() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final IStream<Integer, Integer> s1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    StreamCopySplitter<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(s1);
    final IStream<Integer, Integer> s2 = _split.stream();
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(s2, _function);
    final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<String>println("done");
      }
    };
    _onEach.then(_function_1);
  }
  
  public void testStreamPromise() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Promise<Stream<Integer>> p = PromiseExtensions.<Stream<Integer>>promise(s);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    final Stream<Integer> s2 = PromiseExtensions.<Stream<Integer>, Promise<Stream<Integer>>, Integer>toStream(p);
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        String _message = it.getMessage();
        Assert.fail(_message);
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(s2, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(_onError, _function_1);
    StreamAssert.<Boolean>assertPromiseEquals(_onEach, Boolean.valueOf(true));
  }
  
  public void testStreamPromiseLater() {
    final Promise<Stream<Integer>> p = new Promise<Stream<Integer>>();
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    p.set(s);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    final Stream<Integer> s2 = PromiseExtensions.<Stream<Integer>, Promise<Stream<Integer>>, Integer>toStream(p);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(s2, _function);
    StreamAssert.<Boolean>assertPromiseEquals(_onEach, Boolean.valueOf(true));
  }
  
  @Test
  public void testThrottle() {
    IntegerRange _upTo = new IntegerRange(1, 1000);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    SubStream<Integer, Integer> _throttle = StreamExtensions.<Integer, Integer>throttle(_stream, 10);
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    StreamExtensions.<Integer, Integer>onEach(_throttle, _function);
  }
  
  public void testRateLimit() {
    try {
      IntegerRange _upTo = new IntegerRange(1, 1000);
      final Stream<Integer> stream = StreamExtensions.<Integer>stream(_upTo);
      final Procedure2<Long, Procedure0> _function = new Procedure2<Long, Procedure0>() {
        public void apply(final Long period, final Procedure0 doneFn) {
          Timer _timer = new Timer();
          final TimerTask _function = new TimerTask() {
            @Override
            public void run() {
              doneFn.apply();
            }
          };
          _timer.schedule(_function, period);
        }
      };
      final Procedure2<Long, Procedure0> delayFn = _function;
      final IStream<Integer, Integer> limited = StreamExtensions.<Integer, Integer>ratelimit(stream, 500, delayFn);
      final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
        public void apply(final Integer it) {
          InputOutput.<Integer>println(it);
        }
      };
      StreamExtensions.<Integer, Integer>onEach(limited, _function_1);
      Thread.sleep(5000);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void testLatest() {
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
  
  private Integer setValueCount(final Integer value) {
    return this._valueCount.getAndSet(value);
  }
  
  private Integer getValueCount() {
    return this._valueCount.get();
  }
  
  private Integer incValueCount() {
    return this._valueCount.incrementAndGet();
  }
  
  private Integer decValueCount() {
    return this._valueCount.decrementAndGet();
  }
  
  private Integer incValueCount(final Integer value) {
    return this._valueCount.addAndGet(value);
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
  
  private Integer setOverflowCount(final Integer value) {
    return this._overflowCount.getAndSet(value);
  }
  
  private Integer getOverflowCount() {
    return this._overflowCount.get();
  }
  
  private Integer incOverflowCount() {
    return this._overflowCount.incrementAndGet();
  }
  
  private Integer decOverflowCount() {
    return this._overflowCount.decrementAndGet();
  }
  
  private Integer incOverflowCount(final Integer value) {
    return this._overflowCount.addAndGet(value);
  }
}
