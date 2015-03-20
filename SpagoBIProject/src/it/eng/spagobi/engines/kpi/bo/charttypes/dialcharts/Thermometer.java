/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts;

import it.eng.spagobi.engines.kpi.bo.ChartImpl;
import it.eng.spagobi.engines.kpi.utils.KpiInterval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;

/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class Thermometer extends ChartImpl {
	
	private static transient Logger logger=Logger.getLogger(Thermometer.class);


	/**
	 * Instantiates a new thermometer.
	 */
	public Thermometer() {
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
	 * Creates a chart of type thermometer.
	 * 
	 * @return A chart thermometer.
	 */
	public JFreeChart createChart() {
		logger.debug("IN");

		if (dataset==null){
			logger.debug("The dataset to be represented is null");
			return null;		
		}
		
		ThermometerPlot plot = new ThermometerPlot((ValueDataset)dataset);
		logger.debug("Created the new Thermometer Plot");
		JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT,	plot, true);  
		logger.debug("Created the new Chart");
		chart.setBackgroundPaint(color);
		logger.debug("Setted the background color of the chart");
	
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		logger.debug("Setted the title of the chart");
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
			logger.debug("Setted the subtitle of the chart");
		}
		
		plot.setInsets(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setPadding(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
		plot.setThermometerStroke(new BasicStroke(2.0f));
		plot.setThermometerPaint(Color.lightGray);
		plot.setGap(3);
		plot.setValueLocation(3);
		plot.setRange(lower, upper);
		plot.setUnits(ThermometerPlot.UNITS_NONE);
		logger.debug("Setted all the properties of the plot");

		// set subranges	
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();){
			KpiInterval subrange = (KpiInterval) iterator.next();
			int range=0;
			//For the thermometer the number of intervals is forced to 3 and they have to have as labels the following ones
			if(subrange.getLabel().equalsIgnoreCase("NORMAL"))range=(ThermometerPlot.NORMAL);
			else if(subrange.getLabel().equalsIgnoreCase("WARNING"))range=(ThermometerPlot.WARNING);
			else if(subrange.getLabel().equalsIgnoreCase("CRITICAL"))range=(ThermometerPlot.CRITICAL);

			plot.setSubrange(range, subrange.getMin(), subrange.getMax());
			if(subrange.getColor()!=null){
				plot.setSubrangePaint(range, subrange.getColor());
			}
			logger.debug("Setted new range of the plot");
		}
	
		logger.debug("OUT");
		return chart;       
	}

}
