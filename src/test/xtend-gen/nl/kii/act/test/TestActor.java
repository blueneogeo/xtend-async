package nl.kii.act.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.act.ActorExtensions;
import nl.kii.async.ExecutorExtensions;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestActor {
  @Test
  public void testHelloWorld() {
    final Procedure2<String, Procedure0> _function = new Procedure2<String, Procedure0>() {
      public void apply(final String it, final Procedure0 done) {
        InputOutput.<String>println(("hello " + it));
        done.apply();
      }
    };
    final Actor<String> greeter = ActorExtensions.<String>actor(_function);
    ActorExtensions.<String>operator_doubleGreaterThan(
      "world", greeter);
    ActorExtensions.<String>operator_doubleGreaterThan(
      "Christian!", greeter);
    ActorExtensions.<String>operator_doubleGreaterThan(
      "time to go!", greeter);
  }
  
  @Test
  public void testAsyncCrosscallingActors() {
    try {
      final AtomicInteger doneCounter = new AtomicInteger(0);
      final AtomicReference<Actor<Integer>> decrease = new AtomicReference<Actor<Integer>>();
      final ExecutorService threads = Executors.newCachedThreadPool();
      final Procedure1<Integer> _function = new Procedure1<Integer>() {
        public void apply(final Integer y) {
          final Runnable _function = new Runnable() {
            public void run() {
              try {
                Thread.sleep(5);
                if ((y <= 0)) {
                  doneCounter.incrementAndGet();
                } else {
                  Actor<Integer> _get = decrease.get();
                  ActorExtensions.<Integer>operator_doubleGreaterThan(Integer.valueOf(y), _get);
                }
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          ExecutorExtensions.task(threads, _function);
        }
      };
      final Actor<Integer> checkDone = ActorExtensions.<Integer>actor(_function);
      final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
        public void apply(final Integer value) {
          final Runnable _function = new Runnable() {
            public void run() {
              ActorExtensions.<Integer>operator_doubleGreaterThan(Integer.valueOf((value - 1)), checkDone);
            }
          };
          ExecutorExtensions.task(threads, _function);
        }
      };
      Actor<Integer> _actor = ActorExtensions.<Integer>actor(_function_1);
      decrease.set(_actor);
      ActorExtensions.<Integer>operator_doubleLessThan(checkDone, Integer.valueOf(100));
      ActorExtensions.<Integer>operator_doubleLessThan(checkDone, Integer.valueOf(300));
      ActorExtensions.<Integer>operator_doubleLessThan(checkDone, Integer.valueOf(200));
      Thread.sleep(2000);
      int _get = doneCounter.get();
      Assert.assertEquals(3, _get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testActorLoad() {
    abstract class __TestActor_1 extends Actor<Integer> {
      int counter;
    }
    
    try {
      final __TestActor_1 actor = new __TestActor_1() {
        {
          counter = 0;
        }
        protected void act(final Integer message, final Procedure0 done) {
          int _counter = this.counter;
          this.counter = (_counter + (message).intValue());
          done.apply();
        }
      };
      final ExecutorService threads = Executors.newCachedThreadPool();
      IntegerRange _upTo = new IntegerRange(1, 10);
      final Function1<Integer, Task> _function = new Function1<Integer, Task>() {
        public Task apply(final Integer it) {
          final Runnable _function = new Runnable() {
            public void run() {
              IntegerRange _upTo = new IntegerRange(1, 100000);
              for (final Integer i : _upTo) {
                ActorExtensions.<Integer>operator_doubleLessThan(actor, Integer.valueOf(1));
              }
            }
          };
          return ExecutorExtensions.task(threads, _function);
        }
      };
      Iterable<Task> _map = IterableExtensions.<Integer, Task>map(_upTo, _function);
      Task _all = PromiseExtensions.all(_map);
      final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
        public void apply(final Boolean it) {
          InputOutput.<String>println("done");
        }
      };
      Promise<Boolean> _then = _all.then(_function_1);
      final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          String _message = it.getMessage();
          Assert.fail(_message);
        }
      };
      _then.onError(_function_2);
      Thread.sleep(500);
      Assert.assertEquals(1000000, actor.counter);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
