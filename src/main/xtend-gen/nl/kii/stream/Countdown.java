package nl.kii.stream;

import nl.kii.stream.Gatherer;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Countdown is a specialized version of Collector that collects
 * if various functions have been executed successfully. You can
 * call await with a name like a collector or without a name, in
 * which case the name will be a number. The result function takes
 * a true/false based on whether the async code was successful.
 * <p>
 * You can ask if all functions were successful by calling isSuccess
 * <p>
 * Example code:
 * <p>
 * <pre>
 * val c = new Countdown
 * // create waiting functions
 * val signal1 = c.await
 * val signal2 = c.await
 * // finish when both async waiting functions complete
 * c.listen.onFinish [ println('both code functions ran. success: ' + c.success) ]
 * // sometime later, the signal functions are applied
 * [| ... some code .... signal1.apply(true) .... ].scheduleLater
 * [|... some other async code... signal2.apply(true) ...].scheduleLater
 * </pre>
 */
@SuppressWarnings("all")
public class Countdown extends Gatherer<Boolean> {
  public Procedure1<? super Boolean> await() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Promise<Boolean> to (Boolean)=>void");
  }
  
  public Object isSuccess() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field result is undefined for the type Countdown"
      + "\nType mismatch: cannot convert from Object to boolean"
      + "\nThere is no context to infer the closure\'s argument types from. Consider typing the arguments or put the closures into a typed context."
      + "\nvalues cannot be resolved"
      + "\nreduce cannot be resolved");
  }
}
