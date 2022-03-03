package it.eng.spagobi.api.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.api.BusinessModelOpenParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class MetaUtils {

	private static final Logger LOGGER = LogManager.getLogger(MetaUtils.class);

	private static final MetaUtils INSTANCE = new MetaUtils();

	public static final MetaUtils getInstance() {
		return INSTANCE;
	}

	private MetaUtils() {

	}

	public ArrayList<HashMap<String, Object>> getQbeDrivers(UserProfile userProfile, Locale locale, BIObject biObject) {
		IDataSet dataset = null;
		ArrayList datasetList = null;
		BIObjDataSet biObjDataSet = null;
		Integer dsId = null;
		List docDrivers = null;
		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		String businessModelName = null;
		MetaModel businessModel = null;
		ArrayList<BIObjDataSet> biObjDataSetList = null;
		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		try {

			biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				biObjDataSet = (BIObjDataSet) itDs.next();
				dsId = biObjDataSet.getDataSetId();
				dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
					String config = dataset.getConfiguration();
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					businessModelName = (String) jsonConfig.get("qbeDatamarts");
					parametersArrayList = getQbeDrivers(userProfile, locale, businessModelName);
					if (parametersArrayList != null && !parametersArrayList.isEmpty())
						break;
				}

			}
		} catch (Exception e) {
			LOGGER.error("Cannot retrieve drivers list", e);
			throw new SpagoBIRuntimeException("Cannot retrieve drivers list for object " + biObject, e);
		}
		return parametersArrayList;
	}

	public ArrayList<HashMap<String, Object>> getQbeDrivers(UserProfile userProfile, Locale locale, String businessModelName) {
		ArrayList<HashMap<String, Object>> parametersArrayList;
		parametersArrayList = getDatasetDriversByModelName(userProfile, locale, businessModelName, false);
		return parametersArrayList;
	}



	private ArrayList<HashMap<String, Object>> getDatasetDriversByModelName(UserProfile userProfile, Locale locale, String businessModelName, Boolean loadDSwithDrivers) {
		ArrayList<HashMap<String, Object>> parametersArrList = new ArrayList<>();
		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		List<BusinessModelDriverRuntime> parameters = new ArrayList<>();
		BusinessModelOpenParameters BMOP = new BusinessModelOpenParameters();
		String role;
		try {
			role = userProfile.getRoles().contains("admin") ? "admin" : (String) userProfile.getRoles().iterator().next();
		} catch (EMFInternalError e2) {
			// TODO : Return a significative error message
			LOGGER.debug(e2.getCause(), e2);
			throw new SpagoBIRuntimeException(e2.getMessage(), e2);
		}
		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(businessModelName, role, loadDSwithDrivers);
		if (businessModel == null) {
			return null;
		}
		BusinessModelRuntime dum = new BusinessModelRuntime(userProfile, null);
		parameters = BusinessModelOpenUtils.getParameters(businessModel, role, locale, null, true, dum);
		parametersArrList = transformRuntimeDrivers(parameters, parameterUseDAO, role, businessModel, BMOP);

		return parametersArrList;
	}

	private ArrayList<HashMap<String, Object>> transformRuntimeDrivers(List<BusinessModelDriverRuntime> parameters, IParameterUseDAO parameterUseDAO,
			String role, MetaModel businessModel, BusinessModelOpenParameters BMOP) {
		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		ParameterUse parameterUse;
		for (BusinessModelDriverRuntime objParameter : parameters) {
			Integer paruseId = objParameter.getParameterUseId();
			try {
				parameterUse = parameterUseDAO.loadByUseID(paruseId);
			} catch (EMFUserError e1) {
				// TODO : Return a significative error message
				LOGGER.debug(e1.getCause(), e1);
				throw new SpagoBIRuntimeException(e1.getMessage(), e1);
			}

			HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();
			parameterAsMap.put("id", objParameter.getBiObjectId());
			parameterAsMap.put("label", objParameter.getLabel());
			parameterAsMap.put("urlName", objParameter.getId());
			parameterAsMap.put("type", objParameter.getParType());
			parameterAsMap.put("typeCode", objParameter.getTypeCode());
			parameterAsMap.put("selectionType", objParameter.getSelectionType());
			parameterAsMap.put("valueSelection", parameterUse.getValueSelection());
			parameterAsMap.put("selectedLayer", objParameter.getSelectedLayer());
			parameterAsMap.put("selectedLayerProp", objParameter.getSelectedLayerProp());
			parameterAsMap.put("visible", ((objParameter.isVisible())));
			parameterAsMap.put("mandatory", ((objParameter.isMandatory())));
			parameterAsMap.put("multivalue", objParameter.isMultivalue());
			parameterAsMap.put("driverLabel", objParameter.getPar().getLabel());
			parameterAsMap.put("driverUseLabel", objParameter.getAnalyticalDriverExecModality().getLabel());

			parameterAsMap.put("allowInternalNodeSelection",
					objParameter.getPar().getModalityValue().getLovProvider().contains("<LOVTYPE>treeinner</LOVTYPE>"));

			// get values
			if (objParameter.getDriver().getParameterValues() != null) {

				List paramValueLst = new ArrayList();
				List paramDescrLst = new ArrayList();
				Object paramValues = objParameter.getDriver().getParameterValues();
				Object paramDescriptionValues = objParameter.getDriver().getParameterValuesDescription();

				if (paramValues instanceof List) {

					List<String> valuesList = (List) paramValues;
					List<String> descriptionList = (List) paramDescriptionValues;
					if (paramDescriptionValues == null || !(paramDescriptionValues instanceof List)) {
						descriptionList = new ArrayList<String>();
					}

					// String item = null;
					for (int k = 0; k < valuesList.size(); k++) {

						String itemVal = valuesList.get(k);

						String itemDescr = descriptionList.size() > k && descriptionList.get(k) != null ? descriptionList.get(k) : itemVal;

						try {
							// % character breaks decode method
							if (!itemVal.contains("%")) {
								itemVal = URLDecoder.decode(itemVal, "UTF-8");
							}
							if (!itemDescr.contains("%")) {
								itemDescr = URLDecoder.decode(itemDescr, "UTF-8");
							}

							// check input value and convert if it's an old multivalue syntax({;{xxx;yyy}STRING}) to list of values :["A-OMP", "A-PO", "CL"]
							if (objParameter.isMultivalue() && itemVal.indexOf("{") >= 0) {
								String sep = itemVal.substring(1, 2);
								String val = itemVal.substring(3, itemVal.indexOf("}"));
								String[] valLst = val.split(sep);
								for (int k2 = 0; k2 < valLst.length; k2++) {
									String itemVal2 = valLst[k2];
									if (itemVal2 != null && !"".equals(itemVal2))
										paramValueLst.add(itemVal2);
								}
							} else {
								if (itemVal != null && !"".equals(itemVal))
									paramValueLst.add(itemVal);
								paramDescrLst.add(itemDescr);
							}
						} catch (UnsupportedEncodingException e) {
							LOGGER.debug("An error occured while decoding parameter with value[" + itemVal + "]" + e);
						}
					}
				} else if (paramValues instanceof String) {
					// % character breaks decode method
					if (!((String) paramValues).contains("%")) {
						try {
							paramValues = URLDecoder.decode((String) paramValues, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							LOGGER.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
					}
					paramValueLst.add(paramValues.toString());

					String parDescrVal = paramDescriptionValues != null && paramDescriptionValues instanceof String ? paramDescriptionValues.toString()
							: paramValues.toString();
					if (!parDescrVal.contains("%")) {
						try {
							parDescrVal = URLDecoder.decode(parDescrVal, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							LOGGER.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
					}
					paramDescrLst.add(parDescrVal);
				}

				parameterAsMap.put("parameterValue", paramValueLst);
				parameterAsMap.put("parameterDescription", paramDescriptionValues);
			}

			boolean showParameterLov = true;

			// Parameters NO TREE
			if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())
					&& !objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_TREE)) {

				ArrayList<HashMap<String, Object>> admissibleValues = objParameter.getAdmissibleValues();

				if (!objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_LOOKUP)) {
					parameterAsMap.put("defaultValues", admissibleValues);
				} else {
					parameterAsMap.put("defaultValues", new ArrayList<>());
				}
				parameterAsMap.put("defaultValuesMeta", objParameter.getLovVisibleColumnsNames());
				parameterAsMap.put(DocumentExecutionUtils.VALUE_COLUMN_NAME_METADATA, objParameter.getLovValueColumnName());
				parameterAsMap.put(DocumentExecutionUtils.DESCRIPTION_COLUMN_NAME_METADATA, objParameter.getLovDescriptionColumnName());

				// hide the parameter if is mandatory and have one value in lov (no error parameter)
				if (admissibleValues != null && admissibleValues.size() == 1 && objParameter.isMandatory() && !admissibleValues.get(0).containsKey("error")
						&& (objParameter.getDataDependencies() == null || objParameter.getDataDependencies().isEmpty())
						&& (objParameter.getLovDependencies() == null || objParameter.getLovDependencies().isEmpty())) {
					showParameterLov = false;
				}

				// if parameterValue is not null and is array, check if all element are present in lov
				Object values = parameterAsMap.get("parameterValue");
				if (values != null && admissibleValues != null) {
					BMOP.checkIfValuesAreAdmissible(values, admissibleValues);
				}
			}

			// DATE RANGE DEFAULT VALUE
			if (objParameter.getParType().equals("DATE_RANGE")) {
				try {
					ArrayList<HashMap<String, Object>> defaultValues = BMOP.manageDataRange(businessModel, role, objParameter.getId());
					parameterAsMap.put("defaultValues", defaultValues);
				} catch (SerializationException | EMFUserError | JSONException | IOException e) {
					LOGGER.debug("Filters DATE RANGE ERRORS ", e);
				}
			}

			// convert the parameterValue from array of string in array of object
			DefaultValuesList parameterValueList = new DefaultValuesList();
			Object oVals = parameterAsMap.get("parameterValue");
			Object oDescr = parameterAsMap.get("parameterDescription") != null ? parameterAsMap.get("parameterDescription") : new ArrayList<String>();

			if (oVals != null) {
				if (oVals instanceof List) {
					// CROSS NAV : INPUT PARAM PARAMETER TARGET DOC IS STRING
					if (oVals.toString().startsWith("[") && oVals.toString().endsWith("]") && parameterUse.getValueSelection().equals("man_in")) {
						List<String> valList = (ArrayList) oVals;
						String stringResult = "";
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							if (objParameter.getParType().equals("STRING") && objParameter.isMultivalue()) {
								stringResult += "'" + itemVal + "'";
							} else {
								stringResult += itemVal;
							}
							if (k != valList.size() - 1) {
								stringResult += ",";
							}
						}
						LovValue defValue = new LovValue();
						defValue.setValue(stringResult);
						defValue.setDescription(stringResult);
						parameterValueList.add(defValue);
					} else {
						List<String> valList = (ArrayList) oVals;
						List<String> descrList = (ArrayList) oDescr;
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							String itemDescr = descrList.size() > k ? descrList.get(k) : itemVal;
							LovValue defValue = new LovValue();
							defValue.setValue(itemVal);
							defValue.setDescription(itemDescr != null ? itemDescr : itemVal);
							parameterValueList.add(defValue);
						}
					}
					parameterAsMap.put("parameterValue", parameterValueList);
				}
			}

			parameterAsMap.put("dependsOn", objParameter.getDependencies());
			parameterAsMap.put("dataDependencies", objParameter.getDataDependencies());
			parameterAsMap.put("visualDependencies", objParameter.getVisualDependencies());
			parameterAsMap.put("lovDependencies", (objParameter.getLovDependencies() != null) ? objParameter.getLovDependencies() : new ArrayList<>());

			// load DEFAULT VALUE if present and if the parameter value is empty
			if (objParameter.getDefaultValues() != null && objParameter.getDefaultValues().size() > 0
					&& objParameter.getDefaultValues().get(0).getValue() != null) {
				DefaultValuesList valueList = null;
				// check if the parameter is really valorized (for example if it isn't an empty list)
				List lstValues = (List) parameterAsMap.get("parameterValue");
				// if (lstValues.size() == 0)
				// jsonCrossParameters.remove(objParameter.getId());

				String parLab = objParameter.getDriver() != null && objParameter.getDriver().getParameter() != null
						? objParameter.getDriver().getParameter().getLabel()
						: "";
				String useModLab = objParameter.getAnalyticalDriverExecModality() != null ? objParameter.getAnalyticalDriverExecModality().getLabel() : "";
				String sessionKey = parLab + "_" + useModLab;

				valueList = objParameter.getDefaultValues();

				// in every case fill default values!
				parameterAsMap.put("driverDefaultValue", valueList);
			}

			if (!showParameterLov) {
				parameterAsMap.put("showOnPanel", "false");
			} else {
				parameterAsMap.put("showOnPanel", "true");
			}
			parametersArrayList.add(parameterAsMap);

		}
		for (int z = 0; z < parametersArrayList.size(); z++) {

			Map docP = parametersArrayList.get(z);
			DefaultValuesList defvalList = (DefaultValuesList) docP.get("parameterValue");
			if (defvalList != null && defvalList.size() == 1) {
				LovValue defval = defvalList.get(0);
				if (defval != null) {
					Object val = defval.getValue();
					if (val != null && val.equals("$")) {
						docP.put("parameterValue", "");
					}
				}

			}
		}
		return parametersArrayList;
	}

	public JSONArray getChildrenForTreeLov(ILovDetail lovProvDet, List rows, int treeLovNodeLevel, String treeLovNodeValue) {
		String valueColumn;
		String descriptionColumn;
		boolean addNode;
		String treeLovNodeName = "";
		String treeLovParentNodeName = "";

		try {

			if (treeLovNodeValue == "lovroot") {// root node
				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(0).getFirst();
				treeLovParentNodeName = "lovroot";
				treeLovNodeLevel = -1;
			} else if (lovProvDet.getTreeLevelsColumns().size() > treeLovNodeLevel + 1) {// treeLovNodeLevel-1
				// because
				// the
				// fake
				// root
				// node
				// is
				// the
				// level
				// 0

				treeLovNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel + 1).getFirst();
				treeLovParentNodeName = lovProvDet.getTreeLevelsColumns().get(treeLovNodeLevel).getFirst();
			}

			Set<JSONObject> valuesDataJSON = new LinkedHashSet<JSONObject>();

			valueColumn = lovProvDet.getValueColumnName();
			descriptionColumn = lovProvDet.getDescriptionColumnName();

			for (int q = 0; q < rows.size(); q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = null;
				addNode = false;
				List columns = row.getContainedAttributes();
				valueJSON = new JSONObject();
				boolean notNullNode = false; // if the row does not contain the
				// value atribute we don't add
				// the node
				for (int i = 0; i < columns.size(); i++) {
					SourceBeanAttribute attribute = (SourceBeanAttribute) columns.get(i);
					if ((treeLovParentNodeName == "lovroot") || (attribute.getKey().equalsIgnoreCase(treeLovParentNodeName)
							&& (attribute.getValue().toString()).equalsIgnoreCase(treeLovNodeValue))) {
						addNode = true;
					}

					// its a leaf so we take the value and description defined
					// in the lov definition
					if (lovProvDet.getTreeLevelsColumns().size() == treeLovNodeLevel + 2) {
						if (attribute.getKey().equalsIgnoreCase(descriptionColumn)) {// its
							// the
							// column
							// of
							// the
							// description
							valueJSON.put("description", attribute.getValue());
							notNullNode = true;
						}
						if (attribute.getKey().equalsIgnoreCase(valueColumn)) {// its
							// the
							// column
							// of
							// the
							// value
							valueJSON.put("value", attribute.getValue());
							valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
							notNullNode = true;
						}
						valueJSON.put("leaf", true);
					} else if (attribute.getKey().equalsIgnoreCase(treeLovNodeName)) {
						valueJSON = new JSONObject();
						valueJSON.put("description", attribute.getValue());
						valueJSON.put("value", attribute.getValue());
						valueJSON.put("id", attribute.getValue() + NODE_ID_SEPARATOR + (treeLovNodeLevel + 1));
						notNullNode = true;
					}
				}
				//
				if (addNode && notNullNode) {
					valuesDataJSON.add(valueJSON);
				}
			}

			JSONArray valuesDataJSONArray = new JSONArray();

			for (Iterator iterator = valuesDataJSON.iterator(); iterator.hasNext();) {
				JSONObject jsonObject = (JSONObject) iterator.next();
				valuesDataJSONArray.put(jsonObject);
			}

			return valuesDataJSONArray;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}

	}

	public static String NODE_ID_SEPARATOR = "___SEPA__";

	public JSONObject buildJsonResult(String status, String error, JSONObject obj, JSONArray objArr, String biparameterId) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("status", status);
			jsonObj.put("error", error);
			jsonObj.put("idParam", biparameterId);
			if (objArr != null) {
				jsonObj.put("result", objArr);
			}
			if (obj != null) {
				jsonObj.put("result", obj);
			}
		} catch (JSONException e) {
			LOGGER.error("Non fatal error building JSON Result Document Execution Parameter", e);
		}

		return jsonObj;
	}

	public JSONObject buildJSONForLOV(ILovDetail lovProvDet, List rows, Integer start, Integer limit) throws Exception {
		JSONObject valuesJSON = new JSONObject();

		Map<String, Object> metadata = new LinkedHashMap<>();
		String valueColumn = lovProvDet.getValueColumnName();
		String displayColumn = lovProvDet.getDescriptionColumnName();
		String descriptionColumn = displayColumn;
		List<String> visibleColumnNames = lovProvDet.getVisibleColumnNames();
		List<String> invisibleColumnNames = lovProvDet.getInvisibleColumnNames();
		BiMap<String, String> colName2colPlaceholder = HashBiMap.create();
		AtomicInteger colCount = new AtomicInteger(0);
		JSONArray valuesDataJSON = new JSONArray();

		visibleColumnNames.forEach(e -> {
			colName2colPlaceholder.put("_col" + colCount.getAndIncrement(), e);
		});

		invisibleColumnNames.forEach(e -> {
			colName2colPlaceholder.put("_col" + colCount.getAndIncrement(), e);
		});

		metadata.put("colsMap", colName2colPlaceholder);
		metadata.put("descriptionColumn", descriptionColumn);
		metadata.put("invisibleColumns", invisibleColumnNames);
		metadata.put("valueColumn", valueColumn);
		metadata.put("visibleColumns", visibleColumnNames);

		valuesJSON.put("metadata", metadata);
		valuesJSON.put("data", valuesDataJSON);

		// START building JSON object to be returned
		try {

			int lb = (start != null) ? start.intValue() : 0;
			int ub = (limit != null) ? lb + limit.intValue() : rows.size() - lb;
			ub = (ub > rows.size()) ? rows.size() : ub;

			for (int q = lb; q < ub; q++) {
				SourceBean row = (SourceBean) rows.get(q);
				JSONObject valueJSON = new JSONObject();
				Consumer<String> putInRow = e -> {
					String placeHolder = colName2colPlaceholder.inverse().get(e);

					Object value = row.getAttribute(e.toUpperCase());
					if (value != null) {
						try {
							valueJSON.put(placeHolder, value.toString());
						} catch (JSONException ex) {
							throw new SpagoBIRuntimeException(ex);
						}
					}
				};

				visibleColumnNames.forEach(putInRow);
				invisibleColumnNames.forEach(putInRow);

				valuesDataJSON.put(valueJSON);
			}

			String[] visiblecolumns;

			visiblecolumns = visibleColumnNames.toArray(new String[0]);
			for (int j = 0; j < visiblecolumns.length; j++) {
				visiblecolumns[j] = visiblecolumns[j].toUpperCase();
			}

//			valuesJSON = (JSONObject) JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON, valueColumn.toUpperCase(), displayColumn.toUpperCase(),
//					descriptionColumn.toUpperCase(), visiblecolumns, new Integer(rows.size()));
			return valuesJSON;
		} catch (Exception e) {
			throw new SpagoBIServiceException("Impossible to serialize response", e);
		}
		// END building JSON object to be returned

	}


}
