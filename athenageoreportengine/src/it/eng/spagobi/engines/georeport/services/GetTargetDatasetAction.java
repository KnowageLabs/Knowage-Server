/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.georeport.services;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.engines.BaseServletIOManager;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetTargetDatasetAction extends AbstractBaseServlet {

	// XML tags
	public static final String COLUMNLIST = "COLUMNLIST";
	public static final String COLUMN = "COLUMN";
	public static final String COLUMNS = "COLUMNS";
	public static final String DATASET = "DATASET";
	public static final String META = "META";
	public static final String NAME = "name";
	public static final String FIELD_TYPE = "fieldType";

	private static final long serialVersionUID = 1L;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(GetTargetDatasetAction.class);

	@Override
	public void doService(BaseServletIOManager servletIOManager) throws SpagoBIEngineException {

		GeoReportEngineInstance engineInstance;

		IDataSet dataSet;
		IDataStore dataStore;
		IMetaData dataStoreMeta;

		logger.debug("IN");

		try {
			engineInstance = (GeoReportEngineInstance) servletIOManager.getHttpSession().getAttribute(EngineConstants.ENGINE_INSTANCE);

			// DataSet
			dataSet = engineInstance.getDataSet();
			dataSet.setParamsMap(engineInstance.getEnv());
			dataSet.loadData();

			// Datastore
			dataStore = dataSet.getDataStore();

			for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
				fieldMeta.setName(fieldMeta.getName());
				if (fieldMeta.getAlias() != null) {
					fieldMeta.setAlias(fieldMeta.getAlias());
				}
				// set the fieldType with the datasets' definition metadata (get settings from the user GUI)
				fieldMeta.setFieldType(getDsFieldType(dataSet.getDsMetadata(), fieldMeta.getName()));
			}

			JSONDataWriter dataWriter = new JSONDataWriter();
			JSONObject result = (JSONObject) dataWriter.write(dataStore);

			JSONObject metaData = result.getJSONObject("metaData");
			JSONArray fields = metaData.getJSONArray("fields");
			for (int i = 1; i < fields.length(); i++) {
				JSONObject field = fields.getJSONObject(i);
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i - 1);
				if (fieldMeta.getFieldType().equals(FieldType.MEASURE)) {
					field.put("role", "MEASURE");
				} else {
					field.put("role", "ATTRIBUTE");
				}

			}

			logger.debug(result.toString(3));
			String resultStr = result.toString();
			writeBackToClient(resultStr, servletIOManager);

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
	}

	public void writeBackToClient(String content, BaseServletIOManager servletIOManager) throws IOException {

		logger.debug("IN");

		// setup response header
		if (servletIOManager.getResponse() instanceof HttpServletResponse) {
			servletIOManager.getResponse().setHeader("Content-Disposition", "inline; filename=\"service-response\";");
		}

		servletIOManager.getResponse().setContentType("text/plain");
		logger.debug("Response content type set to [text/plain]");

		servletIOManager.getResponse().setCharacterEncoding("UTF-8");
		logger.debug("Response character encoding set to [UTF-8]");

		byte[] byteContent = content.getBytes("UTF-8");
		servletIOManager.getResponse().setContentLength(byteContent.length);
		logger.debug("Response character length is equal to [" + content.length() + "]");
		logger.debug("Response byte length is equal to [" + byteContent.length + "]");
		logger.debug("Response content length set to [" + byteContent.length + "]");

		if (servletIOManager.getResponse() instanceof HttpServletResponse) {
			servletIOManager.getResponse().setStatus(200);
			logger.debug("Response status code set to [200]");
		}

		servletIOManager.getResponse().getWriter().print(content);
		servletIOManager.getResponse().getWriter().flush();

		logger.debug("OUT");
	}

	@Override
	public void handleException(BaseServletIOManager servletIOManager, Throwable t) {
		t.printStackTrace();
	}

	private IDataSet getDataSet(BaseServletIOManager servletIOManager) {
		IDataSet dataSet;
		DataSetServiceProxy datasetProxy;
		String user;
		String label;

		user = servletIOManager.getParameterAsString("userId");
		label = servletIOManager.getParameterAsString("label");

		datasetProxy = new DataSetServiceProxy(user, servletIOManager.getHttpSession());
		dataSet = datasetProxy.getDataSetByLabel(label);

		return dataSet;
	}

	private FieldType getDsFieldType(String xml, String fieldName) throws Exception {
		FieldType toReturn = IFieldMetaData.FieldType.ATTRIBUTE;
		SourceBean sbXML;

		sbXML = SourceBean.fromXMLString(xml);
		// Columns Metadata Properties
		SourceBean sbColumns = (SourceBean) sbXML.getAttribute(COLUMNLIST);
		List lst = sbColumns.getAttributeAsList(COLUMN);
		for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
			SourceBean sbRow = (SourceBean) iterator.next();
			String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
			if (fieldName.equalsIgnoreCase(name)) {
				String dsFieldType = sbRow.getAttribute(FIELD_TYPE) != null ? sbRow.getAttribute(FIELD_TYPE).toString() : null;
				if (dsFieldType.equalsIgnoreCase("MEASURE")) {
					toReturn = IFieldMetaData.FieldType.MEASURE;
				} else if (dsFieldType.equalsIgnoreCase("ATTRIBUTE")) {
					toReturn = IFieldMetaData.FieldType.ATTRIBUTE;
				}
				break;
			}
		}

		return toReturn;
	}

}
