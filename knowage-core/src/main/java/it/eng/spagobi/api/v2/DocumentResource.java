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
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.tools.glossary.util.Util.fromDocumentLight;
import static it.eng.spagobi.tools.glossary.util.Util.fromObjectParameterListLight;
import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IOutputParameterDAO;
import it.eng.spagobi.api.AbstractDocumentResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.ParameterDAOHibImpl;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.CriteriaParameter.Match;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.sdk.documents.bo.SDKDocument;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter;
import it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent;
import it.eng.spagobi.sdk.documents.impl.DocumentsServiceImpl;
import it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.JSError;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 */
@Path("/2.0/documents")
@ManageAuthorization
public class DocumentResource extends AbstractDocumentResource {
	static protected Logger logger = Logger.getLogger(DocumentResource.class);

	@GET
	@Path("/{label}/parameters")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDocumentParameters(@PathParam("label") String label) {

		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		List<BIObjectParameter> parameters = document.getDrivers();

		for (BIObjectParameter parameter : parameters) {
			parameter.setParameter(loadAnalyticalDriver(parameter));
		}

		return JsonConverter.objectToJson(parameters, parameters.getClass());
	}

	@Override
	@POST
	@Path("/")
	@Consumes("application/json")
	public Response insertDocument(String body) {
		return super.insertDocument(body);
	}

	@GET
	@Path("/{labelOrId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentDetails(@PathParam("labelOrId") String labelOrId) {
		Object documentIdentifier = this.getObjectIdentifier(labelOrId);
		return super.getDocumentDetails(documentIdentifier);
	}

	@Override
	@PUT
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDocument(@PathParam("label") String label, String body) {
		return super.updateDocument(label, body);
	}

	@Override
	@DELETE
	@Path("/{label}")
	public Response deleteDocument(@PathParam("label") String label) {
		return super.deleteDocument(label);
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{id}/roles")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getRolesByDocumentId(@PathParam("id") Integer id) {
		try {
			List<String> list = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id);
			return JsonConverter.objectToJson(list, list.getClass()); // SbiExtRoles.class
		} catch (EMFUserError e) {
			logger.error("Error while try to retrieve roles by document id [" + id + "]", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve roles by document id [" + id + "]", e);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{id}/userroles")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getUserRolesByDocumentId(@PathParam("id") Integer id) {
		try {
			UserProfile userProfile = getUserProfile();
			List<String> roles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id, userProfile);
			String userName = (String) userProfile.getUserId();
			List<String> userRoles = new ArrayList<String>();
			for (String role : roles) {
				userRoles.add(userName + "|" + role);
			}
			return JsonConverter.objectToJson(userRoles, userRoles.getClass());
		} catch (EMFUserError e) {
			logger.error("Error while try to retrieve user roles by document id [" + id + "]", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve user roles by document id [" + id + "]", e);
		}
	}

	public JSONArray writeParameters(List<JSONObject> params) throws Exception {
		JSONArray paramsJSON = new JSONArray();

		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			paramsJSON.put(jsonObject);
		}

		return paramsJSON;
	}

