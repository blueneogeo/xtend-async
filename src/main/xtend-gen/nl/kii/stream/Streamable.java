package nl.kii.stream;

import nl.kii.stream.Stream;

@SuppressWarnings("all")
public interface Streamable<T extends Object> {
  public abstract Stream<T> stream();
}
