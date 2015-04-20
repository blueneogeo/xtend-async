package nl.kii.promise.test;

import nl.kii.promise.Promise;
import nl.kii.promise.Task;

public class TestPromise {
  /* @Atomic
   */private int result = 0;
  
  /* @Test
   */public void canBeFulfilledBeforeListening() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\n<=> cannot be resolved."
      + "\nThe method then is undefined for the type TestPromise"
      + "\n<=> cannot be resolved.");
  }
  
  /* @Test
   */public void canBeFulfilledAfterListening() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\nThe method then is undefined for the type TestPromise"
      + "\n<=> cannot be resolved."
      + "\n<=> cannot be resolved.");
  }
  
  /* @Test
   */public void silentlyFailsWithoutHandler() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\n<=> cannot be resolved."
      + "\nThe method then is undefined for the type TestPromise"
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved");
  }
  
  /* @Test
   */public void canCatchErrorsBeforeListening() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\n<=> cannot be resolved."
      + "\nThe method on is undefined for the type TestPromise"
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\n<=> cannot be resolved."
      + "\nthen cannot be resolved"
      + "\n/ cannot be resolved");
  }
  
  /* @Test
   */public void canCatchErrorsAfterListening() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\n<=> cannot be resolved."
      + "\nThe method then is undefined for the type TestPromise"
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Test
   */public void canCatchSpecificErrors() {
    throw new Error("Unresolved compilation problems:"
      + "\nInteger cannot be resolved to a type."
      + "\n<=> cannot be resolved."
      + "\nThe method then is undefined for the type TestPromise"
      + "\nThe method or field NullPointerException is undefined for the type TestPromise"
      + "\nThe method fail is undefined for the type TestPromise"
      + "\nThe method or field ArithmeticException is undefined for the type TestPromise"
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\nThe method fail is undefined for the type TestPromise"
      + "\n<=> cannot be resolved."
      + "\n/ cannot be resolved"
      + "\non cannot be resolved"
      + "\non cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /* @Test
   */public void testPromiseChaining() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method map is undefined for the type TestPromise"
      + "\nresolve cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  /* @Test
   */public void testTaskChain() {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to Task"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to Task"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to Task"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to Task"
      + "\nmap cannot be resolved"
      + "\nresolve cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nresolve cannot be resolved"
      + "\nthen cannot be resolved");
  }
  
  /* @Test
   */public void testLongChain() {
    throw new Error("Unresolved compilation problems:"
      + "\nAtomicBoolean cannot be resolved."
      + "\nAtomicReference cannot be resolved."
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\nThe method assertEquals is undefined for the type TestPromise"
      + "\nThe method assertNull is undefined for the type TestPromise"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (int)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nThrowable cannot be resolved to a type."
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\non cannot be resolved"
      + "\nset cannot be resolved"
      + "\nalways cannot be resolved"
      + "\nset cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved"
      + "\nget cannot be resolved"
      + "\nget cannot be resolved");
  }
  
  /* @Atomic
   */private boolean alwaysDone;
  
  /* @Atomic
   */private /* Throwable */Object caughtError;
  
  /* @Test
   */public void testLongChainWithError() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method println is undefined for the type TestPromise"
      + "\nException cannot be resolved."
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\nThe method println is undefined for the type TestPromise"
      + "\nThe method println is undefined for the type TestPromise"
      + "\nThe method fail is undefined for the type TestPromise"
      + "\nThe method assertNotNull is undefined for the type TestPromise"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (int)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nInvalid number of arguments. The method addOne(int, Promise<Integer>) is not applicable for the arguments (TestPromise)"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\nType mismatch: cannot convert implicit first argument from TestPromise to int"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\n!= cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncall cannot be resolved"
      + "\non cannot be resolved"
      + "\nthen cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  private final Object threads /* Skipped initializer because of errors */;
  
  /* @Async
   */public Object addOne(final int n, final /* Promise<Integer> */Object promise) {
    throw new Error("Unresolved compilation problems:"
      + "\n<< cannot be resolved."
      + "\n+ cannot be resolved."
      + "\npromise cannot be resolved");
  }
  
  /* @Async
   */public Object sayHello(final Task task) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method println is undefined for the type TestPromise"
      + "\npromise cannot be resolved");
  }
  
  /* @Test
   */public void testPromiseErrorChaining() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method map is undefined for the type TestPromise"
      + "\n/ cannot be resolved."
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\nThe method println is undefined for the type TestPromise"
      + "\npromise cannot be resolved"
      + "\n- cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nmap cannot be resolved"
      + "\n+ cannot be resolved"
      + "\non cannot be resolved"
      + "\nthen cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  /* @Test
   */public void testPromiseChain() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method println is undefined for the type TestPromise"
      + "\nThe method println is undefined for the type TestPromise"
      + "\nThe method println is undefined for the type TestPromise"
      + "\npromise cannot be resolved"
      + "\nmap cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nthen cannot be resolved"
      + "\nthen cannot be resolved"
      + "\nthen cannot be resolved"
      + "\nset cannot be resolved");
  }
  
  /* @Atomic
   */private boolean foundError;
  
  /* @Test
   */public void testPromiseWithLaterError2() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method fail is undefined for the type TestPromise"
      + "\nThe method or field Throwable is undefined for the type TestPromise"
      + "\nThe method assertTrue is undefined for the type TestPromise"
      + "\npromise cannot be resolved"
      + "\nmap cannot be resolved"
      + "\n/ cannot be resolved"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved"
      + "\nset cannot be resolved");
  }
}
