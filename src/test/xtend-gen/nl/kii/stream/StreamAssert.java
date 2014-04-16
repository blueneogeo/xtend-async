package nl.kii.stream;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.Entry;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;

@SuppressWarnings("all")
public class StreamAssert {
  public static <T extends Object> void assertStreamEquals(final List<? extends Entry<T>> entries, final Stream<T> stream) {
    Queue<Entry<T>> _queue = stream.getQueue();
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(_queue, Object.class)), ((Object[])Conversions.unwrapArray(entries, Object.class)));
  }
  
  public static void assertPromiseFinished(final Promise<Boolean> promise) {
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    promise.then(_function);
    boolean _isFinished = promise.isFinished();
    Assert.assertTrue(_isFinished);
  }
  
  public static <T extends Object> void assertPromiseEquals(final Promise<T> promise, final T value) {
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
  
  public static <T extends Object> void assertPromiseEquals(final Promise<List<T>> promise, final List<T> value) {
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
  
  public static <T extends Object> Value<T> value(final T value) {
    return new Value<T>(value);
  }
}
