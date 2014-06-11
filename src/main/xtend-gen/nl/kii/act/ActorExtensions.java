package nl.kii.act;

import com.google.common.base.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.kii.act.Actor;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class ActorExtensions {
  private static ExecutorService defaultActorExecutor = null;
  
  public static synchronized ExecutorService getDefaultExecutorService() {
    ExecutorService _xblockexpression = null;
    {
      boolean _equals = Objects.equal(ActorExtensions.defaultActorExecutor, null);
      if (_equals) {
        ExecutorService _newCachedThreadPool = Executors.newCachedThreadPool();
        ActorExtensions.defaultActorExecutor = _newCachedThreadPool;
      }
      _xblockexpression = ActorExtensions.defaultActorExecutor;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Actor<T> actor(final Procedure2<? super T, ? super Procedure0> actFn) {
    return new Actor<T>() {
      public void act(final T input, final Procedure0 done) {
        actFn.apply(input, done);
      }
    };
  }
  
  public static <T extends Object> Actor<T> actor(final Procedure1<? super T> actFn) {
    return new Actor<T>() {
      public void act(final T input, final Procedure0 done) {
        actFn.apply(input);
        done.apply();
      }
    };
  }
  
  public static <T extends Object> void operator_doubleGreaterThan(final T value, final Actor<T> actor) {
    actor.apply(value);
  }
  
  public static <T extends Object> void operator_doubleLessThan(final Actor<T> actor, final T value) {
    actor.apply(value);
  }
}
