/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.linecharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;

public class LineChart extends ChartImpl{
	
	private static transient Logger logger=Logger.getLogger(LineChart.class);
	HashMap categories;
	int categoriesNumber=0;
	DatasetMap datasetMap;
	String res = "";
	
	public LineChart() {
		super();
		datasetMap=new DatasetMap();
		categories=new HashMap();
	}
	
	public DatasetMap calculateValue(String result) throws Exception {

		logger.debug("IN");
		res = result;
		categories=new HashMap();
		datasetMap=new DatasetMap();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");

		// run all categories (one for each row)
		categoriesNumber=0;

		datasetMap.getDatasets().put("line", new DefaultCategoryDataset());

		boolean first=true;

		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			HashMap series=new LinkedHashMap();
			HashMap additionalValues=new LinkedHashMap();
			String catValue="";

			String nameP="";
			String value="";

			//run all the attributes, to define series!
			int numColumn = 0;
			if (!atts.isEmpty()){
				for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
					numColumn ++;
					SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();
	
					nameP=new String(object.getKey());
					value=new String((String)object.getValue());
					logger.error("Name:"+nameP);
					logger.error("Value:"+value);
					if(nameP.equalsIgnoreCase("x"))
					{
						catValue=value;
						categoriesNumber=categoriesNumber+1;
						categories.put(new Integer(categoriesNumber),value);
	
					}
					else {
								series.put(nameP, value);
						}
				}
			}

			String nameS = "KPI_VALUE";
			String labelS = "kpi Values";
			String valueS=(String)series.get(nameS);
			if (valueS!=null){
			((DefaultCategoryDataset)(datasetMap.getDatasets().get("line"))).addValue(Double.valueOf(valueS).doubleValue(), labelS, catValue);
			}
		}
		logger.debug("OUT");
		return datasetMap;
	}
	
	
	public JFreeChart createChart(){
		
		logger.debug("IN");
		CategoryPlot plot = new CategoryPlot();

		
		NumberAxis rangeAxis = new NumberAxis("Kpi Values");
		rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12 ));
		Color colorLabel= Color.decode("#000000");
		rangeAxis.setLabelPaint(colorLabel);
		rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10 ));
		rangeAxis.setTickLabelPaint(colorLabel);
		plot.setRangeAxis(rangeAxis);
		
		CategoryAxis domainAxis = new CategoryAxis();
		domainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 10 ));
        domainAxis.setLabelPaint(colorLabel);
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10 ));
        domainAxis.setTickLabelPaint(colorLabel);
		plot.setDomainAxis(domainAxis);

		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);


		//I create a line renderer 
		MyStandardCategoryItemLabelGenerator generator=null;

			LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
			lineRenderer.setShapesFilled(true);
			lineRenderer.setBaseItemLabelGenerator(generator);
			lineRenderer.setBaseItemLabelFont(new Font("Arial", Font.PLAIN, 12 ));
			lineRenderer.setBaseItemLabelPaint(colorLabel);
			lineRenderer.setBaseItemLabelsVisible(true);

			DefaultCategoryDataset datasetLine=(DefaultCategoryDataset)datasetMap.getDatasets().get("line");

				for (Iterator iterator = datasetLine.getRowKeys().iterator(); iterator.hasNext();) {
					String serName = (String) iterator.next();
					String labelName = "";
					int index=-1;
					index=datasetLine.getRowIndex(serName);
					
					Color color=Color.decode("#990200");
					lineRenderer.setSeriesPaint(index, color);	
				}

			plot.setDataset(0,datasetLine);
			plot.setRenderer(0,lineRenderer);

		plot.getDomainAxis().setCategoryLabelPositions(
				CategoryLabelPositions.UP_45);
		JFreeChart chart = new JFreeChart(plot);
		logger.debug("Chart created");
		TextTitle title=new TextTitle(name,new Font("Arial", Font.BOLD, 16 ),Color.decode("#990200"), RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
		chart.setTitle(title);
		TextTitle subTitle =new TextTitle(subName,new Font("Arial", Font.PLAIN, 12 ),Color.decode("#000000"), RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
		chart.addSubtitle(subTitle);
		TextTitle subTitle2 =new TextTitle(subName,new Font("Arial", Font.PLAIN, 8 ),Color.decode("#FFFFFF"), RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
		chart.addSubtitle(subTitle2);
		chart.removeLegend();
		
		chart.setBackgroundPaint(Color.white);
		logger.debug("OUT");
		return chart;
	}

}
