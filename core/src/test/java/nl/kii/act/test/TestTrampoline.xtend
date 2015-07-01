//package nl.kii.act.test
//
//import org.junit.Test
//
//class TestTrampoline {
//	
//	@Test
//	def void testTrampoline() {
//		println(Trampoline.trampoline(even2(10000)))
//	}
//	
//	def Operation<Boolean> even2(int n) {
//		if (n == 0) new Done(true)
//  		else new Call [| odd2(n - 1) ]
//	}
//	
//	def Operation<Boolean> odd2(int n) {
//	  if (n == 0) new Done(false)
//	  else new Call [| even2(n - 1) ]
//	}
//	
//}
//
//interface Operation<T> {
//}
//
//class Done<T> implements Operation<T> {
//	public val T result
//	new(T result) { this.result = result }
//}
//
//class Call<T> implements Operation<T> {
//	public val =>Operation<T> thunk
//	new(=>Operation<T> thunk) { this.thunk = thunk }
//}
//
//class Trampoline {
//	
//	def static <T> T trampoline(Operation<T> it) {
//		switch it {
//			Call<T>: trampoline(thunk.apply)
//			Done<T>: result 
//		}
//	}
//	
//}
