/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.utils;




/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.kpi.bo.ChartImpl;
import it.eng.spagobi.engines.kpi.bo.KpiLine;
import it.eng.spagobi.engines.kpi.bo.KpiLineVisibilityOptions;
import it.eng.spagobi.engines.kpi.bo.KpiResourceBlock;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.InputSource;



//TODO: Auto-generated Javadoc
/**
 * The Class BasicTemplateBuilder.
 * 
 * @author Giulio Gavardi
 */
public class BasicTemplateBuilder  {

	private static transient org.apache.log4j.Logger logger=Logger.getLogger(BasicTemplateBuilder.class);

	private KpiLineVisibilityOptions options = new KpiLineVisibilityOptions(); 

	static String staticTextNameS="<staticText>" +
	"	<reportElement x=\"0\"" +
	"	y=\"0\"" +
	"   width=\"210\"" +
	"   height=\"12\"" +
	"	key=\"staticText-1\" />" +
	"	<box></box>" +
	"	<textElement>	" +
	"	<font size=\"8\" />" +
	"	</textElement>" +
	"	<text><![CDATA[KPI1]]></text>" +
	"	</staticText>";


	static String staticTextNumberS="<staticText>" +
	"	<reportElement" +
	"	x=\"235\"" +
	"	y=\"0\"" +
	"	width=\"35\"" +
	"	height=\"12\"" +
	"	key=\"staticText-2\"/>" +
	"	<box></box>" +
	"	<textElement textAlignment=\"Right\" >" +
	"	<font size=\"8\" isBold=\"true\"/>" +
	"	</textElement>" +
	"	<text></text>" +
	"	</staticText>";
	
	static String staticTextWeightS="<staticText>" +
	"	<reportElement" +
	"	x=\"284\"" +
	"	y=\"0\"" +
	"	width=\"17\"" +
	"	height=\"12\"" +
	"	key=\"staticText-2\"/>" +
	"	<box></box>" +
	"	<textElement textAlignment=\"Right\">" +
	"	<font size=\"6\" isBold=\"false\"/>" +
	"	</textElement>" +
	"	<text></text>" +
	"	</staticText>";

	static String imageS="<image  evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" >" +
	"	<reportElement" +
	"	x=\"310\"" +
	"	y=\"35\"" +
	"	width=\"130\"" +
	"	height=\"11\"" +
	"	key=\"image-1\"/>" +
	"	<box></box>" +
	"	<graphicElement stretchType=\"NoStretch\"/>" +
	"	<imageExpression class=\"java.net.URL\"></imageExpression>" +
	"	</image>";
	

	static String resourceBandS="<rectangle>" +
	"	<reportElement" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"535\"" +
	"	height=\"14\"" +
	"	forecolor=\"#FFFFFF\""+
	"	backcolor=\"#5B6C7C\"" +
	"	key=\"rectangle-2\"/>" +
	"	<graphicElement stretchType=\"NoStretch\"/>" +
	"	</rectangle>";


	static String resourceNameS="<staticText>" +
	"	<reportElement" +
	"	x=\"6\"" +
	"	y=\"0\"" +
	"	width=\"120\"" +
	"	height=\"14\"" +
	"	forecolor=\"#FFFFFF\""+
	"	key=\"staticText-3\"/>" +
	"	<box></box>" +
	"	<textElement verticalAlignment=\"Middle\" >" +
	"	<font size=\"10\" isBold=\"true\"/>" +
	" </textElement>" +
	"	<text><![CDATA[risorsa]]></text>" +
	"	</staticText>";
	
	static String columnHeaderBandS="<rectangle>" +
	"	<reportElement" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"535\"" +
	"	height=\"12\"" +
	"	forecolor=\"#FFFFFF\""+
	"	backcolor=\"#DDDDDD\"" +
	"	key=\"rectangle-2\"/>" +
	"	<graphicElement stretchType=\"NoStretch\"/>" +
	"	</rectangle>";
	
	static String columnModelHeaderS="<staticText>"+
	"	<reportElement"+
	"	x=\"6\""+
	"	y=\"15\""+
	"	width=\"93\""+
	"	height=\"12\""+
	"	forecolor=\"#000000\""+
	"	key=\"staticText-4\"/>"+
	"	<box></box>"+
	"	<textElement verticalAlignment=\"Middle\">"+
	"	<font pdfFontName=\"Helvetica-Bold\" size=\"8\" isBold=\"true\"/>"+
	"	</textElement>"+
	"	<text><![CDATA[MODEL]]></text>"+
	"	</staticText>";
	  
	static String columnKPIHeaderS="<staticText>"+
	"	<reportElement"+
	"	x=\"158\""+
	"	y=\"15\""+
	"	width=\"120\""+
	"	height=\"12\""+
	"	forecolor=\"#000000\""+
	"	isPrintWhenDetailOverflows=\"true\""+
	"	key=\"staticText-5\"/>"+
	"	<box></box>"+
	"	<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">"+
	"	<font pdfFontName=\"Helvetica-Bold\" size=\"8\" isBold=\"true\"/>"+
	"	</textElement>"+
	"	<text><![CDATA[KPI VALUE]]></text>"+
	"	</staticText>";	
		
	static String columnWeightHeaderS="<staticText>"+
	"	<reportElement"+
	"	x=\"285\""+
	"	y=\"15\""+
	"	width=\"53\""+
	"	height=\"12\""+
	"	forecolor=\"#000000\""+
	"	isPrintWhenDetailOverflows=\"true\""+
	"	key=\"staticText-6\"/>"+
	"	<box></box>"+
	"	<textElement verticalAlignment=\"Middle\">"+
	"	<font pdfFontName=\"Helvetica-Bold\" size=\"8\" isBold=\"true\"/>"+
	"	</textElement>"+
	"	<text><![CDATA[WEIGHT]]></text>"+
	"	</staticText>";	

	static String columnThresholdHeaderS="<staticText>"+
	"	<reportElement"+
	"	x=\"436\""+
	"	y=\"15\""+
	"	width=\"97\""+
	"	height=\"12\""+
	"	forecolor=\"#000000\""+
	"	key=\"staticText-7\"/>"+
	"	<box></box>"+
	"	<textElement textAlignment=\"Center\" verticalAlignment=\"Middle\">"+
	"	<font pdfFontName=\"Helvetica-Bold\" size=\"8\" isBold=\"true\"/>"+
	"	</textElement>"+
	"	<text><![CDATA[THRESHOLD RANGE]]></text>"+
	"	</staticText>";	

	static String thresholdCodeS="<staticText>"+
	"	<reportElement"+
	"	x=\"446\""+
	"	y=\"15\""+
	"	width=\"72\""+
	"	height=\"8\""+
	"	key=\"staticText-8\"/>"+
	"	<box></box>"+
	"	<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">"+
	"	<font size=\"6\" />"+
	"	</textElement>"+
	"	<text></text>"+
	"	</staticText>";	
	
	static String thresholdValueS="<staticText>"+
	"	<reportElement"+
	"	x=\"446\""+
	"	y=\"47\""+
	"	width=\"72\""+
	"	height=\"8\""+
	"	key=\"staticText-9\"/>"+
	"	<box></box>"+
	"	<textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">"+
	"	<font size=\"6\" />"+
	"	</textElement>"+
	"	<text></text>"+
	"	</staticText>";	

