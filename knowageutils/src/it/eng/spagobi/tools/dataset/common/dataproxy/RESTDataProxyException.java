package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class RESTDataProxyException extends SpagoBIRuntimeException {


	private static final long serialVersionUID = 3586826519991971411L;

	public RESTDataProxyException(String message) {
		super(message);
	}

	public RESTDataProxyException(Throwable cause) {
		super(cause);
	}

	public RESTDataProxyException(String message, Throwable cause) {
		super(message, cause);
	}


}
