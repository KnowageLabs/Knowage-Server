/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.targetcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.TargetThreshold;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.Plot;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

public class TargetCharts extends ChartImpl {

	private static transient Logger logger=Logger.getLogger(TargetCharts.class);
	public double WIN = 0.5;
	public double LOSE = -0.5;

	public boolean useTargets=true;
	//public HashMap<Double, String> thresholds=new HashMap<Double, String>();
	public HashMap<Double, TargetThreshold> thresholds=new HashMap<Double, TargetThreshold>();
	public TargetThreshold bottomThreshold=null;
	// if it is a target the color is referring to what exceed , if its a baseline to what is under
	//public HashMap<String, Color> thresholdColors=new HashMap<String, Color>();
	public Double mainThreshold=null;
	Map confParameters=null;

	final protected TimeSeries timeSeries=new TimeSeries("TimeSerie", Month.class);;
	protected TreeSet<String> yearsDefined=null;
	// store if specified the maximum bar width
	Double maxBarWidth=null;
	StyleLabel styleValueLabels;

	protected Month firstMonth=null;
	protected Month lastMonth=null;
	protected String lastYear="";

	Double wlt_mode=new Double(0.0);


	// ************ PARAMETERS TO SET IN TEMPLATE *************
	/* 	<TARGETS>
  			<TARGET name='target1' value='3.0' color='#00FF00' main='false'/>
  			<TARGET name='target2' value='1.0' color='#0000FF' main='true'/>
  			<TARGET value='bottom' color='#AAAAAA' />
		</TARGETS>*/
	public static final String TARGETS = "TARGETS";
	public static final String BASELINES = "BASELINES";


	/** the maximum bar width, which is specified as a percentage of the available space for all bars 
	 * For Example setting to 0.05 will ensure that the bars never exceed five per cent of the lenght of the axis
	 * */
	public static final String MAXIMUM_BAR_WIDTH = "maximum_bar_width";
	/** Visualization of labels, possible values are from 0.0 to 5.0 */
	public static final String WLT_MODE = "wlt_mode";
	/** Style of labels visualization */
	public static final String STYLE_VALUE_LABELS = "STYLE_VALUE_LABELS";

	/* ************** Parameters to retrieve in dataset ***********+ */
	public static final String YEAR_DS = "year";
	public static final String MONTH_DS = "month";
	public static final String VALUE_DS = "value";


	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");

		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		SourceBean sbRows=SourceBean.fromXMLString(res);

		yearsDefined=new TreeSet<String>();

		//timeSeries = new TimeSeries("TimeSerie", Month.class);
		String name="";
		String value="";
		String year="";
		String month="";
		String valueS="";

