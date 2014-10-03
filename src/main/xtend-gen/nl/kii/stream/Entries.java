package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.List;
import nl.kii.stream.Entry;
import nl.kii.stream.StreamMessage;
import org.eclipse.xtext.xbase.lib.Conversions;

/**
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
@SuppressWarnings("all")
public class Entries<R extends Object, T extends Object> implements StreamMessage {
  public final List<Entry<R, T>> entries;
  
  public Entries(final Entry<R, T>... entries) {
    this.entries = ((List<Entry<R, T>>)Conversions.doWrapArray(entries));
  }
  
  public String toString() {
    return this.entries.toString();
  }
  
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
