/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyCategoryUrlGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


public class StackedBar extends BarCharts implements ILinkableChart {

	String rootUrl=null;
	String mode="";
	String drillLabel="";
	HashMap<String, DrillParameter> drillParametersMap=null;
	String categoryUrlName="";
	String serieUrlname="";

	boolean cumulative=false;
	HashMap colorMap=null;  // keeps user selected colors
	boolean additionalLabels=false;
	boolean percentageValue=false;
	boolean makePercentage=false;
	HashMap catSerLabels=null;
	String drillDocTitle = null;
	String target = "self";

	boolean horizontalView=false; //false is vertical, true is horizontal
	boolean horizontalViewConfigured=false;


	private static transient Logger logger=Logger.getLogger(StackedBar.class);


	/** If adding the cumulative serie */
	public static final String CUMULATIVE = "cumulative";
	/** If draw additional labels */
	public static final String ADD_LABELS = "add_labels";
	/** If adding the cumulative serie */
	public static final String MAKE_PERCENTAGE = "make_percentage";
	/** if percentage value */
	public static final String PERCENTAGE_VALUE = "percentage_value";
	/** Orientation of the chart: horizontal, vertical */
	public static final String ORIENTATION = "orientation";




	/**
	 * Override this functions from BarCharts beacuse I want the hidden serie to be the first!
	 * 
	 * @return the dataset
	 * 
	 * @throws Exception the exception
	 */

	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new HashMap();

		double cumulativeValue=0.0;

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");


		// run all categories (one for each row)
		categoriesNumber=0;
		seriesNames=new Vector();
		catGroupNames=new Vector();
		if(filterCatGroups==true){
			catGroups=new HashMap();
		}

		//categories.put(new Integer(0), "All Categories");
		boolean first=true;
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			HashMap series=new HashMap();
			HashMap additionalValues=new HashMap();
			String catValue="";
			String cat_group_name="";

			String nameP="";
			String value="";

