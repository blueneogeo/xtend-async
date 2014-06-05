package nl.kii.act.test;

import nl.kii.act.test.Bounce;

@SuppressWarnings("all")
public class Done<T extends Object> implements Bounce<T> {
  public final T result;
  
  public Done(final T result) {
    this.result = result;
  }
}
