package nl.kii.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.Entry;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class Gatherer<T extends Object> extends Promise<Map<String, T>> {
  private final ConcurrentHashMap<String, T> data = new ConcurrentHashMap<String, T>();
  
  protected final AtomicInteger count = new AtomicInteger();
  
  protected final AtomicInteger total = new AtomicInteger();
  
  public Promise<T> await(final String name) {
    final Promise<T> promise = new Promise<T>();
    this.total.incrementAndGet();
    final Promise<T> _function = new Promise<T>() {
      public void apply(final Entry<T> it) {
        Stream<Pair<String, Entry<T>>> _stream = StreamExtensions.<Pair<String, Entry<T>>>stream();
        Pair<String, Entry<T>> _mappedTo = Pair.<String, Entry<T>>of(name, it);
        _stream.push(_mappedTo);
        int _incrementAndGet = Gatherer.this.count.incrementAndGet();
        int _get = Gatherer.this.total.get();
        boolean _equals = (_incrementAndGet == _get);
        if (_equals) {
          Stream<Object> _stream_1 = StreamExtensions.<Object>stream();
          _stream_1.finish();
        }
      }
    };
    return _function;
  }
}
