package nl.kii.act.test;

@SuppressWarnings("all")
public class Result extends Exception {
  public final int result;
  
  public Result(final int result) {
    this.result = result;
  }
}
