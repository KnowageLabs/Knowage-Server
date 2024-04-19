package it.eng.spagobi.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseParametersDecoder {

	private static final Logger LOGGER = LogManager.getLogger(BaseParametersDecoder.class);

	public static final String DEFAULT_OPEN_BLOCK_MARKER = "{";
	public static final String DEFAULT_CLOSE_BLOCK_MARKER = "}";

	private String openBlockMarker;
	private String closeBlockMarker;
	private final String multiValueRegex;

	/**
	 * Instantiates a new parameters decoder.
	 */
	public BaseParametersDecoder() {
		this(DEFAULT_OPEN_BLOCK_MARKER, DEFAULT_CLOSE_BLOCK_MARKER);
	}

	/**
	 * Instantiates a new parameters decoder.
	 *
	 * @param openBlockMarker  the open block marker
	 * @param closeBlockMarker the close block marker
	 */
	public BaseParametersDecoder(String openBlockMarker, String closeBlockMarker) {
		this.openBlockMarker = openBlockMarker;
		this.closeBlockMarker = closeBlockMarker;
		this.multiValueRegex = String.format("%s.%s.+%s.+%s", Pattern.quote(openBlockMarker),
				Pattern.quote(openBlockMarker), Pattern.quote(closeBlockMarker), Pattern.quote(closeBlockMarker));
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
	 * Checks if is multi values.
	 *
	 * @param value the value
	 *
	 * @return true, if is multi values
	 */
	public boolean isMultiValues(String value) {
		return value.trim().matches(multiValueRegex);
	}

	/**
	 * Decode.
	 *
	 * @param value the value
	 *
	 * @return the list
	 */
	public List<String> decode(String value) {
		LOGGER.debug("IN: value = {}", value);
		List<String> values = null;

		if (value == null)
			return null;

		if (isMultiValues(value)) {
			values = new ArrayList<>();
			String separator = getSeparator(value);
			String innerBlock = getInnerBlock(value);
			String parameterType = getParameterType(value);
			String[] chunks = innerBlock.split(separator);
			for (int i = 0; i < chunks.length; i++) {
				String singleValue = chunks[i];
				if (parameterType.equalsIgnoreCase("STRING")) {
					LOGGER.debug("Single string value = [{}]", singleValue);
					singleValue = singleValue.replace("'", "''");
					LOGGER.debug("After single quotes (') escape, single string value is = [{}]", singleValue);
					LOGGER.debug("Adding quotes to parameter value ... ");
					singleValue = "'" + singleValue + "'";
					LOGGER.debug("Final single string value is = [{}]", singleValue);
					/*
					 * if (singleValue.trim().equals("")) { LOGGER.debug("Adding quotes to parameter value ... "); singleValue = "'" + singleValue + "'"; } else { if
					 * (!singleValue.startsWith("'") && !singleValue.endsWith("'")) { LOGGER.debug("Adding quotes to parameter value ... "); singleValue = "'" + singleValue + "'";
					 * } }
					 */
				}
				values.add(singleValue);
			}
		} else {
			values = new ArrayList<>();
			values.add(value);
		}

		LOGGER.debug("OUT: list of values = {}", (values == null ? null : values.toString()));
		return values;
	}

	protected final String getSeparator(String value) {
		LOGGER.debug("IN: value = {}", value);
		String separator = null;

		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);
		separator = value.substring(outerBlockOpeningIndex + 1, innerBlockOpeningIndex).trim();

		LOGGER.debug("OUT: separator = {}", separator);
		return separator;
	}

	protected final String getInnerBlock(String value) {
		LOGGER.debug("IN: value = {}", value);
		String innerBlock = null;

		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);
		int innerBlockClosingIndex = value.trim().indexOf(closeBlockMarker, innerBlockOpeningIndex + 1);
		innerBlock = value.substring(innerBlockOpeningIndex + 1, innerBlockClosingIndex).trim();

		LOGGER.debug("OUT: innerBlock = {}", innerBlock);
		return innerBlock;
	}

	protected final String getParameterType(String value) {
		LOGGER.debug("IN: value = {}", value);
		String parameterType = null;

		int innerBlockClosingIndex = value.trim().indexOf(closeBlockMarker);
		int outerBlockClosingIndex = value.trim().indexOf(closeBlockMarker, innerBlockClosingIndex + 1);
		parameterType = value.substring(innerBlockClosingIndex + 1, outerBlockClosingIndex).trim();

		LOGGER.debug("OUT: parameterType = {}", parameterType);
		return parameterType;
	}

}