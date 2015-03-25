/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.dialcharts;


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.KpiInterval;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;


/**
 * A class to generate Speedometer charts
 * @author Giulio Gavardi
 * 
 */
public class SBISpeedometer extends DialCharts{


	private static transient Logger logger=Logger.getLogger(SBISpeedometer.class);

	Vector intervals;
	double increment=0.0;
	int minorTickCount=0;
	boolean dialtextuse = false ;
	String dialtext = "";


	/**
	 * Instantiates a new sBI speedometer.
	 */
	public SBISpeedometer() {
		super();
		intervals=new Vector();	
	}

	/**
	 * set parameters for the creation of the chart getting them from template or from LOV.
	 * 
	 * @param content the content of the template.
	 * 
	 * @return A chart that displays a value as a dial.
	 */


	public void configureChart(SourceBean content) {

		super.configureChart(content);

		logger.debug("IN");

		if(!isLovConfDefined){
			logger.debug("Configuration set in template");
			if(confParameters.get("increment")!=null){	
				String increment=(String)confParameters.get("increment");
				setIncrement(Double.valueOf(increment).doubleValue());
			}
			else {
				logger.error("increment not defined");
				return;
			}
			if(confParameters.get("minor_tick")!=null){	
				String minorTickCount=(String)confParameters.get("minor_tick");
				setMinorTickCount(Integer.valueOf(minorTickCount).intValue());
			}
			else {
				setMinorTickCount(10);
			}

			if(confParameters.get("dialtextuse")!=null){	
				String dialtextusetemp=(String)confParameters.get("dialtextuse");
				if(dialtextusetemp.equalsIgnoreCase("true")){
					dialtextuse = true;
				}
				else dialtextuse = false;
			}

			if(dialtextuse && confParameters.get("dialtext")!=null){
				dialtext=(String)confParameters.get("dialtext");
			}


			//reading intervals information
			SourceBean intervalsSB = (SourceBean)content.getAttribute("INTERVALS");
			if(intervalsSB==null){
				intervalsSB = (SourceBean)content.getAttribute("CONF.INTERVALS");
			}
			List intervalsAttrsList=null;
			if(intervalsSB!=null){
				intervalsAttrsList = intervalsSB.getContainedSourceBeanAttributes();
			}

			if(intervalsAttrsList==null || intervalsAttrsList.isEmpty()){ // if intervals are not defined realize a single interval
				logger.warn("intervals not defined; default settings");
				KpiInterval interval=new KpiInterval();
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.WHITE);
				addInterval(interval);
			}
			else{	

				Iterator intervalsAttrsIter = intervalsAttrsList.iterator();
				while(intervalsAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)intervalsAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();
					String min= (String)param.getAttribute("min");
					String max= (String)param.getAttribute("max");
					String col= (String)param.getAttribute("color");

					KpiInterval interval=new KpiInterval();
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());

					Color color=new Color(Integer.decode(col).intValue());
					if(color!=null){
						interval.setColor(color);}
					else{
						// sets default color
						interval.setColor(Color.WHITE);
					}
					addInterval(interval);
				}
			}
		}
		else{
			logger.debug("configuration defined in LOV"+confDataset);
			String increment=(String)sbRow.getAttribute("increment");
			String minorTickCount=(String)sbRow.getAttribute("minor_tick");
			setIncrement(Double.valueOf(increment).doubleValue());
			setMinorTickCount(Integer.valueOf(minorTickCount).intValue());


			String intervalsNumber=(String)sbRow.getAttribute("intervals_number");
			if(intervalsNumber==null || intervalsNumber.equals("") || intervalsNumber.equals("0")){ // if intervals are not specified
				KpiInterval interval=new KpiInterval();
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.WHITE);
				addInterval(interval);
			}
			else{
				for(int i=1;i<=Integer.valueOf(intervalsNumber).intValue();i++){
					KpiInterval interval=new KpiInterval();
					String min=(String)sbRow.getAttribute("min"+(new Integer(i)).toString());
					String max=(String)sbRow.getAttribute("max"+(new Integer(i)).toString());
					String col=(String)sbRow.getAttribute("color"+(new Integer(i)).toString());
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());
					Color color=new Color(Integer.decode(col).intValue());
					interval.setColor(color);
					addInterval(interval);

				}
			}
		}
		logger.debug("out");
	}





	/**
	 * Creates a chart of type speedometer.
	 * 
	 * @param chartTitle  the chart title.
	 * @param dataset  the dataset.
	 * 
	 * @return A chart speedometer.
	 */

	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		Dataset dataset=(Dataset)datasets.getDatasets().get("1");

		DialPlot plot = new DialPlot();
		plot.setDataset((ValueDataset)dataset);
		plot.setDialFrame(new StandardDialFrame());

		plot.setBackground(new DialBackground());
		if(dialtextuse){
			DialTextAnnotation annotation1 = new DialTextAnnotation(dialtext);			
			annotation1.setFont(styleTitle.getFont());
			annotation1.setRadius(0.7);

			plot.addLayer(annotation1);
		}

		DialValueIndicator dvi = new DialValueIndicator(0);
		dvi.setFont(new Font(labelsValueStyle.getFontName(), Font.PLAIN, labelsValueStyle.getSize()));
		dvi.setPaint(labelsValueStyle.getColor());
		plot.addLayer(dvi);

		StandardDialScale scale = new StandardDialScale(lower, 
				upper, -120, -300, 10.0, 4);

		if (!( increment > 0)){
			logger.warn("increment cannot be less than 0, put default to 0.1");
			increment = 0.01;
		}

		scale.setMajorTickIncrement(increment);

