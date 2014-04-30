package nl.kii.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
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
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          Finish<T> _finish = new Finish<T>();
          data.add(_finish);
          stream.next();
        }
      };
      Stream<T> _onFinish = stream.onFinish(_function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          it.printStackTrace();
          stream.next();
        }
      };
      Stream<T> _onError = _onFinish.onError(_function_1);
      final Procedure1<T> _function_2 = new Procedure1<T>() {
        public void apply(final T it) {
          Value<T> _value = StreamAssert.<T>value(it);
          data.add(_value);
          stream.next();
        }
      };
      _onError.onValue(_function_2);
      stream.next();
      _xblockexpression = data;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> void assertStreamEquals(final Stream<T> stream, final List<? extends Entry<T>> entries) {
    final List<Entry<T>> data = StreamAssert.<T>gather(stream);
    InputOutput.<List<Entry<T>>>println(data);
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(entries, Object.class)), ((Object[])Conversions.unwrapArray(data, Object.class)));
  }
  
  public static void assertFulfilled(final Promise<Boolean> promise) {
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    promise.then(_function);
    boolean _isFulfilled = promise.isFulfilled();
    Assert.assertTrue(_isFulfilled);
  }
  
  public static <T extends Object> void assertPromiseEquals(final Promise<T> promise, final T value) {
    final AtomicReference<T> ref = new AtomicReference<T>();
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        ref.set(it);
      }
    };
    promise.then(_function);
    boolean _isFulfilled = promise.isFulfilled();
    Assert.assertTrue(_isFulfilled);
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
    boolean _isFulfilled = promise.isFulfilled();
    Assert.assertTrue(_isFulfilled);
    List<T> _get = ref.get();
    Assert.assertArrayEquals(((Object[])Conversions.unwrapArray(_get, Object.class)), ((Object[])Conversions.unwrapArray(value, Object.class)));
  }
  
  public static <T extends Object> Value<T> value(final T value) {
    return new Value<T>(value);
  }
}
