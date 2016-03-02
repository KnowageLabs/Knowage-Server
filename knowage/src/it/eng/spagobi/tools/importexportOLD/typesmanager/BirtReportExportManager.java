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
package it.eng.spagobi.tools.importexportOLD.typesmanager;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.importexportOLD.ExportManager;
import it.eng.spagobi.tools.importexportOLD.ExporterMetadata;

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
