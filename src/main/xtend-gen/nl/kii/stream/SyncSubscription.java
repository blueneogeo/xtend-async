package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.Next;
import nl.kii.stream.Stream;
import nl.kii.stream.Subscription;

@SuppressWarnings("all")
public class SyncSubscription<T extends Object> extends Subscription<T> {
  public SyncSubscription(final Stream<T> stream) {
    super(stream);
  }
  
  public void apply(final Entry<T> it) {
    try {
      super.apply(it);
    } finally {
      Next _next = new Next();
      this.stream.perform(_next);
    }
  }
}
