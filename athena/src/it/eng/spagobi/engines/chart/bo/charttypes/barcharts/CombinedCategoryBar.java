/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyCategoryToolTipGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyCategoryUrlGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

public class CombinedCategoryBar extends LinkableBar {


	HashMap seriesDraw=null;
	HashMap seriesScale=null;
	// Maps the serie Label with the reale Serie name
	HashMap seriesCaptions=null;
	boolean additionalLabels=false;
	HashMap catSerLabels=null;
	boolean useLinesRenderers=false; // just one to make code shorter in creation of the chart
	String secondAxisLabel=null;
	Vector lineNoShapeSeries1=null;
	Vector lineNoShapeSeries2=null;

	boolean freeToolTips=false;   //automatically set

	// maps the element with the tooltip information. tip_element or freetip_element
	HashMap<String, String> seriesTooltip=null; 
	HashMap<String, String> categoriesTooltip=null; 

	private static transient Logger logger=Logger.getLogger(CombinedCategoryBar.class);





	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");

		seriesNames=new Vector();
		seriesCaptions=new LinkedHashMap();

		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new HashMap();

		DatasetMap datasetMap=new DatasetMap();

		SourceBean sbRows=SourceBean.fromXMLString(res);

		List listAtts=sbRows.getAttributeAsList("ROW"); // One row for each category

		categoriesNumber=0;

