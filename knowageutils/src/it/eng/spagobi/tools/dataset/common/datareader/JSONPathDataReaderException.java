package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JSONPathDataReaderException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = -3583845788376416456L;

	public JSONPathDataReaderException(String message) {
		super(message);
	}

	public JSONPathDataReaderException(Throwable cause) {
		super(cause);
	}

	public JSONPathDataReaderException(String message, Throwable cause) {
		super(message, cause);
	}


}
