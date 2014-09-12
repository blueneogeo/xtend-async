package nl.kii.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.IPromise;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Function1<Throwable, Boolean> _function = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(it);
                data.add(_error);
                stream.next();
                _xblockexpression = true;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              Finish<T> _finish = new Finish<T>((it).intValue());
              data.add(_finish);
              stream.next();
            }
          };
          it.finish(_function_1);
          final Procedure1<T> _function_2 = new Procedure1<T>() {
            public void apply(final T it) {
              Value<T> _value = StreamAssert.<T>value(it);
              data.add(_value);
              stream.next();
            }
          };
          it.each(_function_2);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.next();
      _xblockexpression = data;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> void assertStreamContains(final Stream<T> stream, final Entry<T>... entries) {
    final List<Entry<T>> data = StreamAssert.<T>gather(stream);
    InputOutput.<List<Entry<T>>>println(data);
    Assert.assertArrayEquals(entries, ((Object[])Conversions.unwrapArray(data, Object.class)));
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
