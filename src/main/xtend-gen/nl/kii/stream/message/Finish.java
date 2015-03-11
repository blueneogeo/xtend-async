package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that a batch of data has finished.
 * Batches of data can be of different levels. The finish has a level property that indicates
 * which level of data was finished.
 */
@SuppressWarnings("all")
public class Finish<I extends Object, O extends Object> implements Entry<I, O> {
  public final I from;
  
  public final int level;
  
  public Finish() {
    this(null, 0);
  }
  
  public Finish(final I from, final int level) {
    this.from = from;
    this.level = level;
  }
  
  @Override
  public String toString() {
    return (("finish(" + Integer.valueOf(this.level)) + ")");
  }
  
  @Override
  public boolean equals(final Object o) {
    boolean _and = false;
    if (!(o instanceof Finish<?, ?>)) {
      _and = false;
    } else {
      _and = (((Finish<?, ?>) o).level == this.level);
    }
    return _and;
  }
}
