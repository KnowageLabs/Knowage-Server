/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.exporters;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.engines.kpi.bo.KpiResourceBlock;
import it.eng.spagobi.kpi.utils.BasicTemplateBuilder;
import it.eng.spagobi.kpi.utils.BasicXmlBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;


/**
 * 
 * @author gavardi
 *
 * This class is intended to take the result of a Kpi Execution and giveBack an export in other formats
 *
 *
 */

public class KpiExporter {

	private static transient Logger logger=Logger.getLogger(KpiExporter.class);


	public File getKpiReportPDF(List<KpiResourceBlock> kpiBlocks, BIObject obj, String userId) throws Exception{
		logger.debug("IN");


		//Build report template
		String docName=(obj!=null) ? obj.getName() : "";
		BasicTemplateBuilder basic=new BasicTemplateBuilder(docName);
		String template2= "";
		List templates = basic.buildTemplate(kpiBlocks);
		boolean first = true;
		
		//String template2=basic.buildTemplate(kpiBlocks);

		//System.out.println(template2);

		String outputType = "PDF";
		HashedMap parameters=new HashedMap();
		parameters.put("PARAM_OUTPUT_FORMAT", outputType);
		
		//parameters.put("SBI_HTTP_SESSION", session);   ???

		JREmptyDataSource conn=new JREmptyDataSource(1);

		// identity string for object execution
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		String executionId = uuid_local.toString();
		executionId = executionId.replaceAll("-", "");

		//Creta etemp file
		String dirS = System.getProperty("java.io.tmpdir");
		File dir = new File(dirS);
		dir.mkdirs();
		
		List filesToDelete = new ArrayList();
		logger.debug("Create Temp File");
		String fileName="report"+executionId;
		File tmpFile = File.createTempFile(fileName, "." + outputType, dir);
		OutputStream out = new FileOutputStream(tmpFile);
		try {								
			if(templates!=null && !templates.isEmpty()){
				int subreports = 0;
				Iterator it = templates.iterator();
				while(it.hasNext()){
					String template =(String)it.next();
					if(first)template2=template;
					else{
					
					File f = new File(dirS + File.separatorChar + "Detail"+subreports+".jasper");
					logger.debug("Compiling subtemplate file: " + f);
					filesToDelete.add(f);
					
					File file = new File(dirS + File.separatorChar + "Detail"+subreports+".jrxml");
					if(file.exists()){
						boolean deleted = file.delete();
						file =new File(dirS + File.separatorChar + "Detail"+subreports+".jrxml");
					}
					FileOutputStream stream = new FileOutputStream(file);
					stream.write(template.getBytes());
					stream.flush();
					stream.close();
					filesToDelete.add(file);
					
					JasperCompileManager.compileReportToFile(dirS + File.separatorChar + "Detail"+subreports+".jrxml",dirS + File.separatorChar + "Detail"+subreports+".jasper");
					subreports ++;
					}
					first = false;
				}
			}
			
			File f = new File(dirS + File.separatorChar + "Master.jasper");
			logger.debug("Compiling subtemplate file: " + f);
			filesToDelete.add(f);
			
			File file = new File(dirS + File.separatorChar + "Master.jrxml");
			if(file.exists()){
				boolean deleted = file.delete();
				file =new File(dirS + File.separatorChar + "Master.jrxml");
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(template2.getBytes());
			stream.flush();
			stream.close();
			filesToDelete.add(file);
			
			StringBufferInputStream sbis=new StringBufferInputStream(template2);
			JasperCompileManager.compileReportToFile(dirS + File.separatorChar + "Master.jrxml",dirS + File.separatorChar + "Master.jasper");

			logger.debug("Filling report ...");
			Context ctx = new InitialContext();
			Session aSession = HibernateSessionManager.getCurrentSession();
			JasperPrint jasperPrint = null;
			try {
				Transaction tx = aSession.beginTransaction();
				//Connection jdbcConnection = aSession.connection();
				Connection jdbcConnection = HibernateSessionManager.getConnection(aSession);
				jasperPrint = JasperFillManager.fillReport(dirS + File.separatorChar + "Master.jasper", parameters,jdbcConnection);
				logger.debug("Report filled succesfully");
			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
				}
			}
			logger.debug("Exporting report: Output format is [" + outputType + "]");
			JRExporter exporter=null;
			//JRExporter exporter = ExporterFactory.getExporter(outputType);	
			// Set the PDF exporter
			exporter = (JRExporter)Class.forName("net.sf.jasperreports.engine.export.JRPdfExporter").newInstance();

			if(exporter == null) exporter = new JRPdfExporter(); 	

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();
			logger.debug("Report exported succesfully");
			//in = new BufferedInputStream(new FileInputStream(tmpFile));
			logger.debug("OUT");
			return tmpFile;


		} catch(Throwable e) {
			logger.error("An exception has occured", e);
			throw new Exception(e);
		} finally {
			out.flush();
			out.close();
			if(filesToDelete!=null && !filesToDelete.isEmpty()){
				Iterator it = filesToDelete.iterator();
				while(it.hasNext()){
					File temp =(File) it.next();
					temp.delete();
				}
			}
			//tmpFile.delete();

		}

	}
	
	public File getKpiExportXML(List<KpiResourceBlock> kpiBlocks, BIObject obj, String userId) throws Exception{
		File tmpFile=null;
		logger.debug("IN");

		try{
			
			// recover BiObject Name
			Object idObject=obj.getId();
			if(idObject==null){
				logger.error("Document id not found");
			}

			Integer id=Integer.valueOf(idObject.toString());
			BIObject document=DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			String docName=document.getName();

			//Recover user Id
			HashedMap parameters=new HashedMap();

			BasicXmlBuilder basic=new BasicXmlBuilder(docName);
			String template = basic.buildTemplate(kpiBlocks);

			String dirS = System.getProperty("java.io.tmpdir");
			File dir = new File(dirS);
			dir.mkdirs();

			tmpFile = File.createTempFile("tempXmlExport", ".xml" , dir);
			FileOutputStream stream = new FileOutputStream(tmpFile);
			stream.write(template.getBytes());
			stream.flush();
			stream.close();
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));

			in.close();
			logger.debug("OUT");
			return tmpFile;

		} catch(Throwable e) {
			logger.error("An exception has occured", e);
			throw new Exception(e);
		} finally {

			//tmpFile.delete();

		}
	}
}




