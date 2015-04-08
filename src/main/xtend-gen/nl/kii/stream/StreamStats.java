package nl.kii.stream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class StreamStats {
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
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("values:   ");
    Long _valueCount = this.getValueCount();
    _builder.append(_valueCount, "");
    _builder.newLineIfNotEmpty();
    _builder.append("errors:   ");
    Long _errorCount = this.getErrorCount();
    _builder.append(_errorCount, "");
    _builder.newLineIfNotEmpty();
    _builder.append("finishes: ");
    Long _finishCount = this.getFinishCount();
    _builder.append(_finishCount, "");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("time taken: ");
    long _timeTaken = this.timeTaken();
    _builder.append(_timeTaken, "");
    _builder.append(" ms");
    _builder.newLineIfNotEmpty();
    _builder.append("value rate: ");
    float _valueRate = this.valueRate();
    _builder.append(_valueRate, "");
    _builder.append(" / ms");
    _builder.newLineIfNotEmpty();
    _builder.append("error rate: ");
    float _errorRate = this.errorRate();
    _builder.append(_errorRate, "");
    _builder.append(" / ms");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    {
      Long _startTS = this.getStartTS();
      boolean _greaterThan = ((_startTS).longValue() > 0);
      if (_greaterThan) {
        _builder.append("created at: ");
        Long _startTS_1 = this.getStartTS();
        String _text = StreamStats.text((_startTS_1).longValue());
        _builder.append(_text, "");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("first entry at: ");
        Long _firstEntryTS = this.getFirstEntryTS();
        String _text_1 = StreamStats.text((_firstEntryTS).longValue());
        _builder.append(_text_1, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("first value at: ");
        Long _firstValueTS = this.getFirstValueTS();
        String _text_2 = StreamStats.text((_firstValueTS).longValue());
        _builder.append(_text_2, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("first error at: ");
        Long _firstErrorTS = this.getFirstErrorTS();
        String _text_3 = StreamStats.text((_firstErrorTS).longValue());
        _builder.append(_text_3, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("first finish at: ");
        Long _firstFinishTS = this.getFirstFinishTS();
        String _text_4 = StreamStats.text((_firstFinishTS).longValue());
        _builder.append(_text_4, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("last entry at: ");
        Long _lastEntryTS = this.getLastEntryTS();
        String _text_5 = StreamStats.text((_lastEntryTS).longValue());
        _builder.append(_text_5, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("last value at: ");
        Long _lastValueTS = this.getLastValueTS();
        String _text_6 = StreamStats.text((_lastValueTS).longValue());
        _builder.append(_text_6, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("last  error at: ");
        Long _lastErrorTS = this.getLastErrorTS();
        String _text_7 = StreamStats.text((_lastErrorTS).longValue());
        _builder.append(_text_7, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.append("last finish at: ");
        Long _lastFinishTS = this.getLastFinishTS();
        String _text_8 = StreamStats.text((_lastFinishTS).longValue());
        _builder.append(_text_8, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("closed at:  ");
        Long _closeTS = this.getCloseTS();
        String _text_9 = StreamStats.text((_closeTS).longValue());
        _builder.append(_text_9, "");
        _builder.newLineIfNotEmpty();
      } else {
        _builder.append("not started");
        _builder.newLine();
      }
    }
    _builder.newLine();
    {
      Long _closeTS_1 = this.getCloseTS();
      boolean _greaterThan_1 = ((_closeTS_1).longValue() > 0);
      if (_greaterThan_1) {
        _builder.append("closed at:  ");
        Long _closeTS_2 = this.getCloseTS();
        String _text_10 = StreamStats.text((_closeTS_2).longValue());
        _builder.append(_text_10, "");
        _builder.newLineIfNotEmpty();
      } else {
        _builder.append("stream still open");
        _builder.newLine();
      }
    }
    _builder.newLine();
    return _builder.toString();
  }
  
  public long timeTaken() {
    long _now = StreamStats.now();
    Long _startTS = this.getStartTS();
    return (_now - (_startTS).longValue());
  }
  
  public float valueRate() {
    Long _valueCount = this.getValueCount();
    Float _float = new Float((_valueCount).longValue());
    long _timeTaken = this.timeTaken();
    return ((_float).floatValue() / _timeTaken);
  }
  
  public float errorRate() {
    Long _errorCount = this.getErrorCount();
    Float _float = new Float((_errorCount).longValue());
    long _timeTaken = this.timeTaken();
    return ((_float).floatValue() / _timeTaken);
  }
  
  private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
  
  private static String text(final long timestamp) {
    String _xblockexpression = null;
    {
      if ((timestamp == 0)) {
        return "-";
      }
      Date _date = new Date(timestamp);
      _xblockexpression = StreamStats.DATEFORMAT.format(_date);
    }
    return _xblockexpression;
  }
  
  private static long now() {
    return System.currentTimeMillis();
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
