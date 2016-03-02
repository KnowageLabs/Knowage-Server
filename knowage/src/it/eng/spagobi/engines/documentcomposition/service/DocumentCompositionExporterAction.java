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
package it.eng.spagobi.engines.documentcomposition.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.container.SpagoBIRequestContainer;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration.Document;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.CurrentConfigurationDocComp;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.DocumentContainer;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.MetadataStyle;
import it.eng.spagobi.engines.exporters.DocumentCompositionExporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class DocumentCompositionExporterAction extends AbstractSpagoBIAction {

	private static transient Logger logger=Logger.getLogger(DocumentCompositionExporterAction.class);

	@Override
	public void doService() {
		logger.debug("IN");
		HttpServletRequest httpRequest = getHttpRequest();
		HttpSession session=httpRequest.getSession();

		this.freezeHttpResponse();

		DocumentCompositionConfiguration docCompConf=(DocumentCompositionConfiguration)session.getAttribute("DOC_COMP_CONF");

		// create the pdfFile		
		String dir=System.getProperty("java.io.tmpdir");
	    Random generator = new Random();
	    int randomInt = generator.nextInt();
	    String path=dir+"/"+Integer.valueOf(randomInt).toString()+".pdf";
		
		//File tmpFile=new File("path");
		File tmpFile=new File(path);

		// map that associates document labels and DOcument Containers (containing informations)
		Map<String, DocumentContainer> documents=new LinkedHashMap<String, DocumentContainer>();

		SpagoBIRequestContainer requestContainer=getSpagoBIRequestContainer();
		SourceBean sb=requestContainer.getRequest();
		
		// Recover currentParametersConfiguration
		Map<String, CurrentConfigurationDocComp> currentConfigurationsMap=new HashMap<String, CurrentConfigurationDocComp>();

		// if only one metadata style is wrong use default table style (ignore table positions)
		boolean defaultStyle=false;

		Map styles=docCompConf.getLstDivStyle();
		//Map docsMap=docCompConf.getDocumentsMap();
		Iterator iteratorStyles=styles.keySet().iterator();
		for (Iterator iterator = docCompConf.getDocumentsArray().iterator(); iterator.hasNext();) {
			Document doc = (Document) iterator.next();
			String label=doc.getSbiObjLabel();
			logger.debug("Document "+label);
			try{
				// recover style informations
				String styleLab=(String)iteratorStyles.next();
				String styleString=(String)styles.get(styleLab);
				if(styleString==null)styleString="";
				MetadataStyle metadataStyle=MetadataStyle.getMetadataStyle(label,styleString.toString(), docCompConf);
				if(defaultStyle==false){
					defaultStyle=(metadataStyle == null) ? true : false;
				}
				
				DocumentContainer documentContainer=new DocumentContainer();
				documentContainer.setStyle(metadataStyle);
				documents.put(label, documentContainer);
			}catch (java.util.NoSuchElementException nse){
				//for frame defined only for the external export 
				continue;
			}
			// get its parameters configuration
			logger.debug("Get parametrs configuration for document "+label);			
			Object urlO=sb.getAttribute("TRACE_PAR_"+label);
			if(urlO!=null){
				String url=urlO.toString();
				CurrentConfigurationDocComp ccdc=new CurrentConfigurationDocComp(label);
				ccdc.fillParsFromUrl(url);
				currentConfigurationsMap.put(label, ccdc);

			}
			
			// get its svg (for highcharts document because they're created only by the client-side)
			logger.debug("Get svg content for the highchart "+label);			
			String svg =  (sb.getAttribute("SVG_"+label) != null)?sb.getAttribute("SVG_"+label).toString():null;
			if(svg != null){
				CurrentConfigurationDocComp ccdc = new CurrentConfigurationDocComp(label);
				Map<String,Object> svgChartPar = new HashMap<String,Object>();
				svgChartPar.put("SVG_"+label, svg);
				ccdc.setParameters(svgChartPar);
				currentConfigurationsMap.put("SVG_"+label, ccdc);
			}
		}

		try{
			// recover BiObject Name
			Integer id = this.getAttributeAsInteger(SpagoBIConstants.OBJECT_ID);
			if(id==null){
				logger.error("Document id not found");
				return;
			}
			BIObject document=null;
			document = DAOFactory.getBIObjectDAO().loadBIObjectById(id);

			IEngUserProfile profile=getUserProfile();

			// CALL EXPORTER
			logger.debug("call exporter");
			DocumentCompositionExporter exporter=new DocumentCompositionExporter();
			tmpFile=exporter.exportDocumentCompositionPDF(tmpFile,docCompConf, document, profile, currentConfigurationsMap, documents, defaultStyle);

			if (tmpFile != null){
				String outputType = "PDF";
				String mimeType = "application/pdf";
	
				logger.debug("Report exported succesfully");
	
				HttpServletResponse response = getHttpResponse();
				response.setContentType(mimeType);							
				response.setHeader("Content-Disposition", "filename=\"report." + outputType + "\";");
				response.setContentLength((int) tmpFile.length());
	
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
				int b = -1;
				while ((b = in.read()) != -1) {
					response.getOutputStream().write(b);
				}
				response.getOutputStream().flush();
				in.close();
			}
			logger.debug("OUT");


		} catch(Throwable e) {
			logger.error("An exception has occured", e);
			//throw new Exception(e);
		} finally {
			if (tmpFile != null)
				tmpFile.delete();
		}
	}
}
