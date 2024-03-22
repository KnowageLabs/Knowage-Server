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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 *             parameters is currently copy-pasted in many places. Where possible use {@link BaseParametersEncoder}.
 */
@Deprecated
public class ParameterValuesEncoder extends BaseParametersEncoder {

	private static final Logger LOGGER = LogManager.getLogger(ParameterValuesEncoder.class);

	/**
	 * Encode.
	 *
	 * @param biobjPar the biobj par
	 *
	 * @return the string
	 */
	public String encode(BIObjectParameter biobjPar) {
		LOGGER.debug("IN");

		List<String> parameterValues = biobjPar.getParameterValues();
		if (parameterValues == null) {
			LOGGER.debug("biobjPar.getParameterValues() == null");
			return null;
		}

		if (parameterValues.isEmpty()) {
			return "";
		}

		Parameter parameter = biobjPar.getParameter();
		if (parameter == null) {
			Integer parId = biobjPar.getParID();
			if (parId == null) {
				LOGGER.warn(
						"Parameter object nor parameter id are set into BIObjectParameter with label = {} of document with id = {}",
						biobjPar.getLabel(), biobjPar.getBiObjectID());
			} else {
				try {
					parameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
				} catch (EMFUserError e) {
					LOGGER.warn("Error loading parameter with id = {}", parId);
				}
			}
		}

		if (parameter == null) {
			LOGGER.error("Unable to load parameter from BIObjectParameter with label = {} of document with id = {}",
					biobjPar.getLabel(), biobjPar.getBiObjectID());
			return null;
		}

		String type = parameter.getType();
		boolean multivalue = biobjPar.isMultivalue();

		ModalitiesValue modValue = parameter.getModalityValue();
		if (modValue != null) {
			String typeCode = modValue.getITypeCd();
			LOGGER.debug("typeCode={}", typeCode);

			if (SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE.equalsIgnoreCase(typeCode)) {
				multivalue = false;
			}
		}

		return encodeValuesFromListOfStrings(multivalue, parameterValues, type);
	}

	/**
	 * Get the description of a BIObjectParameter and encode it's description.. In this way we create a new parameter with the description of the parameter to pass
	 * at the engine
	 *
	 * @param biobjPar the parameter
	 * @return a string with the encoded description
	 */
	public String encodeDescription(BIObjectParameter biobjPar) {
		LOGGER.debug("IN");
		if (biobjPar.getParameterValues() == null) {
			LOGGER.debug("biobjPar.getParameterValues() == null");
			return null;
		}

		Parameter parameter = biobjPar.getParameter();
		if (parameter != null) {

			List<String> parameterValuesDescription = biobjPar.getParameterValuesDescription();
			if (parameterValuesDescription == null) {
				return "";
			}

			ModalitiesValue modValue = parameter.getModalityValue();
			if (modValue != null) {
				boolean mult = biobjPar.isMultivalue();

				String typeCode = biobjPar.getParameter().getModalityValue().getITypeCd();
				LOGGER.debug("typeCode={}", typeCode);
				if (typeCode.equalsIgnoreCase(SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE)) {
					mult = false;
				}

				if (!mult) {
					return parameterValuesDescription.get(0);
				} else {
					return encodeMultivalueParamsDesciption(parameterValuesDescription);
				}
			} else {
				return encodeDescriptionFromBIObjectParameter(biobjPar);
			}
		} else {
			Integer parId = biobjPar.getParID();
			String type = null;
			if (parId == null) {
				LOGGER.warn(
						"Parameter object nor parameter id are set into BiObjectPrameter with label = {} of document with id = {}",
						biobjPar.getLabel(), biobjPar.getBiObjectID());
			} else {
				try {
					Parameter aParameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
					type = aParameter.getType();
				} catch (EMFUserError e) {
					LOGGER.warn("Error loading parameter with id = {}", biobjPar.getParID());
				}
			}
			return encodeDescriptionFromBIObjectParameter(biobjPar);
		}

	}

	private String encodeDescriptionFromBIObjectParameter(BIObjectParameter biobjPar) {
		List<String> values = biobjPar.getParameterValuesDescription();
		return encodeDescriptionFromListOfStrings(values);
	}

}
