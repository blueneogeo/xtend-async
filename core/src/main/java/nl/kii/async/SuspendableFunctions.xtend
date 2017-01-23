package nl.kii.async

import co.paralleluniverse.fibers.Suspendable

class SuspendableFunctions {
	
	interface Function0<Result> {
		@Suspendable def Result apply()
	}
	
	interface Function1<Param, Result> {
		@Suspendable def Result apply(Param p)
	}
	
	interface Function2<P1, P2, Result> {
		@Suspendable def Result apply(P1 p1, P2 p2)
	}
	
	interface Function3<P1, P2, P3, Result> {
		@Suspendable def Result apply(P1 p1, P2 p2, P3 p3)
	}
	
	interface Function4<P1, P2, P3, P4, Result> {
		@Suspendable def Result apply(P1 p1, P2 p2, P3 p3, P4 p4)
	}
	
	interface Function5<P1, P2, P3, P4, P5, Result> {
		@Suspendable def Result apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5)
	}
	
	interface Function6<P1, P2, P3, P4, P5, P6, Result> {
		@Suspendable def Result apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6)
	}
	
}