/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.dialcharts;


import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.KpiInterval;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.BasicStroke;
import java.awt.Color;
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
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;

public class BulletGraph  extends DialCharts{
	private static transient Logger logger=Logger.getLogger(BulletGraph.class);
	Vector intervals;
	Double target;
	
	// Parameters

	
	public BulletGraph() {
		super();
		intervals=new Vector();
	}
	
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
				
		String target=(String)confParameters.get("target");
		if (target!=null)this.target = new Double(target);

		SourceBean confSB = (SourceBean)content.getAttribute("INTERVALS");
		if(confSB==null){
			confSB = (SourceBean)content.getAttribute("CONF.INTERVALS");
		}
		List confAttrsList = confSB.getAttributeAsList(INTERVAL);
		if (!confAttrsList.isEmpty()){
			Iterator it = confAttrsList.iterator();
			while(it.hasNext()){
				SourceBean param = (SourceBean)it.next();
				KpiInterval interval=new KpiInterval();
				String min=(String)param.getAttribute(MIN_INTERVAL);
				String max=(String)param.getAttribute(MAX_INTERVAL);
				String col=(String)param.getAttribute(COLOR_INTERVAL);
				interval.setMin(Double.valueOf(min).doubleValue());
				interval.setMax(Double.valueOf(max).doubleValue());
				Color color=new Color(Integer.decode(col).intValue());
				interval.setColor(color);
				this.intervals.add(interval);
			}
		}			
		logger.debug("OUT");
	}
	
	
	public JFreeChart createChart(DatasetMap datasets) {
		
		logger.debug("IN");
		Dataset dataset=(Dataset)datasets.getDatasets().get("1");
		ValueDataset valDataSet = (ValueDataset)dataset;
		
		Number value = valDataSet.getValue();
		
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
        
        chart.setBackgroundPaint(color);
		
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

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
        rangeAxis.setVisible(true); 
        // calculate the upper limit 
        //double upperBound = target * upperFactor; 
        rangeAxis.setRange(new Range(lower, upper)); 
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        // customize renderer 
        BarRenderer renderer = (BarRenderer) plot.getRenderer(); 
        renderer.setMaximumBarWidth(0.18); 
        renderer.setSeriesPaint(0, Color.BLACK); 
        
        return chart;
	}
}
