package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.stream.IStream;
import nl.kii.stream.StreamObserver;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class StreamMonitor implements StreamObserver, Observable<StreamMonitor> {
  private final Publisher<StreamMonitor> publisher = new Publisher<StreamMonitor>();
  
  @Atomic
  private final AtomicBoolean _active = new AtomicBoolean();
  
  @Atomic
  private final AtomicReference<IStream<?, ?>> _stream = new AtomicReference<IStream<?, ?>>();
  
  @Atomic
  private final AtomicLong _valueCount = new AtomicLong();
  
  @Atomic
  private final AtomicLong _finishCount = new AtomicLong();
  
  @Atomic
  private final AtomicLong _errorCount = new AtomicLong();
  
  @Atomic
  private final AtomicLong _startTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _closeTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _firstEntryTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _firstValueTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _firstErrorTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _firstFinishTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _lastEntryTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _lastValueTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _lastFinishTS = new AtomicLong();
  
  @Atomic
  private final AtomicLong _lastErrorTS = new AtomicLong();
  
  @Atomic
  private final AtomicReference<Object> _lastValue = new AtomicReference<Object>();
  
  @Atomic
  private final AtomicReference<Throwable> _lastError = new AtomicReference<Throwable>();
  
  public void setPublishing(final boolean isOn) {
    this.publisher.setPublishing(Boolean.valueOf(isOn));
  }
  
  public void onValue(final Object from, final Object value) {
    InputOutput.<String>println("yo!");
    Boolean _active = this.getActive();
    boolean _not = (!(_active).booleanValue());
    if (_not) {
      return;
    }
    Long _firstEntryTS = this.getFirstEntryTS();
    boolean _equals = Objects.equal(_firstEntryTS, null);
    if (_equals) {
      long _now = this.now();
      this.setFirstEntryTS(Long.valueOf(_now));
    }
    Long _firstValueTS = this.getFirstValueTS();
    boolean _equals_1 = Objects.equal(_firstValueTS, null);
    if (_equals_1) {
      long _now_1 = this.now();
      this.setFirstValueTS(Long.valueOf(_now_1));
    }
    long _now_2 = this.now();
    this.setLastEntryTS(Long.valueOf(_now_2));
    long _now_3 = this.now();
    this.setLastValueTS(Long.valueOf(_now_3));
    this.setLastValue(value);
    this.incValueCount();
    this.publisher.apply(this);
  }
  
  public void onError(final Object from, final Throwable t) {
    Boolean _active = this.getActive();
    boolean _not = (!(_active).booleanValue());
    if (_not) {
      return;
    }
    Long _firstEntryTS = this.getFirstEntryTS();
    boolean _equals = Objects.equal(_firstEntryTS, null);
    if (_equals) {
      long _now = this.now();
      this.setFirstEntryTS(Long.valueOf(_now));
    }
    Long _firstErrorTS = this.getFirstErrorTS();
    boolean _equals_1 = Objects.equal(_firstErrorTS, null);
    if (_equals_1) {
      long _now_1 = this.now();
      this.setFirstErrorTS(Long.valueOf(_now_1));
    }
    long _now_2 = this.now();
    this.setLastEntryTS(Long.valueOf(_now_2));
    long _now_3 = this.now();
    this.setLastErrorTS(Long.valueOf(_now_3));
    this.setLastError(t);
    this.incErrorCount();
    this.publisher.apply(this);
  }
  
  public void onFinish(final Object from, final int level) {
    Boolean _active = this.getActive();
    boolean _not = (!(_active).booleanValue());
    if (_not) {
      return;
    }
    Long _firstEntryTS = this.getFirstEntryTS();
    boolean _equals = Objects.equal(_firstEntryTS, null);
    if (_equals) {
      long _now = this.now();
      this.setFirstEntryTS(Long.valueOf(_now));
    }
    Long _firstFinishTS = this.getFirstFinishTS();
    boolean _equals_1 = Objects.equal(_firstFinishTS, null);
    if (_equals_1) {
      long _now_1 = this.now();
      this.setFirstFinishTS(Long.valueOf(_now_1));
    }
    long _now_2 = this.now();
    this.setLastEntryTS(Long.valueOf(_now_2));
    long _now_3 = this.now();
    this.setLastFinishTS(Long.valueOf(_now_3));
    this.incFinishCount();
    this.publisher.apply(this);
  }
  
  public void onClosed() {
    Boolean _active = this.getActive();
    boolean _not = (!(_active).booleanValue());
    if (_not) {
      return;
    }
    long _now = this.now();
    this.setLastEntryTS(Long.valueOf(_now));
    long _now_1 = this.now();
    this.setCloseTS(Long.valueOf(_now_1));
    this.publisher.apply(this);
  }
  
  public Procedure0 onChange(final Procedure1<? super StreamMonitor> observeFn) {
    return this.publisher.onChange(observeFn);
  }
  
  private long now() {
    return System.currentTimeMillis();
  }
  
  public void setActive(final Boolean value) {
    this._active.set(value);
  }
  
  public Boolean getActive() {
    return this._active.get();
  }
  
  protected Boolean getAndSetActive(final Boolean value) {
    return this._active.getAndSet(value);
  }
  
  public void setStream(final IStream<?, ?> value) {
    this._stream.set(value);
  }
  
  public IStream<?, ?> getStream() {
    return this._stream.get();
  }
  
  protected IStream<?, ?> getAndSetStream(final IStream<?, ?> value) {
    return this._stream.getAndSet(value);
  }
  
  public void setValueCount(final Long value) {
    this._valueCount.set(value);
  }
  
  public Long getValueCount() {
    return this._valueCount.get();
  }
  
  protected Long getAndSetValueCount(final Long value) {
    return this._valueCount.getAndSet(value);
  }
  
  protected Long incValueCount() {
    return this._valueCount.incrementAndGet();
  }
  
  protected Long decValueCount() {
    return this._valueCount.decrementAndGet();
  }
  
  protected Long incValueCount(final Long value) {
    return this._valueCount.addAndGet(value);
  }
  
  public void setFinishCount(final Long value) {
    this._finishCount.set(value);
  }
  
  public Long getFinishCount() {
    return this._finishCount.get();
  }
  
  protected Long getAndSetFinishCount(final Long value) {
    return this._finishCount.getAndSet(value);
  }
  
  protected Long incFinishCount() {
    return this._finishCount.incrementAndGet();
  }
  
  protected Long decFinishCount() {
    return this._finishCount.decrementAndGet();
  }
  
  protected Long incFinishCount(final Long value) {
    return this._finishCount.addAndGet(value);
  }
  
  public void setErrorCount(final Long value) {
    this._errorCount.set(value);
  }
  
  public Long getErrorCount() {
    return this._errorCount.get();
  }
  
  protected Long getAndSetErrorCount(final Long value) {
    return this._errorCount.getAndSet(value);
  }
  
  protected Long incErrorCount() {
    return this._errorCount.incrementAndGet();
  }
  
  protected Long decErrorCount() {
    return this._errorCount.decrementAndGet();
  }
  
  protected Long incErrorCount(final Long value) {
    return this._errorCount.addAndGet(value);
  }
  
  public void setStartTS(final Long value) {
    this._startTS.set(value);
  }
  
  public Long getStartTS() {
    return this._startTS.get();
  }
  
  protected Long getAndSetStartTS(final Long value) {
    return this._startTS.getAndSet(value);
  }
  
  protected Long incStartTS() {
    return this._startTS.incrementAndGet();
  }
  
  protected Long decStartTS() {
    return this._startTS.decrementAndGet();
  }
  
  protected Long incStartTS(final Long value) {
    return this._startTS.addAndGet(value);
  }
  
  public void setCloseTS(final Long value) {
    this._closeTS.set(value);
  }
  
  public Long getCloseTS() {
    return this._closeTS.get();
  }
  
  protected Long getAndSetCloseTS(final Long value) {
    return this._closeTS.getAndSet(value);
  }
  
  protected Long incCloseTS() {
    return this._closeTS.incrementAndGet();
  }
  
  protected Long decCloseTS() {
    return this._closeTS.decrementAndGet();
  }
  
  protected Long incCloseTS(final Long value) {
    return this._closeTS.addAndGet(value);
  }
  
  public void setFirstEntryTS(final Long value) {
    this._firstEntryTS.set(value);
  }
  
  public Long getFirstEntryTS() {
    return this._firstEntryTS.get();
  }
  
  protected Long getAndSetFirstEntryTS(final Long value) {
    return this._firstEntryTS.getAndSet(value);
  }
  
  protected Long incFirstEntryTS() {
    return this._firstEntryTS.incrementAndGet();
  }
  
  protected Long decFirstEntryTS() {
    return this._firstEntryTS.decrementAndGet();
  }
  
  protected Long incFirstEntryTS(final Long value) {
    return this._firstEntryTS.addAndGet(value);
  }
  
  public void setFirstValueTS(final Long value) {
    this._firstValueTS.set(value);
  }
  
  public Long getFirstValueTS() {
    return this._firstValueTS.get();
  }
  
  protected Long getAndSetFirstValueTS(final Long value) {
    return this._firstValueTS.getAndSet(value);
  }
  
  protected Long incFirstValueTS() {
    return this._firstValueTS.incrementAndGet();
  }
  
  protected Long decFirstValueTS() {
    return this._firstValueTS.decrementAndGet();
  }
  
  protected Long incFirstValueTS(final Long value) {
    return this._firstValueTS.addAndGet(value);
  }
  
  public void setFirstErrorTS(final Long value) {
    this._firstErrorTS.set(value);
  }
  
  public Long getFirstErrorTS() {
    return this._firstErrorTS.get();
  }
  
  protected Long getAndSetFirstErrorTS(final Long value) {
    return this._firstErrorTS.getAndSet(value);
  }
  
  protected Long incFirstErrorTS() {
    return this._firstErrorTS.incrementAndGet();
  }
  
  protected Long decFirstErrorTS() {
    return this._firstErrorTS.decrementAndGet();
  }
  
  protected Long incFirstErrorTS(final Long value) {
    return this._firstErrorTS.addAndGet(value);
  }
  
  public void setFirstFinishTS(final Long value) {
    this._firstFinishTS.set(value);
  }
  
  public Long getFirstFinishTS() {
    return this._firstFinishTS.get();
  }
  
  protected Long getAndSetFirstFinishTS(final Long value) {
    return this._firstFinishTS.getAndSet(value);
  }
  
  protected Long incFirstFinishTS() {
    return this._firstFinishTS.incrementAndGet();
  }
  
  protected Long decFirstFinishTS() {
    return this._firstFinishTS.decrementAndGet();
  }
  
  protected Long incFirstFinishTS(final Long value) {
    return this._firstFinishTS.addAndGet(value);
  }
  
  public void setLastEntryTS(final Long value) {
    this._lastEntryTS.set(value);
  }
  
  public Long getLastEntryTS() {
    return this._lastEntryTS.get();
  }
  
  protected Long getAndSetLastEntryTS(final Long value) {
    return this._lastEntryTS.getAndSet(value);
  }
  
  protected Long incLastEntryTS() {
    return this._lastEntryTS.incrementAndGet();
  }
  
  protected Long decLastEntryTS() {
    return this._lastEntryTS.decrementAndGet();
  }
  
  protected Long incLastEntryTS(final Long value) {
    return this._lastEntryTS.addAndGet(value);
  }
  
  public void setLastValueTS(final Long value) {
    this._lastValueTS.set(value);
  }
  
  public Long getLastValueTS() {
    return this._lastValueTS.get();
  }
  
  protected Long getAndSetLastValueTS(final Long value) {
    return this._lastValueTS.getAndSet(value);
  }
  
  protected Long incLastValueTS() {
    return this._lastValueTS.incrementAndGet();
  }
  
  protected Long decLastValueTS() {
    return this._lastValueTS.decrementAndGet();
  }
  
  protected Long incLastValueTS(final Long value) {
    return this._lastValueTS.addAndGet(value);
  }
  
  public void setLastFinishTS(final Long value) {
    this._lastFinishTS.set(value);
  }
  
  public Long getLastFinishTS() {
    return this._lastFinishTS.get();
  }
  
  protected Long getAndSetLastFinishTS(final Long value) {
    return this._lastFinishTS.getAndSet(value);
  }
  
  protected Long incLastFinishTS() {
    return this._lastFinishTS.incrementAndGet();
  }
  
  protected Long decLastFinishTS() {
    return this._lastFinishTS.decrementAndGet();
  }
  
  protected Long incLastFinishTS(final Long value) {
    return this._lastFinishTS.addAndGet(value);
  }
  
  public void setLastErrorTS(final Long value) {
    this._lastErrorTS.set(value);
  }
  
  public Long getLastErrorTS() {
    return this._lastErrorTS.get();
  }
  
  protected Long getAndSetLastErrorTS(final Long value) {
    return this._lastErrorTS.getAndSet(value);
  }
  
  protected Long incLastErrorTS() {
    return this._lastErrorTS.incrementAndGet();
  }
  
  protected Long decLastErrorTS() {
    return this._lastErrorTS.decrementAndGet();
  }
  
  protected Long incLastErrorTS(final Long value) {
    return this._lastErrorTS.addAndGet(value);
  }
  
  public void setLastValue(final Object value) {
    this._lastValue.set(value);
  }
  
  public Object getLastValue() {
    return this._lastValue.get();
  }
  
  protected Object getAndSetLastValue(final Object value) {
    return this._lastValue.getAndSet(value);
  }
  
  public void setLastError(final Throwable value) {
    this._lastError.set(value);
  }
  
  public Throwable getLastError() {
    return this._lastError.get();
  }
  
  protected Throwable getAndSetLastError(final Throwable value) {
    return this._lastError.getAndSet(value);
  }
}
