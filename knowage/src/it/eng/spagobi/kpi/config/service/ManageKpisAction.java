/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiDocuments;
import it.eng.spagobi.kpi.config.bo.KpiRel;
import it.eng.spagobi.kpi.config.dao.IKpiDAO;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.dao.IThresholdDAO;
import it.eng.spagobi.kpi.threshold.service.ManageThresholdsAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.bo.UdpValue;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageKpisAction extends AbstractSpagoBIAction {
	// logger component
	private static Logger logger = Logger.getLogger(ManageThresholdsAction.class);
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String KPIS_LIST = "KPIS_LIST";
	private final String KPI_INSERT = "KPI_INSERT";
	private final String KPI_DELETE = "KPI_DELETE";
	private final String KPI_LINKS = "KPI_LINKS";
	private final String KPI_LINK_SAVE = "KPI_LINK_SAVE";
	private final String KPI_LINK_DELETE = "KPI_LINK_DELETE";
	private final String KPI_LINKS_BY_DS = "KPI_LINKS_BY_DS";

	private final String KPI_DOMAIN_TYPE = "KPI_TYPE";
	private final String THRESHOLD_SEVERITY_TYPE = "SEVERITY";
	private final String METRIC_SCALE_DOMAIN_TYPE = "METRIC_SCALE_TYPE";
	private final String MEASURE_DOMAIN_TYPE = "MEASURE_TYPE";
	private final String THRESHOLD_DOMAIN_TYPE = "THRESHOLD_TYPE";

	// RES detail
	private final String ID = "id";
	private final String NAME = "name";
	private final String CODE = "code";
	private final String DESCRIPTION = "description";
	private final String WEIGHT = "weight";
	private final String ISADDITIVE = "isAdditive";	
	private final String DATASET = "dataset";
	private final String THR = "threshold";
	private final String DOCS = "documents";
	private final String INTERPRETATION = "interpretation";
	private final String ALGDESC = "algdesc";
	private final String INPUT_ATTR = "inputAttr";
	private final String MODEL_REFERENCE = "modelReference";
	private final String TARGET_AUDIENCE = "targetAudience";

	private final String KPI_TYPE_ID = "kpiTypeId";
	private final String KPI_TYPE_CD = "kpiTypeCd";
	private final String METRIC_SCALE_TYPE_ID = "metricScaleId";
	private final String METRIC_SCALE_TYPE_CD = "metricScaleCd";
	private final String MEASURE_TYPE_ID = "measureTypeId";
	private final String MEASURE_TYPE_CD = "measureTypeCd";
	private final String UDP_VALUE_LIST = "udpValuesAtt";


	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 14;
	//filters parameters
	public static String FILTERS = "FILTERS";

	@Override
	public void doService() {
		logger.debug("IN");
		IKpiDAO kpiDao;
		IDataSetDAO dsDao;
		IThresholdDAO thrDao;
		try {
			kpiDao = DAOFactory.getKpiDAO();
			dsDao = DAOFactory.getDataSetDAO();
			thrDao = DAOFactory.getThresholdDAO();
			kpiDao.setUserProfile(getUserProfile());
			dsDao.setUserProfile(getUserProfile());
			thrDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME,	"Error occurred");
		}
		Locale locale = getLocale();

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type "+serviceType);
		if (serviceType != null && serviceType.equalsIgnoreCase(KPIS_LIST)) {

			try {
				JSONObject filtersJSON = null;

				Integer start = getAttributeAsInteger( START );
				Integer limit = getAttributeAsInteger( LIMIT );

				if(start==null){
					start = START_DEFAULT;
				}
				if(limit==null){
					limit = LIMIT_DEFAULT;
				}

				Integer totalItemsNum = kpiDao.countKpis();
				
				List kpis = null;
				if(this.requestContainsAttribute( FILTERS ) ) {
					filtersJSON = getAttributeAsJSONObject( FILTERS );
					String hsql = filterList(filtersJSON);
					kpis = kpiDao.loadKpiListFiltered(hsql, start, limit);
				}else{//not filtered
					kpis = kpiDao.loadPagedKpiList(start,limit);
				}

				logger.debug("Loaded thresholds list");

				Integer kpiParent = this.getAttributeAsInteger("id");
				if(kpiParent != null){
					kpis =cleanKpiListForRelation((ArrayList<Kpi>)kpis, kpiParent);
				}
				JSONArray resourcesJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(kpis, locale);
				JSONObject resourcesResponseJSON = createJSONResponseResources(resourcesJSON, totalItemsNum);

				writeBackToClient(new JSONSuccess(resourcesResponseJSON));

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving thresholds", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving thresholds", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_INSERT)) {

			String id = getAttributeAsString(ID);
			String code = getAttributeAsString(CODE);
			String name = getAttributeAsString(NAME);
			String description = getAttributeAsString(DESCRIPTION);
			String weight = getAttributeAsString(WEIGHT);
			Boolean isAdditive = getAttributeAsBoolean(ISADDITIVE);
			
			String dsLabel = getAttributeAsString(DATASET);
			String thresholdCode = getAttributeAsString(THR);
			JSONArray docLabelsJSON = null;
			String docs = getAttributeAsString(DOCS);
			if(docs!=null && !docs.contains(",")){
				//Don't do anything
			}else{
				docLabelsJSON = getAttributeAsJSONArray(DOCS);
			}

			String interpretation = getAttributeAsString(INTERPRETATION);
			String algdesc = getAttributeAsString(ALGDESC);
			String inputAttr = getAttributeAsString(INPUT_ATTR);
			String modelReference = getAttributeAsString(MODEL_REFERENCE);
			String targetAudience = getAttributeAsString(TARGET_AUDIENCE);

			String kpiTypeCd = getAttributeAsString(KPI_TYPE_CD);	
			String metricScaleCd = getAttributeAsString(METRIC_SCALE_TYPE_CD);
			String measureTypeCd = getAttributeAsString(MEASURE_TYPE_CD);				

			JSONArray udpValuesArrayJSon = getAttributeAsJSONArray(UDP_VALUE_LIST);

			List<Domain> domains = (List<Domain>)getSessionContainer().getAttribute("kpiTypesList");
			List<Domain> domains1 = (List<Domain>)getSessionContainer().getAttribute("measureTypesList");
			List<Domain> domains2 = (List<Domain>)getSessionContainer().getAttribute("metricScaleTypesList");
			domains.addAll(domains1);
			domains.addAll(domains2);

			HashMap<String, Integer> domainIds = new HashMap<String, Integer> ();
			if(domains != null){
				for(int i=0; i< domains.size(); i++){
					domainIds.put(domains.get(i).getValueCd(), domains.get(i).getValueId());
				}
			}

			Integer kpiTypeId = domainIds.get(kpiTypeCd);
			Integer metricScaleId = domainIds.get(metricScaleCd);
			Integer measureTypeId = domainIds.get(measureTypeCd);

			if (name != null && code != null) {
				Kpi k = new Kpi();

				try {

					k.setKpiName(name);
					k.setCode(code);

					if(description != null){
						k.setDescription(description);
					}
					if(weight != null && !weight.equalsIgnoreCase("")){
						k.setStandardWeight(Double.valueOf(weight));
					}	
					if(isAdditive != null && isAdditive.booleanValue()==true){
						k.setIsAdditive(new Boolean(true));
					}else{
						k.setIsAdditive(new Boolean(false));
					}
					if(dsLabel != null){
						k.setDsLabel(dsLabel);
						IDataSet ds = dsDao.loadDataSetByLabel(dsLabel);

						if(ds!=null){
							int dsId = ds.getId();
							k.setKpiDsId(new Integer(dsId));
						}				
					}
					if(thresholdCode != null){
						Threshold t = thrDao.loadThresholdByCode(thresholdCode);
						k.setThreshold(t);
					}

					k.setKpiName(name);
					k.setCode(code);

					if(description != null){
						k.setDescription(description);
					}
					if(weight != null && !weight.equalsIgnoreCase("")){
						k.setStandardWeight(Double.valueOf(weight));
					}	
					if(dsLabel != null){
						k.setDsLabel(dsLabel);
						IDataSet ds = dsDao.loadDataSetByLabel(dsLabel);

						if(ds!=null){
							int dsId = ds.getId();
							k.setKpiDsId(new Integer(dsId));
						}				
					}
					if(thresholdCode != null){
						Threshold t = thrDao.loadThresholdByCode(thresholdCode);
						k.setThreshold(t);
					}

					List docsList = null;
					if(docLabelsJSON != null){
						docsList = deserializeDocLabelsJSONArray(docLabelsJSON);
						k.setSbiKpiDocuments(docsList);
					}else if(docs!=null && !docs.equalsIgnoreCase("")){
						KpiDocuments d = new KpiDocuments();
						d.setBiObjLabel(docs);
						docsList = new ArrayList();
						docsList.add(d);
						k.setSbiKpiDocuments(docsList);
					}

					if(interpretation != null){
						k.setInterpretation(interpretation);
					}
					if(algdesc != null){
						k.setMetric(algdesc);
					}
					if(inputAttr != null){
						k.setInputAttribute(inputAttr);
					}
					if(modelReference != null){
						k.setModelReference(modelReference);
					}
					if(targetAudience != null){
						k.setTargetAudience(targetAudience);
					}
					if(kpiTypeCd != null){
						k.setKpiTypeCd(kpiTypeCd);
						k.setKpiTypeId(kpiTypeId);
					}
					if(metricScaleCd != null){
						k.setMetricScaleCd(metricScaleCd);
						k.setMetricScaleId(metricScaleId);
					}
					if(measureTypeCd != null){
						k.setMeasureTypeCd(measureTypeCd);
						k.setMeasureTypeId(measureTypeId);
					}		

					// add to Kpi Definition UDP Value list...
					//List udpValues = k.getUdpValues();
					List<UdpValue> udpValues = new ArrayList<UdpValue>();	
					for(int i=0; i< udpValuesArrayJSon.length(); i++){
						JSONObject obj = (JSONObject)udpValuesArrayJSon.get(i);
						// only label and value information are retrieved by JSON object
						String label = obj.getString("name");	
						String value = obj.getString("value");	

						UdpValue udpValue = new UdpValue();

						// reference id is the kpi id
						Integer kpiId = k.getKpiId();

						//udpValue.setLabel(label);
						udpValue.setValue(value);
						udpValue.setReferenceId(kpiId);

						// get the UDP to get ID (otherwise could be taken in js page)
						Udp udp = DAOFactory.getUdpDAO().loadByLabelAndFamily(label, "Kpi");
						Domain familyDomain = DAOFactory.getDomainDAO().loadDomainById(udp.getFamilyId());

						Integer idUdp = udp.getUdpId();

						udpValue.setLabel(udp.getLabel());
						udpValue.setName(udp.getName());
						udpValue.setFamily(familyDomain != null ? familyDomain.getValueCd() : null);
						udpValue.setUdpId(udp.getUdpId());

						udpValues.add(udpValue);
					}

					k.setUdpValues(udpValues);


					if(id != null && !id.equals("") && !id.equals("0")){							
						k.setKpiId(Integer.valueOf(id));
						kpiDao.modifyKpi(k);
						logger.debug("threshold "+id+" updated");
						JSONObject attributesResponseSuccessJSON = new JSONObject();
						attributesResponseSuccessJSON.put("success", true);
						attributesResponseSuccessJSON.put("responseText", "Operation succeded");
						attributesResponseSuccessJSON.put("id", id);
						writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
					}else{
						Integer kpiID = kpiDao.insertKpi(k);
						logger.debug("New threshold inserted");
						JSONObject attributesResponseSuccessJSON = new JSONObject();
						attributesResponseSuccessJSON.put("success", true);
						attributesResponseSuccessJSON.put("responseText", "Operation succeded");
						attributesResponseSuccessJSON.put("id", kpiID);
						writeBackToClient( new JSONSuccess(attributesResponseSuccessJSON) );
					}

				} catch(EMFUserError e){
					logger.error("EMFUserError");
					e.printStackTrace();
				} catch (JSONException e) {
					logger.error("JSONException");
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("IOException");
					e.printStackTrace();
				}

			}else{
				logger.error("Resource name, code or type are missing");
				throw new SpagoBIServiceException(SERVICE_NAME,	"Please fill threshold name, code and type");
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_DELETE)) {
			Integer id = getAttributeAsInteger(ID);
			try {
				kpiDao.deleteKpi(id);
				logger.debug("Resource deleted");
				writeBackToClient( new JSONAcknowledge("Operation succeded") );
			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving resource to delete", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving resource to delete", e);
			}
		}else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_LINKS)) {			
			try {
				Integer id =null;
				try{
					id = getAttributeAsInteger(ID);
				}catch (Exception e) {
					logger.debug("No Kpi Instance Id");
				}
				ArrayList <KpiRel> relations = new ArrayList<KpiRel>();
				//looks up for relations
				if(id != null){
					relations = (ArrayList <KpiRel>)kpiDao.loadKpiRelListByParentId(id);
					logger.debug("Kpi relations loaded");
	
					//looks up for dataset parameters				
					IDataSet dataSet = kpiDao.getDsFromKpiId(id);
					if(dataSet != null){
						String parametersString = dataSet.getParameters();
		
						ArrayList<String> parameters = new ArrayList<String>();
						logger.debug("Dataset Parameters loaded");
						if(parametersString != null){
							SourceBean source = SourceBean.fromXMLString(parametersString);
							if(source.getName().equals("PARAMETERSLIST")) {
								List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
								for(int i=0; i< rows.size(); i++){
									SourceBean row = rows.get(i);
									String name = (String)row.getAttribute("name");
									parameters.add(name);
								}
							}
							JSONArray paramsJSON = serializeParametersList(parameters, relations);
							JSONObject paramsResponseJSON = createJSONResponseResources(paramsJSON, parameters.size());
							writeBackToClient(new JSONSuccess(paramsResponseJSON));
						}else{
							writeBackToClient(new JSONSuccess(new JSONObject()));
						}
					}else{
						writeBackToClient(new JSONSuccess(new JSONObject()));
					}
				}else{
					writeBackToClient(new JSONSuccess(new JSONObject()));
				}

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving kpi links", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving kpi links", e);
			}
		} else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_LINKS_BY_DS)) {			
			try {
				String labelDS = getAttributeAsString("label");
				//looks up for relations
				ArrayList <KpiRel> relations = new ArrayList <KpiRel>();

				//looks up for dataset parameters				
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(labelDS);
				String parametersString = dataSet.getParameters();

				ArrayList<String> parameters = new ArrayList<String>();
				logger.debug("Dataset Parameters loaded");
				if(parametersString != null){
					SourceBean source = SourceBean.fromXMLString(parametersString);
					if(source.getName().equals("PARAMETERSLIST")) {
						List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
						for(int i=0; i< rows.size(); i++){
							SourceBean row = rows.get(i);
							String name = (String)row.getAttribute("name");
							parameters.add(name);
						}
					}
					JSONArray paramsJSON = serializeParametersList(parameters, relations);
					JSONObject paramsResponseJSON = createJSONResponseResources(paramsJSON, parameters.size());
					writeBackToClient(new JSONSuccess(paramsResponseJSON));
				}else{
					writeBackToClient(new JSONSuccess(new JSONObject()));
				}

			} catch (Throwable e) {
				logger.error("Exception occurred while retrieving kpi links", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while retrieving kpi links", e);
			}
		}
		else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_LINK_SAVE)) {

			Integer kpiParentId = getAttributeAsInteger("kpiParentId");
			Integer kpiLinked = getAttributeAsInteger("kpiLinked");
			String parameter = getAttributeAsString("parameter");


			try {
				try{
					Integer relId = getAttributeAsInteger("relId");
					if(relId != null){
						boolean res = kpiDao.deleteKpiRel(relId);
					}
				}catch(Throwable t){
					logger.debug("Insert new relation");
				}
				Integer idRel = kpiDao.setKpiRel(kpiParentId, kpiLinked, parameter);
				logger.debug("Resource deleted");
				writeBackToClient( new JSONSuccess(new JSONObject("{id: "+idRel.intValue()+"}")) );
			} catch (Throwable e) {
				logger.error("Exception occurred while saving kpis link", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving kpis link", e);
			}
		}else if (serviceType != null	&& serviceType.equalsIgnoreCase(KPI_LINK_DELETE)) {

			Integer kpiRelId = getAttributeAsInteger("relId");
			try {
				boolean res = kpiDao.deleteKpiRel(kpiRelId);
				logger.debug("Resource deleted");
				writeBackToClient( new JSONSuccess("Operation succeded") );
			} catch (Throwable e) {
				logger.error("Exception occurred while deleting kpis link", e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception occurred while saving kpis link", e);
			}
		}else if(serviceType == null){
			try {
				List kpiTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(KPI_DOMAIN_TYPE);
				getSessionContainer().setAttribute("kpiTypesList", kpiTypesList);
				List thrSeverityTypes = DAOFactory.getDomainDAO().loadListDomainsByType(THRESHOLD_SEVERITY_TYPE);
				getSessionContainer().setAttribute("thrSeverityTypes", thrSeverityTypes);
				List measureTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(MEASURE_DOMAIN_TYPE);
				getSessionContainer().setAttribute("measureTypesList", measureTypesList);
				List metricScaleTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(METRIC_SCALE_DOMAIN_TYPE);
				getSessionContainer().setAttribute("metricScaleTypesList", metricScaleTypesList);
				List thrTypesList = DAOFactory.getDomainDAO().loadListDomainsByType(THRESHOLD_DOMAIN_TYPE);
				getSessionContainer().setAttribute("thrTypesList", thrTypesList);
				// add also UDPs
				List udpList = DAOFactory.getUdpDAO().loadAllByFamily("Kpi");
				getSessionContainer().setAttribute("udpKpiList", udpList);

			} catch (EMFUserError e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Exception retrieving resources types", e);
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Creates a json array with children users informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseResources(JSONArray rows, Integer totalResNumber)
	throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Kpis");
		results.put("rows", rows);
		return results;
	}

	private List deserializeDocLabelsJSONArray(JSONArray rows) throws JSONException{
		List toReturn = new ArrayList();
		for(int i=0; i< rows.length(); i++){			
			if(!rows.isNull(i)){
				String label = (String)rows.get(i);
				KpiDocuments d = new KpiDocuments();
				d.setBiObjLabel(label);
				toReturn.add(d);
			}
		}	
		return toReturn;
	}

	private JSONArray serializeParametersList(ArrayList params, ArrayList<KpiRel> relations) throws JSONException{
		JSONArray rows = new JSONArray();
		for(int i=0; i< params.size(); i++){
			JSONObject obj = new JSONObject();
			String par = (String)params.get(i);
			obj.put("parameterName", par);
			for(int k =0; k< relations.size(); k++){
				KpiRel rel = relations.get(k);
				if(rel.getParameter().equals(par)){
					obj.put("kpi", rel.getKpiChild().getKpiName());
					obj.put("relId", rel.getKpiRelId().intValue());
				}
			}
			rows.put(obj);
		}
		return rows;

	}
	private List cleanKpiListForRelation(ArrayList kpis, Integer id){
		List newList = null;
		if(kpis != null){
			newList = kpis;
			for(int i=0; i< kpis.size(); i++){
				Kpi kpi = (Kpi)kpis.get(i);
				if(kpi.getKpiId().intValue() == id.intValue()){
					//remove it
					newList.remove(kpi);
				}
			}
		}

		return newList;
	}
	private String filterList(JSONObject filtersJSON) throws JSONException {
		logger.debug("IN");
		String hsql= " from SbiKpi k ";
		if (filtersJSON != null) {
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String columnFilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			if(typeFilter.equals("=")){
				hsql += " where k."+columnFilter+" = '"+valuefilter+"'";
			}else if(typeFilter.equals("like")){
				hsql += " where k."+columnFilter+" like '%"+valuefilter+"%'";
			}
			
		}
		logger.debug("OUT");
		return hsql;
	}
}
