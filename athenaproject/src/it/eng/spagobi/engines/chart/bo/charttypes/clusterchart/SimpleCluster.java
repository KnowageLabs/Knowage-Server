/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.clusterchart;

import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYZDataset;

public class SimpleCluster extends ClusterCharts {
	
	

	public JFreeChart createChart(DatasetMap datasets) {

		DefaultXYZDataset dataset=(DefaultXYZDataset)datasets.getDatasets().get("1");

		JFreeChart chart = ChartFactory.createBubbleChart(
				name, yLabel, xLabel, dataset, 
				PlotOrientation.HORIZONTAL, legend, true, false);

		/*Font font = new Font("Tahoma", Font.BOLD, titleDimension);
		TextTitle title = new TextTitle(name, font);
		chart.setTitle(title);*/

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		Color colorSubInvisibleTitle=Color.decode("#FFFFFF");
		StyleLabel styleSubSubTitle=new StyleLabel("Arial",12,colorSubInvisibleTitle);
		TextTitle subsubTitle =setStyleTitle("", styleSubSubTitle);
		chart.addSubtitle(subsubTitle);
		
		chart.setBackgroundPaint(color);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		//plot.setForegroundAlpha(0.50f);
		plot.setForegroundAlpha(0.65f);


		XYItemRenderer renderer = plot.getRenderer();

		//define colors
		int seriesN=dataset.getSeriesCount();
		if(colorMap!=null){
			boolean isSerieSel = true;
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getSeriesKey(i);
				String tmpName = serieName.replaceAll(" ", "");
				tmpName = tmpName.replace('.',' ').trim();
				if (serie_selected != null && serie_selected.size()>0){
					String serieSel = serie_selected.get(tmpName).toString();
					isSerieSel = (serieSel.equalsIgnoreCase("TRUE") || serieSel.equalsIgnoreCase("YES") ||
										  serieSel.equalsIgnoreCase("1"))? true : false;
					serieName = tmpName;
				}
				
				if(color!=null && isSerieSel){
					Color color=(Color)colorMap.get(serieName);
					renderer.setSeriesPaint(i, color);
				}
				else{
					Color color = new Color(Integer.decode(defaultColor).intValue());
					renderer.setSeriesPaint(i, color);
				}
			}
		}


		// increase the margins to account for the fact that the auto-range 
		// doesn't take into account the bubble size...
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		//domainAxis.setAutoRange(true);
		domainAxis.setRange(yMin, yMax);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		
		//rangeAxis.setAutoRange(true);
		rangeAxis.setRange(xMin,xMax);

		TickUnits units=null;
		if(decimalXValues==false)
			units=(TickUnits)NumberAxis.createIntegerTickUnits();
		else
			units=(TickUnits)NumberAxis.createStandardTickUnits();
		rangeAxis.setStandardTickUnits(units);

		TickUnits domainUnits=null;
		if(decimalYValues==false)
			domainUnits=(TickUnits)NumberAxis.createIntegerTickUnits();
		else
			domainUnits=(TickUnits)NumberAxis.createStandardTickUnits();
		domainAxis.setStandardTickUnits(domainUnits);
		
		rangeAxis.setLowerMargin(1.0);
		rangeAxis.setUpperMargin(1.0);
		
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
        domainAxis.setLabelPaint(styleYaxesLabels.getColor());
        domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
        domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
        
		domainAxis.setLowerMargin(1.0);
		domainAxis.setUpperMargin(1.0);
		//DecimalFormat format=(new DecimalFormat("0"));
		//rangeAxis.setNumberFormatOverride(format);

		if(legend==true)	
			drawLegend(chart);

		return chart;
	}

}
