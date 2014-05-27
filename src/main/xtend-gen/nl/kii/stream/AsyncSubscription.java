package nl.kii.stream;

import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.Subscription;

@SuppressWarnings("all")
public class AsyncSubscription<T extends Object> extends Subscription<T> {
  public AsyncSubscription(final Stream<T> stream) {
    super(stream);
  }
  
  public void next() {
    Next _next = new Next();
    this.stream.perform(_next);
  }
  
  public void skip() {
    Skip _skip = new Skip();
    this.stream.perform(_skip);
  }
}
