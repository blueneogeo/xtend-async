package nl.kii.act.test;

@SuppressWarnings("all")
public class Call2 extends Exception {
  public final int input;
  
  public final int result;
  
  public Call2(final int input, final int result) {
    this.input = input;
    this.result = result;
  }
}
