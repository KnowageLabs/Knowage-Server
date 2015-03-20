/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.targetcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.TargetThreshold;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

public class SparkLine extends TargetCharts{

	private static transient Logger logger=Logger.getLogger(SparkLine.class);

	// all the targets
	Vector<Double> targets=null;
	Vector<Double> baselines=null;
	private int lastIndexMonth=1;
	static protected Color colorAverage=Color.GRAY;

	/** avreage line color */
	public static final String AVG_COLOR = "avg_color";


	@Override
	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");

		DatasetMap datasets = super.calculateValue();
		if(datasets==null || yearsDefined==null){
			logger.error("Error in TrargetCharts calculate value");
			return null;
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection(); 
		if(datasets!=null && yearsDefined.isEmpty()){
			logger.warn("no rows found with dataset");
		}
		else{

			int itemCount = timeSeries.getItemCount();		
			// this is the main time series, to be linked with Line and shape
			dataset.addSeries(timeSeries);

			// Check if defining target and baseline
			Double mainTarget=null;
			Double mainBaseline=null;
			if(useTargets)mainTarget=mainThreshold;
			else mainBaseline=mainThreshold;

			// run all the years defined
			lastIndexMonth = 1;
			for (Iterator iterator = yearsDefined.iterator(); iterator.hasNext();) {
				String currentYearS = (String) iterator.next();
				int currentYear=Integer.valueOf(currentYearS).intValue();
				// get the last in l
				for(int i = 1; i < 13; i++) {
					TimeSeriesDataItem item = timeSeries.getDataItem(new Month(i, currentYear));
					if(item == null || item.getValue() == null) {
						//timeSeries.addOrUpdate(new Month(i, currentYear), null);
					} else {
						lastIndexMonth = i;
					}
				}
			}
		}
		datasets.addDataset("1",dataset);

		logger.debug("OUT");
		return datasets;
	}




