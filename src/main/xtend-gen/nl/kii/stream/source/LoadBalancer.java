package nl.kii.stream.source;

import java.util.List;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.source.StreamSplitter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * This splitter sends each message to the first stream that is ready.
 * This means that each attached stream receives different messages.
 */
@SuppressWarnings("all")
public class LoadBalancer<T extends Object> extends StreamSplitter<T> {
  public LoadBalancer(final Stream<T> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<T> entry) {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Value<R, T>; it cannot be parameterized with arguments <T>"
      + "\nIncorrect number of arguments for type Finish<R, T>; it cannot be parameterized with arguments <T>"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <T>");
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
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return Boolean.valueOf(it.isSkipping());
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.source.skip();
  }
  
  protected void close() {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        boolean _isOpen = it.isOpen();
        return Boolean.valueOf((!_isOpen));
      }
    };
    boolean _all = StreamSplitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    this.source.close();
  }
}
