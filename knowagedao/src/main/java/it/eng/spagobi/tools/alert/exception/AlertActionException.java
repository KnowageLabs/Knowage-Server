package it.eng.spagobi.tools.alert.exception;

public class AlertActionException extends Exception {

	public AlertActionException(Throwable t) {
		super(t);
	}

	public AlertActionException(String message, Throwable t) {
		super(message, t);
	}
}
