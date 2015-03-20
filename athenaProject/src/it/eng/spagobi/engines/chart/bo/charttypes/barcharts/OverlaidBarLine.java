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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

public class OverlaidBarLine extends LinkableBar {


	HashMap seriesDraw=null;
	HashMap seriesScale=null;
	HashMap seriesCaptions=null;
	boolean additionalLabels=false;
	HashMap catSerLabels=null;


	boolean useBars=false;
	boolean useLines=false;
	boolean secondAxis=false;
	String secondAxisLabel=null;
	boolean freeToolTips=false;   //automatically set

	boolean stackedBarRenderer_1=false;
	boolean stackedBarRenderer_2=false;

	// maps the element with the tooltip information. tip_element or freetip_element
	HashMap<String, String> seriesTooltip=null; 
	HashMap<String, String> categoriesTooltip=null; 

	Vector lineNoShapeSeries1=null;
	Vector lineNoShapeSeries2=null;

	/** If present gives the second axis a name and enable the presence of the second axis */
	public static final String SECOND_AXIS_LABEL = "second_axis_label";
	public static final String STACKED_BAR_RENDERER_1 = "stacked_bar_renderer_1";
	public static final String STACKED_BAR_RENDERER_2 = "stacked_bar_renderer_2";


	private static transient Logger logger=Logger.getLogger(OverlaidBarLine.class);


	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");

		seriesNames=new Vector();
		seriesCaptions=new LinkedHashMap();
		categoriesTooltip=new HashMap<String, String>();
		seriesTooltip=new HashMap<String, String>();
		lineNoShapeSeries1=new Vector<String>();
		lineNoShapeSeries2=new Vector<String>();

		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new HashMap();

		//DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		DatasetMap datasetMap=new DatasetMap();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");


		// run all categories (one for each row)
		categoriesNumber=0;

		// one dataset for mapping left, one for mapping right
		//		datasetMap.getDatasets().put("bar", new DefaultCategoryDataset());
		//		datasetMap.getDatasets().put("line", new DefaultCategoryDataset());

