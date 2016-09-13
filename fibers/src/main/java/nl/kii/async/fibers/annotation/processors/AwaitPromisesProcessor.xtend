package nl.kii.async.fibers.annotation.processors

import nl.kii.async.fibers.FiberExtensions
import nl.kii.async.fibers.StreamIterator
import nl.kii.async.fibers.annotation.AwaitPromises
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import nl.kii.async.stream.Sink
import nl.kii.async.stream.Stream
import nl.kii.util.annotation.ActiveAnnotationTools
import nl.kii.util.annotation.Locked
import nl.kii.util.annotation.NamedParams
import nl.kii.util.annotation.processor.CopyMethodsProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference

import static extension nl.kii.util.annotation.ActiveAnnotationTools.*
import co.paralleluniverse.fibers.Suspendable

class AwaitPromisesProcessor extends CopyMethodsProcessor {

	override getAnnotationType() {
		AwaitPromises
	}

	override doCopyMethod(MutableClassDeclaration targetClass, TypeReference originalCls, MethodDeclaration originalMethod, AnnotationReference annotation, extension TransformationContext context) {
		val extension tools = new ActiveAnnotationTools(context)
		try {
			val promiseType = originalMethod.returnType.getPromiseTypeParameter(context)?.add(originalMethod.typeParameters)
			if(promiseType != null) {
				targetClass.doCopyAsAwaitingMethod(context) [
					it.originalCls = originalCls
					it.originalMethod = originalMethod
					it.resultType = promiseType
					methodNameBegin = annotation.getStringValue('promiseMethodNameBegin')
					methodNameRemove = annotation.getStringValue('methodNameStrip')
				]
			} else {
				val streamType = originalMethod.returnType.getStreamTypeParameter(context)?.add(originalMethod.typeParameters)
				if(streamType != null) {
					targetClass.doCopyAsStreamIteratorMethod(context) [
						it.originalCls = originalCls
						it.originalMethod = originalMethod
						it.resultType = streamType
						methodNameBegin = annotation.getStringValue('streamMethodNameBegin')
						methodNameRemove = annotation.getStringValue('methodNameStrip')
					]
				} else if(annotation.getBooleanValue('copyOtherStaticMethods')) {
					super.doCopyMethod(targetClass, originalCls, originalMethod, annotation, context)
				}
			} 
		} catch(Exception e) {
			throw new Exception('error while processing ' + originalMethod.signature + ': ' + e.message, e)
		}
	}

	@NamedParams
	def MutableMethodDeclaration doCopyAsAwaitingMethod(@Locked MutableClassDeclaration targetClass, TypeReference originalCls, MethodDeclaration originalMethod, TypeReference resultType, String methodNameBegin, String methodNameRemove, @Locked extension TransformationContext context) {
		val extension tools = new ActiveAnnotationTools(context)
		val newMethodName = methodNameBegin + (if(methodNameBegin != '') originalMethod.simpleName.toFirstUpper else originalMethod.simpleName)
		val newMethodNameStripped = newMethodName.replaceAll(methodNameRemove, '')
		targetClass.addMethodCopy(originalCls, originalMethod, newMethodNameStripped, true, true) [ extension newMethod, types |
			val instanceName = originalCls.simpleName.toFirstLower + 'Instance'
			// we return a promise or task
			returnType = resultType.add(types)
			// was it a task?
			val isTask = Task.newTypeReference.isAssignableFrom(originalMethod.returnType)
			// remove the first parameter if we have an instance extension method
			val newParameters = if(!originalMethod.static) parameters.tail else parameters
			// make it suspendable
			addAnnotation(Suspendable.newAnnotationReference)			
			// create the body
			body = '''
				«IF !isTask»return «ENDIF»«FiberExtensions».await(
					«if(originalMethod.static) originalCls.name else instanceName».«originalMethod.simpleName»(«FOR newParameter : newParameters SEPARATOR ', '»«newParameter.simpleName»«ENDFOR»)				
				);
			'''
		]
	}

	@NamedParams
	def MutableMethodDeclaration doCopyAsStreamIteratorMethod(@Locked MutableClassDeclaration targetClass, TypeReference originalCls, MethodDeclaration originalMethod, TypeReference resultType, String methodNameBegin, String methodNameRemove, @Locked extension TransformationContext context) {
		val extension tools = new ActiveAnnotationTools(context)
		val newMethodName = methodNameBegin + (if(methodNameBegin != '') originalMethod.simpleName.toFirstUpper else originalMethod.simpleName)
		val newMethodNameStripped = newMethodName.replaceAll(methodNameRemove, '')
		targetClass.addMethodCopy(originalCls, originalMethod, newMethodNameStripped, true, true) [ extension newMethod, types |
			val instanceName = originalCls.simpleName.toFirstLower + 'Instance'
			// we return an iterator of the stream
			returnType = StreamIterator.ref(resultType.add(types))
			// remove the first parameter if we have an instance extension method
			val newParameters = if(!originalMethod.static) parameters.tail else parameters
			// make it suspendable
			addAnnotation(Suspendable.newAnnotationReference)
			// create the body
			body = '''
				return new «returnType»(
					«if(originalMethod.static) originalCls.name else instanceName».«originalMethod.simpleName»(«FOR newParameter : newParameters SEPARATOR ', '»«newParameter.simpleName»«ENDFOR»)
				);
			'''
		]
	}

	/** Get the T inside the Promise<T> or null if the type does not match */
	private def static TypeReference getPromiseTypeParameter(TypeReference promiseType, extension TransformationContext context) {
		val extension tools = new ActiveAnnotationTools(context)
		if(promiseType.extendsType(Task.ref)) return void.newTypeReference
		if(!promiseType.extendsType(Promise.ref)) return null
		if(promiseType.actualTypeArguments.size != 2) throw new Exception('strange.. a promise but with ' + promiseType.actualTypeArguments.size + ' parameters! expected 2. For type ' + promiseType.simpleName)
		promiseType.actualTypeArguments.get(1) // the 1st is the input, the 2nd the output. we get the 2nd, since we want the output.
	}

	/** Get the T inside the Promise<T> or null if the type does not match */
	private def static TypeReference getStreamTypeParameter(TypeReference streamType, extension TransformationContext context) {
		val extension tools = new ActiveAnnotationTools(context)
		if(streamType.extendsType(Sink.ref)) {
			if(streamType.actualTypeArguments.size != 1) throw new Exception('strange.. a sink but with ' + streamType.actualTypeArguments.size + ' parameters! expected 2. For type ' + streamType.simpleName)
			streamType.actualTypeArguments.head // the 1st is the input, the 2nd the output. we get the 2nd, since we want the output.
		} else if(streamType.extendsType(Stream.ref)) {
			if(streamType.actualTypeArguments.size != 2) throw new Exception('strange.. a stream but with ' + streamType.actualTypeArguments.size + ' parameters! expected 2. For type ' + streamType.simpleName)
			streamType.actualTypeArguments.get(1) // the 1st is the input, the 2nd the output. we get the 2nd, since we want the output.
		} else null
	}
	
}
