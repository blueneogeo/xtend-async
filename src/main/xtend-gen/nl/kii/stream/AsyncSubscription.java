package nl.kii.stream;

import nl.kii.stream.Stream;
import nl.kii.stream.Subscription;

@SuppressWarnings("all")
public class AsyncSubscription<T extends Object> extends Subscription<T> {
  public AsyncSubscription(final Stream<T> stream) {
    super(stream);
  }
  
  public void next() {
    this.stream.next();
  }
  
  public void skip() {
    this.stream.skip();
  }
}
