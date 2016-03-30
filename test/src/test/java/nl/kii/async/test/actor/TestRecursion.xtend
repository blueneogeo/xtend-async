package nl.kii.async.test.actor

import org.junit.Test

/**
 * There are ways in which you can exchange stack for heap. 
 * 
 * Instead of making a recursive call within a function, 
 * have it return a lazy datastructure that makes the call when evaluated. 
 * 
 * You can then unwind the "stack" with Java's for-construct.
 * 
 * http://stackoverflow.com/questions/860550/stack-overflows-from-deep-recursion-in-java/861385#861385
 * 
 * Other way: throw an Exception!!!
 *  
 */

class TestRecursion {
	
	@Test
	def void testRecursion() {
		println(loop(1000000))
	}
	
	def loop(int loops) {
		try {
			var input = loops
			var result = 0
			while(loops > 0) {
				try {
					count(input, result)
				} catch(Call2 call) {
					input = call.input
					result = call.result
				}
			}
		} catch(Result result) {
			return result.result
		}
	}
	
	def count(int left, int sum) throws Call2, Result {
		if(left == 0) throw new Result(sum)
		else throw new Call2(left-1, sum+1)
	}
	
}

class Call2 extends Exception {
	
	public val int input
	public val int result
	
	new(int input, int result) {
		this.input = input
		this.result = result
	}
	
}

class Result extends Exception {
	
	public val int result
	
	new(int result) {
		this.result = result
	}
	
}
