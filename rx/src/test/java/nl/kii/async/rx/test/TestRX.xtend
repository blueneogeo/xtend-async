package nl.kii.async.rx.test

import org.junit.Test
import rx.Observable
import rx.Subscriber
import org.junit.Ignore

class TestRX {

	static var counter = 0
	
	@Ignore @Test
	def void testRXRecursion() {
		Observable.from(1..1_000_000_000).subscribe(new Subscriber<Integer> {
			
			override onCompleted() {
				println('done')
			}
			
			override onError(Throwable e) {
			}
			
			override onNext(Integer t) {
				counter++
			}
			
		})
		println(counter)
	}
	
}
