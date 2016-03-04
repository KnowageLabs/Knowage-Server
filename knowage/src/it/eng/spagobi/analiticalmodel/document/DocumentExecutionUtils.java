package it.eng.spagobi.analiticalmodel.document;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentParameters;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentExecutionUtils {
	public static final String PARAMETERS = "PARAMETERS";
	public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";
	
	public static ILovDetail getLovDetail(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValue();
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov detail associated to input BIObjectParameter", e);
		}
		return lovProvDet;
	}
	
	public static List<ObjParuse> getDependencies(String executionRole, BIObjectParameter parameter) {
		List<ObjParuse> biParameterExecDependencies = new ArrayList<ObjParuse>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(parameter.getParID(), executionRole);
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			biParameterExecDependencies.addAll(objParuseDAO.loadObjParuse(parameter.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}
	

	/**
	 * This method finds out the cache to be used for lov's result cache. This key is composed mainly by the user identifier and the lov definition. Note that,
	 * in case when the lov is a query and there is correlation, the executed statement if different from the original query (since correlation expression is
	 * injected inside SQL query using in-line view construct), therefore we should consider the modified query.
	 *
	 * @param profile
	 *            The user profile
	 * @param lovDefinition
	 *            The lov original definition
	 * @param dependencies
	 *            The dependencies to be considered (if any)
	 * @param executionInstance
	 *            The execution instance (it may be null, since a lov can be executed outside an execution instance context)
	 * @return The key to be used in cache
	 */
	private String getCacheKey(IEngUserProfile profile, ILovDetail lovDefinition, List<ObjParuse> dependencies, ExecutionInstance executionInstance) {
		String toReturn = null;
		String userID = (String) ((UserProfile) profile).getUserId();
		if (lovDefinition instanceof QueryDetail) {
			QueryDetail queryDetail = (QueryDetail) lovDefinition;
			QueryDetail clone = queryDetail.clone();
			clone.setQueryDefinition(queryDetail.getWrappedStatement(dependencies, executionInstance.getBIObject().getBiObjectParameters()));
			toReturn = userID + ";" + clone.toXML();
		} else {
			toReturn = userID + ";" + lovDefinition.toXML();
		}
		return toReturn;
	}

	public static List<DocumentParameters> getParameters(BIObject obj, String executionRole, Locale locale, String modality) {
		List<DocumentParameters> parametersForExecution = new ArrayList<DocumentParameters>();
		BIObject document = new BIObject();
		try {
			document = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByIdAndRole(obj.getId(), executionRole);
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<BIObjectParameter>  parameters = document.getBiObjectParameters();
		if (parameters != null && parameters.size() > 0) {
			Iterator<BIObjectParameter> it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				parametersForExecution.add(new DocumentParameters(parameter, executionRole, locale, document));
			}
		}
		return parametersForExecution;
	}
	

	public static JSONObject handleNormalExecution(
			UserProfile profile, BIObject obj, HttpServletRequest req, String env, String role, 
			String modality, String parametersJson, Locale locale) { // isFromCross,
		
		JSONObject response = new JSONObject();
		HashMap<String, String> logParam = new HashMap<String, String>();
		logParam.put("NAME", obj.getName());
		logParam.put("ENGINE", obj.getEngine().getName());
		logParam.put("PARAMS", parametersJson); // this.getAttributeAsString(PARAMETERS)
		DocumentUrlManager documentUrlManager = new DocumentUrlManager(profile, locale);
		try {
			List errors = null;
			JSONObject executionInstanceJSON = null;
			try {
				executionInstanceJSON = new JSONObject(parametersJson);
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			documentUrlManager.refreshParametersValues(executionInstanceJSON, false, obj);
			try {
				errors = documentUrlManager.getParametersErrors(obj, role);
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
			}
			try {
				errors = documentUrlManager.getParametersErrors(obj, role);
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
			}
			// ERRORS
			// if (errors != null && errors.size() > 0) {
			// there are errors on parameters validation, send errors' descriptions to the client
			JSONArray errorsArray = new JSONArray();
			Iterator errorsIt = errors.iterator();
			while (errorsIt.hasNext()) {
				EMFUserError error = (EMFUserError) errorsIt.next();
				errorsArray.put(error.getDescription());
			}
			try {
				response.put("errors", errorsArray);
			} catch (JSONException e) {
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "ERR");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize errors to the client", e);
			}
			// }else {
			// URL
			// there are no errors, we can proceed, so calculate the execution url and send it back to the client
			String url = documentUrlManager.getExecutionUrl(obj, modality, role);
			// url += "&isFromCross=" + (isFromCross == true ? "true" : "false");
			// adds information about the environment
			if (env == null) {
				env = "DOCBROWSER";
			}
			url += "&SBI_ENVIRONMENT=" + env;
			try {
				response.put("url", url);
			} catch (JSONException e) {
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "KO");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "ERR");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize the url [" + url + "] to the client", e);
			}

			AuditLogUtilities.updateAudit(req, profile, "DOCUMENT.GET_URL", logParam, "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
}
