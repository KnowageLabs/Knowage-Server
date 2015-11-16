/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

public class OverlaidStackedBarLine extends LinkableBar {


	HashMap seriesDraw=null;
	HashMap seriesCaptions=null;
	boolean additionalLabels=false;
	HashMap catSerLabels=null;
	boolean useBars=false;
	boolean useLines=false;

	boolean secondAxis=false;
	String secondAxisLabel=null;
	boolean freeToolTips=false;   //automatically set

	// maps the element with the tooltip information. tip_element or freetip_element
	HashMap<String, String> seriesTooltip=null; 
	HashMap<String, String> categoriesTooltip=null; 

	private static transient Logger logger=Logger.getLogger(OverlaidStackedBarLine.class);

	public DatasetMap calculateValue() throws Exception {

		seriesNames=new Vector();
		seriesCaptions=new LinkedHashMap();
		categoriesTooltip=new HashMap<String, String>();
		seriesTooltip=new HashMap<String, String>();
		// I must identify different series

		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new LinkedHashMap();

		//DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		DatasetMap datasetMap=new DatasetMap();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");


		// run all categories (one for each row)
		categoriesNumber=0;

		// new versione: two datasets one for bar and one for lines
		datasetMap.getDatasets().put("stackedbar", new DefaultCategoryDataset());
		datasetMap.getDatasets().put("line", new DefaultCategoryDataset());

		boolean first=true;
		//categories.put(new Integer(0), "All Categories");
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			//HashMap series=new HashMap();
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
			int contSer = 0;
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
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
							if((this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization()) && seriesDraw.get(nameP)!= null &&
									((String)seriesDraw.get(nameP)).equalsIgnoreCase("StackedBar")){
								String serieLabel = (String)seriesLabelsMap.get(nameP);
								series.put(serieLabel, value);
								seriesCaptions.put(serieLabel, nameP);
								contSer++;
							}
							else if(this.getNumberSerVisualization() == 0  
									|| 
									( seriesDraw.get(nameP)!= null &&
											((String)seriesDraw.get(nameP)).equalsIgnoreCase("line"))
							)
							{ //all series
								String serieLabel = (String)seriesLabelsMap.get(nameP);
								series.put(serieLabel, value);
								seriesCaptions.put(serieLabel, nameP);
							}
						}
						else if(this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization() &&
								((String)seriesDraw.get(nameP)).equalsIgnoreCase("StackedBar")){
							series.put(nameP, value);
							contSer++;
						}
						else if(this.getNumberSerVisualization() == 0  || ((String)seriesDraw.get(nameP)).equalsIgnoreCase("line")){ //all series
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

					// if to draw as a line
					if(seriesDraw.get(nameS)!=null && ((String)seriesDraw.get(nameS.toUpperCase())).equalsIgnoreCase("line")){
						if(!seriesNames.contains(nameS.toUpperCase()))seriesNames.add(nameS.toUpperCase());
						((DefaultCategoryDataset)(datasetMap.getDatasets().get("line"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);
					}
					else{ // if to draw as a bar
						if(!seriesNames.contains(nameS.toUpperCase()))seriesNames.add(nameS.toUpperCase());
						((DefaultCategoryDataset)(datasetMap.getDatasets().get("stackedbar"))).addValue(valueD!=null ? valueD.doubleValue() : null, labelS, catValue);

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
		return datasetMap;


	}

	public void configureChart(SourceBean content) {
		super.configureChart(content);

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


		//reading series colors if present
		SourceBean draws = (SourceBean)content.getAttribute("SERIES_DRAW");

		if(draws == null) {
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
					seriesDraw.put(serieName.toUpperCase(), "line");
					useLines=true;
				}
				else{
					seriesDraw.put(serieName.toUpperCase(), "stackedbar");					
					useBars=true;
				}

			}		

		}
		else{
			useBars=true;
		}

		if(confParameters.get("second_axis_label")!=null){	
			secondAxis=true;
			secondAxisLabel=(String)confParameters.get("second_axis_label");
		}




	}

	public JFreeChart createChart(DatasetMap datasets) {

		// create the first renderer...


		CategoryPlot plot = new CategoryPlot();
		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		NumberAxis rangeAxis = new NumberAxis(getValueLabel());
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setNumberFormatOverride(nf);		
		plot.setRangeAxis(rangeAxis);
		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}

		CategoryAxis domainAxis = new CategoryAxis(getCategoryLabel());
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
		plot.setDomainAxis(domainAxis);

		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);

