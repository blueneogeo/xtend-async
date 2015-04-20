package nl.kii.async.annotation.test;

import nl.kii.async.annotation.test.Tester;

public class TestAtomicAnnotation {
  /* @Atomic
   */public int counter = 2;
  
  /* @Atomic
   */private /* Long */Object longNumber;
  
  /* @Atomic
   */private float price;
  
  /* @Atomic
   */private Tester tester /* Skipped initializer because of errors */;
  
  /* @Test
   */public void testInteger() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method getAndSetCounter is undefined for the type TestAtomicAnnotation"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method or field incCounter is undefined for the type TestAtomicAnnotation"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method or field decCounter is undefined for the type TestAtomicAnnotation"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method incCounter is undefined for the type TestAtomicAnnotation");
  }
  
  /* @Test
   */public void testLong() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method or field incLongNumber is undefined for the type TestAtomicAnnotation");
  }
  
  /* @Test
   */public void testFloat() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nType mismatch: cannot convert from double to float");
  }
  
  /* @Atomic
   */private int i = 0;
  
  /* @Test
   */public void testReference() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method getAndSetTester is undefined for the type TestAtomicAnnotation"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\nThe method assertEquals is undefined for the type TestAtomicAnnotation"
      + "\n+ cannot be resolved."
      + "\nThe method println is undefined for the type TestAtomicAnnotation"
      + "\nThe field name is not visible"
      + "\nInvalid number of arguments. The constructor Tester() is not applicable for the arguments (java.lang.String)"
      + "\nInvalid number of arguments. The constructor Tester() is not applicable for the arguments (java.lang.String)"
      + "\nThe field name is not visible"
      + "\nname cannot be resolved");
  }
  
  public Object doSomething(final /*  */Object closure) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
}
