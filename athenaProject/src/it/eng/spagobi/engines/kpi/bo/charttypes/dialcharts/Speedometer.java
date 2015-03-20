/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts;


import it.eng.spagobi.engines.kpi.bo.ChartImpl;
import it.eng.spagobi.engines.kpi.utils.KpiInterval;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.util.HashMap;
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
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class Speedometer extends ChartImpl {
	
	private static transient Logger logger=Logger.getLogger(Speedometer.class);


	double increment=0.0;//increment between each MajorTickLines
	int minorTickCount=5;//Number of MinorTickLines between every 2 MajorTickLines
	boolean dialtextuse = false ;
	String dialtext = "";//Text to be written into the chart (usually not used)


	/**
	 * Instantiates a new Speedometer.
	 */
	public Speedometer() {
		super();
		intervals=new Vector();	
	}

	public void configureChart(HashMap conf) {
		logger.info("IN");
		super.configureChart(conf);
		logger.debug("OUT");
	}
	
	public void setThresholds(List thresholds) {		
		super.setThresholdValues(thresholds);		
	}


	/**
	 * Creates a chart of type Speedometer.
	 * 
	 * @return A Speedometer Chart
	 */
	public JFreeChart createChart() {
		logger.debug("IN");
	
		if (dataset==null){
			logger.debug("The dataset to be represented is null");
			return null;		
		}
		
		DialPlot plot = new DialPlot();
		logger.debug("Created new DialPlot");
		
		plot.setDataset((ValueDataset)dataset);
		plot.setDialFrame(new StandardDialFrame());
		plot.setBackground(new DialBackground());
		
		
		if(dialtextuse){
			//Usually it shoudn'tbe used. It is a type of title written into the graph
			DialTextAnnotation annotation1 = new DialTextAnnotation(dialtext);			
			annotation1.setFont(styleTitle.getFont());
			annotation1.setRadius(0.7);
			plot.addLayer(annotation1);
		}
		
		DialValueIndicator dvi = new DialValueIndicator(0);
		plot.addLayer(dvi);

		increment = (upper-lower)/10;
		StandardDialScale scale = new StandardDialScale(lower, upper, -120, -300, 10.0, 4);
//		if (!( increment > 0)){
//			increment = 0.01;
//		}
		scale.setMajorTickIncrement(increment);
		logger.debug("Setted the unit after which a new MajorTickline will be drawed");
		scale.setMinorTickCount(minorTickCount);
		logger.debug("Setted the number of MinorTickLines between every MajorTickline");
		scale.setTickRadius(0.88);
		scale.setTickLabelOffset(0.15);
		Font f =new Font("Arial",Font.PLAIN,11);
		scale.setTickLabelFont(f);
		plot.addScale(0, scale);
		plot.addPointer(new DialPointer.Pin());
		
		DialCap cap = new DialCap();
		plot.setCap(cap);

		// sets intervals
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();
			StandardDialRange range = new StandardDialRange(interval.getMin(), interval.getMax(), interval.getColor()); 
			range.setInnerRadius(0.52);
			range.setOuterRadius(0.55);
			plot.addLayer(range);
			logger.debug("new range added to the plot");
		}

		GradientPaint gp = new GradientPaint(new Point(),new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
		DialBackground db = new DialBackground(gp);
		db.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		plot.setBackground(db);
		plot.removePointer(0);
		
		DialPointer.Pointer p = new DialPointer.Pointer();
		//Pointer color
		p.setFillPaint(Color.black);
		plot.addPointer(p);
		logger.debug("Setted all properties of the plot");

		JFreeChart chart=new JFreeChart(name, plot);
		logger.debug("Created the chart");
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		logger.debug("Setted the title of the chart");
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
			logger.debug("Setted the subtitle of the chart");
		}
		
		chart.setBackgroundPaint(color);
		logger.debug("Setted background color of the chart");
		
		logger.debug("OUT");
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
