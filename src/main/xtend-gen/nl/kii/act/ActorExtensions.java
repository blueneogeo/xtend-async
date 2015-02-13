package nl.kii.act;

import nl.kii.act.Actor;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class ActorExtensions {
  public static <T extends Object> Actor<T> actor(final Procedure2<? super T, ? super Procedure0> actFn) {
    return new Actor<T>() {
      @Override
      public void act(final T input, final Procedure0 done) {
        actFn.apply(input, done);
      }
    };
  }
  
  public static <T extends Object> Actor<T> actor(final Procedure1<? super T> actFn) {
    return new Actor<T>() {
      @Override
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
