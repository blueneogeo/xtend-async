package nl.kii.async

import co.paralleluniverse.fibers.Suspendable

class SuspendableProcedures {
	
	interface Procedure0 {
		@Suspendable def void apply()
	}
	
	interface Procedure1<Param> {
		@Suspendable def void apply(Param p)
	}
	
	interface Procedure2<P1, P2> {
		@Suspendable def void apply(P1 p1, P2 p2)
	}
	
	interface Procedure3<P1, P2, P3> {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3)
	}
	
	interface Procedure4<P1, P2, P3, P4> {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4)
	}
	
	interface Procedure5<P1, P2, P3, P4, P5> {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5)
	}
	
	interface Procedure6<P1, P2, P3, P4, P5, P6> {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6)
	}
	
}