/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts.BulletGraph;
import it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts.Meter;
import it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts.SimpleDial;
import it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts.Speedometer;
import it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts.Thermometer;
import it.eng.spagobi.engines.kpi.bo.charttypes.trendcharts.LineChart;
import it.eng.spagobi.engines.kpi.utils.KpiInterval;
import it.eng.spagobi.engines.kpi.utils.StyleLabel;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class ChartImpl implements Serializable{
	
	private static transient Logger logger=Logger.getLogger(ChartImpl.class);
	
	protected String name=null;//Chart Title
	protected String subName=null;//Chart Sub Title
	protected StyleLabel styleTitle;//Chart Title style
	protected StyleLabel styleSubTitle;//Chart Sub Title style
	protected Integer width;//Chart Width
	protected Integer height;//Chart height
	protected IEngUserProfile profile;
	protected Color color;//background color of the chart
	protected Boolean legend = true;//true if legend visible; false if not	
	protected Boolean show_axis = false;
	
	protected DefaultValueDataset dataset ;//ValueDataset for the chart
	
	protected Vector intervals ;//List of chart intervals	
	protected double lower=0.0;//Chart's lower bound
	protected double upper=0.0;//Chart's higher bound
	protected Double target=null;//Chart's target to reach

	/**
	 * This function creates the chart object.
	 * 
	 * @return the JFreeChart
	 */
	public JFreeChart createChart() {
		return null;
	}
	
	/**
	 * This function calculates the values for the LineChart by transforming the xml string result in a DatasetMap
	 * 
	 * @param Xml String result in form ROWS.ROW
	 * @return DatasetMap
	 */
	public DatasetMap calculateValue(String result) throws Exception {
		return null;
	}

	/**
	 * This function creates the object of the right subtype 
	 * 
	 * @param subtype: the subtype of the Dial Chart
	 * 
	 * @return the correct ChartImpl instanciated
	 */
	public static ChartImpl createChart(String subtype){
		logger.debug("IN");
		ChartImpl sbi=null;
			if(subtype.equalsIgnoreCase("Speedometer")){
				sbi=new Speedometer();
				logger.debug("Speedometer chart instanciated");
			}
			else if(subtype.equalsIgnoreCase("SimpleDial")){
				sbi= new SimpleDial();
				logger.debug("SimpleDial chart instanciated");
			}
			else if(subtype.equalsIgnoreCase("Thermometer")){
				sbi= new Thermometer();
				logger.debug("Thermometer chart instanciated");
			}
			else if(subtype.equalsIgnoreCase("Meter")){
				sbi= new Meter();
				logger.debug("Meter chart instanciated");
			}
			else if(subtype.equalsIgnoreCase("BulletGraph")){
				sbi= new BulletGraph();
				logger.debug("Meter chart instanciated");
			}
			else if(subtype.equalsIgnoreCase("LineChart")){
				sbi= new LineChart();
				logger.debug("Line chart instanciated");
			}			
			logger.debug("OUT");	
		return sbi;
	}

	/**
	 * This function configures the chart with the parameters passed in the config HashMap 
	 * 
	 * @param subtype: the subtype of the Dial Chart
	 * 
	 * @return the correct ChartImpl instanciated
	 */
	public void configureChart(HashMap config) {
		logger.debug("IN");
		name = (String) config.get("name");
			logger.debug("Chart title setted: "+((name!=null)?name:""));
		subName =(String) config.get("subName");
			logger.debug("Chart subtitle setted: "+((subName!=null)?subName:""));
		styleTitle = (StyleLabel)config.get("styleTitle");
			logger.debug("Chart style title setted");
		styleSubTitle =(StyleLabel) config.get("styleSubTitle");
			logger.debug("Chart style subtitle setted");
		show_axis =(Boolean) config.get("show_axis");
			logger.debug("Chart show_axis setted");
		logger.debug("OUT");	
	}
	
	/**
	 * Sets the Double value to represent into the Chart
	 * 
	 * @param Double value to set
	 * 
	 */
	public void setValueDataSet(Double valueToRepresent){
		this.dataset = new DefaultValueDataset(valueToRepresent);
	}
	
	/**
	 * Sets the Double value to represent the target into the Chart
	 * 
	 * @param Double value of the target to set
	 * 
	 */	
	public void setTarget(Double target){
		this.target = target;
	}
	
	/**
	 * This function returns the Double value represented in the chart
	 * 
	 * @return the Double value represented in the chart
	 */
	public DefaultValueDataset getValueDataSet(){
		return this.dataset ;
	}

	/**
	 * This function fills up the vector "intervals" with the intervals of the chart, getting them from a list of Thresholds 
	 * 
	 * @param List of thresholds to set
	 */
	public void setThresholdValues(List thresholdValues) {
		logger.debug("IN");
		if(thresholdValues!=null && !thresholdValues.isEmpty()){
			Iterator it = thresholdValues.iterator();

			while(it.hasNext()){
				ThresholdValue t = (ThresholdValue)it.next();
				String type = t.getThresholdType();
				Double min = t.getMinValue();
				Double max = t.getMaxValue();
				String label = t.getLabel();
				Color c = t.getColor();
				
				if (type.equals("RANGE")){
					if (min!= null && min.doubleValue()<lower){
						lower = min.doubleValue();
					}else if(min==null && max!=null)	{
						if(max.doubleValue()==0){
							lower = -10;
						}else if(max.doubleValue()>0){
							lower = 0;
						}else if(max.doubleValue()<0){
							lower = max.doubleValue()*2;
						}
					}
					
					if (max!=null && max.doubleValue()>upper){
						upper = max.doubleValue();
					}else if(max==null && min!=null)	{
						if(min.doubleValue()==0){
							upper = 10;
						}else if(min.doubleValue()>0){
							upper = min.doubleValue()*2;
						}else if(min.doubleValue()<0){
							upper = 0;
						}					
					}
				
					KpiInterval interval = new KpiInterval();
					
					if(c!=null)	{
						interval.setColor(c);
					}else{
						interval.setColor(Color.WHITE);
					}
					if(label!=null)	{
						interval.setLabel(label);
					}else{
						interval.setLabel("");
					}
					
					if(max!=null)	{
						interval.setMax(max);
					}else{						
						interval.setMax(upper);
					}
					
					if(min!=null)	{
						interval.setMin(min);
					}else{
						interval.setMin(lower);
					}
					String color = Integer.toHexString( interval.getColor().getRGB() & 0x00ffffff ) ;
					intervals.add(interval);
				}else if (type.equals("MINIMUM")){
					
					if (min.doubleValue()<lower){
						lower = min.doubleValue()*2;
					}	
					if(min.doubleValue()>0){
						upper = min.doubleValue()*2;
					}else if(min.doubleValue()==0){
						upper = 10;
						lower = -10;
					}
					
					KpiInterval interval1 = new KpiInterval();
					
					if(c!=null)	{
						interval1.setColor(c);
					}else{
						interval1.setColor(Color.WHITE);
					}
					if(label!=null)	{
						interval1.setLabel(label);
					}else{
						interval1.setLabel("");
					}
						interval1.setMax(min);
						interval1.setMin(lower);
					String color1 = Integer.toHexString( interval1.getColor().getRGB() & 0x00ffffff ) ;
					intervals.add(interval1);
					KpiInterval interval2 = new KpiInterval();
						interval2.setColor(Color.WHITE);
						interval2.setLabel("");
						interval2.setMax(upper);
						interval2.setMin(min);
					String color2 = Integer.toHexString( interval2.getColor().getRGB() & 0x00ffffff ) ;
					intervals.add(interval2);
					
				}else if (type.equals("MAXIMUM")){
					
					if (max.doubleValue()>upper){
						upper = max.doubleValue()*2;
					}	
					if (max.doubleValue()<0){
						lower = max.doubleValue()*2;
					}else if(max.doubleValue()==0){
						lower = -10;
						upper = 10;
					}
					
					KpiInterval interval1 = new KpiInterval();
						interval1.setColor(Color.WHITE);
					
						interval1.setLabel("");
						interval1.setMax(max);
						interval1.setMin(lower);	
					String color1 = Integer.toHexString( interval1.getColor().getRGB() & 0x00ffffff ) ;	
					intervals.add(interval1);
					KpiInterval interval2 = new KpiInterval();
					if(c!=null)	{
						interval2.setColor(c);
					}else{
						interval2.setColor(Color.WHITE);
					}
					if(label!=null)	{
						interval2.setLabel(label);
					}else{
						interval2.setLabel("");
					}
						interval2.setMax(upper);
						interval2.setMin(max);	
					String color2 = Integer.toHexString( interval2.getColor().getRGB() & 0x00ffffff ) ;	
					intervals.add(interval2);				
				}				
				logger.debug("New interval added to the Vector");
			}
		}
		logger.debug("OUT");
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getWidth()
	 */
	public int getWidth() {
		return width;

	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setHeight(int)
	 */
	public void setHeight(int _height) {
		height=_height;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setName(java.lang.String)
	 */
	public void setName(String _name) {
		name=_name;		
	}
	
	public void setSubName(String _name) {
		subName=_name;		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setWidth(int)
	 */
	public void setWidth(int _width) {
		width=_width;
	}

	/**
	 * Gets the profile.
	 * 
	 * @return the profile
	 */
	public IEngUserProfile getProfile() {
		return profile;
	}

	/**
	 * Sets the profile.
	 * 
	 * @param profile the new profile
	 */
	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}


	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color.
	 * 
	 * @param color the new color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#isLegend()
	 */
	public boolean isLegend() {
		return legend;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setLegend(boolean)
	 */
	public void setLegend(boolean legend) {
		this.legend = legend;
	}
	
	public void setShowAxis(boolean show_axis) {
		this.show_axis=show_axis;
	}



	public TextTitle setStyleTitle(String title,StyleLabel titleLabel){
		Font font=null;
		Color color=null;


		boolean definedFont=true;
		boolean definedColor=true;

		if(titleLabel!=null ){
			if(titleLabel.getFont()!=null){
				font=titleLabel.getFont();
			}
			else{
				definedFont=false;
			}
			if(titleLabel.getColor()!=null){
				color=titleLabel.getColor();
			}
			else{
				definedColor=false;
			}
		}
		else{
			definedColor=false;
			definedFont=false;
		}

		if(!definedFont)
			font=new Font("Arial", Font.BOLD, 18);
		if(!definedColor)
			color=Color.BLACK;

		TextTitle titleText=new TextTitle(title,font,color, RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);

		return titleText;
	}

}
