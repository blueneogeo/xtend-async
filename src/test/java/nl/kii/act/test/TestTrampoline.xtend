package nl.kii.act.test

import org.junit.Test

class TestTrampoline {
	
	@Test
	def void testTrampoline() {
		println(Trampoline.trampoline(even2(10000)))
	}
	
	def Bounce<Boolean> even2(int n) {
		if (n == 0) new Done(true)
  		else new Call [| odd2(n - 1) ]
	}
	
	def Bounce<Boolean> odd2(int n) {
	  if (n == 0) new Done(false)
	  else new Call [| even2(n - 1) ]
	}
	
}

// trampoline(even2(9999))

interface Bounce<T> {
}

class Done<T> implements Bounce<T> {
	public val T result
	new(T result) { this.result = result }
}

class Call<T> implements Bounce<T> {
	public val =>Bounce<T> thunk
	new(=>Bounce<T> thunk) { this.thunk = thunk }
}

class Trampoline {
	
	def static <T> T trampoline(Bounce<T> it) {
		switch it {
			Call<T>: trampoline(thunk.apply)
			Done<T>: result 
		}
	}
	
}

//sealed trait Bounce[A]
//case class Done[A](result: A) extends Bounce[A]
//case class Call[A](thunk: () => Bounce[A]) extends Bounce[A]
//
//def trampoline[A](bounce: Bounce[A]): A = bounce match {
//  case Call(thunk) => trampoline(thunk())
//  case Done(x) => x
//}


