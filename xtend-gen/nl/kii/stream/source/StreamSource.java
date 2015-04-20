package nl.kii.stream.source;

import nl.kii.stream.IStream;

/**
 * A source is a streamable source of information.
 */
public interface StreamSource<I extends java.lang.Object, O extends java.lang.Object> {
  /**
   * Create a new stream and pipe source stream to this stream
   */
  public abstract IStream<I, O> stream();
  
  /**
   * Connect an existing stream as a listener to the source stream
   */
  public abstract StreamSource<I, O> pipe(final /* IStream<I, ? extends  */Object stream);
}
