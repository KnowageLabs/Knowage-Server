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
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.importexport.ExportManager;
import it.eng.spagobi.tools.importexport.ExporterMetadata;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.misc.BASE64Decoder;

public class MetaModelsNeedExportManager extends AbstractTypesExportManager {


	static private Logger logger = Logger.getLogger(MetaModelsNeedExportManager.class);
	private static final BASE64Decoder DECODER = new BASE64Decoder();


	public MetaModelsNeedExportManager(String type, ExporterMetadata exporter,
			ExportManager manager) {
		super(type, exporter, manager);
	}

	/**
	 *  export for console needs to get from template datasets relationship and insert them
	 */

	public void manageExport(BIObject biobj, Session session)
	throws EMFUserError {

		logger.debug("IN");
		String datamartName = null;
		
		// get the template
		ObjTemplate template = biobj.getActiveTemplate();
		if (template != null) {
			try {
				byte[] tempFileCont = template.getContent();
				String tempFileStr = new String(tempFileCont);
				SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);
				
				Object datamartSB = tempFileSB.getAttribute("DATAMART");
				if(datamartSB != null){
					Object datamartNameO = 	((SourceBean)datamartSB).getAttribute("name");
					if(datamartNameO != null){
						datamartName = datamartNameO.toString();
					}
				}
				
				if(datamartName!= null){
					logger.debug("Datamart to retrieve is "+datamartName);

					IMetaModelsDAO metaModelsDAO = DAOFactory.getMetaModelsDAO();
					MetaModel metaModel = metaModelsDAO.loadMetaModelByName(datamartName);
					Content metaModelContent = metaModelsDAO.loadActiveMetaModelContentByName(datamartName);

					if(metaModel != null){
						boolean inserted = exporter.insertMetaModel(metaModel, session);
						if(inserted){
							exporter.insertMetaModelContent(metaModel, metaModelContent, session);
						}
					}
					else{
						logger.debug("Could not find datamart "+datamartName+" to retrieve");
					}

				}
				else{
					logger.debug("No Datamart  to retrieve");
				}
			} catch (Exception e) {
				logger.error("Error while exporting document with id " + biobj.getId() + " and label " + biobj.getLabel() + " : " +
				"could not find datamart designated in template.");					
				throw new EMFUserError(EMFErrorSeverity.ERROR, "8010", "component_impexp_messages");
			}
		}
		logger.debug("OUT");
	}
}
