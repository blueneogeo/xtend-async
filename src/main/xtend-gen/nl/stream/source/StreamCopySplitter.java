package nl.stream.source;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamNotification;
import nl.stream.source.StreamSplitter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * This splitter simply tries to pass all incoming values
 * from the source stream to the piped streams.
 * <p>
 * Flow control is maintained by only allowing the next
 * entry from the source stream if all piped streams are ready.
 * This means that you need to make sure that all connected
 * streams do not block their flow, since one blocking stream
 * will block all streams from flowing.
 */
@SuppressWarnings("all")
public class StreamCopySplitter<T extends Object> extends StreamSplitter<T> {
  @Atomic
  private final AtomicReference<Entry<T>> _buffer = new AtomicReference<Entry<T>>();
  
  public StreamCopySplitter(final Stream<T> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<T> entry) {
    this.setBuffer(entry);
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isReady();
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      this.publish();
    }
  }
  
  protected void onCommand(@Extension final StreamNotification msg) {
    boolean _matched = false;
    if (!_matched) {
      if (msg instanceof Next) {
        _matched=true;
        this.next();
      }
    }
    if (!_matched) {
      if (msg instanceof Skip) {
        _matched=true;
        this.skip();
      }
    }
    if (!_matched) {
      if (msg instanceof Close) {
        _matched=true;
        this.close();
      }
    }
  }
  
  protected Entry<T> publish() {
    Entry<T> _xifexpression = null;
    Entry<T> _buffer = this.getBuffer();
    boolean _notEquals = (!Objects.equal(_buffer, null));
    if (_notEquals) {
      Entry<T> _xblockexpression = null;
      {
        List<Stream<T>> _streams = this.getStreams();
        for (final Stream<T> it : _streams) {
          Entry<T> _buffer_1 = this.getBuffer();
          it.apply(_buffer_1);
        }
        _xblockexpression = this.setBuffer(null);
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  protected void next() {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isReady();
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.source.next();
    this.publish();
  }
  
  protected void skip() {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isSkipping();
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.skip();
  }
  
  protected void close() {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        Boolean _isOpen = it.isOpen();
        return Boolean.valueOf((!(_isOpen).booleanValue()));
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.close();
  }
  
  private Entry<T> setBuffer(final Entry<T> value) {
    return this._buffer.getAndSet(value);
  }
  
  private Entry<T> getBuffer() {
    return this._buffer.get();
  }
}
