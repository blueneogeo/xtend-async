package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that a batch of data has finished.
 * Batches of data can be of different levels. The finish has a level property that indicates
 * which level of data was finished.
 */
public class Finish<I extends java.lang.Object, O extends java.lang.Object> implements Entry<I, O> {
  public final I from;
  
  public final int level;
  
  public Finish() {
    this(null, 0);
  }
  
  public Finish(final I from, final int level) {
    this.from = from;
    this.level = level;
  }
  
  public Object toString() {
    throw new Error("Unresolved compilation problems:"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  public Object equals(final /* Object */Object o) {
    throw new Error("Unresolved compilation problems:"
      + "\n&& cannot be resolved."
      + "\n== cannot be resolved.");
  }
}
