package nl.kii.act.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.act.ActorExtensions;
import nl.kii.stream.PromiseExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
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
          PromiseExtensions.run(threads, _function);
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
          PromiseExtensions.run(threads, _function);
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
    try {
      final ExecutorService threads = Executors.newCachedThreadPool();
      final AtomicInteger counter = new AtomicInteger(0);
      final AtomicReference<Actor<Integer>> ref = new AtomicReference<Actor<Integer>>();
      final Procedure2<Integer, Procedure0> _function = new Procedure2<Integer, Procedure0>() {
        public void apply(final Integer i, final Procedure0 done) {
          final Runnable _function = new Runnable() {
            public void run() {
              try {
                Thread.sleep(1);
                counter.incrementAndGet();
                done.apply();
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          PromiseExtensions.run(threads, _function);
        }
      };
      final Actor<Integer> a = ActorExtensions.<Integer>actor(_function);
      ref.set(a);
      IntegerRange _upTo = new IntegerRange(1, 100000);
      for (final Integer i : _upTo) {
        ActorExtensions.<Integer>operator_doubleLessThan(a, i);
      }
      Thread.sleep(2000);
      int _get = counter.get();
      Assert.assertEquals(100000, _get);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
