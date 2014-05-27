package nl.kii.stream;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamBalancer;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class ControlledBalancer<T extends Object> implements StreamBalancer<T> {
  private final Stream<T> source;
  
  private final Map<Stream<T>, Function1<? super T, ? extends Boolean>> streams = new ConcurrentHashMap<Stream<T>, Function1<? super T, ? extends Boolean>>();
  
  private final Map<Stream<T>, Boolean> ready = new ConcurrentHashMap<Stream<T>, Boolean>();
  
  public ControlledBalancer(final Stream<T> source) {
    this.source = source;
  }
  
  public StreamBalancer<T> register(final Stream<T> stream) {
    final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
      public Boolean apply(final T it) {
        return Boolean.valueOf(true);
      }
    };
    return this.register(stream, _function);
  }
  
  public StreamBalancer<T> register(final Stream<T> stream, final Function1<? super T, ? extends Boolean> criterium) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onClose is undefined for the type ControlledBalancer"
      + "\nThe method onReadyForNext is undefined for the type ControlledBalancer"
      + "\nThe method onSkip is undefined for the type ControlledBalancer");
  }
  
  public StreamBalancer<T> unregister(final Stream<T> stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onClose is undefined for the type ControlledBalancer"
      + "\nThe method onReadyForNext is undefined for the type ControlledBalancer");
  }
  
  public void close() throws IOException {
    Set<Stream<T>> _keySet = this.streams.keySet();
    final Procedure1<Stream<T>> _function = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        ControlledBalancer.this.unregister(it);
      }
    };
    IterableExtensions.<Stream<T>>forEach(_keySet, _function);
  }
  
  public Set<Stream<T>> getStreams() {
    return this.streams.keySet();
  }
  
  public void start() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onNextValue is undefined for the type ControlledBalancer"
      + "\nThe method push is undefined for the type ControlledBalancer"
      + "\nThere is no context to infer the closure\'s argument types from. Consider typing the arguments or put the closures into a typed context."
      + "\nVoid functions cannot return a value.");
  }
  
  public void stop() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
}
