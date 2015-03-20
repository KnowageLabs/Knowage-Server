/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */




package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.TextAnchor;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


public class SimpleBar extends BarCharts{

	boolean horizontalView=false; //false is vertical, true is horizontal
	boolean horizontalViewConfigured=false;

	public static final String CHANGE_VIEW_HORIZONTAL="horizontal";
	public static final String CHANGE_VIEW_LABEL="Set View Orientation";
	public static final String CHANGE_VIEW_LABEL1="Set Vertical View";
	public static final String CHANGE_VIEW_LABEL2="Set Horizontal View";
	private static transient Logger logger=Logger.getLogger(SimpleBar.class);

	/** Orientation of the chart: horizontal, vertical */
	public static final String ORIENTATION = "orientation";


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts#configureChart(it.eng.spago.base.SourceBean)
	 */
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		if(confParameters.get(ORIENTATION)!=null){	
			String orientation=(String)confParameters.get(ORIENTATION);
			if(orientation.equalsIgnoreCase("vertical")){
				horizontalViewConfigured=true;
				horizontalView=false;
			}
			else if(orientation.equalsIgnoreCase("horizontal")){
				horizontalViewConfigured=true;
				horizontalView=true;
			}
		}
		logger.debug("OUT");
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		CategoryDataset dataset=(CategoryDataset)datasets.getDatasets().get("1");

		PlotOrientation plotOrientation=PlotOrientation.VERTICAL;
		if(horizontalView)
		{
			plotOrientation=PlotOrientation.HORIZONTAL;
		}


		JFreeChart chart = ChartFactory.createBarChart(
				name,       // chart title
				categoryLabel,               // domain axis label
				valueLabel,                  // range axis label
				dataset,                  // data
				plotOrientation, // orientation
				false,                     // include legend
				true,                     // tooltips?
				false                     // URLs?
		);

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		// set the background color for the chart...
		chart.setBackgroundPaint(color);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setUpperMargin(0.10);
		rangeAxis.setNumberFormatOverride(nf);

		if(firstAxisLB != null && firstAxisUB != null){
			rangeAxis.setLowerBound(firstAxisLB);
			rangeAxis.setUpperBound(firstAxisUB);
		}


		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		else rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

		if(rangeAxisLocation != null) {
			if(rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
			} else if(rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
			}else if(rangeAxisLocation.equalsIgnoreCase("TOP_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_RIGHT);
			} else if(rangeAxisLocation.equalsIgnoreCase("TOP_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
			}
		}

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// add
		CategorySeriesLabelGenerator generator = new StandardCategorySeriesLabelGenerator("{0}");
		renderer.setLegendItemLabelGenerator(generator);

		if(maxBarWidth!=null){
			renderer.setMaximumBarWidth(maxBarWidth.doubleValue());
		}

		if(showValueLabels){
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());			
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());

			//			if(valueLabelsPosition.equalsIgnoreCase("inside")){
			//			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
			//			ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
			//			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
			//			ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
			//			} else {
			//			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
			//			ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_LEFT));
			//			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
			//			ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_LEFT));
			//			}

		}

		// PROVA LEGENDA		
		if(legend==true){

			drawLegend(chart);

			/*BlockContainer wrapper = new BlockContainer(new BorderArrangement());
			wrapper.setFrame(new BlockBorder(1.0, 1.0, 1.0, 1.0));

			LabelBlock titleBlock = new LabelBlock("Legend Items:",
					new Font("SansSerif", Font.BOLD, 12));
			title.setPadding(5, 5, 5, 5);
			wrapper.add(titleBlock, RectangleEdge.TOP);

			LegendTitle legend = new LegendTitle(chart.getPlot());
			BlockContainer items = legend.getItemContainer();
			items.setPadding(2, 10, 5, 2);
			wrapper.add(items);
			legend.setWrapper(wrapper);

			if(legendPosition.equalsIgnoreCase("bottom")) legend.setPosition(RectangleEdge.BOTTOM);
			else if(legendPosition.equalsIgnoreCase("left")) legend.setPosition(RectangleEdge.LEFT);
			else if(legendPosition.equalsIgnoreCase("right")) legend.setPosition(RectangleEdge.RIGHT);
			else if(legendPosition.equalsIgnoreCase("top")) legend.setPosition(RectangleEdge.TOP);
			else legend.setPosition(RectangleEdge.BOTTOM);

			legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
			chart.addSubtitle(legend);*/
		}


		int seriesN=dataset.getRowCount();

		// the order color vedctor overrides the color map!!

		if(orderColorVector != null && orderColorVector.size()>0){
			logger.debug("color serie by SERIES_ORDER_COLORS template specification");
			for (int i = 0; i < seriesN; i++) {
				if( orderColorVector.get(i)!= null){
					Color color = orderColorVector.get(i);
					renderer.setSeriesPaint(i, color);
				}		
			}	
		}		
		else if(colorMap!=null){
			logger.debug("color serie by SERIES_COLORS template specification");
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getRowKey(i);	
				String labelName = "";
				int index=-1;
				if (seriesCaptions != null && seriesCaptions.size()>0){
					labelName = serieName;
					serieName = (String)seriesCaptions.get(serieName);
					index=dataset.getRowIndex(labelName);
				}
				else
					index=dataset.getRowIndex(serieName);

				Color color=(Color)colorMap.get(serieName);
				if(color!=null){
					//renderer.setSeriesPaint(i, color);
					renderer.setSeriesPaint(index, color);
					renderer.setSeriesItemLabelFont(i, new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
					renderer.setSeriesItemLabelPaint(i, defaultLabelsStyle.getColor());
				}	
			}
		}



		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
				CategoryLabelPositions.createUpRotationLabelPositions(
						Math.PI / 6.0));
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setUpperMargin(0.10);
		logger.debug("OUT");
		return chart;

	}



	/**
	 * Checks if is horizontal view.
	 * 
	 * @return true, if is horizontal view
	 */
	public boolean isHorizontalView() {
		return horizontalView;
	}

	/**
	 * Sets the horizontal view.
	 * 
	 * @param changeViewChecked the new horizontal view
	 */
	public void setHorizontalView(boolean changeViewChecked) {
		this.horizontalView = changeViewChecked;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isChangeableView()
	 */
	public boolean isChangeableView() {
		return true;
	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getPossibleChangePars()
	 */
	public List getPossibleChangePars() {
		List l=new Vector();
		if(!horizontalViewConfigured){
			l.add(CHANGE_VIEW_HORIZONTAL);}

		return l;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#setChangeViewsParameter(java.lang.String, boolean)
	 */
	public void setChangeViewsParameter(String changePar, boolean how) {
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			horizontalView=how;
		}

	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameter(java.lang.String)
	 */
	public boolean getChangeViewParameter(String changePar) {
		boolean ret=false;
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			ret=horizontalView;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameterLabel(java.lang.String, int)
	 */
	public String getChangeViewParameterLabel(String changePar, int i) {
		String ret="";
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			if(i==0)	
				ret=CHANGE_VIEW_LABEL;
			else if(i==1) ret=CHANGE_VIEW_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_LABEL2;

		}
		return ret;
	}








}