	@GET
	@Path("/{label}/parameters/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentParameter(@PathParam("label") String label, @PathParam("id") Integer id) {
		IBIObjectParameterDAO parameterDAO = null;
		BIObjectParameter parameter = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
			parameter = parameterDAO.loadBiObjParameterById(id);

			parameter.setParameter(loadAnalyticalDriver(parameter));
		} catch (HibernateException e) {
			logger.error("Error while try to retrieve the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve the specified parameter", e);
		}
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null) {
			logger.error("Document with label [" + label + "] doesn't exist");
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");
		}

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error("Parameter with id [" + id + "] is parameter of [" + parameter.getBiObjectID() + "], not [" + label + "]");
			throw new SpagoBIRuntimeException("Parameter with id [" + id + "] is not a parameter of [" + label + "]");
		}

		return JsonConverter.objectToJson(parameter, BIObjectParameter.class);
	}

	private Parameter loadAnalyticalDriver(BIObjectParameter biPar) {
		try {
			ParameterDAOHibImpl parameterDAO = (ParameterDAOHibImpl) DAOFactory.getParameterDAO();

			return parameterDAO.loadForDetailByParameterID(biPar.getParameter().getId());
		} catch (EMFUserError e) {
			logger.error("Error while retrieving analytical driver associated with the parameter", e);
			throw new SpagoBIRuntimeException("Error while retrieving analytical driver associated with the parameter", e);
		}
	}

	@POST
	@Path("/{label}/parameters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response addParameter(@PathParam("label") String label, String body) {
		BIObjectParameter parameter = (BIObjectParameter) JsonConverter.jsonToValidObject(body, BIObjectParameter.class);

		IBIObjectParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
		} catch (HibernateException e) {
			logger.error("Error while retrieving parameters", e);
			throw new SpagoBIRuntimeException("Error while retrieving parameters", e);
		}
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error(
					"[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId() + "]");
			throw new SpagoBIRuntimeException(
					"[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId() + "]");
		}

		try {
			parameterDAO.insertBIObjectParameter(parameter);
		} catch (HibernateException e) {
			logger.error("Error while inserting new parameter", e);
			throw new SpagoBIRuntimeException("Error while inserting new parameter", e);
		}

		return Response.ok().build();
	}

	@PUT
	@Path("/{label}/parameters/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response modifyParameter(@PathParam("label") String label, @PathParam("id") Integer id, String body) {
		BIObjectParameter parameter = (BIObjectParameter) JsonConverter.jsonToValidObject(body, BIObjectParameter.class);

		IBIObjectParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
		} catch (HibernateException e) {
			logger.error("Error while retrieving parameters", e);
			throw new SpagoBIRuntimeException("Error while retrieving parameters", e);
		}
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error(
					"[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId() + "]");
			throw new SpagoBIRuntimeException(
					"[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId() + "]");
		}

		parameter.setId(id);

		try {
			parameterDAO.modifyBIObjectParameter(parameter);
		} catch (HibernateException e) {
			logger.error("Error while modifying the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while modifying the specified parameter", e);
		}

		return Response.ok().build();
	}

	@DELETE
	@Path("/{label}/parameters/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteParameter(@PathParam("label") String label, @PathParam("id") Integer id) {
		IBIObjectParameterDAO parameterDAO = null;
		BIObjectParameter parameter = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();

			parameter = parameterDAO.loadBiObjParameterById(id);
		} catch (HibernateException e) {
			logger.error("Error while try to retrieve the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve the specified parameter", e);
		}
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null) {
			logger.error("Document with label [" + label + "] doesn't exist");
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");
		}

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error("Parameter with id [" + id + "] is parameter of [" + parameter.getBiObjectID() + "], not [" + label + "]");
			throw new SpagoBIRuntimeException("Parameter with id [" + id + "] is not a parameter of [" + label + "]");
		}

		try {
			parameterDAO.eraseBIObjectParameter(parameter, true);
		} catch (HibernateException e) {
			logger.error("Error while trying to delete the specified parameter");
			throw new SpagoBIRuntimeException("Error while trying to delete the specified parameter");
		}

		return Response.ok().build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("{label}/content")
	@Produces("application/pdf")
	public Response execute(@PathParam("label") String label, @QueryParam("outputType") String outputType, String body) {
		SDKDocumentParameter[] parameters = null;
		if (!body.isEmpty())
			parameters = (SDKDocumentParameter[]) JsonConverter.jsonToValidObject(body, SDKDocumentParameter[].class);
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		try {
			if (ObjectsAccessVerifier.canExec(document, getUserProfile())) {
				SDKObjectsConverter converter = new SDKObjectsConverter();
				SDKDocument sdkDocument = converter.fromBIObjectToSDKDocument(document);

				DocumentsServiceImpl documentService = new DocumentsService();

				Collection<String> roles = getUserProfile().getRoles();
				byte[] byteContent = null;
				SDKExecutedDocumentContent content = null;

				if (outputType == null || outputType.isEmpty())
					outputType = "HTML";

				for (String role : roles) {
					try {
						content = documentService.executeDocument(sdkDocument, parameters, role, outputType);

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						content.getContent().writeTo(outputStream);
						byteContent = outputStream.toByteArray();
					} catch (NonExecutableDocumentException e) {
						// Don't do anything: maybe another role of the user
						// gives him permission to execute the document
					}
				}

				if (byteContent != null) {
					ResponseBuilder rb = Response.ok(byteContent);
					rb.header("Content-Disposition", "attachment; filename=" + content.getFileName());
					return rb.build();
				} else
					throw new SpagoBIRuntimeException(
							"User [" + getUserProfile().getUserName() + "] has no rights to execute document with label [" + label + "]");
			} else
				throw new SpagoBIRuntimeException("User [" + getUserProfile().getUserName() + "] has no rights to execute document with label [" + label + "]");

		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (EMFInternalError e) {
			logger.error("Error while looking for authorizations", e);
			throw new SpagoBIRuntimeException("Error while looking for authorizations", e);
		} catch (Exception e) {
			logger.error("Error while executing document", e);
			throw new SpagoBIRuntimeException("Error while executing document", e);
		}
	}

	private class DocumentsService extends DocumentsServiceImpl {
		@Override
		protected IEngUserProfile getUserProfile() throws Exception {
			return DocumentResource.this.getUserProfile();
		}
	}

	@GET
	@Path("/listDocument")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentSearchAndPaginate(@QueryParam("Page") String pageStr, @QueryParam("ItemPerPage") String itemPerPageStr,
			@QueryParam("label") String label, @QueryParam("name") String name, @QueryParam("descr") String descr,
			@QueryParam("excludeType") String excludeType, @QueryParam("includeType") String includeType, @QueryParam("scope") String scope,
			@QueryParam("loadObjPar") Boolean loadObjPar, @QueryParam("objLabelIn") String objLabelIn, @QueryParam("objLabelNotIn") String objLabelNotIn,
			@QueryParam("forceVis") @DefaultValue("false") Boolean forceVisibility) throws EMFInternalError {
		logger.debug("IN");
		UserProfile profile = getUserProfile();
		IBIObjectDAO documentsDao = null;
		List<BIObject> filterObj = null;
		Integer page = getNumberOrNull(pageStr);
		Integer item_per_page = getNumberOrNull(itemPerPageStr);
		List<CriteriaParameter> disjunctions = new ArrayList<CriteriaParameter>();
		if (label != null && !label.isEmpty()) {
			disjunctions.add(new CriteriaParameter("label", label, Match.ILIKE));
		}
		if (name != null && !name.isEmpty()) {
			disjunctions.add(new CriteriaParameter("name", name, Match.ILIKE));
		}
		if (descr != null && !descr.isEmpty()) {
			disjunctions.add(new CriteriaParameter("descr", descr, Match.ILIKE));
		}

		String UserFilter = profile.getIsSuperadmin() ? null : profile.getUserId().toString();

		// in glossary, the user with admin role and specific authorization can
		// see all document of the organization
		if (scope != null && scope.compareTo("GLOSSARY") == 0) {
			if (UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE,
					new String[] { SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL })) {
				UserFilter = null;
			}
		}

		List<CriteriaParameter> restritions = new ArrayList<CriteriaParameter>();

		// filter document if is USER profile
		// Commented out: this kind of logic has to be handled by the
		// "ObjectsAccessVerifier.canSee" utility method
		// (ATHENA-138/SBI-532/SBI-533)
		/*
		 * if (UserFilter != null) { restritions.add(new CriteriaParameter("creationUser", UserFilter, Match.EQ)); }
		 */

		if (excludeType != null) {
			restritions.add(new CriteriaParameter("objectTypeCode", excludeType, Match.NOT_EQ));
		}
		if (includeType != null) {
			restritions.add(new CriteriaParameter("objectTypeCode", includeType, Match.EQ));
		}
		if (objLabelIn != null) {
			restritions.add(new CriteriaParameter("label", objLabelIn.split(","), Match.IN));
		}
		if (objLabelNotIn != null) {
			restritions.add(new CriteriaParameter("label", objLabelNotIn.split(","), Match.NOT_IN));
		}

		// hide if user is not admin or devel and visible is false
		if (!forceVisibility && !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
				&& !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
			restritions.add(new CriteriaParameter("visible", Short.valueOf("1"), Match.EQ));
		}
		try {
			documentsDao = DAOFactory.getBIObjectDAO();

			filterObj = documentsDao.loadPaginatedSearchBIObjects(page, item_per_page, disjunctions, restritions);
			JSONArray jarr = new JSONArray();
			if (filterObj != null) {
				for (BIObject sbiob : filterObj) {
					if (forceVisibility || ObjectsAccessVerifier.canSee(sbiob, profile)) {
						JSONObject tmp = fromDocumentLight(sbiob);
						if (loadObjPar != null && loadObjPar == true) {
							tmp.put("objParameter", fromObjectParameterListLight(sbiob.getDrivers()));
						}
						jarr.put(tmp);
					}
				}
			}
			JSONObject jo = new JSONObject();
			jo.put("item", jarr);
			jo.put("itemCount", documentsDao.countBIObjects(label != null ? label : "", UserFilter));

			return jo.toString();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentsV2(@QueryParam("type") String type, @QueryParam("folderId") String folderIdStr, @QueryParam("date") String date,
			@QueryParam("searchKey") String searchKey, @QueryParam("searchAttributes") String attributes, @QueryParam("searchSimilar") Boolean similar) {
		logger.debug("IN");
		IBIObjectDAO documentsDao = null;
		List<BIObject> allObjects = null;
		List<BIObject> objects = null;

		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.documents.list");

		Integer functionalityId = getNumberOrNull(folderIdStr);

		boolean isTypeFilterValid = !StringUtilities.isEmpty(type);
		boolean isFolderFilterValid = functionalityId != null;
		boolean isDateFilterValid = !StringUtilities.isEmpty(date);
		boolean isSearchFilterValid = !StringUtilities.isEmpty(searchKey);

		try {
			documentsDao = DAOFactory.getBIObjectDAO();
			// NOTICE: at the time being, filter on date, folder and search key are mutually exclusive, i.e. the service cannot filter on date and folder and
			// search for a specified key at the same time
			if (isDateFilterValid) {
				logger.debug("Date input parameter found: [" + date + "]. Loading documents before that date...");
				allObjects = documentsDao.loadDocumentsBeforeDate(date);
			} else if (isFolderFilterValid) {
				logger.debug("Folder id parameter found: [" + functionalityId + "]. Loading documents belonging to that folder...");
				allObjects = documentsDao.loadAllBIObjectsByFolderId(functionalityId);
			} else if (isSearchFilterValid) {
				logger.debug("Search key found: [" + searchKey + "]. Loading documents that match search key...");
				allObjects = documentsDao.loadAllBIObjectsBySearchKey(searchKey, attributes);
			} else {
				logger.debug("Neither filter on date nor on folder nor a search key was found, loading all documents...");
				allObjects = documentsDao.loadAllBIObjects();
			}

			UserProfile profile = getUserProfile();
			objects = new ArrayList<BIObject>();

			for (BIObject obj : allObjects) {
				try {
					// hide if user is not admin or devel and visible is false
					if (!obj.isVisible() && !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
							&& !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) || isDeprecated(obj)) {
						continue;
					}
					if (ObjectsAccessVerifier.canSee(obj, profile) && (!isTypeFilterValid || obj.getBiObjectTypeCode().equals(type))) {
						objects.add(obj);
					}
				} catch (EMFInternalError e) {
					throw new RuntimeException("Error while checking visibility of a document", e);
				}

			}

			String toBeReturned = JsonConverter.objectToJson(objects, objects.getClass());

			Response response = Response.ok(toBeReturned).build();

			monitor.stop();

			return response;
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			monitor.stop(e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean isDeprecated(BIObject obj) {
		String type = obj.getBiObjectTypeCode();
		if (type.equalsIgnoreCase("CHART") || type.equalsIgnoreCase("DATA_MINING"))
			return true;
		else
			return false;
	}

	@GET
	@Path("/{id}/listOutParams")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response listOutParams(@PathParam("id") Integer id) {
		IOutputParameterDAO op = DAOFactory.getOutputParameterDAO();
		op.setUserProfile(getUserProfile());
		List<OutputParameter> lst = op.getOutputParametersByObjId(id);
		String toBeReturned = JsonConverter.objectToJson(lst, lst.getClass());
		return Response.ok(toBeReturned).build();
	}

	@POST
	@Path("/saveOutParam")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response addOutputParameter(String body) {
		OutputParameter op = (OutputParameter) JsonConverter.jsonToValidObject(body, OutputParameter.class);

		if (op.getType() == null) {
			return Response.ok(new JSError().addError("sbi.document.outparam.type.mandatory").toString()).build();
		}

		IOutputParameterDAO parameterDAO = DAOFactory.getOutputParameterDAO();
		parameterDAO.setUserProfile(getUserProfile());
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(op.getBiObjectId());
		if (document == null) {
			throw new SpagoBIRuntimeException("Document with id [" + op.getBiObjectId() + "] doesn't exist");
		}

		parameterDAO.saveParameter(op);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}/deleteOutParam")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response removeOutputParameter(@PathParam("id") Integer id, @Context HttpServletRequest req) {
		try {
			IOutputParameterDAO parameterDAO = DAOFactory.getOutputParameterDAO();
			parameterDAO.setUserProfile(getUserProfile());
			parameterDAO.removeParameter(id);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while deleting parameter with id " + id);
		}

		return Response.ok().build();
	}

	@GET
	@Path("/{label}/parameters/{id}/values")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentParameterValues(@PathParam("label") String label, @PathParam("id") Integer id, @QueryParam("role") String role) {
		logger.debug("IN");

		List<String> values = new ArrayList<String>();
		String columnName = null;
		boolean manualInput = false;

		try {
			IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
			ParameterUse parameterUse = parameterUseDAO.loadByParameterIdandRole(id, role);

			values = new ArrayList<String>();
			columnName = null;

			Integer manualInputInteger = parameterUse.getManualInput();
			if (manualInputInteger != null) {
				manualInput = manualInputInteger.intValue() == 1;
			}

			if (!manualInput) {
				IParameterDAO parameterDAO = DAOFactory.getParameterDAO();
				Parameter parameter = parameterDAO.loadForExecutionByParameterIDandRoleName(id, role, false);
				ModalitiesValue modVal = parameter.getModalityValue();
				String lovProvider = modVal.getLovProvider();
				String lovType = LovDetailFactory.getLovTypeCode(lovProvider);

				ILovDetail lovDetail = null;
				if (lovType.equalsIgnoreCase("QUERY")) {
					lovDetail = QueryDetail.fromXML(lovProvider);
				} else if (lovType.equalsIgnoreCase("FIXED_LIST")) {
					lovDetail = FixedListDetail.fromXML(lovProvider);
				} else if (lovType.equalsIgnoreCase("SCRIPT")) {
					lovDetail = ScriptDetail.fromXML(lovProvider);
				} else if (lovType.equalsIgnoreCase("JAVA_CLASS")) {
					lovDetail = JavaClassDetail.fromXML(lovProvider);
				}
				columnName = lovDetail.getValueColumnName();

				String result = lovDetail.getLovResult(getUserProfile(), null, null, null);
				SourceBean rowsSourceBean = SourceBean.fromXMLString(result);

				if (rowsSourceBean != null) {
					List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
					for (int i = 0; i < rows.size(); i++) {
						SourceBean row = (SourceBean) rows.get(i);
						String value = row.getAttribute(columnName).toString();
						values.add(value);
					}
				}
			}

			JSONObject jo = new JSONObject();
			jo.put("values", values);
			jo.put("manualInput", manualInput);
			jo.put("columnName", columnName);
			return jo.toString();
		} catch (Exception e) {
			String error = "Error while getting the list of parameter values by document [" + label + "], parameter [" + id + "] and role [" + role + "]";
			logger.error(error, e);
			throw new SpagoBIRuntimeException(error, e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{label}/preview")
	public Response getPreviewFile(@PathParam("label") String label) {
		logger.debug("IN");
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(label);

		ResponseBuilder rb;

		if (document == null) {
			logger.error("Document with label [" + label + "] doesn't exist");
			rb = Response.status(Status.NOT_FOUND);
			return rb.build();
		}

		try {

			if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {

				// String toBeReturned = JsonConverter.objectToJson(document,
				// BIObject.class);
				// return Response.ok(toBeReturned).build();

				String previewFileName = document.getPreviewFile();

				if (previewFileName == null || previewFileName.equalsIgnoreCase("")) {
					logger.debug("No preview file associated to document " + document.getLabel());
					// rb = Response.ok();
					rb = Response.status(Status.NOT_FOUND);
					return rb.build();
				}

				File previewDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();

				String previewFilePath = previewDirectory.getAbsolutePath() + File.separator + previewFileName;

				File previewFile = new File(previewFilePath);
				if (!previewFile.exists()) {
					logger.error("Preview file " + previewFileName + " does not exist");
					rb = Response.status(Status.NOT_FOUND);
					return rb.build();
					// throw new SpagoBIRuntimeException("Preview file " +
					// previewFileName + " does not exist");
				}

				// to prevent attacks check file parent is really the expected
				// one
				String parentPath = previewFile.getParentFile().getAbsolutePath();
				String directoryPath = previewDirectory.getAbsolutePath();
				if (!parentPath.equals(directoryPath)) {
					logger.error("Path Traversal Attack security check failed: file parent path: " + parentPath + " is different" + " from directory path: "
							+ directoryPath);
					throw new SpagoBIRuntimeException("Path Traversal Attack security check failed");
				}

				byte[] previewBytes = Files.readAllBytes(previewFile.toPath());

				try {
					rb = Response.ok(previewBytes);
				} catch (Exception e) {
					logger.error("Error while getting preview file", e);
					throw new SpagoBIRuntimeException("Error while getting preview file", e);
				}

				rb.header("Content-Disposition", "attachment; filename=" + previewFileName);
				return rb.build();

			} else {
				logger.error("User [" + getUserProfile().getUserName() + "] has no rights to see document with label [" + label + "]");
				// throw new SpagoBIRuntimeException("User [" +
				// getUserProfile().getUserName() +
				// "] has no rights to see document with label [" + label +
				// "]");
				rb = Response.status(Status.UNAUTHORIZED);
				return rb.build();
			}
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while converting document in Json", e);
			throw new SpagoBIRuntimeException("Error while converting document in Json", e);
		}

	}

	@Override
	@GET
	@Path("/{label}/template")
	public Response getDocumentTemplate(@PathParam("label") String label) {
		return super.getDocumentTemplate(label);
	}

	@Override
	@POST
	@Path("/{label}/template")
	public Response addDocumentTemplate(@PathParam("label") String label, MultiPartBody input) {
		return super.addDocumentTemplate(label, input);
	}

	@Override
	@DELETE
	@Path("/{label}/template")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteCurrentTemplate(@PathParam("label") String label) {
		return super.deleteCurrentTemplate(label);
	}

	@Override
	@DELETE
	@Path("/{label}/template/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteTemplateById(@PathParam("label") String label, @PathParam("id") Integer templateId) {
		return super.deleteTemplateById(label, templateId);
	}

}
