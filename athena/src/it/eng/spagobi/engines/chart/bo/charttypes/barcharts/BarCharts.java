/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */




package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */




public class BarCharts extends ChartImpl {

	Map confParameters;
	String categoryLabel="";
	String valueLabel="";
	Integer numberCatVisualization=null;
	Integer numberSerVisualization=null;
	boolean dynamicNumberCatVisualization=false;

	boolean rangeIntegerValues=false;

	// <PARAMETER name="enable_tooltips" value="true" />
	boolean enableToolTips=false;	

	/** mapping name_serie => color*/
	HashMap colorMap=null;  // keeps user selected colors// serie position - color
	/** Colors in order for series (override color Map)*/
	Vector<Color> orderColorVector = null;
	HashMap seriesNumber=null; //track serie name with number position (to preserve color)
	HashMap seriesCaptions=null;
	int categoriesNumber=0;
	HashMap categories;
	//int currentSerie=-1;
	Vector currentSeries=null;
	private static transient Logger logger=Logger.getLogger(BarCharts.class);
	Vector seriesNames=null;
	Vector hiddenSeries=null;

	// keep the order of the series in the datase
	ArrayList<String> seriesOrder=null;

	StyleLabel styleXaxesLabels;
	StyleLabel styleYaxesLabels;
	StyleLabel styleValueLabels;

	// three booleans that read from template if has to be filter series, cats groups and categories with slider
	boolean filterCatGroups=true;
	boolean filterSeries=true;
	boolean filterCategories=true;
	String filterStyle="";

	// store if specified the maximum bar width
	Double maxBarWidth=null;

	// Enable if true/Disable if false select all and unselect all buttons on filter series form
	boolean filterSeriesButtons=true;

	boolean showValueLabels=false;
	String valueLabelsPosition="inside";


	HashMap catGroups=null; // only if filterCatGroups is set to true, trace cat_name /cat_group_name
	Vector currentCatGroups=null;
	Vector catGroupNames=null;

	String rangeAxisLocation=null;

	Integer firstAxisUB = null;
	Integer firstAxisLB = null;
	Integer secondAxisUB = null;
	Integer secondAxisLB = null;

	// Parameters

	/** Label for category Axis */
	public static final String CATEGORY_LABEL = "category_label";
	/** Label for value Axis */
	public static final String VALUE_LABEL = "value_label";
	/** Number of categories visualization (both) */
	public static final String N_CAT_VISUALIZATION = "n_cat_visualization";
	public static final String N_VISUALIZATION = "n_visualization";
	/** If true enable dynamic choice of numbers of categories to view*/
	public static final String DYNAMIC_N_VISUALIZATION = "dynamic_n_visualization";
	/** Number of series visualization*/
	public static final String N_SER_VISUALIZATION = "n_ser_visualization";
	/** If true enables filtering of cat  Groups*/
	public static final String FILTER_CAT_GROUPS = "filter_cat_groups";
	/** If true enables filtering of series*/
	public static final String FILTER_SERIES = "filter_series";
	/** If true shows select all and deselect all buttons from serie filters*/
	public static final String FILTER_SERIES_BUTTONS = "filter_series_buttons";
	/** If true enables filtering of categories*/
	public static final String FILTER_CATEGORIES = "filter_categories";
	/** If true show value labels*/
	public static final String SHOW_VALUE_LABLES = "show_value_labels";
	/** value labels position: values inside and outside*/
	public static final String VALUE_LABELS_POSITION = "value_labels_position";
	/** If true enables tooltips*/
	public static final String ENABLE_TOOLTIPS = "enable_tooltips";
	/** the maximum bar width, which is specified as a percentage of the available space for all bars 
	 * For Example setting to 0.05 will ensure that the bars never exceed five per cent of the lenght of the axis
	 * */
	public static final String MAXIMUM_BAR_WIDTH = "maximum_bar_width";
	/** Range Integer; If this string equals true on the range axis only int values appear
	 * Possible Values: TRUE or FALSE (the same as null), Default is False*/
	public static final String RANGE_INTEGER_VALUES = "range_integer_values";

