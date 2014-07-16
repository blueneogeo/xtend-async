package nl.kii.async.annotation.test;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.async.annotation.test.Tester;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestAtomicAnnotation {
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger(2);
  
  @Atomic
  private final AtomicLong _longNumber = new AtomicLong();
  
  @Atomic
  private final AtomicDouble _price = new AtomicDouble();
  
  @Atomic
  private final AtomicReference<Tester> _tester = new AtomicReference<Tester>();
  
  @Test
  public void testInteger() {
    Integer _setCounter = this.setCounter(Integer.valueOf(3));
    Assert.assertEquals(2, (_setCounter).intValue());
    Integer _incCounter = this.incCounter();
    Assert.assertEquals(4, (_incCounter).intValue());
    Integer _decCounter = this.decCounter();
    Assert.assertEquals(3, (_decCounter).intValue());
    Integer _incCounter_1 = this.incCounter(Integer.valueOf(2));
    Assert.assertEquals(5, (_incCounter_1).intValue());
  }
  
  @Test
  public void testLong() {
    this.setLongNumber(Long.valueOf(45346465433435435L));
    Long _incLongNumber = this.incLongNumber();
    Assert.assertEquals(45346465433435436L, (_incLongNumber).longValue());
  }
  
  @Test
  public void testFloat() {
    this.setPrice(Double.valueOf(4.5));
    Double _price = this.getPrice();
    Assert.assertEquals(4.5, (_price).doubleValue(), 0);
  }
  
  private Integer setCounter(final Integer value) {
    return this._counter.getAndSet(value);
  }
  
  private Integer getCounter() {
    return this._counter.get();
  }
  
  private Integer incCounter() {
    return this._counter.incrementAndGet();
  }
  
  private Integer decCounter() {
    return this._counter.decrementAndGet();
  }
  
  private Integer incCounter(final Integer value) {
    return this._counter.addAndGet(value);
  }
  
  private Long setLongNumber(final Long value) {
    return this._longNumber.getAndSet(value);
  }
  
  private Long getLongNumber() {
    return this._longNumber.get();
  }
  
  private Long incLongNumber() {
    return this._longNumber.incrementAndGet();
  }
  
  private Long decLongNumber() {
    return this._longNumber.decrementAndGet();
  }
  
  private Long incLongNumber(final Long value) {
    return this._longNumber.addAndGet(value);
  }
  
  private Double setPrice(final Double value) {
    return this._price.getAndSet(value);
  }
  
  private Double getPrice() {
    return this._price.get();
  }
  
  private Double incPrice(final Double value) {
    return this._price.addAndGet(value);
  }
  
  private Tester setTester(final Tester value) {
    return this._tester.getAndSet(value);
  }
  
  private Tester getTester() {
    return this._tester.get();
  }
}
