package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Wraps a streamed data value of type T
 */
public class Value<I extends java.lang.Object, O extends java.lang.Object> implements Entry<I, O> {
  public final I from;
  
  public final O value;
  
  public Value(final I from, final O value) {
    this.from = from;
    this.value = value;
  }
  
  public Object toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method toString is undefined for the type Value");
  }
  
  public Object equals(final /* Object */Object o) {
    throw new Error("Unresolved compilation problems:"
      + "\n&& cannot be resolved."
      + "\n== cannot be resolved");
  }
}
