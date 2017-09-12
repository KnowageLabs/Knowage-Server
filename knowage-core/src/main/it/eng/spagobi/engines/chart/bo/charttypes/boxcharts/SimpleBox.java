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