		datasetMap.getDatasets().put("1-bar", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("1-line", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("2-bar", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("2-line", new DefaultCategoryDataset());



		boolean first=true;
		//categories.put(new Integer(0), "All Categories");
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

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

				nameP=new String(object.getKey());
				value=new String((String)object.getValue());
				if(nameP.equalsIgnoreCase("x"))
				{
					catValue=value;
					categoriesNumber=categoriesNumber+1;
					categories.put(new Integer(categoriesNumber),value);


				}
				else {
					if(nameP.startsWith("add_") || nameP.startsWith("ADD_")){
						if(additionalLabels){
							String ind=nameP.substring(4);							
							additionalValues.put(ind, value);
						}
					}
					// must be after x definition
					else if(nameP.toUpperCase().startsWith("TIP_X")){       // additional information
						if(enableToolTips){
							categoriesTooltip.put(nameP+"_"+catValue, value);
						}
					}

					else if(nameP.toUpperCase().startsWith("TIP_")){       // additional information
						if(enableToolTips){
							seriesTooltip.put(nameP, value);
						}
					}
					else if(nameP.toUpperCase().startsWith("FREETIP_X")){       // additional information
						if(enableToolTips){
							freeToolTips=true; //help the search later in MyCategoryToolTipGenerator
							categoriesTooltip.put(nameP+"_"+catValue, value);
						}
					}					
					else{
						if(seriesLabelsMap!=null){
							String serieLabel = (String)seriesLabelsMap.get(nameP);
							if(serieLabel == null){
								if(!hiddenSeries.contains(nameP)){
									logger.error("serie Label not found for serie with name "+nameP+ ": this may lead to errors, check if serie's name from dataset is equal to the one specified in template");
									logger.warn("series name in template are wrongly defined, remove series naming, check template");
									series.put(nameP, value);
									seriesLabelsMap = null;
								}
							}
							else{
								series.put(serieLabel, value);
								seriesCaptions.put(serieLabel, nameP);
								int i=0;
							}
						}
						else{
							logger.debug("SERIES_LABELS not specified: insert real serie's name");
							series.put(nameP, value);
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
					}
					else
						labelS = nameS;	



					//Line and second axis

					// LINE CASE
					if(!isHiddenSerie(nameS) && seriesDraw.get(nameS)!=null && (((String)seriesDraw.get(nameS)).equalsIgnoreCase("line") || ((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes"))){
						useLines=true;
						if(!seriesNames.contains(nameS))seriesNames.add(nameS);
						// SET THE AXIS
						if(seriesScale != null && seriesScale.get(nameS)!=null && ((String)seriesScale.get(nameS)).equalsIgnoreCase("2")){
							//set the nonShapes
							if(((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes") && !lineNoShapeSeries2.contains(nameS)){
								lineNoShapeSeries2.add(nameS);
							}							
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("2-line"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}
						else{
							if(((String)seriesDraw.get(nameS)).equalsIgnoreCase("line_no_shapes") && !lineNoShapeSeries1.contains(nameS)){
								lineNoShapeSeries1.add(nameS);
							}							
							((DefaultCategoryDataset)(datasetMap.getDatasets().get("1-line"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
						}

					}
					else if(!isHiddenSerie(nameS)){// BAR CASE
						useBars=true;
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


					// if there is an additional label are 
					if(additionalValues.get(nameS)!=null){
						String val=(String)additionalValues.get(nameS);
						String index=catValue+"-"+nameS;
						catSerLabels.put(index, val);
					}


				}

			}


		}
		if (listAtts.size() == 0){
			if (name.indexOf("$F{") >= 0){
				setTitleParameter("");
			}
			if (getSubName().indexOf("$F") >= 0){
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



		if(confParameters.get("stacked_bar_renderer_1")!=null){	
			String stacked=(String)confParameters.get("stacked_bar_renderer_1");
			if(stacked.equalsIgnoreCase("true")){
				stackedBarRenderer_1=true;
			}
		}
		if(confParameters.get("stacked_bar_renderer_2")!=null){	
			String stacked=(String)confParameters.get("stacked_bar_renderer_2");
			if(stacked.equalsIgnoreCase("true")){
				stackedBarRenderer_2=true;
			}
		}



		//reading series draw: there is specified if a serie has to be drawn as a bar or as a line.
		SourceBean draws = (SourceBean)content.getAttribute("SERIES_DRAW");
		if(draws==null){
			draws = (SourceBean)content.getAttribute("CONF.SERIES_DRAW");
		}
		seriesDraw=new LinkedHashMap();
		if(draws!=null){

			List atts=draws.getContainedAttributes();

			String serieName="";
			String serieDraw="";
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
		else{
			useBars=true;
		}

		if(confParameters.get(SECOND_AXIS_LABEL)!=null && !confParameters.get(SECOND_AXIS_LABEL).equals("")){	
			secondAxis=true;
			secondAxisLabel=(String)confParameters.get(SECOND_AXIS_LABEL);

			// only if second axis is defined check wich series has to be mapped to the first axis and wich to the second
			SourceBean scales = (SourceBean)content.getAttribute("SERIES_SCALES");
			if(scales==null){
				scales = (SourceBean)content.getAttribute("CONF.SERIES_SCALES");
			}
			seriesScale=new LinkedHashMap();
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

		}

		logger.debug("OUT");


	}




	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");


		// create the first renderer...


		CategoryPlot plot = new CategoryPlot();

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		NumberAxis rangeAxis = new NumberAxis(getValueLabel());
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		//		rangeAxis.setLowerBound(600);
		//		rangeAxis.setUpperBound(720);
		if(firstAxisLB != null && firstAxisUB != null){
			rangeAxis.setLowerBound(firstAxisLB);
			rangeAxis.setUpperBound(firstAxisUB);
		}

		rangeAxis.setUpperMargin(0.10);
		plot.setRangeAxis(0,rangeAxis);
		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}
		rangeAxis.setNumberFormatOverride(nf);

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

		DefaultCategoryDataset datasetLineFirstAxis=(DefaultCategoryDataset)datasets.getDatasets().get("1-line");
		DefaultCategoryDataset datasetBarFirstAxis=(DefaultCategoryDataset)datasets.getDatasets().get("1-bar");
		DefaultCategoryDataset datasetLineSecondAxis=(DefaultCategoryDataset)datasets.getDatasets().get("2-line");
		DefaultCategoryDataset datasetBarSecondAxis=(DefaultCategoryDataset)datasets.getDatasets().get("2-bar");

		//I create one bar renderer and one line
		MyStandardCategoryItemLabelGenerator generator=null;

		// value labels and additional values are mutually exclusive
		if(showValueLabels==true)additionalLabels=false;

		if(additionalLabels){
			generator = new MyStandardCategoryItemLabelGenerator(catSerLabels,"{1}", NumberFormat.getInstance());
		}

		if(useBars){

			CategoryItemRenderer barRenderer = null; 
			if(stackedBarRenderer_1 == true){
				barRenderer = new StackedBarRenderer();				
			}
			else{
				barRenderer = new BarRenderer();
			}


			CategoryItemRenderer barRenderer2 = new BarRenderer();

			if(stackedBarRenderer_2 == true){
				barRenderer2 = new StackedBarRenderer();				
			}
			else{
				barRenderer2 = new BarRenderer();
			}


			if(maxBarWidth!=null){
				((BarRenderer)barRenderer).setMaximumBarWidth(maxBarWidth.doubleValue());
				((BarRenderer)barRenderer2).setMaximumBarWidth(maxBarWidth.doubleValue());
			}

			if(showValueLabels){
				barRenderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				barRenderer.setBaseItemLabelsVisible(true);
				barRenderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				barRenderer.setBaseItemLabelPaint(styleValueLabels.getColor());

				//				barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
				//						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				//
				//				barRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
				//						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

				if (valueLabelsPosition.equalsIgnoreCase("inside")) {
					barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
					barRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
				} else {
					barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
					barRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				}

			}
			else if(additionalLabels){
				barRenderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
				barRenderer.setBaseItemLabelGenerator(generator);
				double orient=(-Math.PI / 2.0);
				if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
					orient=0.0;
				}

				barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));
				barRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));
				barRenderer.setBaseItemLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
				barRenderer.setBaseItemLabelPaint(defaultLabelsStyle.getColor());
				barRenderer.setBaseItemLabelsVisible(true);
			}


			if(showValueLabels){
				barRenderer2.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				barRenderer2.setBaseItemLabelsVisible(true);
				barRenderer2.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				barRenderer2.setBaseItemLabelPaint(styleValueLabels.getColor());

				//				barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
				//						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				//
				//				barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
				//						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));

				if (valueLabelsPosition.equalsIgnoreCase("inside")) {
					barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
					barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
				} else {
					barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
					barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
				}



			}
			else if(additionalLabels){
				barRenderer2.setBaseItemLabelGenerator(generator);
				double orient=(-Math.PI / 2.0);
				if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
					orient=0.0;
				}

				barRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));
				barRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));


				barRenderer2.setBaseItemLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
				barRenderer2.setBaseItemLabelPaint(defaultLabelsStyle.getColor());
				barRenderer2.setBaseItemLabelsVisible(true);

			}


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
						barRenderer.setSeriesPaint(index, color);
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
						/* test con un renderer
						if (idx > index){
							index = idx+1;
						}

						barRenderer.setSeriesPaint(index, color);*/
					}	
				}				
			}
			// add tooltip if enabled
			if(enableToolTips){
				MyCategoryToolTipGenerator generatorToolTip=new MyCategoryToolTipGenerator(freeToolTips, seriesTooltip, categoriesTooltip, seriesCaptions);
				barRenderer.setToolTipGenerator(generatorToolTip);
				barRenderer2.setToolTipGenerator(generatorToolTip);
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
			if(mycatUrl!=null && (!mycatUrl.getCategoryUrlLabel().equals("") || !mycatUrl.getSerieUrlLabel().equals(""))){
				barRenderer.setItemURLGenerator(mycatUrl);
				barRenderer2.setItemURLGenerator(mycatUrl);
			}

			plot.setDataset(2,datasetBarFirstAxis);
			plot.setDataset(3,datasetBarSecondAxis);

			plot.setRenderer(2,barRenderer);
			plot.setRenderer(3,barRenderer2);

		}

		if(useLines){

			LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
			LineAndShapeRenderer lineRenderer2 = new LineAndShapeRenderer();

			//lineRenderer.setShapesFilled(false);
			lineRenderer.setShapesFilled(true);
			lineRenderer2.setShapesFilled(true);

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
					lineRenderer.setSeriesShapesVisible(index, false);
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

			if(enableToolTips){
				MyCategoryToolTipGenerator generatorToolTip=new MyCategoryToolTipGenerator(freeToolTips, seriesTooltip, categoriesTooltip, seriesCaptions);
				lineRenderer.setToolTipGenerator(generatorToolTip);
				lineRenderer2.setToolTipGenerator(generatorToolTip);				
			}

			if(showValueLabels){
				lineRenderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				lineRenderer.setBaseItemLabelsVisible(true);
				lineRenderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.ITALIC, styleValueLabels.getSize()));
				lineRenderer.setBaseItemLabelPaint(styleValueLabels.getColor());

				lineRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_RIGHT));

				lineRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_RIGHT));

			}	
			else if(additionalLabels){
				lineRenderer.setBaseItemLabelGenerator(generator);
				lineRenderer.setBaseItemLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
				lineRenderer.setBaseItemLabelPaint(defaultLabelsStyle.getColor());
				lineRenderer.setBaseItemLabelsVisible(true);
			}

			if(showValueLabels){
				lineRenderer2.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
				lineRenderer2.setBaseItemLabelsVisible(true);
				lineRenderer2.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.ITALIC, styleValueLabels.getSize()));
				lineRenderer2.setBaseItemLabelPaint(styleValueLabels.getColor());

				lineRenderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_RIGHT));

				lineRenderer2.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_RIGHT));

			}	
			else if(additionalLabels){
				lineRenderer2.setBaseItemLabelGenerator(generator);
				lineRenderer2.setBaseItemLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
				lineRenderer2.setBaseItemLabelPaint(defaultLabelsStyle.getColor());
				lineRenderer2.setBaseItemLabelsVisible(true);
			}


			//			DefaultCategoryDataset datasetSecondAxis=(DefaultCategoryDataset)datasets.getDatasets().get("2");


			if(colorMap!=null){
				for (Iterator iterator = datasetLineSecondAxis.getRowKeys().iterator(); iterator.hasNext();) {
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
				for (Iterator iterator = datasetLineFirstAxis.getRowKeys().iterator(); iterator.hasNext();) {
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
						lineRenderer.setSeriesPaint(index, color);
					}	
				}

			}
			plot.setDataset(0,datasetLineFirstAxis);
			plot.setRenderer(0,lineRenderer);
			plot.setDataset(1,datasetLineSecondAxis);
			plot.setRenderer(1,lineRenderer2);

		}


		if(secondAxis){
			NumberAxis na=new NumberAxis(secondAxisLabel);
			na.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
			na.setLabelPaint(styleXaxesLabels.getColor());
			na.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
			na.setTickLabelPaint(styleXaxesLabels.getColor());
			na.setUpperMargin(0.10);
			na.setNumberFormatOverride(nf);
			//			rangeAxis.setLowerBound(270);
			//			rangeAxis.setUpperBound(340);
			if(secondAxisLB != null && secondAxisUB != null){
				rangeAxis.setLowerBound(secondAxisLB);
				rangeAxis.setUpperBound(secondAxisUB);
			}
			plot.setRangeAxis(1,na);
			plot.mapDatasetToRangeAxis(0, 0);
			plot.mapDatasetToRangeAxis(2, 0);
			plot.mapDatasetToRangeAxis(1, 1);
			plot.mapDatasetToRangeAxis(3, 1);
			if(rangeIntegerValues==true){
				na.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
			}

		}


		//plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.getDomainAxis().setCategoryLabelPositions(
				CategoryLabelPositions.UP_45);
		JFreeChart chart = new JFreeChart(plot);

		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
		chart.setBackgroundPaint(Color.white);

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
