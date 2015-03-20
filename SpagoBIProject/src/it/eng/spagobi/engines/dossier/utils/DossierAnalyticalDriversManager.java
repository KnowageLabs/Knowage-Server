/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.utils;

import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.dossier.bo.ConfiguredBIDocument;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

public class DossierAnalyticalDriversManager {

	static private Logger logger = Logger.getLogger(DossierAnalyticalDriversManager.class);
	
	public List<EMFValidationError> adjustRequiredAnalyticalDrivers(Integer dossierId, List docs) {
		logger.debug("IN");
		BIObject dossier = null;
		try {
			dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
			dossier.setBiObjectParameters(DAOFactory.getBIObjectDAO().getBIObjectParameters(dossier));
		} catch (EMFUserError e) {
			throw new RuntimeException("Cannot load details of dossier with id " + dossierId, e);
		}
		List<EMFValidationError> list = removeNotEmptyAnalyticalDrivers(dossier, docs);
		addEmptyAnalyticalDrivers(dossier, docs);
		logger.debug("OUT");
		return list;
	}

	private List<EMFValidationError> removeNotEmptyAnalyticalDrivers(BIObject dossier, List docs) {
		logger.debug("IN");
		List parameters = dossier.getBiObjectParameters();
		List<BIObjectParameter> biParametersToBeRemoved = new ArrayList<BIObjectParameter>();
		if (parameters != null && parameters.size() > 0) {
			HashMap<Integer, List<BIObjectParameter>> analyticalDrivers = getAnalyticalDriverCount(parameters);
			Set<Entry<Integer, List<BIObjectParameter>>> entries = analyticalDrivers.entrySet();
			Iterator<Entry<Integer, List<BIObjectParameter>>> it = entries.iterator();
			while (it.hasNext()) {
				Entry<Integer, List<BIObjectParameter>> entry = it.next();
				List<BIObjectParameter> biParameters = entry.getValue();
				int occurrences = biParameters.size();
				int emptyOccurences = getMaxEmptyOccurrences(biParameters.get(0), docs);
				int difference = occurrences - emptyOccurences;
				for (int i = 1; i <= difference; i++) {
					// remove last ones
					biParametersToBeRemoved.add(biParameters.get(biParameters.size() - i));
				}
			}
		}
		Iterator<BIObjectParameter> biParametersToBeRemovedIt = biParametersToBeRemoved.iterator();
		List<EMFValidationError> toReturn = new ArrayList<EMFValidationError>();
		while (biParametersToBeRemovedIt.hasNext()) {
			BIObjectParameter aBIObjectParameter = biParametersToBeRemovedIt.next();
			EMFValidationError error = removeAnalyticalDriver(aBIObjectParameter);
			if (error != null) {
				toReturn.add(error);
			}
		}
		updateBIObjectParameters(dossier);
		logger.debug("OUT");
		return toReturn;
	}
	
	
	private HashMap<Integer, List<BIObjectParameter>> getAnalyticalDriverCount(List parameters) {
		logger.debug("IN");
		HashMap<Integer, List<BIObjectParameter>> toReturn = new HashMap<Integer, List<BIObjectParameter>>();
		Iterator it = parameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter biParameter = (BIObjectParameter) it.next();
			Integer key = biParameter.getParID();
			List<BIObjectParameter> list =  toReturn.get(key);
			if (list == null) {
				list = new ArrayList<BIObjectParameter>();
			}
			list.add(biParameter);
			toReturn.put(key, list);
		}
		logger.debug("OUT");
		return toReturn;
	}

	private int getMaxEmptyOccurrences(BIObjectParameter biParameter, List docs) {
		int toReturn = 0;
		logger.debug("IN");
		Iterator it = docs.iterator();
		while (it.hasNext()) {
			ConfiguredBIDocument configuredBIDocument = (ConfiguredBIDocument) it.next();
			Map parameters = configuredBIDocument.getParameters();
			BIObject configuredDocument = configuredBIDocument.loadBIObjectDetails();
			List configuredDocumentParameters = configuredDocument.getBiObjectParameters();
			int emptyOccurrences = getAnalyticalDriverEmptyOccurrences(biParameter, configuredDocumentParameters, parameters);
			if (emptyOccurrences > toReturn) {
				toReturn = emptyOccurrences;
			}
		}
		logger.debug("OUT: " + toReturn);
		return toReturn;
	}

	private EMFValidationError removeAnalyticalDriver(BIObjectParameter aParameter) {
		logger.debug("IN");
		EMFValidationError error = null;
		try {
			error = DetailBIObjectModule.checkForDependancies(aParameter.getId());
			if (error == null) {
				DAOFactory.getBIObjectParameterDAO().eraseBIObjectParameter(aParameter, true);
			}
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot remove document parameter " + aParameter.getLabel(), e);
		}
		logger.debug("OUT");
		return error;
	}

	private void addEmptyAnalyticalDrivers(BIObject dossier, List docs) {
		logger.debug("IN");
		Iterator containedDocsIterator = docs.iterator();
		while (containedDocsIterator.hasNext()) {
			ConfiguredBIDocument configuredBIDocument = (ConfiguredBIDocument) containedDocsIterator.next();
			addEmptyAnalyticalDrivers(dossier, configuredBIDocument);
		}
		logger.debug("OUT");
	}

	private void addEmptyAnalyticalDrivers(BIObject dossier,
			ConfiguredBIDocument configuredBIDocument) {
		logger.debug("IN");
		Map parameters = configuredBIDocument.getParameters();
		BIObject configuredDocument = configuredBIDocument.loadBIObjectDetails();
		List configuredDocumentParameters = configuredDocument.getBiObjectParameters();
		Iterator configuredBIObjectParametersIt = configuredDocumentParameters.iterator();
		while (configuredBIObjectParametersIt.hasNext()) {
			BIObjectParameter biParameter = (BIObjectParameter) configuredBIObjectParametersIt.next();
			int emptyOccurrences = getAnalyticalDriverEmptyOccurrences(biParameter, configuredDocumentParameters, parameters);
			if (emptyOccurrences > 0) {
				int occurrenciesInDossier = getAnalyticalDriverOccurrenciesInDossier(dossier, biParameter.getParID());
				for (int c = occurrenciesInDossier; c < emptyOccurrences; c++) {
					BIObjectParameter parameterToBeAdded = getEmptyOccurrence(biParameter, configuredDocumentParameters, parameters, c);
					addBIObjectParameterToDossier(dossier, parameterToBeAdded);
				}
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Returns the number of occurrences of the same Analytical Driver in the document, i.e. the number of 
	 * BIObjectParameter that are related to the same Parameter object.
	 * 
	 * @param dossier The dossier document
	 * @param biParameter Th dossier's BIObjectParameter
	 * @return the number of occurrences of the same Analytical Driver in the document
	 */
	private int getAnalyticalDriverOccurrenciesInDossier(BIObject dossier,
			Integer parameterId) {
		int toReturn = 0;
		logger.debug("IN");
		List parameters = dossier.getBiObjectParameters();
		Iterator it = parameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter aParameter = (BIObjectParameter) it.next();
			if (aParameter.getParID().equals(parameterId)) {
				toReturn++;
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	/**
	 * Returns the number of empty occurrence of the Analytical Driver associated to the input BIObjectParameter
	 * in the contained document's parameters.
	 * 
	 * @param biParameter
	 * @param configuredDocumentParameters
	 * @param parameters
	 * @return
	 */
	private int getAnalyticalDriverEmptyOccurrences(BIObjectParameter biParameter,
			List configuredDocumentParameters, Map parameters) {
		int toReturn = 0;
		logger.debug("IN");
		Iterator it = configuredDocumentParameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter aBIObjectParameter = (BIObjectParameter) it.next();
			if (aBIObjectParameter.getParID().equals(biParameter.getParID()) && isEmpty(biParameter, parameters)) {
				toReturn++;
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private boolean isEmpty(BIObjectParameter biParameter, Map parameters) {
		String key = biParameter.getParameterUrlName();
		String value = (String) parameters.get(key);
		return value == null || value.trim().equals("");
	}

	private void addBIObjectParameterToDossier(BIObject dossier,
			BIObjectParameter parameterToBeAdded) {
		logger.debug("IN");
		IBIObjectParameterDAO objParDAO;
		try {
			objParDAO = DAOFactory.getBIObjectParameterDAO();
			BIObjectParameter objPar = new BIObjectParameter();
			objPar.setId(new Integer(-1));
			objPar.setBiObjectID(dossier.getId());
			objPar.setParID(parameterToBeAdded.getParID());
	        Parameter par = new Parameter();
	        par.setId(parameterToBeAdded.getParID());
	        objPar.setParameter(par);
	        objPar.setLabel(parameterToBeAdded.getLabel());
	        objPar.setParameterUrlName(parameterToBeAdded.getParameterUrlName());
	        objPar.setRequired(parameterToBeAdded.getRequired());
	        objPar.setModifiable(parameterToBeAdded.getModifiable());
	        objPar.setVisible(parameterToBeAdded.getVisible());
	        objPar.setMultivalue(parameterToBeAdded.getMultivalue());
	        List existingParameters = dossier.getBiObjectParameters();
	        int priority = existingParameters != null ? existingParameters.size() + 1 : 1;
	        objPar.setPriority(new Integer(priority)); 
			parameterToBeAdded.setId(new Integer(-1));
			objParDAO.insertBIObjectParameter(objPar);
		} catch (EMFUserError e) {
			throw new RuntimeException("Cannot save new parameter into dossier with label " + dossier.getLabel(), e);
		}
		updateBIObjectParameters(dossier);
		logger.debug("OUT");
	}

	private void updateBIObjectParameters(BIObject dossier) {
		logger.debug("IN");
		try {
			List parameters = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(dossier.getId());
			dossier.setBiObjectParameters(parameters);
		} catch (EMFUserError e) {
			throw new RuntimeException("Cannot reload parameters of dossier with label " + dossier.getLabel(), e);
		}
		logger.debug("OUT");
	}

	private BIObjectParameter getEmptyOccurrence(BIObjectParameter biParameter,
			List configuredDocumentParameters, Map parameters, int c) {
		BIObjectParameter toReturn = null;
		int counter = 0;
		logger.debug("IN");
		Iterator it = configuredDocumentParameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter aBIObjectParameter = (BIObjectParameter) it.next();
			if (aBIObjectParameter.getParID().equals(biParameter.getParID()) && isEmpty(biParameter, parameters)) {
				if (counter == c) {
					toReturn = aBIObjectParameter;
					break;
				} else {
					counter++;
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void fillEmptyAnalyticalDrivers(Map parameters, BIObject dossier, BIObject containedDocument) {
		logger.debug("IN");
		List dossierParameters = dossier.getBiObjectParameters();
		if (dossierParameters != null && dossierParameters.size() > 0) {
			Iterator it = dossierParameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter dossierParameter = (BIObjectParameter) it.next();
				List<BIObjectParameter> containedDocumentParameters = getRelevantContainedDocumentAnalyticalDrivers(dossierParameter, containedDocument);
				if (containedDocumentParameters != null && containedDocumentParameters.size() > 0) {
					Iterator<BIObjectParameter> containedDocumentParametersIt = containedDocumentParameters.iterator();
					while (containedDocumentParametersIt.hasNext()) {
						BIObjectParameter containedDocumentParameter = containedDocumentParametersIt.next();
						if (isEmpty(containedDocumentParameter, parameters)) {
							logger.debug("Updating parameters of document ["
									+ "label : " + containedDocument.getLabel()
									+ "name : " + containedDocument.getName()
									+ "]");
							fillEmptyAnalyticalDriver(dossierParameter, containedDocumentParameter, parameters);
							break;
						}
					}
				}
			}
		}
		logger.debug("OUT");
	}

	private void fillEmptyAnalyticalDriver(
			BIObjectParameter dossierParameter, BIObjectParameter containedDocumentParameter, Map parameters) {
		logger.debug("IN");
		String value = dossierParameter.getParameterValuesAsString();
		String key = containedDocumentParameter.getParameterUrlName();
		logger.debug("Updating value of parameter [" + 
				"label : " + containedDocumentParameter.getLabel() + 
				"urlName : " + containedDocumentParameter.getParameterUrlName() + 
				"] to : " + value);
		parameters.put(key, value);
		logger.debug("OUT");
	}

	private List<BIObjectParameter> getRelevantContainedDocumentAnalyticalDrivers(
			BIObjectParameter dossierParameter, BIObject containedDocument) {
		List<BIObjectParameter> toReturn = new ArrayList<BIObjectParameter>();
		logger.debug("IN");
		List containedDocumentParameters = containedDocument.getBiObjectParameters();
		if (containedDocumentParameters != null && containedDocumentParameters.size() > 0) {
			Iterator it = containedDocumentParameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter containedDocumentParameter = (BIObjectParameter) it.next();
				if (containedDocumentParameter.getParID().equals(dossierParameter.getParID())) {
					toReturn.add(containedDocumentParameter);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	
}