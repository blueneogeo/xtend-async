package nl.kii.act.test;

import nl.kii.act.test.Bounce;
import nl.kii.act.test.Call;
import nl.kii.act.test.Done;
import nl.kii.act.test.Trampoline;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.junit.Test;

@SuppressWarnings("all")
public class TestTrampoline {
  @Test
  public void testTrampoline() {
    Bounce<Boolean> _even2 = this.even2(10000);
    Boolean _trampoline = Trampoline.<Boolean>trampoline(_even2);
    InputOutput.<Boolean>println(_trampoline);
  }
  
  public Bounce<Boolean> even2(final int n) {
    Bounce<Boolean> _xifexpression = null;
    if ((n == 0)) {
      _xifexpression = new Done<Boolean>(Boolean.valueOf(true));
    } else {
      final Function0<Bounce<Boolean>> _function = new Function0<Bounce<Boolean>>() {
        public Bounce<Boolean> apply() {
          return TestTrampoline.this.odd2((n - 1));
        }
      };
      _xifexpression = new Call<Boolean>(_function);
    }
    return _xifexpression;
  }
  
  public Bounce<Boolean> odd2(final int n) {
    Bounce<Boolean> _xifexpression = null;
    if ((n == 0)) {
      _xifexpression = new Done<Boolean>(Boolean.valueOf(false));
    } else {
      final Function0<Bounce<Boolean>> _function = new Function0<Bounce<Boolean>>() {
        public Bounce<Boolean> apply() {
          return TestTrampoline.this.even2((n - 1));
        }
      };
      _xifexpression = new Call<Boolean>(_function);
    }
    return _xifexpression;
  }
}
