/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.piecharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyPieUrlGenerator;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

public class LinkablePie extends PieCharts implements ILinkableChart{

	boolean threeD=false; //false is 2D, true is 3D
	boolean isThreedViewConfigured=false;
	boolean percentage=false;
	boolean isPercentageConfigured=false;
	private static transient Logger logger=Logger.getLogger(LinkablePie.class);


	String rootUrl=null;
	String mode="";
	String drillLabel="";
	HashMap<String, DrillParameter> drillParametersMap=null;
	String categoryUrlName="";
	String drillDocTitle = null;
	String target = "self";


	public static final String CHANGE_VIEW_3D_LABEL="Set View Dimension";
	public static final String CHANGE_VIEW_3D_LABEL1="Set 2D";
	public static final String CHANGE_VIEW_3D_LABEL2="Set 3D";


	public static final String CHANGE_VIEW_3D="threeD";

	public static final String CHANGE_VIEW_PERCENTAGE_LABEL="Set Percentage Mode";
	public static final String CHANGE_VIEW_PERCENTAGE_LABEL1="Absolute Values";
	public static final String CHANGE_VIEW_PERCENTAGE_LABEL2="Percentage Values";


	public static final String CHANGE_VIEW_PERCENTAGE="percentage";



	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.piecharts.PieCharts#configureChart(it.eng.spago.base.SourceBean)
	 */
	public void configureChart(SourceBean content) {
		// TODO Auto-generated method stub
		super.configureChart(content);
		if(confParameters.get("dimensions")!=null){	
			String orientation=(String)confParameters.get("dimensions");
			if(orientation.equalsIgnoreCase("3D")){
				threeD=true;
				isThreedViewConfigured=true;
			}
			else if(orientation.equalsIgnoreCase("2D")){
				threeD=false;
				isThreedViewConfigured=true;
			}
		}
		if(confParameters.get("values")!=null){	
			String orientation=(String)confParameters.get("values");
			if(orientation.equalsIgnoreCase("percentage")){
				percentage=true;
				isPercentageConfigured=true;
			}
			else if(orientation.equalsIgnoreCase("absolute")){
				percentage=false;
				isPercentageConfigured=true;
			}
		}


		SourceBean drillSB = (SourceBean)content.getAttribute("DRILL");
		if(drillSB==null){
			drillSB = (SourceBean)content.getAttribute("CONF.DRILL");
		}
		if(drillSB!=null){
			String lab=(String)drillSB.getAttribute("document");
			if(lab!=null) drillLabel=lab;
			else{
				logger.error("Drill label not found");
			}

			List parameters =drillSB.getAttributeAsList("PARAM");
			if(parameters!=null){
				drillParametersMap=new HashMap<String, DrillParameter>();

				for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
					SourceBean att = (SourceBean) iterator.next();
					String name=(String)att.getAttribute("name");
					String type=(String)att.getAttribute("type");
					String value=(String)att.getAttribute("value");
					
					// default is relative
					if(type!=null && type.equalsIgnoreCase("absolute"))
						type="absolute";
					else
						type="relative";
					
					if(name.equalsIgnoreCase("categoryurlname"))categoryUrlName=value;
					else if(name.equalsIgnoreCase("target")){
						if(value!=null && value.equalsIgnoreCase("tab")){
							setTarget("tab");
						}else{
							setTarget("self");
						}
					}else if(name.equalsIgnoreCase("title")){
						if(value!=null && !value.equals("")){
							setDrillDocTitle(value);
						}
					}else{
						DrillParameter drillPar=new DrillParameter(name,type,value);
						drillParametersMap.put(name, drillPar);

					}
					
				}
			}
		}

	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.piecharts.PieCharts#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap datasets) {
		Dataset dataset=(Dataset)datasets.getDatasets().get("1");

