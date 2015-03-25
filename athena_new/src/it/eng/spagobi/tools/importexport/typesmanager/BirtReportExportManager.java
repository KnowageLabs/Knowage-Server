/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.typesmanager;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.importexport.ExportManager;
import it.eng.spagobi.tools.importexport.ExporterMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class BirtReportExportManager extends AbstractTypesExportManager {

	static private Logger logger = Logger
			.getLogger(BirtReportExportManager.class);

	public BirtReportExportManager(String type, ExporterMetadata exporter,
			ExportManager manager) {
		super(type, exporter, manager);
	}

	/**
	 * export for console needs to get from template datasets relationship and
	 * insert them
	 */

	public void manageExport(BIObject biobj, Session session)
			throws EMFUserError {

		logger.debug("IN");

		// get the template
		ObjTemplate template = biobj.getActiveTemplate();
		if (template != null && template.getName().endsWith("rptdesign")) {
			try {
				exportBirtDatasets(template, session);
			} catch (Exception e) {
				logger.error("Error while exporting document with id "
						+ biobj.getId() + " and label " + biobj.getLabel()
						+ " : "
						+ "could not find artifact designated in template.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "8010",
						"component_impexp_messages");
			}
		}
		logger.debug("OUT");
	}

	private void exportBirtDatasets(ObjTemplate template, Session session) throws Exception {
		List<String> datasets = getBirtDatasets(template);
		if (datasets != null && !datasets.isEmpty()) {
			for (int i = 0; i < datasets.size(); i++) {
				String datasetLabel = datasets.get(i);
				IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
				IDataSet dataset = datasetDao
						.loadDataSetByLabel(datasetLabel);
				IDataSet guiGenericDataSet = datasetDao
						.loadDataSetByLabel(datasetLabel);
				if (dataset == null) {
					logger.warn("Error while exporting template with name "
							+ template.getName() + " of document with id "
							+ template.getBiobjId() + " : "
							+ "the template refers to a dataset with label "
							+ datasetLabel + " that does not exist!");
				} else {
					exporter.insertDataSet(guiGenericDataSet, session, false);
				}
			}
		}
	}

	private List<String> getBirtDatasets(ObjTemplate template) throws Exception {
		List<String> datasets = new ArrayList<String>();
		byte[] tempFileCont = template.getContent();
		String tempFileStr = new String(tempFileCont);
		SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);
		List datasetsSBs = tempFileSB.getFilteredSourceBeanAttributeAsList(
				"data-sets.oda-data-set", "extensionID",
				"spagobi.birt.oda.dataSet");
		if (datasetsSBs != null && !datasetsSBs.isEmpty()) {
			Iterator it = datasetsSBs.iterator();
			while (it.hasNext()) {
				SourceBean datasetSB = (SourceBean) it.next();
				SourceBean xmlProperty = (SourceBean) datasetSB
						.getFilteredSourceBeanAttributeAsList("xml-property",
								"name", "queryText").get(0);
				String datasetLabel = xmlProperty.getCharacters();
				datasets.add(datasetLabel);
			}
		}
		return datasets;
	}

}