	static String semaphorS="<rectangle>" +
	"	<reportElement" +
	"	mode=\"Opaque\"" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"7\"" +
	"	height=\"7\"" +
	"	forecolor=\"#FFFFFF\"" +
	"	backcolor=\"#FFFFFF\"" +
	"	key=\"rectangle-1\"/>" +
	"	<graphicElement stretchType=\"NoStretch\"/>" +
	"	</rectangle>";
	
	
	static String oddLineSeparator="<line>"+
	"	<reportElement"+
	"	x=\"0\""+
	"	y=\"103\""+
	"	width=\"535\""+
	"	height=\"0\""+
	"	forecolor=\"#666666\""+
	"	key=\"line-1\"/>"+
	"	<graphicElement stretchType=\"NoStretch\"/>"+
	"	</line>";
	
	static String evenLineSeparator="<line>"+
	"	<reportElement"+
	"	x=\"0\""+
	"	y=\"103\""+
	"	width=\"535\""+
	"	height=\"0\""+
	"	forecolor=\"#990000\""+
	"	key=\"line-1\"/>"+
	"	<graphicElement stretchType=\"NoStretch\"/>"+
	"	</line>";
	
	static String thresholdBandS="<rectangle>" +
	"	<reportElement" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"535\"" +
	"	height=\"14\"" +
	"	forecolor=\"#FFFFFF\""+
	"	backcolor=\"#009999\"" +
	"	key=\"rectangle-2\"/>" +
	"	<graphicElement stretchType=\"NoStretch\"/>" +
	"	</rectangle>";
	
	static String thresholdTitleS="<staticText>" +
	"	<reportElement" +
	"	x=\"6\"" +
	"	y=\"0\"" +
	"	width=\"120\"" +
	"	height=\"14\"" +
	"	forecolor=\"#FFFFFF\""+
	"	key=\"staticText-3\"/>" +
	"	<box></box>" +
	"	<textElement verticalAlignment=\"Middle\" >" +
	"	<font size=\"10\" isBold=\"true\"/>" +
	" </textElement>" +
	"	<text><![CDATA[THRESHOLD DETAILS]]></text>" +
	"	</staticText>";


	static String thresholdTextCodeS="<staticText>" +
	"	<reportElement" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"95\"" +
	"	height=\"12\"" +
	"	forecolor=\"#000000\""+
	"	key=\"staticText-15\"/>" +
	"	<box></box>" +
	"	<textElement verticalAlignment=\"Middle\" textAlignment=\"Left\" >" +
	"	<font size=\"8\" />" +
	"   </textElement>" +
	"	<text></text>" +
	"	</staticText>";

	static String thresholdValuesCodeS="<staticText>" +
	"	<reportElement" +
	"	x=\"0\"" +
	"	y=\"0\"" +
	"	width=\"90\"" +
	"	height=\"12\"" +
	"	key=\"staticText-15\"/>" +
	"	<box></box>" +
	"	<textElement verticalAlignment=\"Middle\" textAlignment=\"Left\" >" +
	"	<font size=\"8\" />" +
	"   </textElement>" +
	"	<text></text>" +
	"	</staticText>";
	
	static String thresholdLineSeparatorS="<line>"+
	"	<reportElement"+
	"	x=\"0\""+
	"	y=\"103\""+
	"	width=\"535\""+
	"	height=\"0\""+
	"	forecolor=\"#000099\""+
	"	key=\"line-1\"/>"+
	"	<graphicElement stretchType=\"NoStretch\"/>"+
	"	</line>";
	
	static String subReportS ="<subreport  isUsingCache=\"true\">"+
	"	<reportElement"	+
	"	x=\"0\""+
	"	y=\"0\""+
	"	width=\"535\""+
	"	height=\"6\""+
    "	key=\"subreport-1\"/>"+
	"	<connectionExpression><![CDATA[$P{REPORT_CONNECTION}]]></connectionExpression>"	+
	"	<subreportExpression  class=\"java.lang.String\"></subreportExpression>"+
	"	</subreport>";
	


	SourceBean staticTextName=null;
	SourceBean staticTextNumber=null;
	SourceBean staticTextWeightNumber=null;
	SourceBean image=null;
	SourceBean resourceBand=null;
	SourceBean resourceName=null;
	SourceBean semaphor=null;
	SourceBean oddLineS=null;
	SourceBean evenLineS=null;
	SourceBean columnHeaderBand=null;
	SourceBean columnModelHeader=null;
	SourceBean columnKPIHeader=null;
	SourceBean columnWeightHeader=null;
	SourceBean columnThresholdHeader=null;
	SourceBean thresholdCode=null;
	SourceBean thresholdValue=null;
	
	SourceBean thresholdBand=null;
	SourceBean thresholdTitle=null;
	SourceBean thresholdLineSeparator=null;
	SourceBean thresholdTextCode=null;
	SourceBean thresholdTextValue=null;
	
	SourceBean subReport=null;

	String documentName=null;
	
	List thresholdsList=new ArrayList();

	// margin left of text in summary band
	final Integer xStarter=new Integer(0);
	// indentation value
	final Integer xIncrease=new Integer(5);
	// margin up of text in summary bend
	final Integer yStarter=new Integer(5);
	// Height of the gray band with the resource name
	final Integer resourceBandHeight=new Integer(14); 
	// Height of a value row
	final Integer valueHeight=new Integer(20); 
//	height between lines
	final Integer separatorHeight=new Integer(1);
//	height between lines
	final Integer separatorModelsHeight=new Integer(10);
//	width between elements
	final Integer separatorWidth = new Integer(5);
//	Width of text label with code - name
	final Integer textWidth=new Integer(280);
//	width of text label with numbers
	final Integer numbersWidth=new Integer(50);	
//	width of the semaphor
	final Integer semaphorWidth=new Integer(10);
//	width of the title band
	final Integer titleHeight=new Integer(50);
//	height of the column header band
	final Integer columnHeaderHeight=new Integer(14);
//	height of the column header band
	final Integer thresholdFieldWidth=new Integer(92);
	
	final Integer thresholdSemaphoreWidth=new Integer(10);
//	height of the column header band
	final Integer thresholdFieldSeparatorWidth=new Integer(10);
//	height of the column header band
	Integer maxFirstSubTemplateHeight=new Integer(745);
	final Integer maxSubTemplateHeight=new Integer(760);
	final Integer subreportHeight = new Integer(8);
	final Integer maxSubTemplateWIdtht=new Integer(530);

	int countSubreports = 0;
	
	Integer masterHeight=new Integer(10);	

	// counting the actual height of the report
	Integer actualHeight=new Integer(0);
	// counting the actual height of the detail band of master report
	Integer detailMasterHeight=new Integer(0);

	// Map for the name resolution of upper case tag names
	//Map nameResolution=new Map<String>();
	List nameResolution=new ArrayList();

	List resources;
	InputSource inputSource;

	//public SourceBean templateBaseContent=null; 
	public SourceBean detailMaster=null;
	//public SourceBean bandDetailReport=null;
	public SourceBean detailBandMaster=null;
	
	public List subreports = new ArrayList();
	public SourceBean subTemplateBaseContent = null;
	SourceBean subtitleSB = null;
	SourceBean bandDetailReport = null;
	SourceBean subSummarySB = null;
	SourceBean bandSummaryReport = null;


	public BasicTemplateBuilder(String documentName) {
		super();
		this.documentName = documentName;
	}


