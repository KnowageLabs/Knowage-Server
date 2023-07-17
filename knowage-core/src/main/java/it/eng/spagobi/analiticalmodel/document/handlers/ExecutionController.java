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
package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spago.util.JavaScript;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

public class ExecutionController {

	private static Logger logger = Logger.getLogger(ExecutionController.class);

	private BIObject biObject = null;

	/**
	 * Check if the document can be executed (all parameters must be filled). It is used in scheduler (see {@link ExecuteBDocumentJob}).
	 *
	 * @return true, if successful
	 */
	public boolean directExecution() {

		if (biObject == null)
			return false;

		List<BIObjectParameter> biParameters = biObject.getDrivers();
		if (biParameters == null)
			return false;
		if (biParameters.isEmpty())
			return true;

		int countHidePar = 0;
		Iterator<BIObjectParameter> iterPars = biParameters.iterator();

		BIObjectParameter biParameter = null;

		while (iterPars.hasNext()) {
			biParameter = iterPars.next();
			Parameter par = biParameter.getParameter();

			if (biParameter.isTransientParmeters()) {
				countHidePar++;
				continue;
			}

			if (biParameter.hasValidValues()) {
				countHidePar++;
				continue;
			}

			if (par == null) {
				SpagoBITracer.major(ObjectsTreeConstants.NAME_MODULE, "ExecuteBIObjectMOdule", "directExecution", "The biparameter with label = ['"
						+ biParameter.getLabel() + "'] and url name = ['" + biParameter.getParameterUrlName() + "'] has no parameter associated. ");
			}

		}

		return countHidePar == biParameters.size();
	}

