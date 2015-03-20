/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.scattercharts;

import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;


public class SimpleScatter extends ScatterCharts {

	public JFreeChart createChart(DatasetMap datasets) {

		DefaultXYDataset dataset=(DefaultXYDataset)datasets.getDatasets().get("1");

		JFreeChart chart = ChartFactory.createScatterPlot(
				name, yLabel, xLabel, dataset, 
				PlotOrientation.HORIZONTAL, false, true, false);

		Font font = new Font("Tahoma", Font.BOLD, titleDimension);
		//TextTitle title = new TextTitle(name, font);
		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		chart.setBackgroundPaint(Color.white);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setForegroundAlpha(0.65f);

		XYItemRenderer renderer = plot.getRenderer();


		int seriesN=dataset.getSeriesCount();
		if(colorMap!=null){
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getSeriesKey(i);
				Color color=(Color)colorMap.get(serieName);
				if(color!=null){
					renderer.setSeriesPaint(i, color);
				}	
			}
		}

		// increase the margins to account for the fact that the auto-range 
		// doesn't take into account the bubble size...
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRange(true);
		domainAxis.setRange(yMin, yMax);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setRange(xMin,xMax);
		
		if(legend==true){
			drawLegend(chart);
		}
		return chart;
	}


}
