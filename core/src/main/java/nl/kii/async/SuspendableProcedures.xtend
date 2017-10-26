package nl.kii.async

import co.paralleluniverse.fibers.Suspendable
import java.io.Serializable

class SuspendableProcedures {
	
	interface Procedure0 extends Serializable {
		@Suspendable def void apply()
	}
	
	interface Procedure1<Param> extends Serializable {
		@Suspendable def void apply(Param p)
	}
	
	interface Procedure2<P1, P2> extends Serializable {
		@Suspendable def void apply(P1 p1, P2 p2)
	}
	
	interface Procedure3<P1, P2, P3> extends Serializable {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3)
	}
	
	interface Procedure4<P1, P2, P3, P4> extends Serializable {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4)
	}
	
	interface Procedure5<P1, P2, P3, P4, P5> extends Serializable {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5)
	}
	
	interface Procedure6<P1, P2, P3, P4, P5, P6> extends Serializable {
		@Suspendable def void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6)
	}
	
}