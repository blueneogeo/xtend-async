package nl.kii.stream

import nl.kii.async.annotation.Atomic
import java.util.Date
import java.text.SimpleDateFormat

class StreamStats {

	// counts
	
	@Atomic public long valueCount
	@Atomic public long finishCount
	@Atomic public long errorCount

	// moments
	
	@Atomic public long startTS
	@Atomic public long closeTS
	
	@Atomic public long firstEntryTS
	@Atomic public long firstValueTS
	@Atomic public long firstErrorTS
	@Atomic public long firstFinishTS

	@Atomic public long lastEntryTS
	@Atomic public long lastValueTS
	@Atomic public long lastFinishTS
	@Atomic public long lastErrorTS

	// values

	@Atomic public Object lastValue
	@Atomic public Throwable lastError
	
	override toString() '''
		values:   «valueCount»
		errors:   «errorCount»
		finishes: «finishCount»
		
		time taken: «timeTaken» ms
		value rate: «valueRate» / ms
		error rate: «errorRate» / ms
		
		«IF startTS > 0»
			created at: «startTS.text»
			
			first entry at: «firstEntryTS.text» 
			first value at: «firstValueTS.text» 
			first error at: «firstErrorTS.text» 
			first finish at: «firstFinishTS.text» 
			
			last entry at: «lastEntryTS.text» 
			last value at: «lastValueTS.text» 
			last  error at: «lastErrorTS.text» 
			last finish at: «lastFinishTS.text» 
			
			closed at:  «closeTS.text»
		«ELSE»
			not started
		«ENDIF»

		«IF closeTS > 0»
			closed at:  «closeTS.text»
		«ELSE»
			stream still open
		«ENDIF»
		
	'''
	
	def timeTaken() { now - startTS }
	def valueRate() { new Float(valueCount) / timeTaken }
	def errorRate() { new Float(errorCount) / timeTaken }
	
	val static DATEFORMAT = new SimpleDateFormat('HH:mm:ss.SSS')
	
	private static def text(long timestamp) {
		DATEFORMAT.format(new Date(timestamp))
	}
	
	private static def now() {
		System.currentTimeMillis
	}
	
}
