package nl.kii.stream;

import java.io.Closeable;
import java.util.Set;
import nl.kii.stream.Stream;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

@SuppressWarnings("all")
public interface StreamBalancer<T extends Object> extends Closeable {
  public abstract void start();
  
  public abstract void stop();
  
  public abstract StreamBalancer<T> register(final Stream<T> stream);
  
  public abstract StreamBalancer<T> register(final Stream<T> stream, final Function1<? super T, ? extends Boolean> criterium);
  
  public abstract StreamBalancer<T> unregister(final Stream<T> stream);
  
  public abstract Set<Stream<T>> getStreams();
}
