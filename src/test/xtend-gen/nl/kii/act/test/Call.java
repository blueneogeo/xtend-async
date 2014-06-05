package nl.kii.act.test;

import nl.kii.act.test.Bounce;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

@SuppressWarnings("all")
public class Call<T extends Object> implements Bounce<T> {
  public final Function0<? extends Bounce<T>> thunk;
  
  public Call(final Function0<? extends Bounce<T>> thunk) {
    this.thunk = thunk;
  }
}
