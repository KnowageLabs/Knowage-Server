/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.objects.Couple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class AbstractLOV implements ILovDetail {

	private static transient Logger logger = Logger.getLogger(AbstractLOV.class);

	@Override
	public boolean isSimpleLovType() {
		return this.getLovType() == null || this.getLovType().equalsIgnoreCase("simple");
	}

	protected List<String> getTreeValueColumns() {
		try {
			List<Couple<String, String>> list = this.getTreeLevelsColumns();
			List<String> toReturn = new ArrayList<String>();
			if (list != null) {
				Iterator<Couple<String, String>> it = list.iterator();
				while (it.hasNext()) {
					toReturn.add(it.next().getFirst());
				}
			}
			return toReturn;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting tree value columns", e);
		}
	}

	protected List<String> getTreeDescriptionColumns() {
		try {
			List<Couple<String, String>> list = this.getTreeLevelsColumns();
			List<String> toReturn = new ArrayList<String>();
			if (list != null) {
				Iterator<Couple<String, String>> it = list.iterator();
				while (it.hasNext()) {
					toReturn.add(it.next().getSecond());
				}
			}
			return toReturn;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting tree value columns", e);
		}
	}

	public Map<String, String> getParametersNameToValueMap(List<BIObjectParameter> biObjectParameters) throws Exception {
		Map<String, String> parameters = null;
		Set<String> parameterNames = getParameterNames();
		if (parameterNames != null && !parameterNames.isEmpty()) {
			if (biObjectParameters != null && !biObjectParameters.isEmpty()) {
				parameters = new HashMap<String, String>(parameterNames.size());
				int numberOfParametersFound = 0;
				for (BIObjectParameter objParam : biObjectParameters) {
					if (parameterNames.contains(objParam.getParameter().getLabel())) {
						logger.debug("Found parameter with name [" + objParam.getParameter().getLabel() + "], also used by the LOV.");
						numberOfParametersFound++;
						String parameterValue = null;
						List<String> parameterValues = objParam.getParameterValues();
						if (parameterValues != null && !parameterValues.isEmpty()) {
							if (parameterValues.size() == 1) {
								parameterValue = StringUtilities.getSingleValue(parameterValues.get(0), objParam.getParameter().getType());
							} else {
								// build the value as multivalue
								parameterValue = StringUtilities.getMultiValue(SpagoBIUtilities.fromListToString(parameterValues, ","), objParam.getParameter()
										.getType());
							}
							logger.debug("The parameter with name [" + objParam.getParameter().getLabel() + "] has value [" + parameterValue + "]");
							parameters.put(objParam.getParameter().getLabel(), parameterValue);
						} else {
							logger.error("The parameter with name [" + objParam.getParameter().getLabel() + "] is null");
							throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
						}
					}
				}
				if (numberOfParametersFound != parameterNames.size()) {
					logger.error("The LOV needs " + parameterNames.size()
							+ ", but the parameters from the document execution do not match all the LOV parameters.");
					throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
				}
			} else {
				logger.error("The LOV needs " + parameterNames.size() + ", but the set of parameter from the document execution is null or empty.");
				throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
			}
		}
		return parameters;
	}

	public Map<String, String> getParametersNameToTypeMap(List<BIObjectParameter> biObjectParameters) throws Exception {
		Map<String, String> parameters = null;
		Set<String> parameterNames = getParameterNames();
		if (parameterNames != null && !parameterNames.isEmpty()) {
			if (biObjectParameters != null && !biObjectParameters.isEmpty()) {
				parameters = new HashMap<String, String>(parameterNames.size());
				int numberOfParametersFound = 0;
				for (BIObjectParameter objParam : biObjectParameters) {
					if (parameterNames.contains(objParam.getParameter().getLabel())) {
						String parameterType = objParam.getParameter().getType();
						logger.debug("Found parameter with name [" + objParam.getParameter().getLabel() + "], also used by the LOV.");
						numberOfParametersFound++;
						if (parameterType != null) {
							logger.debug("The parameter with name [" + objParam.getParameter().getLabel() + "] has value [" + parameterType + "]");
							parameters.put(objParam.getParameter().getLabel(), parameterType);
						} else {
							logger.error("The parameter with name [" + objParam.getParameter().getLabel() + "] is null");
							throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
						}
					}
				}
				if (numberOfParametersFound != parameterNames.size()) {
					logger.error("The LOV needs " + parameterNames.size()
							+ ", but the parameters from the document execution do not match all the LOV parameters.");
					throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
				}
			} else {
				logger.error("The LOV needs " + parameterNames.size() + ", but the set of parameter from the document execution is null or empty.");
				throw new SpagoBIRuntimeException("Impossible to retrieve all the parameters value for the LOV.");
			}
		}
		return parameters;
	}

}