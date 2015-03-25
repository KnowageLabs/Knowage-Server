/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.boxcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

public class SimpleBox extends BoxCharts {

	public DatasetMap calculateValue() throws Exception {
		// TODO Auto-generated method stub
		return super.calculateValue();
	}

	public void configureChart(SourceBean content) {
		// TODO Auto-generated method stub
		super.configureChart(content);
	}



	public JFreeChart createChart(DatasetMap datasetMap) {

		BoxAndWhiskerCategoryDataset dataset=(BoxAndWhiskerCategoryDataset)datasetMap.getDatasets().get("1"); 

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				name, categoryLabel, valueLabel, dataset, 
				false);

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
		
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		BoxAndWhiskerRenderer renderer=(BoxAndWhiskerRenderer)plot.getRenderer();
		chart.setBackgroundPaint(Color.white);

		plot.setBackgroundPaint(new Color(Integer.decode("#c0c0c0").intValue()));
		plot.setDomainGridlinePaint(Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);
		renderer.setFillBox(true);
		renderer.setArtifactPaint(Color.BLACK);
		renderer.setSeriesPaint(0,new Color(Integer.decode("#0000FF").intValue()));

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setRange(min, max);

		return chart;
	}




	public Dataset filterDataset(Dataset dataset, HashMap categories,
			int catSelected, int numberCatsVisualization) {
		// TODO Auto-generated method stub
		return super.filterDataset(dataset, categories, catSelected,
				numberCatsVisualization);
	}

}
