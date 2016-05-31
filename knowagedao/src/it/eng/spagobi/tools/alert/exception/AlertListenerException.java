package it.eng.spagobi.tools.alert.exception;

public class AlertListenerException extends Exception {

	public AlertListenerException(Throwable t) {
		super(t);
	}

	public AlertListenerException(String message, Throwable t) {
		super(message, t);
	}
}
