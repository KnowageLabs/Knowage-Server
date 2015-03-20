/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.TextAnchor;

/**   @author Antonella Giachino
 *     antonella.giachino@eng.it
 */


public class StackedBarGroup extends BarCharts {//implements ILinkableChart {

	String rootUrl=null;
	String mode="";
	String drillLabel="";
	HashMap<String, DrillParameter> drillParametersMap=null;
	String categoryUrlName="";
	List categoryNames;
	int subCategoriesNumber=0;
	int realCatNumber = 0;
	int realSubCatNumber = 0;
	HashMap subCategories;
	List subCategoryNames;
	String subCategoryLabel = "";
	String serieUrlname="";
	Integer numSerieForGroup;
	Integer numGroups;

	HashMap colorMap=null;  // keeps user selected colors
	HashMap subCatLabelsMap=null;  // keeps user selected labels fot subcategories
	boolean additionalLabels=false;
	boolean percentageValue=false;
	HashMap catSerLabels=null;
	HashMap gradientMap=null;  // keeps user selected last gradient colors

	boolean horizontalView=false; //false is vertical, true is horizontal
	boolean horizontalViewConfigured=false;

	
	private static transient Logger logger=Logger.getLogger(StackedBarGroup.class);


	/** subcategory_label*/
	public static final String SUBCATEGORY_LABEL = "subcategory_label";
	/** If draw additional labels */
	public static final String ADD_LABELS = "add_labels";
	/** Number of series for group*/
	public static final String N_SERIE_FOR_GROUP = "n_serie_for_group";
	/** number of groups */
	public static final String N_GROUPS = "n_groups";
	/** percentage value */
	public static final String PERCENTAGE_VALUE = "percentage_value";
	/** Orientation of the chart: horizontal, vertical */
	public static final String ORIENTATION = "orientation";



	/**
	 * Override this functions from BarCharts beacuse I manage a group of stacked bar!
	 * 
	 * @return the dataset
	 * 
	 * @throws Exception the exception
	 */

	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new LinkedHashMap();
		subCategories=new LinkedHashMap();
		subCategoryNames=new ArrayList();
		categoryNames=new ArrayList();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");


