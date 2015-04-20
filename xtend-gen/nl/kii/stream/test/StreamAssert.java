package nl.kii.stream.test;

import nl.kii.promise.IPromise;
import nl.kii.stream.IStream;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Value;

public class StreamAssert {
  /**
   * pull all queued data from a stream put it in a list, and print any error
   */
  public static <R extends java.lang.Object, T extends java.lang.Object> /* List<Entry<R, T>> */Object gather(final IStream<R, T> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nLinkedList cannot be resolved."
      + "\nThe method or field $0 is undefined for the type StreamAssert"
      + "\nThe method or field $1 is undefined for the type StreamAssert"
      + "\nThe method or field $0 is undefined for the type StreamAssert"
      + "\nThe method or field $1 is undefined for the type StreamAssert"
      + "\nThe method each is undefined for the type StreamAssert"
      + "\nThe method or field $0 is undefined for the type StreamAssert"
      + "\nThe method or field $1 is undefined for the type StreamAssert"
      + "\nInvalid number of arguments. The method error(IStream<I, O>, String) is not applicable for the arguments (Object)"
      + "\nadd cannot be resolved"
      + "\nadd cannot be resolved"
      + "\nadd cannot be resolved");
  }
  
  public static <R extends java.lang.Object, T extends java.lang.Object> Object assertStreamContains(final IStream<R, T> stream, final Entry<R, T>... entries) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method println is undefined for the type StreamAssert"
      + "\nThe method assertArrayEquals is undefined for the type StreamAssert");
  }
  
  public static Object assertFulfilled(final /* IPromise<? extends  */Object promise) {
    throw new Error("Unresolved compilation problems:"
      + "\nassertTrue cannot be resolved");
  }
  
  public static <T extends java.lang.Object> Object assertPromiseEquals(final /* IPromise<? extends  */Object promise, final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nset cannot be resolved"
      + "\nassertTrue cannot be resolved"
      + "\nget cannot be resolved"
      + "\nassertEquals cannot be resolved");
  }
  
  public static <T extends java.lang.Object> void assertPromiseEquals(final /* IPromise<? extends  */Object promise, final /* List<T> */Object value) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nList cannot be resolved to a type."
      + "\nset cannot be resolved"
      + "\nassertTrue cannot be resolved"
      + "\nget cannot be resolved"
      + "\nassertArrayEquals cannot be resolved");
  }
  
  public static <R extends java.lang.Object, T extends java.lang.Object> Value<R, T> value(final T value) {
    return new Value<R, T>(null, value);
  }
}