		// 4 datasets, 2 for first axis named 1, 2 for second axis named 2
		datasetMap.getDatasets().put("1-bar", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("1-line", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("2-bar", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("2-line", new DefaultCategoryDataset());

		lineNoShapeSeries1=new Vector<String>();
		lineNoShapeSeries2=new Vector<String>();
		categoriesTooltip=new HashMap<String, String>();
		seriesTooltip=new HashMap<String, String>();
		seriesOrder=new ArrayList<String>();

		boolean first=true;
		//categories.put(new Integer(0), "All Categories");

		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) { 		// run all categories (one for each row)
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();							// attributes: x is category name, values as serie_name are others, addition values can be added

			HashMap series=new LinkedHashMap();
			HashMap additionalValues=new LinkedHashMap();
			String catValue="";

			String nameP="";
			String value="";

			if(first){
				if (name.indexOf("$F{") >= 0){
					setTitleParameter(atts);
				}
				if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				first=false;
			}


			//run all the attributes, to define series!
			int numColumn = 0;
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				numColumn ++;
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();
				// In order to have tootltip for category they must be defined after x
				nameP=new String(object.getKey());
				value=new String((String)object.getValue());
				if(nameP.equalsIgnoreCase("x"))       								// category name
				{
					catValue=value;
					categoriesNumber=categoriesNumber+1;
					categories.put(new Integer(categoriesNumber),value);
				}
				else {
					if(nameP.toUpperCase().startsWith("ADD_")){       // additional information
						if(additionalLabels){
							String ind=nameP.substring(4);							
							additionalValues.put(ind, value);
						}
					} // must be after x definition
					else if((nameP.toUpperCase()).startsWith("TIP_X")){       // additional information
						if(enableToolTips){
							categoriesTooltip.put(nameP.toUpperCase()+"_"+catValue, value);
						}
					}

					else if(nameP.toUpperCase().startsWith("TIP_")){       // additional information
						if(enableToolTips){
							seriesTooltip.put(nameP.toUpperCase(), value);
						}
					}
					else if(nameP.toUpperCase().startsWith("FREETIP_X")){       // additional information
						if(enableToolTips){
							freeToolTips=true; //help the search later in MyCategoryToolTipGenerator
							categoriesTooltip.put(nameP.toUpperCase()+"_"+catValue, value);
						}
					}					
					else{
						if(seriesLabelsMap!=null){									// a serie
							String serieLabel = (String)seriesLabelsMap.get(nameP);
							series.put(serieLabel, value);
							if(!seriesOrder.contains(serieLabel)){
								seriesOrder.add(serieLabel);
							}
							seriesCaptions.put(serieLabel, nameP);							

						}
						else {
							series.put(nameP, value);
							if(!seriesOrder.contains(nameP)){
								seriesOrder.add(nameP);
							}
						}
					}
					// for now I make like if addition value is checked he seek for an attribute with name with value+name_serie
				}
			}


			// for each serie
			for (Iterator iterator3 = series.keySet().iterator(); iterator3.hasNext();) {
				String nameS = (String) iterator3.next();
				String labelS = "";
				String valueS=(String)series.get(nameS);
				Double valueD=null;
				try{
					valueD=Double.valueOf(valueS);
				}
				catch (Exception e) {
					logger.warn("error in double conversion, put default to null");
					valueD=null;
				}

				
				if(!hiddenSeries.contains(nameS)){
					if(seriesLabelsMap != null && (seriesCaptions != null && seriesCaptions.size()>0)){
						nameS = (String)(seriesCaptions.get(nameS));
						labelS = (String)seriesLabelsMap.get(nameS);
						if(labelS==null)labelS=nameS;
					}
					else
						labelS = nameS;	


					// Fill DATASET: Check if has to be filled dataset 1 or dataset 2, to bar or lines
					// LINE CASE
					if(!isHiddenSerie(nameS) && seriesDraw.get(nameS)!=null && 
							(((String)seriesDraw.get(nameS)).equalsIgnoreCase("line") || ((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes"))
					)
					{
						if(!seriesNames.contains(nameS))seriesNames.add(nameS);
						// SET THE AXIS
						if(seriesScale != null && seriesScale.get(nameS)!=null && ((String)seriesScale.get(nameS)).equalsIgnoreCase("2")){
							useLinesRenderers=true;
							if(((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes") && !lineNoShapeSeries2.contains(nameS)){
								lineNoShapeSeries2.add(nameS);
							}
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("2-line"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}
						else{ 
							useLinesRenderers=true;
							if(((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes") && !lineNoShapeSeries1.contains(nameS)){
								lineNoShapeSeries1.add(nameS);
							}
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("1-line"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}

					}
					else if(!isHiddenSerie(nameS)){// BAR CASE
						if(!seriesNames.contains(nameS))seriesNames.add(nameS);
						// if to draw mapped to first axis
						if(seriesScale != null && seriesScale.get(nameS)!=null && ((String)seriesScale.get(nameS)).equalsIgnoreCase("2")){
							if(!seriesNames.contains(nameS))seriesNames.add(nameS);
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("2-bar"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}
						else{ // if to draw as a bar
							if(!seriesNames.contains(nameS))seriesNames.add(nameS);
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("1-bar"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}
					}

//					if(!isHiddenSerie(nameS)){ 
//					if(!seriesNames.contains(nameS))
//					seriesNames.add(nameS);
//					if(seriesScale != null && seriesScale.get(nameS)!=null && ((String)seriesScale.get(nameS)).equalsIgnoreCase("2")){ // 2 axis
//					if(!seriesNames.contains(nameS))seriesNames.add(nameS);
//					((DefaultCategoryDataset)(datasetMap.getDatasets().get("2"))).addValue(Double.valueOf(valueS).doubleValue(), labelS, catValue);
//					}
//					else{ 																												// 1 axis	
//					if(!seriesNames.contains(nameS))seriesNames.add(nameS);							
//					((DefaultCategoryDataset)(datasetMap.getDatasets().get("1"))).addValue(Double.valueOf(valueS).doubleValue(), labelS, catValue);
//					}
//					}

					// if there is an additional label are 
					if(additionalValues.get(nameS)!=null){
						String val=(String)additionalValues.get(nameS);
						String index=catValue+"-"+nameS;
						catSerLabels.put(index, val);
					}

				}
			}     // close series cycle

		} // Close cycle on SpagoBI Dataset rows

		if (listAtts.size() == 0){
			if (name.indexOf("$F{") >= 0){
				setTitleParameter("");
			}
			if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
				setSubTitleParameter("");
			}
		}
		logger.debug("OUT");

		return datasetMap;


	}






	public void configureChart(SourceBean content) {
		super.configureChart(content);
		logger.debug("IN");


		if(confParameters.get("add_labels")!=null){	
			String additional=(String)confParameters.get("add_labels");
			if(additional.equalsIgnoreCase("true")){
				additionalLabels=true;
				catSerLabels=new LinkedHashMap();
			}
			else additionalLabels=false;
		}
		else
		{
			additionalLabels=false;
		}

		if(confParameters.get("add_labels")!=null){
			String additional=(String)confParameters.get("add_labels");
			if(additional.equalsIgnoreCase("true")){
				additionalLabels=true;
				catSerLabels=new LinkedHashMap();
			}
			else additionalLabels=false;
		}
		else
		{
			additionalLabels=false;
		}


		// In template: <SERIES_DRAW serie1='line' serie2='bar< />

		SourceBean draws = (SourceBean)content.getAttribute("SERIES_DRAW");
		if(draws==null){
			draws = (SourceBean)content.getAttribute("CONF.SERIES_DRAW");
		}
		seriesDraw=new LinkedHashMap();
		if(draws!=null){

			List atts=draws.getContainedAttributes();
			String serieName="";
			String serieDraw="";
			// Run all the series specified in template and check if they are bar or line, by default will be bar; if not specified but present will be bar
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
				serieName=new String(object.getKey());
				serieDraw=new String((String)object.getValue());

				if(serieDraw.equalsIgnoreCase("line")){
					seriesDraw.put(serieName, "line");
				}
				else if(serieDraw.equalsIgnoreCase("line_no_shapes")){
					seriesDraw.put(serieName, "line_no_shapes");
				}
				else{
					seriesDraw.put(serieName, "bar");					
				}

			}		

		}



		if(confParameters.get("second_axis_label")!=null){	
			secondAxisLabel=(String)confParameters.get("second_axis_label");
		}


		// check wich series has to be mapped to the first axis and wich to the second
		SourceBean scales = (SourceBean)content.getAttribute("SERIES_SCALES");

		if(scales==null){
			scales = (SourceBean)content.getAttribute("CONF.SERIES_SCALES");
		}
		seriesScale=new LinkedHashMap(); // Maps serie Name to scale Number (1 or 2)

		if(scales!=null){

			List attsScales=scales.getContainedAttributes();

			String serieName="";
			Integer serieScale=1;
			for (Iterator iterator = attsScales.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
				serieName=new String(object.getKey());
				try{
					String serieScaleS=(String)object.getValue();
					serieScale=Integer.valueOf(serieScaleS);
				}
				catch (Exception e) {
					logger.error("Not correct numebr scale; setting default 1");
					serieScale=Integer.valueOf(1);
				}

				if(serieScale.equals(2)){
					seriesScale.put(serieName, "2");
				}
				else{
					seriesScale.put(serieName, "1");					
				}
			}		
		}

		logger.debug("OUT");
	}




	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");

		// recover the datasets
		DefaultCategoryDataset datasetBarFirstAxis=(DefaultCategoryDataset)datasets.getDatasets().get("1-bar");
		DefaultCategoryDataset datasetBarSecondAxis=(DefaultCategoryDataset)datasets.getDatasets().get("2-bar");
		DefaultCategoryDataset datasetLineFirstAxis=(DefaultCategoryDataset)datasets.getDatasets().get("1-line");
		DefaultCategoryDataset datasetLineSecondAxis=(DefaultCategoryDataset)datasets.getDatasets().get("2-line");

		// create the two subplots
		CategoryPlot subPlot1 = new CategoryPlot();
		CategoryPlot subPlot2 = new CategoryPlot();
		CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot();

		subPlot1.setDataset(0,datasetBarFirstAxis);
		subPlot2.setDataset(0,datasetBarSecondAxis);

		subPlot1.setDataset(1,datasetLineFirstAxis);
		subPlot2.setDataset(1,datasetLineSecondAxis);

		// localize numbers on y axis
        NumberFormat nf = (NumberFormat) NumberFormat.getNumberInstance(locale);

		
		// Range Axis 1
		NumberAxis rangeAxis = new NumberAxis(getValueLabel());
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setUpperMargin(0.10);
        rangeAxis.setNumberFormatOverride(nf);
		subPlot1.setRangeAxis(rangeAxis);
		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}

		// Range Axis 2
		NumberAxis rangeAxis2 = new NumberAxis(secondAxisLabel);
		rangeAxis2.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis2.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis2.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis2.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis2.setUpperMargin(0.10);
        rangeAxis2.setNumberFormatOverride(nf);		
		subPlot2.setRangeAxis(rangeAxis2);
		if(rangeIntegerValues==true){
			rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}

		// Category Axis
		CategoryAxis domainAxis = new CategoryAxis(getCategoryLabel());
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setUpperMargin(0.10);
		plot.setDomainAxis(domainAxis);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);

		// Add subplots to main plot
		plot.add(subPlot1, 1);
		plot.add(subPlot2, 2);

		MyStandardCategoryItemLabelGenerator generator=null;

		// value labels and additional values are mutually exclusive
		if(showValueLabels==true)additionalLabels=false;

		if(additionalLabels){
			generator = new MyStandardCategoryItemLabelGenerator(catSerLabels,"{1}", NumberFormat.getInstance());
		}

//		Create Renderers!
		CategoryItemRenderer barRenderer1=new BarRenderer();
		CategoryItemRenderer barRenderer2=new BarRenderer();
		LineAndShapeRenderer lineRenderer1=(useLinesRenderers==true) ? new LineAndShapeRenderer() : null;
		LineAndShapeRenderer lineRenderer2=(useLinesRenderers==true) ? new LineAndShapeRenderer() : null;

		subPlot1.setRenderer(0,barRenderer1);
		subPlot2.setRenderer(0,barRenderer2);

		if(useLinesRenderers==true){
			subPlot1.setRenderer(1,lineRenderer1);
			subPlot2.setRenderer(1,lineRenderer2);

			// no shapes for line_no_shapes  series
			for (Iterator iterator = lineNoShapeSeries1.iterator(); iterator.hasNext();) {
				String ser = (String) iterator.next();
				// if there iS a abel associated search for that
				String label=null;
				if(seriesLabelsMap!=null){
					label=(String)seriesLabelsMap.get(ser);
				}
				if(label==null)label=ser;
				int index=datasetLineFirstAxis.getRowIndex(label);
				if(index!=-1){
					lineRenderer1.setSeriesShapesVisible(index, false);
				}
			}
			for (Iterator iterator = lineNoShapeSeries2.iterator(); iterator.hasNext();) {
				String ser = (String) iterator.next();
				// if there iS a abel associated search for that

				String label=null;
				if(seriesLabelsMap!=null){
					label=(String)seriesLabelsMap.get(ser);
				}
				if(label==null)label=ser;
				int index=datasetLineSecondAxis.getRowIndex(label);
				if(index!=-1){
					lineRenderer2.setSeriesShapesVisible(index, false);
				}
			}

		}

		// add tooltip if enabled
		if(enableToolTips){
			MyCategoryToolTipGenerator generatorToolTip=new MyCategoryToolTipGenerator(freeToolTips, seriesTooltip, categoriesTooltip, seriesCaptions);
			barRenderer1.setToolTipGenerator(generatorToolTip);
			barRenderer2.setToolTipGenerator(generatorToolTip);
			if(useLinesRenderers){
				lineRenderer1.setToolTipGenerator(generatorToolTip);
				lineRenderer2.setToolTipGenerator(generatorToolTip);				
			}
		}

		subPlot1.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		subPlot2.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		// COnfigure renderers: I do in extensive way so will be easier to add customization in the future

		if(maxBarWidth!=null){
			((BarRenderer)barRenderer1).setMaximumBarWidth(maxBarWidth.doubleValue());
			((BarRenderer)barRenderer2).setMaximumBarWidth(maxBarWidth.doubleValue());
		}


		// Values or addition Labels for first BAR Renderer
		if(showValueLabels){
			barRenderer1.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
			barRenderer1.setBaseItemLabelsVisible(true);
			barRenderer1.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			barRenderer1.setBaseItemLabelPaint(styleValueLabels.getColor());

			barRenderer1.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

			barRenderer1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

			barRenderer2.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
			barRenderer2.setBaseItemLabelsVisible(true);
			barRenderer2.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			barRenderer2.setBaseItemLabelPaint(styleValueLabels.getColor());

			barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

			barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

		}
		else if(additionalLabels){
			barRenderer1.setBaseItemLabelGenerator(generator);
			barRenderer2.setBaseItemLabelGenerator(generator);

			double orient=(-Math.PI / 2.0);
			if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
				orient=0.0;
			}

			barRenderer1.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
			barRenderer1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
			barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
			barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));

		}

		// Values or addition Labels for line Renderers if requested
		if(useLinesRenderers==true){
			if(showValueLabels){
				lineRenderer1.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				lineRenderer1.setBaseItemLabelsVisible(true);
				lineRenderer1.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				lineRenderer1.setBaseItemLabelPaint(styleValueLabels.getColor());
				lineRenderer1.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				lineRenderer1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				lineRenderer2.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				lineRenderer2.setBaseItemLabelsVisible(true);
				lineRenderer2.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				lineRenderer2.setBaseItemLabelPaint(styleValueLabels.getColor());
				lineRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				lineRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

			}
			else if(additionalLabels){
				lineRenderer1.setBaseItemLabelGenerator(generator);
				lineRenderer2.setBaseItemLabelGenerator(generator);
				double orient=(-Math.PI / 2.0);
				if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
					orient=0.0;
				}
				lineRenderer1.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
				lineRenderer1.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
				lineRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));
				lineRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, orient));

			}
		}

		// Bar Dataset Colors!
		if(colorMap!=null){
			int idx = -1;
			for (Iterator iterator = datasetBarFirstAxis.getRowKeys().iterator(); iterator.hasNext();) {
				idx++;
				String serName = (String) iterator.next();
				String labelName = "";
				int index=-1;

				if (seriesCaptions != null && seriesCaptions.size()>0){
					labelName = serName;
					serName = (String)seriesCaptions.get(serName);
					index=datasetBarFirstAxis.getRowIndex(labelName);
				}
				else
					index=datasetBarFirstAxis.getRowIndex(serName);

				Color color=(Color)colorMap.get(serName);
				if(color!=null){
					barRenderer1.setSeriesPaint(index, color);
				}	
			}

			for (Iterator iterator = datasetBarSecondAxis.getRowKeys().iterator(); iterator.hasNext();) {
				idx++;
				String serName = (String) iterator.next();
				String labelName = "";
				int index=-1;

				if (seriesCaptions != null && seriesCaptions.size()>0){
					labelName = serName;
					serName = (String)seriesCaptions.get(serName);
					index=datasetBarSecondAxis.getRowIndex(labelName);
				}
				else
					index=datasetBarSecondAxis.getRowIndex(serName);

				Color color=(Color)colorMap.get(serName);
				if(color!=null){
					barRenderer2.setSeriesPaint(index, color);
				}	
			}				
		}




		// LINE Dataset Colors!
		if(useLinesRenderers==true){
			if(colorMap!=null){
				int idx = -1;
				for (Iterator iterator = datasetLineFirstAxis.getRowKeys().iterator(); iterator.hasNext();) {
					idx++;
					String serName = (String) iterator.next();
					String labelName = "";
					int index=-1;

					if (seriesCaptions != null && seriesCaptions.size()>0){
						labelName = serName;
						serName = (String)seriesCaptions.get(serName);
						index=datasetLineFirstAxis.getRowIndex(labelName);
					}
					else
						index=datasetLineFirstAxis.getRowIndex(serName);

					Color color=(Color)colorMap.get(serName);
					if(color!=null){
						lineRenderer1.setSeriesPaint(index, color);
					}	
				}

				for (Iterator iterator = datasetLineSecondAxis.getRowKeys().iterator(); iterator.hasNext();) {
					idx++;
					String serName = (String) iterator.next();
					String labelName = "";
					int index=-1;

					if (seriesCaptions != null && seriesCaptions.size()>0){
						labelName = serName;
						serName = (String)seriesCaptions.get(serName);
						index=datasetLineSecondAxis.getRowIndex(labelName);
					}
					else
						index=datasetLineSecondAxis.getRowIndex(serName);

					Color color=(Color)colorMap.get(serName);
					if(color!=null){
						lineRenderer2.setSeriesPaint(index, color);
					}	
				}				
			}
		}


		//defines url for drill
		boolean document_composition=false;
		if(mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))document_composition=true;

		logger.debug("Calling Url Generation");


		MyCategoryUrlGenerator mycatUrl=null;
		if(super.rootUrl!=null){
			logger.debug("Set MycatUrl");
			mycatUrl=new MyCategoryUrlGenerator(super.rootUrl);

			mycatUrl.setDocument_composition(document_composition);
			mycatUrl.setCategoryUrlLabel(super.categoryUrlName);
			mycatUrl.setSerieUrlLabel(super.serieUrlname);
			mycatUrl.setDrillDocTitle(drillDocTitle);
			mycatUrl.setTarget(target);
		}
		if(mycatUrl!=null){
			barRenderer1.setItemURLGenerator(mycatUrl);
			barRenderer2.setItemURLGenerator(mycatUrl);
			if(useLinesRenderers){
				lineRenderer1.setItemURLGenerator(mycatUrl);
				lineRenderer2.setItemURLGenerator(mycatUrl);			
			}

		}


		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

		JFreeChart chart = new JFreeChart(plot);
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
		chart.setBackgroundPaint(Color.white);

