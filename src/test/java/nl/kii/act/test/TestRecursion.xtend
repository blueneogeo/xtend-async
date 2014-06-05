package nl.kii.act.test

import org.junit.Test
import java.util.Stack

/**
 * There are ways in which you can exchange stack for heap. 
 * 
 * Instead of making a recursive call within a function, 
 * have it return a lazy datastructure that makes the call when evaluated. 
 * 
 * You can then unwind the "stack" with Java's for-construct.
 * 
 * http://stackoverflow.com/questions/860550/stack-overflows-from-deep-recursion-in-java/861385#861385 
 */

class TestRecursion {
	
	@Test
	def void testRecursion() {
		#[1, 2, 3].perform [ println ]
	}
	
	def void perform(int[] inbox, (int)=>void processor) {
		if(inbox.empty) return;
		perform(inbox.clone.subList(1, inbox.length-1), processor)
	}
	
}

class Recursion<T, R> {
	
	static class Operation<T, R> {
		
		
		
	}

	Stack<Operation<T, R>> ops
	
}

