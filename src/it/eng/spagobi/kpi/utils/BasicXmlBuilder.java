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
import it.eng.spagobi.engines.kpi.bo.KpiLine;
import it.eng.spagobi.engines.kpi.bo.KpiResourceBlock;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.dao.IModelInstanceDAO;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;



//TODO: Auto-generated Javadoc
/**
 * The Class BasicTemplateBuilder.
 * 
 * @author Chiara Chiarelli
 */
public class BasicXmlBuilder  {

	private static transient org.apache.log4j.Logger logger=Logger.getLogger(BasicXmlBuilder.class);
	
	static String docKpiBlock = "<dockpi></dockpi>";
	
	static String title = "<title></title>";
	
	static String subtitle = "<subtitle></subtitle>";
	
	static String resourceBlock = "<resource name =\"\"></resource>";

	static String modelNodeLine = "<modelinstancenode name=\"\"  code=\"\"  description=\"\" ></modelinstancenode>";
	
	static String kpi = "<kpi name=\"\"  code=\"\"  description=\"\" interpretation=\"\" ></kpi>";
		
	static String kpivalue = "<kpivalue value=\"\" weight=\"\" weightedvalue=\"\" begindate=\"\" enddate=\"\" description=\"\" target=\"\" thresholdid=\"\" ></kpivalue>";
	
	static String thresholds = "<thresholds></thresholds>";
	
	static String threshold = "<threshold id=\"\" type=\"\" code=\"\" ></threshold>";
	
	static String range = "<range label=\"\" color=\"\" min=\"\" max=\"\" ></range>";

	SourceBean docKpiBlockS = null;
	SourceBean modelNodeLineS = null;
	SourceBean resourceBlockS = null;
	SourceBean titleS = null;
	SourceBean subtitleS = null;
	SourceBean kpiS = null;
	SourceBean kpivalueS = null;
	SourceBean thresholdsS = null;
	SourceBean thresholdS = null;
	SourceBean rangeS = null;

	String documentName=null;
	String documentTitle=null;
	String documentSubTitle=null;
	
	List thresholdsList=new ArrayList();

	List resources;
	InputSource inputSource;



	public BasicXmlBuilder(String documentName,String documentTitle,String documentSubTitle ) {
		super();
		this.documentName = documentName;
		this.documentTitle = documentTitle;
		this.documentSubTitle = documentSubTitle;
	}
	
	public BasicXmlBuilder(String documentName ) {
		super();
		this.documentName = documentName;
	}


	/* Build the template
	 * @see it.eng.qbe.export.ITemplateBuilder#buildTemplate()
	 */
	public String buildTemplate(List resources) {
		logger.debug("IN");
	
		SourceBean xmlBaseContent = null;
		
		try {
			docKpiBlockS = SourceBean.fromXMLString(docKpiBlock); 
			titleS = SourceBean.fromXMLString(title); 
			subtitleS = SourceBean.fromXMLString(subtitle); 
			resourceBlockS = SourceBean.fromXMLString(resourceBlock); 
			modelNodeLineS = SourceBean.fromXMLString(modelNodeLine);
			kpiS = SourceBean.fromXMLString(kpi); 
			kpivalueS = SourceBean.fromXMLString(kpivalue);
			thresholdsS = SourceBean.fromXMLString(thresholds); 
			thresholdS = SourceBean.fromXMLString(threshold);
			rangeS = SourceBean.fromXMLString(range);
		} catch (Exception e) {
			logger.error("Error in converting static elemnts into Source Beans, check the XML code");
		}
		createXml(resources);
		
		String finalTemplate = docKpiBlockS.toXML(false, false);

		System.out.println(finalTemplate);
		logger.debug("OUT");
		return finalTemplate;
	}

	public void createXml(List resources){
		logger.debug("IN");
		
		KpiResourceBlock tempBlock = (KpiResourceBlock)resources.get(0);
		this.documentTitle = tempBlock.getTitle();
		this.documentSubTitle = tempBlock.getSubtitle();
		createTitle();
		createSubtitle();
		
		for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
			SourceBean resToAdd = null;
			KpiResourceBlock thisBlock = (KpiResourceBlock) iterator.next();
			resToAdd = newResource(thisBlock);
			    try {
					docKpiBlockS.setAttribute(resToAdd);
				} catch (SourceBeanException e) {
					logger.error("SourceBean Exception",e);
					e.printStackTrace();
				}

		}
		
		createThresholdsList();
		