//		I want to re order the legend
		LegendItemCollection legends=plot.getLegendItems();
		// legend Temp 
		HashMap<String, LegendItem> legendTemp=new HashMap<String, LegendItem>();
		Vector<String> alreadyInserted=new Vector<String>();
		for (int i = 0; i<legends.getItemCount(); i++) {
			LegendItem item=legends.get(i);
			String label=item.getLabel();
			legendTemp.put(label, item);
		}
		LegendItemCollection newLegend=new LegendItemCollection();
		// force the order of the ones specified
		for (Iterator iterator = seriesOrder.iterator(); iterator.hasNext();) {
			String serie = (String) iterator.next();
			if(legendTemp.keySet().contains(serie)){
				newLegend.add(legendTemp.get(serie));
				alreadyInserted.add(serie);
			}
		}
		// check that there are no serie not specified, otherwise add them
		for (Iterator iterator = legendTemp.keySet().iterator(); iterator.hasNext();) {
			String serie = (String) iterator.next();
			if(!alreadyInserted.contains(serie)){
				newLegend.add(legendTemp.get(serie));
			}
		}

		plot.setFixedLegendItems(newLegend);

		if(legend==true) drawLegend(chart);
		logger.debug("OUT");

		return chart;



	}



	private boolean isHiddenSerie(String serName){
		boolean res = false;

		for (int i=0; i < hiddenSeries.size(); i++){
			if (((String)hiddenSeries.get(i)).equalsIgnoreCase(serName)){
				res = true;
				break;
			}
		}
		return res;
	}
}
