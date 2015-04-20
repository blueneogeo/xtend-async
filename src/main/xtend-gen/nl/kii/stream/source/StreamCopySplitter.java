package nl.kii.stream.source;

import com.google.common.base.Objects;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.IStream;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.source.StreamSplitter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

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
public class StreamCopySplitter<I extends Object, O extends Object> extends StreamSplitter<I, O> {
  @Atomic
  private final AtomicReference<Entry<I, O>> _buffer = new AtomicReference<Entry<I, O>>();
  
  public StreamCopySplitter(final IStream<I, O> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  @Override
  protected void onEntry(final Entry<I, O> entry) {
    this.setBuffer(entry);
    List<IStream<I, ?>> _streams = this.getStreams();
    final Function1<IStream<I, ?>, Boolean> _function = (IStream<I, ?> it) -> {
      return Boolean.valueOf(it.isReady());
    };
    boolean _all = StreamSplitter.<IStream<I, ?>>all(_streams, _function);
    if (_all) {
      this.publish();
    }
  }
  
  @Override
  protected void onCommand(@Extension final StreamEvent msg) {
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
  
  protected void publish() {
    Entry<I, O> _buffer = this.getBuffer();
    boolean _notEquals = (!Objects.equal(_buffer, null));
    if (_notEquals) {
      List<IStream<I, ?>> _streams = this.getStreams();
      for (final IStream<I, ?> s : _streams) {
        Entry<I, O> _buffer_1 = this.getBuffer();
        s.apply(_buffer_1);
      }
      this.setBuffer(null);
    }
  }
  
  protected void next() {
    List<IStream<I, ?>> _streams = this.getStreams();
    final Function1<IStream<I, ?>, Boolean> _function = (IStream<I, ?> it) -> {
      return Boolean.valueOf(it.isReady());
    };
    boolean _all = StreamSplitter.<IStream<I, ?>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.source.next();
    this.publish();
  }
  
  protected void skip() {
    List<IStream<I, ?>> _streams = this.getStreams();
    final Function1<IStream<I, ?>, Boolean> _function = (IStream<I, ?> it) -> {
      return Boolean.valueOf(it.isSkipping());
    };
    boolean _all = StreamSplitter.<IStream<I, ?>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.skip();
  }
  
  protected void close() {
    List<IStream<I, ?>> _streams = this.getStreams();
    final Function1<IStream<I, ?>, Boolean> _function = (IStream<I, ?> it) -> {
      boolean _isOpen = it.isOpen();
      return Boolean.valueOf((!_isOpen));
    };
    boolean _all = StreamSplitter.<IStream<I, ?>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.publish();
    this.source.close();
  }
  
  private void setBuffer(final Entry<I, O> value) {
    this._buffer.set(value);
  }
  
  private Entry<I, O> getBuffer() {
    return this._buffer.get();
  }
  
  private Entry<I, O> getAndSetBuffer(final Entry<I, O> value) {
    return this._buffer.getAndSet(value);
  }
}
