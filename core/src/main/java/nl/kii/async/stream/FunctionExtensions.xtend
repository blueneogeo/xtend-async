package nl.kii.async.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

class FunctionExtensions {
	
	def static $(Procedure0 fn) {
		fn.apply
	}

	def static <P1> $(Procedure1<P1> fn, P1 p1) {
		fn.apply(p1)
	}

	def static <P1, P2> $(Procedure2<P1, P2> fn, P1 p1, P2 p2) {
		fn.apply(p1, p2)
	}
	
}
