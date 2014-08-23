package nl.kii.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.IPromise;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Subscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;

@SuppressWarnings("all")
public class StreamAssert {
  /**
   * pull all queued data from a stream put it in a list, and print any error
   */
  public static <T extends Object> List<Entry<T>> gather(final Stream<T> stream) {
    LinkedList<Entry<T>> _xblockexpression = null;
    {
      final LinkedList<Entry<T>> data = new LinkedList<Entry<T>>();
      final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
        public void apply(final Finish<T> it) {
          Finish<T> _finish = new Finish<T>(it.level);
          data.add(_finish);
        }
      };
      Subscription<T> _onFinish = StreamExtensions.<T>onFinish(stream, _function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          Value<T> _value = StreamAssert.<T>value(it);
          data.add(_value);
        }
      };
      StreamExtensions.<T>onEach(_onFinish, _function_1);
      _xblockexpression = data;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> void assertStreamEquals(final Stream<T> stream, final List<? extends Entry<T>> entries) {
    final List<Entry<T>> data = StreamAssert.<T>gather(stream);
    InputOutput.<List<Entry<T>>>println(data);
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(entries, Object.class)), ((Object[])Conversions.unwrapArray(data, Object.class)));
  }
  
  public static void assertFulfilled(final IPromise<Boolean> promise) {
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    promise.then(_function);
    Boolean _fulfilled = promise.getFulfilled();
    Assert.assertTrue((_fulfilled).booleanValue());
  }
  
  public static <T extends Object> void assertPromiseEquals(final IPromise<T> promise, final T value) {
    final AtomicReference<T> ref = new AtomicReference<T>();
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        ref.set(it);
      }
    };
    promise.then(_function);
    Boolean _fulfilled = promise.getFulfilled();
    Assert.assertTrue((_fulfilled).booleanValue());
    T _get = ref.get();
    Assert.assertEquals(_get, value);
  }
  
  public static <T extends Object> void assertPromiseEquals(final IPromise<List<T>> promise, final List<T> value) {
    final AtomicReference<List<T>> ref = new AtomicReference<List<T>>();
    final Procedure1<List<T>> _function = new Procedure1<List<T>>() {
      public void apply(final List<T> it) {
        ref.set(it);
      }
    };
    promise.then(_function);
    Boolean _fulfilled = promise.getFulfilled();
    Assert.assertTrue((_fulfilled).booleanValue());
    List<T> _get = ref.get();
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(_get, Object.class)), ((Object[])Conversions.unwrapArray(value, Object.class)));
  }
  
  public static <T extends Object> Value<T> value(final T value) {
    return new Value<T>(value);
  }
}
