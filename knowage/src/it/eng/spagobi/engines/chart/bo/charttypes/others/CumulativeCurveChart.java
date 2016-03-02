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
package it.eng.spagobi.engines.chart.bo.charttypes.others;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.util.SortOrder;


public class CumulativeCurveChart extends BarCharts{

	String xLabel="";
	String yLabel="";
	Map confParameters;
	private static transient Logger logger=Logger.getLogger(CumulativeCurveChart.class);
	SortOrder sortOrder=SortOrder.ASCENDING;
	
	public DatasetMap calculateValue() throws Exception {

		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);

		SourceBean sbRows=SourceBean.fromXMLString(res);
		SourceBean sbRow=(SourceBean)sbRows.getAttribute("ROW");
		List listAtts=sbRow.getContainedAttributes();

		DefaultKeyedValues keyedValues=new DefaultKeyedValues();



		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBeanAttribute att = (SourceBeanAttribute) iterator.next();
			String name=att.getKey();
			String valueS=(String)att.getValue();

			//try Double and Integer Conversion

			Double valueD=null;
			try{
				valueD=Double.valueOf(valueS);
			}
			catch (Exception e) {}

			Integer valueI=null;
			if(valueD==null){
				valueI=Integer.valueOf(valueS);
			}

			if(name!=null && valueD!=null){
				keyedValues.addValue(name, valueD);
			}
			else if(name!=null && valueI!=null){
				keyedValues.addValue(name, valueI);
			}
		}
		keyedValues.sortByValues(sortOrder);  //let user choose


		KeyedValues cumulative = DataUtilities.getCumulativePercentages(keyedValues);

		CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
				"Languages", keyedValues);
		CategoryDataset dataset2 = DatasetUtilities.createCategoryDataset(
				"Cumulative", cumulative);


		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		datasets.addDataset("2",dataset2);

		return datasets;

	}

	public void configureChart(SourceBean content) {
		super.configureChart(content);
		confParameters = new HashMap();
		SourceBean confSB = (SourceBean)content.getAttribute("CONF");

		if(confSB==null) return;
		List confAttrsList = confSB.getAttributeAsList("PARAMETER");

		Iterator confAttrsIter = confAttrsList.iterator();
		while(confAttrsIter.hasNext()) {
			SourceBean param = (SourceBean)confAttrsIter.next();
			String nameParam = (String)param.getAttribute("name");
			String valueParam = (String)param.getAttribute("value");
			confParameters.put(nameParam, valueParam);
		}	
		if(confParameters.get("x_label")!=null){	
			xLabel=(String)confParameters.get("x_label");
		}
		else
		{
			xLabel="x";
		}

		if(confParameters.get("y_label")!=null){	
			yLabel=(String)confParameters.get("y_label");
		}
		else
		{
			yLabel="y";
		}

		if(confParameters.get("sort_order")!=null){	
			String order=(String)confParameters.get("sort_order");
			if(order.equalsIgnoreCase("DESCENDING")) sortOrder=SortOrder.DESCENDING;
		}

	}

	public JFreeChart createChart(DatasetMap datasetMap) {
		CategoryDataset datasetValue=(CategoryDataset)datasetMap.getDatasets().get("1");
		CategoryDataset datasetCumulative=(CategoryDataset)datasetMap.getDatasets().get("2");


		JFreeChart chart = ChartFactory.createBarChart(
				name,  // chart title
				xLabel,                     // domain axis label
				yLabel,                     // range axis label
				datasetValue,                        // data
				PlotOrientation.VERTICAL,
				true,                           // include legend
				true,
				false
		);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.02);
		domainAxis.setUpperMargin(0.02);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();

		NumberAxis axis2 = new NumberAxis("Percent");
		axis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
		plot.setRangeAxis(1, axis2);
		plot.setDataset(1, datasetCumulative);
		plot.setRenderer(1, renderer2);
		plot.mapDatasetToRangeAxis(1, 1);

		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		return chart;   
	}






}
