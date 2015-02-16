package nl.kii.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.IPromise;
import nl.kii.stream.IStream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.internal.StreamResponder;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;

@SuppressWarnings("all")
public class StreamAssert {
  /**
   * pull all queued data from a stream put it in a list, and print any error
   */
  public static <R extends Object, T extends Object> List<Entry<R, T>> gather(final IStream<R, T> stream) {
    LinkedList<Entry<R, T>> _xblockexpression = null;
    {
      final LinkedList<Entry<R, T>> data = new LinkedList<Entry<R, T>>();
      final Procedure1<StreamResponder<R, T>> _function = new Procedure1<StreamResponder<R, T>>() {
        public void apply(final StreamResponder<R, T> it) {
          final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              nl.kii.stream.message.Error<R, T> _error = new nl.kii.stream.message.Error<R, T>($0, $1);
              data.add(_error);
              stream.next();
            }
          };
          it.error(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              Finish<R, T> _finish = new Finish<R, T>($0, ($1).intValue());
              data.add(_finish);
              stream.next();
            }
          };
          it.finish(_function_1);
          final Procedure2<R, T> _function_2 = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Value<R, T> _value = new Value<R, T>($0, $1);
              data.add(_value);
              stream.next();
            }
          };
          it.each(_function_2);
        }
      };
      StreamExtensions.<R, T>on(stream, _function);
      stream.next();
      _xblockexpression = data;
    }
    return _xblockexpression;
  }
  
  public static <R extends Object, T extends Object> void assertStreamContains(final IStream<R, T> stream, final Entry<R, T>... entries) {
    final List<Entry<R, T>> data = StreamAssert.<R, T>gather(stream);
    InputOutput.<List<Entry<R, T>>>println(data);
    Assert.assertArrayEquals(entries, ((Object[])Conversions.unwrapArray(data, Object.class)));
  }
  
  public static void assertFulfilled(final IPromise<?, Boolean> promise) {
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    promise.then(_function);
    Boolean _fulfilled = promise.getFulfilled();
    Assert.assertTrue((_fulfilled).booleanValue());
  }
  
  public static <T extends Object> void assertPromiseEquals(final IPromise<?, T> promise, final T value) {
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
  
  public static <T extends Object> void assertPromiseEquals(final IPromise<?, List<T>> promise, final List<T> value) {
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
  
  public static <R extends Object, T extends Object> Value<R, T> value(final T value) {
    return new Value<R, T>(null, value);
  }
}
