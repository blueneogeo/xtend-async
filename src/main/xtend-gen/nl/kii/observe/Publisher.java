package nl.kii.observe;

import com.google.common.base.Objects;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Observable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A threadsafe non-blocking distributor of events to its registered listeners.
 * <p>
 * A Publisher is more lightweight than a stream with a streamobserver.
 * It does not have any flow control or async support, and has only
 * a single queue. Contrary to a stream, it allows for multiple
 * subscriptions, and each subscription can be unsubscribed by calling
 * the returned method.
 * <p>
 * For it to work correctly, the listeners should be non-blocking.
 */
@SuppressWarnings("all")
public class Publisher<T extends Object> extends Actor<T> implements Procedure1<T>, Observable<T> {
  @Atomic
  private final AtomicBoolean _publishing = new AtomicBoolean(true);
  
  @Atomic
  private final transient AtomicReference<List<Procedure1<T>>> _observers = new AtomicReference<List<Procedure1<T>>>();
  
  public Publisher() {
  }
  
  public Publisher(final boolean isPublishing) {
    this.setPublishing(Boolean.valueOf(isPublishing));
  }
  
  /**
   * Listen for publications from the publisher
   */
  public Procedure0 onChange(final Procedure1<? super T> observeFn) {
    List<Procedure1<T>> _observers = this.getObservers();
    boolean _equals = Objects.equal(_observers, null);
    if (_equals) {
      CopyOnWriteArrayList<Procedure1<T>> _copyOnWriteArrayList = new CopyOnWriteArrayList<Procedure1<T>>();
      this.setObservers(_copyOnWriteArrayList);
    }
    List<Procedure1<T>> _observers_1 = this.getObservers();
    _observers_1.add(((Procedure1<T>)observeFn));
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        List<Procedure1<T>> _observers = Publisher.this.getObservers();
        _observers.remove(observeFn);
      }
    };
    return _function;
  }
  
  public void act(final T message, final Procedure0 done) {
    boolean _and = false;
    List<Procedure1<T>> _observers = this.getObservers();
    boolean _notEquals = (!Objects.equal(_observers, null));
    if (!_notEquals) {
      _and = false;
    } else {
      Boolean _publishing = this.getPublishing();
      _and = (_publishing).booleanValue();
    }
    if (_and) {
      List<Procedure1<T>> _observers_1 = this.getObservers();
      for (final Procedure1<T> observer : _observers_1) {
        observer.apply(message);
      }
    }
    done.apply();
  }
  
  public int getSubscriptionCount() {
    int _xifexpression = (int) 0;
    List<Procedure1<T>> _observers = this.getObservers();
    boolean _notEquals = (!Objects.equal(_observers, null));
    if (_notEquals) {
      List<Procedure1<T>> _observers_1 = this.getObservers();
      _xifexpression = _observers_1.size();
    } else {
      _xifexpression = 0;
    }
    return _xifexpression;
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Publisher { publishing: ");
    Boolean _publishing = this.getPublishing();
    _builder.append(_publishing, "");
    _builder.append(", observers: ");
    List<Procedure1<T>> _observers = this.getObservers();
    int _size = _observers.size();
    _builder.append(_size, "");
    _builder.append(", inbox: ");
    Collection<T> _inbox = this.getInbox();
    int _size_1 = _inbox.size();
    _builder.append(_size_1, "");
    _builder.append(" } ");
    return _builder.toString();
  }
  
  public void setPublishing(final Boolean value) {
    this._publishing.set(value);
  }
  
  public Boolean getPublishing() {
    return this._publishing.get();
  }
  
  public Boolean getAndSetPublishing(final Boolean value) {
    return this._publishing.getAndSet(value);
  }
  
  private void setObservers(final List<Procedure1<T>> value) {
    this._observers.set(value);
  }
  
  private List<Procedure1<T>> getObservers() {
    return this._observers.get();
  }
  
  private List<Procedure1<T>> getAndSetObservers(final List<Procedure1<T>> value) {
    return this._observers.getAndSet(value);
  }
}
