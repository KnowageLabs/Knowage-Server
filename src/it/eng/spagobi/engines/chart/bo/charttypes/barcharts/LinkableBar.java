/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyCategoryUrlGenerator;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


public class LinkableBar extends BarCharts implements ILinkableChart {

	String rootUrl=null;
	String mode="";
	String drillLabel="";
	HashMap<String, DrillParameter> drillParametersMap=null;
	String categoryUrlName="";
	String serieUrlname="";
	boolean horizontalView=false; //false is vertical, true is horizontal
	boolean horizontalViewConfigured=false;
	/** Orientation of the chart: horizontal, vertical */
	public static final String ORIENTATION = "orientation";
	String drillDocTitle = null;
	String target = "self";

	private static transient Logger logger=Logger.getLogger(LinkableBar.class);


	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);

		if(confParameters.get(ORIENTATION)!=null){	
			String orientation=(String)confParameters.get(ORIENTATION);
			if(orientation.equalsIgnoreCase("vertical")){
				horizontalViewConfigured=true;
				horizontalView=false;
			}
			else if(orientation.equalsIgnoreCase("horizontal")){
				horizontalViewConfigured=true;
				horizontalView=true;
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

					if(name.equalsIgnoreCase("seriesurlname"))serieUrlname=value;
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
					}
					else if(name.equalsIgnoreCase("categoryurlname"))categoryUrlName=value;
					else{
						if(this.getParametersObject().get(name)!=null){
							value=(String)getParametersObject().get(name);
						}

						DrillParameter drillPar=new DrillParameter(name,type,value);
						drillParametersMap.put(name, drillPar);
					}
				}
				//}
			}
		}
		logger.debug("OUT");	
	}




	/**
	 * Inherited by IChart.
	 * 
	 * @param chartTitle the chart title
	 * @param dataset the dataset
	 * 
	 * @return the j free chart
	 */



	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		CategoryDataset dataset=(CategoryDataset)datasets.getDatasets().get("1");

		CategoryAxis categoryAxis = new CategoryAxis(categoryLabel);
		ValueAxis valueAxis = new NumberAxis(valueLabel);
		if(rangeIntegerValues==true){
			valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}

		org.jfree.chart.renderer.category.BarRenderer renderer = new org.jfree.chart.renderer.category.BarRenderer();

		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
//		renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
//		renderer.setBaseItemLabelPaint(styleValueLabels.getColor());

		if(showValueLabels){
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());			
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());
		}		


		if(maxBarWidth!=null){
			renderer.setMaximumBarWidth(maxBarWidth.doubleValue());
		}

		boolean document_composition=false;
		if(mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))document_composition=true;


		MyCategoryUrlGenerator mycatUrl=new MyCategoryUrlGenerator(rootUrl);
		mycatUrl.setDocument_composition(document_composition);
		mycatUrl.setCategoryUrlLabel(categoryUrlName);
		mycatUrl.setSerieUrlLabel(serieUrlname);
		mycatUrl.setDrillDocTitle(drillDocTitle);
		mycatUrl.setTarget(target);

		renderer.setItemURLGenerator(mycatUrl);

		/*		}
		else{
			renderer.setItemURLGenerator(new StandardCategoryURLGenerator(rootUrl));
		}*/

		CategoryPlot plot = new CategoryPlot((CategoryDataset)dataset, categoryAxis, valueAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		if(horizontalView)
		{
			plot.setOrientation(PlotOrientation.HORIZONTAL);
		}

		JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}



		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(color);

		// get a reference to the plot for further customisation...
		//CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setNumberFormatOverride(nf);



		if(rangeAxisLocation != null) {
			if(rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
			} else if(rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
			}else if(rangeAxisLocation.equalsIgnoreCase("TOP_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_RIGHT);
			} else if(rangeAxisLocation.equalsIgnoreCase("TOP_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
			}
		}

		// disable bar outlines...
		//BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);


		/*	if(currentSeries!=null && colorMap!=null){
			//for each serie selected
			int j=0;	
			for (Iterator iterator = currentSeries.iterator(); iterator.hasNext();) {
				String s = (String) iterator.next();
				Integer position=(Integer)seriesNumber.get(s);
				// check if for that position a value is defined
				if(colorMap.get("color"+position.toString())!=null){
					Color col= (Color)colorMap.get("color"+position);
					renderer.setSeriesPaint(j, col);
				}
				j++;
			}  // close for on series
		} // close case series selcted and color defined
		else{
			if(colorMap!=null){ // if series not selected check color each one

				for (Iterator iterator = colorMap.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					Color col= (Color)colorMap.get(key);
					String keyNum=key.substring(5, key.length());
					int num=Integer.valueOf(keyNum).intValue();
					num=num-1;
					renderer.setSeriesPaint(num, col);
				}
			}
		}*/

		int seriesN=dataset.getRowCount();
		
		if(orderColorVector != null && orderColorVector.size()>0){
			logger.debug("color serie by SERIES_ORDER_COLORS template specification");
			for (int i = 0; i < seriesN; i++) {
				if( orderColorVector.get(i)!= null){
					Color color = orderColorVector.get(i);
					renderer.setSeriesPaint(i, color);
				}		
			}	
		}		
		else 
		if(colorMap!=null){
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getRowKey(i);
				Color color=(Color)colorMap.get(serieName);
				if(color!=null){
					renderer.setSeriesPaint(i, color);
				}	
			}
		}


		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
				CategoryLabelPositions.createUpRotationLabelPositions(
						Math.PI / 6.0));
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());

		if(legend==true) drawLegend(chart);

		logger.debug("OUT");
		return chart;

	}



	/**
	 * Gets document parameters and return a string in the form &param1=value1&param2=value2 ...
	 * 
	 * @param drillParameters the drill parameters
	 * 
	 * @return the document_ parameters
	 */

	public String getDocument_Parameters(HashMap<String, DrillParameter> _drillParametersMap) { 
		logger.debug("IN");
		String document_parameter="";
		if(_drillParametersMap!=null){
			for (Iterator iterator = _drillParametersMap.keySet().iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				DrillParameter drillPar=(DrillParameter)_drillParametersMap.get(name);
				String value=drillPar.getValue();
				if(name!=null && !name.equals("") && value!=null && !value.equals("")){
					document_parameter+="%26"+name+"%3D"+value;
					//document_parameter+="&"+name+"="+value;
				}
			}
		} 
		logger.debug("OUT");
		return document_parameter;
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
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isLinkable()
	 */
	public boolean isLinkable(){
		return true;
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
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getSerieUrlname()
	 */
	public String getSerieUrlname() {
		return serieUrlname;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setSerieUrlname(java.lang.String)
	 */
	public void setSerieUrlname(String serieUrlname) {
		this.serieUrlname = serieUrlname;
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
