package nl.kii.stream.source;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.IStream;
import nl.kii.stream.internal.SubStream;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.message.StreamMessage;
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
public abstract class StreamSplitter<I extends Object, O extends Object> extends Actor<StreamMessage> implements StreamSource<I, O> {
  /**
   * the source stream that gets distributed
   */
  protected final IStream<I, O> source;
  
  /**
   * the connected listening streams
   */
  @Atomic
  private final AtomicReference<List<IStream<I, ?>>> _streams = new AtomicReference<List<IStream<I, ?>>>();
  
  public StreamSplitter(final IStream<I, O> source) {
    this.source = source;
    CopyOnWriteArrayList<IStream<I, ?>> _copyOnWriteArrayList = new CopyOnWriteArrayList<IStream<I, ?>>();
    this.setStreams(_copyOnWriteArrayList);
    final Procedure1<Entry<I, O>> _function = new Procedure1<Entry<I, O>>() {
      @Override
      public void apply(final Entry<I, O> it) {
        StreamSplitter.this.apply(it);
      }
    };
    source.onChange(_function);
  }
  
  @Override
  public StreamSource<I, O> pipe(final IStream<I, ?> stream) {
    StreamSplitter<I, O> _xblockexpression = null;
    {
      List<IStream<I, ?>> _streams = this.getStreams();
      _streams.add(stream);
      final Procedure1<StreamEvent> _function = new Procedure1<StreamEvent>() {
        @Override
        public void apply(final StreamEvent it) {
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
  
  @Override
  public IStream<I, O> stream() {
    SubStream<I, O> _subStream = new SubStream<I, O>(this.source);
    final Procedure1<SubStream<I, O>> _function = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        StreamSplitter.this.pipe(it);
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_subStream, _function);
  }
  
  /**
   * we are wrapping in an actor to make things threadsafe
   */
  @Override
  protected void act(final StreamMessage message, final Procedure0 done) {
    boolean _matched = false;
    if (!_matched) {
      if (message instanceof Entry) {
        _matched=true;
        this.onEntry(((Entry<I, O>)message));
      }
    }
    if (!_matched) {
      if (message instanceof StreamEvent) {
        _matched=true;
        this.onCommand(((StreamEvent)message));
      }
    }
    done.apply();
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected abstract void onEntry(final Entry<I, O> entry);
  
  /**
   * Handle a message coming from a piped stream
   */
  protected abstract void onCommand(final StreamEvent msg);
  
  /**
   * Utility method that only returns true if all members match the condition
   */
  protected static <T extends Object> boolean all(final Iterable<T> list, final Function1<? super T, ? extends Boolean> conditionFn) {
    final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
      @Override
      public Boolean apply(final T it) {
        Boolean _apply = conditionFn.apply(it);
        return Boolean.valueOf((!(_apply).booleanValue()));
      }
    };
    T _findFirst = IterableExtensions.<T>findFirst(list, _function);
    return Objects.equal(_findFirst, null);
  }
  
  protected void setStreams(final List<IStream<I, ?>> value) {
    this._streams.set(value);
  }
  
  protected List<IStream<I, ?>> getStreams() {
    return this._streams.get();
  }
  
  protected List<IStream<I, ?>> getAndSetStreams(final List<IStream<I, ?>> value) {
    return this._streams.getAndSet(value);
  }
}