	/**
	 * Refresh parameters.
	 *
	 * @param obj                       the obj
	 * @param userProvidedParametersStr the user provided parameters str
	 */
	public void refreshParameters(BIObject obj, String userProvidedParametersStr) {
		if (userProvidedParametersStr != null) {
			List<BIObjectParameter> biparameters = obj.getDrivers();
			if (biparameters == null) {
				try {
					IBIObjectParameterDAO pardao = DAOFactory.getBIObjectParameterDAO();
					biparameters = pardao.loadBIObjectParametersById(obj.getId());
				} catch (Exception e) {
					SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "refreshParameters",
							"Error while loading biparameters of the biobject with id " + obj.getId());
					return;
				}
			}
			userProvidedParametersStr = JavaScript.unescape(userProvidedParametersStr);
			String[] userProvidedParameters = userProvidedParametersStr.split("&");
			for (int i = 0; i < userProvidedParameters.length; i++) {
				String[] chunks = userProvidedParameters[i].split("=");
				if (chunks == null || chunks.length > 2) {
					SpagoBITracer.warning(ObjectsTreeConstants.NAME_MODULE, this.getClass().getName(), "refreshParameters", "User provided parameter ["
							+ userProvidedParameters[i] + "] cannot be splitted in " + "[parameter url name=parameter value] by '=' characters.");
					continue;
				}
				String parUrlName = chunks[0];
				if (parUrlName == null || parUrlName.trim().equals(""))
					continue;

				String value = "";
				// if the user specified the parameter value it is considered, elsewhere an empty String is considered
				if (chunks.length == 2) {
					value = chunks[1];
				}

				if (parUrlName.endsWith("_field_visible_description")) {
					parUrlName = parUrlName.substring(0, parUrlName.indexOf("_field_visible_description"));
					setBIObjectParameterDescriptions(biparameters, parUrlName, value);
				} else {
					setBIObjectParameterValues(biparameters, parUrlName, value);
				}
			}
			obj.setDrivers(biparameters);
		}
	}

	private void setBIObjectParameterDescriptions(List<BIObjectParameter> biparameters, String parUrlName, String parDescriptionsEconded) {
		BIObjectParameter biparameter = getBIObjectParameter(biparameters, parUrlName);
		if (biparameter == null) {
			SpagoBITracer.info(ObjectsTreeConstants.NAME_MODULE, this.getClass().getName(), "refreshParameters",
					"No BIObjectParameter with url name = ['" + parUrlName + "'] was found.");
			return;
		}
		if (parDescriptionsEconded != null && parDescriptionsEconded.equalsIgnoreCase("NULL")) {
			biparameter.setParameterValuesDescription(null);
		} else {
			if (parDescriptionsEconded.startsWith("ITERATE:{")) {
				biparameter.setIterative(true);
				parDescriptionsEconded = parDescriptionsEconded.substring("ITERATE:{".length(), parDescriptionsEconded.length() - 1);
			} else {
				biparameter.setIterative(false);
			}
			String[] descriptions = parDescriptionsEconded.split(";");
			List<String> parameterDescriptions = Arrays.asList(descriptions);
			biparameter.setParameterValuesDescription(parameterDescriptions);
		}
		biparameter.setTransientParmeters(true);
	}

	private void setBIObjectParameterValues(List<BIObjectParameter> biparameters, String parUrlName, String parValuesEnconded) {
		BIObjectParameter biparameter = getBIObjectParameter(biparameters, parUrlName);
		if (biparameter == null) {
			SpagoBITracer.info(ObjectsTreeConstants.NAME_MODULE, this.getClass().getName(), "refreshParameters",
					"No BIObjectParameter with url name = ['" + parUrlName + "'] was found.");
			return;
		}
		if (parValuesEnconded != null && parValuesEnconded.equalsIgnoreCase("NULL")) {
			biparameter.setParameterValues(null);
		} else {
			if (parValuesEnconded.startsWith("ITERATE:{")) {
				biparameter.setIterative(true);
				parValuesEnconded = parValuesEnconded.substring("ITERATE:{".length(), parValuesEnconded.length() - 1);
			} else {
				biparameter.setIterative(false);
			}
			String[] values = parValuesEnconded.split(";");
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].trim();
			}
			List<String> parameterValues = Arrays.asList(values);
			biparameter.setParameterValues(parameterValues);
		}
		biparameter.setTransientParmeters(true);
	}

	private BIObjectParameter getBIObjectParameter(List<BIObjectParameter> biparameters, String parUrlName) {
		BIObjectParameter biparameter = null;
		Iterator<BIObjectParameter> it = biparameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter temp = it.next();
			if (temp.getParameterUrlName().equals(parUrlName)) {
				biparameter = temp;
				break;
			}
		}
		return biparameter;
	}

	/**
	 * Gets the bi object.
	 *
	 * @return the bi object
	 */
	public BIObject getBiObject() {
		return biObject;
	}

	/**
	 * Sets the bi object.
	 *
	 * @param biObject the new bi object
	 */
	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	/**
	 * Refresh parameters.
	 *
	 * @param biobj    the biobj
	 * @param confPars the conf pars
	 *
	 * @throws Exception the exception
	 */
	public void refreshParameters(BIObject biobj, Map confPars) throws Exception {
		logger.debug("IN");
		try {
			// load the list of parameter of the biobject
			IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
			List<BIObjectParameter> params = biobjpardao.loadBIObjectParametersById(biobj.getId());
			logger.debug("biobject parameter list " + params);
			// for each parameter set the configured value
			Iterator<BIObjectParameter> iterParams = params.iterator();
			while (iterParams.hasNext()) {
				BIObjectParameter par = iterParams.next();
				String parUrlName = par.getParameterUrlName();
				logger.debug("processing biparameter with url name " + parUrlName);
				String value = (String) confPars.get(parUrlName);
				logger.debug("usign " + value + " as value for the parameter");
				if (value != null) {
					List<String> values = new ArrayList<>();
					values.add(value);
					par.setParameterValues(values);
					logger.debug("parameter value set");
				}
			}
			// set the parameters into the biobject
			biobj.setDrivers(params);
		} finally {
			logger.debug("OUT");
		}
	}

}
