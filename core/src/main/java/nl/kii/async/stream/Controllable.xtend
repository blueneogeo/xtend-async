package nl.kii.async.stream

interface Controllable {

	def void next()

	def void pause()

	def void resume()

	def void close()

}