	/** the location of the range axis. 
	 * Possibe values:  BOTTOM_OR_LEFT, BOTTOM_OR_RIGHT, TOP_OR_RIGHT, TOP_OR_LEFT 
	 * This parameter is avalaible only for those charts with one single axis*/
	public static final String RANGE_AXIS_LOCATION = "range_axis_location";

	/** name of the tag that specifies color for series in order of apparition*/
	public static final String SERIES_ORDER_COLORS = "SERIES_ORDER_COLORS";

	/** name of the tag that specifies color for each serie name */
	public static final String SERIES_COLORS = "SERIES_COLORS";

	/** name of the tag that specifies color for each serie name */
	public static final String FIRST_AXIS_UB = "first_axis_ub";
	public static final String FIRST_AXIS_LB = "first_axis_lb";
	public static final String SECOND_AXIS_UB = "second_axis_ub";
	public static final String SECOND_AXIS_LB = "second_axis_lb";




	/**
	 * Inherited by IChart: calculates chart value.
	 * 
	 * @return the dataset
	 * 
	 * @throws Exception the exception
	 */

	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);
		categories=new HashMap();
		seriesCaptions=new LinkedHashMap();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");


		// run all categories (one for each row)
		categoriesNumber=0;
		seriesNames=new Vector();
		catGroupNames=new Vector();
		//categories.put(new Integer(0), "All Categories");

		if(filterCatGroups==true){
			catGroups=new HashMap();
		}


		boolean first=true;

		// run all dataset rows
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			if(first){
				if (name.indexOf("$F{") >= 0){
					setTitleParameter(atts);
				}
				if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				first=false;
			}


			HashMap series=new HashMap();

			String catValue="";
			String cat_group_name="";

			String name="";
			String value="";



			int contSer = 0;
			//run all the attributes in a row, to define series pertaining to a category!
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				name=new String(object.getKey());
				value=new String((String)object.getValue());

				// the one targeted x is the category name
				if(name.equalsIgnoreCase("x"))
				{
					catValue=value;
					categoriesNumber=categoriesNumber+1;
					categories.put(new Integer(categoriesNumber),value);
				}
				else if(name.equalsIgnoreCase("cat_group")){
					cat_group_name=value;
				}
				else if (this.getNumberSerVisualization().intValue() > 0 && contSer < this.getNumberSerVisualization().intValue()){
					// map containing the series
					series.put(name, value);
					contSer++;
				}
				else if(seriesLabelsMap!=null){
					String serieLabel = (String)seriesLabelsMap.get(name);
					if (serieLabel != null){
						series.put(serieLabel,value);
						seriesCaptions.put(serieLabel,name);
					}
				}
				else
					series.put(name, value);

			}
			// if a category group was found add it
			if(!cat_group_name.equalsIgnoreCase("") && !catValue.equalsIgnoreCase("") && catGroups!=null)
			{	
				catGroups.put(catValue, cat_group_name);
				if(!(catGroupNames.contains(cat_group_name))){
					catGroupNames.add(cat_group_name);}
			}

			// add series to dataset only if not hidden
			for (Iterator iterator3 = series.keySet().iterator(); iterator3.hasNext();) {
				String nameS = (String) iterator3.next();
				String labelS = "";
				if(!hiddenSeries.contains(nameS)){
					if(seriesLabelsMap != null && (seriesCaptions != null && seriesCaptions.size()>0)){
						nameS = (String)(seriesCaptions.get(nameS));
						labelS = (String)seriesLabelsMap.get(nameS);
					}
					else
						labelS = nameS;

					String valueS=(String)series.get(labelS);
					if(labelS!=null && valueS!=null && !valueS.equals("null") && !valueS.equals("")){
						//orig dataset.addValue(Double.valueOf(valueS).doubleValue(), nameS, catValue);
						dataset.addValue(Double.valueOf(valueS).doubleValue(), labelS, catValue);
						/*orig if(!seriesNames.contains(nameS)){
							seriesNames.add(nameS);
						}*/
						if(!seriesNames.contains(labelS)){
							seriesNames.add(labelS);
						}
					}
				}
			}

		}
		if (listAtts.size() == 0){
			if (name.indexOf("$F{") >= 0){
				setTitleParameter("");
			}
			if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
				setSubTitleParameter("");
			}
		}


		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		return datasets;
	}


	/**
	 * Calculates chart value;
	 * 
	 * 
	 * public Dataset calculateValue(String cat, Map parameters) throws Exception {
	 * logger.debug("IN");
	 * String res=DataSetAccessFunctions.getDataSetResult(profile, getData(),parameters);
	 * 
	 * DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	 * 
	 * SourceBean sbRows=SourceBean.fromXMLString(res);
	 * List listAtts=sbRows.getAttributeAsList("ROW");
	 * 
	 * 
	 * // run all categories (one for each row)
	 * categoriesNumber=0;
	 * for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
	 * SourceBean category = (SourceBean) iterator.next();
	 * List atts=category.getContainedAttributes();
	 * 
	 * HashMap series=new HashMap();
	 * String catValue="";
	 * 
	 * String name="";
	 * String value="";
	 * 
	 * //run all the attributes, to define series!
	 * for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
	 * SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();
	 * 
	 * name=new String(object.getKey());
	 * value=new String((String)object.getValue());
	 * if(name.equalsIgnoreCase("x"))catValue=value;
	 * else series.put(name, value);
	 * }
	 * for (Iterator iterator3 = series.keySet().iterator(); iterator3.hasNext();) {
	 * String nameS = (String) iterator3.next();
	 * String valueS=(String)series.get(nameS);
	 * dataset.addValue(Double.valueOf(valueS).doubleValue(), nameS, catValue);
	 * categoriesNumber=categoriesNumber+1;
	 * }
	 * 
	 * }
	 * logger.debug("OUT");
	 * return dataset;
	 * }
	 * 
	 * @param content the content
	 */

	public void configureChart(SourceBean content) {
		logger.debug("IN");
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

		if(confParameters.get("category_label")!=null){	
			categoryLabel=(String)confParameters.get("category_label");
		}
		else
		{
			//categoryLabel="category";
			categoryLabel="";
		}

		if(confParameters.get(VALUE_LABEL)!=null){	
			valueLabel=(String)confParameters.get(VALUE_LABEL);
			String tmpValueLabel = valueLabel;
			while (!tmpValueLabel.equals("")){
				if (tmpValueLabel.indexOf("$P{") >= 0){
					String parName = tmpValueLabel.substring(tmpValueLabel.indexOf("$P{")+3, tmpValueLabel.indexOf("}"));

					String parValue = (parametersObject.get(parName)==null)?"":(String)parametersObject.get(parName);
					parValue = parValue.replaceAll("\'", "");

					if(parValue.equals("%")) parValue = "";
					int pos = tmpValueLabel.indexOf("$P{"+parName+"}") + (parName.length()+4);
					valueLabel = valueLabel.replace("$P{" + parName + "}", parValue);
					tmpValueLabel = tmpValueLabel.substring(pos);
				}
				else
					tmpValueLabel = "";
			}
			setValueLabel(valueLabel);
		}
		else
		{
			//valueLabel="values";
			setValueLabel("");
		}

		if(confParameters.get(N_CAT_VISUALIZATION)!=null || confParameters.get(N_VISUALIZATION)!=null){		
			String nu=(String)confParameters.get(N_VISUALIZATION);
			if (nu == null) nu = (String)confParameters.get(N_CAT_VISUALIZATION);
			numberCatVisualization=Integer.valueOf(nu);
		}
		else
		{
			numberCatVisualization=new Integer(1);
		}

		dynamicNumberCatVisualization=false;
		if(confParameters.get(DYNAMIC_N_VISUALIZATION)!=null){		
			String dynamicS=(String)confParameters.get(DYNAMIC_N_VISUALIZATION);
			if(dynamicS.equalsIgnoreCase("true"))dynamicNumberCatVisualization=true;
		}



		if(confParameters.get(N_SER_VISUALIZATION)!=null){		
			String nu=(String)confParameters.get(N_SER_VISUALIZATION);
			numberSerVisualization=Integer.valueOf(nu);
		}
		else
		{
			numberSerVisualization=new Integer(0);
		}



		if(confParameters.get(FILTER_CAT_GROUPS)!=null){		
			String filterCatGroupsS=(String)confParameters.get(FILTER_CAT_GROUPS);
			if(filterCatGroupsS.equalsIgnoreCase("false"))filterCatGroups=false;
			else filterCatGroups=true;
		}
		else
		{
			filterCatGroups=true;
		}

		if(confParameters.get(FILTER_SERIES)!=null){		
			String filterSeriesS=(String)confParameters.get(FILTER_SERIES);
			if(filterSeriesS.equalsIgnoreCase("false"))filterSeries=false;
			else filterSeries=true;
		}
		else
		{
			filterSeries=true;
		}

		if(confParameters.get(FILTER_SERIES_BUTTONS)!=null){		
			String filterSeriesS=(String)confParameters.get(FILTER_SERIES_BUTTONS);
			if(filterSeriesS.equalsIgnoreCase("false"))filterSeriesButtons=false;
		}

		if(confParameters.get(FILTER_CATEGORIES)!=null){		
			String filterCategoriesS=(String)confParameters.get(FILTER_CATEGORIES);
			if(filterCategoriesS.equalsIgnoreCase("false"))filterCategories=false;
			else filterCategories=true;
		}
		else
		{
			filterCategories=true;
		}

		if(confParameters.get(SHOW_VALUE_LABLES)!=null){		
			String valueLabelsS=(String)confParameters.get(SHOW_VALUE_LABLES);
			if(valueLabelsS.equalsIgnoreCase("true"))showValueLabels=true;
		}

		valueLabelsPosition = "inside";
		if(confParameters.get(VALUE_LABELS_POSITION)!=null){		
			String valueLabelpos=(String)confParameters.get(VALUE_LABELS_POSITION);
			if(valueLabelpos.equalsIgnoreCase("outside"))
				valueLabelsPosition = "outside";
		}

		if(confParameters.get(ENABLE_TOOLTIPS)!=null){		
			String enableTooltipsS=(String)confParameters.get(ENABLE_TOOLTIPS);
			if(enableTooltipsS.equalsIgnoreCase("true"))enableToolTips=true;
		}

		if(confParameters.get(MAXIMUM_BAR_WIDTH)!=null){		
			String maxBarWidthS=(String)confParameters.get(MAXIMUM_BAR_WIDTH);
			try{
				maxBarWidth=Double.valueOf(maxBarWidthS);
			}
			catch (NumberFormatException e) {
				logger.error("error in defining parameter "+MAXIMUM_BAR_WIDTH+": should be a double, it will be ignored",e);
			}
		}

		if(confParameters.get(RANGE_INTEGER_VALUES)!=null){		
			String rangeIntegerValuesS=(String)confParameters.get(RANGE_INTEGER_VALUES);
			if(rangeIntegerValuesS.equalsIgnoreCase("true"))rangeIntegerValues=true;
		}


		if(confParameters.get(RANGE_AXIS_LOCATION)!=null){		
			//BOTTOM_OR_LEFT, BOTTOM_OR_RIGHT, TOP_OR_RIGHT, TOP_OR_LEFT
			String axisLocation=(String)confParameters.get(RANGE_AXIS_LOCATION);
			if(axisLocation.equalsIgnoreCase("BOTTOM_OR_LEFT") || axisLocation.equalsIgnoreCase("BOTTOM_OR_RIGHT") || axisLocation.equalsIgnoreCase("TOP_OR_LEFT") || axisLocation.equalsIgnoreCase("TOP_OR_RIGHT")){
				rangeAxisLocation=axisLocation;
			}
			else{
				logger.warn("Range Axis location specified: "+axisLocation+" not a valid value.");
			}
		}


		if(confParameters.get(FIRST_AXIS_LB)!=null){		
			String axis=confParameters.get(FIRST_AXIS_LB).toString();
			Integer axisInte = Integer.valueOf(axis);
			firstAxisLB = axisInte;
		}
		
		if(confParameters.get(FIRST_AXIS_UB)!=null){		
			String axis=confParameters.get(FIRST_AXIS_UB).toString();
			Integer axisInte = Integer.valueOf(axis);
			firstAxisUB = axisInte;
		}

		if(confParameters.get(SECOND_AXIS_LB)!=null){		
			String axis=confParameters.get(SECOND_AXIS_LB).toString();
			Integer axisInte = Integer.valueOf(axis);
			secondAxisLB = axisInte;
		}
		
		if(confParameters.get(SECOND_AXIS_UB)!=null){		
			String axis=confParameters.get(SECOND_AXIS_UB).toString();
			Integer axisInte = Integer.valueOf(axis);
			secondAxisUB = axisInte;
		}

		//reading series colors if present
		SourceBean colors = (SourceBean)content.getAttribute(SERIES_COLORS);
		if(colors==null){
			colors = (SourceBean)content.getAttribute("CONF.SERIES_COLORS");
		}
		if(colors!=null){
			colorMap=new HashMap();
			List atts=colors.getContainedAttributes();
			String colorNum="";
			String colorSerie="";
			String num="";
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


		//reading series colors if present, if present this overrides series colors!!!
		SourceBean orderColors = (SourceBean)content.getAttribute(SERIES_ORDER_COLORS);
		if(orderColors==null){
			orderColors = (SourceBean)content.getAttribute("CONF."+SERIES_ORDER_COLORS);
		}
		if(orderColors!=null){
			orderColorVector=new Vector<Color>();
			List atts=orderColors.getContainedAttributes();
			String numSerie="";
			String colorSerie="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
				numSerie=new String(object.getKey());
				colorSerie=new String((String)object.getValue());
				Color col=new Color(Integer.decode(colorSerie).intValue());
				if(col!=null){
					orderColorVector.add(col);
					//colorMap.put(numSerie,col); 
				}
			}		

		}


		//reading filter style if present
		SourceBean sbSerieStyle = (SourceBean)content.getAttribute("STYLE_SLIDER_AREA");
		if(sbSerieStyle==null){
			sbSerieStyle = (SourceBean)content.getAttribute("CONF.STYLE_SLIDER_AREA");
		}
		if(sbSerieStyle!=null){
			List atts=sbSerieStyle.getContainedAttributes();
			String StyleValue = "";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
				String styleLabel=(String)object.getKey();
				StyleValue=new String((String)object.getValue());
				if(StyleValue!=null){
					if (styleLabel.equalsIgnoreCase("font")) styleLabel = "font-family";
					else if (styleLabel.equalsIgnoreCase("size")) styleLabel = "font-size";
					else if (styleLabel.equalsIgnoreCase("color")) styleLabel = "color";

					filterStyle += styleLabel +":"+StyleValue+";"; 
				}
			}		
		}

		// check if there is some serie to be hidden
		boolean moreHiddenSeries=true;
		int i=1;
		hiddenSeries=new Vector();
		while(moreHiddenSeries){
			String iS=new Integer(i).toString(); 
			if(confParameters.get("hidden_serie"+iS)!=null){
				String hiddenSerName=(String)confParameters.get("hidden_serie"+iS);
				hiddenSeries.add(hiddenSerName);
				i++;
			}
			else
				moreHiddenSeries=false;

		}

		// check if there is some info about additional labels style

		SourceBean styleXaxisLabelsSB = (SourceBean)content.getAttribute("STYLE_X_AXIS_LABELS");
		if(styleXaxisLabelsSB!=null){

			String fontS = (String)content.getAttribute("STYLE_X_AXIS_LABELS.font");
			if(fontS==null){
				fontS = defaultLabelsStyle.getFontName();
			}
			String sizeS = (String)content.getAttribute("STYLE_X_AXIS_LABELS.size");
			String colorS = (String)content.getAttribute("STYLE_X_AXIS_LABELS.color");
			String orientationS = (String)content.getAttribute("STYLE_X_AXIS_LABELS.orientation");
			if(orientationS==null){
				orientationS = "horizontal";
			}

			try{
				Color color= Color.BLACK;
				if(colorS!=null){
					color=Color.decode(colorS);
				}else{
					defaultLabelsStyle.getColor();
				}
				int size= 12;
				if(sizeS!=null){
					size=Integer.valueOf(sizeS).intValue();
				}else{
					size = defaultLabelsStyle.getSize();
				}

				styleXaxesLabels=new StyleLabel(fontS,size,color);

			}
			catch (Exception e) {
				logger.error("Wrong style labels settings, use default");
			}

		}else{
			styleXaxesLabels = defaultLabelsStyle;
		}

		SourceBean styleYaxisLabelsSB = (SourceBean)content.getAttribute("STYLE_Y_AXIS_LABELS");
		if(styleYaxisLabelsSB!=null){

			String fontS = (String)content.getAttribute("STYLE_Y_AXIS_LABELS.font");
			if(fontS==null){
				fontS = defaultLabelsStyle.getFontName();
			}
			String sizeS = (String)content.getAttribute("STYLE_Y_AXIS_LABELS.size");
			String colorS = (String)content.getAttribute("STYLE_Y_AXIS_LABELS.color");
			String orientationS = (String)content.getAttribute("STYLE_Y_AXIS_LABELS.orientation");
			if(orientationS==null){
				orientationS = "horizontal";
			}

			try{
				Color color= Color.BLACK;
				if(colorS!=null){
					color=Color.decode(colorS);
				}else{
					defaultLabelsStyle.getColor();
				}
				int size= 12;
				if(sizeS!=null){
					size=Integer.valueOf(sizeS).intValue();
				}else{
					size = defaultLabelsStyle.getSize();
				}

				styleYaxesLabels=new StyleLabel(fontS,size,color);

			}
			catch (Exception e) {
				logger.error("Wrong style labels settings, use default");
			}

		}else{
			styleYaxesLabels = defaultLabelsStyle;
		}

		SourceBean styleValueLabelsSB = (SourceBean)content.getAttribute("STYLE_VALUE_LABELS");
		if(styleValueLabelsSB!=null){

			String fontS = (String)content.getAttribute("STYLE_VALUE_LABELS.font");
			if(fontS==null){
				fontS = defaultLabelsStyle.getFontName();
			}
			String sizeS = (String)content.getAttribute("STYLE_VALUE_LABELS.size");
			String colorS = (String)content.getAttribute("STYLE_VALUE_LABELS.color");
			String orientationS = (String)content.getAttribute("STYLE_VALUE_LABELS.orientation");
			if(orientationS==null){
				orientationS = "horizontal";
			}

			try{
				Color color= Color.BLACK;
				if(colorS!=null){
					color=Color.decode(colorS);
				}else{
					defaultLabelsStyle.getColor();
				}
				int size= 12;
				if(sizeS!=null){
					size=Integer.valueOf(sizeS).intValue();
				}else{
					size = defaultLabelsStyle.getSize();
				}

				styleValueLabels=new StyleLabel(fontS,size,color,orientationS);

			}
			catch (Exception e) {
				logger.error("Wrong style labels settings, use default");
			}

		}
		else{
			styleValueLabels = defaultLabelsStyle;
		}

		seriesNumber=new HashMap();

		logger.debug("OUT");
	}


	/**
	 * @return the filterStyle
	 */
	public String getFilterStyle() {
		return filterStyle;
	}


	/**
	 * @param filterStyle the filterStyle to set
	 */
	public void setFilterStyle(String filterStyle) {
		this.filterStyle = filterStyle;
	}


	/**
	 * Use for slider: limits the categories visualization from cat selected to cat selected+numberscatsVisualization.
	 * 
	 * @param dataset the dataset
	 * @param categories the categories
	 * @param catSelected the cat selected
	 * @param numberCatsVisualization the number cats visualization
	 * 
	 * @return the dataset
	 */

	public Dataset filterDataset(Dataset dataset, HashMap categories, int catSelected, int numberCatsVisualization) {
		logger.debug("IN");
		DefaultCategoryDataset catDataset=(DefaultCategoryDataset)dataset;

		int numCats=categories.size();
		Vector visCat=new Vector();
		// from the choice point to min(chose point+interval, end point)
		//int startPoint=((catSelected-1)*numberCatsVisualization)+1;
		int startPoint=catSelected;

		int endPoint;
		if((startPoint+numberCatsVisualization-1)<=(categories.size()))
			endPoint=startPoint+numberCatsVisualization-1;
		else
			endPoint=categories.size();

		for(int i=(startPoint);i<=endPoint;i++){
			String name=(String)categories.get(new Integer(i));
			visCat.add(name);
		}


		List columns=new Vector(catDataset.getColumnKeys());
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			if(!(visCat.contains(col))){
				catDataset.removeColumn(col);
			}			
		}
		logger.debug("OUT");

		return catDataset;

	}

	/**
	 * Limits the dataset to a particular serie.
	 * 
	 * @param dataset the dataset
	 * @param serie the serie
	 * 
	 * @return the dataset
	 */

	public Dataset filterDatasetSeries(Dataset dataset, Vector series) {
		logger.debug("IN");
		DefaultCategoryDataset catDataset=(DefaultCategoryDataset)dataset;

		//keeps track of wich series has to be shown
		currentSeries=series;

		//List rowKeys=new Vector();

		List rowKeys=new Vector(catDataset.getRowKeys());

		for (Iterator iterator = rowKeys.iterator(); iterator.hasNext();) {
			String row = (String) iterator.next();
			if(!(series.contains(row))){
				catDataset.removeRow(row);	
				seriesNames.remove(row);
			}			
		}

		logger.debug("OUT");
		return catDataset;

	}


	public Dataset filterDatasetCatGroups(Dataset dataset, Vector groups) {
		logger.debug("IN");
		DefaultCategoryDataset catDataset=(DefaultCategoryDataset)dataset;

		//keeps track of wich series has to be shown
		currentCatGroups=groups;
		String catGroup="";
		//List rowKeys=new Vector();

		//List rowKeys=new Vector(catDataset.getRowKeys());
		List colKeys=new Vector(catDataset.getColumnKeys());

		for (Iterator iterator = colKeys.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			// iterate on cols, get their group and see if it has to be kept
			catGroup=(String)catGroups.get(col);
			if(!(groups.contains(catGroup))){ 
				catDataset.removeColumn(col);	
				catGroupNames.remove(col);
			}			
		}

		logger.debug("OUT");
		return catDataset;

	}





	/**
	 * Gets the conf parameters.
	 * 
	 * @return the conf parameters
	 */
	public Map getConfParameters() {
		return confParameters;
	}

	/**
	 * Sets the conf parameters.
	 * 
	 * @param confParameters the new conf parameters
	 */
	public void setConfParameters(Map confParameters) {
		this.confParameters = confParameters;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap dataset) {
		// TODO Auto-generated method stub
		return super.createChart(dataset);
	}

	/**
	 * Gets the category label.
	 * 
	 * @return the category label
	 */
	public String getCategoryLabel() {
		return categoryLabel;
	}

	/**
	 * Sets the category label.
	 * 
	 * @param categoryLabel the new category label
	 */
	public void setCategoryLabel(String categoryLabel) {
		this.categoryLabel = categoryLabel;
	}

	/**
	 * Gets the value label.
	 * 
	 * @return the value label
	 */
	public String getValueLabel() {
		return valueLabel;
	}

	/**
	 * Sets the value label.
	 * 
	 * @param valueLabel the new value label
	 */
	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}

	/**
	 * Gets the categories number.
	 * 
	 * @return the categories number
	 */
	public int getCategoriesNumber() {
		return categoriesNumber;
	}

	/**
	 * Sets the categories number.
	 * 
	 * @param categoriesNumber the new categories number
	 */
	public void setCategoriesNumber(int categoriesNumber) {
		this.categoriesNumber = categoriesNumber;
	}

	/**
	 * Gets the categories.
	 * 
	 * @return the categories
	 */
	public Map getCategories() {
		return categories;
	}



	/**
	 * Gets the number cat visualization.
	 * 
	 * @return the number cat visualization
	 */
	public Integer getNumberCatVisualization() {
		return numberCatVisualization;
	}

	/**
	 * Sets the number cat visualization.
	 * 
	 * @param numberCatVisualization the new number cat visualization
	 */
	public void setNumberCatVisualization(Integer numberCatVisualization) {
		this.numberCatVisualization = numberCatVisualization;
	}




	public Vector getCurrentSeries() {
		return currentSeries;
	}


	public void setCurrentSeries(Vector currentSeries) {
		this.currentSeries = currentSeries;
	}


	public HashMap getSeriesNumber() {
		return seriesNumber;
	}


	public void putSeriesNumber(String name, int index) {
		this.seriesNumber.put(name, new Integer(index));
	}


	public Vector getSeriesNames() {
		return seriesNames;
	}


	public void setSeriesNames(Vector seriesNames) {
		this.seriesNames = seriesNames;
	}


	public boolean isFilterCatGroups() {
		return filterCatGroups;
	}


	public void setFilterCatGroups(boolean filterCatGroups) {
		this.filterCatGroups = filterCatGroups;
	}


	public HashMap getCatGroups() {
		return catGroups;
	}


	public void setCatGroups(HashMap catGroups) {
		this.catGroups = catGroups;
	}


	public Vector getCurrentCatGroups() {
		return currentCatGroups;
	}


	public void setCurrentCatGroups(Vector currentCatGroups) {
		this.currentCatGroups = currentCatGroups;
	}


	public Vector getCatGroupNames() {
		return catGroupNames;
	}


	public void setCatGroupNames(Vector catGroupNames) {
		this.catGroupNames = catGroupNames;
	}


	public boolean isFilterSeries() {
		return filterSeries;
	}


	public void setFilterSeries(boolean filterSeries) {
		this.filterSeries = filterSeries;
	}


	public boolean isFilterCategories() {
		return filterCategories;
	}


	public void setFilterCategories(boolean filterCategories) {
		this.filterCategories = filterCategories;
	}


	/**
	 * @return the numberSerVisualization
	 */
	public Integer getNumberSerVisualization() {
		if (numberSerVisualization == null) numberSerVisualization = new Integer(0);
		return numberSerVisualization;
	}


	/**
	 * @param numberSerVisualization the numberSerVisualization to set
	 */
	public void setNumberSerVisualization(Integer numberSerVisualization) {
		this.numberSerVisualization = numberSerVisualization;
	}


	public boolean isDynamicNumberCatVisualization() {
		return dynamicNumberCatVisualization;
	}


	public void setDynamicNumberCatVisualization(
			boolean dynamicNumberCatVisualization) {
		this.dynamicNumberCatVisualization = dynamicNumberCatVisualization;
	}


	public boolean isFilterSeriesButtons() {
		return filterSeriesButtons;
	}


	public void setFilterSeriesButtons(boolean filterSeriesButtons) {
		this.filterSeriesButtons = filterSeriesButtons;
	}


	public ArrayList<String> getSeriesOrder() {
		return seriesOrder;
	}


	public void setSeriesOrder(ArrayList<String> seriesOrder) {
		this.seriesOrder = seriesOrder;
	}





}
