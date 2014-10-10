package nl.kii.stream.source;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.IStream;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.source.StreamSplitter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;

/**
 * This splitter simply tries to pass all incoming values
 * from the source stream to the piped streams.
 * <p>
 * Flow control is maintained by only allowing the next
 * entry from the source stream if all piped streams are ready.
 * <p>
 * Note: This means that you need to make sure that all connected
 * streams do not block their flow, since one blocking stream
 * will block all streams from flowing.
 */
@SuppressWarnings("all")
public class StreamCopySplitter<R extends Object, T extends Object> extends StreamSplitter<R, T> {
  @Atomic
  private final AtomicReference<Entry<R, T>> _buffer = new AtomicReference<Entry<R, T>>();
  
  public StreamCopySplitter(final IStream<R, T> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<R, T> entry) {
    InputOutput.<Entry<R, T>>println(entry);
    this.setBuffer(entry);
    List<IStream<R, T>> _streams = this.getStreams();
    final Function1<IStream<R, T>, Boolean> _function = new Function1<IStream<R, T>, Boolean>() {
      public Boolean apply(final IStream<R, T> it) {
        return Boolean.valueOf(it.isReady());
      }
    };
    boolean _all = StreamSplitter.<IStream<R, T>>all(_streams, _function);
    if (_all) {
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
  
  protected Entry<R, T> publish() {
    Entry<R, T> _xifexpression = null;
    Entry<R, T> _buffer = this.getBuffer();
    boolean _notEquals = (!Objects.equal(_buffer, null));
    if (_notEquals) {
      Entry<R, T> _xblockexpression = null;
      {
        List<IStream<R, T>> _streams = this.getStreams();
        for (final IStream<R, T> s : _streams) {
          Entry<R, T> _buffer_1 = this.getBuffer();
          s.apply(_buffer_1);
        }
        _xblockexpression = this.setBuffer(null);
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  protected void next() {
    InputOutput.<String>println("next!");
    List<IStream<R, T>> _streams = this.getStreams();
    final Function1<IStream<R, T>, Boolean> _function = new Function1<IStream<R, T>, Boolean>() {
      public Boolean apply(final IStream<R, T> it) {
        return Boolean.valueOf(it.isReady());
      }
    };
    boolean _all = StreamSplitter.<IStream<R, T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.source.next();
    this.publish();
  }
  
  protected void skip() {
    List<IStream<R, T>> _streams = this.getStreams();
    final Function1<IStream<R, T>, Boolean> _function = new Function1<IStream<R, T>, Boolean>() {
      public Boolean apply(final IStream<R, T> it) {
        return Boolean.valueOf(it.isSkipping());
      }
    };
    boolean _all = StreamSplitter.<IStream<R, T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.skip();
  }
  
  protected void close() {
    List<IStream<R, T>> _streams = this.getStreams();
    final Function1<IStream<R, T>, Boolean> _function = new Function1<IStream<R, T>, Boolean>() {
      public Boolean apply(final IStream<R, T> it) {
        boolean _isOpen = it.isOpen();
        return Boolean.valueOf((!_isOpen));
      }
    };
    boolean _all = StreamSplitter.<IStream<R, T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.close();
  }
  
  private Entry<R, T> setBuffer(final Entry<R, T> value) {
    return this._buffer.getAndSet(value);
  }
  
  private Entry<R, T> getBuffer() {
    return this._buffer.get();
  }
}