		// <ROW month='' year='' value='' /> (month is a number, 1 ... 12)  Run all the rows and fill time series
		List listAtts=sbRows.getAttributeAsList("ROW");
		try {
			boolean firstTurn=true;
			
			for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {

				SourceBean category = (SourceBean) iterator.next();
				List atts=category.getContainedAttributes();
				month="";
				year="";
				valueS="";


				// run all the attributes contained in the row
				for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();
					name=new String(object.getKey());
					value=new String((String)object.getValue());

					if(name.equalsIgnoreCase(MONTH_DS)){
						month=value;
					}
					else if(name.equalsIgnoreCase(YEAR_DS)){
						year=value;
						if(!yearsDefined.contains(year)){
							yearsDefined.add(year); 
						}
					}
					else if(name.equalsIgnoreCase(VALUE_DS)){
						valueS=value;
					}
				} // close run attributes

				int monthInt=Integer.valueOf(month).intValue();
				int yearInt=Integer.valueOf(year).intValue();

				double valueD=Double.valueOf(valueS);
				Month monthTo=new Month(monthInt,yearInt);
				timeSeries.add(monthTo,valueD);
			}
			//close run rows


		}
		catch (NumberFormatException e) {
			logger.error("not a valid Number format, row will be ignored",e);
		}			
		catch (Exception e) {
			logger.error("Error while retrieving data from dataset",e);
			return null;
		}

		if(yearsDefined.isEmpty()){
			logger.warn("dataset returned no rows");
			return new DatasetMap();
		}
		
		// Set the first and the last periods

		TimeSeriesDataItem item1=timeSeries.getDataItem(0);
		firstMonth=(Month)item1.getPeriod();
		TimeSeriesDataItem item2=timeSeries.getDataItem(timeSeries.getItemCount()-1);
		lastMonth=(Month)item2.getPeriod();
		lastYear=yearsDefined.last();

		DatasetMap datasets=new DatasetMap();
		logger.debug("OUT");
		return datasets;
	}

	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		confParameters = new HashMap<String, String>();
		SourceBean confSB = (SourceBean)content.getAttribute("CONF");
		if(confSB==null) return;
		List confAttrsList = confSB.getAttributeAsList("PARAMETER");
		Iterator confAttrsIter = confAttrsList.iterator();
		while(confAttrsIter.hasNext()) {
			SourceBean param = (SourceBean)confAttrsIter.next();
			String nameParam = (String)param.getAttribute("name");
			String valueParam = (String)param.getAttribute("value");
			confParameters.put(nameParam, valueParam);
		}

		//check if targets or baselines are defined as parameters, if not then search for them in template
		//**************************** PARAMETERES TARGET OR BASELINES DEFINITION **********************
		boolean targets=false;
		boolean baselines=false;
		boolean parameterThresholdDefined=false;
		Vector<String> targetNames=new Vector<String>();
		Vector<String> baselinesNames=new Vector<String>();
		for (Iterator iterator = parametersObject.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Object value=parametersObject.get(name);
			if(name.startsWith("target") && !value.toString().equalsIgnoreCase("[]")){
				targets=true;
				targetNames.add(name);
			}
			if(name.startsWith("baseline") && !value.toString().equalsIgnoreCase("[]")){ // if targets are used baseline will be ignored
				baselines=true;
				baselinesNames.add(name);
			}
		}

		if(targets==true){   // Case Target Found on parameters
			useTargets=true;			
			for (Iterator iterator = targetNames.iterator(); iterator.hasNext();) {
				String targetName = (String) iterator.next();
				String valueToParse=(String)parametersObject.get(targetName);
				TargetThreshold targThres=new TargetThreshold(valueToParse);
				if(targThres.getName().equalsIgnoreCase("bottom"))bottomThreshold=targThres;
				else{
					if(targThres.isVisible()){
						thresholds.put(targThres.getValue(), targThres);
						if(targThres.isMain()==true)mainThreshold=targThres.getValue();
					}
				}
			}
			if(bottomThreshold==null) bottomThreshold=new TargetThreshold("bottom",null, Color.BLACK,false, true);		
		}
		else if(baselines==true){ // Case Baselines found on parameters
			useTargets=false;
			for (Iterator iterator = baselinesNames.iterator(); iterator.hasNext();) {
				String targetName = (String) iterator.next();
				String valueToParse=(String)parametersObject.get(targetName);
				TargetThreshold targThres=new TargetThreshold(valueToParse);
				if(targThres.getName().equalsIgnoreCase("bottom"))bottomThreshold=targThres;
				else{
					if(targThres.isVisible()){
						thresholds.put(targThres.getValue(), targThres);
						if(targThres.isMain()==true)mainThreshold=targThres.getValue();
					}
				}
			}
			if(bottomThreshold==null) bottomThreshold=new TargetThreshold("bottom",null, Color.BLACK,false, true);

		}
		//**************************** TEMPLATE TARGET OR BASELINES DEFINITION **********************
		else {                       // Case targets or baselines defined in template
			/* <TARGETS>
			 *  <TARGET name='first' value='5' main='true'>
			 *  </TARGETS>
			 */
			List thresAttrsList=null;	
			SourceBean thresholdsSB = (SourceBean)content.getAttribute(TARGETS);
			if(thresholdsSB==null){
				thresholdsSB = (SourceBean)content.getAttribute(BASELINES);
				if(thresholdsSB==null)return;
				useTargets=false;
			}

			if(thresholdsSB!=null){
				thresAttrsList = thresholdsSB.getContainedSourceBeanAttributes();
			}
			if(thresAttrsList==null || thresAttrsList.isEmpty()){ 
				logger.error("targets or baselines not defined; error ");
				return;
			}
			else{
				thresholds=new HashMap<Double, TargetThreshold>();
				//thresholdColors=new HashMap<String, Color>();			
				Iterator targetsAttrsIter = thresAttrsList.iterator();
				while(targetsAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)targetsAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();
					String name= (String)param.getAttribute("name");
					String value= (String)param.getAttribute("value");
					String main= (String)param.getAttribute("main");
					String colorS = (String)param.getAttribute("color");
					String visibleS = (String)param.getAttribute("visible");

					Color colorC=Color.BLACK;
					boolean isMain=(main!=null && main.equalsIgnoreCase("true")) ? true : false;
					if(colorS!=null){
						try{
							colorC=Color.decode(colorS);
						}
						catch (Exception e) {
							logger.error("error in color defined, put BLACK as default"); 
						}
					}
					boolean isVisible=(visibleS!=null && (visibleS.equalsIgnoreCase("false") || visibleS.equalsIgnoreCase("0") || visibleS.equalsIgnoreCase("0.0"))) ? false : true;

					// The value of the threshold is bottom or a double value
					if(value!=null){
						if(value.equalsIgnoreCase("bottom")){ //if definin bottom case
							bottomThreshold=new TargetThreshold("bottom",null, colorC,false,true);
						}
						else if(!value.equalsIgnoreCase("bottom")){
							Double valueD=null;
							try{
								valueD=Double.valueOf(value);
							}
							catch (NumberFormatException e) {
								logger.error("Error in converting threshold double", e);
								return;
							}
							if(isVisible==true){
								thresholds.put(valueD, new TargetThreshold(name,valueD,colorC,isMain, isVisible));
								if(isMain==true){
									mainThreshold=valueD;
								}
							}
						}


					}
				}
			} // Template definition
		}



		if(confParameters.get(WLT_MODE)!=null){		
			String wltModeS=(String)confParameters.get(WLT_MODE);
			wlt_mode=Double.valueOf(wltModeS);
		}

		if(confParameters.get(MAXIMUM_BAR_WIDTH)!=null){		
			String maxBarWidthS=(String)confParameters.get(MAXIMUM_BAR_WIDTH);
			try{
				maxBarWidth=Double.valueOf(maxBarWidthS);
			}
			catch (NumberFormatException e) {
				logger.error("error in defining parameter "+MAXIMUM_BAR_WIDTH+": should be a double, it will be ignored",e);
			}
		}

		SourceBean styleValueLabelsSB = (SourceBean)content.getAttribute(STYLE_VALUE_LABELS);
		if(styleValueLabelsSB!=null){

			String fontS = (String)styleValueLabelsSB.getAttribute(FONT_STYLE);
			if(fontS==null){
				fontS = defaultLabelsStyle.getFontName();
			}
			String sizeS = (String)styleValueLabelsSB.getAttribute(SIZE_STYLE);
			String colorS = (String)styleValueLabelsSB.getAttribute(COLOR_STYLE);
			String orientationS = (String)styleValueLabelsSB.getAttribute(ORIENTATION_STYLE);
			if(orientationS==null){
				orientationS = "horizontal";
			}

			try{
				Color color= Color.BLACK;
				if(colorS!=null){
					color=Color.decode(colorS);
				}else{
					defaultLabelsStyle.getColor();
				}
				int size= 12;
				if(sizeS!=null){
					size=Integer.valueOf(sizeS).intValue();
				}else{
					size = defaultLabelsStyle.getSize();
				}

				styleValueLabels=new StyleLabel(fontS,size,color,orientationS);

			}
			catch (Exception e) {
				logger.error("Wrong style labels settings, use default");
			}

		}
		else{
			styleValueLabels = defaultLabelsStyle;
		}

		logger.debug("OUT");
	}



	public LegendItemCollection createThresholdLegend(Plot plot){
		logger.debug("IN");

		LegendItemCollection collection=new LegendItemCollection();

		for (Iterator iterator = thresholds.keySet().iterator(); iterator.hasNext();) {
			Double thres = (Double) iterator.next();
			TargetThreshold thresTarg=thresholds.get(thres);
			String thresholdName= thresTarg!=null ? thresTarg.getName() : "";

			Color color=Color.BLACK;

			if(thresTarg.getColor()!=null){
				color=thresTarg.getColor();		
			}
			// could add bottom only if used
			LegendItem item=new LegendItem(thresholdName, thresholdName, thresholdName,thresholdName, new Rectangle(10,10),color);
			collection.add(item);
		}
		logger.debug("OUT");
		return collection;
	}



}
