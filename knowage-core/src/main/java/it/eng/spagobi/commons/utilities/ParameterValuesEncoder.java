/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.commons.utilities;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;

/**
 * @author Gioia
 * @deprecated Too much coupling: this class should be used by all the WARs which need it but it's not movable outside knowage-core because of the coupling with
 *             {@link DAOFactory}, {@link BIObjectParameter} and {@link Parameter}; we need to reduce the coupling because the code to encode multivalue
 *             parameters is currently copy-pasted in many places.
 */
@Deprecated
public class ParameterValuesEncoder {

	private static Logger logger = Logger.getLogger(ParameterValuesEncoder.class);
	private String separator;
	private String openBlockMarker;
	private String closeBlockMarker;

	public static final String DEFAULT_SEPARATOR = ";";
	public static final String DEFAULT_OPEN_BLOCK_MARKER = "{";
	public static final String DEFAULT_CLOSE_BLOCK_MARKER = "}";

	// ///////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// ///////////////////////////////////////////////////////////

	/**
	 * Instantiates a new parameter values encoder.
	 */
	public ParameterValuesEncoder() {
		this(DEFAULT_SEPARATOR, DEFAULT_OPEN_BLOCK_MARKER, DEFAULT_CLOSE_BLOCK_MARKER);
	}

	/**
	 * Instantiates a new parameter values encoder.
	 *
	 * @param separator        the separator
	 * @param openBlockMarker  the open block marker
	 * @param closeBlockMarker the close block marker
	 */
	public ParameterValuesEncoder(String separator, String openBlockMarker, String closeBlockMarker) {
		this.separator = separator;
		this.openBlockMarker = openBlockMarker;
		this.closeBlockMarker = closeBlockMarker;
	}

	// ///////////////////////////////////////////////////////////
	// ACCESS METHODS
	// ///////////////////////////////////////////////////////////

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

	// ///////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// ///////////////////////////////////////////////////////////

	/**
	 * Encode.
	 *
	 * @param biobjPar the biobj par
	 *
	 * @return the string
	 */
	public String encode(BIObjectParameter biobjPar) {
		logger.debug("IN");

		List parameterValues = biobjPar.getParameterValues();
		if (parameterValues == null) {
			logger.debug("biobjPar.getParameterValues() == null");
			return null;
		}

		if (parameterValues.isEmpty()) {
			return "";
		}

		Parameter parameter = biobjPar.getParameter();
		if (parameter == null) {
			Integer parId = biobjPar.getParID();
			if (parId == null) {
				logger.warn("Parameter object nor parameter id are set into BIObjectParameter with label = "
						+ biobjPar.getLabel() + " of document with id = " + biobjPar.getBiObjectID());
			} else {
				try {
					parameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
				} catch (EMFUserError e) {
					logger.warn("Error loading parameter with id = " + parId);
				}
			}
		}

		if (parameter == null) {
			logger.error("Unable to load parameter from BIObjectParameter with label = " + biobjPar.getLabel()
					+ " of document with id = " + +biobjPar.getBiObjectID());
			return null;
		}

		String type = parameter.getType();
		boolean multivalue = biobjPar.isMultivalue();

		ModalitiesValue modValue = parameter.getModalityValue();
		if (modValue != null) {
			String typeCode = modValue.getITypeCd();
			logger.debug("typeCode=" + typeCode);

			if (SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE.equalsIgnoreCase(typeCode)) {
				multivalue = false;
			}
		}

		if (!multivalue) {
			return (String) parameterValues.get(0);
		} else {
			return encodeMultivaluesParam(parameterValues, type);
		}
	}

	/**
	 * Get the description of a BIObjectParameter and encode it's description.. In this way we create a new parameter with the description of the parameter to pass
	 * at the engine
	 *
	 * @param biobjPar the parameter
	 * @return a string with the encoded description
	 */
	public String encodeDescription(BIObjectParameter biobjPar) {
		logger.debug("IN");
		if (biobjPar.getParameterValues() == null) {
			logger.debug("biobjPar.getParameterValues() == null");
			return null;
		}

		Parameter parameter = biobjPar.getParameter();
		if (parameter != null) {

			if (biobjPar.getParameterValuesDescription() == null) {
				return "";
			}

			ModalitiesValue modValue = parameter.getModalityValue();
			if (modValue != null) {
				boolean mult = biobjPar.isMultivalue();

				String typeCode = biobjPar.getParameter().getModalityValue().getITypeCd();
				logger.debug("typeCode=" + typeCode);
				if (typeCode.equalsIgnoreCase(SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE)) {
					mult = false;
				}

				if (!mult) {
					return (String) biobjPar.getParameterValuesDescription().get(0);
				} else {
					return encodeMultivalueParamsDesciption(biobjPar.getParameterValuesDescription());
				}
			} else {
				List values = biobjPar.getParameterValuesDescription();
				if (values != null && !values.isEmpty()) {
					if (values.size() == 1)
						return (String) biobjPar.getParameterValuesDescription().get(0);
					else
						return encodeMultivalueParamsDesciption(biobjPar.getParameterValuesDescription());
				} else
					return "";
			}
		} else {
			Integer parId = biobjPar.getParID();
			String type = null;
			if (parId == null) {
				logger.warn("Parameter object nor parameter id are set into BiObjectPrameter with label = "
						+ biobjPar.getLabel() + " of document with id = " + biobjPar.getBiObjectID());
			} else {
				try {
					Parameter aParameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
					type = aParameter.getType();
				} catch (EMFUserError e) {
					logger.warn("Error loading parameter with id = " + biobjPar.getParID());
				}
			}
			List values = biobjPar.getParameterValuesDescription();
			if (values != null && !values.isEmpty()) {
				if (values.size() == 1)
					return (String) biobjPar.getParameterValuesDescription().get(0);
				else
					return encodeMultivalueParamsDesciption(biobjPar.getParameterValuesDescription());
			} else
				return "";
		}

	}

	// ///////////////////////////////////////////////////////////
	// UTILITY METHODS
	// ///////////////////////////////////////////////////////////

	/**
	 * Multi values parameters are encoded in the following way: openBlockMarker + separator + openBlockMarker + [values separated by the separator] +
	 * closeBlockMarker + parameterType + closeBlockMarker Examples: {,{string1,string2,string3}STRING} {,{number1,number1,number1}NUM}
	 *
	 * parameterType: the type of the parameter (NUM/STRING/DATE)
	 */
	private String encodeMultivaluesParam(List values, String parameterType) {
		logger.debug("IN");
		StringBuilder value = new StringBuilder("");

		if (values == null || values.isEmpty())
			return value.toString();

		value.append(openBlockMarker);
		value.append(separator);
		value.append(openBlockMarker);
		for (int i = 0; i < values.size(); i++) {
			String valueToBeAppended = (values.get(i) == null) ? "" : (String) values.get(i);
			value.append((i > 0) ? separator : "");
			value.append(valueToBeAppended);
		}
		value.append(closeBlockMarker);
		value.append(parameterType);
		value.append(closeBlockMarker);
		logger.debug("IN.value=" + value);
		return value.toString();
	}

	private String encodeMultivalueParamsDesciption(List values) {
		logger.debug("IN");
		StringBuilder value = new StringBuilder("");

		if (values == null || values.isEmpty())
			return value.toString();

		for (int i = 0; i < values.size(); i++) {
			String valueToBeAppended = (values.get(i) == null) ? "" : (String) values.get(i);
			value.append((i > 0) ? separator : "");
			value.append(valueToBeAppended);
		}

		logger.debug("IN.value=" + value);
		return value.toString();
	}
}
