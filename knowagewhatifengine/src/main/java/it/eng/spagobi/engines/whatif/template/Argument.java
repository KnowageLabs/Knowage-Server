/**
 *
 */
package it.eng.spagobi.engines.whatif.template;

/**
 * @author Dragan Pirkovic
 *
 */
public class Argument {
	private String defaultValue;

	/**
	 * @param defaultValue
	 */
	public Argument(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
