package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Publisher;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * A SkippableStream extends Stream with the possibility to skip to the end of
 * a batch of incoming data. In Java8 this feature is called shortcutting a stream.
 * <p>
 * This is useful for optimising streams. Say you have a million entries coming into
 * a stream but only need the top 3. With a normal stream, your listeners would get
 * all of the values.
 * <p>
 * In a skippable stream, you not only get a value but also a done
 * function that you can call. By calling this function, the listener can indicate that
 * it no longer needs data. When all listeners have indicated they no longer have a need
 * for data, the listener will skip all data until a finish command is given/received.
 * <p>
 * After this, a new batch is started, and new values pushed to the stream will be sent
 * to the listeners again, until they call the done function again, etc.
 */
@SuppressWarnings("all")
public class SkippableStream<T extends Object> extends Stream<T> {
  /**
   * Amount of done listeners. Used to know if a stream is done. If there are
   * no open listeners anymore, a stream can stop streaming until the next finish.
   */
  private final AtomicBoolean skippingToFinish = new AtomicBoolean(false);
  
  /**
   * Times the stream was finished
   */
  private final AtomicInteger timesFinished = new AtomicInteger(0);
  
  /**
   * How many of the listeners are done for this batch
   */
  private final AtomicInteger doneListenerCount = new AtomicInteger(0);
  
  /**
   * Optional listener for when skipToFinish is called
   */
  private Procedure0 skipListener;
  
  public SkippableStream() {
    super(null);
  }
  
  public SkippableStream(final Stream<?> parentStream) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Stream<?> to Publisher<Entry<T>>");
  }
  
  public SkippableStream(final Publisher<Entry<T>> publisher, final Stream<?> parentStream) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of arguments. The constructor Stream(Publisher<Entry<T>>) is not applicable for the arguments (Publisher<Entry<T>>,Stream<?>)");
  }
  
  /**
   * Push an entry into the stream. An entry can be a Value, a Finish or an Error.
   */
  public void apply(final Entry<T> entry) {
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(entry, null)) {
        _matched=true;
        throw new NullPointerException("cannot stream a null entry");
      }
    }
    if (!_matched) {
      if (entry instanceof Value) {
        _matched=true;
        boolean _get = this.skippingToFinish.get();
        boolean _not = (!_get);
        if (_not) {
          this.stream.apply(entry);
        }
      }
    }
    if (!_matched) {
      if (entry instanceof Finish) {
        _matched=true;
        this.timesFinished.incrementAndGet();
        this.skippingToFinish.set(false);
        this.doneListenerCount.set(0);
        this.stream.apply(entry);
      }
    }
    if (!_matched) {
      if (entry instanceof nl.kii.stream.Error) {
        _matched=true;
        this.stream.apply(entry);
      }
    }
  }
  
  /**
   * Setting this to true will disregard all next incoming values, until we get a Finish entry/call,
   * which will resume normal settings. It will also communicate this to the parentStream, if available.
   * This allows the parent streams to stop streaming unnecessarily as well.
   * 
   * NOTE: there may be a problem here when pushing this up the chain when we process async commands.
   * The parent streams may be further in processing than the substream...
   */
  public Stream<T> skipToFinish() {
    SkippableStream<T> _xblockexpression = null;
    {
      this.skippingToFinish.set(true);
      boolean _notEquals = (!Objects.equal(this.skipListener, null));
      if (_notEquals) {
        this.skipListener.apply();
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Call the listener when skipping. Useful for calling parentStreams when
   * chaining streams together. That way, parent streams can also skip.
   */
  public Procedure0 onSkip(final Procedure0 listener) {
    return this.skipListener = listener;
  }
  
  /**
   * Listen to values coming from the stream. For each new value, the passed
   * listerer will be called with the value.
   */
  public SkippableStream<T> each(final Procedure2<? super T, ? super Procedure0> listener) {
    SkippableStream<T> _xblockexpression = null;
    {
      final AtomicInteger timesCalled = new AtomicInteger(0);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          int _get = timesCalled.get();
          int _get_1 = SkippableStream.this.timesFinished.get();
          int _plus = (_get_1 + 1);
          boolean _lessEqualsThan = (_get <= _plus);
          if (_lessEqualsThan) {
            timesCalled.incrementAndGet();
            final int amountDone = SkippableStream.this.doneListenerCount.incrementAndGet();
            int _subscriptionCount = SkippableStream.this.getSubscriptionCount();
            boolean _equals = (amountDone == _subscriptionCount);
            if (_equals) {
              SkippableStream.this.skipToFinish();
            }
          }
        }
      };
      final Procedure0 onDone = _function;
      final Procedure1<Entry<T>> _function_1 = new Procedure1<Entry<T>>() {
        public void apply(final Entry<T> it) {
          int _get = timesCalled.get();
          int _get_1 = SkippableStream.this.timesFinished.get();
          int _plus = (_get_1 + 1);
          boolean _lessEqualsThan = (_get <= _plus);
          if (_lessEqualsThan) {
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Value) {
                _matched=true;
                listener.apply(((Value<T>)it).value, onDone);
              }
            }
          }
        }
      };
      this.onChange(_function_1);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
}
