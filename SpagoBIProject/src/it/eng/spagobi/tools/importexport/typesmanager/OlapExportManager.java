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
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.importexport.ExportManager;
import it.eng.spagobi.tools.importexport.ExporterMetadata;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import sun.misc.BASE64Decoder;

public class OlapExportManager extends AbstractTypesExportManager {


	static private Logger logger = Logger.getLogger(OlapExportManager.class);
	private static final BASE64Decoder DECODER = new BASE64Decoder();


	public OlapExportManager(String type, ExporterMetadata exporter,
			ExportManager manager) {
		super(type, exporter, manager);
	}

	/**
	 *  export for console needs to get from template datasets relationship and insert them
	 */

	public void manageExport(BIObject biobj, Session session)
			throws EMFUserError {

		logger.debug("IN");
		String cubeName = null;

		// get the template
		ObjTemplate template = biobj.getActiveTemplate();
		if (template != null) {
			try {
				byte[] tempFileCont = template.getContent();
				String tempFileStr = new String(tempFileCont);
				SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);

				Object cubeSB = tempFileSB.getAttribute("cube");
				if(cubeSB != null){
					Object cubeNameO = 	((SourceBean)cubeSB).getAttribute("reference");
					if(cubeNameO != null){
						cubeName = cubeNameO.toString();
					}
				}

				if(cubeName!= null){
					logger.debug("Datamart to retrieve is "+cubeName);

					IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();
					Artifact artifact = artifactsDAO.loadArtifactByNameAndType(cubeName, "MONDRIAN_SCHEMA");

					if(artifact != null){
						Content artifactContent = artifactsDAO.loadActiveArtifactContent(artifact.getId());

						boolean inserted = exporter.insertArtifact(artifact, session);
						if(inserted){
							exporter.insertArtifactContent(artifact, artifactContent, session);
						}
					}
					else{
						logger.debug("Could not ins artifact "+cubeName+" to retrieve");
					}
				}
				else{
					logger.debug("NO cube name retrieved in template");
					
				}
				

		} catch (Exception e) {
			logger.error("Error while exporting document with id " + biobj.getId() + " and label " + biobj.getLabel() + " : " +
					"could not find artifact designated in template.");					
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8010", "component_impexp_messages");
		}
	}
	logger.debug("OUT");
}
}