	/* Build the template
	 * @see it.eng.qbe.export.ITemplateBuilder#buildTemplate()
	 */
	public List buildTemplate(List resources) {
		logger.debug("IN");
		// name resolution for upper cases tag
		nameResolution();

		// Create Source Bean of template of template
		String templateStr = getTemplateTemplate();

		SourceBean templateBaseContent =null;
		List toReturn = new ArrayList();
		String finalTemplate="";

		logger.debug("Recovered template START ");
		logger.debug(templateStr);
		logger.debug("Recovered template END ");
		if(templateStr!=null){
			try {
				templateBaseContent = SourceBean.fromXMLString(templateStr);
			} catch (Exception e) {
				logger.error("Error in converting template of template into a SOurce Bean, check the XML code");
			}

		try {
			staticTextName = SourceBean.fromXMLString(staticTextNameS); // this is for text
			staticTextNumber = SourceBean.fromXMLString(staticTextNumberS); 
			staticTextWeightNumber = SourceBean.fromXMLString(staticTextWeightS);
			image = SourceBean.fromXMLString(imageS);
			resourceBand=SourceBean.fromXMLString(resourceBandS);
			resourceName=SourceBean.fromXMLString(resourceNameS);
			semaphor=SourceBean.fromXMLString(semaphorS);
			evenLineS=SourceBean.fromXMLString(evenLineSeparator);
			oddLineS=SourceBean.fromXMLString(oddLineSeparator);
			columnHeaderBand=SourceBean.fromXMLString(columnHeaderBandS);
			columnModelHeader=SourceBean.fromXMLString(columnModelHeaderS);
			columnKPIHeader=SourceBean.fromXMLString(columnKPIHeaderS);
			columnWeightHeader=SourceBean.fromXMLString(columnWeightHeaderS);
			columnThresholdHeader=SourceBean.fromXMLString(columnThresholdHeaderS);
			thresholdCode=SourceBean.fromXMLString(thresholdCodeS);
			thresholdValue=SourceBean.fromXMLString(thresholdValueS);
			
			thresholdBand=SourceBean.fromXMLString(thresholdBandS);
			thresholdTitle=SourceBean.fromXMLString(thresholdTitleS);
			thresholdTextValue=SourceBean.fromXMLString(thresholdValuesCodeS);
			thresholdTextCode=SourceBean.fromXMLString(thresholdTextCodeS);
			thresholdLineSeparator=SourceBean.fromXMLString(thresholdLineSeparatorS);
			
			subReport=SourceBean.fromXMLString(subReportS);
		} catch (Exception e) {
			logger.error("Error in converting static elemnts into Source Beans, check the XML code");
		}

		//change title
		SourceBean titleSB=(SourceBean)templateBaseContent.getAttribute("title");
		SourceBean titleText=(SourceBean)titleSB.getAttribute("band.staticText.text");
		titleText.setCharacters(documentName);
		
		// make DETAIL BAND of master with subreports
		detailMaster=(SourceBean)templateBaseContent.getAttribute("DETAIL");
		detailBandMaster=(SourceBean)detailMaster.getAttribute("BAND");
		masterHeight = new Integer(0);
		
		List subreports = createSubreports(resources);
		
		finalTemplate=templateBaseContent.toXML(false, false);
		for (Iterator iterator = nameResolution.iterator(); iterator.hasNext();) {
			NameRes nameR = (NameRes) iterator.next();
			String toReplace = nameR.getToSubstitute();
			String replaceWith=nameR.getCorrectString();
			finalTemplate=finalTemplate.replaceAll("<"+toReplace, "<"+replaceWith);
			finalTemplate=finalTemplate.replaceAll("</"+toReplace, "</"+replaceWith);		
		}
		toReturn.add(finalTemplate);
		logger.debug("Built template START");
		logger.debug(finalTemplate);
		logger.debug("Built template END");
		
		
		if(subreports!=null && !subreports.isEmpty()){
			logger.debug("There are subreports!");
			Iterator suit = subreports.iterator();
			while(suit.hasNext()){
				SourceBean subTemplateContent = (SourceBean)suit.next();
				String subTemplate = subTemplateContent.toXML(false);
				for (Iterator iterator = nameResolution.iterator(); iterator.hasNext();) {
					NameRes nameR = (NameRes) iterator.next();
					String toReplace = nameR.getToSubstitute();
					String replaceWith=nameR.getCorrectString();
					subTemplate=subTemplate.replaceAll("<"+toReplace, "<"+replaceWith);
					subTemplate=subTemplate.replaceAll("</"+toReplace, "</"+replaceWith);
				}
				toReturn.add(subTemplate);
				logger.debug("Built subtemplate: "+subTemplate);
				//System.out.println(subTemplate);
			}
		}
	
		}
		//System.out.println(finalTemplate);
		logger.debug("OUT");
		return toReturn;
	}

