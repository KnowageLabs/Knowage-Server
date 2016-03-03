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
package it.eng.spagobi.engines.qbe.query;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;import net.sf.jasperreports.engine.export.JRTextExporter;

import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;


/**
 * Compile, fill and export a report template to a file or stream.
 * 
 * @author Gioia
 */
public class ReportRunner {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(ReportRunner.class);
	
	/**
	 * Instantiates a new report runner.
	 */
	public ReportRunner() {}
	
	/**
	 * Run.
	 * 
	 * @param templateFile the template file
	 * @param reportFile the report file
	 * @param outputType the output type
	 * @param conn the conn
	 * 
	 * @throws Exception the exception
	 */
	public void run(String templateContent, File reportFile, String outputType, Connection conn, Locale locale) throws Exception {
		
		InputStream is = new ByteArrayInputStream( templateContent.getBytes("ISO-8859-1") );
		
		JasperReport report  = JasperCompileManager.compileReport(is);

		HashMap params = new HashMap();
		if (locale == null) {
			logger.warn("Input locale is null!!!");
		} else {
			logger.debug("Using locale: " + locale);
			params.put("REPORT_LOCALE", locale);
		}
		
		
		// virtualization block
		String tmpDirectory = System.getProperty("java.io.tmpdir");
		String pagingDirectory =  tmpDirectory + System.getProperty("file.separator") + "jrcache";
		File file = new File(pagingDirectory);
		file.mkdirs();
		params.put(JRParameter.REPORT_VIRTUALIZER, getVirtualizer(100, pagingDirectory));
		// virtualization block
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, conn);
			
		JRExporter exporter = null; 
			
		if (outputType.equalsIgnoreCase("text/html")) {
		   	exporter = new JRHtmlExporter();
		   	exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
		   	exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
		} else if (outputType.equalsIgnoreCase("text/xml")) {
		   	exporter = new JRXmlExporter();
		} else if (outputType.equalsIgnoreCase("text/plain")) {
		   	//exporter = new JRTextExporter(); 
		   	exporter = new JRTextExporter(); 
		} else if (outputType.equalsIgnoreCase("text/csv")) {
		   	exporter = new JRCsvExporter(); 	
		} else if (outputType.equalsIgnoreCase("application/pdf"))	{			
		   	exporter = new JRPdfExporter(); 	
		} else if (outputType.equalsIgnoreCase("application/rtf"))	{			
		   	exporter = new JRRtfExporter(); 		
		} else if (outputType.equalsIgnoreCase("application/vnd.ms-excel")) {
		   	exporter = new JExcelApiExporter();
		} else {
		   	exporter = new JRPdfExporter();
		}
			
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
	    exporter.setParameter(JRExporterParameter.OUTPUT_FILE , reportFile);
	    exporter.exportReport();
	    
	}
	
	/**
	 * Gets the virtualizer.
	 * 
	 * @param maxNumOfPages the max num of pages
	 * @param tmpDirectory the tmp directory
	 * 
	 * @return the virtualizer
	 */
	public JRFileVirtualizer getVirtualizer(int maxNumOfPages, String tmpDirectory) {
		JRFileVirtualizer virtualizer = null; 
		
		logger.debug("Max page cached during virtualization process: " + maxNumOfPages);
		logger.debug("Dir used as storing area during virtualization: " + tmpDirectory);
		virtualizer = new JRFileVirtualizer(maxNumOfPages, tmpDirectory);
		virtualizer.setReadOnly(true);
		
		return virtualizer;
	}
	
}