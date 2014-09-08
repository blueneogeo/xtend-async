package nl.kii.stream;

import nl.kii.stream.StreamNotification;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface StreamNotificationObserver extends Procedure1<StreamNotification> {
  public abstract void next(final Procedure1<? super Void> handler);
  
  public abstract void skip(final Procedure1<? super Void> handler);
  
  public abstract void close(final Procedure1<? super Void> handler);
}
