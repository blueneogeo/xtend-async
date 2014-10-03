package nl.kii.stream;

import nl.kii.stream.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface StreamResponder {
  public abstract void next(final Procedure1<? super Void> handler);
  
  public abstract void skip(final Procedure1<? super Void> handler);
  
  public abstract void close(final Procedure1<? super Void> handler);
  
  public abstract void overflow(final Procedure1<? super Entry<?, ?>> handler);
}
