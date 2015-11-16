package it.eng.spagobi.utilities.exceptions;

import it.eng.spagobi.utilities.messages.EngineMessageBundle;

import java.util.Locale;

public class SpagoBIRestServiceException extends SpagoBIRuntimeException {

	private String localizationCode = "generic.error";
	private Locale locale = Locale.US;

	private static final long serialVersionUID = 7238971352468593356L;

	/**
	 * Builds a <code>SpagoBIRestServiceException</code>.
	 * 
	 * @param locale
	 *            the locale
	 * @param ex
	 *            the parent exception
	 */
	public SpagoBIRestServiceException(Locale locale, Throwable ex) {
		super(ex);
		this.locale = locale;
	}

	/**
	 * Builds a <code>SpagoBIRestServiceException</code>.
	 * 
	 * @param localizationCode
	 *            the error code for localization
	 * @param locale
	 *            the locale
	 * @param ex
	 *            the parent exception
	 */
	public SpagoBIRestServiceException(String localizationCode, Locale locale, Throwable ex) {
		super(ex);
		setLocalizationCode(localizationCode);
		this.locale = locale;
	}

	/**
	 * Builds a <code>SpagoBIRestServiceException</code>.
	 * 
	 * @param localizationCode
	 *            the error code for localization
	 * @param locale
	 *            the locale
	 * @param message
	 *            a message for the exception
	 * @param ex
	 *            the parent exception
	 */
	public SpagoBIRestServiceException(String localizationCode, Locale locale, String message, Throwable ex) {
		super(message, ex);
		setLocalizationCode(localizationCode);
		this.locale = locale;
	}

	/**
	 * Builds a <code>SpagoBIRestServiceException</code>.
	 * 
	 * @param localizationCode
	 *            the error code for localization
	 * @param locale
	 *            the locale
	 * @param message
	 *            a message for the exception
	 */
	public SpagoBIRestServiceException(String localizationCode, Locale locale, String message) {
		super(message);
		this.locale = locale;
		setLocalizationCode(localizationCode);
	}

	public String getLocalizationCode() {
		return localizationCode;
	}

	public void setLocalizationCode(String localizationCode) {
		this.localizationCode = localizationCode;
	}

	@Override
	public String getLocalizedMessage() {
		String localizedMessage = EngineMessageBundle.getMessage(getLocalizationCode(), getLocale());
		return localizedMessage;
	}

	public Locale getLocale() {
		if (locale == null) {
			locale = Locale.US;
		}
		return locale;
	}

}
