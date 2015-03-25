package it.eng.spagobi.behaviouralmodel.analyticaldriver.service;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/analyticalDriver")
public class AnalitycalDriver {

	static private Logger logger = Logger.getLogger(AnalitycalDriver.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {

		logger.debug("IN");

		IParameterDAO parametersDAO = null;
		List<Parameter> parameters;
		JSONObject parametersJSON = new JSONObject();

		try {
			parametersDAO = DAOFactory.getParameterDAO();
			parameters = parametersDAO.loadAllParameters();

			logger.debug("OUT: Returned analitical drivers");

			parametersJSON = serializeParameters(parameters);

		} catch (Exception e) {

			logger.debug("OUT: Exception in returning analitical drivers");
			throw new SpagoBIServiceException("analyticalDriver", "There is the problem with get", e);

		}

		return parametersJSON.toString();

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@Context HttpServletRequest req) {

		logger.debug("IN");

		Parameter parameter = null;

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String label = (String) requestBodyJSON.opt("LABEL");
			parameter = DAOFactory.getParameterDAO().loadForDetailByParameterLabel(label);

			DAOFactory.getParameterUseDAO().eraseParameterUseByParId(parameter.getId());

			DAOFactory.getParameterDAO().eraseParameter(parameter);

			logger.debug("OUT: Deleted analitical drivers");

			return Response.ok().build();
		} catch (Exception e) {

			logger.debug("OUT: Exception in deleting analitical driver");
			throw new SpagoBIServiceException("analyticalDriver", "There is the problem with delete", e);

		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String put(@Context HttpServletRequest req) {

		logger.debug("IN");

		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			IParameterDAO pardao = DAOFactory.getParameterDAO();

			Parameter parameter = recoverParameterDetails(requestBodyJSON);

			pardao.modifyParameter(parameter);

			parameter = reloadParameter(parameter.getLabel());

			logger.debug("OUT: Put analitical driver");

			JSONObject parameterIdentifier = new JSONObject();
			parameterIdentifier.put("ID", parameter.getId());

			return parameterIdentifier.toString();
		} catch (Exception e) {

			logger.debug("OUT: Exception in puting analitical driver");
			throw new SpagoBIServiceException("analyticalDriver", "There is the problem with put", e);

		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String post(@Context HttpServletRequest req) {

		logger.debug("IN");

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			IParameterDAO pardao = DAOFactory.getParameterDAO();

			Parameter parameter = recoverParameterDetails(requestBodyJSON);

			pardao.insertParameter(parameter);

			parameter = reloadParameter(parameter.getLabel());

			logger.debug("OUT: Posted analitical driver");

			JSONObject parameterIdentifier = new JSONObject();
			parameterIdentifier.put("ID", parameter.getId());

			return parameterIdentifier.toString();
		} catch (Exception e) {

			logger.debug("OUT: Exception in posting analitical driver");
			throw new SpagoBIServiceException("analyticalDriver", "There is the problem with post", e);
		}

	}

	private JSONObject serializeParameters(List<Parameter> parameters) {

		logger.debug("IN");

		JSONObject parametersJSON = new JSONObject();
		JSONArray parametersJSONArray = new JSONArray();

		try {
			parametersJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(parameters, null);

			parametersJSON.put("root", parametersJSONArray);

			logger.debug("OUT: Serialized analitical driver");

		} catch (Exception e) {

			logger.debug("OUT: Exception in serializing analitical driver");
			throw new SpagoBIServiceException("analyticalDriver", "There is a serialize problem", e);

		}

		return parametersJSON;
	}

	private Parameter recoverParameterDetails(JSONObject requestBodyJSON) throws EMFUserError {

		logger.debug("IN");

		Parameter par = new Parameter();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("ID");
		if (idStr != null && !idStr.equals("")) {

			id = new Integer(idStr);

		}

		String desc = (String) requestBodyJSON.opt("DESCRIPTION");
		String lenght = (String) requestBodyJSON.opt("LENGTH");
		String label = (String) requestBodyJSON.opt("LABEL");
		String name = (String) requestBodyJSON.opt("NAME");
		String functionalFlag = (String) requestBodyJSON.opt("FUNCTIONALFLAG");
		String temporalFlag = (String) requestBodyJSON.opt("TEMPORALFLAG");
		String inputTypeCode = (String) requestBodyJSON.opt("INPUTTYPECD");
		Domain domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("PAR_TYPE", inputTypeCode);
		String modality = (inputTypeCode + ',' + domain.getValueId().toString());

		Assert.assertNotNull(id, "Id cannot be null");
		Assert.assertNotNull(desc, "Description cannot be null");

		Assert.assertNotNull(modality, "Modality cannot be null");
		Assert.assertNotNull(label, "Label cannot be null");
		Assert.assertNotNull(name, "Name cannot be null");

		par.setId(id);
		par.setDescription(desc);
		par.setLabel(label);
		par.setName(name);

		if (lenght == null || lenght == "") {
			par.setLength(new Integer(0));
		} else {
			par.setLength(new Integer(lenght));
		}

		if (functionalFlag.equals("on")) {
			par.setIsFunctional(true);
		} else {
			par.setIsFunctional(false);
		}

		if (temporalFlag.equals("on")) {
			par.setIsTemporal(true);
		} else {
			par.setIsTemporal(false);

		}
		par.setModality(modality);

		StringTokenizer st;
		st = new StringTokenizer(modality, ",", false);
		String par_type_cd = st.nextToken();
		String par_type_id = st.nextToken();
		par.setType(par_type_cd);
		par.setTypeId(new Integer(par_type_id));

		logger.debug("OUT: Recovered analitical driver use");

		return par;
	}

	private Parameter reloadParameter(String label) throws EMFInternalError {
		if (label == null || label.trim().equals(""))
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "Invalid input data for method relaodParameter in DetailParameterModule");
		Parameter parameter = null;
		try {
			IParameterDAO parareterDAO = DAOFactory.getParameterDAO();
			List parameters = parareterDAO.loadAllParameters();
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				Parameter aParameter = (Parameter) it.next();
				if (aParameter.getLabel().equals(label)) {
					parameter = aParameter;
					break;
				}
			}
		} catch (EMFUserError e) {
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE, "DetailParameterModule", "reloadParameter", "Cannot reload Parameter", e);
		}
		/*
		 * if (parameter == null) { SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE, "DetailParameterModule", "reloadParameter", "Parameter with label '" +
		 * label + "' not found."); parameter = createNewParameter(); }
		 */
		return parameter;
	}

}
