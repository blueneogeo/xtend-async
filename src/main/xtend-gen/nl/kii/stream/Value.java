package nl.kii.stream;

import com.google.common.base.Objects;
import nl.kii.stream.Entry;

/**
 * Wraps a streamed data value of type T
 */
@SuppressWarnings("all")
public class Value<I extends Object, O extends Object> implements Entry<I, O> {
  public final I from;
  
  public final O value;
  
  public Value(final I from, final O value) {
    this.from = from;
    this.value = value;
  }
  
  public String toString() {
    return this.value.toString();
  }
  
  public boolean equals(final Object o) {
    boolean _and = false;
    if (!(o instanceof Value<?, ?>)) {
      _and = false;
    } else {
      boolean _equals = Objects.equal(((Value<?, ?>) o).value, this.value);
      _and = _equals;
    }
    return _and;
  }
}
