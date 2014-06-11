package nl.kii.stream;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nl.kii.stream.CommandSubscription;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamBalancer;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SyncSubscription;
import nl.kii.util.SynchronizeExt;
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
    ControlledBalancer<T> _xblockexpression = null;
    {
      this.streams.put(stream, criterium);
      this.ready.put(stream, Boolean.valueOf(false));
      final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
        public void apply(final CommandSubscription it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              ControlledBalancer.this.ready.put(stream, Boolean.valueOf(true));
            }
          };
          it.onNext(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
            }
          };
          it.onSkip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              ControlledBalancer.this.unregister(stream);
            }
          };
          it.onClose(_function_2);
        }
      };
      StreamExtensions.<T>monitor(stream, _function);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public StreamBalancer<T> unregister(final Stream<T> stream) {
    ControlledBalancer<T> _xblockexpression = null;
    {
      this.streams.remove(stream);
      this.ready.remove(stream);
      final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
        public void apply(final CommandSubscription it) {
        }
      };
      StreamExtensions.<T>monitor(stream, _function);
      _xblockexpression = this;
    }
    return _xblockexpression;
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
    final Procedure1<SyncSubscription<T>> _function = new Procedure1<SyncSubscription<T>>() {
      public void apply(final SyncSubscription<T> it) {
        final Procedure1<T> _function = new Procedure1<T>() {
          public void apply(final T it) {
            final Procedure1<T> _function = new Procedure1<T>() {
              public void apply(final T it) {
                Set<Stream<T>> _keySet = ControlledBalancer.this.ready.keySet();
                for (final Stream<T> stream : _keySet) {
                  Boolean _get = ControlledBalancer.this.ready.get(stream);
                  if ((_get).booleanValue()) {
                    stream.push(it);
                    ControlledBalancer.this.ready.put(stream, Boolean.valueOf(false));
                  }
                }
              }
            };
            SynchronizeExt.<T>synchronize(it, _function);
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<T>on(this.source, _function);
  }
  
  public void stop() {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
}
