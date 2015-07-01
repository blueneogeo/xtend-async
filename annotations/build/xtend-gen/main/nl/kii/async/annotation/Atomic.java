package nl.kii.async.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import nl.kii.async.processors.AtomicProcessor;
import org.eclipse.xtend.lib.macro.Active;

/**
 * The Atomic xtend active annotation transforms a field into atomic field.
 * This lets you reason about atomic values the same way as normal vars.
 * For more about atomic values, check the
 * <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/package-summary.html">Java Atomic Package</a>.
 * <p>
 * It creates a getter and setter for the field, and the field itself is transformed
 * into either an AtomicInt, AtomicBoolean, AtomicLong, AtomicFloat or AtomicReference.
 * <p>
 * The setters perform a getAndSet, meaning that they return the value that was in
 * the value before you perform the set operation:
 * 
 * <pre>
 * @Atomic val i = 10
 * val j = (i = 11)
 * assertEquals(11, i)
 * assertEquals(10, j)
 * </pre>
 * 
 * ints and longs also get a method incXXX and decXXX, where XXX is the
 * name of the method. These will add / remove one from the variable, and returned the
 * modified value:
 * 
 * <pre>
 * @Atomic val number = 10
 * assertEquals(11, incNumber)
 * </pre>
 * 
 * <p>
 * Supported types are:
 * <ul>
 * <li>boolean, Boolean: converted into AtomicBoolean
 * <li>int, Integer: converted into AtomicInteger
 * <li>long, Long: converted into AtomicLong
 * <li>float, Float, double, Double: converted into AtomicDouble
 * <li>any other object: converted into AtomicReference. Generics work.
 * </ul>
 * <p>
 * The following methods are added:
 * <ul>
 * <li>all types: getFieldName() and setFieldName(value). setFieldName returns the value BEFORE setting (calls getAndSet())
 * <li>int, Integer, long, Long: incFieldName() and decFieldName, increases and decreases by one and returns the new value
 * <li>int, Integer, long, Long, float, Float, double, Double: incFieldName(value): increases the field by the given value, and returns the new value
 * </ul>
 * <p>
 * Supported field properties are:
 * <ul>
 * <li>visibility: public (creates a property), private(default), protected, and package
 * <li>static: will create static getters and setters
 * </ul>
 * Setting the visibility to public will only make the getter and setter method public,
 * the other incXXX etc methods will be set to protected (so you can use the atomic as
 * an atomic property without polluting the class public method with many extra methods).
 */
@Active(AtomicProcessor.class)
@Target(ElementType.FIELD)
public @interface Atomic {
}
