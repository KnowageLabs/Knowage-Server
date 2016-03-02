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
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.dao.IModelInstanceDAO;
import it.eng.spagobi.tools.importexportOLD.ExportManager;
import it.eng.spagobi.tools.importexportOLD.ExporterMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;


/** called by export manager when exporting a KPI document
 * 
 * @author gavardi
 *
 */

public class KPIExportManager extends AbstractTypesExportManager {

	
	static private Logger logger = Logger.getLogger(KPIExportManager.class);


	
	
	

	public KPIExportManager(String type, ExporterMetadata exporter,
			ExportManager manager) {
		super(type, exporter, manager);
	}






	public void manageExport(BIObject biobj, Session session ) throws EMFUserError {

		List objsToInsert=new ArrayList();
		ObjTemplate template = biobj.getActiveTemplate();
		if (template != null) {
			try {
				byte[] tempFileCont = template.getContent();
				String tempFileStr = new String(tempFileCont);
				SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);


				String modelInstanceLabel = (String) tempFileSB.getAttribute("model_node_instance");

				// biObjectToInsert keeps track of objects that have to be inserted beacuse related to Kpi

				if (modelInstanceLabel != null) {
					IModelInstanceDAO modelInstanceDao = DAOFactory.getModelInstanceDAO();
					ModelInstance modelInstance = modelInstanceDao.loadModelInstanceWithoutChildrenByLabel(modelInstanceLabel);
					if (modelInstance == null) {
						logger.warn("Error while exporting kpi with id " + biobj.getId() + " and label " + biobj.getLabel() + " : " +
								"the template refers to a Model Instance with label " + modelInstanceLabel + " that does not exist!");
					} else {
						objsToInsert=exporter.insertAllFromModelInstance(modelInstance, session);
						//exporter.insertModelInstance(modelInstance, session);
					}
				}
			} catch (Exception e) {
				logger.error("Error while exporting kpi with id " + biobj.getId() + " and label " + biobj.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "8010", "component_impexp_messages");

			}
		}

		IBIObjectDAO biobjDAO = DAOFactory.getBIObjectDAO();

		for (Iterator iterator = objsToInsert.iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			BIObject obj=(BIObject)biobjDAO.loadBIObjectById(id);
			if(obj!=null){
				exportManager.exportSingleObj(obj.getId().toString());
			}
			else{
				logger.error("Could not find object with id"+id);
			}
		}
	}


}
