package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.Observable;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Transforms a stream into an observable.
 * Note that by doing so you lose flow control.
 */
@SuppressWarnings("all")
public class StreamObserver<T extends Object> implements Observable<T> {
  private final AtomicReference<List<Procedure1<T>>> observers = new AtomicReference<List<Procedure1<T>>>();
  
  public StreamObserver(final Stream<T> source) {
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T value) {
        List<Procedure1<T>> _get = StreamObserver.this.observers.get();
        if (_get!=null) {
          final Procedure1<Procedure1<T>> _function = new Procedure1<Procedure1<T>>() {
            public void apply(final Procedure1<T> it) {
              it.apply(value);
            }
          };
          IterableExtensions.<Procedure1<T>>forEach(_get, _function);
        }
      }
    };
    StreamExtensions.<T>onEach(source, _function);
  }
  
  public Procedure0 onChange(final Procedure1<? super T> observeFn) {
    List<Procedure1<T>> _get = this.observers.get();
    boolean _equals = Objects.equal(_get, null);
    if (_equals) {
      LinkedList<Procedure1<T>> _newLinkedList = CollectionLiterals.<Procedure1<T>>newLinkedList(((Procedure1<T>)observeFn));
      this.observers.set(_newLinkedList);
    } else {
      List<Procedure1<T>> _get_1 = this.observers.get();
      _get_1.add(((Procedure1<T>)observeFn));
    }
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        List<Procedure1<T>> _get = StreamObserver.this.observers.get();
        _get.remove(observeFn);
      }
    };
    return _function;
  }
}
