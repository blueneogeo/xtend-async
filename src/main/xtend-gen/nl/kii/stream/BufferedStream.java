package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.Entry;
import nl.kii.stream.Publisher;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamException;
import nl.kii.stream.impl.ThreadSafePublisher;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * A Buffered Stream extends a stream with following features:
 * 
 * <li>a stream needs to be started before listeners get messages
 * <li>all registered listeners have to request a next value before an entry is streamed
 * <li>until the above requirements are matched, incoming entries are buffered in a queue
 * <p>
 * This is useful for at least three scenarios:
 * <ol>
 * <li>you want to create a stream and push in data before you start listening
 * <li>you need flow control
 * <li>shortcutting
 * </ol>
 * 
 * <h1>Push before listening</h1>
 * 
 * If you create a normal stream, it will directly push to its listeners whatever you
 * put in. So say that you already have a list of ids and want to print them:
 * <p>
 * <pre>
 * val stream = new Stream<Integer> << 1 << 2 << 3 // items stream directly!
 * stream.each [ println(it) ] // so nothing gets printed
 * </pre>
 * <p>
 * In a BufferedStream, this will not happen, because streams will buffer:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer> << 1 << 2 << 3 // items get queued
 * stream.each [ println(it) ] // each starts the stream, items get printed
 * </pre>
 * <p>
 * You can also choose to not start a stream with each:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer> << 1 << 2 << 3 // items get queued
 * stream.each(false) [ println(it) ] // nothing gets printed yet
 * stream.each(false) [ println(it + 'again') ] // adding another listener, still nothing gets printed
 * stream.start // starts the stream, everthing gets printed twice (two listeners added)
 * </pre>
 * 
 * <h1>Flow Control</h1>
 * 
 * Flow control is necessary especially for asynchronous programming. Say that you push
 * a million values into a stream, and you map these to asynchronous functions. These
 * mappings will be instantanious. This means that a normal stream would push all of its
 * values through at once, and you'd be making a million parallel calls at the same time.
 * <p>
 * Example where this goes wrong:
 * <p>
 * <pre>
 * val stream = new Stream<Integer>
 * stream
 *     .async [ loadArticleAsync(it) ] // loadArticleAsync returns a Promise
 *     .each [ println('got article with title ' + title ]
 * 1..1000000.each [ it >> stream ] // this overwhelms the system
 * </pre>
 * <p>
 * How you can control it:
 * <p>
 * BufferedStream has a version of .each() which takes an onNext closure as a parameter.
 * By calling this closure, you signal that the listener you pass to .each() is ready to
 * recieve the next value. BufferedStream keeps track of its listeners and only lets the
 * next value flow from its buffer when all listeners are ready. (Normal .each() calls
 * always are ready). An example:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer>
 * stream.each [ it, next |
 *     val promise = loadArticleAsync
 *     promise.then [
 *         println('got article with title ' + title)
 *         next.apply // done! ask for the next article
 *     ]
 * ]
 * 1..1000000.each [ it >> stream ] // now handled one by one
 * </pre>
 * <p>
 * BufferedStreamExt adds many other versions of this, and a version of async that
 * lets you specify the amount of parallelism you wish for. It encapsulates the call
 * to next for you, so you don't need to consider it anymore in your code.
 * <p>
 * For 3 parallel calls maximum:
 * <p>
 * <pre>
 * val stream = new BufferedStream<Integer>
 * stream
 *     .async(3) [ loadArticleAsync ]
 *     .each [ println('got article with title ' + title ]
 * 1..1000000.each [ it >> stream ] // now handled max 3 at the same time
 * </pre>
 * 
 * <h1>Shortcutting</h1>
 * 
 * Sometimes you know in advance you don't need to continue listening. For example,
 * if you just want the first 5 values from a stream, you can signal that you are
 * done from then on. This is called shortcutting. Since bufferedstreams can indicate
 * when they need a value, they can simply stop asking.
 * 
 * 
 * @see Stream<T>
 */
@SuppressWarnings("all")
public class BufferedStream<T extends Object> extends Stream<T> {
  /**
   * The buffer gets filled when there are entries entering the stream
   * even though there are no listeners yet. The buffer will only grow
   * upto the maxBufferSize. If more entries enter than the size, the
   * buffer will overflow and discard these later entries.
   */
  private Queue<Entry<T>> buffer;
  
  /**
   * Amount of open listeners. Used to know if a stream is done. If there are
   * no open listeners anymore, a stream can stop streaming until the next finish.
   */
  private final AtomicInteger readyListenerCount = new AtomicInteger(0);
  
  /**
   * Creates a new Stream.
   */
  public BufferedStream() {
    this(new ThreadSafePublisher<Entry<T>>());
  }
  
  /**
   * Most detailed constructor, where you can specify your own publisher.
   */
  public BufferedStream(final Publisher<Entry<T>> publisher) {
    super(publisher);
  }
  
  /**
   * Returns the buffer that gets built up when values are pushed into a stream without the
   * stream having started or listeners being ready.
   */
  public Queue<Entry<T>> getBuffer() {
    Queue<Entry<T>> _xblockexpression = null;
    {
      boolean _equals = Objects.equal(this.buffer, null);
      if (_equals) {
        ConcurrentLinkedQueue<Entry<T>> _concurrentLinkedQueue = new ConcurrentLinkedQueue<Entry<T>>();
        this.buffer = _concurrentLinkedQueue;
      }
      _xblockexpression = this.buffer;
    }
    return _xblockexpression;
  }
  
  /**
   * We are ready to process if the stream is started and ALL listeners have requested a next value
   */
  public boolean isReady() {
    boolean _and = false;
    boolean _get = this.isOpen.get();
    if (!_get) {
      _and = false;
    } else {
      int _get_1 = this.readyListenerCount.get();
      int _subscriptionCount = this.getSubscriptionCount();
      boolean _equals = (_get_1 == _subscriptionCount);
      _and = _equals;
    }
    return _and;
  }
  
  /**
   * Start streaming. If anything was buffered and the listeners are ready,
   * it will also start processing.
   */
  public Stream<T> open() {
    try {
      BufferedStream<T> _xblockexpression = null;
      {
        boolean _get = this.isOpen.get();
        if (_get) {
          throw new StreamException("cannot start an already started stream.");
        }
        super.open();
        boolean _isReady = this.isReady();
        if (_isReady) {
          this.processNext();
        }
        _xblockexpression = this;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * Push an entry into the stream buffer. It will also try to process the entry, but that
   * will only happen if the stream is started and all the listeners are ready.
   */
  public void apply(final Entry<T> value) {
    boolean _equals = Objects.equal(value, null);
    if (_equals) {
      throw new NullPointerException("cannot stream a null value");
    }
    Queue<Entry<T>> _buffer = this.getBuffer();
    _buffer.add(value);
    boolean _isReady = this.isReady();
    if (_isReady) {
      this.processNext();
    }
  }
  
  /**
   * Takes a value from the buffer/queue and pushes it to the listeners for processing.
   * @return true if a value was processed from the buffer.
   */
  public boolean processNext() {
    boolean _xifexpression = false;
    boolean _and = false;
    boolean _notEquals = (!Objects.equal(this.buffer, null));
    if (!_notEquals) {
      _and = false;
    } else {
      boolean _isEmpty = this.buffer.isEmpty();
      boolean _not = (!_isEmpty);
      _and = _not;
    }
    if (_and) {
      boolean _xblockexpression = false;
      {
        Entry<T> _poll = this.buffer.poll();
        this.stream.apply(_poll);
        _xblockexpression = true;
      }
      _xifexpression = _xblockexpression;
    } else {
      _xifexpression = false;
    }
    return _xifexpression;
  }
  
  public BufferedStream<T> each(final boolean startStream, final Procedure2<? super T, ? super Procedure0> listener) {
    BufferedStream<T> _xblockexpression = null;
    {
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          BufferedStream.this.readyListenerCount.incrementAndGet();
          boolean _isReady = BufferedStream.this.isReady();
          if (_isReady) {
            BufferedStream.this.processNext();
          }
        }
      };
      final Procedure0 nextFn = _function;
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          listener.apply(it, nextFn);
        }
      };
      this.each(_function_1);
      boolean _and = false;
      if (!startStream) {
        _and = false;
      } else {
        boolean _get = this.isOpen.get();
        boolean _not = (!_get);
        _and = _not;
      }
      if (_and) {
        this.open();
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    Class<? extends BufferedStream> _class = this.getClass();
    String _name = _class.getName();
    _builder.append(_name, "");
    _builder.append(" { ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t");
    _builder.append("open: ");
    _builder.append(this.isOpen, "\t\t\t");
    _builder.append(", buffer: ");
    {
      boolean _notEquals = (!Objects.equal(this.buffer, null));
      if (_notEquals) {
        _builder.append(" ");
        int _length = ((Object[])Conversions.unwrapArray(this.buffer, Object.class)).length;
        _builder.append(_length, "\t\t\t");
        _builder.append(" ");
      } else {
        _builder.append(" none ");
      }
    }
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
}
