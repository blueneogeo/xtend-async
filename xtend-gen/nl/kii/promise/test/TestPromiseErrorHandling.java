package nl.kii.promise.test;

public class TestPromiseErrorHandling {
  /* @Atomic
   */private int value;
  
  /* @Atomic
   */private boolean match1;
  
  /* @Atomic
   */private boolean match2;
  
  /* @Atomic
   */private boolean match3;
  
  /* @Atomic
   */private boolean match4;
  
  /* @Test
   */public void canMonitorErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method fail is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Throwable is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Test
   */public void canMatchErrorTypes() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method fail is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field IllegalArgumentException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Exception is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Throwable is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved"
      + "\non cannot be resolved"
      + "\non cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Test
   */public void canSwallowErrorTypes() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method fail is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Exception is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Throwable is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved"
      + "\neffect cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Test
   */public void canMapErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Exception is undefined for the type TestPromiseErrorHandling"
      + "\nThe method println is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field cause is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\nmap cannot be resolved"
      + "\non cannot be resolved"
      + "\nthen cannot be resolved");
  }
  
  /* @Test
   */public void canFilterMapErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field IllegalArgumentException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Throwable is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nthen cannot be resolved");
  }
  
  /* @Test
   */public void canFilterAsyncMapErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method map is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field IllegalArgumentException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromiseErrorHandling"
      + "\nThe method or field Throwable is undefined for the type TestPromiseErrorHandling"
      + "\n<< cannot be resolved."
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\nthen cannot be resolved");
  }
}
