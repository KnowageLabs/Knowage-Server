/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.kpi.bo.charttypes.dialcharts;


import it.eng.spagobi.engines.kpi.bo.ChartImpl;
import it.eng.spagobi.engines.kpi.utils.KpiInterval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.ValueDataset;

/**
 * 
 * @author Chiara Chiarelli
 * 
 */

public class Meter extends ChartImpl {
	
	private static transient Logger logger=Logger.getLogger(Meter.class);

	/**
	 * Instantiates a new meter.
	 */
	public Meter() {
		super();
		intervals=new Vector();	
	}


	public void configureChart(HashMap conf) {
		logger.debug("IN");
		super.configureChart(conf);
		logger.debug("OUT");
	}
	
	public void setThresholds(List thresholds) {		
		super.setThresholdValues(thresholds);		
	}
	

	/**
	 * Creates the MeterChart .
	 * 
	 * @return A MeterChart .
	 */
	public JFreeChart createChart() {
		logger.debug("IN");
		if (dataset==null){
			logger.debug("The dataset to be represented is null");
			return null;		
		}
		MeterPlot plot = new MeterPlot((ValueDataset)dataset);
		logger.debug("Created new plot");
		plot.setRange(new Range(lower, upper));
		logger.debug("Setted plot range");


		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();
			plot.addInterval(new MeterInterval(interval.getLabel(), new Range(interval.getMin(), interval.getMax()), 
					Color.lightGray, new BasicStroke(2.0f), interval.getColor()));
			logger.debug("Added new interval to the plot");
		}

		plot.setNeedlePaint(Color.darkGray);
		plot.setDialBackgroundPaint(Color.white);
		plot.setDialOutlinePaint(Color.gray);
		plot.setDialShape(DialShape.CHORD);
		plot.setMeterAngle(260);
		plot.setTickLabelsVisible(true);
		Font f =new Font("Arial",Font.PLAIN,11);
		plot.setTickLabelFont(f);
		plot.setTickLabelPaint(Color.darkGray);
		plot.setTickSize(5.0);
		plot.setTickPaint(Color.lightGray);		
		plot.setValuePaint(Color.black);
		plot.setValueFont(new Font("Arial", Font.PLAIN, 14));
		logger.debug("Setted all properties of the plot");

		JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		logger.debug("Created the chart");
		chart.setBackgroundPaint(color);
		logger.debug("Setted background color of the chart");
		
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		logger.debug("Setted the title of the chart");
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
			logger.debug("Setted the subtitle of the chart");
		}
		logger.debug("OUT");
		return chart;
	}
}
