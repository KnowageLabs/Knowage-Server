package it.eng.spagobi.functions.dao;

import java.util.ArrayList;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class FunctionInUseException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = -5539516967452702081L;

	ArrayList<String> objectsLabel;
	String userMessage;
	String fullMessage;

	public static final String USER_MESSAGE = "FunctionInUseException";

	public FunctionInUseException(String message) {
		super(message);
		this.userMessage = USER_MESSAGE;
		this.fullMessage = message;
	}

	public void setObjectsLabel(ArrayList<String> objectsLabel) {
		this.objectsLabel = objectsLabel;
	}
}
