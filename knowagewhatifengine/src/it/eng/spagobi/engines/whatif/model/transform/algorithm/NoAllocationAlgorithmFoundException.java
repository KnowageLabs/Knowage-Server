package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

public class NoAllocationAlgorithmFoundException extends SpagoBIEngineException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2456862812696266212L;

	public NoAllocationAlgorithmFoundException(String message) {
		super(message);
	}

	public NoAllocationAlgorithmFoundException(String message, String description) {
		super(message);
		setDescription(description);
	}

	public NoAllocationAlgorithmFoundException(String message, Throwable ex) {
		super(message, ex);
	}

	public NoAllocationAlgorithmFoundException(String message, String description, Throwable ex) {
		super(message, ex);
		setDescription(description);
	}
}
