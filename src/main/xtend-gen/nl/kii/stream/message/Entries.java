package nl.kii.stream.message;

import com.google.common.base.Objects;
import java.util.List;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamMessage;
import org.eclipse.xtext.xbase.lib.Conversions;

/**
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
@SuppressWarnings("all")
public class Entries<I extends Object, O extends Object> implements StreamMessage {
  public final List<Entry<I, O>> entries;
  
  public Entries(final Entry<I, O>... entries) {
    this.entries = ((List<Entry<I, O>>)Conversions.doWrapArray(entries));
  }
  
  @Override
  public String toString() {
    return this.entries.toString();
  }
  
  @Override
  public boolean equals(final Object o) {
    boolean _and = false;
    if (!(o instanceof Entries<?, ?>)) {
      _and = false;
    } else {
      boolean _equals = Objects.equal(((Entries<?, ?>) o).entries, this.entries);
      _and = _equals;
    }
    return _and;
  }
}
