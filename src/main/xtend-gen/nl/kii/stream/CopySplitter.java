package nl.kii.stream;

import java.util.List;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Splitter;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamCommand;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;

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
public class CopySplitter<T extends Object> extends Splitter<T> {
  public CopySplitter(final Stream<T> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<T> entry) {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isReady();
      }
    };
    boolean _all = Splitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    List<Stream<T>> _streams_1 = this.getStreams();
    for (final Stream<T> it : _streams_1) {
      it.apply(entry);
    }
  }
  
  protected void onCommand(@Extension final StreamCommand msg) {
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
    List<Stream<T>> _streams = this.getStreams();
    InputOutput.<List<Stream<T>>>println(_streams);
    List<Stream<T>> _streams_1 = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isReady();
      }
    };
    boolean _all = Splitter.<Stream<T>>all(_streams_1, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    InputOutput.<String>println("next!");
    this.source.next();
  }
  
  protected void skip() {
    List<Stream<T>> _streams = this.getStreams();
    final Function1<Stream<T>, Boolean> _function = new Function1<Stream<T>, Boolean>() {
      public Boolean apply(final Stream<T> it) {
        return it.isSkipping();
      }
    };
    boolean _all = Splitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    InputOutput.<String>println("skip!");
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
    boolean _all = Splitter.<Stream<T>>all(_streams, _function);
    boolean _not = (!_all);
    if (_not) {
      return;
    }
    InputOutput.<String>println("close!");
    this.source.close();
  }
}