			ArrayList orderSeries=new ArrayList();

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
				else if(nameP.equalsIgnoreCase("cat_group")){
					cat_group_name=value;
				}
				else {
					nameP = nameP.toUpperCase();
					if(nameP.startsWith("ADD_")){
						if(additionalLabels){
							String ind=nameP.substring(4);							
							additionalValues.put(ind, value);
						}
					}
					else if (this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization()){

						String serieName = nameP;
						if(seriesLabelsMap != null && seriesLabelsMap.keySet().contains(nameP)){
							serieName = seriesLabelsMap.get(nameP).toString();
						}
						series.put(serieName, value);
						orderSeries.add(serieName);
						contSer++;
						
					}
					else if (this.getNumberSerVisualization() == 0){
						String serieName = nameP;
						if(seriesLabelsMap != null && seriesLabelsMap.keySet().contains(nameP)){
							serieName = seriesLabelsMap.get(nameP).toString();
						}
						series.put(serieName, value);
						orderSeries.add(serieName);
					}

					
					// for now I make like if addition value is checked he seek for an attribute with name with value+name_serie
				}
			}

			// if a category group was found add it
			if(!cat_group_name.equalsIgnoreCase("") && !catValue.equalsIgnoreCase("") && catGroups!=null)
			{	
				catGroups.put(catValue, cat_group_name);
				if(!(catGroupNames.contains(cat_group_name))){
					catGroupNames.add(cat_group_name);}
			}


			// if it is cumulative automatically get the vamount value
			if(cumulative){
				dataset.addValue(cumulativeValue, "CUMULATIVE", catValue);
			}

			// if there is an hidden serie put that one first!!! if it is not cumulative
			/*if(serieHidden!=null && !this.cumulative && !serieHidden.equalsIgnoreCase("")){
				String valueS=(String)series.get(serieHidden);
				dataset.addValue(Double.valueOf(valueS).doubleValue(), serieHidden, catValue);
				if(!seriesNames.contains(serieHidden)){
					seriesNames.add(serieHidden);
				}				
			}*/


			for (Iterator iterator3 = orderSeries.iterator(); iterator3.hasNext();) {
				String nameS = (String) iterator3.next();
				if(!hiddenSeries.contains(nameS)){
					String valueS=((String)series.get(nameS)).equalsIgnoreCase("null")?"0":(String)series.get(nameS);
					Double valueD=null;
					try{
						valueD=Double.valueOf(valueS);
					}
					catch (Exception e) {
						logger.warn("error in double conversion, put default to null");
						valueD=null;
					}

					
					// check if serie must be rinominated!
//					String serieName = nameS;
//					if(seriesLabelsMap != null && seriesLabelsMap.keySet().contains(nameS)){
//						serieName = seriesLabelsMap.get(nameS).toString();
//					}
					
					
					dataset.addValue(valueD!=null ? valueD.doubleValue() : null, nameS, catValue);
					cumulativeValue+=valueD!=null ? valueD.doubleValue() : 0.0;
					if(!seriesNames.contains(nameS)){
						seriesNames.add(nameS);
					}
					// if there is an additional label are 
					if(additionalValues.get(nameS)!=null){
						String val=(String)additionalValues.get(nameS);
						String index=catValue+"-"+nameS;						
						//String totalVal = valueS;
						String totalVal=val;						
						//if (percentageValue) totalVal += "%";
						//totalVal += "\n" + val;
						catSerLabels.put(index, totalVal);
					}

				}

			}
			// Check additional Values for CUmulative
			if(additionalValues.get("CUMULATIVE")!=null){
				String val=(String)additionalValues.get("CUMULATIVE");
				String index=catValue+"-"+"CUMULATIVE";						
				catSerLabels.put(index, val);	
			}



		}
		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		return datasets;
	}




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


		if(confParameters.get(CUMULATIVE)!=null){	
			String orientation=(String)confParameters.get(CUMULATIVE);
			if(orientation.equalsIgnoreCase("true")){
				cumulative=true;
			}
			else {
				cumulative=false;
			}
		}

		if(confParameters.get(ADD_LABELS)!=null){	
			String additional=(String)confParameters.get(ADD_LABELS);
			if(additional.equalsIgnoreCase("true")){
				additionalLabels=true;
				catSerLabels=new HashMap();
			}
			else additionalLabels=false;
		}
		else
		{
			additionalLabels=false;
		}

		if(confParameters.get(MAKE_PERCENTAGE)!=null){	
			String perc=(String)confParameters.get(MAKE_PERCENTAGE);
			if(perc.equalsIgnoreCase("true")){
				makePercentage=true;
			}
			else makePercentage=false;
		}
		else
		{
			makePercentage=false;
		}

		if(confParameters.get(PERCENTAGE_VALUE)!=null){	
			String perc=(String)confParameters.get(PERCENTAGE_VALUE);
			if(perc.equalsIgnoreCase("true")){
				percentageValue=true;
			}
			else percentageValue=false;
		}
		else
		{
			percentageValue=false;
		}

		SourceBean drillSB = (SourceBean)content.getAttribute("DRILL");
		if(drillSB==null){
			drillSB = (SourceBean)content.getAttribute("CONF.DRILL");
		}
		if(drillSB!=null){
			String lab=(String)drillSB.getAttribute("document");
			if(lab!=null) drillLabel=lab;
			else{
				logger.error("Drill label not found");
			}

			List parameters =drillSB.getAttributeAsList("PARAM");
			if(parameters!=null){
				drillParametersMap=new HashMap<String, DrillParameter>();

				for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
					SourceBean att = (SourceBean) iterator.next();
					String name=(String)att.getAttribute("name");
					String type=(String)att.getAttribute("type");
					String value=(String)att.getAttribute("value");

					// default is relative
					if(type!=null && type.equalsIgnoreCase("absolute"))
						type="absolute";
					else
						type="relative";

//					if(type!=null && type.equalsIgnoreCase("RELATIVE")){ // Case relative
//					if(value.equalsIgnoreCase("serie"))serieUrlname=name;				// ?????????????'''
//					if(value.equalsIgnoreCase("category"))categoryUrlName=name;
//					}

//					else{												// Case absolute
//					drillParameter.put(name, value);
//					}


					if(name.equalsIgnoreCase("seriesurlname"))serieUrlname=value;
					else if(name.equalsIgnoreCase("target")){
						if(value!=null && value.equalsIgnoreCase("tab")){
							setTarget("tab");
						}else{
							setTarget("self");
						}
					}else if(name.equalsIgnoreCase("title")){
						if(value!=null && !value.equals("")){
							setDrillDocTitle(value);
						}
					}
					else if(name.equalsIgnoreCase("categoryurlname"))categoryUrlName=value;
					else{
						if(this.getParametersObject().get(name)!=null){
							value=(String)getParametersObject().get(name);
						}

						DrillParameter drillPar=new DrillParameter(name,type,value);
						drillParametersMap.put(name, drillPar);
					}


				}
			}
		}
		//reading series colors if present
		SourceBean colors = (SourceBean)content.getAttribute("SERIES_COLORS");
		if(colors==null){
			colors = (SourceBean)content.getAttribute("CONF.SERIES_COLORS");
		}
		if(colors!=null){
			colorMap=new HashMap();
			List atts=colors.getContainedAttributes();
			String colorSerie="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

				String serieName=new String(object.getKey());
				// I put the serieName if rinominated
				String nameRinominated = (seriesLabelsMap != null && seriesLabelsMap.containsKey(serieName)) ? seriesLabelsMap.get(serieName).toString() : serieName;
				
				colorSerie=new String((String)object.getValue());
				Color col=new Color(Integer.decode(colorSerie).intValue());
				if(col!=null){
					colorMap.put(nameRinominated,col); 
				}
			}		

		}
		logger.debug("OUT");	
	}




	/**
	 * Inherited by IChart.
	 * 
	 * @param chartTitle the chart title
	 * @param dataset the dataset
	 * 
	 * @return the j free chart
	 */



	public JFreeChart createChart(DatasetMap datasets) {



		logger.debug("IN");
		CategoryDataset dataset=(CategoryDataset)datasets.getDatasets().get("1");

		logger.debug("Taken Dataset");


		logger.debug("Get plot orientaton");
		PlotOrientation plotOrientation=PlotOrientation.VERTICAL;
		if(horizontalView)
		{
			plotOrientation=PlotOrientation.HORIZONTAL;
		}


		logger.debug("Call Chart Creation");
		JFreeChart chart = ChartFactory.createStackedBarChart(
				name,  // chart title
				categoryLabel,                  // domain axis label
				valueLabel,                     // range axis label
				dataset,                     // data
				plotOrientation,    // the plot orientation
				false,                        // legend
				true,                        // tooltips
				false                        // urls
		);
		logger.debug("Chart Created");

		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(color);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);


		logger.debug("set renderer");
		StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		renderer.setBaseItemLabelsVisible(true);

		if (percentageValue)
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##.#%")));
		else if(makePercentage)
			renderer.setRenderAsPercentages(true);

		/*
		else
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		 */
		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());

		if(maxBarWidth!=null){
			renderer.setMaximumBarWidth(maxBarWidth.doubleValue());
		}


		boolean document_composition=false;
		if(mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))document_composition=true;

		logger.debug("Calling Url Generation");

		MyCategoryUrlGenerator mycatUrl=null;
		if(rootUrl!=null){
			logger.debug("Set MycatUrl");
			mycatUrl=new MyCategoryUrlGenerator(rootUrl);

			mycatUrl.setDocument_composition(document_composition);
			mycatUrl.setCategoryUrlLabel(categoryUrlName);
			mycatUrl.setSerieUrlLabel(serieUrlname);
		}
		if(mycatUrl!=null)
			renderer.setItemURLGenerator(mycatUrl);

		logger.debug("Text Title");

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		logger.debug("Style Labels");

		Color colorSubInvisibleTitle=Color.decode("#FFFFFF");
		StyleLabel styleSubSubTitle=new StyleLabel("Arial",12,colorSubInvisibleTitle);
		TextTitle subsubTitle =setStyleTitle("", styleSubSubTitle);
		chart.addSubtitle(subsubTitle);
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(color);


		logger.debug("Axis creation");
		// set the range axis to display integers only...

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		if(makePercentage)
			rangeAxis.setNumberFormatOverride(NumberFormat.getPercentInstance());
		else
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}

		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setNumberFormatOverride(nf);

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

		renderer.setDrawBarOutline(false);

		logger.debug("Set series color");

		int seriesN=dataset.getRowCount();
		if(orderColorVector != null && orderColorVector.size()>0){
			logger.debug("color serie by SERIES_ORDER_COLORS template specification");
			for (int i = 0; i < seriesN; i++) {
				if( orderColorVector.get(i)!= null){
					Color color = orderColorVector.get(i);
					renderer.setSeriesPaint(i, color);
				}		
			}	
		}		
		else 		
		if(colorMap!=null){
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getRowKey(i);
				
				// if serie has been rinominated I must search with the new name!
				String nameToSearchWith = (seriesLabelsMap != null && seriesLabelsMap.containsKey(serieName)) ? seriesLabelsMap.get(serieName).toString() : serieName;
				
				Color color=(Color)colorMap.get(nameToSearchWith);
				if(color!=null){
					renderer.setSeriesPaint(i, color);
					renderer.setSeriesItemLabelFont(i, new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
				}	
			}
		}

		logger.debug("If cumulative set series paint "+cumulative);

		if(cumulative){
			int row=dataset.getRowIndex("CUMULATIVE");
			if(row!=-1){
				if(color!=null)
					renderer.setSeriesPaint(row, color);
				else
					renderer.setSeriesPaint(row, Color.WHITE);
			}
		}


		MyStandardCategoryItemLabelGenerator generator=null;
		logger.debug("Are there addition labels "+additionalLabels);
		logger.debug("Are there value labels "+showValueLabels);

		if(showValueLabels){
			renderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());

			if (valueLabelsPosition.equalsIgnoreCase("inside")) {
				renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
				renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.CENTER, TextAnchor.BASELINE_LEFT));
			} else {
				renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_LEFT));
				renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
						ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_LEFT));
			}
		}
		else if(additionalLabels){

			generator = new MyStandardCategoryItemLabelGenerator(catSerLabels,"{1}", NumberFormat.getInstance());
			logger.debug("generator set");

			double orient=(-Math.PI / 2.0);
			logger.debug("add labels style");
			if(styleValueLabels.getOrientation()!= null && styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
				orient=0.0;
			}
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());

			logger.debug("add labels style set");

			renderer.setBaseItemLabelGenerator(generator);
			renderer.setBaseItemLabelsVisible(true);
			//vertical labels 			
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER, TextAnchor.CENTER, 
					orient));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER, TextAnchor.CENTER, 
					orient));

			logger.debug("end of add labels ");


		}

		logger.debug("domain axis");

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
				CategoryLabelPositions.createUpRotationLabelPositions(
						Math.PI / 4.0));
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
		//opacizzazione colori
		if(!cumulative) plot.setForegroundAlpha(0.6f);
		if(legend==true) drawLegend(chart);

		logger.debug("OUT");
		return chart;

	}



	/**
	 * Gets document parameters and return a string in the form &param1=value1&param2=value2 ...
	 * 
	 * @param drillParameters the drill parameters
	 * 
	 * @return the document_ parameters
	 */

	public String getDocument_Parameters(HashMap<String, DrillParameter> drillParametersMap) {
		String document_parameter="";
		if (drillParametersMap != null){
			for (Iterator iterator = drillParametersMap.keySet().iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				DrillParameter drillPar=drillParametersMap.get(name);
				String value=drillPar.getValue();
				if(name!=null && !name.equals("") && value!=null && !value.equals("")){
					document_parameter+="%26"+name+"%3D"+value;
					//document_parameter+="&"+name+"="+value;
				}

			}
		}
		return document_parameter;
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getRootUrl()
	 */
	public String getRootUrl() {
		return rootUrl;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setRootUrl(java.lang.String)
	 */
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isLinkable()
	 */
	public boolean isLinkable(){
		return true;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getMode()
	 */
	public String getMode() {
		return mode;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setMode(java.lang.String)
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getDrillLabel()
	 */
	public String getDrillLabel() {
		return drillLabel;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setDrillLabel(java.lang.String)
	 */
	public void setDrillLabel(String drillLabel) {
		this.drillLabel = drillLabel;
	}





	public HashMap<String, DrillParameter> getDrillParametersMap() {
		return drillParametersMap;
	}




	public void setDrillParametersMap(
			HashMap<String, DrillParameter> drillParametersMap) {
		this.drillParametersMap = drillParametersMap;
	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getCategoryUrlName()
	 */
	public String getCategoryUrlName() {
		return categoryUrlName;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setCategoryUrlName(java.lang.String)
	 */
	public void setCategoryUrlName(String categoryUrlName) {
		this.categoryUrlName = categoryUrlName;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getSerieUrlname()
	 */
	public String getSerieUrlname() {
		return serieUrlname;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setSerieUrlname(java.lang.String)
	 */
	public void setSerieUrlname(String serieUrlname) {
		this.serieUrlname = serieUrlname;
	}




	public String getDrillDocTitle() {
		return drillDocTitle;
	}




	public void setDrillDocTitle(String drillDocTitle) {
		this.drillDocTitle = drillDocTitle;
	}




	public String getTarget() {
		return target;
	}




	public void setTarget(String target) {
		this.target = target;
	}



}
