package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Entry;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamCommand;
import nl.kii.stream.StreamSource;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
@SuppressWarnings("all")
public abstract class StreamSplitter<T extends Object> implements StreamSource<T> {
  /**
   * the source stream that gets distributed
   */
  protected final Stream<T> source;
  
  /**
   * the connected listening streams
   */
  @Atomic
  private final AtomicReference<List<Stream<T>>> _streams = new AtomicReference<List<Stream<T>>>();
  
  public StreamSplitter(final Stream<T> source) {
    this.source = source;
    CopyOnWriteArrayList<Stream<T>> _copyOnWriteArrayList = new CopyOnWriteArrayList<Stream<T>>();
    this.setStreams(_copyOnWriteArrayList);
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        StreamSplitter.this.onEntry(it);
      }
    };
    source.onChange(_function);
  }
  
  public Stream<T> stream() {
    Stream<T> _stream = new Stream<T>();
    final Procedure1<Stream<T>> _function = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        StreamSplitter.this.pipe(it);
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_stream, _function);
  }
  
  public StreamSource<T> pipe(final Stream<T> stream) {
    StreamSplitter<T> _xblockexpression = null;
    {
      List<Stream<T>> _streams = this.getStreams();
      _streams.add(stream);
      final Procedure1<StreamCommand> _function = new Procedure1<StreamCommand>() {
        public void apply(final StreamCommand it) {
          StreamSplitter.this.onCommand(it);
        }
      };
      stream.onNotification(_function);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected abstract void onEntry(final Entry<T> entry);
  
  /**
   * Handle a message coming from a piped stream
   */
  protected abstract void onCommand(final StreamCommand msg);
  
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
  
  protected List<Stream<T>> setStreams(final List<Stream<T>> value) {
    return this._streams.getAndSet(value);
  }
  
  protected List<Stream<T>> getStreams() {
    return this._streams.get();
  }
}
