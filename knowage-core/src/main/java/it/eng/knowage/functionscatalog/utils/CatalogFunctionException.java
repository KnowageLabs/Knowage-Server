package it.eng.knowage.functionscatalog.utils;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CatalogFunctionException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = -430030929593020346L;

	public CatalogFunctionException(String message) {
		super(message);
	}

	public CatalogFunctionException(Throwable cause) {
		super(cause);
	}

	public CatalogFunctionException(String message, Throwable cause) {
		super(message, cause);
	}
}
