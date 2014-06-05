package nl.kii.act.test;

import nl.kii.act.test.Bounce;
import nl.kii.act.test.Call;
import nl.kii.act.test.Done;

@SuppressWarnings("all")
public class Trampoline {
  public static <T extends Object> T trampoline(final Bounce<T> it) {
    T _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Call) {
        _matched=true;
        Bounce<T> _apply = ((Call<T>)it).thunk.apply();
        _switchResult = Trampoline.<T>trampoline(_apply);
      }
    }
    if (!_matched) {
      if (it instanceof Done) {
        _matched=true;
        _switchResult = ((Done<T>)it).result;
      }
    }
    return _switchResult;
  }
}
