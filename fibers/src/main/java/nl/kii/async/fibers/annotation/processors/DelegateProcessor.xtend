package nl.kii.async.fibers.annotation.processors

import com.google.common.annotations.Beta
import java.util.List
import nl.kii.async.fibers.annotation.Delegate
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration

class DelegateProcessor extends org.eclipse.xtend.lib.annotations.DelegateProcessor {

	override doTransform(List<? extends MutableMemberDeclaration> elements, extension TransformationContext context) {
		val extension util = new DelegateProcessor.Util2(context)
		for(element : elements) {
			if(element.validDelegate) {
				for(method : element.methodsToImplement) {
					val delegateMethod = element.implementMethod(method)
					for(annotation : method.declaration.annotations) {
						delegateMethod.addAnnotation(annotation)
					}
				}
			}
		}
	}
	
	/**
	 * @since 2.7
	 * @noextend
	 * @noreference
 	*/
 	@Beta
	static class Util2 extends Util {
	
		extension TransformationContext context
	
		new(TransformationContext context) {
			super(context)
			this.context = context
		}

		override getDelegates(TypeDeclaration it) {
			declaredMembers.filter[findAnnotation(findTypeGlobally(Delegate)) !== null]
		}
		
		override listedInterfaces(MemberDeclaration it) {
			findAnnotation(findTypeGlobally(Delegate)).getClassArrayValue("value").toSet
		}
		
	}

}
