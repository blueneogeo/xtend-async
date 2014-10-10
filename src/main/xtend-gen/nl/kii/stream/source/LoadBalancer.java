package nl.kii.stream.source;

import java.util.List;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.Value;
import nl.kii.stream.source.StreamSplitter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * This splitter sends each message to the first stream that is ready.
 * This means that each attached stream receives different messages.
 */
@SuppressWarnings("all")
public class LoadBalancer<R extends Object, T extends Object> extends StreamSplitter<R, T> {
  public LoadBalancer(final IStream<R, T> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<R, T> entry) {
    boolean _matched = false;
    if (!_matched) {
      if (entry instanceof Value) {
        _matched=true;
        List<IStream<R, T>> _streams = this.getStreams();
        for (final IStream<R, T> stream : _streams) {
          boolean _isReady = stream.isReady();
          if (_isReady) {
            stream.apply(entry);
            return;
          }
        }
      }
    }
    if (!_matched) {
      if (entry instanceof Finish) {
        _matched=true;
        List<IStream<R, T>> _streams = this.getStreams();
        for (final IStream<R, T> stream : _streams) {
          stream.finish();
        }
      }
    }
    if (!_matched) {
      if (entry instanceof nl.kii.stream.Error) {
        _matched=true;
        List<IStream<R, T>> _streams = this.getStreams();
        for (final IStream<R, T> stream : _streams) {
          stream.error(((nl.kii.stream.Error<R, T>)entry).error);
        }
      }
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
  
  protected void next() {
    this.source.next();
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
    this.source.close();
  }
}