//		if (!( minorTickCount > 0)){
//			logger.warn("minor tick count cannot be less than 0, put default to 1");
//			minorTickCount = 1;
//		}

		scale.setMinorTickCount(minorTickCount);
		scale.setTickRadius(0.88);
		scale.setTickLabelOffset(0.15);
		//set tick label style
		Font tickLabelsFont = new Font(labelsTickStyle.getFontName(), Font.PLAIN, labelsTickStyle.getSize());
		scale.setTickLabelFont(tickLabelsFont);
		scale.setTickLabelPaint(labelsTickStyle.getColor());
		plot.addScale(0, scale);

		plot.addPointer(new DialPointer.Pin());

		DialCap cap = new DialCap();
		plot.setCap(cap);

		// sets intervals
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();
			StandardDialRange range = new StandardDialRange(interval.getMin(), interval.getMax(), 
					interval.getColor()); 
			range.setInnerRadius(0.52);
			range.setOuterRadius(0.55);
			plot.addLayer(range);

		}

		GradientPaint gp = new GradientPaint(new Point(), 
				new Color(255, 255, 255), new Point(), 
				new Color(170, 170, 220));
		DialBackground db = new DialBackground(gp);
		db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
				GradientPaintTransformType.VERTICAL));
		plot.setBackground(db);

		plot.removePointer(0);
		DialPointer.Pointer p = new DialPointer.Pointer();
		p.setFillPaint(Color.yellow);
		plot.addPointer(p);

		logger.debug("OUT");
		JFreeChart chart=new JFreeChart(name, plot);

		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		chart.setBackgroundPaint(color);
		return chart;
	}



	/**
	 * Gets the intervals.
	 * 
	 * @return the intervals
	 */
	public Vector getIntervals() {
		return intervals;
	}





	/**
	 * Adds the interval.
	 * 
	 * @param interval the interval
	 */
	public void addInterval(KpiInterval interval) {
		this.intervals.add(interval);
	}


	/**
	 * Gets the increment.
	 * 
	 * @return the increment
	 */
	public double getIncrement() {
		return increment;
	}



	/**
	 * Sets the increment.
	 * 
	 * @param increment the new increment
	 */
	public void setIncrement(double increment) {
		this.increment = increment;
	}



	/**
	 * Gets the minor tick count.
	 * 
	 * @return the minor tick count
	 */
	public int getMinorTickCount() {
		return minorTickCount;
	}



	/**
	 * Sets the minor tick count.
	 * 
	 * @param minorTickCount the new minor tick count
	 */
	public void setMinorTickCount(int minorTickCount) {
		this.minorTickCount = minorTickCount;
	}








}
