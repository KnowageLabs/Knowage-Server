package it.eng.spagobi.utilities.exceptions;

public class ConfigurationException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 4719866971506983513L;

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable ex) {
		super(message, ex);
	}

	public ConfigurationException(Throwable ex) {
		super(ex);
	}

}
