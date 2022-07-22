package it.eng.knowage.boot.error;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.eng.knowage.boot.utils.EngineMessageBundle;

public class InvalidHtmlPayloadException extends KnowageRuntimeException {

	private static final long serialVersionUID = 2861675799031365764L;

	/*
	 * Status
	 */
	private Status status = Response.Status.INTERNAL_SERVER_ERROR;

	/*
	 * Error Code
	 */
	private String code = "KN-CORE-001";

	/*
	 * User oriented description of the exception. It is usually prompted to the user. Instead the message passed to the constructor is developer oriented and
	 * it should be just logged.
	 */
	private String description = "Invalid HTML payload";

	private String payload;

	{
		addHint("Please fix the HTML payload");
	}

	public InvalidHtmlPayloadException(String payload) {
		super((String) null, (Throwable) null);
		this.payload = payload;
	}

	/**
	 * Builds a <code>SpagoBIException</code>.
	 *
	 * @param message Text of the exception
	 * @param ex      previous Throwable object
	 */
	public InvalidHtmlPayloadException(String payload, Throwable ex) {
		super((String) null, ex);
		this.payload = payload;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String getLocalizedMessage() {
		String localizedMessage = EngineMessageBundle.getMessage(getCode(), getLocale(), new String[] { escapeHtml4(payload) });
		return localizedMessage;
	}

}
