package it.eng.spagobi.engines.drivers;

public class DefaultOutputParameter {

	public static enum TYPE {
		String, Number, Date
	};

	private TYPE paramType;
	private String paramName;

	public DefaultOutputParameter(String paramName, TYPE paramType) {
		this.paramName = paramName;
		this.paramType = paramType;
	}

	/**
	 * @return the paramType
	 */
	public TYPE getParamType() {
		return paramType;
	}

	/**
	 * @param paramType
	 *            the paramType to set
	 */
	public void setParamType(TYPE paramType) {
		this.paramType = paramType;
	}

	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName
	 *            the paramName to set
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

}
