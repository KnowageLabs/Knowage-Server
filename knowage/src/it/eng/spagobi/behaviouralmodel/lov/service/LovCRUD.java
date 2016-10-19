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
package it.eng.spagobi.behaviouralmodel.lov.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

@Path("/2.0/lovs")
@ManageAuthorization
public class LovCRUD extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(LovCRUD.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public List<ModalitiesValue> getAllListOfValues() {

		logger.debug("IN");

		List<ModalitiesValue> modalitiesValues = null;
		IModalitiesValueDAO modalitiesValueDAO = null;

		try {

			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();

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

	@POST
	@Path("/preview")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public String preview(@javax.ws.rs.core.Context HttpServletRequest req) {
		logger.debug("IN");

		JSONObject pagination = new JSONObject();
		JSONObject data = new JSONObject();
		GridMetadataContainer lovExecutionResult = new GridMetadataContainer();
		SourceBean rowsSourceBean = null;
		List<String> colNames = new ArrayList<String>();
		String result = null;
		String toReturn = null;
		String typeLov = null;
		String lovProvider = null;

		Map<String, String> paramFilled = (Map<String, String>) getAttributeFromHttpSession(SpagoBIConstants.PARAMETERS_FILLED);

		try {
			IEngUserProfile profile = getUserProfile();
			String unsafe = RestUtilities.readBodyXSSUnsafe(req);
			JSONObject paramsObj = new JSONObject(unsafe);
			pagination = paramsObj.getJSONObject("pagination");
			data = paramsObj.getJSONObject("data");
			typeLov = data.getString("itypeCd");
			lovProvider = data.getString("lovProvider");
			if (typeLov != null && typeLov.equalsIgnoreCase("JAVA_CLASS")) {
				JavaClassDetail javaClassDetail = JavaClassDetail.fromXML(lovProvider);
				try {
					String javaClassName = javaClassDetail.getJavaClassName();
					IJavaClassLov javaClassLov = (IJavaClassLov) Class.forName(javaClassName).newInstance();
					result = javaClassLov.getValues(profile);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {

				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("FIX_LOV")) {
				FixedListDetail fixlistDet = FixedListDetail.fromXML(lovProvider);
				try {
					result = fixlistDet.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {

				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("QUERY")) {
				QueryDetail qd = QueryDetail.fromXML(lovProvider);
				try {
					result = qd.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null, true);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {

				}

			} else if (typeLov != null && typeLov.equalsIgnoreCase("SCRIPT")) {
				ScriptDetail scriptDetail = ScriptDetail.fromXML(lovProvider);
				try {
					result = scriptDetail.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {

				}
			} else if (typeLov != null && typeLov.equalsIgnoreCase("DATASET")) {
				DatasetDetail datasetClassDetail = DatasetDetail.fromXML(lovProvider);
				try {
					result = datasetClassDetail.getLovResult(profile, null, toMockedBIObjectParameters(paramFilled), null);
					rowsSourceBean = SourceBean.fromXMLString(result);
					colNames = findFirstRowAttributes(rowsSourceBean);
				} catch (Exception e) {

				}
			}
			Integer start = pagination.getInt("paginationStart");
			Integer limit = pagination.getInt("paginationLimit");
			lovExecutionResult.setValues(toList(rowsSourceBean, start, limit));
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
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response post(@javax.ws.rs.core.Context HttpServletRequest req) {
		IModalitiesValueDAO modalitiesValueDAO;
		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = toModality(requestBodyJSON);
			modalitiesValueDAO.insertModalitiesValue(modVal);

			logger.debug("OUT: Posting the LOV - done successfully");

			// int newID =
			// modalitiesValueDAO.loadModalitiesValueByLabel(modValue.getLabel()).getId();

			return Response.ok().build();

		} catch (Exception exception) {

			logger.error("Error while posting LOV", exception);
			throw new SpagoBIServiceException("Error while posting LOV", exception);

		}
	}

	@POST
	@Path("/deleteSmth")
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response remove(@javax.ws.rs.core.Context HttpServletRequest req) {

		IModalitiesValueDAO modalitiesValueDAO;

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = toModality(requestBodyJSON);
			modalitiesValueDAO.eraseModalitiesValue(modVal);

			logger.debug("OUT: Posting the LOV - done successfully");

			return Response.ok().build();

		} catch (Exception exception) {

			logger.error("Error while deleting LOV", exception);
			throw new SpagoBIServiceException("Error while deleting LOV", exception);

		}
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response put(@javax.ws.rs.core.Context HttpServletRequest req) {

		logger.debug("IN");
		IModalitiesValueDAO modalitiesValueDAO;
		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesValueDAO.setUserProfile(getUserProfile());
			ModalitiesValue modVal = toModality(requestBodyJSON);
			modalitiesValueDAO.modifyModalitiesValue(modVal);
			logger.debug("OUT: Putting the LOV - done successfully");

			return Response.ok().build();

		} catch (Exception exception) {

			logger.error("Error while putting LOV", exception);
			throw new SpagoBIServiceException("Error while putting LOV", exception);

		}

	}

	@DELETE
	// @Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.LOVS_MANAGEMENT })
	public Response delete(@Context HttpServletRequest servletRequest) {

		logger.debug("IN");

		try {

			JSONObject requestJSONObject = RestUtilities.readBodyAsJSONObject(servletRequest);
			ModalitiesValue modVal = recoverModalitiesValueDetails(requestJSONObject);
			IModalitiesValueDAO modalitiesDAO = DAOFactory.getModalitiesValueDAO();
			modalitiesDAO.setUserProfile(getUserProfile());
			modalitiesDAO.eraseModalitiesValue(modVal);

			logger.debug("OUT: Deleting the LOV - done successfully");

			return Response.ok().build();

		} catch (Exception exception) {

			logger.error("Error while deleting LOV", exception);
			throw new SpagoBIServiceException("Error while deleting LOV", exception);
			// return Response.status(Status.NOT_MODIFIED).build();

		}

	}

	// private JSONArray serializeModalitiesValues(List<ModalitiesValue>
	// modalitiesValues) {
	//
	// logger.debug("IN");
	//
	// JSONArray modalitiesValuesJSONArray = new JSONArray();
	//
	// Assert.assertNotNull(modalitiesValues, "Input object cannot be null");
	//
	// try {
	//
	// modalitiesValuesJSONArray = (JSONArray)
	// SerializerFactory.getSerializer("application/json").serialize(modalitiesValues,
	// null);
	// logger.debug("OUT: Serializing the list of LOVs - done successfully");
	//
	// } catch (SerializationException exception) {
	//
	// logger.error("Error while serializing list of LOVs", exception);
	// throw new SpagoBIServiceException("Error while serializing list of LOVs",
	// exception);
	//
	// }
	//
	// return modalitiesValuesJSONArray;
	//
	// }
	//
	// private JSONObject serializeModalitiesValues(ModalitiesValue
	// modalitiesValue) {
	//
	// logger.debug("IN");
	//
	// JSONObject modalitiesValuesJSON = new JSONObject();
	// Assert.assertNotNull(modalitiesValue, "Input object cannot be null");
	//
	// try {
	// modalitiesValuesJSON = (JSONObject)
	// SerializerFactory.getSerializer("application/json").serialize(modalitiesValue,
	// null);
	// logger.debug("OUT: Serializing one LOV - done successfully");
	//
	// } catch (SerializationException exception) {
	//
	// logger.error("Error while serializing list one LOV", exception);
	// throw new SpagoBIServiceException("Error while serializing one LOV",
	// exception);
	//
	// }
	//
	// return modalitiesValuesJSON;
	//
	// }

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

	private List<Map<String, String>> toList(SourceBean rowsSourceBean, Integer start, Integer limit) throws JSONException {
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
					JSONObject rowJson = new JSONObject();
					List<SourceBeanAttribute> rowAttrs = (rows.get(i)).getContainedAttributes();
					Iterator<SourceBeanAttribute> rowAttrsIter = rowAttrs.iterator();
					map = new HashMap<String, String>();
					while (rowAttrsIter.hasNext()) {
						SourceBeanAttribute rowAttr = rowAttrsIter.next();
						map.put(rowAttr.getKey(), (rowAttr.getValue()).toString());
					}
					list.add(map);
				}
			}
		}
		return list;
	}
}