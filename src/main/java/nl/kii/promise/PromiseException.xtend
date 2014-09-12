package nl.kii.promise

class PromiseException extends Exception {
	
	public val Object value
	
	new(String message, Object value) {
		super(message + ': ' + value)
		this.value = value
	}

	new(String message, Object value, Throwable cause) {
		super(message + ': ' + value, cause)
		this.value = value
	}
	
}
