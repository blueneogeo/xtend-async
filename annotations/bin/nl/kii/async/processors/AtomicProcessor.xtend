package nl.kii.async.processors

import com.google.common.util.concurrent.AtomicDouble
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import org.eclipse.xtend.lib.macro.AbstractFieldProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.Visibility

class AtomicProcessor extends AbstractFieldProcessor {


	override doTransform(MutableFieldDeclaration field, extension TransformationContext context) {

		val BOOLEAN = Boolean.newTypeReference
		val INTEGER = Integer.newTypeReference
		val LONG = Long.newTypeReference
		val DOUBLE = Double.newTypeReference
		
		// determine the wrapped type			
		val type = switch field.type.simpleName {
			case 'boolean', case 'Boolean': BOOLEAN
			case 'int', case 'Integer': INTEGER
			case 'long', case 'Long': LONG
			case 'float', case 'Float', case 'double', case 'Double': DOUBLE
			default: field.type
		}
		
		// determine the type of atomic
		val atomicType = switch type {
			case BOOLEAN: AtomicBoolean.newTypeReference
			case INTEGER: AtomicInteger.newTypeReference
			case LONG: AtomicLong.newTypeReference
			case DOUBLE: AtomicDouble.newTypeReference
			default: AtomicReference.newTypeReference(type)
		}
		
		// transform the field
		val methodVisibility = field.visibility
		val atomicMethodsVisibility = 
			if(methodVisibility == Visibility.PUBLIC) Visibility.PROTECTED
			else methodVisibility
		val fieldName = field.simpleName
		field.type = atomicType
		field.visibility = Visibility.PRIVATE
		val defaultValue = field.initializer?:''
		field.initializer = ['''new «atomicType.simpleName»(«defaultValue»)''']
		field.final = true
		field.simpleName = '_' + field.simpleName
		
		// add the setter
		field.declaringType.addMethod('set' + fieldName.toFirstUpper) [
			primarySourceElement = field
			visibility = methodVisibility
			static = field.static
			addParameter('value', type)
			// returnType = type
			body = '''
				this.«field.simpleName».set(value);
			'''
		]
		
		// add the getter
		field.declaringType.addMethod('get' + fieldName.toFirstUpper) [
			primarySourceElement = field
			visibility = methodVisibility
			static = field.static
			returnType = type
			body = '''
				return this.«field.simpleName».get();
			'''
		]
		
		// add getAndSet
		field.declaringType.addMethod('getAndSet' + fieldName.toFirstUpper) [
			primarySourceElement = field
			visibility = atomicMethodsVisibility
			static = field.static
			addParameter('value', type)
			returnType = type
			body = '''
				return this.«field.simpleName».getAndSet(value);
			'''
		]
		
		// for int and long, add increment and decrement
		if(type == INTEGER || type == LONG) {
			
			// add the increment method
			field.declaringType.addMethod('inc' + fieldName.toFirstUpper) [
				primarySourceElement = field
				visibility = atomicMethodsVisibility
				static = field.static
				returnType = type
				body = '''
					return this.«field.simpleName».incrementAndGet();
				'''
			]
			
			// add the increment method
			field.declaringType.addMethod('dec' + fieldName.toFirstUpper) [
				primarySourceElement = field
				visibility = atomicMethodsVisibility
				static = field.static
				returnType = type
				body = '''
					return this.«field.simpleName».decrementAndGet();
				'''
			]
			
		}
		
		// for all but object and boolean type
		if(type == INTEGER || type == LONG || type == DOUBLE) {
			// add the add method
			field.declaringType.addMethod('inc' + fieldName.toFirstUpper) [
				primarySourceElement = field
				addParameter('value', type)
				static = field.static
				visibility = atomicMethodsVisibility
				returnType = type
				body = '''
					return this.«field.simpleName».addAndGet(value);
				'''
			]
		}
			
	}
	
}
