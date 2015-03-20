/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.targetcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyBarRendererThresholdPaint;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.BasicStroke;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleInsets;

public class WinLose extends TargetCharts{


	private static transient Logger logger=Logger.getLogger(WinLose.class);

	// all the targets
	Vector<Double> targets=null;
	Vector<Double> baselines=null;

	Vector<String> nullValues=null;
	double barHeight=0.5;
	/** sets the height of bars */
	public static final String BAR_HEIGHT = "bar_height";


	@Override
	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");

		DatasetMap datasets = super.calculateValue();
		if(datasets==null || yearsDefined==null){
			logger.error("Error in TrargetCharts calculate value");
			return null;
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 		
		datasets.addDataset("1",dataset);

		if(datasets!=null && yearsDefined.isEmpty()){
			logger.warn("no rows found with dataset");
		}
		else{

			// Check if defining target and baseline
			Double mainTarget=null;
			Double mainBaseline=null;
			if(mainThreshold==null){
				logger.error("No main target or baseline defined, not possible to draw the chart");
			}
			else{
				if(useTargets)mainTarget=mainThreshold;
				else mainBaseline=mainThreshold;
				nullValues=new Vector<String>();

				// run all the years defined
				for (Iterator iterator = yearsDefined.iterator(); iterator.hasNext();) {
					String currentYearS = (String) iterator.next();
					int currentYear=Integer.valueOf(currentYearS).intValue();
					boolean stop=false;
					for(int i = 1; i < 13 && stop==false; i++) {
						Month currentMonth=new Month(i,currentYear);
						// if it is the first year and th ecurrent month is previous than the first month
						if(currentYearS.equalsIgnoreCase(yearsDefined.first()) && i<firstMonth.getMonth()){
							// continue
						}
						else{
							TimeSeriesDataItem item = timeSeries.getDataItem(currentMonth);
							if(item != null && item.getValue() != null) {
								double v = item.getValue().doubleValue();
								double result = 0;
								if(mainTarget != null) {
									result = (v >= mainTarget.doubleValue())?WIN:LOSE;
								} else if(mainBaseline != null) {
									result = (v > mainBaseline.doubleValue())?LOSE:WIN;
								}
								else{
									logger.warn("could not find a threshold");
								}

								dataset.addValue(result, timeSeries.getKey(), "" + i+"-"+currentYear);   

							} else {
								if(wlt_mode.doubleValue() == 5){
									dataset.addValue(0.001, timeSeries.getKey(), "" + i+"-"+currentYear); 
									nullValues.add("" + i+"-"+currentYear);
								}
								else{
									dataset.addValue(0.0, timeSeries.getKey(), "" + i+"-"+currentYear); 							
								}
							}
							// if it is last year and current month is after the last month stop 
							if(currentYearS.equalsIgnoreCase(lastYear) && i>=lastMonth.getMonth()){
								stop=true;
							}

						} 
					}

				}
			}
		}
		logger.debug("OUT");
		return datasets;
	}







	@Override
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);

		if(confParameters.get(BAR_HEIGHT)!=null){		
			String bh=(String)confParameters.get(BAR_HEIGHT);
			barHeight=Double.valueOf(bh).doubleValue();
			WIN=barHeight;
			LOSE=-barHeight;

		}
		else
		{
			barHeight=0.5;
		}



		logger.debug("OUT");
	}




	@Override
	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		DefaultCategoryDataset dataset=(DefaultCategoryDataset)datasets.getDatasets().get("1");

		JFreeChart chart = ChartFactory.createBarChart( 
				name, 
				null, 
				null, 
				dataset, 
				PlotOrientation.VERTICAL, 
				legend, 
				false, 
				false); 
		chart.setBorderVisible(false); 
		chart.setBackgroundPaint(color);

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setOutlineVisible(false); 
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0)); 
		plot.setBackgroundPaint(color); 
		plot.setDomainGridlinesVisible(false); 
		plot.setRangeGridlinesVisible(false); 
		plot.setRangeCrosshairVisible(true);
		plot.setRangeCrosshairStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		plot.setRangeCrosshairPaint(color.BLACK);

		// customize axes 
		CategoryAxis domainAxis = plot.getDomainAxis(); 
		domainAxis.setVisible(false);
		domainAxis.setCategoryMargin(0.2);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); 
		rangeAxis.setVisible(false); 
		rangeAxis.setRange(new Range(-(barHeight+0.2 ), (barHeight+0.2))); 

		// customize renderer 
		MyBarRendererThresholdPaint renderer=new MyBarRendererThresholdPaint(useTargets, thresholds, dataset, timeSeries, nullValues,bottomThreshold, color );

		if(wlt_mode.doubleValue() == 0) {
			renderer.setBaseItemLabelsVisible(Boolean.FALSE, true);
		} else {
			renderer.setBaseItemLabelsVisible(Boolean.TRUE, true);
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.#")) {
				public String generateLabel(CategoryDataset dataset, int row, int column) {
					if(dataset.getValue(row, column) == null || dataset.getValue(row, column).doubleValue() == 0) return "";
					String columnKey=(String)dataset.getColumnKey(column);
					int separator=columnKey.indexOf('-');
					String month=columnKey.substring(0,separator);
					String year=columnKey.substring(separator+1);
					int monthNum = Integer.parseInt(month);
					if(wlt_mode.doubleValue() >= 1 && wlt_mode.doubleValue() <= 4) {
						if(wlt_mode.doubleValue() == 2 && column%2 == 0) return "";

						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.MONTH, monthNum-1);
						SimpleDateFormat dataFormat = new SimpleDateFormat("MMM");        		
						return dataFormat.format( calendar.getTime() );
					} else return "" + monthNum;
				}
			});
		}

		if(wlt_mode.doubleValue() == 3) {
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BOTTOM_CENTER,org.jfree.ui.TextAnchor.BOTTOM_RIGHT, Math.PI/2));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, org.jfree.ui.TextAnchor.TOP_CENTER, org.jfree.ui.TextAnchor.HALF_ASCENT_LEFT, Math.PI/2));

		} else if (wlt_mode.doubleValue() == 4 || wlt_mode.doubleValue() == 5) {
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BOTTOM_CENTER,org.jfree.ui.TextAnchor.BOTTOM_RIGHT, Math.PI/4));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, org.jfree.ui.TextAnchor.TOP_CENTER, org.jfree.ui.TextAnchor.HALF_ASCENT_LEFT, Math.PI/4));
		}

		if(legend==true){
			LegendItemCollection collection=createThresholdLegend(plot);
			plot.setFixedLegendItems(collection);
		}

		if(maxBarWidth!=null){
			renderer.setMaximumBarWidth(maxBarWidth); 
		}
		//renderer.setSeriesPaint(0, Color.BLUE); 
		plot.setRenderer(renderer);

		logger.debug("OUT");
		if(mainThreshold==null)return null;
		return chart;


	}



}
