/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts;

import it.eng.spagobi.engines.kpi.bo.ChartImpl;
import it.eng.spagobi.engines.kpi.utils.KpiInterval;

import java.awt.BasicStroke;
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
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
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

public class SimpleDial extends ChartImpl {
	
	private static transient Logger logger=Logger.getLogger(SimpleDial.class);

	double increment=0.0;
	int minorTickCount=2;

	/**
	 * Instantiates a new simple dial.
	 */
	public SimpleDial() {
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
	 * Creates the chart .
	 * 
	 * @return A Simple Dial chart .
	 */
	public JFreeChart createChart() {
		logger.debug("IN");
		
		if (dataset==null){
			logger.debug("The dataset to be represented is null");
			return null;		
		}
		
		DialPlot plot = new DialPlot();
		plot.setDataset((ValueDataset)dataset);
		logger.debug("Created the new Dial Plot");

		ArcDialFrame dialFrame=null;
		plot.setView(0.21, 0.0, 0.58, 0.30);
		dialFrame = new ArcDialFrame(60.0, 60.0);
		dialFrame.setInnerRadius(0.65);
		dialFrame.setOuterRadius(0.90);
		dialFrame.setForegroundPaint(Color.darkGray);
		dialFrame.setStroke(new BasicStroke(3.0f));
		plot.setDialFrame(dialFrame);

		GradientPaint gp = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(240, 240, 240));
		DialBackground sdb = new DialBackground(gp);

		GradientPaintTransformType gradientPaintTransformType=GradientPaintTransformType.HORIZONTAL;

		sdb.setGradientPaintTransformer(new StandardGradientPaintTransformer(gradientPaintTransformType));
		plot.addLayer(sdb);

		increment = (upper-lower)/4;
		StandardDialScale scale=null;
		scale = new StandardDialScale(lower, upper, 115.0,-50.0, increment, minorTickCount);

		// sets intervals
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval)iterator.next();
			StandardDialRange range = new StandardDialRange(interval.getMin(), interval.getMax(), interval.getColor()); 
			range.setInnerRadius(0.70);
			range.setOuterRadius(0.75);
			plot.addLayer(range);

		}

		scale.setTickRadius(0.88);
		scale.setTickLabelOffset(0.07);
		Font f =new Font("Arial",Font.PLAIN,11);
		scale.setTickLabelFont(f);
		//scale.setMajorTickIncrement(25.0);
		plot.addScale(0, scale);

		DialPointer needle = new DialPointer.Pin();
		needle.setRadius(0.82);
		plot.addLayer(needle);
		
		JFreeChart chart1 = new JFreeChart(plot);
		logger.debug("Created the chart");
		chart1.setBackgroundPaint(color);
		logger.debug("Setted background color of the chart");
		
		TextTitle title = setStyleTitle(name, styleTitle);
		chart1.setTitle(title);
		logger.debug("Setted the title of the chart");
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart1.addSubtitle(subTitle);
			logger.debug("Setted the subtitle of the chart");
		}

		
		logger.debug("OUT");
		return chart1;
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

}