		logger.debug("OUT");
	}
	

	public void createTitle(){
		logger.debug("IN");
		try {
			SourceBean title = new SourceBean(titleS);
			title.setCharacters(this.documentTitle);
			docKpiBlockS.setAttribute(title);
		 } catch (SourceBeanException e) {
				logger.error("SourceBean Exception",e);
				e.printStackTrace();
			}		
		logger.debug("OUT");
	}
	
	public void createThresholdsList(){
		logger.debug("IN");
		try {
			SourceBean thresholdslist = new SourceBean(thresholdsS);
			if(thresholdsList!=null && !thresholdsList.isEmpty()){
				Iterator th = thresholdsList.iterator();
				while(th.hasNext()){
					Threshold t2 = (Threshold)th.next();
					SourceBean singlethreshold = new SourceBean(thresholdS);
					singlethreshold.setAttribute("id",t2.getId()!=null?t2.getId().toString():"");
					
					String type = "";
					String code = "";
					List thValues = t2.getThresholdValues();
					if(thValues!=null && !thValues.isEmpty()){
						Iterator th2 = thValues.iterator();
						while(th2.hasNext()){
							ThresholdValue t = (ThresholdValue)th2.next();
							SourceBean range = new SourceBean(rangeS);
							code = t.getThresholdCode() != null ? t.getThresholdCode() : "";							
							String label = t.getLabel() != null ? t.getLabel() : "";
							String min = t.getMinValue()!= null ? t.getMinValue().toString() : "";
							String max = t.getMaxValue()!= null ?  t.getMaxValue().toString() : "";
							String color = t.getColourString()!= null ?  t.getColourString() : "";
							type = t.getThresholdType()!= null ?   t.getThresholdType() : "";
							
							range.setAttribute("label",label);
							range.setAttribute("min",min);
							range.setAttribute("max",max);
							range.setAttribute("color",color);
							singlethreshold.setAttribute(range);
						}
					}		
					singlethreshold.setAttribute("code",code);
					singlethreshold.setAttribute("type",type!=null?type:"");
					thresholdslist.setAttribute(singlethreshold);
				}			
			}
						
			docKpiBlockS.setAttribute(thresholdslist);
		 } catch (SourceBeanException e) {
				logger.error("SourceBean Exception",e);
				e.printStackTrace();
			}		
		logger.debug("OUT");
	}
	
	public void createSubtitle(){
		logger.debug("IN");
		try {
			SourceBean subtitle = new SourceBean(subtitleS);
			subtitle.setCharacters(this.documentSubTitle);
			docKpiBlockS.setAttribute(subtitle);
		 } catch (SourceBeanException e) {
				logger.error("SourceBean Exception",e);
				e.printStackTrace();
			}		
		logger.debug("OUT");
	}
	
	public SourceBean newResource(KpiResourceBlock thisBlock){
		logger.debug("IN");
		SourceBean toReturn = null;
		Resource res=thisBlock.getR();
			
			try{
				SourceBean bandRes=new SourceBean(resourceBlockS);
				KpiLine lineRoot=thisBlock.getRoot();
				SourceBean modelNodeLine = newLine(lineRoot);
				
				if(res!=null){					
					bandRes.setAttribute("name",res.getName()!=null?res.getName():"");
					bandRes.setAttribute(modelNodeLine);
					toReturn = bandRes;
				}else{
					toReturn = modelNodeLine;
				}		
			}
			catch (Exception e) {
				logger.error("SourceBean Exception",e);
				return null;
			}
		
		logger.debug("OUT");
		return toReturn;
	}
	
	public SourceBean newLine(KpiLine line){
		logger.debug("IN");
		SourceBean modelNodeL = null;
			
			try{
				modelNodeL=new SourceBean(modelNodeLineS);
				
				modelNodeL.setAttribute("code",line.getModelInstanceCode()!=null?line.getModelInstanceCode():"");
				modelNodeL.setAttribute("name",line.getModelNodeName()!=null?line.getModelNodeName():"");
				
				KpiInstance k = null;
				if (line.getModelInstanceNodeId()!=null){
					Integer id = new Integer(line.getModelInstanceNodeId());
					Date d = new Date();
					IModelInstanceDAO modInstDAO=DAOFactory.getModelInstanceDAO();
					ModelInstanceNode n = modInstDAO.loadModelInstanceById(id, d);
					String descr = n.getDescr();
					modelNodeL.setAttribute("description",descr!=null?descr:"");		
					k= n.getKpiInstanceAssociated();
				}
				
				if(k!=null){
					SourceBean kpiToAdd = newKpi(k);
					modelNodeL.setAttribute(kpiToAdd);
				}
				
				if(line.getValue()!=null){
				SourceBean kpiValueToAdd = newKpiValue(line.getValue());
				ThresholdValue t = null;
				if ( line.getValue()!=null && line.getValue().getValue() != null) {
					t = line.getValue().getThresholdOfValue();
				}	
				if(t!=null){
					try {
						Threshold tr = DAOFactory.getThresholdDAO().loadThresholdById(t.getThresholdId());
						if (!thresholdsList.contains(tr)){
							thresholdsList.add(tr);
						}
						kpiValueToAdd.setAttribute("thresholdid",t.getThresholdId()!=null ? t.getThresholdId().toString() :"");
						
					} catch (EMFUserError e) {
						logger.error("error in loading the Threshold by Id",e);
						e.printStackTrace();
					}
							
				}
				modelNodeL.setAttribute(kpiValueToAdd);
				}
				
				
				
				List<KpiLine> children=line.getChildren();
				children = orderChildren(new ArrayList(),children);
			
				if(children!=null){
					for (Iterator iterator = children.iterator(); iterator.hasNext();) {
						KpiLine kpiLineChild = (KpiLine) iterator.next();
							SourceBean childNode = newLine(kpiLineChild);
							modelNodeL.setAttribute(childNode);
						}
					}
			}
			catch (Exception e) {
				logger.error("SourceBean Exception",e);
				return null;
			}
		
		logger.debug("OUT");
		return modelNodeL;
	}
	
	public SourceBean newKpi(KpiInstance ki){
		logger.debug("IN");
		SourceBean kpi = null;
			
			try{
				kpi=new SourceBean(kpiS);
				
				if (ki!=null){
					
					Integer kpiID = ki.getKpi();
					if (kpiID!=null){
						Kpi k = DAOFactory.getKpiDAO().loadKpiById(kpiID);
						String kpiCode = k.getCode();
						String kpiDescription = k.getDescription();
						String kpiInterpretation = k.getInterpretation();
						String kpiName = k.getKpiName();
						
						kpi.setAttribute("code",kpiCode!=null?kpiCode:"");
						kpi.setAttribute("name",kpiName!=null?kpiName:"");
						kpi.setAttribute("description",kpiDescription!=null?kpiDescription:"");	
						kpi.setAttribute("interpretation",kpiInterpretation!=null?kpiInterpretation:"");	
						
						/*List thresholdValues = null;
						if(ki.getThresholdId()!=null){
							thresholdValues=DAOFactory.getThresholdValueDAO().loadThresholdValuesByThresholdId(kI.getThresholdId());
						}*/
					}
				}
			}
			catch (Exception e) {
				logger.error("SourceBean Exception",e);
				return null;
			}
		
		logger.debug("OUT");
		return kpi;
	}
	
	public SourceBean newKpiValue(KpiValue value){
		logger.debug("IN");
		SourceBean kpivalue = null;
			
			try{
				kpivalue=new SourceBean(kpivalueS);
				
				if (value!=null){
						String kpiValue = value.getValue();
						Double weight = value.getWeight();
						String weightedValue = "";
						if (kpiValue!=null && !kpiValue.equals("") && weight!=null){
							Double val = new Double(kpiValue);
							Float lo =new Float(val*weight);
							weightedValue = lo.toString();
						}
						kpivalue.setAttribute("value",kpiValue!=null?kpiValue:"");
						kpivalue.setAttribute("weight",weight!=null?weight.toString():"");
						kpivalue.setAttribute("weightedvalue",weightedValue!=null?weightedValue:"");	
						kpivalue.setAttribute("begindate",value.getBeginDate()!=null?value.getBeginDate().toString():"");	
						kpivalue.setAttribute("enddate",value.getEndDate()!=null?value.getEndDate().toString():"");
						kpivalue.setAttribute("description",value.getValueDescr()!=null?value.getValueDescr():"");
						kpivalue.setAttribute("target",value.getTarget()!=null?value.getTarget().toString():"");	
						if(value.getValueXml()!=null){
							SourceBean xml = SourceBean.fromXMLString(value.getValueXml());
							kpivalue.setAttribute(xml);
						}
						//kpivalue.setAttribute("thresholdid",kpiInterpretation!=null?kpiInterpretation:"");	

				}
			}
			catch (Exception e) {
				logger.error("SourceBean Exception",e);
				return null;
			}
		
		logger.debug("OUT");
		return kpivalue;
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