		DefaultCategoryDataset datasetBar=(DefaultCategoryDataset)datasets.getDatasets().get("stackedbar");


		//I create one bar renderer and one line



		MyStandardCategoryItemLabelGenerator generator=null;
		if(additionalLabels){
			generator = new MyStandardCategoryItemLabelGenerator(catSerLabels,"{1}", NumberFormat.getInstance());}

		if(useBars){
			CategoryItemRenderer barRenderer = new StackedBarRenderer();

			if(maxBarWidth!=null){
				((BarRenderer)barRenderer).setMaximumBarWidth(maxBarWidth.doubleValue());
			}

			if(additionalLabels){
				barRenderer.setBaseItemLabelGenerator(generator);
				double orient=(-Math.PI / 2.0);
				if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
					orient=0.0;
				}
				barRenderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				barRenderer.setBaseItemLabelPaint(styleValueLabels.getColor());
				barRenderer.setBaseItemLabelsVisible(true);
				barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));
				barRenderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
						orient));	

			}



			if(colorMap!=null){
				for (Iterator iterator = datasetBar.getRowKeys().iterator(); iterator.hasNext();) {
					String serName = (String) iterator.next();
					String labelName = "";
					int index=-1;
					if (seriesCaptions != null && seriesCaptions.size()>0){
						labelName = serName;
						serName = (String)seriesCaptions.get(serName);
						index=datasetBar.getRowIndex(labelName);
					}
					else
						index=datasetBar.getRowIndex(serName);

					Color color=(Color)colorMap.get(serName);
					if(color!=null){
						barRenderer.setSeriesPaint(index, color);
					}	
				}
			}
			// add tooltip if enabled
			if(enableToolTips){
				MyCategoryToolTipGenerator generatorToolTip=new MyCategoryToolTipGenerator(freeToolTips, seriesTooltip, categoriesTooltip, seriesCaptions);
				barRenderer.setToolTipGenerator(generatorToolTip);
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
				barRenderer.setItemURLGenerator(mycatUrl);
			}

			plot.setDataset(1,datasetBar);
			plot.setRenderer(1,barRenderer);

		}

		if(useLines){

			LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
			//lineRenderer.setShapesFilled(false);
			lineRenderer.setShapesFilled(true);
			if(additionalLabels){lineRenderer.setBaseItemLabelGenerator(generator);
			lineRenderer.setBaseItemLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
			lineRenderer.setBaseItemLabelPaint(defaultLabelsStyle.getColor());
			lineRenderer.setBaseItemLabelsVisible(true);
			}

			DefaultCategoryDataset datasetLine=(DefaultCategoryDataset)datasets.getDatasets().get("line");

			if(enableToolTips){
				MyCategoryToolTipGenerator generatorToolTip=new MyCategoryToolTipGenerator(freeToolTips, seriesTooltip, categoriesTooltip, seriesCaptions);
				lineRenderer.setToolTipGenerator(generatorToolTip);				
			}

			if(colorMap!=null){
				for (Iterator iterator = datasetLine.getRowKeys().iterator(); iterator.hasNext();) {
					String serName = (String) iterator.next();
					String labelName = "";
					int index=-1;

					if (seriesCaptions != null && seriesCaptions.size()>0){
						labelName = serName;
						serName = (String)seriesCaptions.get(serName);
						index=datasetLine.getRowIndex(labelName);
					}
					else
						index=datasetLine.getRowIndex(serName);

					Color color=(Color)colorMap.get(serName);
					if(color!=null){
						lineRenderer.setSeriesPaint(index, color);
					}	
				}
			}
			plot.setDataset(0,datasetLine);
			plot.setRenderer(0,lineRenderer);
		}


		if(secondAxis){
			NumberAxis na=new NumberAxis(secondAxisLabel);
			na.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
			na.setLabelPaint(styleXaxesLabels.getColor());
			na.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
			na.setTickLabelPaint(styleXaxesLabels.getColor());
			na.setUpperMargin(0.10);
			if(rangeIntegerValues==true){
				na.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
			}
			na.setNumberFormatOverride(nf);			
			plot.setRangeAxis(1,na);
			plot.mapDatasetToRangeAxis(0, 1);
		}



		//plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		plot.getDomainAxis().setCategoryLabelPositions(
				CategoryLabelPositions.UP_45);
		JFreeChart chart = new JFreeChart(plot);
		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		chart.setBackgroundPaint(Color.white);

		if(legend==true) drawLegend(chart);
		return chart;



	}


}
