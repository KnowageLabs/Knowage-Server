/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.tools.glossary.util.Util.fromDocumentLight;
import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.ParameterDAOHibImpl;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.CriteriaParameter.Match;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.DocumentsJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.indexing.IndexingConstants;
import it.eng.spagobi.commons.utilities.indexing.LuceneSearcher;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.sdk.documents.bo.SDKDocument;
import it.eng.spagobi.sdk.documents.bo.SDKDocumentParameter;
import it.eng.spagobi.sdk.documents.bo.SDKExecutedDocumentContent;
import it.eng.spagobi.sdk.documents.impl.DocumentsServiceImpl;
import it.eng.spagobi.sdk.exceptions.NonExecutableDocumentException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 * 
 */
@Path("/2.0/documents")
public class DocumentResource extends it.eng.spagobi.api.DocumentResource {
	static protected Logger logger = Logger.getLogger(DocumentResource.class);

	@Override
	public String getDocumentParameters(String label) {
		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		List<BIObjectParameter> parameters = document.getBiObjectParameters();

		for (BIObjectParameter parameter : parameters) {
			parameter.setParameter(loadAnalyticalDriver(parameter));
		}

		return JsonConverter.objectToJson(parameters, parameters.getClass());
	}

	@GET
	@Path("/{id}/roles")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getRolesByDocumentId(@PathParam("id") Integer id) {
		try {
			List<String> lst = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id);
			return JsonConverter.objectToJson(lst, lst.getClass());// SbiExtRoles.class
		} catch (EMFUserError e) {
			logger.error("Error while try to retrieve the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve roles by document id [" + id + "]", e);
		}
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
		} catch (EMFUserError e) {
			logger.error("Error while try to retrieve the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve the specified parameter", e);
		}

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
		} catch (EMFUserError e) {
			logger.error("Error while retrieving parameters", e);
			throw new SpagoBIRuntimeException("Error while retrieving parameters", e);
		}

		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error("[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId()
					+ "]");
			throw new SpagoBIRuntimeException("[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is ["
					+ document.getId() + "]");
		}

		try {
			parameterDAO.insertBIObjectParameter(parameter);
		} catch (EMFUserError e) {
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
		} catch (EMFUserError e) {
			logger.error("Error while retrieving parameters", e);
			throw new SpagoBIRuntimeException("Error while retrieving parameters", e);
		}

		BIObject document = documentManager.getDocument(label);
		if (document == null)
			throw new SpagoBIRuntimeException("Document with label [" + label + "] doesn't exist");

		if (!parameter.getBiObjectID().equals(document.getId())) {
			logger.error("[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is [" + document.getId()
					+ "]");
			throw new SpagoBIRuntimeException("[" + parameter.getBiObjectID() + "] is not the id of document with label [" + label + "]. The correct id is ["
					+ document.getId() + "]");
		}

		parameter.setId(id);

		try {
			parameterDAO.modifyBIObjectParameter(parameter);
		} catch (EMFUserError e) {
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
		} catch (EMFUserError e) {
			logger.error("Error while try to retrieve the specified parameter", e);
			throw new SpagoBIRuntimeException("Error while try to retrieve the specified parameter", e);
		}

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
		} catch (EMFUserError e) {
			logger.error("Error while trying to delete the specified parameter");
			throw new SpagoBIRuntimeException("Error while trying to delete the specified parameter");
		}

		return Response.ok().build();
	}

	@POST
	@Path("{label}/content")
	@Produces("application/pdf")
	public Response execute(@PathParam("label") String label, @QueryParam("outputType") String outputType, String body) {
		SDKDocumentParameter[] parameters = null;
		if (!body.isEmpty())
			parameters = (SDKDocumentParameter[]) JsonConverter.jsonToValidObject(body, SDKDocumentParameter[].class);

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
						// Don't do anything: maybe another role of the user gives him permission to execute the document
					}
				}

				if (byteContent != null) {
					ResponseBuilder rb = Response.ok(byteContent);
					rb.header("Content-Disposition", "attachment; filename=" + content.getFileName());
					return rb.build();
				} else
					throw new SpagoBIRuntimeException("User [" + getUserProfile().getUserName() + "] has no rights to execute document with label [" + label
							+ "]");
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
			@QueryParam("excludeType") String excludeType, @QueryParam("scope") String scope) throws EMFInternalError {
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

		// in glossary, the user with admin role and specific authorization can see all document of the organization
		if (scope != null && scope.compareTo("GLOSSARY") == 0) {
			if (UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[] { SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL })) {
				UserFilter = null;
			}
		}

		List<CriteriaParameter> restritions = new ArrayList<CriteriaParameter>();

		// filter document if is USER profile
		// Commented out: this kind of logic has to be handled by the "ObjectsAccessVerifier.canSee" utility method (ATHENA-138/SBI-532/SBI-533)
		/*
		 * if (UserFilter != null) { restritions.add(new CriteriaParameter("creationUser", UserFilter, Match.EQ)); }
		 */

		if (excludeType != null) {
			restritions.add(new CriteriaParameter("objectTypeCode", excludeType, Match.NOT_EQ));
		}
		try {
			documentsDao = DAOFactory.getBIObjectDAO();

			filterObj = documentsDao.loadPaginatedSearchBIObjects(page, item_per_page, disjunctions, restritions);
			JSONArray jarr = new JSONArray();
			if (filterObj != null) {
				for (BIObject sbiob : filterObj) {
					if (ObjectsAccessVerifier.canSee(sbiob, profile))
						jarr.put(fromDocumentLight(sbiob));
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
	
	@GET
	@Path("/searchDocument")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentByLucene(@QueryParam("value") String valueFilter,
			@QueryParam("attributes") String attributes, @QueryParam("similar") Boolean similar) {
		logger.debug("IN");
		
		try {
			UserProfile profile = (UserProfile) getUserProfile();
			
			List<String> fieldsToSearch = new ArrayList<String>();
			String metaDataToSearch = null;
			
			if (attributes != null && !attributes.isEmpty()) {
				if (attributes.equalsIgnoreCase("ALL")) {
					// search in all fields
					fieldsToSearch.add(IndexingConstants.BIOBJ_LABEL);
					fieldsToSearch.add(IndexingConstants.BIOBJ_NAME);
					fieldsToSearch.add(IndexingConstants.BIOBJ_DESCR);
					fieldsToSearch.add(IndexingConstants.METADATA);
					// search metadata binary content
					fieldsToSearch.add(IndexingConstants.CONTENTS);
					// search subobject fields
					fieldsToSearch.add(IndexingConstants.SUBOBJ_DESCR);
					fieldsToSearch.add(IndexingConstants.SUBOBJ_NAME);
				} else if (attributes.equalsIgnoreCase("LABEL")) {
					// search in label field
					fieldsToSearch.add(IndexingConstants.BIOBJ_LABEL);
				} else if (attributes.equalsIgnoreCase("NAME")) {
					// search in name field
					fieldsToSearch.add(IndexingConstants.BIOBJ_NAME);
				} else if (attributes.equalsIgnoreCase("DESCRIPTION")) {
					// search in description field
					fieldsToSearch.add(IndexingConstants.BIOBJ_DESCR);
				} else {
					// search in categories
					metaDataToSearch = attributes;
					fieldsToSearch.add(IndexingConstants.CONTENTS);
				}
			}
			
			String indexBasePath = "";
			String jndiBeanName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			if (jndiBeanName != null) {
				indexBasePath = SpagoBIUtilities.readJndiResource(jndiBeanName);
			}
			String indexFolderPath = indexBasePath + "/idx";
			
			HashMap hashMap = null;
			List<BIObject> objects = new ArrayList<BIObject>();
			
			try {
				IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexFolderPath)), true);
				IndexSearcher searcher = new IndexSearcher(reader);

				String[] fields = new String[fieldsToSearch.size()];
				fieldsToSearch.toArray(fields);
				
				// getting documents
				if (similar != null && similar) {
					hashMap = LuceneSearcher.searchIndexFuzzy(searcher, valueFilter, indexFolderPath, fields, metaDataToSearch);
				} else {
					hashMap = LuceneSearcher.searchIndex(searcher, valueFilter, indexFolderPath, fields, metaDataToSearch);
				}
				ScoreDoc[] hits = (ScoreDoc[]) hashMap.get("hits");

				if (hits != null) {
					for (int i = 0; i < hits.length; i++) {
						ScoreDoc hit = hits[i];
						Document doc = searcher.doc(hit.doc);
						String biobjId = doc.get(IndexingConstants.BIOBJ_ID);

						BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForDetail(Integer.valueOf(biobjId));
						if (obj != null) {
							if (ObjectsAccessVerifier.canSee(obj, profile)) {
								objects.add(obj);
							}
						}
					}
				}
				searcher.close();
			} catch (CorruptIndexException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Index corrupted", e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Unable to read index", e);
			}
			catch (ParseException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Wrong query syntax", e);
			}
			String toBeReturned = JsonConverter.objectToJson(objects, objects.getClass());
			return Response.ok(toBeReturned).build();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocuments(@QueryParam("type") String type,
			@QueryParam("folderId") String folderIdStr) {
		logger.debug("IN");
		IBIObjectDAO documentsDao = null;
		List<BIObject> allObjects = null;
		List<BIObject> objects = null;
		
		Integer functionalityId = getNumberOrNull(folderIdStr);
		
		boolean isTypeFilterValid = type != null && !type.isEmpty();
		boolean isFolderFilterValid = functionalityId != null;

		try {
			documentsDao = DAOFactory.getBIObjectDAO();
			allObjects = documentsDao.loadAllBIObjects();

			UserProfile profile = getUserProfile();
			objects = new ArrayList<BIObject>();
			
			for (BIObject obj : allObjects) {
				if (ObjectsAccessVerifier.canSee(obj, profile)
						&& (!isTypeFilterValid || obj.getBiObjectTypeCode().equals(type))
						&& (!isFolderFilterValid || obj.getFunctionalities().contains(functionalityId)))
					objects.add(obj);
			}
			
			String toBeReturned = JsonConverter.objectToJson(objects, objects.getClass());
			return Response.ok(toBeReturned).build();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}
}
