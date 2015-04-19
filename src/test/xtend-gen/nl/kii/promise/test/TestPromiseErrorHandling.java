package nl.kii.promise.test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.util.JUnitExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromiseErrorHandling {
  @Atomic
  private final AtomicInteger _value = new AtomicInteger();
  
  @Atomic
  private final AtomicBoolean _match1 = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _match2 = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _match3 = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _match4 = new AtomicBoolean();
  
  @Test
  public void canMonitorErrors() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        JUnitExtensions.fail("an error should occur");
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch1(Boolean.valueOf(true));
      }
    };
    PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_2);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Boolean _match1 = this.getMatch1();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match1);
  }
  
  @Test
  public void canMatchErrorTypes() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        JUnitExtensions.fail("an error should occur");
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch1(Boolean.valueOf(true));
      }
    };
    IPromise<Boolean, Boolean> _on = PromiseExtensions.<Boolean, Boolean>on(_then, ArithmeticException.class, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch2(Boolean.valueOf(true));
      }
    };
    IPromise<Boolean, Boolean> _on_1 = PromiseExtensions.<Boolean, Boolean>on(_on, IllegalArgumentException.class, _function_3);
    final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch3(Boolean.valueOf(true));
      }
    };
    IPromise<Boolean, Boolean> _on_2 = PromiseExtensions.<Boolean, Boolean>on(_on_1, Exception.class, _function_4);
    final Procedure1<Throwable> _function_5 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch4(Boolean.valueOf(true));
      }
    };
    PromiseExtensions.<Boolean, Boolean>on(_on_2, Throwable.class, _function_5);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Boolean _match1 = this.getMatch1();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match1);
    Boolean _match2 = this.getMatch2();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(false), _match2);
    Boolean _match3 = this.getMatch3();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match3);
    Boolean _match4 = this.getMatch4();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match4);
  }
  
  @Test
  public void canSwallowErrorTypes() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        JUnitExtensions.fail("an error should occur");
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch1(Boolean.valueOf(true));
      }
    };
    IPromise<Boolean, Boolean> _on = PromiseExtensions.<Boolean, Boolean>on(_then, ArithmeticException.class, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch2(Boolean.valueOf(true));
      }
    };
    IPromise<Boolean, Boolean> _effect = PromiseExtensions.<Boolean, Boolean>effect(_on, Exception.class, _function_3);
    final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromiseErrorHandling.this.setMatch3(Boolean.valueOf(true));
      }
    };
    PromiseExtensions.<Boolean, Boolean>on(_effect, Throwable.class, _function_4);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Boolean _match1 = this.getMatch1();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match1);
    Boolean _match2 = this.getMatch2();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match2);
    Boolean _match3 = this.getMatch3();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(false), _match3);
  }
  
  @Test
  public void canMapErrors() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Function1<Throwable, Integer> _function_1 = new Function1<Throwable, Integer>() {
      @Override
      public Integer apply(final Throwable it) {
        return Integer.valueOf(10);
      }
    };
    SubPromise<Integer, Integer> _map_1 = PromiseExtensions.<Integer, Integer>map(_map, ArithmeticException.class, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        Throwable _cause = it.getCause();
        InputOutput.<Throwable>println(_cause);
        TestPromiseErrorHandling.this.setMatch1(Boolean.valueOf(true));
      }
    };
    IPromise<Integer, Integer> _on = PromiseExtensions.<Integer, Integer>on(_map_1, Exception.class, _function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromiseErrorHandling.this.setMatch2(Boolean.valueOf(true));
      }
    };
    _on.then(_function_3);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Boolean _match1 = this.getMatch1();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(false), _match1);
    Boolean _match2 = this.getMatch2();
    JUnitExtensions.<Boolean>operator_spaceship(
      Boolean.valueOf(true), _match2);
  }
  
  @Test
  public void canFilterMapErrors() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Function1<Throwable, Integer> _function_1 = new Function1<Throwable, Integer>() {
      @Override
      public Integer apply(final Throwable it) {
        return Integer.valueOf(10);
      }
    };
    SubPromise<Integer, Integer> _map_1 = PromiseExtensions.<Integer, Integer>map(_map, IllegalArgumentException.class, _function_1);
    final Function1<Throwable, Integer> _function_2 = new Function1<Throwable, Integer>() {
      @Override
      public Integer apply(final Throwable it) {
        return Integer.valueOf(20);
      }
    };
    SubPromise<Integer, Integer> _map_2 = PromiseExtensions.<Integer, Integer>map(_map_1, ArithmeticException.class, _function_2);
    final Function1<Throwable, Integer> _function_3 = new Function1<Throwable, Integer>() {
      @Override
      public Integer apply(final Throwable it) {
        return Integer.valueOf(30);
      }
    };
    SubPromise<Integer, Integer> _map_3 = PromiseExtensions.<Integer, Integer>map(_map_2, Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromiseErrorHandling.this.setValue(it);
      }
    };
    _map_3.then(_function_4);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Integer _value = this.getValue();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(20), _value);
  }
  
  @Test
  public void canFilterAsyncMapErrors() {
    final Promise<Integer> p = new Promise<Integer>();
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Function1<Throwable, IPromise<?, Integer>> _function_1 = new Function1<Throwable, IPromise<?, Integer>>() {
      @Override
      public IPromise<?, Integer> apply(final Throwable it) {
        return PromiseExtensions.<Integer>promise(Integer.valueOf(20));
      }
    };
    SubPromise<Integer, Integer> _call = PromiseExtensions.<Integer, Integer>call(_map, IllegalArgumentException.class, _function_1);
    final Function1<Throwable, IPromise<?, Integer>> _function_2 = new Function1<Throwable, IPromise<?, Integer>>() {
      @Override
      public IPromise<?, Integer> apply(final Throwable it) {
        Promise<Integer> _xblockexpression = null;
        {
          InputOutput.<String>println("x2");
          _xblockexpression = PromiseExtensions.<Integer>promise(Integer.valueOf(30));
        }
        return _xblockexpression;
      }
    };
    SubPromise<Integer, Integer> _call_1 = PromiseExtensions.<Integer, Integer>call(_call, ArithmeticException.class, _function_2);
    final Function1<Throwable, IPromise<?, Integer>> _function_3 = new Function1<Throwable, IPromise<?, Integer>>() {
      @Override
      public IPromise<?, Integer> apply(final Throwable it) {
        return PromiseExtensions.<Integer>promise(Integer.valueOf(40));
      }
    };
    SubPromise<Integer, Integer> _call_2 = PromiseExtensions.<Integer, Integer>call(_call_1, Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromiseErrorHandling.this.setValue(it);
      }
    };
    _call_2.then(_function_4);
    PromiseExtensions.<Integer, Integer>operator_doubleLessThan(p, Integer.valueOf(10));
    Integer _value = this.getValue();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(30), _value);
  }
  
  private void setValue(final Integer value) {
    this._value.set(value);
  }
  
  private Integer getValue() {
    return this._value.get();
  }
  
  private Integer getAndSetValue(final Integer value) {
    return this._value.getAndSet(value);
  }
  
  private Integer incValue() {
    return this._value.incrementAndGet();
  }
  
  private Integer decValue() {
    return this._value.decrementAndGet();
  }
  
  private Integer incValue(final Integer value) {
    return this._value.addAndGet(value);
  }
  
  private void setMatch1(final Boolean value) {
    this._match1.set(value);
  }
  
  private Boolean getMatch1() {
    return this._match1.get();
  }
  
  private Boolean getAndSetMatch1(final Boolean value) {
    return this._match1.getAndSet(value);
  }
  
  private void setMatch2(final Boolean value) {
    this._match2.set(value);
  }
  
  private Boolean getMatch2() {
    return this._match2.get();
  }
  
  private Boolean getAndSetMatch2(final Boolean value) {
    return this._match2.getAndSet(value);
  }
  
  private void setMatch3(final Boolean value) {
    this._match3.set(value);
  }
  
  private Boolean getMatch3() {
    return this._match3.get();
  }
  
  private Boolean getAndSetMatch3(final Boolean value) {
    return this._match3.getAndSet(value);
  }
  
  private void setMatch4(final Boolean value) {
    this._match4.set(value);
  }
  
  private Boolean getMatch4() {
    return this._match4.get();
  }
  
  private Boolean getAndSetMatch4(final Boolean value) {
    return this._match4.getAndSet(value);
  }
}
