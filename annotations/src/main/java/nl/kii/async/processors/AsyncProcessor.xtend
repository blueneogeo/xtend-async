package nl.kii.async.processors

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference
import nl.kii.async.annotation.Async
import org.eclipse.xtend.lib.macro.AbstractMethodProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference

class AsyncProcessor extends AbstractMethodProcessor {

	def boolean isPromiseType(TypeReference type) {
		type.simpleName.startsWith('IPromise') ||
		type.simpleName.startsWith('Promise') ||
		type.simpleName.startsWith('Task')
	}

	override doTransform(MutableMethodDeclaration method, extension TransformationContext context) {
		
		// find the promise/task parameter
		val promiseParameter = method.parameters.filter[type.isPromiseType].head

		// if there is no promise parameter, it must be in the return type
		if(promiseParameter == null) {
			if(!method.returnType.inferred && !method.returnType.isPromiseType) {
				method.addError('Methods annotated with @Async must either return a Task or Promise, or pass a Task or Promise in their parameters.')
			}
			return
		}
		
		// add the new method that calls the old method, on the same thread
		method.declaringType.addMethod(method.simpleName) [
			primarySourceElement = method
			// copy properties
			if(!method.typeParameters?.empty)
				method.addError('Currently type parameters are not supported for @Async, because Xtend Active Annotation do not fully support typed parameters')
			for(typeParameter : method.typeParameters) {
				addTypeParameter(typeParameter.simpleName, typeParameter.upperBounds)
			}
			static = method.static
			docComment = method.docComment
			visibility = method.visibility
			synchronized = method.synchronized
			exceptions = method.exceptions
			varArgs = method.varArgs
			// find the promise/task parameter and add the other parameters to the new method
			val promise = new AtomicReference<MutableParameterDeclaration>
			for(parameter : method.parameters) {
				if(
					parameter.type.simpleName.startsWith('Promise') ||
					parameter.type.simpleName.startsWith('Task')
				) promise.set(parameter)
				else addParameter(parameter.simpleName, parameter.type)
			}
			// we must have found at least one task or promise to return
			if(promise.get == null) {
				method.addError('Methods annotated with @Async must pass the Promise or Task to return in its parameters.')
			} else {
				returnType = promise.get.type
				// and build the code that wraps the old method
				body = ['''
					final «promise.get.type.name» «promise.get.simpleName» = new «promise.get.type.name»();
					try {
						«method.simpleName»(«FOR parameter : method.parameters SEPARATOR ','»«parameter.simpleName»«ENDFOR»);
					} catch(Throwable t) {
						«promise.get.simpleName».error(t);
					} finally {
						return «promise.get.simpleName»;
					}
				''']
			}
		]
		// also add a new method that does the same but on a new thread from a passed executor
		val async = method.findAnnotation(Async.newTypeReference.type)
		if(!async?.getBooleanValue('value')) return;
		method.declaringType.addMethod(method.simpleName) [
			primarySourceElement = method
			// copy properties
			for(typeParameter : method.typeParameters) {
				addTypeParameter(typeParameter.simpleName, typeParameter.upperBounds)
			}
			static = method.static
			docComment = method.docComment
			visibility = method.visibility
			synchronized = method.synchronized
			exceptions = method.exceptions
			varArgs = method.varArgs
			// start as a first parameter with the Executor to use
			addParameter('executor', Executor.newTypeReference)
			// find the promise/task parameter and add the other parameters to the new method
			val promise = new AtomicReference<MutableParameterDeclaration>
			for(parameter : method.parameters) {
				if(
					parameter.type.simpleName.startsWith('Promise') ||
					parameter.type.simpleName.startsWith('Task')
				) promise.set(parameter)
				else addParameter(parameter.simpleName, parameter.type)
			}
			// we must have found at least one task or promise to return
			if(promise.get == null) {
				method.addError('Methods annotated with @Async must pass the Promise or Task to return in its parameters.')
			} else {
				returnType = promise.get.type
				// and build the code that wraps the old method
				body = ['''
					final «promise.get.type.simpleName» «promise.get.simpleName» = new «promise.get.type.simpleName»();
					final Runnable toRun = new Runnable() {
						public void run() {
							try {
								«method.simpleName»(«FOR parameter : method.parameters SEPARATOR ','»«parameter.simpleName»«ENDFOR»);
							} catch(Throwable t) {
								«promise.get.simpleName».error(t);
							}
						}
					};
					executor.execute(toRun);
					return «promise.get.simpleName»;
				''']
			}
		]
	}
	
}
