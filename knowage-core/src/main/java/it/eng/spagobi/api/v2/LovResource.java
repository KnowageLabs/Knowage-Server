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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.behaviouralmodel.lov.service.GridMetadataContainer;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

@Path("/2.0/lovs")
@ManageAuthorization
public class LovResource extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(LovResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Path("/get/all")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public List<ModalitiesValue> getAllListOfValues() {

		logger.debug("IN");

		List<ModalitiesValue> modalitiesValues = null;
		IModalitiesValueDAO modalitiesValueDAO = null;

		try {
			// TODO this part
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();
			for (ModalitiesValue lov : modalitiesValues) {
				String providerString = lov.getLovProvider();
				String converted = convertSpecialChars(providerString);
				String result = Xml.xml2json(converted);
				lov.setLovProvider(result);
			}
			logger.debug("Getting the list of all LOVs - done successfully");

		} catch (Exception exception) {

			logger.error("Error while getting the list of LOVs", exception);
			throw new SpagoBIServiceException("Error while getting the list of LOVs", exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", modalitiesValues.toString());

		}

		return modalitiesValues;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public ModalitiesValue getOnlyOneListOfValue(@PathParam("id") Integer idLOV) {

		logger.debug("IN: input id = " + idLOV);

		IModalitiesValueDAO modalitiesValueDAO;
		ModalitiesValue listOfValues = null;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			listOfValues = modalitiesValueDAO.loadModalitiesValueByID(idLOV);

			// JSONObject lovJSONObject =
			// serializeModalitiesValues(listOfValues);

			// toReturn = JsonConverter.objectToJson(listOfValues, null);

			// toReturn = lovJSONObject.toString();
			logger.debug(String.format("Getting the LOV with ID=%d - done successfully", idLOV));

		} catch (Exception exception) {

			String messageToSend = String.format("Error while getting LOV with ID : %d", idLOV);
			logger.error(messageToSend, exception);
			throw new SpagoBIServiceException(messageToSend, exception);

		} finally {

			LogMF.debug(logger, "OUT: returning [{0}]", listOfValues.toString());

		}

		return listOfValues;

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/analyticalDrivers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadDriversByLovId(@PathParam("id") Integer lovId) {

		IParameterDAO driversDao = null;
		List<Parameter> fullList = null;

		try {

			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			fullList = driversDao.loadParametersByLovId(lovId);
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PARAMETER_MANAGEMENT })
	@Path("/{id}/documents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumetsByLovId(@PathParam("id") Integer lovId) {

		IBIObjectDAO documentsDao = null;
		List<BIObject> documents = null;
		logger.debug("IN");

		try {
			documentsDao = DAOFactory.getBIObjectDAO();
			documentsDao.setUserProfile(getUserProfile());
			documents = documentsDao.loadBIObjectsByLovId(lovId);

			return Response.ok(documents).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("{id}/preview")
	@Produces(MediaType.APPLICATION_JSON)
	// @UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public List previewLovValues(@PathParam("id") Integer lovId) {
		logger.debug("IN: input id = " + lovId);
		GridMetadataContainer lovExecutionResult = new GridMetadataContainer();
		IModalitiesValueDAO modalitiesValueDAO;
		ModalitiesValue listOfValues = null;
		List lovValues = new ArrayList();
		List lovColumns = new ArrayList();
		List valuesScript = new ArrayList();
		String scriptValue = "";
		SourceBean rowsSourceBean = null;
		try {
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			listOfValues = modalitiesValueDAO.loadModalitiesValueByID(lovId);
			if (listOfValues.getITypeCd().equalsIgnoreCase("SCRIPT")) {
				ScriptDetail scriptDetail = ScriptDetail.fromXML(listOfValues.getLovProvider());
				scriptValue = scriptDetail.getLovResult(getUserProfile(), null, null, null);
				rowsSourceBean = SourceBean.fromXMLString(scriptValue);
				lovColumns = findFirstRowAttributes(rowsSourceBean);
				lovExecutionResult.setValues(filterNulls(rowsSourceBean, lovColumns.size(), 0, lovValues.size()));
				lovExecutionResult.setFields(GridMetadataContainer.buildHeaderMapForGrid(lovColumns));
				List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
				lovExecutionResult.setResults(rows.size());
				valuesScript = lovExecutionResult.getValues();
				return valuesScript;
			} else {
				String lovProv = listOfValues.getLovProvider();
				ILovDetail lovProvDet = null;
				lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
				String lovResult = lovProvDet.getLovResult(getUserProfile(), null, null, null);
				LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
				lovValues = lovResultHandler.getValues(lovProvDet.getValueColumnName());
				return lovValues;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov details", e);
		}
		// return lovValues;
	}

	@POST
	@Path("/preview")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public String preview(@javax.ws.rs.core.Context HttpServletRequest req) {
		logger.debug("IN");

		JSONObject pagination = new JSONObject();
		JSONObject data = new JSONObject();
		JSONArray dependencies = new JSONArray();
		JSONArray parameters = new JSONArray();
		JSONArray profiles = new JSONArray();
		GridMetadataContainer lovExecutionResult = new GridMetadataContainer();
		SourceBean rowsSourceBean = null;
		List<String> colNames = new ArrayList<String>();
		String result = null;
		String toReturn = null;
		String typeLov = null;
		String lovProvider = null;

		Map<String, String> paramFilled = new HashMap<>();

		try {
			IEngUserProfile profile = getUserProfile();
			String unsafe = RestUtilities.readBodyXSSUnsafe(req);
			JSONObject dependenciesObj = new JSONObject(unsafe);
			pagination = dependenciesObj.getJSONObject("pagination");
			data = dependenciesObj.getJSONObject("data");
			dependencies = dependenciesObj.optJSONArray("dependencies");
			if (dependencies != null && dependencies.length() > 0) {

				for (int i = 0; i < dependencies.length(); i++) {
					JSONObject obj = dependencies.getJSONObject(i);
					if (obj.getString("type").equals("profile")) {
						profiles.put(obj);
					} else {
						parameters.put(obj);
					}
				}
			}

			if (profiles != null && profiles.length() > 0) {
				UserProfile fake = insertIntoFakeUser(profiles);
				profile = fake;
			}
			if (parameters != null && parameters.length() > 0) {
				for (int i = 0; i < parameters.length(); i++) {
					String name = parameters.getJSONObject(i).getString("name");
					String value = parameters.getJSONObject(i).getString("value");
					paramFilled.put(name, value);
				}
			}

			typeLov = data.getString("itypeCd");
			lovProvider = data.getString("lovProvider");
			lovProvider = StringEscapeUtils.unescapeXml(lovProvider);

			if (typeLov != null && typeLov.equalsIgnoreCase("JAVA_CLASS")) {
				JavaClassDetail javaClassDetail = JavaClassDetail.fromXML(lovProvider);
				try {
					String javaClassName = javaClassDetail.getJavaClassName();
					IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
					result = javaClassLov.getValues(profile);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					logger.error("Cannot get values from java class");
					throw new SpagoBIRuntimeException("Cannot get values from java class", e);
				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("FIX_LOV")) {

				FixedListDetail fixlistDet = FixedListDetail.fromXML(lovProvider);
				try {
					result = fixlistDet.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					logger.error("Cannot get values from fixed lov");
					throw new SpagoBIRuntimeException("Cannot get values from fixed lov", e);
				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("QUERY")) {

				QueryDetail qd = QueryDetail.fromXML(lovProvider);
				try {
					result = qd.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null, true);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					logger.error("Cannot get values from query");
					throw new SpagoBIRuntimeException("Cannot get values from query", e);
				}

			} else if (typeLov != null && typeLov.equalsIgnoreCase("SCRIPT")) {

				ScriptDetail scriptDetail = ScriptDetail.fromXML(lovProvider);
				try {
					result = scriptDetail.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					logger.error("Cannot get values from script");
					throw new SpagoBIRuntimeException("Cannot get values from script", e);
				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("DATASET")) {
				DatasetDetail datasetClassDetail = DatasetDetail.fromXML(lovProvider);
				try {
					result = datasetClassDetail.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {
					logger.error("Cannot get values from dataset");
					throw new SpagoBIRuntimeException("Cannot get values from dataset", e);
				}
			}
			Integer start = pagination.getInt("paginationStart");
			Integer limit = pagination.getInt("paginationLimit");
			lovExecutionResult.setValues(filterNulls(rowsSourceBean, colNames.size(), start, limit));
			lovExecutionResult.setFields(GridMetadataContainer.buildHeaderMapForGrid(colNames));
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			lovExecutionResult.setResults(rows.size());
			toReturn = lovExecutionResult.toJSONString();

		} catch (Exception e) {
			logger.error("Error reading body", e);
		}
		return toReturn;
	}

	@POST
	@Path("/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response post(@javax.ws.rs.core.Context HttpServletRequest req) {
		IModalitiesValueDAO modalitiesValueDAO;
		Integer id = null;
		try {

			String unsafe = RestUtilities.readBodyXSSUnsafe(req);
			JSONObject requestBodyJSON = new JSONObject(unsafe);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = toModality(requestBodyJSON);

			Integer lovId = null;
			if (requestBodyJSON.has("id"))
				lovId = requestBodyJSON.getInt("id");

			if (labelControl(lovId, modVal.getLabel())) {
				logger.error("LOV with same label already exists");
//				throw new SpagoBIRestServiceException("LOV with same label already exists", getLocale(), "LOV with same label already exists");

				return Response.status(Status.CONFLICT).build();
			} else {
				id = modalitiesValueDAO.insertModalitiesValue(modVal);

				logger.debug("OUT: Posting the LOV - done successfully");

				// int newID =
				// modalitiesValueDAO.loadModalitiesValueByLabel(modValue.getLabel()).getId();

			}

			return Response.ok(id).build();

		} catch (Exception exception) {

			logger.error("Error while posting LOV", exception);
			throw new SpagoBIServiceException("Error while posting LOV", exception);

		}
	}

	@DELETE
	@Path("/delete/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response remove(@PathParam("id") Integer id) {
		IModalitiesValueDAO modalitiesValueDAO;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = modalitiesValueDAO.loadModalitiesValueByID(id);
			boolean hasPar = DAOFactory.getModalitiesValueDAO().hasParameters(id.toString());
			if (hasPar) {
				logger.error("Lov cant be deleted it has parameters associated");
				throw new SpagoBIRuntimeException("Lov cannot be deleted it has parameters associated");
			}
			modalitiesValueDAO.eraseModalitiesValue(modVal);

			String encodedLov = URLEncoder.encode("" + modVal.getId(), "UTF-8");
			return Response.ok().entity(encodedLov).build();

		} catch (Exception exception) {

			logger.error("Error while deleting LOV", exception);
			throw new SpagoBIRestServiceException(exception.getLocalizedMessage(), buildLocaleFromSession(), exception);

		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response put(@javax.ws.rs.core.Context HttpServletRequest req) {

		logger.debug("IN");
		IModalitiesValueDAO modalitiesValueDAO;
		try {

			String unsafe = RestUtilities.readBodyXSSUnsafe(req);
			JSONObject requestBodyJSON = new JSONObject(unsafe);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = toModality(requestBodyJSON);
			String provider = modVal.getLovProvider();
			provider = provider.replace("<STMT>", "<STMT><![CDATA[");
			provider = provider.replace("</STMT>", "]]></STMT>");
			provider = provider.replace("<decoded_STMT>", "<decoded_STMT><![CDATA[");
			provider = provider.replace("</decoded_STMT>", "]]></decoded_STMT>");
			modVal.setLovProvider(provider);

			Integer lovId = null;
			if (requestBodyJSON.has("id"))
				lovId = requestBodyJSON.getInt("id");

			if (labelControl(lovId, modVal.getLabel())) {
				logger.error("LOV with same label already exists");
//				throw new SpagoBIRestServiceException("LOV with same label already exists", getLocale(), "LOV with same label already exists");

				return Response.status(Status.CONFLICT).build();
			} else {
				modalitiesValueDAO.modifyModalitiesValue(modVal);
				logger.debug("OUT: Putting the LOV - done successfully");

				return Response.ok().build();

			}
		} catch (Exception exception) {

			logger.error("Error while putting LOV", exception);
			throw new SpagoBIServiceException("Error while putting LOV", exception);

		}

	}

	@POST
	@Path("/checkdependecies")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public String checkDependencies(@javax.ws.rs.core.Context HttpServletRequest req) {

		IModalitiesValueDAO modalitiesValueDAO;
		JSONArray dependencies = new JSONArray();

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			String unsafe = RestUtilities.readBodyXSSUnsafe(req);
			JSONObject requestBodyJSON = new JSONObject(unsafe);
			String lovProv = requestBodyJSON.getString("provider");
			lovProv = StringEscapeUtils.unescapeXml(lovProv);
			ILovDetail lovDet = LovDetailFactory.getLovFromXML(lovProv);
			List profAttrToFill = getProfileAttributesToFill(lovDet);
			Set<String> paramsToFill = lovDet.getParameterNames();
			if (profAttrToFill.size() != 0) {
				for (Object profile : profAttrToFill) {
					JSONObject obj = new JSONObject();
					obj.put("name", profile);
					obj.put("type", "profile");
					dependencies.put(obj);
				}
			}

			if (paramsToFill.size() != 0) {
				for (String param : paramsToFill) {
					JSONObject obj = new JSONObject();
					obj.put("name", param);
					obj.put("type", "parameter");
					dependencies.put(obj);
				}
			}

		} catch (Exception exception) {

			logger.error("Could not get dependencies", exception);
			throw new SpagoBIServiceException("Could not get dependencies", exception);

		}

		return dependencies.toString();

	}

	private ModalitiesValue toModality(JSONObject requestBodyJSON) throws EMFUserError, SerializationException, SourceBeanException {

		logger.debug("IN");

		ModalitiesValue lovToReturn = new ModalitiesValue();

		Integer id = -1;
		if (requestBodyJSON.opt("id") != null) {

			if (requestBodyJSON.opt("id").getClass() == Integer.class) {
				id = (Integer) requestBodyJSON.opt("id");
			} else {
				id = new Integer((String) requestBodyJSON.opt("id"));

			}
		}

		String lovName = (String) requestBodyJSON.opt("name");
		String lovDecription = (String) requestBodyJSON.opt("description");
		String lovLabel = (String) requestBodyJSON.opt("label");
		String lovSelType = (String) requestBodyJSON.opt("SELECTION_TYPE");
		if (lovSelType == null) {
			lovSelType = "";
		}

		String lovProvider = (String) requestBodyJSON.opt("lovProvider");
		String lovInputTypeCD = (String) requestBodyJSON.opt("itypeCd");

		String lovInputTypeID = (String) requestBodyJSON.opt("itypeId");

		Integer dataSetID = -1;

		Assert.assertNotNull(lovName, "LOV name cannot be null");
		Assert.assertNotNull(lovProvider, "LOV provider cannot be null");
		Assert.assertNotNull(lovInputTypeCD, "LOV input type cannot be null");
		Assert.assertNotNull(lovInputTypeID, "LOV input type ID cannot be null");
		Assert.assertNotNull(lovLabel, "LOV label cannot be null");
		Assert.assertNotNull(lovSelType, "LOV selection type cannot be null");

		if (lovInputTypeCD.equalsIgnoreCase("DATASET")) {

			DatasetDetail dataSetDetail = (DatasetDetail) LovDetailFactory.getLovFromXML(lovProvider);
			dataSetID = Integer.parseInt(dataSetDetail.getDatasetId());

			logger.debug(dataSetDetail.getDatasetId());
		}

		lovToReturn.setId(id);
		lovToReturn.setName(lovName);
		lovToReturn.setDescription(lovDecription);
		lovToReturn.setLovProvider(lovProvider);
		lovToReturn.setITypeCd(lovInputTypeCD);
		lovToReturn.setITypeId(lovInputTypeID);
		lovToReturn.setLabel(lovLabel);
		lovToReturn.setSelectionType(lovSelType);
		lovToReturn.setDatasetID(dataSetID);

		logger.debug("OUT: Recovering data of the LOV from JSON object - done successfully");

		return lovToReturn;

	}

	public boolean labelControl(Integer lovId, String newLabel) {
		List<ModalitiesValue> modalitiesValues = null;
		IModalitiesValueDAO modalitiesValueDAO = null;

		try {
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();
			for (ModalitiesValue lov : modalitiesValues) {
				if ((lovId == null || (lovId != null && !lovId.equals(lov.getId()))) && lov.getLabel().equalsIgnoreCase(newLabel)) {
					return true;
				}
			}
		} catch (EMFUserError e) {
			logger.error("Error while getting the list of LOVs", e);
			throw new SpagoBIServiceException("Error while getting the list of LOVs", e);
		}
		return false;
	}

	private ModalitiesValue recoverModalitiesValueDetails(JSONObject requestBodyJSON) throws EMFUserError, SerializationException, SourceBeanException {

		logger.debug("IN");

		ModalitiesValue lovToReturn = new ModalitiesValue();

		Integer id = -1;

		if (requestBodyJSON.opt("LOV_ID").getClass() == Integer.class) {
			// idString = Integer.toString((int) requestBodyJSON.opt("LOV_ID"));
			id = (Integer) requestBodyJSON.opt("LOV_ID");
		} else {
			id = new Integer((String) requestBodyJSON.opt("LOV_ID"));

		}

		// if (idString != null && idString != "") {
		// id = new Integer(idString);
		// }

		String lovName = (String) requestBodyJSON.opt("LOV_NAME");
		String lovDecription = (String) requestBodyJSON.opt("LOV_DESCRIPTION");
		String lovLabel = (String) requestBodyJSON.opt("LOV_LABEL");
		String lovSelType = (String) requestBodyJSON.opt("SELECTION_TYPE");

		String lovProvider = (String) requestBodyJSON.opt("LOV_PROVIDER");
		String lovInputTypeCD = (String) requestBodyJSON.opt("I_TYPE_CD");
		String lovInputTypeID = (String) requestBodyJSON.opt("I_TYPE_ID");

		Integer dataSetID = -1;

		Assert.assertNotNull(lovName, "LOV name cannot be null");
		Assert.assertNotNull(lovDecription, "LOV description cannot be null");
		Assert.assertNotNull(lovProvider, "LOV provider cannot be null");
		Assert.assertNotNull(lovInputTypeCD, "LOV input type cannot be null");
		Assert.assertNotNull(lovInputTypeID, "LOV input type ID cannot be null");
		Assert.assertNotNull(lovLabel, "LOV label cannot be null");
		Assert.assertNotNull(lovSelType, "LOV selection type cannot be null");

		if (lovInputTypeCD.equalsIgnoreCase("DATASET")) {

			DatasetDetail dataSetDetail = (DatasetDetail) LovDetailFactory.getLovFromXML(lovProvider);
			dataSetID = Integer.parseInt(dataSetDetail.getDatasetId());

			logger.debug(dataSetDetail.getDatasetId());
		}

		lovToReturn.setId(id);
		lovToReturn.setName(lovName);
		lovToReturn.setDescription(lovDecription);
		lovToReturn.setLovProvider(lovProvider);
		lovToReturn.setITypeCd(lovInputTypeCD);
		lovToReturn.setITypeId(lovInputTypeID);
		lovToReturn.setLabel(lovLabel);
		lovToReturn.setSelectionType(lovSelType);
		lovToReturn.setDatasetID(dataSetID);

		logger.debug("OUT: Recovering data of the LOV from JSON object - done successfully");

		return lovToReturn;

	}

	private List getProfileAttributesToFill(ILovDetail lovDet) {
		List attrsToFill = new ArrayList();
		try {
			Collection userAttrNames = getUserProfile().getUserAttributeNames();
			List attrsRequired = lovDet.getProfileAttributeNames();
			Iterator attrsReqIter = attrsRequired.iterator();
			while (attrsReqIter.hasNext()) {
				String attrName = (String) attrsReqIter.next();
				if (!userAttrNames.contains(attrName)) {
					attrsToFill.add(attrName);
				}
			}
		} catch (Exception e) {
			logger.error("Error while checking the profile " + "attributes required for test", e);
		}
		return attrsToFill;
	}

	private UserProfile insertIntoFakeUser(JSONArray profiles) throws EMFInternalError, JSONException {

		// create a fake user profile
		UserProfile currentUserProfile = getUserProfile();
		UserProfile userProfile = new UserProfile((String) currentUserProfile.getUserId(), currentUserProfile.getOrganization());
		// copy all the roles, functionalities of the original profile
		userProfile.setFunctionalities(getUserProfile().getFunctionalities());
		userProfile.setRoles(getUserProfile().getRolesForUse());

		// copy attributes and add the missing ones
		Map attributes = new HashMap();
		Collection origAttrNames = getUserProfile().getUserAttributeNames();
		Iterator origAttrNamesIter = origAttrNames.iterator();
		while (origAttrNamesIter.hasNext()) {
			String profileAttrName = (String) origAttrNamesIter.next();
			String profileAttrValue = getUserProfile().getUserAttribute(profileAttrName).toString();
			attributes.put(profileAttrName, profileAttrValue);
		}
		for (int i = 0; i < profiles.length(); i++) {
			String profileAttrName = profiles.getJSONObject(i).getString("name");
			String profileAttrValue = profiles.getJSONObject(i).getString("value");
			attributes.put(profileAttrName, profileAttrValue);

		}

		userProfile.setAttributes(attributes);
		return userProfile;

	}

	private List<BIObjectParameter> toMockedBIObjectParameters(Map<String, String> parameters) {
		List<BIObjectParameter> objParams;
		if (parameters == null || parameters.isEmpty()) {
			return null;
		} else {
			objParams = new ArrayList<BIObjectParameter>(parameters.size());
			for (String parameterName : parameters.keySet()) {
				String parameterValue = parameters.get(parameterName);
				if (parameterValue == null) {
					logger.error("There is no name-value mapping for parameter [" + parameterName + "].");
					throw new SpagoBIRuntimeException("Error while retrieving the value for parameter [" + parameterName + "].");
				} else {
					BIObjectParameter objParam = new BIObjectParameter();
					Parameter parameterDefinition = new Parameter();
					parameterDefinition.setLabel(parameterName);
					objParam.setParameter(parameterDefinition);
					if (parameterValue.contains(",")) {
						objParam.setParameterValues(Arrays.asList(parameterValue.split(",")));
					} else {
						objParam.setParameterValues(Arrays.asList(parameterValue));
					}
					objParams.add(objParam);
				}
			}
			return objParams;
		}
	}

	private List<String> findFirstRowAttributes(SourceBean rowsSourceBean) {
		List<String> columnsNames = new ArrayList<String>();
		if (rowsSourceBean != null) {
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() != 0) {
				SourceBean row = (SourceBean) rows.get(0);
				List rowAttrs = row.getContainedAttributes();
				Iterator rowAttrsIter = rowAttrs.iterator();
				while (rowAttrsIter.hasNext()) {
					SourceBeanAttribute rowAttr = (SourceBeanAttribute) rowAttrsIter.next();
					columnsNames.add(rowAttr.getKey());
				}
			}
		}
		return columnsNames;
	}

	private List<Map<String, String>> filterNulls(SourceBean rowsSourceBean, int numCols, Integer start, Integer limit) throws JSONException {
		Map<String, String> map;
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		int startIter = 0;
		int endIter;

		if (start != null) {
			startIter = start;
		}

		if (rowsSourceBean != null) {
			List<SourceBean> rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() > 0) {
				if (limit != null && limit > 0) {
					endIter = startIter + limit;
					if (endIter > rows.size()) {
						endIter = rows.size();
					}
				} else {
					endIter = rows.size();
				}

				for (int i = startIter; i < endIter; i++) {
					List<SourceBeanAttribute> rowAttrs = (rows.get(i)).getContainedAttributes();
					Iterator<SourceBeanAttribute> rowAttrsIter = rowAttrs.iterator();
					map = new HashMap<String, String>();
					while (rowAttrsIter.hasNext()) {
						SourceBeanAttribute rowAttr = rowAttrsIter.next();
						map.put(rowAttr.getKey(), (rowAttr.getValue()).toString());
					}
					if (map.keySet().size() < numCols) {
						logger.warn("Row [" + rows.get(i) + "] contains some null values. It will be skipped.");
					} else {
						list.add(map);
					}
				}
			}
		}
		return list;
	}

	public String convertSpecialChars(String provider) {
		/*
		 * String converted = ""; String script = ""; String query = ""; int startInd = 0; int endId = 0; if (provider.indexOf("<SCRIPT>") != -1) { startInd =
		 * provider.indexOf("<SCRIPT>"); endId = provider.indexOf("</SCRIPT>"); startInd = startInd +8; script = provider.substring(startInd, endId); script =
		 * script.trim(); }
		 *
		 * if (provider.indexOf("<STMT>") != -1) { startInd = provider.indexOf("<STMT>"); endId = provider.indexOf("</STMT>"); startInd = startInd +6; query =
		 * provider.substring(startInd, endId); query = query.trim();
		 *
		 * }
		 *
		 * if (!script.isEmpty()) { converted = script; } else { converted = query; }
		 *
		 * if (converted.contains("'")) { converted = converted.replaceAll("'", "&#x27;"); } if (converted.contains("<")) { converted =
		 * converted.replaceAll("<", "&lt;"); } if (converted.contains(">")) { converted = converted.replaceAll(">", "&gt;"); }
		 *
		 * if (converted.contains("&")) { converted = converted.replaceAll("&", "&amp;"); }
		 *
		 * if (!converted.isEmpty()) { provider = provider.replace(provider.substring(startInd, endId), converted);
		 *
		 * }
		 */

		if (provider.contains("<SCRIPT>")) {
			// SourceBean sb =
			// SourceBean.fromXMLString(lovProvider).getContainedAttributes()[0];
			// sb.getContainedAttributes()
			provider = provider.replace("<SCRIPT><![CDATA[", "<SCRIPT>");
			provider = provider.replace("]]></SCRIPT>", "</SCRIPT>");
			int pos1 = provider.indexOf("<SCRIPT>");
			int pos2 = provider.indexOf("</SCRIPT>");
			String content = provider.substring(pos1 + 8, pos2);
			content = StringEscapeUtils.unescapeHtml4(content);
			Base64.Encoder bASE64Encoder = Base64.getEncoder();
			String encoded = bASE64Encoder.encodeToString(content.getBytes());
			provider = provider.substring(0, pos1 + 8) + encoded + provider.substring(pos2);

		}

		if (provider.contains("<STMT>")) {
			// SourceBean sb =
			// SourceBean.fromXMLString(provider).getContainedAttributes()[0];
			// sb.getContainedAttributes()
			provider = provider.replace("<STMT><![CDATA[", "<STMT>");
			provider = provider.replace("]]></STMT>", "</STMT>");
			int pos1 = provider.indexOf("<STMT>");
			int pos2 = provider.indexOf("</STMT>");
			String content = provider.substring(pos1 + 6, pos2);
			content = StringEscapeUtils.unescapeHtml4(content);
			Base64.Encoder bASE64Encoder = Base64.getEncoder();
			String encoded = bASE64Encoder.encodeToString(content.getBytes());
			provider = provider.substring(0, pos1 + 6) + encoded + provider.substring(pos2);

		}

		return provider;

	}

	private List<Parameter> getDriversByLovId(Integer lovId) {
		IParameterUseDAO useModesDao = null;
		List<ParameterUse> modes = null;
		IParameterDAO driversDao = null;

		List<Parameter> drivers = new ArrayList<Parameter>();
		List<Parameter> driversToReturn = new ArrayList<Parameter>();

		try {
			useModesDao = DAOFactory.getParameterUseDAO();
			useModesDao.setUserProfile(getUserProfile());
			modes = useModesDao.getParameterUsesAssociatedToLov(lovId);
			driversDao = DAOFactory.getParameterDAO();
			driversDao.setUserProfile(getUserProfile());
			drivers = driversDao.loadAllParameters();

			for (int i = 0; i < drivers.size(); i++) {
				List<ParameterUse> driverModes = useModesDao.loadParametersUseByParId(drivers.get(i).getId());
				for (int j = 0; j < driverModes.size(); j++) {
					ParameterUse driverMode = driverModes.get(j);
					for (int k = 0; k < modes.size(); k++) {
						if (driverMode.getId() == modes.get(k).getId()) {
							if (!driversToReturn.contains(drivers.get(i))) {
								driversToReturn.add(drivers.get(i));
							}

						}
					}
				}
			}
			return driversToReturn;
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}

	}
}