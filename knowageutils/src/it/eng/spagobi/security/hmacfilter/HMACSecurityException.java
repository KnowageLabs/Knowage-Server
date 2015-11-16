package it.eng.spagobi.security.hmacfilter;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class HMACSecurityException extends ServletException {

	public HMACSecurityException(String message) {
		super(message);

	}

	public HMACSecurityException(Throwable cause) {
		super(cause);

	}

	public HMACSecurityException(String message, Throwable cause) {
		super(message, cause);

	}

}