		// run all categories (one for each row)
		categoriesNumber=0;
		seriesNames=new Vector();
		//categories.put(new Integer(0), "All Categories");
		boolean first=true;
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			HashMap myseries=new LinkedHashMap();
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
					categoriesNumber++;
					categories.put(new Integer(categoriesNumber),value);
					if (!categoryNames.contains(value)){
						categoryNames.add(value);
						realCatNumber++;
					}
				}
				else if(nameP.equalsIgnoreCase("x2"))
				{
					subCategoriesNumber++;
					subCategories.put(new Integer(subCategoriesNumber),value);
					if (!subCategoryNames.contains(value)){
						subCategoryNames.add(value);
						realSubCatNumber++;
					}
				}
				else {
					if(nameP.startsWith("add_") || nameP.startsWith("ADD_")){
						if(additionalLabels){
							String ind=nameP.substring(4);							
							additionalValues.put(ind, value);
						}
					}
					else {
						if (this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization()){

							myseries.put(nameP, value);
							contSer++;
						}
						else if (this.getNumberSerVisualization() == 0 ) 
							myseries.put(nameP, value);
					}

					// for now I make like if addition value is checked he seek for an attribute with name with value+name_serie
				}
			}

			for (Iterator iterator3 = myseries.keySet().iterator(); iterator3.hasNext();) {
				String nameS = (String) iterator3.next();
				if(!hiddenSeries.contains(nameS)){
					String valueS=(String)myseries.get(nameS);
					Double valueD=null;
					try{
						valueD=Double.valueOf(valueS);
					}
					catch (Exception e) {
						logger.warn("error in double conversion, put default to null");
						valueD=null;
					}

					String subcat = (String)subCategoryNames.get(realSubCatNumber-1);
					//dataset.addValue(Double.valueOf(valueS).doubleValue(), value, catValue);
					dataset.addValue(valueD!=null ? valueD.doubleValue() : null, subcat, catValue);
					//	System.out.println("dataset.addValue("+Double.valueOf(valueS).doubleValue()+ ", '"+subcat+"'"+",'"+catValue+"');");
					if(!seriesNames.contains(nameS)){ 
						seriesNames.add(nameS);
					}
					// if there is an additional label are 
					if(additionalValues.get(nameS)!=null){
						String val=(String)additionalValues.get(nameS);
						String index=catValue+"-"+nameS;						
						String totalVal=val;						
						catSerLabels.put(index, totalVal);
					}

				}

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

		
		if(confParameters.get(SUBCATEGORY_LABEL)!=null){	
			subCategoryLabel=(String)confParameters.get(SUBCATEGORY_LABEL);
		}
		else
		{
			subCategoryLabel="";
		}

		if(confParameters.get(ADD_LABELS)!=null){	
			String additional=(String)confParameters.get(ADD_LABELS);
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

		if(confParameters.get("percentage_value")!=null){	
			String perc=(String)confParameters.get("percentage_value");
			if(perc.equalsIgnoreCase("true")){
				percentageValue=true;
			}
			else percentageValue=false;
		}
		else
		{
			percentageValue=false;
		}

		if(confParameters.get("n_serie_for_group")!=null){	
			numSerieForGroup=Integer.valueOf((String)confParameters.get("n_serie_for_group"));
		}
		else
		{
			numSerieForGroup=new Integer("1");
		}

		if(confParameters.get("n_groups")!=null){	
			numGroups=Integer.valueOf((String)confParameters.get("n_groups"));
		}
		else
		{
			numGroups=new Integer("1");
		}


		/*
		SourceBean drillSB = (SourceBean)content.getAttribute("CONF.DRILL");
		if(drillSB!=null){
			String lab=(String)drillSB.getAttribute("document");
			if(lab!=null) drillLabel=lab;
			else{
				logger.error("Drill label not found");
			}

			List parameters =drillSB.getAttributeAsList("PARAM");
			if(parameters!=null){
				drillParameter=new HashMap();	

				for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
					SourceBean att = (SourceBean) iterator.next();
					String name=(String)att.getAttribute("name");
					String type=(String)att.getAttribute("type");
					String value=(String)att.getAttribute("value");

					if(type!=null && type.equalsIgnoreCase("RELATIVE")){ // Case relative
						if(value.equalsIgnoreCase("serie"))serieUrlname=name;
						if(value.equalsIgnoreCase("category"))categoryUrlName=name;
					}
					else{												// Case absolute
						drillParameter.put(name, value);
					}
				}
			}
		}
		 */

		//reading series colors if present
		SourceBean colors = (SourceBean)content.getAttribute("SERIES_COLORS");
		if(colors==null){
			colors = (SourceBean)content.getAttribute("CONF.SERIES_COLORS");
		}
		if(colors!=null){
			colorMap=new LinkedHashMap();
			List atts=colors.getContainedAttributes();
			String colorSerie="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

				String serieName=new String(object.getKey());
				colorSerie=new String((String)object.getValue());
				Color col=new Color(Integer.decode(colorSerie).intValue());
				if(col!=null){
					colorMap.put(serieName,col); 
				}
			}		
		}
		//reading gradient colors if present
		SourceBean gradients = (SourceBean)content.getAttribute("GRADIENTS_COLORS");
		if(gradients==null){
			gradients = (SourceBean)content.getAttribute("CONF.GRADIENTS_COLORS");
		}
		if(gradients!=null){
			gradientMap=new LinkedHashMap();
			List atts=gradients.getContainedAttributes();
			String gradientSerie="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

				String serieName=new String(object.getKey());
				gradientSerie=new String((String)object.getValue());
				Color col=new Color(Integer.decode(gradientSerie).intValue());
				if(col!=null){
					gradientMap.put(serieName,col); 
				}
			}		
		}
		//reading subcategories labels
		SourceBean subcatLabels = (SourceBean)content.getAttribute("SUBCATEGORY_LABELS");
		if(subcatLabels==null){
			subcatLabels = (SourceBean)content.getAttribute("CONF.SUBCATEGORY_LABELS");
		}
		if(subcatLabels!=null){
			subCatLabelsMap=new LinkedHashMap();
			List atts=subcatLabels.getContainedAttributes();
			String label="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

				String subcatName=new String(object.getKey());
				label=new String((String)object.getValue());
				if(label!=null){
					subCatLabelsMap.put(subcatName,label); 
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

		logger.debug("Get plot orientaton");
		PlotOrientation plotOrientation=PlotOrientation.VERTICAL;
		if(horizontalView)
		{
			plotOrientation=PlotOrientation.HORIZONTAL;
		}
		
		

		JFreeChart chart = ChartFactory.createStackedBarChart(
				name,  							// chart title
				categoryLabel,                  // domain axis label
				valueLabel,                     // range axis label
				dataset,                     	// data
				plotOrientation,    	// the plot orientation
				legend,                        	// legend
				true,                        	// tooltips
				false                        	// urls
		);

		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(color);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);

		GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
		KeyToGroupMap map = new KeyToGroupMap("G1");
		int numElForGroup = 0;
		for (int idx=0; idx < numGroups.intValue(); idx++){
			for (int j=0; j < numSerieForGroup.intValue(); j++ ){
				try{
					String tmpSubCat = (String)subCategoryNames.get(j+idx*numSerieForGroup.intValue());
					map.mapKeyToGroup(tmpSubCat, "G"+(idx+1));

				}
				catch (Exception e) {
					logger.error("out of range error in inserting in stacked bar group: continue anayway", e);
				}
			}
		}

		renderer.setSeriesToGroupMap(map);
		renderer.setItemMargin(0.0);
		renderer.setDrawBarOutline(false);
		renderer.setBaseItemLabelsVisible(true);
		if (percentageValue)
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##.#%")));
		else
			renderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());

		if(maxBarWidth!=null){
			renderer.setMaximumBarWidth(maxBarWidth.doubleValue());
		}

		boolean document_composition=false;
		if(mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))document_composition=true;

		/*
		MyCategoryUrlGenerator mycatUrl=new MyCategoryUrlGenerator(rootUrl);
		mycatUrl.setDocument_composition(document_composition);
		mycatUrl.setCategoryUrlLabel(categoryUrlName);
		mycatUrl.setSerieUrlLabel(serieUrlname);

		renderer.setItemURLGenerator(mycatUrl);
		 */

		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(color);

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setNumberFormatOverride(nf);
		if(rangeIntegerValues==true){
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());	
		}
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


		int seriesN=dataset.getRowCount();
		int numSerieColored = 0;
		
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
			while (numSerieColored < seriesN){
				for (int i=1; i <= colorMap.size();i++){
					Color color=(Color)colorMap.get("SER"+i);
					Color gradient=new Color(Integer.decode("#FFFFFF").intValue());
					if (gradientMap != null)
						gradient=(Color)gradientMap.get("SER"+i);

					if(color!=null){
						Paint p = new GradientPaint(
								0.0f, 0.0f, color, 0.0f, 0.0f, gradient);

						//renderer.setSeriesPaint(numSerieColored, color);
						renderer.setSeriesPaint(numSerieColored, p);
					}
					numSerieColored++;
				}
			}
		}
		renderer.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL));

		MyStandardCategoryItemLabelGenerator generator=null;
		if(additionalLabels){
			generator = new MyStandardCategoryItemLabelGenerator(catSerLabels,"{1}", NumberFormat.getInstance());

			double orient=(-Math.PI / 2.0);
			if(styleValueLabels.getOrientation().equalsIgnoreCase("horizontal")){
				orient=0.0;
			}
			renderer.setBaseItemLabelFont(new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());

			renderer.setBaseItemLabelGenerator(generator);
			renderer.setBaseItemLabelsVisible(true);
			//vertical labels 			
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
					orient));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 
					orient));

			//horizontal labels
			/*
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
	                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
	                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
			 */

		}

		SubCategoryAxis domainAxis = new SubCategoryAxis(categoryLabel + " / " + subCategoryLabel);    
		String subCatLabel = "";
		for (int j=1; j <= numGroups.intValue(); j++ ){
			if(subCatLabelsMap!=null)
				subCatLabel=(String)subCatLabelsMap.get("CAT"+j);
			else
				subCatLabel = subCategoryLabel;

			domainAxis.addSubCategory(subCatLabel);
			domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
			domainAxis.setLabelPaint(styleYaxesLabels.getColor());
			domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
			domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());
		}
		plot.setDomainAxis(domainAxis);
		plot.setRenderer(renderer);


		/*
		domainAxis.setCategoryLabelPositions(
				CategoryLabelPositions.createUpRotationLabelPositions(
						Math.PI / 6.0));
		 */
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
		for (Iterator iterator = drillParametersMap.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			DrillParameter drillParameter=drillParametersMap.get(name);
			String value=drillParameter.getValue();
			if(name!=null && !name.equals("") && value!=null && !value.equals("")){
				document_parameter+="%26"+name+"%3D"+value;
				//document_parameter+="&"+name+"="+value;
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
		return false;
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




	public int getSubCategoriesNumber() {
		return subCategoriesNumber;
	}




	public void setSubCategoriesNumber(int subCategoriesNumber) {
		this.subCategoriesNumber = subCategoriesNumber;
	}




	public int getRealCatNumber() {
		return realCatNumber;
	}




	public void setRealSubCatNumber(int realCatNumber) {
		this.realCatNumber = realCatNumber;
	}




	public HashMap getSubCategories() {
		return subCategories;
	}




	public void setSubCategories(HashMap subCategories) {
		this.subCategories = subCategories;
	}




	public List getSubCategoryNames() {
		return subCategoryNames;
	}




	public void setSubCategoryNames(List subCategoryNames) {
		this.subCategoryNames = subCategoryNames;
	}




	public String getSubCategoryLabel() {
		return subCategoryLabel;
	}




	public void setSubCategoryLabel(String subCategoryLabel) {
		this.subCategoryLabel = subCategoryLabel;
	}


}
