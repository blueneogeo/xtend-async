package nl.kii.stream;

import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.promise.internal.SubTask;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamStats;
import nl.kii.stream.internal.StreamEventHandler;
import nl.kii.stream.internal.StreamException;
import nl.kii.stream.internal.StreamObserver;
import nl.kii.stream.internal.SubStream;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Finish;
import nl.kii.stream.source.LoadBalancer;
import nl.kii.stream.source.StreamCopySplitter;
import nl.kii.stream.source.StreamSource;

public class StreamExtensions {
  /**
   * Create a stream of the given type
   */
  public static <T extends java.lang.Object> Stream<T> stream(final /* Class<T> */Object type) {
    return new Stream<T>();
  }
  
  /**
   * Create a stream of a set of data and finish it.
   * Note: the reason this method is called datastream instead of stream, is that
   * the type binds to anything, even void. That means that datastream() becomes a valid expression
   * which is errorprone.
   */
  public static <T extends java.lang.Object> Object datastream(final T... data) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method iterator is undefined for the type StreamExtensions"
      + "\nstream cannot be resolved");
  }
  
  /**
   * stream the data of a map as a list of key->value pairs
   */
  public static <K extends java.lang.Object, V extends java.lang.Object> Object stream(final /* Map<K, V> */Object data) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field key is undefined for the type StreamExtensions"
      + "\nThe method or field value is undefined for the type StreamExtensions"
      + "\nentrySet cannot be resolved"
      + "\nmap cannot be resolved"
      + "\n-> cannot be resolved"
      + "\nstream cannot be resolved");
  }
  
  /**
   * Create a stream of values out of a Promise of a list. If the promise throws an error,
   */
  public static <R extends java.lang.Object, T extends java.lang.Object, T2/*  extends Iterable<T> */> Stream<T> stream(final IPromise<R, T2> promise) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type StreamExtensions");
  }
  
  /**
   * stream an list, ending with a finish. makes an immutable copy internally.
   */
  public static <T extends java.lang.Object> Object streamList(final /* List<T> */Object list) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field ImmutableList is undefined for the type StreamExtensions"
      + "\ncopyOf cannot be resolved"
      + "\niterator cannot be resolved"
      + "\nstream cannot be resolved");
  }
  
  /**
   * stream an interable, ending with a finish
   */
  public static <T extends java.lang.Object> Object stream(final /* Iterable<T> */Object iterable) {
    throw new Error("Unresolved compilation problems:"
      + "\niterator cannot be resolved"
      + "\nstream cannot be resolved");
  }
  
  /**
   * stream an iterable, ending with a finish
   */
  public static <T extends java.lang.Object> Stream<T> stream(final /* Iterator<T> */Object iterator) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicBoolean cannot be resolved."
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\nAssignment to final field"
      + "\nget cannot be resolved"
      + "\nhasNext cannot be resolved"
      + "\nnext cannot be resolved"
      + "\n>> cannot be resolved"
      + "\nset cannot be resolved"
      + "\napply cannot be resolved"
      + "\nset cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * stream a standard Java inputstream. closing the stream closes the inputstream.
   */
  public static /* Stream<List<Byte>> */Object stream(final /* InputStream */Object stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type."
      + "\nIOException cannot be resolved to a type."
      + "\nByteProcessor cannot be resolved."
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\n! cannot be resolved."
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\nByte cannot be resolved to a type."
      + "\nreadBytes cannot be resolved"
      + "\nclose cannot be resolved"
      + "\nclose cannot be resolved");
  }
  
  /**
   * create an unending stream of random integers in the range you have given
   */
  public static Object streamRandom(final /* IntegerRange */Object range) {
    throw new Error("Unresolved compilation problems:"
      + "\nRandom cannot be resolved."
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\nstream cannot be resolved"
      + "\nwhen cannot be resolved"
      + "\nopen cannot be resolved"
      + "\nstart cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nnextInt cannot be resolved"
      + "\nsize cannot be resolved"
      + "\npush cannot be resolved"
      + "\nclose cannot be resolved"
      + "\nclose cannot be resolved");
  }
  
  /**
   * Create from the stream a new stream of just the output type
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<O, O> newStream(final IStream<I, O> stream) {
    return StreamExtensions.<I, O, O>newStreamOf(stream, );
  }
  
  /**
   * Transform the stream input based on the existing input and output type.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, T extends java.lang.Object> Stream<T> newStreamOf(final IStream<I, O> stream, final /*  */Object mapFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Observe the entries coming off this stream using a StreamObserver.
   * Note that you can only have ONE stream observer for every stream!
   * If you want more than one observer, you can split the stream, or
   * use StreamExtensions.monitor, which does this for you.
   * <p>
   * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
   * instead, for a more concise and elegant builder syntax.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void observe(final IStream<I, O> stream, final StreamObserver<I, O> observer) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field from is undefined for the type StreamExtensions"
      + "\nThe method or field value is undefined for the type StreamExtensions"
      + "\nThe method or field from is undefined for the type StreamExtensions"
      + "\nThe method or field level is undefined for the type StreamExtensions"
      + "\nThe method or field from is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method error(String) is not applicable without arguments");
  }
  
  /**
   * Listen to commands given to the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void handle(final IStream<I, O> stream, final StreamEventHandler controller) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field entry is undefined for the type StreamExtensions");
  }
  
  /**
   * Create a publisher for the stream. This allows you to observe the stream
   * with multiple listeners. Publishers do not support flow control, and the
   * created Publisher will eagerly pull all data from the stream for publishing.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Publisher<O> publish(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nAssignment to final field");
  }
  
  /**
   * Create new streams from an observable. Notice that these streams are
   * being pushed only, you lose flow control. Closing the stream will also
   * unsubscribe from the observable.
   */
  public static <T extends java.lang.Object> Stream<T> stream(final Observable<T> observable) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Add a value to a stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleGreaterThan(final I input, final IStream<I, O> stream) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.push(input);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final I input) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.push(input);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleGreaterThan(final /* List<I> */Object input, final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nforEach cannot be resolved");
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final /* List<I> */Object value) {
    throw new Error("Unresolved compilation problems:"
      + "\nforEach cannot be resolved");
  }
  
  /**
   * Add an entry to a stream (such as error or finish)
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final Entry<I, O> entry) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      _input.apply(entry);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the << operator
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final /* Throwable */Object t) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      nl.kii.stream.message.Error<I, O> _error = new nl.kii.stream.message.Error<I, O>(null, t);
      _input.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the >> operator
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> operator_doubleGreaterThan(final /* Throwable */Object t, final IStream<I, O> stream) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      nl.kii.stream.message.Error<I, O> _error = new nl.kii.stream.message.Error<I, O>(null, t);
      _input.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object operator_doubleGreaterThan(final IStream<I, O> source, final IStream<I, O> dest) {
    return StreamExtensions.<I, O>pipe(source, dest);
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object operator_doubleLessThan(final IStream<I, O> dest, final IStream<I, O> source) {
    return StreamExtensions.<I, O>pipe(source, dest);
  }
  
  /**
   * split a source into a new destination stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> StreamSource<I, O> operator_doubleGreaterThan(final StreamSource<I, O> source, final IStream<I, O> dest) {
    return source.pipe(dest);
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Finish<I, O> finish() {
    return new Finish<I, O>(null, 0);
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Finish<I, O> finish(final int level) {
    return new Finish<I, O>(null, level);
  }
  
  /**
   * Forwards commands given to the newStream directly to the parent.
   */
  public static <I1 extends java.lang.Object, I2 extends java.lang.Object, O1 extends java.lang.Object, O2 extends java.lang.Object> IStream<I1, O2> controls(final IStream<I1, O2> newStream, final IStream<I2, O1> parent) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way");
  }
  
  /**
   * Tell the stream something went wrong
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void error(final IStream<I, O> stream, final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved.");
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void error(final IStream<I, O> stream, final /* String */Object message, final /* Object */Object value) {
    StreamException _streamException = new StreamException(message, value, null);
    stream.error(_streamException);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void error(final IStream<I, O> stream, final /* String */Object message, final /* Throwable */Object cause) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved.");
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> void error(final IStream<I, O> stream, final /* String */Object message, final /* Object */Object value, final /* Throwable */Object cause) {
    StreamException _streamException = new StreamException(message, value, cause);
    stream.error(_streamException);
  }
  
  /**
   * Set the concurrency of the stream, letting you keep chaining by returning the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> concurrency(final IStream<I, O> stream, final int value) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.setConcurrency(int.valueOf(value));
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> java.lang.Object map(final IStream<I, O> stream, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> SubStream<I, R> map(final IStream<I, O> stream, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved");
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object filter(final IStream<I, O> stream, final /*  */Object filterFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object filter(final IStream<I, O> stream, final /*  */Object filterFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> filter(final IStream<I, O> stream, final /*  */Object filterFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicLong cannot be resolved."
      + "\nAtomicLong cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nincrementAndGet cannot be resolved"
      + "\napply cannot be resolved"
      + "\nget cannot be resolved"
      + "\nincrementAndGet cannot be resolved"
      + "\nset cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /**
   * Splits a stream into multiple parts. These parts are separated by Finish entries.
   * <p>
   * Streams support multiple levels of finishes, to indicate multiple levels of splits.
   * This allows you to split a stream, and then split it again. Rules for splitting a stream:
   * <ul>
   * <li>when a new split is applied, it is always at finish level 0
   * <li>all other stream operations that use finish, always use this level
   * <li>the existing splits are all upgraded a level. so a level 0 finish becomes a level 1 finish
   * <li>splits at a higher level always are carried through to a lower level. so wherever there is a
   *     level 1 split for example, there is also a level 0 split
   * </ul>
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> split(final IStream<I, O> stream, final /*  */Object splitConditionFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicBoolean cannot be resolved."
      + "\nAtomicBoolean cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\nget cannot be resolved"
      + "\napply cannot be resolved"
      + "\nset cannot be resolved"
      + "\nset cannot be resolved"
      + "\n== cannot be resolved"
      + "\nset cannot be resolved"
      + "\nget cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /**
   * Merges one level of finishes.
   * @see StreamExtensions.split
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> merge(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\n> cannot be resolved"
      + "\n- cannot be resolved");
  }
  
  /**
   * Only let pass a certain amount of items through the stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object limit(final IStream<I, O> stream, final int amount) {
    throw new Error("Unresolved compilation problems:"
      + "\n> cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object until(final IStream<I, O> stream, final /*  */Object untilFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> until(final IStream<I, O> stream, final /*  */Object untilFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicLong cannot be resolved."
      + "\nAtomicLong cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nincrementAndGet cannot be resolved"
      + "\napply cannot be resolved"
      + "\nget cannot be resolved"
      + "\nincrementAndGet cannot be resolved"
      + "\nset cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * Passes a counter as second parameter to the untilFn, starting at 1.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> until(final IStream<I, O> stream, final /*  */Object untilFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicLong cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved"
      + "\nincrementAndGet cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /**
   * Flatten a stream of streams into a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object, S extends IStream<I2, O>> SubStream<I, O> flatten(final IStream<I, S> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\non cannot be resolved"
      + "\nnext cannot be resolved"
      + "\nnext cannot be resolved"
      + "\nnext cannot be resolved");
  }
  
  /**
   * Performs a flatmap operation on the stream using the passed mapping function.
   * <p>
   * Flatmapping allows you to transform the values of the stream to multiple streams,
   * which are then merged to a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> Object flatMap(final IStream<I, O> stream, final /*  */Object mapFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nflatten cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Keep count of how many items have been streamed so far, and for each
   * value from the original stream, push a pair of count->value.
   * Finish(0) resets the count.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> /* SubStream<I, Pair> */Object index(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nPair cannot be resolved to a type."
      + "\nAtomicInteger cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nInteger cannot be resolved to a type."
      + "\nincrementAndGet cannot be resolved"
      + "\n-> cannot be resolved"
      + "\n== cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /**
   * Create a splitter StreamSource from a stream that lets you listen to the stream
   * with multiple listeners.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> StreamCopySplitter<I, O> split(final IStream<I, O> stream) {
    return new StreamCopySplitter<I, O>(stream);
  }
  
  /**
   * Balance the stream into multiple listening streams.
   */
  public static <T extends java.lang.Object> LoadBalancer<T, T> balance(final Stream<T> stream) {
    return new LoadBalancer<T, T>(stream);
  }
  
  /**
   * Tell the stream what size its buffer should be, and what should happen in case
   * of a buffer overflow.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object buffer(final IStream<I, O> stream, final int maxSize, final /*  */Object onOverflow) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nThe method overflow is undefined for the type StreamExtensions"
      + "\nThe method overflow is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\nAssignment to final field"
      + "\napply cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds. All other values are dropped.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object throttle(final IStream<I, O> stream, final long periodMs) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicLong cannot be resolved."
      + "\n- cannot be resolved."
      + "\nThe method or field System is undefined for the type StreamExtensions"
      + "\n- cannot be resolved."
      + "\ncurrentTimeMillis cannot be resolved"
      + "\nget cannot be resolved"
      + "\n== cannot be resolved"
      + "\n|| cannot be resolved"
      + "\n- cannot be resolved"
      + "\nget cannot be resolved"
      + "\n> cannot be resolved"
      + "\nset cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Only allows one value per given period. Other values are dropped.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object throttle(final IStream<I, O> stream, final /* Period */Object period) {
    throw new Error("Unresolved compilation problems:"
      + "\nms cannot be resolved");
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds to pass through the stream.
   * All other values are buffered, and dropped only after the buffer has reached a given size.
   * This method requires a timer function, that takes a period in milliseconds, and that calls
   * the passed procedure when that period has expired.
   * <p>
   * Since ratelimiting does not throw away values when the source is pushing data faster
   * than can be processed, it can cause buffer overflow after some time.
   * <p>
   * You are strongly adviced to put a buffer statement before the ratelimit so you can
   * set the max buffer size and handle overflowing data, like this:
   * <p>
   * <pre>
   * someStream
   *    .buffer(1000) [ println('overflow error! handle this') ]
   *    .ratelimit(100) [ period, timeoutFn, | vertx.setTimer(period, timeoutFn) ]
   *    .onEach [ ... ]
   * </pre>
   * FIX: BREAKS ON ERRORS
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> ratelimit(final IStream<I, O> stream, final long periodMs, final /*  */Object timerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n<= cannot be resolved."
      + "\nAtomicLong cannot be resolved."
      + "\n- cannot be resolved."
      + "\nAtomicBoolean cannot be resolved."
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method or field System is undefined for the type StreamExtensions"
      + "\n- cannot be resolved."
      + "\nThe method or field System is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\ncurrentTimeMillis cannot be resolved"
      + "\nget cannot be resolved"
      + "\n== cannot be resolved"
      + "\n|| cannot be resolved"
      + "\n- cannot be resolved"
      + "\nget cannot be resolved"
      + "\n> cannot be resolved"
      + "\nset cannot be resolved"
      + "\nget cannot be resolved"
      + "\n! cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n- cannot be resolved"
      + "\nget cannot be resolved"
      + "\napply cannot be resolved"
      + "\nset cannot be resolved"
      + "\ncurrentTimeMillis cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
   * <p>
   * When an error occurs, delay the next call by a given period, and if the next stream value again generates
   * an error, multiply the period by 2 and wait that period. This way increasing the period
   * up to a maximum period of one hour per error. The moment a normal value gets processed, the period is reset
   * to the initial period.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> backoff(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final long periodMs, final /*  */Object timerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n* cannot be resolved."
      + "\n* cannot be resolved");
  }
  
  /**
   * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
   * <p>
   * When an error occurs, delay the next call by a given period, and if the next stream value again generates
   * an error, multiply the period by the given factor and wait that period. This way increasing the period
   * up to a maximum period that you pass. The moment a normal value gets processed, the period is reset to the
   * initial period.
   * 
   * TODO: needs testing!
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> backoff(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final long periodMs, final int factor, final long maxPeriodMs, final /*  */Object timerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n<= cannot be resolved."
      + "\n<= cannot be resolved."
      + "\nAtomicLong cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field Math is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\n|| cannot be resolved"
      + "\nset cannot be resolved"
      + "\napply cannot be resolved"
      + "\nget cannot be resolved"
      + "\nmin cannot be resolved"
      + "\nget cannot be resolved"
      + "\n* cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Splits a stream of values up into a stream of lists of those values,
   * where each new list is started at each period interval.
   * <p>
   * FIX: this can break the finishes, lists may come in after a finish.
   * FIX: somehow ratelimit and window in a single stream do not time well together
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> /* SubStream<I, List> */Object window(final IStream<I, O> stream, final long periodMs, final /*  */Object timerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type."
      + "\nAtomicReference cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field newLinkedList is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nList cannot be resolved to a type."
      + "\nget cannot be resolved"
      + "\n== cannot be resolved"
      + "\nset cannot be resolved"
      + "\napply cannot be resolved"
      + "\ngetAndSet cannot be resolved"
      + "\nget cannot be resolved"
      + "\nadd cannot be resolved");
  }
  
  /**
   * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
   * <p>
   * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> synchronizeWith(final IStream<I, O> stream, final /* IStream<? extends  */Object timerStream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way");
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * It only asks the next promise from the stream when the previous promise has been resolved.
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object> Object resolve(final IStream<I, ? extends IPromise<I2, O>> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved.");
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * <p>
   * Allows concurrent promises to be resolved in parallel.
   * Passing a concurrency of 0 means all incoming promises will be called concurrently.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> resolve(final /* IStream<I, ? extends IPromise<? extends  */Object stream, final int concurrency) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicBoolean cannot be resolved."
      + "\nAtomicInteger cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field Throwable is undefined for the type StreamExtensions"
      + "\n> cannot be resolved."
      + "\n== cannot be resolved."
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method next is undefined for the type StreamExtensions"
      + "\nThe method close is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nThe static method skip(IStream<I, O>, int) should be accessed in a static way"
      + "\non cannot be resolved"
      + "\ndecrementAndGet cannot be resolved"
      + "\n== cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ncompareAndSet cannot be resolved"
      + "\nthen cannot be resolved"
      + "\ndecrementAndGet cannot be resolved"
      + "\n== cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ncompareAndSet cannot be resolved"
      + "\nincrementAndGet cannot be resolved"
      + "\n|| cannot be resolved"
      + "\nget cannot be resolved"
      + "\n== cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(stream.concurrency)
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> Object call(final IStream<I, O> stream, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(stream.concurrency)
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> Object call(final IStream<I, O> stream, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> java.lang.Object call(final IStream<I, O> stream, final int concurrency, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> Object call(final IStream<I, O> stream, final int concurrency, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nresolve cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> onError(final IStream<I, O> stream, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type StreamExtensions");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> onError(final IStream<I, O> stream, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type StreamExtensions");
  }
  
  /**
   * Catch errors of the specified type coming from the stream, and call the handler with the error.
   * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work).
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> on(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final boolean swallow, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThrowable cannot be resolved to a type."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\n! cannot be resolved."
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\nmatches cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Catch errors of the specified type, call the handler, and pass on the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> on(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Catch errors of the specified type, call the handler, and pass on the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> on(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Catch errors of the specified type, call the handler, and swallow them from the stream chain.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> effect(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Catch errors of the specified type, call the handler, and swallow them from the stream chain.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> effect(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\napply cannot be resolved");
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object map(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> map(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThrowable cannot be resolved to a type."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\nmatches cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object call(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> call(final IStream<I, O> stream, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThrowable cannot be resolved to a type."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field Throwable is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\nmatches cannot be resolved"
      + "\napply cannot be resolved"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> onErrorThrow(final IStream<I, O> stream) {
    return StreamExtensions.<I, O>onErrorThrow(stream, "onErrorThrow");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> onErrorThrow(final IStream<I, O> stream, final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)");
  }
  
  /**
   * Lets you respond to the closing of the stream
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> onClosed(final IStream<I, O> stream, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved");
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubTask<I> onEach(final IStream<I, O> stream, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubTask<I> onEach(final IStream<I, O> stream, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,boolean)"
      + "\napply cannot be resolved"
      + "\n== cannot be resolved");
  }
  
  /**
   * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Performs the task for every value, and only requests the next value from the stream once the task has finished.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<I2, R>> Object onEachCall(final IStream<I, O> stream, final /*  */Object taskFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nresolve cannot be resolved"
      + "\nonEach cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Performs the task for every value, and only requests the next value from the stream once the task has finished.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<I2, R>> Object onEachCall(final IStream<I, O> stream, final /*  */Object taskFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nresolve cannot be resolved"
      + "\nonEach cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Shortcut for splitting a stream and then performing a pipe to another stream.
   * @return a CopySplitter source that you can connect more streams to.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object pipe(final IStream<I, O> stream, final /* IStream<I, ? extends  */Object target) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved.");
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Closes the stream once it has the value or an error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> first(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method fulfilled is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method fulfilled is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nType mismatch: cannot convert from Promise<I> to I"
      + "\nType mismatch: cannot convert from SubPromise<I, O> to IPromise<I, O>"
      + "\n! cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Will keep asking next on the stream until it gets to the last value!
   * Skips any stream errors, and closes the stream when it is done.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubPromise<I, O> last(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method fulfilled is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method fulfilled is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method fulfilled is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (null,Object)"
      + "\nType mismatch: cannot convert from Promise<I> to I"
      + "\n! cannot be resolved"
      + "\nset cannot be resolved"
      + "\n== cannot be resolved"
      + "\n! cannot be resolved"
      + "\n&& cannot be resolved"
      + "\nget cannot be resolved"
      + "\n!= cannot be resolved"
      + "\nget cannot be resolved"
      + "\n! cannot be resolved"
      + "\n&& cannot be resolved"
      + "\nget cannot be resolved"
      + "\n!= cannot be resolved"
      + "\nget cannot be resolved");
  }
  
  /**
   * Skip an amount of items from the stream, and process only the ones after that.
   * Resets at finish.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> skip(final IStream<I, O> stream, final int amount) {
    throw new Error("Unresolved compilation problems:"
      + "\n> cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Take only a set amount of items from the stream.
   * Resets at finish.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object take(final IStream<I, O> stream, final int amount) {
    throw new Error("Unresolved compilation problems:"
      + "\n> cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Start the stream and and promise the first value from it.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object then(final IStream<I, O> stream, final /* Procedure1<O> */Object listener) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved.");
  }
  
  /**
   * Convenient builder to easily asynchronously respond to stream entries.
   * Defaults for each[], finish[], and error[] is to simply ask for the next entry.
   * <p>
   * Note: you can only have a single entry handler for a stream.
   * <p>
   * Usage example:
   * <pre>
   * val stream = (1.10).stream
   * stream.on [
   *    each [ println('got ' + it) stream.next ] // do not forget to ask for the next entry
   *    error [ printStackTrace stream.next true ] // true to indicate you want to throw the error
   * ]
   * stream.next // do not forget to start the stream!
   * </pre>
   * @return a task that completes on finish(0) or closed, or that gives an error
   * if the stream passed an error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object on(final IStream<I, O> stream, final /*  */Object handlerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> on(final IStream<I, O> stream, final /*  */Object handlerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nstream cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Convenient builder to easily respond to commands given to a stream.
   * <p>
   * Note: you can only have a single monitor for a stream.
   * <p>
   * Example:
   * <pre>
   * stream.when [
   *     next [ println('next was called on the stream') ]
   *     close [ println('the stream was closed') ]
   * ]
   * </pre>
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> when(final IStream<I, O> stream, final /*  */Object handlerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Check on each value if the assert/check description is valid.
   * Throws an Exception with the check description if not.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object check(final IStream<I, O> stream, final /* String */Object checkDescription, final /*  */Object checkFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved."
      + "\napply cannot be resolved"
      + "\n! cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object effect(final IStream<I, O> stream, final /*  */Object listener) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object effect(final IStream<I, O> stream, final /*  */Object listener) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object perform(final IStream<I, O> stream, final /*  */Object promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    return StreamExtensions.<I, O>perform(stream, (_concurrency).IntegerValue(), promiseFn);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object perform(final IStream<I, O> stream, final /*  */Object promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    return StreamExtensions.<I, O>perform(stream, (_concurrency).IntegerValue(), promiseFn);
  }
  
  /**
   * Perform some asynchronous side-effect action based on the stream.
   * Perform at most 'concurrency' calls in parallel.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object perform(final IStream<I, O> stream, final int concurrency, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Perform some asynchronous side-effect action based on the stream.
   * Perform at most 'concurrency' calls in parallel.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object perform(final IStream<I, O> stream, final int concurrency, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved"
      + "\nmap cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Opposite of collect, separate each list in the stream into separate
   * stream entries and streams those separately.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> separate(final /* IStream<I, List<O>> */Object stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nmap cannot be resolved");
  }
  
  /**
   * Collect all items from a stream, separated by finishes
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> /* SubStream<I, List> */Object collect(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type."
      + "\nThe method or field newArrayList is undefined for the type StreamExtensions"
      + "\nconcat cannot be resolved");
  }
  
  /**
   * Concatenate a lot of strings into a single string, separated by a separator string.
   * <pre>
   * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object join(final IStream<I, O> stream, final /* String */Object separator) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\n+ cannot be resolved"
      + "\n!= cannot be resolved"
      + "\n+ cannot be resolved"
      + "\ntoString cannot be resolved");
  }
  
  /**
   * Add the value of all the items in the stream until a finish.
   */
  public static <I extends java.lang.Object, O/*  extends Number */> Object sum(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nThe method or field doubleValue is undefined for the type StreamExtensions"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Average the items in the stream until a finish.
   */
  public static <I extends java.lang.Object, O/*  extends Number */> Object average(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n-> cannot be resolved."
      + "\nThe method or field key is undefined for the type StreamExtensions"
      + "\nThe method or field value is undefined for the type StreamExtensions"
      + "\nThe method or field value is undefined for the type StreamExtensions"
      + "\nThe method or field key is undefined for the type StreamExtensions"
      + "\n-> cannot be resolved"
      + "\nvalue cannot be resolved"
      + "\n+ cannot be resolved"
      + "\ndoubleValue cannot be resolved"
      + "\n/ cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Count the number of items passed in the stream until a finish.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object count(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\n+ cannot be resolved");
  }
  
  /**
   * Gives the maximum value found on the stream.
   * Values must implement Comparable
   */
  public static <I extends java.lang.Object, O/*  extends Comparable<O> */> SubStream<I, O> max(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ncompareTo cannot be resolved"
      + "\n> cannot be resolved");
  }
  
  /**
   * Gives the minimum value found on the stream.
   * Values must implement Comparable
   */
  public static <I extends java.lang.Object, O/*  extends Comparable<O> */> SubStream<I, O> min(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved"
      + "\n&& cannot be resolved"
      + "\ncompareTo cannot be resolved"
      + "\n< cannot be resolved");
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * Errors in the stream are suppressed.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> SubStream<I, R> reduce(final IStream<I, O> stream, final R initial, final /*  */Object reducerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nset cannot be resolved"
      + "\napply cannot be resolved"
      + "\nget cannot be resolved"
      + "\n== cannot be resolved"
      + "\ngetAndSet cannot be resolved"
      + "\n!= cannot be resolved"
      + "\n- cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> java.lang.Object scan(final IStream<I, O> stream, final R initial, final /*  */Object reducerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> SubStream<I, R> scan(final IStream<I, O> stream, final R initial, final /*  */Object reducerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved"
      + "\nget cannot be resolved"
      + "\nset cannot be resolved"
      + "\n!= cannot be resolved"
      + "\nset cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Streams true if all stream values match the test function
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, boolean> all(final IStream<I, O> stream, final /*  */Object testFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n&& cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Streams true if no stream values match the test function
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, boolean> none(final IStream<I, O> stream, final /*  */Object testFn) {
    throw new Error("Unresolved compilation problems:"
      + "\n&& cannot be resolved"
      + "\napply cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  /**
   * Streams true if any of the values match the passed testFn.
   * <p>
   * Note that this is not a normal reduction, since no finish is needed
   * for any to fire true. The moment testFn gives off true, true is streamed
   * and the rest of the incoming values are skipped.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> /* SubStream<I, Boolean> */Object any(final IStream<I, O> stream, final /*  */Object testFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nBoolean cannot be resolved to a type."
      + "\nAtomicBoolean cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,boolean)"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,boolean)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved"
      + "\nset cannot be resolved"
      + "\n== cannot be resolved"
      + "\nget cannot be resolved"
      + "\nset cannot be resolved"
      + "\n! cannot be resolved"
      + "\n- cannot be resolved");
  }
  
  /**
   * Streams the first value that matches the testFn
   * <p>
   * Note that this is not a normal reduction, since no finish is needed to fire a value.
   * The moment testFn gives off true, the value is streamed and the rest of the incoming
   * values are skipped.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object first(final IStream<I, O> stream, final /*  */Object testFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Streams the first value that matches the testFn
   * <p>
   * Note that this is not a normal reduction, since no finish is needed to fire a value.
   * The moment testFn gives off true, the value is streamed and the rest of the incoming
   * values are skipped.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> first(final IStream<I, O> stream, final /*  */Object testFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved."
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved"
      + "\nset cannot be resolved"
      + "\n== cannot be resolved"
      + "\nset cannot be resolved"
      + "\n- cannot be resolved");
  }
  
  /**
   * Returns an atomic reference which updates as new values come in.
   * This way you can always get the latest value that came from the stream.
   * @return the latest value, self updating. may be null if no value has yet come in.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object latest(final IStream<I, O> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicReference cannot be resolved.");
  }
  
  /**
   * Keeps an atomic reference that you pass updated with the latest values
   * that comes from the stream.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> SubStream<I, O> latest(final IStream<I, O> stream, final /* AtomicReference<O> */Object latestValue) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method or field $0 is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nInvalid number of arguments. The method push(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method finish(int) is not applicable for the arguments (Object,Object)"
      + "\nset cannot be resolved");
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> monitor(final IStream<I, O> stream, final StreamMonitor monitor) {
    String _operation = stream.getOperation();
    return StreamExtensions.<I, O>monitor(stream, _operation, monitor);
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> monitor(final IStream<I, O> stream, final /* String */Object name, final StreamMonitor monitor) {
    IStream<I, O> _xblockexpression = null;
    {
      final StreamStats stats = new StreamStats();
      monitor.add(name, stats);
      _xblockexpression = StreamExtensions.<I, O>monitor(stream, stats);
    }
    return _xblockexpression;
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object> IStream<I, O> monitor(final IStream<I, O> stream, final StreamStats stats) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method each is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstValueTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstValueTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastValueTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastValue is undefined for the type StreamExtensions"
      + "\nThe method or field valueCount is undefined for the type StreamExtensions"
      + "\nThe method or field valueCount is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstErrorTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstErrorTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastErrorTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastError is undefined for the type StreamExtensions"
      + "\nThe method or field errorCount is undefined for the type StreamExtensions"
      + "\nThe method or field errorCount is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstFinishTS is undefined for the type StreamExtensions"
      + "\nThe method or field firstFinishTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field lastFinishTS is undefined for the type StreamExtensions"
      + "\nThe method or field finishCount is undefined for the type StreamExtensions"
      + "\nThe method or field finishCount is undefined for the type StreamExtensions"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field lastEntryTS is undefined for the type StreamExtensions"
      + "\nThe method or field closeTS is undefined for the type StreamExtensions"
      + "\n== cannot be resolved"
      + "\n== cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nstream cannot be resolved"
      + "\nnext cannot be resolved"
      + "\n== cannot be resolved"
      + "\n== cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nstream cannot be resolved"
      + "\nnext cannot be resolved"
      + "\n== cannot be resolved"
      + "\n== cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nstream cannot be resolved"
      + "\nnext cannot be resolved"
      + "\nstream cannot be resolved"
      + "\nnext cannot be resolved");
  }
  
  /**
   * Complete a task when the stream finishes or closes,
   * or give an error on the task when the stream gives an error.
   */
  public static void pipe(final /* IStream<? extends  */Object stream, final Task task) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method closed is undefined for the type StreamExtensions"
      + "\nThe method or field $1 is undefined for the type StreamExtensions"
      + "\nThe method each is undefined for the type StreamExtensions");
  }
  
  public static Task toTask(final /* IStream<? extends  */Object stream) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      StreamExtensions.pipe(stream, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  private static <T extends java.lang.Object> /* List<T> */Object concat(final /* Iterable<? extends T> */Object list, final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved."
      + "\nThe method or field ImmutableList is undefined for the type StreamExtensions"
      + "\n!= cannot be resolved."
      + "\nThe method or field ImmutableList is undefined for the type StreamExtensions"
      + "\nbuilder cannot be resolved"
      + "\nadd cannot be resolved"
      + "\nbuilder cannot be resolved"
      + "\naddAll cannot be resolved"
      + "\nadd cannot be resolved"
      + "\nbuild cannot be resolved");
  }
  
  private static long now() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field System is undefined for the type StreamExtensions"
      + "\ncurrentTimeMillis cannot be resolved");
  }
}
