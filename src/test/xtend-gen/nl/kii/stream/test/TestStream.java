package nl.kii.stream.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStream {
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Test
  public void testObservingAStream() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Stream<Integer, Object> to Stream<Integer, Integer>");
  }
  
  @Test
  public void testUnbufferedStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Stream<Integer, Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Boolean> _function = new Function1<Integer, Boolean>() {
      public Boolean apply(final Integer it) {
        return Boolean.valueOf(((it).intValue() != 2));
      }
    };
    Stream<Integer, Object> _filter = StreamExtensions.<Integer>filter(s, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    Stream<Integer> _map = StreamExtensions.<Integer, Integer>map(_filter, _function_1);
    final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(_map, _function_2);
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    int _get = counter.get();
    Assert.assertEquals(6, _get);
    StreamExtensions.<Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    int _get_1 = counter.get();
    Assert.assertEquals(8, _get_1);
  }
  
  @Test
  public void testBufferedStream() {
    final AtomicInteger counter = new AtomicInteger(0);
    Stream<Integer, Integer> _stream = new Stream<Integer, Integer>();
    Stream<Integer> _doubleLessThan = StreamExtensions.<Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    Stream<Integer> _doubleLessThan_1 = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    final Stream<Integer> s = StreamExtensions.<Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      public void apply(final Integer it) {
        counter.addAndGet((it).intValue());
      }
    };
    StreamExtensions.<Integer>onEach(s, _function);
    int _get = counter.get();
    Assert.assertEquals(6, _get);
  }
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Test
  public void testControlledStream() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from T to Integer"
      + "\nCannot make a static reference to the non-static type T"
      + "\nIncorrect number of arguments for type StreamHandlerBuilder<R, T>; it cannot be parameterized with arguments <Integer>");
  }
  
  @Atomic
  private final AtomicInteger _result = new AtomicInteger();
  
  @Test
  public void testControlledChainedBufferedStream() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from T to Integer"
      + "\nCannot make a static reference to the non-static type T"
      + "\nIncorrect number of arguments for type StreamHandlerBuilder<R, T>; it cannot be parameterized with arguments <Integer>");
  }
  
  @Atomic
  private final AtomicReference<Throwable> _error = new AtomicReference<Throwable>();
  
  @Test
  public void testStreamErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of type arguments. The constructor Stream<R, T> is not applicable for the type arguments <Integer>");
  }
  
  @Test
  public void testChainedBufferedSkippingStream() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from T to int"
      + "\nCannot make a static reference to the non-static type T"
      + "\nIncorrect number of arguments for type StreamHandlerBuilder<R, T>; it cannot be parameterized with arguments <Integer>");
  }
  
  @Atomic
  private final AtomicInteger _sum = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _overflow = new AtomicInteger();
  
  @Test
  public void testParallelHighThroughputStreaming() {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <? extends Object>");
  }
  
  @Atomic
  private final AtomicInteger _overflowCount = new AtomicInteger();
  
  @Test
  public void testStreamBufferOverflow() {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of type arguments. The constructor Stream<R, T> is not applicable for the type arguments <Integer>");
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
  
  private Integer setResult(final Integer value) {
    return this._result.getAndSet(value);
  }
  
  private Integer getResult() {
    return this._result.get();
  }
  
  private Integer incResult() {
    return this._result.incrementAndGet();
  }
  
  private Integer decResult() {
    return this._result.decrementAndGet();
  }
  
  private Integer incResult(final Integer value) {
    return this._result.addAndGet(value);
  }
  
  private Throwable setError(final Throwable value) {
    return this._error.getAndSet(value);
  }
  
  private Throwable getError() {
    return this._error.get();
  }
  
  private Integer setSum(final Integer value) {
    return this._sum.getAndSet(value);
  }
  
  private Integer getSum() {
    return this._sum.get();
  }
  
  private Integer incSum() {
    return this._sum.incrementAndGet();
  }
  
  private Integer decSum() {
    return this._sum.decrementAndGet();
  }
  
  private Integer incSum(final Integer value) {
    return this._sum.addAndGet(value);
  }
  
  private Integer setOverflow(final Integer value) {
    return this._overflow.getAndSet(value);
  }
  
  private Integer getOverflow() {
    return this._overflow.get();
  }
  
  private Integer incOverflow() {
    return this._overflow.incrementAndGet();
  }
  
  private Integer decOverflow() {
    return this._overflow.decrementAndGet();
  }
  
  private Integer incOverflow(final Integer value) {
    return this._overflow.addAndGet(value);
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
