package nl.kii.stream.test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubTask;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.internal.StreamResponder;
import nl.kii.stream.internal.SubStream;
import nl.kii.stream.message.Finish;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamErrorHandling {
  @Atomic
  private final AtomicInteger _result = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _counter = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _errors = new AtomicInteger();
  
  @Atomic
  private final AtomicBoolean _complete = new AtomicBoolean();
  
  @Atomic
  private final AtomicBoolean _failed = new AtomicBoolean();
  
  @Test
  public void canMonitorErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Procedure1<Throwable> _function_1 = (Throwable it) -> {
      Integer _errors = this.getErrors();
      int _plus = ((_errors).intValue() + 1);
      this.setErrors(Integer.valueOf(_plus));
    };
    SubStream<Integer, Integer> _on = StreamExtensions.<Integer, Integer>on(_map, Throwable.class, _function_1);
    final Procedure1<Integer> _function_2 = (Integer it) -> {
      Assert.fail("an error should occur");
    };
    StreamExtensions.<Integer, Integer>onEach(_on, _function_2);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Integer _errors = this.getErrors();
    Assert.assertEquals(3, (_errors).intValue());
  }
  
  @Test
  public void canMatchErrorTypes() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Procedure1<Throwable> _function_1 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    SubStream<Integer, Integer> _on = StreamExtensions.<Integer, Integer>on(_map, IllegalArgumentException.class, _function_1);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      Integer _errors = this.getErrors();
      int _plus = ((_errors).intValue() + 1);
      this.setErrors(Integer.valueOf(_plus));
    };
    SubStream<Integer, Integer> _on_1 = StreamExtensions.<Integer, Integer>on(_on, ArithmeticException.class, _function_2);
    final Procedure1<Integer> _function_3 = (Integer it) -> {
      Assert.fail("an error should occur");
    };
    StreamExtensions.<Integer, Integer>onEach(_on_1, _function_3);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Boolean _failed = this.getFailed();
    Assert.assertEquals(Boolean.valueOf(false), _failed);
    Integer _errors = this.getErrors();
    Assert.assertEquals(3, (_errors).intValue());
  }
  
  @Test
  public void canSwallowErrorTypes() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Procedure1<Throwable> _function_1 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    SubStream<Integer, Integer> _on = StreamExtensions.<Integer, Integer>on(_map, IllegalArgumentException.class, _function_1);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      Integer _errors = this.getErrors();
      int _plus = ((_errors).intValue() + 1);
      this.setErrors(Integer.valueOf(_plus));
    };
    SubStream<Integer, Integer> _on_1 = StreamExtensions.<Integer, Integer>on(_on, ArithmeticException.class, _function_2);
    final Procedure1<Throwable> _function_3 = (Throwable it) -> {
      Integer _errors = this.getErrors();
      int _plus = ((_errors).intValue() + 1);
      this.setErrors(Integer.valueOf(_plus));
    };
    SubStream<Integer, Integer> _effect = StreamExtensions.<Integer, Integer>effect(_on_1, Exception.class, _function_3);
    final Procedure1<Throwable> _function_4 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    SubStream<Integer, Integer> _on_2 = StreamExtensions.<Integer, Integer>on(_effect, Throwable.class, _function_4);
    final Procedure1<Integer> _function_5 = (Integer it) -> {
      Assert.fail("an error should occur");
    };
    StreamExtensions.<Integer, Integer>onEach(_on_2, _function_5);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Boolean _failed = this.getFailed();
    Assert.assertEquals(Boolean.valueOf(false), _failed);
    Integer _errors = this.getErrors();
    Assert.assertEquals(6, (_errors).intValue());
  }
  
  @Test
  public void canMapErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Procedure1<Throwable> _function_1 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    SubStream<Integer, Integer> _on = StreamExtensions.<Integer, Integer>on(_map, IllegalArgumentException.class, _function_1);
    final Function1<Throwable, Integer> _function_2 = (Throwable it) -> {
      return Integer.valueOf(10);
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_on, ArithmeticException.class, _function_2);
    final Procedure1<Throwable> _function_3 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    SubStream<Integer, Integer> _on_1 = StreamExtensions.<Integer, Integer>on(_map_1, Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = (Integer it) -> {
      Integer _result = this.getResult();
      int _plus = ((_result).intValue() + (it).intValue());
      this.setResult(Integer.valueOf(_plus));
    };
    StreamExtensions.<Integer, Integer>onEach(_on_1, _function_4);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Boolean _failed = this.getFailed();
    Assert.assertEquals(Boolean.valueOf(false), _failed);
    Integer _result = this.getResult();
    Assert.assertEquals(30, (_result).intValue());
  }
  
  @Test
  public void canFilterMapErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Throwable, Integer> _function_1 = (Throwable it) -> {
      return Integer.valueOf(10);
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer>map(_map, IllegalArgumentException.class, _function_1);
    final Function1<Throwable, Integer> _function_2 = (Throwable it) -> {
      return Integer.valueOf(20);
    };
    SubStream<Integer, Integer> _map_2 = StreamExtensions.<Integer, Integer>map(_map_1, ArithmeticException.class, _function_2);
    final Function1<Throwable, Integer> _function_3 = (Throwable it) -> {
      return Integer.valueOf(30);
    };
    SubStream<Integer, Integer> _map_3 = StreamExtensions.<Integer, Integer>map(_map_2, Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = (Integer it) -> {
      Integer _result = this.getResult();
      int _plus = ((_result).intValue() + (it).intValue());
      this.setResult(Integer.valueOf(_plus));
    };
    StreamExtensions.<Integer, Integer>onEach(_map_3, _function_4);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Boolean _failed = this.getFailed();
    Assert.assertEquals(Boolean.valueOf(false), _failed);
    Integer _result = this.getResult();
    Assert.assertEquals(60, (_result).intValue());
  }
  
  @Test
  public void canFilterAsyncMapErrors() {
    final Stream<Integer> s = new Stream<Integer>();
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() / 0));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Throwable, IPromise<?, Integer>> _function_1 = (Throwable it) -> {
      return PromiseExtensions.<Integer>promise(Integer.valueOf(10));
    };
    SubStream<Integer, Integer> _call = StreamExtensions.<Integer, Integer>call(_map, IllegalArgumentException.class, _function_1);
    final Function1<Throwable, IPromise<?, Integer>> _function_2 = (Throwable it) -> {
      return PromiseExtensions.<Integer>promise(Integer.valueOf(20));
    };
    SubStream<Integer, Integer> _call_1 = StreamExtensions.<Integer, Integer>call(_call, ArithmeticException.class, _function_2);
    final Function1<Throwable, IPromise<?, Integer>> _function_3 = (Throwable it) -> {
      return PromiseExtensions.<Integer>promise(Integer.valueOf(30));
    };
    SubStream<Integer, Integer> _call_2 = StreamExtensions.<Integer, Integer>call(_call_1, Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = (Integer it) -> {
      Integer _result = this.getResult();
      int _plus = ((_result).intValue() + (it).intValue());
      this.setResult(Integer.valueOf(_plus));
    };
    StreamExtensions.<Integer, Integer>onEach(_call_2, _function_4);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    Boolean _failed = this.getFailed();
    Assert.assertEquals(Boolean.valueOf(false), _failed);
    Integer _result = this.getResult();
    Assert.assertEquals(60, (_result).intValue());
  }
  
  @Test
  public void testStreamsSwallowExceptions() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return it;
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = (Integer it) -> {
      try {
        Integer _xblockexpression = null;
        {
          if ((((it).intValue() == 2) || ((it).intValue() == 4))) {
            throw new Exception();
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Function1<Integer, Integer> _function_2 = (Integer it) -> {
      return it;
    };
    SubStream<Integer, Integer> _map_2 = StreamExtensions.<Integer, Integer, Integer>map(_map_1, _function_2);
    final Procedure1<Integer> _function_3 = (Integer it) -> {
      this.incCounter();
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(_map_2, _function_3);
    final Procedure1<Boolean> _function_4 = (Boolean it) -> {
      this.setComplete(Boolean.valueOf(true));
    };
    _onEach.then(_function_4);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    StreamExtensions.<Object, Object>finish();
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
    Boolean _complete = this.getComplete();
    Assert.assertFalse((_complete).booleanValue());
  }
  
  @Test
  public void testStreamsErrorHandlersSwallowExceptions() {
    final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return it;
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = (Integer it) -> {
      try {
        Integer _xblockexpression = null;
        {
          if ((((it).intValue() == 2) || ((it).intValue() == 4))) {
            throw new Exception();
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      this.incErrors();
    };
    SubStream<Integer, Integer> _effect = StreamExtensions.<Integer, Integer>effect(_map_1, Exception.class, _function_2);
    final Function1<Integer, Integer> _function_3 = (Integer it) -> {
      return it;
    };
    SubStream<Integer, Integer> _map_2 = StreamExtensions.<Integer, Integer, Integer>map(_effect, _function_3);
    final Procedure1<Integer> _function_4 = (Integer it) -> {
      this.incCounter();
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(_map_2, _function_4);
    final Procedure1<Boolean> _function_5 = (Boolean it) -> {
      this.setComplete(Boolean.valueOf(true));
    };
    Task _then = _onEach.then(_function_5);
    final Procedure1<Throwable> _function_6 = (Throwable it) -> {
      this.setFailed(Boolean.valueOf(true));
    };
    PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_6);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
    StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, _finish);
    Integer _errors = this.getErrors();
    Assert.assertEquals(2, (_errors).intValue());
    Integer _counter = this.getCounter();
    Assert.assertEquals(3, (_counter).intValue());
    Boolean _complete = this.getComplete();
    Assert.assertTrue((_complete).booleanValue());
    Boolean _failed = this.getFailed();
    Assert.assertFalse((_failed).booleanValue());
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream() {
    IntegerRange _upTo = new IntegerRange(1, 20);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = (Integer it) -> {
      return Boolean.valueOf((((it).intValue() % 4) == 0));
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(_stream, _function);
    final Function1<Integer, Integer> _function_1 = (Integer it) -> {
      try {
        Integer _xblockexpression = null;
        {
          if ((((it).intValue() % 6) == 0)) {
            throw new Exception();
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_split, _function_1);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      InputOutput.<String>println(("error " + it));
    };
    SubStream<Integer, List<Integer>> _on = StreamExtensions.<Integer, List<Integer>>on(_collect, Exception.class, _function_2);
    final Procedure1<List<Integer>> _function_3 = (List<Integer> it) -> {
      InputOutput.<String>println(("result : " + it));
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_on, _function_3);
    final Procedure1<Boolean> _function_4 = (Boolean it) -> {
      InputOutput.<String>println("done");
    };
    Task _then = _onEach.then(_function_4);
    final Procedure1<Throwable> _function_5 = (Throwable it) -> {
      String _message = it.getMessage();
      Assert.fail(_message);
    };
    PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_5);
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream2() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, Integer.valueOf(5));
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(6));
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(8));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(9));
    IStream<Integer, Integer> _doubleLessThan_9 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, Integer.valueOf(10));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_10 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_9, _finish);
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish(1);
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_10, _finish_1);
    final Function1<Integer, Boolean> _function = (Integer it) -> {
      return Boolean.valueOf((((it).intValue() % 4) == 0));
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(s, _function);
    final Function1<Integer, Integer> _function_1 = (Integer it) -> {
      try {
        Integer _xblockexpression = null;
        {
          if ((((it).intValue() % 6) == 0)) {
            throw new Exception();
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_split, _function_1);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      InputOutput.<String>println(("error " + it));
    };
    SubStream<Integer, List<Integer>> _on = StreamExtensions.<Integer, List<Integer>>on(_collect, Exception.class, _function_2);
    final Procedure1<List<Integer>> _function_3 = (List<Integer> it) -> {
      InputOutput.<String>println(("result : " + it));
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_on, _function_3);
    final Procedure1<Boolean> _function_4 = (Boolean it) -> {
      InputOutput.<String>println("done");
    };
    Task _then = _onEach.then(_function_4);
    final Procedure1<Throwable> _function_5 = (Throwable it) -> {
      String _message = it.getMessage();
      Assert.fail(_message);
    };
    PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_5);
  }
  
  @Test
  public void testStreamAggregationsShouldFailOnInternalErrorsButNotBreakTheStream3() {
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(int.class);
    IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_stream, Integer.valueOf(1));
    IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
    IStream<Integer, Integer> _doubleLessThan_2 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, Integer.valueOf(3));
    IStream<Integer, Integer> _doubleLessThan_3 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_2, Integer.valueOf(4));
    Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_4 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_3, _finish);
    IStream<Integer, Integer> _doubleLessThan_5 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_4, Integer.valueOf(5));
    IStream<Integer, Integer> _doubleLessThan_6 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_5, Integer.valueOf(6));
    IStream<Integer, Integer> _doubleLessThan_7 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_6, Integer.valueOf(7));
    IStream<Integer, Integer> _doubleLessThan_8 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_7, Integer.valueOf(8));
    Finish<Integer, Integer> _finish_1 = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_9 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_8, _finish_1);
    IStream<Integer, Integer> _doubleLessThan_10 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_9, Integer.valueOf(9));
    IStream<Integer, Integer> _doubleLessThan_11 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_10, Integer.valueOf(10));
    Finish<Integer, Integer> _finish_2 = StreamExtensions.<Integer, Integer>finish(0);
    IStream<Integer, Integer> _doubleLessThan_12 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_11, _finish_2);
    Finish<Integer, Integer> _finish_3 = StreamExtensions.<Integer, Integer>finish(1);
    final IStream<Integer, Integer> s = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_12, _finish_3);
    final Function1<Integer, Integer> _function = (Integer it) -> {
      try {
        Integer _xblockexpression = null;
        {
          if ((((it).intValue() % 6) == 0)) {
            throw new Exception();
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    SubStream<Integer, List<Integer>> _collect = StreamExtensions.<Integer, Integer>collect(_map);
    final Procedure1<Throwable> _function_1 = (Throwable it) -> {
      InputOutput.<String>println(("error " + it));
    };
    SubStream<Integer, List<Integer>> _on = StreamExtensions.<Integer, List<Integer>>on(_collect, Exception.class, _function_1);
    final Procedure1<List<Integer>> _function_2 = (List<Integer> it) -> {
      InputOutput.<String>println(("result : " + it));
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, List<Integer>>onEach(_on, _function_2);
    final Procedure1<Boolean> _function_3 = (Boolean it) -> {
      InputOutput.<String>println("done");
    };
    Task _then = _onEach.then(_function_3);
    final Procedure1<Throwable> _function_4 = (Throwable it) -> {
      InputOutput.<String>println("error");
      String _message = it.getMessage();
      Assert.fail(_message);
    };
    PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_4);
  }
  
  @Test
  public void testSplit() {
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Boolean> _function = (Integer it) -> {
      return Boolean.valueOf((((it).intValue() % 4) == 0));
    };
    SubStream<Integer, Integer> _split = StreamExtensions.<Integer, Integer>split(_stream, _function);
    final Procedure1<StreamResponder<Integer, Integer>> _function_1 = (StreamResponder<Integer, Integer> it) -> {
      final Procedure2<Integer, Integer> _function_2 = (Integer $0, Integer $1) -> {
        InputOutput.<Integer>println($1);
        IStream<Integer, Integer> _stream_1 = it.getStream();
        _stream_1.next();
      };
      it.each(_function_2);
      final Procedure2<Integer, Integer> _function_3 = (Integer $0, Integer $1) -> {
        InputOutput.<String>println(("finish " + $1));
        IStream<Integer, Integer> _stream_1 = it.getStream();
        _stream_1.next();
      };
      it.finish(_function_3);
      IStream<Integer, Integer> _stream_1 = it.getStream();
      _stream_1.next();
    };
    StreamExtensions.<Integer, Integer>on(_split, _function_1);
  }
  
  @Atomic
  private final AtomicReference<Throwable> _caught = new AtomicReference<Throwable>();
  
  @Test
  public void testHandlingBelowErrorShouldFilterTheException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = (Integer it) -> {
        return it;
      };
      SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = (Integer it) -> {
        return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
      };
      SubStream<Integer, Integer> _filter = StreamExtensions.<Integer, Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = (Integer it) -> {
        return it;
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_filter, _function_2);
      final Procedure1<Throwable> _function_3 = (Throwable it) -> {
        this.setCaught(it);
      };
      SubStream<Integer, Integer> _on = StreamExtensions.<Integer, Integer>on(_map_1, Exception.class, _function_3);
      final Procedure1<Integer> _function_4 = (Integer it) -> {
      };
      StreamExtensions.<Integer, Integer>onEach(_on, _function_4);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
      StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Assert.fail("error should be handled");
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    Throwable _caught = this.getCaught();
    Assert.assertNotNull(_caught);
  }
  
  @Test
  public void testHandlingAtTaskShouldFilterTheException() {
    try {
      final Stream<Integer> s = StreamExtensions.<Integer>stream(int.class);
      final Function1<Integer, Integer> _function = (Integer it) -> {
        return it;
      };
      SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
      final Function1<Integer, Boolean> _function_1 = (Integer it) -> {
        return Boolean.valueOf(((1 / ((it).intValue() % 2)) == 0));
      };
      SubStream<Integer, Integer> _filter = StreamExtensions.<Integer, Integer>filter(_map, _function_1);
      final Function1<Integer, Integer> _function_2 = (Integer it) -> {
        return it;
      };
      SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_filter, _function_2);
      final Procedure1<Integer> _function_3 = (Integer it) -> {
      };
      SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(_map_1, _function_3);
      final Procedure1<Throwable> _function_4 = (Throwable it) -> {
        this.setCaught(it);
      };
      PromiseExtensions.<Integer, Boolean>on(_onEach, Throwable.class, _function_4);
      IStream<Integer, Integer> _doubleLessThan = StreamExtensions.<Integer, Integer>operator_doubleLessThan(s, Integer.valueOf(1));
      IStream<Integer, Integer> _doubleLessThan_1 = StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan, Integer.valueOf(2));
      Finish<Integer, Integer> _finish = StreamExtensions.<Integer, Integer>finish();
      StreamExtensions.<Integer, Integer>operator_doubleLessThan(_doubleLessThan_1, _finish);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Assert.fail("error should be handled");
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    Throwable _caught = this.getCaught();
    Assert.assertNotNull(_caught);
  }
  
  @Atomic
  private final AtomicBoolean _finished = new AtomicBoolean();
  
  @Atomic
  private final AtomicInteger _errorCount = new AtomicInteger();
  
  @Atomic
  private final AtomicInteger _count = new AtomicInteger();
  
  @Test
  public void testErrorHandlingBeforeCollect() {
    this.setFinished(Boolean.valueOf(false));
    this.setErrorCount(Integer.valueOf(0));
    this.setCount(Integer.valueOf(0));
    IntegerRange _upTo = new IntegerRange(1, 10);
    final Stream<Integer> s = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = (Integer it) -> {
      return Integer.valueOf(((it).intValue() % 3));
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(s, _function);
    final Function1<Integer, Integer> _function_1 = (Integer it) -> {
      return Integer.valueOf((100 / (it).intValue()));
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Procedure1<Throwable> _function_2 = (Throwable it) -> {
      this.incErrorCount();
    };
    SubStream<Integer, Integer> _effect = StreamExtensions.<Integer, Integer>effect(_map_1, Exception.class, _function_2);
    final Procedure1<Integer> _function_3 = (Integer it) -> {
      this.incCount();
    };
    SubTask<Integer> _onEach = StreamExtensions.<Integer, Integer>onEach(_effect, _function_3);
    final Procedure1<Boolean> _function_4 = (Boolean it) -> {
      this.setFinished(Boolean.valueOf(true));
    };
    _onEach.then(_function_4);
    Integer _count = this.getCount();
    Assert.assertEquals(7, (_count).intValue());
    Integer _errorCount = this.getErrorCount();
    Assert.assertEquals(3, (_errorCount).intValue());
    Boolean _finished = this.getFinished();
    Assert.assertTrue((_finished).booleanValue());
  }
  
  private void setResult(final Integer value) {
    this._result.set(value);
  }
  
  private Integer getResult() {
    return this._result.get();
  }
  
  private Integer getAndSetResult(final Integer value) {
    return this._result.getAndSet(value);
  }
  
  private Integer incResult() {
    return this._result.incrementAndGet();
  }
  
  private Integer decResult() {
    return this._result.decrementAndGet();
  }
  
  private Integer incResult(final Integer value) {
    return this._result.addAndGet(value);
  }
  
  private void setCounter(final Integer value) {
    this._counter.set(value);
  }
  
  private Integer getCounter() {
    return this._counter.get();
  }
  
  private Integer getAndSetCounter(final Integer value) {
    return this._counter.getAndSet(value);
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
  
  private void setErrors(final Integer value) {
    this._errors.set(value);
  }
  
  private Integer getErrors() {
    return this._errors.get();
  }
  
  private Integer getAndSetErrors(final Integer value) {
    return this._errors.getAndSet(value);
  }
  
  private Integer incErrors() {
    return this._errors.incrementAndGet();
  }
  
  private Integer decErrors() {
    return this._errors.decrementAndGet();
  }
  
  private Integer incErrors(final Integer value) {
    return this._errors.addAndGet(value);
  }
  
  private void setComplete(final Boolean value) {
    this._complete.set(value);
  }
  
  private Boolean getComplete() {
    return this._complete.get();
  }
  
  private Boolean getAndSetComplete(final Boolean value) {
    return this._complete.getAndSet(value);
  }
  
  private void setFailed(final Boolean value) {
    this._failed.set(value);
  }
  
  private Boolean getFailed() {
    return this._failed.get();
  }
  
  private Boolean getAndSetFailed(final Boolean value) {
    return this._failed.getAndSet(value);
  }
  
  private void setCaught(final Throwable value) {
    this._caught.set(value);
  }
  
  private Throwable getCaught() {
    return this._caught.get();
  }
  
  private Throwable getAndSetCaught(final Throwable value) {
    return this._caught.getAndSet(value);
  }
  
  private void setFinished(final Boolean value) {
    this._finished.set(value);
  }
  
  private Boolean getFinished() {
    return this._finished.get();
  }
  
  private Boolean getAndSetFinished(final Boolean value) {
    return this._finished.getAndSet(value);
  }
  
  private void setErrorCount(final Integer value) {
    this._errorCount.set(value);
  }
  
  private Integer getErrorCount() {
    return this._errorCount.get();
  }
  
  private Integer getAndSetErrorCount(final Integer value) {
    return this._errorCount.getAndSet(value);
  }
  
  private Integer incErrorCount() {
    return this._errorCount.incrementAndGet();
  }
  
  private Integer decErrorCount() {
    return this._errorCount.decrementAndGet();
  }
  
  private Integer incErrorCount(final Integer value) {
    return this._errorCount.addAndGet(value);
  }
  
  private void setCount(final Integer value) {
    this._count.set(value);
  }
  
  private Integer getCount() {
    return this._count.get();
  }
  
  private Integer getAndSetCount(final Integer value) {
    return this._count.getAndSet(value);
  }
  
  private Integer incCount() {
    return this._count.incrementAndGet();
  }
  
  private Integer decCount() {
    return this._count.decrementAndGet();
  }
  
  private Integer incCount(final Integer value) {
    return this._count.addAndGet(value);
  }
}