		boolean document_composition=false;
		if(mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))document_composition=true;


		JFreeChart chart=null; 

		if(!threeD){
			chart = ChartFactory.createPieChart(
					name,  
					(PieDataset)dataset,             // data
					legend,                // include legend
					true,
					false
			);


			chart.setBackgroundPaint(color);

			TextTitle title = chart.getTitle();
			title.setToolTipText("A title tooltip!");


			PiePlot plot = (PiePlot) chart.getPlot();
			plot.setLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
			plot.setCircular(false);
			plot.setLabelGap(0.02);
			plot.setNoDataMessage("No data available");

			if(percentage==false){
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({1})"));}
			else
			{
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({2})"));
			}

			MyPieUrlGenerator pieUrl=new MyPieUrlGenerator(rootUrl);
			pieUrl.setDocument_composition(document_composition);
			pieUrl.setCategoryUrlLabel(categoryUrlName);
			pieUrl.setDrillDocTitle(drillDocTitle);
			pieUrl.setTarget(target);

			plot.setURLGenerator(pieUrl);			

			
			


		}
		else{
			chart = ChartFactory.createPieChart3D(
					name,  
					(PieDataset)dataset,             // data
					true,                // include legend
					true,
					false
			);



			chart.setBackgroundPaint(color);

			TextTitle title = chart.getTitle();
			title.setToolTipText("A title tooltip!");


			PiePlot3D plot = (PiePlot3D) chart.getPlot();

			plot.setDarkerSides(true);
			plot.setStartAngle(290);
			plot.setDirection(Rotation.CLOCKWISE);
			plot.setForegroundAlpha(1.0f);
			plot.setDepthFactor(0.2);

			plot.setLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
			plot.setCircular(false);
			plot.setLabelGap(0.02);
			plot.setNoDataMessage("No data available");



			//org.jfree.chart.renderer.category.BarRenderer renderer = new org.jfree.chart.renderer.category.AreaRenderer);

			MyPieUrlGenerator pieUrl=new MyPieUrlGenerator(rootUrl);
			pieUrl.setDocument_composition(document_composition);
			pieUrl.setCategoryUrlLabel(categoryUrlName);
			pieUrl.setDrillDocTitle(drillDocTitle);
			pieUrl.setTarget(target);

			plot.setURLGenerator(pieUrl);			


			if(percentage==false){
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
						"{0} ({1})"));}
			else
			{
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({2})"));
			}
		}


		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}



		return chart;

	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isChangeableView()
	 */
	public boolean isChangeableView() {
		return true;	
	}





	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#setChangeViewsParameter(java.lang.String, boolean)
	 */
	public void setChangeViewsParameter(String changePar, boolean how) {
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			threeD=how;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			percentage =how;
		}


	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameter(java.lang.String)
	 */
	public boolean getChangeViewParameter(String changePar) {
		boolean ret=false;
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			ret=threeD;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			ret=percentage;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameterLabel(java.lang.String, int)
	 */
	public String getChangeViewParameterLabel(String changePar, int i) {
		String ret="";
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			if(i==0) ret=CHANGE_VIEW_3D_LABEL;
			else if(i==1) ret=CHANGE_VIEW_3D_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_3D_LABEL2;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			if(i==0) ret=CHANGE_VIEW_PERCENTAGE_LABEL;
			else if(i==1) ret=CHANGE_VIEW_PERCENTAGE_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_PERCENTAGE_LABEL2;
		}

		return ret;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getPossibleChangePars()
	 */
	public List getPossibleChangePars() {
		List l=new Vector();
		if(!isThreedViewConfigured)	{l.add(CHANGE_VIEW_3D); }
		if(!isPercentageConfigured){ l.add(CHANGE_VIEW_PERCENTAGE); }
		return l;
	}


	/**
	 * Checks if is three d.
	 * 
	 * @return true, if is three d
	 */
	public boolean isThreeD() {
		return threeD;
	}


	/**
	 * Sets the three d.
	 * 
	 * @param threeD the new three d
	 */
	public void setThreeD(boolean threeD) {
		this.threeD = threeD;
	}


	/**
	 * Checks if is threed view configured.
	 * 
	 * @return true, if is threed view configured
	 */
	public boolean isThreedViewConfigured() {
		return isThreedViewConfigured;
	}


	/**
	 * Sets the threed view configured.
	 * 
	 * @param isThreedViewConfigured the new threed view configured
	 */
	public void setThreedViewConfigured(boolean isThreedViewConfigured) {
		this.isThreedViewConfigured = isThreedViewConfigured;
	}


	/**
	 * Checks if is percentage.
	 * 
	 * @return true, if is percentage
	 */
	public boolean isPercentage() {
		return percentage;
	}


	/**
	 * Sets the percentage.
	 * 
	 * @param percentage the new percentage
	 */
	public void setPercentage(boolean percentage) {
		this.percentage = percentage;
	}


	/**
	 * Checks if is percentage configured.
	 * 
	 * @return true, if is percentage configured
	 */
	public boolean isPercentageConfigured() {
		return isPercentageConfigured;
	}


	/**
	 * Sets the percentage configured.
	 * 
	 * @param isPercentageConfigured the new percentage configured
	 */
	public void setPercentageConfigured(boolean isPercentageConfigured) {
		this.isPercentageConfigured = isPercentageConfigured;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getRootUrl()
	 */
	public String getRootUrl() {
		return rootUrl;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setRootUrl(java.lang.String)
	 */
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getMode()
	 */
	public String getMode() {
		return mode;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setMode(java.lang.String)
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getDrillLabel()
	 */
	public String getDrillLabel() {
		return drillLabel;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setDrillLabel(java.lang.String)
	 */
	public void setDrillLabel(String drillLabel) {
		this.drillLabel = drillLabel;
	}





	public HashMap<String, DrillParameter> getDrillParametersMap() {
		return drillParametersMap;
	}


	public void setDrillParametersMap(
			HashMap<String, DrillParameter> drillParametersMap) {
		this.drillParametersMap = drillParametersMap;
	}


	/**
	 * Gets the cHANG e_ vie w_3 d_ label.
	 * 
	 * @return the cHANG e_ vie w_3 d_ label
	 */
	public static String getCHANGE_VIEW_3D_LABEL() {
		return CHANGE_VIEW_3D_LABEL;
	}


	/**
	 * Gets the cHANG e_ vie w_3 d_ labe l1.
	 * 
	 * @return the cHANG e_ vie w_3 d_ labe l1
	 */
	public static String getCHANGE_VIEW_3D_LABEL1() {
		return CHANGE_VIEW_3D_LABEL1;
	}


	/**
	 * Gets the cHANG e_ vie w_3 d_ labe l2.
	 * 
	 * @return the cHANG e_ vie w_3 d_ labe l2
	 */
	public static String getCHANGE_VIEW_3D_LABEL2() {
		return CHANGE_VIEW_3D_LABEL2;
	}


	/**
	 * Gets the cHANG e_ vie w_3 d.
	 * 
	 * @return the cHANG e_ vie w_3 d
	 */
	public static String getCHANGE_VIEW_3D() {
		return CHANGE_VIEW_3D;
	}


	/**
	 * Gets the cHANG e_ vie w_ percentag e_ label.
	 * 
	 * @return the cHANG e_ vie w_ percentag e_ label
	 */
	public static String getCHANGE_VIEW_PERCENTAGE_LABEL() {
		return CHANGE_VIEW_PERCENTAGE_LABEL;
	}


	/**
	 * Gets the cHANG e_ vie w_ percentag e_ labe l1.
	 * 
	 * @return the cHANG e_ vie w_ percentag e_ labe l1
	 */
	public static String getCHANGE_VIEW_PERCENTAGE_LABEL1() {
		return CHANGE_VIEW_PERCENTAGE_LABEL1;
	}


	/**
	 * Gets the cHANG e_ vie w_ percentag e_ labe l2.
	 * 
	 * @return the cHANG e_ vie w_ percentag e_ labe l2
	 */
	public static String getCHANGE_VIEW_PERCENTAGE_LABEL2() {
		return CHANGE_VIEW_PERCENTAGE_LABEL2;
	}


	/**
	 * Gets the cHANG e_ vie w_ percentage.
	 * 
	 * @return the cHANG e_ vie w_ percentage
	 */
	public static String getCHANGE_VIEW_PERCENTAGE() {
		return CHANGE_VIEW_PERCENTAGE;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isLinkable()
	 */
	public boolean isLinkable(){
		return true;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getCategoryUrlName()
	 */
	public String getCategoryUrlName() {
		return categoryUrlName;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setCategoryUrlName(java.lang.String)
	 */
	public void setCategoryUrlName(String categoryUrlName) {
		this.categoryUrlName = categoryUrlName;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getDocument_Parameters(java.util.HashMap)
	 */
	public String getDocument_Parameters(HashMap<String, DrillParameter> drillParametersMap) {
		String document_parameter="";
		for (Iterator iterator = drillParametersMap.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			DrillParameter drillPar=(DrillParameter)drillParametersMap.get(name);
			String value=drillPar.getValue();
			if(name!=null && !name.equals("") && value!=null && !value.equals("")){
				document_parameter+="%26"+name+"%3D"+value;
				//document_parameter+="&"+name+"="+value;
			}

		}
		return document_parameter;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getSerieUrlname()
	 */
	public String getSerieUrlname() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setSerieUrlname(java.lang.String)
	 */
	public void setSerieUrlname(String serieUrlname) {
		// TODO Auto-generated method stub
		
	}


	public String getDrillDocTitle() {
		return drillDocTitle;
	}


	public void setDrillDocTitle(String drillDocTitle) {
		this.drillDocTitle = drillDocTitle;
	}


	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}


}
