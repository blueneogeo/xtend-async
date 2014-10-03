package nl.kii.stream;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.IPromise;
import nl.kii.stream.Entry;
import nl.kii.stream.Stream;
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
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamAssert"
      + "\nInvalid number of arguments. The constructor Error(R, Throwable) is not applicable for the arguments (T)"
      + "\nType mismatch: cannot convert from (Integer)=>void to int"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <T>");
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
  
  public static <T extends Object> Value<T, Object> value(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of arguments. The constructor Value(R, T) is not applicable for the arguments (T)");
  }
}
