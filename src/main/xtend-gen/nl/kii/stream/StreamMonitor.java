package nl.kii.stream;

import nl.kii.observe.Observable;
import nl.kii.stream.Entry;
import nl.kii.stream.StreamStats;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class StreamMonitor implements Procedure1<Entry<?, ?>>, Observable<StreamStats> {
  public void apply(final Entry<?, ?> p) {
    throw new UnsupportedOperationException("TODO: auto-generated method stub");
  }
  
  public Procedure0 onChange(final Procedure1<? super StreamStats> observeFn) {
    return null;
  }
}
