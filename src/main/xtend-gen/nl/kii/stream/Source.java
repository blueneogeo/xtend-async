package nl.kii.stream;

import nl.kii.stream.Stream;

/**
 * A source is a streamable source of information.
 */
@SuppressWarnings("all")
public interface Source<T extends Object> {
  /**
   * Create a new stream and pipe source stream to this stream
   */
  public abstract Stream<T> stream();
  
  /**
   * Connect an existing stream as a listener to the source stream
   */
  public abstract Source<T> pipe(final Stream<T> stream);
}
