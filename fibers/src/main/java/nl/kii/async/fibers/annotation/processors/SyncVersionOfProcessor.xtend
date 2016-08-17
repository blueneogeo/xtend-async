package nl.kii.async.fibers.annotation.processors

import nl.kii.async.annotation.Async
import nl.kii.async.fibers.FiberExtensions
import nl.kii.async.fibers.annotation.SyncVersionOf
import nl.kii.async.promise.Input
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeReference

class SyncVersionOfProcessor extends AbstractClassProcessor {

//	val static ASYNC_RETURN_TYPES = #[Promise, Input, Task]

	override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {
		val annotation = cls.findAnnotation(SyncVersionOf.newTypeReference.type)

		val originalCls = annotation.getClassValue('value')
		val asyncExtensionMethods = originalCls.allResolvedMethods
			.map [ declaration ]
			.filter [ static && !abstract && !returnType.inferred ]
			.filter [ findAnnotation(Async.newTypeReference.type) != null ]
//			.filter [ method | ASYNC_RETURN_TYPES.map[newTypeReference].findFirst [ isAssignableFrom(method.returnType) ] != null ]
		
		for(asyncMethod : asyncExtensionMethods) {
			cls.doTransform(asyncMethod, originalCls, context)
		}
	}

	def doTransform(MutableClassDeclaration cls, MethodDeclaration asyncMethod, TypeReference originalCls, extension TransformationContext context) {

		cls.addMethod(asyncMethod.simpleName) [
			static = asyncMethod.static
			final = asyncMethod.final
			varArgs = asyncMethod.varArgs
			visibility = asyncMethod.visibility
			deprecated = asyncMethod.deprecated
			docComment = asyncMethod.docComment
			exceptions = asyncMethod.exceptions
			synchronized = asyncMethod.synchronized
			
			for(parameter : asyncMethod.parameters) {
				addParameter(parameter.simpleName, parameter.type)
			}
			switch type : asyncMethod.returnType {
				case Input.newTypeReference.isAssignableFrom(type): {
					returnType = type.actualTypeArguments.get(0)
					body = ['''
						«IF !asyncMethod.returnType.isVoid»return «ENDIF»«FiberExtensions.name».await(
							«originalCls.name».«asyncMethod.simpleName»(«FOR parameter : asyncMethod.parameters SEPARATOR ', '»«parameter.simpleName»«ENDFOR»)
						);
					''']
				}
				case Promise.newTypeReference.isAssignableFrom(type): {
					returnType = type.actualTypeArguments.get(1)
					body = ['''
						«IF !asyncMethod.returnType.isVoid»return «ENDIF»«FiberExtensions.name».await(
							«originalCls.name».«asyncMethod.simpleName»(«FOR parameter : asyncMethod.parameters SEPARATOR ', '»«parameter.simpleName»«ENDFOR»)
						);
					''']
				}
				case Task.newTypeReference.isAssignableFrom(type): {
					body = ['''
						«IF !asyncMethod.returnType.isVoid»return «ENDIF»«FiberExtensions.name».await(
							«originalCls.name».«asyncMethod.simpleName»(«FOR parameter : asyncMethod.parameters SEPARATOR ', '»«parameter.simpleName»«ENDFOR»)
						);
					''']
				}
				default: {
					
				}
			}
		]
		
	}
	
}
