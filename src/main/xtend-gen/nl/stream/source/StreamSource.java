package nl.stream.source;

import nl.kii.stream.Stream;

/**
 * A source is a streamable source of information.
 */
@SuppressWarnings("all")
public interface StreamSource<T extends Object> {
  /**
   * Create a new stream and pipe source stream to this stream
   */
  public abstract Stream<T> stream();
  
  /**
   * Connect an existing stream as a listener to the source stream
   */
  public abstract StreamSource<T> pipe(final Stream<T> stream);
}