	public List createSubreports(List resources){
		logger.debug("IN");
		subreports = new ArrayList();
		subTemplateBaseContent = createNewSubReport(countSubreports);
		countSubreports ++;
		
		//change subtemplatetitle
		subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
		bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");	
		
		//change subtemplatesummary
		subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
		bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
		
		try {
		// cycle on resources
		for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
			KpiResourceBlock thisBlock = (KpiResourceBlock) iterator.next();
			options = thisBlock.getOptions();	
				
				if(actualHeight+separatorModelsHeight+resourceBandHeight+10<maxFirstSubTemplateHeight){
					List sourceBeansToAdd = newResource(thisBlock,bandDetailReport);	
					if (sourceBeansToAdd!=null && !sourceBeansToAdd.isEmpty()){
					Iterator it = sourceBeansToAdd.iterator();
						while(it.hasNext()){
							SourceBean toAdd = (SourceBean)it.next();
							bandDetailReport.setAttribute(toAdd);
						}
					}
				}else{
					
					//Add last subreport to the List
					increaseHeight(subTemplateBaseContent);
					subreports.add(subTemplateBaseContent);
					actualHeight = new Integer(0);
					subTemplateBaseContent = createNewSubReport(countSubreports);
					countSubreports ++;
					//Get my bandDetailReport from new subreport
					subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
					bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");
					//change subtemplatesummary
					subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
					bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
					//NEW SUBREPORT
					List sourceBeansToAdd = newResource(thisBlock,bandDetailReport);	
					if (sourceBeansToAdd!=null && !sourceBeansToAdd.isEmpty()){
					Iterator it = sourceBeansToAdd.iterator();
						while(it.hasNext()){
							SourceBean toAdd = (SourceBean)it.next();
							bandDetailReport.setAttribute(toAdd);
						}	
					}
				}
			
			
			
					
					if (actualHeight+separatorHeight+valueHeight+10<maxFirstSubTemplateHeight){
						KpiLine lineRoot=thisBlock.getRoot();
						List sourceBeansToAdd2 = newLine(lineRoot, 0,true);
						if (sourceBeansToAdd2!=null && !sourceBeansToAdd2.isEmpty()){
						Iterator it = sourceBeansToAdd2.iterator();
							while(it.hasNext()){
								SourceBean toAdd = (SourceBean)it.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}else{
						//Add last subreport to the List
						increaseHeight(subTemplateBaseContent);
						subreports.add(subTemplateBaseContent);
						actualHeight = new Integer(0);
						subTemplateBaseContent = createNewSubReport(countSubreports);
						countSubreports ++;
						//Get my bandDetailReport from new subreport
						subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
						bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");
						//change subtemplatesummary
						subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
						bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
						//NEW SUBREPORT
						KpiLine lineRoot=thisBlock.getRoot();
						List sourceBeansToAdd2 = newLine(lineRoot, 0,true);
						if (sourceBeansToAdd2!=null && !sourceBeansToAdd2.isEmpty()){
						Iterator it = sourceBeansToAdd2.iterator();
							while(it.hasNext()){
								SourceBean toAdd = (SourceBean)it.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}
				}
		
			if (actualHeight+separatorModelsHeight+columnHeaderHeight+10<maxFirstSubTemplateHeight){
				List sourceBeansToAdd3 = newThresholdBlock(bandDetailReport);
				if (sourceBeansToAdd3!=null && !sourceBeansToAdd3.isEmpty()){
				Iterator it = sourceBeansToAdd3.iterator();
					while(it.hasNext()){
						SourceBean toAdd = (SourceBean)it.next();
						bandDetailReport.setAttribute(toAdd);
					}	
				}
			}else{
				//Add last subreport to the List
				increaseHeight(subTemplateBaseContent);
				subreports.add(subTemplateBaseContent);
				actualHeight = new Integer(0);
				subTemplateBaseContent = createNewSubReport(countSubreports);
				countSubreports ++;
				//Get my bandDetailReport from new subreport
				subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
				bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");
				//change subtemplatesummary
				subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
				bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
				//NEW SUBREPORT
				List sourceBeansToAdd3 = newThresholdBlock(bandDetailReport);
				if (sourceBeansToAdd3!=null && !sourceBeansToAdd3.isEmpty()){
				Iterator it = sourceBeansToAdd3.iterator();
					while(it.hasNext()){
						SourceBean toAdd = (SourceBean)it.next();
						bandDetailReport.setAttribute(toAdd);
					}	
				}
			}
		

		if(thresholdsList!=null && !thresholdsList.isEmpty()){
			Iterator th = thresholdsList.iterator();
			while(th.hasNext()){
				Threshold t =(Threshold)th.next();

					if (actualHeight+separatorHeight+10<maxFirstSubTemplateHeight){
						List sourceBeansToAdd4 = newThresholdLine(t);
						if (sourceBeansToAdd4!=null && !sourceBeansToAdd4.isEmpty()){
						Iterator it = sourceBeansToAdd4.iterator();
							while(it.hasNext()){
								SourceBean toAdd = (SourceBean)it.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}else{
						
						//Add last subreport to the List
						increaseHeight(subTemplateBaseContent);
						subreports.add(subTemplateBaseContent);
						actualHeight = new Integer(0);
						subTemplateBaseContent = createNewSubReport(countSubreports);
						countSubreports ++;
						//Get my bandDetailReport from new subreport
						subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
						bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");
						//change subtemplatesummary
						subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
						bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
						//NEW SUBREPORT
						List sourceBeansToAdd4 = newThresholdLine(t);
						if (sourceBeansToAdd4!=null && !sourceBeansToAdd4.isEmpty()){
						Iterator it = sourceBeansToAdd4.iterator();
							while(it.hasNext()){
								SourceBean toAdd = (SourceBean)it.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}
				}
			}
		} catch (SourceBeanException e) {
			logger.error("SourceBeanException",e);
			e.printStackTrace();
		}
		bandDetailReport = increaseHeight(subTemplateBaseContent);
		subreports.add(subTemplateBaseContent);
		logger.debug("OUT");
		return subreports;
	}
	
	public SourceBean createNewSubReport(int numOfSubreport){
		logger.debug("IN");

		SourceBean subTemplateBaseContent =null;
		// Create Source Bean of template of subtemplate
		String subTemplateStr = getTemplateSubTemplate();
		try {
			subTemplateBaseContent = SourceBean.fromXMLString(subTemplateStr);
		} catch (Exception e) {
			logger.error("Error in converting template of template into a SOurce Bean, check the XML code");
		}
		
		SourceBean subreport1;
		try {
			subreport1 = new SourceBean(subReport);
			subreport1.setAttribute("reportElement.y", new Integer(0));
			SourceBean subreport2=(SourceBean)subreport1.getAttribute("subreportExpression");
			String dirS=System.getProperty("java.io.tmpdir");
			String subr = dirS+ File.separatorChar + "Detail"+numOfSubreport+".jasper";
			subr = subr.replaceAll("\\\\", "/");
			subreport2.setCharacters("\""+subr+"\"");
			if(numOfSubreport==0){
				detailBandMaster.setAttribute(subreport1);
			}else{
				bandSummaryReport.setAttribute(subreport1);
			}
			detailMasterHeight += subreportHeight;
			
		} catch (SourceBeanException e) {
			e.printStackTrace();
			logger.error(e);
		}  
		logger.debug("OUT");
		return subTemplateBaseContent;
	}

	// set the total height 
	public SourceBean increaseHeight(SourceBean tCont){
		logger.debug("IN");

		try {
			tCont.setAttribute("pageHeight",maxSubTemplateHeight);
			bandDetailReport.setAttribute("height", (actualHeight));
			bandSummaryReport.setAttribute("height", new Integer(7));
		} catch (SourceBeanException e) {
			logger.error("error in setting the height");
			return null;
		}
		logger.debug("OUT");
		return bandDetailReport;

	}


//	Add a resource band
	public List newResource(KpiResourceBlock block, SourceBean bandDetailReport){
		logger.debug("IN");
		List sourceBeansToAdd = new ArrayList();
		Resource res=block.getR();
			
			try{
				actualHeight+=separatorModelsHeight;

				SourceBean bandRes=new SourceBean(resourceBand);
				SourceBean bandName=new SourceBean(resourceName);
				SourceBean columnHeadBand=new SourceBean(columnHeaderBand);
				SourceBean modelColHeader=new SourceBean(columnModelHeader);
				SourceBean weightColHeader=new SourceBean(columnWeightHeader);
				SourceBean kpiColHeader=new SourceBean(columnKPIHeader);
				SourceBean kthreshColHeader=new SourceBean(columnThresholdHeader);
				
				if(res!=null){
					bandRes.setAttribute("reportElement.y", actualHeight.toString());
					bandName.setAttribute("reportElement.y", actualHeight.toString());
					logger.debug("add resource band for resource "+res.getName());
					SourceBean textValue1=(SourceBean)bandName.getAttribute("text");
					textValue1.setCharacters("RESOURCE: "+res.getName());

					sourceBeansToAdd.add(bandRes);
					sourceBeansToAdd.add(bandName);
					actualHeight+=resourceBandHeight;
				}
				
				columnHeadBand.setAttribute("reportElement.y",actualHeight.toString());
				modelColHeader.setAttribute("reportElement.y",actualHeight.toString());
				kpiColHeader.setAttribute("reportElement.y",actualHeight.toString());
				weightColHeader.setAttribute("reportElement.y",actualHeight.toString());
				kthreshColHeader.setAttribute("reportElement.y",actualHeight.toString());
				
				if(options.getModel_title()!=null ){
					SourceBean textValue=(SourceBean)modelColHeader.getAttribute("text");
					textValue.setCharacters(options.getModel_title());
				}
				if(options.getKpi_title()!=null ){
				SourceBean textValue1=(SourceBean)kpiColHeader.getAttribute("text");
				textValue1.setCharacters(options.getKpi_title());
				}
				if(options.getWeight_title()!=null ){
				SourceBean textValue2=(SourceBean)weightColHeader.getAttribute("text");
				textValue2.setCharacters(options.getWeight_title());
				}
				/*SourceBean textValue3=(SourceBean)kthreshColHeader.getAttribute("text");
				textValue3.setCharacters(options.getBullet_chart_title());*/
				
				sourceBeansToAdd.add(columnHeadBand);
				sourceBeansToAdd.add(modelColHeader);
				sourceBeansToAdd.add(kpiColHeader);
				sourceBeansToAdd.add(weightColHeader);
				sourceBeansToAdd.add(kthreshColHeader);			
				
				actualHeight+=columnHeaderHeight;

			}
			catch (Exception e) {
				logger.error("Error in setting the resource band",e);
				return null;
			}
		
		logger.debug("OUT");
		return sourceBeansToAdd;
	}


	public List newLine(KpiLine kpiLine, int level,Boolean evenLevel){
		logger.debug("IN");
		List sourceBeansToAdd = new ArrayList();
		try {
			actualHeight+=separatorHeight;
			SourceBean textCodeName=new SourceBean(staticTextName);   // code - name
			SourceBean textValue=new SourceBean(staticTextNumber);  //value number
			SourceBean textWeight=new SourceBean(staticTextWeightNumber);  // weight number
			SourceBean image1=new SourceBean(image);// Bullet Chart
			SourceBean semaphor1=new SourceBean(semaphor);// Semaphore
			SourceBean threshCode=new SourceBean(thresholdCode);// Threshold Code
			SourceBean threshValue=new SourceBean(thresholdValue);// Threshold Value
			SourceBean evenLine=new SourceBean(evenLineS);// Separator for even lines
			SourceBean oddLine=new SourceBean(oddLineS);// Separator for odd lines
			SourceBean extraimageToAdd = null;//in case 2 images are required
			if(evenLevel){
				extraimageToAdd =setLineAttributes(kpiLine,semaphor1,textCodeName,textValue,textWeight,image1,level,evenLine,threshCode,threshValue,extraimageToAdd);
			}else{
				extraimageToAdd = setLineAttributes(kpiLine,semaphor1,textCodeName,textValue,textWeight,image1,level,oddLine,threshCode,threshValue,extraimageToAdd);
			}
			actualHeight+=valueHeight;

			sourceBeansToAdd.add(semaphor1);
			sourceBeansToAdd.add(textCodeName);
			sourceBeansToAdd.add(textValue);
			sourceBeansToAdd.add(textWeight);
			sourceBeansToAdd.add(image1);
			if(extraimageToAdd!=null){
				sourceBeansToAdd.add(extraimageToAdd);
			}
			sourceBeansToAdd.add(threshCode);
			sourceBeansToAdd.add(threshValue);
			if(evenLevel){
				sourceBeansToAdd.add(evenLine);
			}else{
				sourceBeansToAdd.add(oddLine);
			}	
			
		} catch (SourceBeanException e) {
			logger.error("error while adding a line");
			return null;
		}

		List<KpiLine> children=kpiLine.getChildren();
		children = orderChildren(new ArrayList(),children);
		try {
			
		if(children!=null){
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				KpiLine kpiLineChild = (KpiLine) iterator.next();	
				
				Iterator it3 = sourceBeansToAdd.iterator();
				while(it3.hasNext()){
					SourceBean toAdd = (SourceBean)it3.next();
					bandDetailReport.setAttribute(toAdd);
				}	
				sourceBeansToAdd = new ArrayList();	
				
					if (actualHeight+10<maxFirstSubTemplateHeight){
						List sourceBeansToAdd2 = newLine(kpiLineChild, level+1,!evenLevel);
						if (sourceBeansToAdd2!=null && !sourceBeansToAdd2.isEmpty()){
						Iterator it = sourceBeansToAdd2.iterator();
							while(it.hasNext()){
								SourceBean toAdd = (SourceBean)it.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}else{
								
						//Add last subreport to the List
						increaseHeight(subTemplateBaseContent);
						subreports.add(subTemplateBaseContent);
						actualHeight = new Integer(0);
						subTemplateBaseContent = createNewSubReport(countSubreports);
						countSubreports ++;
						//Get my bandDetailReport from new subreport
						subtitleSB=(SourceBean)subTemplateBaseContent.getAttribute("title");
						bandDetailReport=(SourceBean)subtitleSB.getAttribute("BAND");
						//change subtemplatesummary
						subSummarySB=(SourceBean)subTemplateBaseContent.getAttribute("summary");
						bandSummaryReport=(SourceBean)subSummarySB.getAttribute("BAND");	
						//NEW SUBREPORT
						List sourceBeansToAdd2 = newLine(kpiLineChild, level+1,!evenLevel);
						if (sourceBeansToAdd2!=null && !sourceBeansToAdd2.isEmpty()){
						Iterator it2 = sourceBeansToAdd2.iterator();
							while(it2.hasNext()){
								SourceBean toAdd = (SourceBean)it2.next();
								bandDetailReport.setAttribute(toAdd);
							}	
						}
					}
				}
			}
		} catch (SourceBeanException e) {
			logger.error("SourceBeanException",e);
			e.printStackTrace();
		}
		
		logger.debug("OUT");
		return sourceBeansToAdd;
	}


	protected List orderChildren(List ordered, List notordered) {

		List toReturn = ordered;
		List temp = new ArrayList();
		KpiLine l = null;
		if(notordered!=null && !notordered.isEmpty()){
			Iterator it = notordered.iterator();
			while(it.hasNext()){
				KpiLine k = (KpiLine)it.next();
				if(l==null){
					l = k ;
				}else{
					if (k!=null && k.compareTo(l)<=0){
						temp.add(l);
						l = k;
					}else{
						temp.add(k);
					}
				}
			}
			toReturn.add(l);
			toReturn = orderChildren(toReturn,temp);
		}
		return toReturn;
	}

	private SourceBean setLineAttributes(KpiLine line,SourceBean semaphor, SourceBean textCodeName, SourceBean textValue, 
			SourceBean textWeight, SourceBean image1, int level, SourceBean separatorline,SourceBean threshCode,SourceBean threshValue, SourceBean extraimageToAdd){
		logger.debug("IN");
		
		KpiValue kpiValue=line.getValue();
		
		ThresholdValue t = null;
		Color colorSemaphor = null;
		if ( kpiValue!=null && kpiValue.getValue() != null) {
			t = kpiValue.getThresholdOfValue();
			if(t!=null){
				colorSemaphor = t.getColor();	
			}
		}	

		Integer xValue=xStarter+(xIncrease*Integer.valueOf(level));
		Integer yValue=actualHeight;

		try {
			//set Semaphor
			semaphor.setAttribute("reportElement.x", xValue.toString());
			semaphor.setAttribute("reportElement.y", new Integer(yValue.intValue()+2).toString());
			if(colorSemaphor!=null){

				String color=Integer.toHexString(colorSemaphor.getRGB());
				color="#"+color.substring(2);

				semaphor.setAttribute("reportElement.forecolor",  "#000000");
				semaphor.setAttribute("reportElement.backcolor", color);
			}else{
				semaphor.setAttribute("reportElement.forecolor", "#FFFFFF");
				semaphor.setAttribute("reportElement.backcolor", "#FFFFFF");
			}
			xValue=xValue+semaphorWidth+separatorWidth;

			// set text 1: Model CODE - Model NAME
			textCodeName.setAttribute("reportElement.x", (xValue));
			textCodeName.setAttribute("reportElement.y", yValue.toString());
			SourceBean textValue1=(SourceBean)textCodeName.getAttribute("text");
			textValue1.setCharacters(line.getModelInstanceCode()+"-"+line.getModelNodeName());

			xValue=xValue+textWidth+separatorWidth;

			//Set Value, weight and threshold code and value
			if(kpiValue!=null){
				String value1=kpiValue.getValue() != null ? kpiValue.getValue() : "";
				//set text2
				textValue.setAttribute("reportElement.y", yValue.toString());
				SourceBean textValue2=(SourceBean)textValue.getAttribute("text");
				textValue2.setCharacters(value1);

				String weight=(kpiValue.getWeight()!=null) ? kpiValue.getWeight().toString() : "";
				//set text2
				xValue=xValue+numbersWidth+separatorWidth;
				textWeight.setAttribute("reportElement.y", new Integer(yValue.intValue()+2).toString());
				SourceBean textValue3=(SourceBean)textWeight.getAttribute("text");
				textValue3.setCharacters(weight);
				
				
				if(t!=null){
					try {
						Threshold tr = DAOFactory.getThresholdDAO().loadThresholdById(t.getThresholdId());
						if (!thresholdsList.contains(tr)){
							thresholdsList.add(tr);
						}
						
					} catch (EMFUserError e) {
						logger.error("error in loading the Threshold by Id",e);
						e.printStackTrace();
					}
					String code=t.getThresholdCode() != null ? t.getThresholdCode() : "";
					String codeTh = "Code: "+code;
					if(codeTh.length()>20)codeTh = codeTh.substring(0, 19);
					
					threshCode.setAttribute("reportElement.y",  new Integer(yValue.intValue()-2).toString());
					SourceBean threshCode2=(SourceBean)threshCode.getAttribute("text");
					threshCode2.setCharacters(codeTh);
				
				
					String labelTh=t.getLabel() != null ? t.getLabel() : "";
					String min = t.getMinValue()!= null ? t.getMinValue().toString() : null;
					String max = t.getMaxValue()!= null ?  t.getMaxValue().toString() : null;
					String valueTh = "Value: ";
					if(t.getThresholdType().equalsIgnoreCase("RANGE")){
						if (min!=null && max !=null){
							  valueTh = valueTh + min+"-"+max+" "+labelTh;
						}else if (min!=null && max==null){
							valueTh = valueTh + "> "+min+" "+labelTh;
						}else if (min==null && max!=null){
							 valueTh = valueTh + "< "+max+" "+labelTh;
						}
					}else if(t.getThresholdType().equalsIgnoreCase("MINIMUM")){
						valueTh = valueTh + "< "+min+" "+labelTh;
					}else if(t.getThresholdType().equalsIgnoreCase("MAXIMUM")){
						valueTh = valueTh + "> "+max+" "+labelTh;
					}
					if(valueTh.length()>25)valueTh = valueTh.substring(0, 24);
					
					threshValue.setAttribute("reportElement.y", new Integer(yValue.intValue()+7).toString());
					SourceBean threshValue2=(SourceBean)threshValue.getAttribute("text");
					threshValue2.setCharacters(valueTh);
				}

			}
			//Sets the bullet chart and or the threshold image
			if(options.getDisplay_bullet_chart() && options.getDisplay_threshold_image()){
				//both threshold image and bullet chart have to be seen
				if ( kpiValue!=null &&  kpiValue.getValue()!= null && kpiValue.getThresholdValues()!=null && !kpiValue.getThresholdValues().isEmpty()) {

					List thresholdValues = kpiValue.getThresholdValues();			
					// String chartType = value.getChartType(); 
					String chartType = "BulletGraph";
					Double val = new Double(kpiValue.getValue());
					Double target = kpiValue.getTarget();
					ChartImpl sbi = ChartImpl.createChart(chartType);
					sbi.setValueDataSet(val);
					if (target != null) {
						sbi.setTarget(target);
					}
					sbi.setShowAxis(options.getShow_axis());	
					sbi.setThresholdValues(thresholdValues);
				
					JFreeChart chart = sbi.createChart();
					ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
					String requestIdentity = null;
					UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
					UUID uuid = uuidGen.generateTimeBasedUUID();
					requestIdentity = uuid.toString();
					requestIdentity = requestIdentity.replaceAll("-", "");
					String path_param = requestIdentity;
					String dir=System.getProperty("java.io.tmpdir");
					String path=dir+"/"+requestIdentity+".png";
					java.io.File file1 = new java.io.File(path);
					logger.debug("Where is the image: "+path);
					try {
						ChartUtilities.saveChartAsPNG(file1, chart, 89, 11, info);
					} catch (IOException e) {
						e.printStackTrace();
						logger.error("Error in saving chart",e);
					}
					String urlPng=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
					"?ACTION_NAME=GET_PNG2&NEW_SESSION=TRUE&path="+path_param+"&LIGHT_NAVIGATOR_DISABLED=TRUE";
					urlPng = "new java.net.URL(\""+urlPng+"\")";
					logger.debug("Image url: "+urlPng);
					
					image1.setAttribute("reportElement.y", yValue.toString());
					image1.setAttribute("reportElement.x", new Integer(310).toString());
					image1.setAttribute("reportElement.width", 90);
					SourceBean imageValue=(SourceBean)image1.getAttribute("imageExpression");
					imageValue.setCharacters(urlPng);
				}
				ThresholdValue tOfVal = line.getThresholdOfValue();
				if (tOfVal!=null && tOfVal.getPosition()!=null && tOfVal.getThresholdCode()!=null){
					String fileName ="position_"+tOfVal.getPosition().intValue();
					String dirName = tOfVal.getThresholdCode();
					String urlPng=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
					"?ACTION_NAME=GET_THR_IMAGE&NEW_SESSION=TRUE&fileName="+fileName+"&dirName="+dirName+"&LIGHT_NAVIGATOR_DISABLED=TRUE";	
					
					urlPng = "new java.net.URL(\""+urlPng+"\")";
					logger.debug("url: "+urlPng);
					
					extraimageToAdd=new SourceBean(image);
					extraimageToAdd.setAttribute("reportElement.y", yValue.toString());
					extraimageToAdd.setAttribute("reportElement.width",35);
					extraimageToAdd.setAttribute("reportElement.x", new Integer(408).toString());
					SourceBean imageValue=(SourceBean)extraimageToAdd.getAttribute("imageExpression");
					imageValue.setCharacters(urlPng);			
				}
			}else if(options.getDisplay_bullet_chart() && !options.getDisplay_threshold_image()){
				//only bullet chart has to be seen
				if ( kpiValue!=null &&  kpiValue.getValue()!= null && kpiValue.getThresholdValues()!=null && !kpiValue.getThresholdValues().isEmpty()) {

					List thresholdValues = kpiValue.getThresholdValues();			
					// String chartType = value.getChartType(); 
					String chartType = "BulletGraph";
					Double val = new Double(kpiValue.getValue());
					Double target = kpiValue.getTarget();
					ChartImpl sbi = ChartImpl.createChart(chartType);
					sbi.setValueDataSet(val);
					if (target != null) {
						sbi.setTarget(target);
					}
					sbi.setShowAxis(options.getShow_axis());	
					sbi.setThresholdValues(thresholdValues);
					
					JFreeChart chart = sbi.createChart();
					ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
					String requestIdentity = null;
					UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
					UUID uuid = uuidGen.generateTimeBasedUUID();
					requestIdentity = uuid.toString();
					requestIdentity = requestIdentity.replaceAll("-", "");
					String path_param = requestIdentity;
					String dir=System.getProperty("java.io.tmpdir");
					String path=dir+"/"+requestIdentity+".png";
					java.io.File file1 = new java.io.File(path);
					logger.debug("Where is the image: "+path);
					try {
						ChartUtilities.saveChartAsPNG(file1, chart, 130, 11, info);
					} catch (IOException e) {
						e.printStackTrace();
						logger.error("Error in saving chart",e);
					}
					String urlPng=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
					"?ACTION_NAME=GET_PNG2&NEW_SESSION=TRUE&path="+path_param+"&LIGHT_NAVIGATOR_DISABLED=TRUE";
					urlPng = "new java.net.URL(\""+urlPng+"\")";
					logger.debug("Image url: "+urlPng);
					
					image1.setAttribute("reportElement.y", yValue.toString());
					SourceBean imageValue=(SourceBean)image1.getAttribute("imageExpression");
					imageValue.setCharacters(urlPng);
				}
			}else if(!options.getDisplay_bullet_chart() && options.getDisplay_threshold_image()){
				//only threshold image has to be seen
				ThresholdValue tOfVal = line.getThresholdOfValue();
				if (tOfVal!=null && tOfVal.getPosition()!=null && tOfVal.getThresholdCode()!=null){
					String fileName ="position_"+tOfVal.getPosition().intValue();
					String dirName = tOfVal.getThresholdCode();
					String urlPng=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
					"?ACTION_NAME=GET_THR_IMAGE&NEW_SESSION=TRUE&fileName="+fileName+"&dirName="+dirName+"&LIGHT_NAVIGATOR_DISABLED=TRUE";	
					
					urlPng = "new java.net.URL(\""+urlPng+"\")";
					logger.debug("url: "+urlPng);
					image1.setAttribute("reportElement.y", yValue.toString());
					SourceBean imageValue=(SourceBean)image1.getAttribute("imageExpression");
					imageValue.setCharacters(urlPng);
					
				}
			}

			separatorline.setAttribute("reportElement.y", new Integer(yValue.intValue()+16).toString());

		} catch (SourceBeanException e) {
			logger.error("error in drawing the line",e);
			e.printStackTrace();
		}
		logger.debug("OUT");
		return extraimageToAdd;
	}

	public List newThresholdBlock( SourceBean bandDetailReport){
		logger.debug("IN");
		List sourceBeansToAdd = new ArrayList();
			try{//Draws the Threshold Band and Title
				actualHeight+=separatorModelsHeight;
				SourceBean thresholdBand1=new SourceBean(thresholdBand);  
				SourceBean thresholdTitle1=new SourceBean(thresholdTitle); 
				
				thresholdBand1.setAttribute("reportElement.y",actualHeight.toString());
				thresholdTitle1.setAttribute("reportElement.y",actualHeight.toString());	
				
				sourceBeansToAdd.add(thresholdBand1);
				sourceBeansToAdd.add(thresholdTitle1);
				
				actualHeight+=columnHeaderHeight;
			}	
			catch (Exception e) {
				logger.error("Error in setting the resource band");
				return null;
			}
		
		logger.debug("OUT");
		return sourceBeansToAdd;
	}
	
	public List newThresholdLine(Threshold t){
		List sourceBeansToAdd = new ArrayList();
		if (t!=null){
			try {
			actualHeight+=separatorHeight;	
			Integer yValue=actualHeight;
			//Draws the Threshold Code
			SourceBean thresholdTextCode1 = new SourceBean(thresholdTextCode);
			
			String code=t.getCode() != null ? t.getCode() : "";
			String codeTh = "Code: "+code;
			if(codeTh.length()>20)codeTh = codeTh.substring(0, 19);

			thresholdTextCode1.setAttribute("reportElement.y", yValue.toString());
			SourceBean threshCode2=(SourceBean)thresholdTextCode1.getAttribute("text");
			threshCode2.setCharacters(codeTh);
			
			sourceBeansToAdd.add(thresholdTextCode1);
			List semaphoreValue = newThresholdLine(t, bandDetailReport);
			sourceBeansToAdd.addAll(semaphoreValue);		
			
			//Adds a separator line
			SourceBean thresholdLineSeparator1=new SourceBean(thresholdLineSeparator); 
			thresholdLineSeparator1.setAttribute("reportElement.y",new Integer(yValue.intValue()+16).toString());
			sourceBeansToAdd.add(thresholdLineSeparator1);
			
			} catch (SourceBeanException e) {
				logger.error("SourceBeanException", e);
				e.printStackTrace();
			}  
		}
		return sourceBeansToAdd;
	}
	
	public List newThresholdLine(Threshold t, SourceBean bandDetailReport){
		logger.debug("IN");
		List sourceBeansToAdd = new ArrayList();
		try {
			actualHeight+=separatorHeight;	
			Integer yValue=actualHeight;
			Integer xValue = new Integer(5);
			List thValues = t.getThresholdValues();
			if(thValues!=null && !thValues.isEmpty()){
				Iterator thIt = thValues.iterator();
				while(thIt.hasNext()){
					ThresholdValue val = (ThresholdValue)thIt.next();
					if (val!=null){
						SourceBean semaphor1=new SourceBean(semaphor);
						SourceBean thresholdTextValue1=new SourceBean(thresholdTextValue);
						
						//Semaphore Threshold creation
						
						xValue = xValue + thresholdFieldWidth;
						
						if(xValue +thresholdSemaphoreWidth + thresholdFieldWidth>=maxSubTemplateWIdtht){	
							xValue = new Integer(5) + thresholdFieldWidth;
							actualHeight+=valueHeight;
							yValue = actualHeight;
						}
							String colorSemaphor = val.getColourString();
							semaphor1.setAttribute("reportElement.x", xValue.toString());
							semaphor1.setAttribute("reportElement.y", new Integer(yValue.intValue()+2).toString());
							if(colorSemaphor!=null){
								semaphor1.setAttribute("reportElement.forecolor",  "#000000");
								semaphor1.setAttribute("reportElement.backcolor", colorSemaphor);
							}else{
								semaphor1.setAttribute("reportElement.forecolor", "#FFFFFF");
								semaphor1.setAttribute("reportElement.backcolor", "#FFFFFF");
							}
							sourceBeansToAdd.add(semaphor1);
						
							xValue = xValue + thresholdFieldSeparatorWidth;	
												
							//Threshold Value Creation	
							String labelTh=val.getLabel() != null ? val.getLabel() : "";
							String min = val.getMinValue()!= null ? val.getMinValue().toString() : null;
							String max = val.getMaxValue()!= null ?  val.getMaxValue().toString() : null;
							String valueTh = "Value: ";
							if(val.getThresholdType().equalsIgnoreCase("RANGE")){
								if (min!=null && max !=null){
									  valueTh = valueTh + min+"-"+max+" "+labelTh;
								}else if (min!=null && max==null){
									valueTh = valueTh + "> "+min+" "+labelTh;
								}else if (min==null && max!=null){
									 valueTh = valueTh + "< "+max+" "+labelTh;
								}
							}else if(val.getThresholdType().equalsIgnoreCase("MINIMUM")){
								valueTh = valueTh + "< "+min+" "+labelTh;
							}else if(val.getThresholdType().equalsIgnoreCase("MAXIMUM")){
								valueTh = valueTh + "> "+max+" "+labelTh;
							}
							if(valueTh.length()>25)valueTh = valueTh.substring(0, 24);
	
							thresholdTextValue1.setAttribute("reportElement.x", xValue.toString());
							thresholdTextValue1.setAttribute("reportElement.y", yValue.toString());
							SourceBean threshValue2=(SourceBean)thresholdTextValue1.getAttribute("text");
							threshValue2.setCharacters(valueTh);
							
							sourceBeansToAdd.add(thresholdTextValue1);
					}
				}
			}

			actualHeight+=valueHeight;
			
		} catch (SourceBeanException e) {
			logger.error("error while adding a threshold line");
			return null;
		}

		logger.debug("OUT");
		return sourceBeansToAdd;
	}


	private void nameResolution(){

//		property
//		import
//		queryString
//		field
//		variable
//		background
//		band
//		title
//		line
//		reportElement
//		graphicElement
//		textField
//		box
//		textElement
//		font
//		textFieldExpression
//		pageHeader
//		columnHeader
//		detail
//		columnFooter
//		pageFooter
//		summary
//		staticText
//		text
//		image
//		imageExpression
		
		nameResolution.add(new NameRes("QUERYSTRING", "queryString"));
		nameResolution.add(new NameRes("TOPPEN", "topPen"));
		nameResolution.add(new NameRes("BOTTOMPEN", "bottomPen"));
		nameResolution.add(new NameRes("LEFTPEN", "leftPen"));
		nameResolution.add(new NameRes("RIGHTPEN", "rightPen"));
		nameResolution.add(new NameRes("GROUPEXPRESSION", "groupExpression"));
		nameResolution.add(new NameRes("groupEXPRESSION", "groupExpression"));
		nameResolution.add(new NameRes("GROUPHEADER", "groupHeader"));
		nameResolution.add(new NameRes("GROUPFOOTER", "groupFooter"));
		nameResolution.add(new NameRes("groupHEADER", "groupHeader"));
		nameResolution.add(new NameRes("groupFOOTER", "groupFooter"));
		nameResolution.add(new NameRes("GROUP", "group"));
		nameResolution.add(new NameRes("IMAGEEXPRESSION", "imageExpression"));
		nameResolution.add(new NameRes("imageEXPRESSION", "imageExpression"));
		nameResolution.add(new NameRes("SUBREPORT", "subreport"));
		nameResolution.add(new NameRes("SUBREPORTEXPRESSION", "subreportExpression"));
		nameResolution.add(new NameRes("subreportEXPRESSION", "subreportExpression"));
		nameResolution.add(new NameRes("CONNECTIONEXPRESSION", "connectionExpression"));
		nameResolution.add(new NameRes("connectionEXPRESSION", "connectionExpression"));
		nameResolution.add(new NameRes("JASPERREPORT", "jasperReport"));
		nameResolution.add(new NameRes("IMPORT", "import"));
		nameResolution.add(new NameRes("PROPERTY", "property"));
		nameResolution.add(new NameRes("QUERYSTRING", "queryString"));
		nameResolution.add(new NameRes("FIELD", "field"));
		nameResolution.add(new NameRes("VARIABLE", "variable"));
		nameResolution.add(new NameRes("BACKGROUND", "background"));
		nameResolution.add(new NameRes("BAND", "band"));
		nameResolution.add(new NameRes("TITLE", "title"));
		nameResolution.add(new NameRes("LINE", "line"));
		nameResolution.add(new NameRes("REPORTELEMENT", "reportElement"));
		nameResolution.add(new NameRes("GRAPHICELEMENT", "graphicElement"));
		nameResolution.add(new NameRes("reportELEMENT", "reportElement"));
		nameResolution.add(new NameRes("graphicELEMENT", "graphicElement"));
		nameResolution.add(new NameRes("TEXTFIELD", "textField"));
		nameResolution.add(new NameRes("textFIELD", "textField"));
		nameResolution.add(new NameRes("BOX", "box"));
		nameResolution.add(new NameRes("TEXTELEMENT", "textElement"));
		nameResolution.add(new NameRes("textELEMENT", "textElement"));
		nameResolution.add(new NameRes("FONT", "font"));
		nameResolution.add(new NameRes("TEXTFIELDEXPRESSION", "textFieldExpression"));
		nameResolution.add(new NameRes("textFIELDEXPRESSION", "textFieldExpression"));
		nameResolution.add(new NameRes("textFieldEXPRESSION", "textFieldExpression"));
		nameResolution.add(new NameRes("PAGEHEADER", "pageHeader"));
		nameResolution.add(new NameRes("COULMNHEADER", "columnHeader"));
		nameResolution.add(new NameRes("DETAIL", "detail"));
		nameResolution.add(new NameRes("COLUMNFOOTER", "columnFooter"));
		nameResolution.add(new NameRes("PAGEFOOTER", "pageFooter"));
		nameResolution.add(new NameRes("SUMMARY", "summary"));
		nameResolution.add(new NameRes("STATICTEXT", "staticText"));
		nameResolution.add(new NameRes("STATICText", "staticText"));
		nameResolution.add(new NameRes("TEXT", "text"));
		nameResolution.add(new NameRes("IMAGE", "image"));	
		nameResolution.add(new NameRes("RECTANGLE", "rectangle"));
		nameResolution.add(new NameRes("INITIALVALUEEXPRESSION", "initialValueExpression"));
		nameResolution.add(new NameRes("COLUMNHEADER", "columnHeader"));
	}

	

	private class NameRes
	{
		private String toSubstitute ;
		private String correctString;
		
		private NameRes(String a, String b){
			toSubstitute=a;
			correctString=b;
		}
		
		private String getToSubstitute(){
			return toSubstitute;
		}
		
		private String getCorrectString(){
			return correctString;
		}
	}

	/**
	 * Gets the template template.
	 * 
	 * @return the template template
	 */
	public String getTemplateTemplate() {
		StringBuffer buffer = new StringBuffer();
		logger.debug("IN");
		try{

			String templateDirPath = "it/eng/spagobi/kpi/utils/";
			logger.debug("templateDirPath: "+templateDirPath!=null ? templateDirPath : "");
			templateDirPath+="templateKpi.jrxml";
			logger.debug("templatePath: "+templateDirPath!=null ? templateDirPath : "");
			
			if (templateDirPath!=null){
								
			    InputStream fis= Thread.currentThread().getContextClassLoader().getResourceAsStream(templateDirPath);
	
				if(fis!=null){
					logger.debug("File Input Stream created");
				}
				inputSource=new InputSource(fis);
				if(inputSource!=null){
					logger.debug("Input Source created");
				}
				BufferedReader reader = new BufferedReader( new InputStreamReader(fis) );
				if(reader!=null){
					logger.debug("Buffer Reader created");
				}
				String line = null;
				try {
					while( (line = reader.readLine()) != null) {
						buffer.append(line + "\n");
					}
				} catch (IOException e) {
					logger.error("error in appending lines to the buffer",e);
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			logger.error("error in retrieving the template",e);
			e.printStackTrace();
			return null;
		}
		logger.debug("OUT");
		return buffer.toString();
	}
	
	/**
	 * Gets the template template.
	 * 
	 * @return the template template
	 */
	public String getTemplateSubTemplate() {
		StringBuffer buffer = new StringBuffer();
		logger.debug("IN");
		try{

			String templateDirPath = "it/eng/spagobi/kpi/utils/";
			logger.debug("templateDirPath: "+templateDirPath!=null ? templateDirPath : "");
			templateDirPath+="subTemplateKpi.jrxml";
			logger.debug("templatePath: "+templateDirPath!=null ? templateDirPath : "");
			
			if (templateDirPath!=null){
				InputStream fis= Thread.currentThread().getContextClassLoader().getResourceAsStream(templateDirPath);
				
				if(fis!=null){
					logger.debug("File Input Stream created");
				}
				inputSource=new InputSource(fis);
				if(inputSource!=null){
					logger.debug("Input Source created");
				}
				BufferedReader reader = new BufferedReader( new InputStreamReader(fis) );
				if(reader!=null){
					logger.debug("Buffer Reader created");
				}
				String line = null;
				try {
					while( (line = reader.readLine()) != null) {
						buffer.append(line + "\n");
					}
				} catch (IOException e) {
					logger.error("error in appending lines to the buffer",e);
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			logger.error("error in retrieving the template",e);
			e.printStackTrace();
			return null;
		}
		logger.debug("OUT");
		return buffer.toString();
	}


	/**
	 * Replace param.
	 * 
	 * @param template the template
	 * @param pname the pname
	 * @param pvalue the pvalue
	 * 
	 * @return the string
	 */
	private String replaceParam(String template, String pname, String pvalue) {
		logger.debug("IN");
		int index = -1;
		while( (index = template.indexOf("${" + pname + "}")) != -1) {
			template = template.replaceAll("\\$\\{" + pname + "\\}", pvalue);
		}
		logger.debug("OUT");
		return template;
	}

}
