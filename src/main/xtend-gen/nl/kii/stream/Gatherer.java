package nl.kii.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExt;
import nl.kii.stream.Streamable;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Gatherer can collect data from various asynchronous sources.
 * <p>
 * You use it by calling the await(name) method, which gives you
 * a Procedure1 in return. The async code elsewhere can then call
 * this procedure with a result when it is ready.
 * <p>
 * AFTER creating all the await functions you need, you can listen
 * for the functions to finish with a result as an Observable.
 * You can both monitor the results coming in, as well as respond
 * to the closing of the stream using listen.onFinish [ ... ]
 * <p>
 * In the meantime, you also can asynchronously check if the
 * collector has finished using the isFinished method.
 * <p>
 * When the wait functions have finished, you can request the result
 * data of all awaited functions with the result call, which gives
 * you a map<name, value> of all values. This map also gets filled
 * as the data comes in and is a concurrent map.
 * <p>
 * The awaited functions often work great as closures. For example:
 * <p>
 * <pre>
 * val c = new Gatherer<JSON>
 * // perform slow async calls
 * API.loadUser(12, c.await('user'))
 * API.loadRights(45, c.await('rights'))
 * // listen to the results
 * c.stream.onFinish [
 * 		val it = c.result
 * 		println('loaded user: ' + get('user'))
 * 		println('loaded rights: ' + get('rights'))
 * ]
 * </pre>
 */
@SuppressWarnings("all")
public class Gatherer<T extends Object> implements Streamable<Pair<String,T>> {
  private final Stream<Pair<String,T>> stream = new Stream<Pair<String, T>>();
  
  protected final AtomicInteger count = new AtomicInteger();
  
  protected final AtomicInteger total = new AtomicInteger();
  
  private final ConcurrentHashMap<String,T> data = new ConcurrentHashMap<String, T>();
  
  public Gatherer() {
    final Procedure1<Pair<String,T>> _function = new Procedure1<Pair<String,T>>() {
      public void apply(final Pair<String,T> it) {
        String _key = it.getKey();
        T _value = it.getValue();
        Gatherer.this.data.put(_key, _value);
      }
    };
    StreamExt.<Pair<String,T>>onEach(this.stream, _function);
  }
  
  public Procedure1<? super T> await(final String name) {
    this.total.incrementAndGet();
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        Pair<String,T> _mappedTo = Pair.<String, T>of(name, it);
        Gatherer.this.stream.push(_mappedTo);
        int _incrementAndGet = Gatherer.this.count.incrementAndGet();
        int _get = Gatherer.this.total.get();
        boolean _equals = (_incrementAndGet == _get);
        if (_equals) {
          Gatherer.this.stream.finish();
        }
      }
    };
    return _function;
  }
  
  public void collect(final String name, final T value) {
    Procedure1<? super T> _await = this.await(name);
    _await.apply(value);
  }
  
  public Stream<Pair<String,T>> stream() {
    return this.stream;
  }
  
  public ConcurrentHashMap<String,T> result() {
    return this.data;
  }
  
  public boolean isFinished() {
    int _get = this.count.get();
    int _get_1 = this.total.get();
    return (_get == _get_1);
  }
  
  public Stream<Pair<String,T>> onFinish(final Procedure1<? super Map<String,T>> closure) {
    final Procedure1<Void> _function = new Procedure1<Void>() {
      public void apply(final Void it) {
        ConcurrentHashMap<String,T> _result = Gatherer.this.result();
        closure.apply(_result);
      }
    };
    return StreamExt.<Pair<String,T>>onFinish(this.stream, _function);
  }
}
