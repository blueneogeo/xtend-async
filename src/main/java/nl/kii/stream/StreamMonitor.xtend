package nl.kii.stream

import java.util.List
import nl.kii.stream.StreamStats

class StreamMonitor {
	
	List<Pair<String, StreamStats>> chain = newLinkedList
	
	def add(String name, StreamStats stats) {
		chain.add(name -> stats)
	}
	
	def stats(String name) {
		chain.findFirst [ key == name ] ?.value
	}

	def stats(int position) {
		if(chain.size < position) return null
		chain.get(position - 1) ?.value
	}
	
	def getChain() {
		chain
	}
	
	override toString() '''
		«FOR pair : chain»
			--- «pair.key» --------------
				«pair.value»
		«ENDFOR»
	'''
	
}