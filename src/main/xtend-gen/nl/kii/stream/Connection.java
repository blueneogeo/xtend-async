package nl.kii.stream;

import nl.kii.stream.Stream;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class Connection<T extends Object> implements Procedure1<T> {
  protected Stream<T> stream;
  
  protected Procedure0 openFn;
  
  protected Procedure0 nextFn;
  
  protected Procedure0 skipFn;
  
  protected Procedure0 closeFn;
  
  public void open() {
    this.openFn.apply();
  }
  
  public void next() {
    this.nextFn.apply();
  }
  
  public void skip() {
    this.skipFn.apply();
  }
  
  public void close() {
    this.closeFn.apply();
  }
  
  public abstract void apply(final T entry);
}
