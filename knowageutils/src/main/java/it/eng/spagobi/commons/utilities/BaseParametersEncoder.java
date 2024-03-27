package it.eng.spagobi.commons.utilities;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseParametersEncoder {

	private static final Logger LOGGER = LogManager.getLogger(BaseParametersEncoder.class);

	public static final String DEFAULT_SEPARATOR = ";";
	public static final String DEFAULT_OPEN_BLOCK_MARKER = "{";
	public static final String DEFAULT_CLOSE_BLOCK_MARKER = "}";

	private String separator;
	private String openBlockMarker;
	private String closeBlockMarker;

	/**
	 * Instantiates a new parameter values encoder.
	 */
	public BaseParametersEncoder() {
		this(DEFAULT_SEPARATOR, DEFAULT_OPEN_BLOCK_MARKER, DEFAULT_CLOSE_BLOCK_MARKER);
	}

	/**
	 * Instantiates a new parameter values encoder.
	 *
	 * @param separator        the separator
	 * @param openBlockMarker  the open block marker
	 * @param closeBlockMarker the close block marker
	 */
	public BaseParametersEncoder(String separator, String openBlockMarker, String closeBlockMarker) {
		this.separator = separator;
		this.openBlockMarker = openBlockMarker;
		this.closeBlockMarker = closeBlockMarker;
	}

	/**
	 * Gets the close block marker.
	 *
	 * @return the close block marker
	 */
	public String getCloseBlockMarker() {
		return closeBlockMarker;
	}

	/**
	 * Sets the close block marker.
	 *
	 * @param closeBlockMarker the new close block marker
	 */
	public void setCloseBlockMarker(String closeBlockMarker) {
		this.closeBlockMarker = closeBlockMarker;
	}

	/**
	 * Gets the open block marker.
	 *
	 * @return the open block marker
	 */
	public String getOpenBlockMarker() {
		return openBlockMarker;
	}

	/**
	 * Sets the open block marker.
	 *
	 * @param openBlockMarker the new open block marker
	 */
	public void setOpenBlockMarker(String openBlockMarker) {
		this.openBlockMarker = openBlockMarker;
	}

	/**
	 * Gets the separator.
	 *
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * Sets the separator.
	 *
	 * @param separator the new separator
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String encodeValuesFromListOfStrings(boolean multivalue, List<String> parameterValues, String type) {
		if (!multivalue) {
			return parameterValues.get(0);
		} else {
			return encodeMultivaluesParam(parameterValues, type);
		}
	}

	public String encodeDescriptionFromListOfStrings(List<String> values) {
		if (values != null && !values.isEmpty()) {
			if (values.size() == 1)
				return values.get(0);
			else
				return encodeMultivalueParamsDesciption(values);
		} else
			return "";
	}

	protected final String encodeMultivalueParamsDesciption(List<String> values) {
		LOGGER.debug("Encode multivalue parameter descriptions from list {}", values);
		StringBuilder ret = new StringBuilder("");

		if (values == null || values.isEmpty())
			return ret.toString();

		for (int i = 0; i < values.size(); i++) {
			String valueToBeAppended = (values.get(i) == null) ? "" : values.get(i);
			ret.append((i > 0) ? separator : "");
			ret.append(valueToBeAppended);
		}

		LOGGER.debug("End encoding multivalue parameter descriptions from list {} returning {}", values, ret);
		return ret.toString();
	}

	/**
	 * Multi values parameters are encoded in the following way: openBlockMarker + separator + openBlockMarker + [values separated by the separator] +
	 * closeBlockMarker + parameterType + closeBlockMarker Examples: {,{string1,string2,string3}STRING} {,{number1,number1,number1}NUM}
	 *
	 * parameterType: the type of the parameter (NUM/STRING/DATE)
	 */
	protected final String encodeMultivaluesParam(List<String> values, String parameterType) {
		LOGGER.debug("Encode multivalue parameter values from list {} and type {}", values, parameterType);
		StringBuilder value = new StringBuilder("");

		if (values == null || values.isEmpty())
			return value.toString();

		value.append(openBlockMarker);
		value.append(separator);
		value.append(openBlockMarker);
		for (int i = 0; i < values.size(); i++) {
			String valueToBeAppended = (values.get(i) == null) ? "" : values.get(i);
			value.append((i > 0) ? separator : "");
			value.append(valueToBeAppended);
		}
		value.append(closeBlockMarker);
		value.append(parameterType);
		value.append(closeBlockMarker);
		LOGGER.debug("End encoding multivalue parameter values from list {} and type {} returning {}", values,
				parameterType, value);
		return value.toString();
	}

}