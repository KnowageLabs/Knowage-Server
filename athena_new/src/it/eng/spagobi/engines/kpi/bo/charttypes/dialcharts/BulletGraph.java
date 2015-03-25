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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;

public class BulletGraph  extends ChartImpl{
	private static transient Logger logger=Logger.getLogger(BulletGraph.class);
	
	public BulletGraph() {
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
	
	
	public JFreeChart createChart(){
		
		logger.debug("IN");
		Number value = null;
		
		if (dataset==null){
			logger.debug("The dataset to be represented is null");
			value = new Double(0);	
		}else{
			value = dataset.getValue();
		}
		
		DefaultCategoryDataset datasetC = new DefaultCategoryDataset(); 
	        datasetC.addValue(value, "", ""); 
		
		 // customize a bar chart 
        JFreeChart chart = ChartFactory.createBarChart( 
                null, 
                null, 
                null, 
                datasetC, 
                PlotOrientation.HORIZONTAL, 
                false, 
                false, 
                false); 
        chart.setBorderVisible(false); 

        CategoryPlot plot = chart.getCategoryPlot(); 
        plot.setOutlineVisible(true); 
        plot.setOutlinePaint(Color.BLACK);
       
        plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0)); 
        plot.setBackgroundPaint(null); 
        plot.setDomainGridlinesVisible(false); 
        plot.setRangeGridlinesVisible(false); 
        plot.setRangeCrosshairVisible(false); 
        plot.setAnchorValue(value.doubleValue());
        
        
     // add the target marker 
        if(target != null) {
	        ValueMarker marker = new ValueMarker( target.doubleValue(), Color.BLACK, new BasicStroke(2.0f)); 	        
	        plot.addRangeMarker(marker, Layer.FOREGROUND); 
        }
        
        
        //sets different marks
        for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();
			// add the marks 
            IntervalMarker marker = new IntervalMarker(interval.getMin(), interval.getMax(), interval.getColor()); 
            plot.addRangeMarker(marker, Layer.BACKGROUND);
			logger.debug("Added new interval to the plot");
		}
        
        // customize axes 
        CategoryAxis domainAxis = plot.getDomainAxis(); 
        domainAxis.setVisible(false); 

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); 
        rangeAxis.setVisible(show_axis); 
        rangeAxis.setLabelFont(new Font("Arial",Font.PLAIN,4));
        // calculate the upper limit 
        //double upperBound = target * upperFactor; 
        rangeAxis.setRange(new Range(lower, upper)); 
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        // customize renderer 
        BarRenderer renderer = (BarRenderer) plot.getRenderer(); 
        renderer.setMaximumBarWidth(0.18); 
        renderer.setSeriesPaint(0, Color.BLACK); 
        /*BasicStroke d = new BasicStroke(3f,BasicStroke.CAP_ROUND ,BasicStroke.JOIN_ROUND);
        renderer.setSeriesOutlineStroke(0, d);
        renderer.setSeriesStroke(0, d);
       
        renderer.setStroke(d);*/
        
        return chart;
	}
}
