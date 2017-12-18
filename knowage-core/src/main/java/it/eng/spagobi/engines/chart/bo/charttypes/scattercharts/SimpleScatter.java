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
