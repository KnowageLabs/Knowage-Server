/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.chart.bo.charttypes.dialcharts;


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultValueDataset;

/** 
 *  * @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */

public class DialCharts extends ChartImpl {

	private static transient Logger logger=Logger.getLogger(DialCharts.class);
	protected double lower=0.0;
	protected double upper=0.0;
	protected String units = "";
	StyleLabel labelsTickStyle;
	StyleLabel labelsValueStyle;
	Map confParameters;
	SourceBean sbRow;

	// *************************** PARAMETERS ***************************
	/** Style for tick labels*/
	public static final String STYLE_TICK_LABELS = "STYLE_TICK_LABELS";
	/** Style for value labels*/
	public static final String STYLE_VALUE_LABEL = "STYLE_VALUE_LABEL";
	/** Upper bound*/
	public static final String UPPER = "upper";
	/** Lower Bound*/
	public static final String LOWER = "lower";
	/** Units*/
	public static final String UNITS = "units";
	/** multichart*/
	public static final String MULTICHART = "multichart";
	/** orientation multichart: can be horiontal o vertical*/
	public static final String ORIENTATION_MULTICHART = "orientation_multichart";
	/** if to draw the legend*/
	public static final String LEGEND = "legend";

	/** Tag to define INTERVAL and attributes (not used by all dial charts*/
	public static final String INTERVAL = "INTERVAL";
	public static final String LABEL_INTERVAL = "label";
	public static final String MIN_INTERVAL = "min";
	public static final String MAX_INTERVAL = "max";
	public static final String COLOR_INTERVAL = "color";
	public static final String INTERVALS_NUMBER = "intervals_number";


	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap dataset){
		return null;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#configureChart(it.eng.spago.base.SourceBean)
	 */
	public void configureChart(SourceBean content){
		logger.debug("IN");

		super.configureChart(content);

		try{
			// check if there is some info about additional labels style

			SourceBean styleTickLabelsSB = (SourceBean)content.getAttribute(STYLE_TICK_LABELS);
			if(styleTickLabelsSB!=null){

				String fontS = (String)styleTickLabelsSB.getAttribute(FONT_STYLE);
				if(fontS==null){
					fontS = defaultLabelsStyle.getFontName();
				}
				String sizeS = (String)styleTickLabelsSB.getAttribute(SIZE_STYLE);
				String colorS = (String)styleTickLabelsSB.getAttribute(COLOR_STYLE);

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
					
					labelsTickStyle=new StyleLabel(fontS,size,color);

				}
				catch (Exception e) {
					logger.error("Wrong style labels settings, use default");
				}

			}else{
				labelsTickStyle = defaultLabelsStyle;
			}
			
			SourceBean styleValueLabelsSB = (SourceBean)content.getAttribute(STYLE_VALUE_LABEL);
			if(styleValueLabelsSB!=null){

				String fontS = (String)styleValueLabelsSB.getAttribute(FONT_STYLE);
				if(fontS==null){
					fontS = defaultLabelsStyle.getFontName();
				}
				String sizeS = (String)styleValueLabelsSB.getAttribute(SIZE_STYLE);
				String colorS = (String)styleValueLabelsSB.getAttribute(COLOR_STYLE);
				
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
					labelsValueStyle=new StyleLabel(fontS,size,color);

				}
				catch (Exception e) {
					logger.error("Wrong style labels settings, use default");
				}

			}else{
				labelsValueStyle = defaultLabelsStyle;
			}

			if(isLovConfDefined==false){  // the configuration parameters are set in template
				logger.debug("Configuration in template");
				confParameters = new HashMap();
				SourceBean confSB = (SourceBean)content.getAttribute("CONF");

				List confAttrsList = confSB.getAttributeAsList("PARAMETER");

				Iterator confAttrsIter = confAttrsList.iterator();
				while(confAttrsIter.hasNext()) {
					SourceBean param = (SourceBean)confAttrsIter.next();
					String nameParam = (String)param.getAttribute("name");
					String valueParam = (String)param.getAttribute("value");
					confParameters.put(nameParam, valueParam);
				}	
				if(confParameters.get(LOWER)!=null){	
					String lower=(String)confParameters.get(LOWER);
					setLower(Double.valueOf(lower).doubleValue());
				}
				else {
					logger.error("lower bound not defined");
					throw new Exception("lower bound not defined");
				}
				if(confParameters.get(UPPER)!=null){	
					String upper=(String)confParameters.get(UPPER);
					setUpper(Double.valueOf(upper).doubleValue());
				}
				else {
					logger.error("upper bound not defined");
					throw new Exception("upper bound not defined");
				}
				
				if(confParameters.get(UNITS)!=null){	
					String units=(String)confParameters.get(UNITS);
					setUnits(units);
				}
				
				multichart=false;
				if(confParameters.get("multichart")!=null && !(((String)confParameters.get(MULTICHART)).equalsIgnoreCase("") )){	
					String multiple=(String)confParameters.get(MULTICHART);
					if(multiple.equalsIgnoreCase("true"))
						setMultichart(true);
				}
				
				orientationMultichart="horizontal";
				if(confParameters.get(ORIENTATION_MULTICHART)!=null && !(((String)confParameters.get(ORIENTATION_MULTICHART)).equalsIgnoreCase("") )){	
					String ori=(String)confParameters.get(ORIENTATION_MULTICHART);
					if(ori.equalsIgnoreCase("horizontal") || ori.equalsIgnoreCase("vertical") )
						setOrientationMultichart(ori);
				}

			}
			else{ // configuration parameters are set in a LOV
				logger.debug("configuration parameters set in LOV");
				//String parameters=LovAccessFunctions.getLovResult(profile, confLov);

				
				
				String parameters=DataSetAccessFunctions.getDataSetResultFromLabel(profile, confDataset, parametersObject);
				
				
				SourceBean sourceBeanResult=null;
				try {
					sourceBeanResult = SourceBean.fromXMLString(parameters);
				} catch (SourceBeanException e) {
					logger.error("error in reading configuration lov");
					throw new Exception("error in reading configuration lov");
				}

				sbRow=(SourceBean)sourceBeanResult.getAttribute("ROW");
				String lower=(String)sbRow.getAttribute(LOWER);
				String upper=(String)sbRow.getAttribute(UPPER);
				String legend=(String)sbRow.getAttribute(LEGEND);
				String multichart=(String)sbRow.getAttribute(MULTICHART);
				String orientation=(String)sbRow.getAttribute(ORIENTATION_MULTICHART);

				if(lower==null || upper==null){
					logger.error("error in reading configuration lov");
					throw new Exception("error in reading configuration lov");
				}

				setLower(Double.valueOf(lower).doubleValue());
				setUpper(Double.valueOf(upper).doubleValue());
				setMultichart((multichart.equalsIgnoreCase("true")?true:false));
				setLegend(legend.equalsIgnoreCase("true")?true:false);
				setOrientationMultichart(orientation);
			}
			
			
		}catch (Exception e) {
			logger.error("error in reading template configurations");
		}

		logger.debug("OUT");
	}	




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#calculateValue()
	 */
	public DatasetMap calculateValue() throws Exception{
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		if (res!=null){
			logger.debug("Dataset result:"+res);
			SourceBean sbRows=SourceBean.fromXMLString(res);
			SourceBean sbRow=(SourceBean)sbRows.getAttribute("ROW");
			String result="";
			if(sbRow==null){
				result=(new Double(lower)).toString();
			}
			else{
				List atts=sbRow.getContainedAttributes();
				if (name.indexOf("$F{") >= 0){
					logger.debug("name: " + name);
					setTitleParameter(atts);
				}
				if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				result=(String)sbRow.getAttribute("value");
			}
			DefaultValueDataset dataset = new DefaultValueDataset(Double.valueOf(result));
			logger.debug("OUT");

			DatasetMap datasets=new DatasetMap();
			datasets.addDataset("1",dataset);
			return datasets;		
		}
		logger.error("dataset is null!!!!!!!!!");
		return null;
	}




	/**
	 * Gets the lower.
	 * 
	 * @return the lower
	 */
	public double getLower() {
		return lower;
	}

	/**
	 * Sets the lower.
	 * 
	 * @param lower the new lower
	 */
	public void setLower(double lower) {
		this.lower = lower;
	}

	/**
	 * Gets the upper.
	 * 
	 * @return the upper
	 */
	public double getUpper() {
		return upper;
	}

	/**
	 * Sets the upper.
	 * 
	 * @param upper the new upper
	 */
	public void setUpper(double upper) {
		this.upper = upper;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isLovConfDefined()
	 */
	public boolean isLovConfDefined() {
		return isLovConfDefined;
	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#setLovConfDefined(boolean)
	 */
	public void setLovConfDefined(boolean isLovConfDefined) {
		this.isLovConfDefined = isLovConfDefined;
	}




	/**
	 * Gets the conf parameters.
	 * 
	 * @return the conf parameters
	 */
	public Map getConfParameters() {
		return confParameters;
	}




	/**
	 * Sets the conf parameters.
	 * 
	 * @param confParameters the new conf parameters
	 */
	public void setConfParameters(Map confParameters) {
		this.confParameters = confParameters;
	}




	/**
	 * Gets the sb row.
	 * 
	 * @return the sb row
	 */
	public SourceBean getSbRow() {
		return sbRow;
	}




	/**
	 * Sets the sb row.
	 * 
	 * @param sbRow the new sb row
	 */
	public void setSbRow(SourceBean sbRow) {
		this.sbRow = sbRow;
	}


	public String getUnits() {
		return units;
	}


	public void setUnits(String units) {
		this.units = units;
	}

	
}