	@Override
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		if(confParameters.get(AVG_COLOR)!=null && !(((String)confParameters.get(AVG_COLOR)).equalsIgnoreCase("") )){	
			String avg_color=(String)confParameters.get(AVG_COLOR);
			Color color=Color.GRAY;
			try{
				color=Color.decode(avg_color);
				if(color!=null){
					colorAverage=color;
				}
			}
			catch (NumberFormatException e) {
				logger.error("Error in average line color, put default GREY",e);
			}
		}
		logger.debug("OUT");
	}



	@Override
	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		XYDataset dataset=(XYDataset)datasets.getDatasets().get("1");

		final JFreeChart sparkLineGraph = ChartFactory.createTimeSeriesChart( 
				null, 
				null, 
				null, 
				dataset, 
				legend, 
				false, 
				false 
		); 
		sparkLineGraph.setBackgroundPaint(color);

		TextTitle title =setStyleTitle(name, styleTitle);
		sparkLineGraph.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			sparkLineGraph.addSubtitle(subTitle);
		}

		sparkLineGraph.setBorderVisible(false); 
		sparkLineGraph.setBorderPaint(Color.BLACK); 
		XYPlot plot = sparkLineGraph.getXYPlot(); 
		plot.setOutlineVisible(false); 
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0)); 
		plot.setBackgroundPaint(null); 
		plot.setDomainGridlinesVisible(false); 
		plot.setDomainCrosshairVisible(false); 
		plot.setRangeGridlinesVisible(false); 
		plot.setRangeCrosshairVisible(false); 
		plot.setBackgroundPaint(color); 

		// calculate the last marker color
		Paint colorLast = getLastPointColor();

		// Calculate average, minimum and maximum to draw plot borders.
		boolean isFirst = true;
		double avg = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		int count = 0;
		for(int i = 0; i < timeSeries.getItemCount(); i++) {
			if( timeSeries.getValue(i) != null ) {
				count++;
				if(isFirst) {
					min = timeSeries.getValue(i).doubleValue();
					max = timeSeries.getValue(i).doubleValue();
					isFirst = false;
				}
				double n = timeSeries.getValue(i).doubleValue();
				//calculate avg, min, max
				avg += n;
				if(n < min) min = n;
				if(n > max) max = n;
			}
		}
		// average
		avg = avg/(double)count;

		// calculate min and max between thresholds!
		boolean isFirst2 = true;
		double lb = 0, ub = 0;
		for (Iterator iterator = thresholds.keySet().iterator(); iterator.hasNext();) {
			Double thres = (Double) iterator.next();
			if(isFirst2==true){
				ub=thres.doubleValue();
				lb=thres.doubleValue();
				isFirst2=false;
			}
			if(thres.doubleValue()>ub) ub=thres.doubleValue();
			if(thres.doubleValue()<lb) lb=thres.doubleValue();
		}

		plot.getRangeAxis().setRange(new Range(Math.min(lb, min-2), Math.max(ub,max+2)+2));


		addMarker(1, avg, Color.GRAY, 0.8f, plot);
		//addAvaregeSeries(series, plot);
		addPointSeries(timeSeries, plot);

		int num = 3;
		for (Iterator iterator = thresholds.keySet().iterator(); iterator.hasNext();) {
			Double thres = (Double) iterator.next();
			TargetThreshold targThres=thresholds.get(thres);
			Color color=Color.WHITE;
			if(targThres!=null && targThres.getColor()!=null){
				color=targThres.getColor();
			}
			if(targThres.isVisible()){
				addMarker(num++, thres.doubleValue(), color, 0.5f, plot);
			}
		}


		ValueAxis domainAxis = plot.getDomainAxis(); 
		domainAxis.setVisible(false); 
		domainAxis.setUpperMargin(0.2);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); 
		rangeAxis.setVisible(false); 

		plot.getRenderer().setSeriesPaint(0, Color.BLACK); 
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false) { 
			public boolean getItemShapeVisible(int _series, int item) { 
				TimeSeriesDataItem tsdi=timeSeries.getDataItem(item);
				if(tsdi==null)return false;
				Month period=(Month)tsdi.getPeriod();
				int currMonth=period.getMonth();
				int currYear=period.getYearValue();
				int lastMonthFilled=lastMonth.getMonth();
				int lastYearFilled=lastMonth.getYearValue();
				boolean isLast=false;
				if(currYear==lastYearFilled && currMonth==lastMonthFilled){
					isLast=true;
				}
				return isLast; 
			}
		}; 
		renderer.setSeriesPaint(0, Color.decode("0x000000")); 


		renderer.setBaseShapesVisible(true); 
		renderer.setBaseShapesFilled(true); 
		renderer.setDrawOutlines(true); 
		renderer.setUseFillPaint(true); 
		renderer.setBaseFillPaint(colorLast); 
		renderer.setBaseOutlinePaint(Color.BLACK); 
		renderer.setUseOutlinePaint(true); 
		renderer.setSeriesShape(0, new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));



		if(wlt_mode.doubleValue() == 0) {
			renderer.setBaseItemLabelsVisible(Boolean.FALSE, true);
		} else {
			renderer.setBaseItemLabelsVisible(Boolean.TRUE, true);
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());
			renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{2}", new DecimalFormat("0.###"),new DecimalFormat("0.###")) {
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

		} else if (wlt_mode.doubleValue() == 4) {
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.ui.TextAnchor.BOTTOM_CENTER,org.jfree.ui.TextAnchor.BOTTOM_RIGHT, Math.PI/4));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, org.jfree.ui.TextAnchor.TOP_CENTER, org.jfree.ui.TextAnchor.HALF_ASCENT_LEFT, Math.PI/4));
		}

		if(legend==true){
			LegendItemCollection collection=createThresholdLegend(plot);
			LegendItem item=new LegendItem("Avg", "Avg", "Avg","Avg", new Rectangle(10,10),colorAverage);
			collection.add(item);
			plot.setFixedLegendItems(collection);

		}

		plot.setRenderer(0, renderer);
		logger.debug("OUT");
		return sparkLineGraph; 
	} 

	private void addAvaregeSeries(TimeSeries series, XYPlot plot) {
		logger.debug("IN");
		boolean isFirst = true;
		double avg = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		int count = 0;
		for(int i = 0; i < series.getItemCount(); i++) {
			if( series.getValue(i) != null ) {
				count++;
				if(isFirst) {
					min = series.getValue(i).doubleValue();
					max = series.getValue(i).doubleValue();
					isFirst = false;
				}
				double n = series.getValue(i).doubleValue();
				avg += n;
				if(n < min) min = n;
				if(n > max) max = n;
				logger.debug(n);
			}

		}
		avg = avg/(double)count;


		//plot.getRangeAxis().setRange(new Range(min-2, max+2));

		addMarker(1, avg, colorAverage, 0.8f, plot);
		logger.debug("OUT");

	}

	private void addMarker(int index, double value, final Color color,  float stoke, XYPlot plot) {
		logger.debug("IN");
		TimeSeries markerSeries = new TimeSeries("Marker" + index, Month.class);
		for (Iterator iterator = yearsDefined.iterator(); iterator.hasNext();) {
			String currentYear = (String) iterator.next();
			boolean stop=false;			
			for(int i = 1; i < 13 && stop==false; i++) {
				if(!(currentYear.equalsIgnoreCase(yearsDefined.first()) && i<firstMonth.getMonth())){
					markerSeries.add(new Month(i, Integer.valueOf(currentYear).intValue()), value);
				}
				if(currentYear.equalsIgnoreCase(lastYear) && i>=lastMonth.getMonth()){
					stop=true;
				}
			}
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection(markerSeries); 


		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false) { 
			public boolean getItemShapeVisible(int _series, int item) { 
				return (false); 
			} 

			public Paint getItemPaint(int row, int column) {
				return color;
			}
		}; 

		renderer.setBaseStroke(new BasicStroke(stoke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

		renderer.setBaseShapesVisible(true); 
		renderer.setBaseShapesFilled(true); 
		renderer.setDrawOutlines(true); 
		renderer.setUseFillPaint(true); 
		renderer.setBaseFillPaint(Color.GRAY); 
		renderer.setBaseOutlinePaint(Color.BLACK); 
		renderer.setUseOutlinePaint(true); 

		plot.setDataset(index, dataset);
		plot.setRenderer(index, renderer);		
		logger.debug("OUT");
	}

	private void addPointSeries(TimeSeries series, XYPlot plot) {
		logger.debug("IN");
		TimeSeries pointSerie = new TimeSeries("Point", Month.class);
		for(int i = 0; i < series.getItemCount(); i++) {
			pointSerie.add(series.getTimePeriod(i), series.getValue(i));
		}
		final TimeSeriesCollection avgDs = new TimeSeriesCollection(pointSerie); 



		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false) { 
			public boolean getItemShapeVisible(int _series, int item) { 
				return (true); 
			} 
		}; 
		renderer.setSeriesPaint(2, Color.LIGHT_GRAY);
		renderer.setBaseShapesVisible(true); 
		renderer.setBaseShapesFilled(true); 
		renderer.setDrawOutlines(true); 
		renderer.setUseFillPaint(true); 
		renderer.setBaseFillPaint(Color.BLACK); 
		renderer.setBaseOutlinePaint(Color.BLACK); 
		renderer.setUseOutlinePaint(true); 
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));

		plot.setDataset(2, avgDs);
		plot.setRenderer(2, renderer);
		logger.debug("OUT");

	}




	public Paint getLastPointColor(){
		logger.debug("IN");
		Color colorToReturn=null;

		try{
			final int last = lastIndexMonth;
			TimeSeriesDataItem item = timeSeries.getDataItem(new Month(last, Integer.valueOf(lastYear).intValue()));			
			if(item==null || item.getValue()==null){
				return Color.WHITE;	
			}
			Double currentValue=(Double)item.getValue();
			// get the color of the last element
			TreeSet<Double> orderedThresholds=new TreeSet<Double>(thresholds.keySet());
			Double thresholdGiveColor=null;		
			// if dealing with targets, begin from first target and go to on till the current value is major
			if(useTargets){
				boolean stop=false;
				for (Iterator iterator = orderedThresholds.iterator(); iterator.hasNext() && stop==false;) {
					Double currentThres = (Double) iterator.next();
					if(currentValue>=currentThres){
						thresholdGiveColor=currentThres;
					}
					else{
						stop=true;
					}
				}
				//previous threshold is the right threshold that has been passed, if it is null means that we are in the bottom case
			}
			else if(!useTargets){ 
				// if dealing with baseline, begin from first baseline and go to the last; 
				// opposite case than targets, it gets the next baseline
				boolean stop=false;
				for (Iterator iterator = orderedThresholds.iterator(); iterator.hasNext() && stop==false;) {
					Double currentThres = (Double) iterator.next();
					if(currentValue>currentThres){
					}
					else{
						stop=true;
						thresholdGiveColor=currentThres;
					}
				}
				if(stop==false) { // means that current value was > than last baselines, so we are in the bottom case
					thresholdGiveColor=null;
				}
			}

			// ******* Get the color *************
			if(thresholdGiveColor==null){ //bottom case
				if(bottomThreshold!=null && bottomThreshold.getColor()!=null)
					colorToReturn=bottomThreshold.getColor();
				else 
					colorToReturn=Color.GREEN;

			}
			else{
				TargetThreshold currThreshold=thresholds.get(thresholdGiveColor);
				colorToReturn=currThreshold.getColor();
				if(colorToReturn==null){
					colorToReturn=Color.BLACK;
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception while deifning last ponter color: set default green",e); 
			return Color.GREEN;
		}
		logger.debug("OUT");
		return colorToReturn;
	}


	public Color getColorAverage() {
		return colorAverage;
	}

	public void setColorAverage(Color colorAverage) {
		this.colorAverage = colorAverage;
	}


}
