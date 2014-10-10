package nl.kii.stream.source;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Entry;
import nl.kii.stream.IStream;
import nl.kii.stream.StreamMessage;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.SubStream;
import nl.kii.stream.source.StreamSource;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
@SuppressWarnings("all")
public abstract class StreamSplitter<R extends Object, T extends Object> extends Actor<StreamMessage> implements StreamSource<R, T> {
  /**
   * the source stream that gets distributed
   */
  protected final IStream<R, T> source;
  
  /**
   * the connected listening streams
   */
  @Atomic
  private final AtomicReference<List<IStream<R, T>>> _streams = new AtomicReference<List<IStream<R, T>>>();
  
  public StreamSplitter(final IStream<R, T> source) {
    this.source = source;
    CopyOnWriteArrayList<IStream<R, T>> _copyOnWriteArrayList = new CopyOnWriteArrayList<IStream<R, T>>();
    this.setStreams(_copyOnWriteArrayList);
    final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
      public void apply(final Entry<R, T> it) {
        StreamSplitter.this.apply(it);
      }
    };
    source.onChange(_function);
  }
  
  public StreamSource<R, T> pipe(final IStream<R, T> stream) {
    StreamSplitter<R, T> _xblockexpression = null;
    {
      List<IStream<R, T>> _streams = this.getStreams();
      _streams.add(stream);
      final Procedure1<StreamNotification> _function = new Procedure1<StreamNotification>() {
        public void apply(final StreamNotification it) {
          StreamSplitter.this.apply(it);
        }
      };
      stream.onNotify(_function);
      boolean _isReady = stream.isReady();
      if (_isReady) {
        stream.next();
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public IStream<R, T> stream() {
    SubStream<R, T> _subStream = new SubStream<R, T>(this.source);
    final Procedure1<SubStream<R, T>> _function = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        StreamSplitter.this.pipe(it);
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_subStream, _function);
  }
  
  /**
   * we are wrapping in an actor to make things threadsafe
   */
  protected void act(final StreamMessage message, final Procedure0 done) {
    boolean _matched = false;
    if (!_matched) {
      if (message instanceof Entry) {
        _matched=true;
        this.onEntry(((Entry<R, T>)message));
      }
    }
    if (!_matched) {
      if (message instanceof StreamNotification) {
        _matched=true;
        this.onCommand(((StreamNotification)message));
      }
    }
    done.apply();
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected abstract void onEntry(final Entry<R, T> entry);
  
  /**
   * Handle a message coming from a piped stream
   */
  protected abstract void onCommand(final StreamNotification msg);
  
  /**
   * Utility method that only returns true if all members match the condition
   */
  protected static <T extends Object> boolean all(final Iterable<T> list, final Function1<? super T, ? extends Boolean> conditionFn) {
    final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
      public Boolean apply(final T it) {
        Boolean _apply = conditionFn.apply(it);
        return Boolean.valueOf((!(_apply).booleanValue()));
      }
    };
    T _findFirst = IterableExtensions.<T>findFirst(list, _function);
    return Objects.equal(_findFirst, null);
  }
  
  protected List<IStream<R, T>> setStreams(final List<IStream<R, T>> value) {
    return this._streams.getAndSet(value);
  }
  
  protected List<IStream<R, T>> getStreams() {
    return this._streams.get();
  }
}
