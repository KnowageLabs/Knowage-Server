/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.chart.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.chart.bo.charttypes.XYCharts.BlockChart;
import it.eng.spagobi.engines.chart.bo.charttypes.XYCharts.SimpleBlockChart;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.CombinedCategoryBar;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.LinkableBar;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.OverlaidBarLine;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.OverlaidStackedBarLine;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.SimpleBar;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.StackedBar;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.StackedBarGroup;
import it.eng.spagobi.engines.chart.bo.charttypes.blockcharts.TimeBlockChart;
import it.eng.spagobi.engines.chart.bo.charttypes.boxcharts.SimpleBox;
import it.eng.spagobi.engines.chart.bo.charttypes.clusterchart.SimpleCluster;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.BulletGraph;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.Meter;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.SBISpeedometer;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.SimpleDial;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.SpeedometerMultiValue;
import it.eng.spagobi.engines.chart.bo.charttypes.dialcharts.Thermometer;
import it.eng.spagobi.engines.chart.bo.charttypes.piecharts.LinkablePie;
import it.eng.spagobi.engines.chart.bo.charttypes.piecharts.SimplePie;
import it.eng.spagobi.engines.chart.bo.charttypes.scattercharts.MarkerScatter;
import it.eng.spagobi.engines.chart.bo.charttypes.scattercharts.SimpleScatter;
import it.eng.spagobi.engines.chart.bo.charttypes.targetcharts.SparkLine;
import it.eng.spagobi.engines.chart.bo.charttypes.targetcharts.WinLose;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;


/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


public class ChartImpl implements IChart {
	
	private static transient Logger logger=Logger.getLogger(ChartImpl.class);
	protected int titleDimension;
	protected String name=null;
	protected String subName=null;
	protected int width;
	protected int height;
	protected String data;
	protected String confDataset;
	protected boolean isLovConfDefined;
	protected IEngUserProfile profile;
	protected String type="";
	protected String subtype="";
	protected Color color;
	protected boolean legend=true;
	protected String legendPosition="bottom";
	protected Map parametersObject;
	
	protected boolean filter=true;
	protected boolean slider=true;
	protected boolean sliderStartFromEnd=false;
	protected String positionSlider;
	protected StyleLabel styleTitle;
	protected StyleLabel styleSubTitle;
	protected StyleLabel defaultLabelsStyle;
	protected StyleLabel styleLegend;	
	protected HashMap seriesLabelsMap = null;
	
	protected boolean multichart=false;
	protected String orientationMultichart="";

	protected Locale locale=Locale.ITALIAN;
	
	// REGISTER CHART TYPES and SUB TYPES
	
	public static final String DIAL_CHART = "DIALCHART";
	public static final String speedometer = "speedometer";
	public static final String speedometerMultiValue = "speedometerMultiValue";
	public static final String simpledial = "simpledial";
	public static final String thermomether = "thermomether";
	public static final String meter = "meter";
	public static final String bullet = "bullet";

	public static final String PIECHART = "PIECHART";
	public static final String simplepie = "simplepie";
	public static final String linkablepie = "linkablepie";
	
	public static final String BARCHART = "BARCHART";
	public static final String simplebar = "simplebar";
	public static final String linkablebar = "linkablebar";
	public static final String overlaid_barline = "overlaid_barline";
	public static final String stacked_bar = "stacked_bar";
	public static final String stacked_bar_group = "stacked_bar_group";
	public static final String overlaid_stacked_barline= "overlaid_stacked_barline";
	public static final String combined_category_bar= "combined_category_bar";

	public static final String BOXCHART = "BOXCHART";
	public static final String simplebox = "simplebox";
	
	public static final String CLUSTERCHART = "CLUSTERCHART";
	public static final String simplecluster = "simplecluster";

	public static final String XYCHART = "XYCHART";
	public static final String blockchart = "blockchart";
	public static final String simpleblockchart = "simpleblockchart";

	public static final String SCATTERCHART = "SCATTERCHART";
	public static final String simplescatter = "simplescatter";
	public static final String markerscatter = "markerscatter";

	public static final String TARGETCHART = "TARGETCHART";
	public static final String sparkline = "spark_line";
	public static final String winlose = "win_lose";

	public static final String BLOCKCHART = "BLOCKCHART";
	public static final String simpletimeblock = "simpletimeblock";

	
	
// Register Parameters
	/** Nameof the chart., can be parametrized */
	public static final String NAME = "name";
	/** tag for style title */
	public static final String STYLE_TITLE = "STYLE_TITLE";
	/** tag for style subTitle */
	public static final String STYLE_SUBTITLE = "STYLE_SUBTITLE";
	/** tag for style default labels */
	public static final String STYLE_LABELS_DEFAULT = "STYLE_LABELS_DEFAULT";
	/** parameters for style tags */
	public static final String NAME_STYLE = "name";
	public static final String FONT_STYLE = "font";
	public static final String SIZE_STYLE = "size";
	public static final String COLOR_STYLE = "color";
	public static final String ORIENTATION_STYLE = "orientation";
	/** title dimension */
	public static final String TITLE_DIMENSION = "title_dimension";
		/** title dimension */
	public static final String COLORS_BACKGROUND = "COLORS.background";
	/** dimensions of chart */
	public static final String DIMENSION_WIDTH = "DIMENSION.width";
	public static final String DIMENSION_HEIGHT = "DIMENSION.height";
	/** dataset for configuration */
	public static final String  CONF_DATASET = "confdataset";

	/** Parameter in the tag CONF*/
/**  TAG CONF */
	public static final String CONF = "CONF";
	/** if true draw legend */
	public static final String LEGEND = "legend";
	/** legend position; bottom, top, left, right*/
	public static final String LEGEND_POSITION = "legend_position";
	/** legend style; font, sizem color*/
	public static final String LEGEND_STYLE = "STYLE_LEGEND";
	/** if true view filter*/
	public static final String VIEW_FILTER = "view_filter";
	/** if true view slider*/
	public static final String VIEW_SLIDER = "view_slider";
	/** if true the slider starts from last n categories*/
	public static final String SLIDER_START_FROM_END = "slider_start_from_end";
	/** top or bottom, where to put slider*/
	public static final String POSITION_SLIDER = "position_slider";

	// Outside of CONF
	/** Labels for series, should be put outside CONF*/
	public static final String SERIES_LABELS = "SERIES_LABELS";
	
	
	/**
	 * configureChart reads the content of the template and sets the chart parameters.
	 * 
	 * @param content the content
	 */
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		// common part for all charts
		//setting the title with parameter values if is necessary
		if(content.getAttribute(NAME)!=null) {
			String titleChart = (String)content.getAttribute(NAME);
			String tmpTitle = titleChart;
			while (!tmpTitle.equals("")){
				if (tmpTitle.indexOf("$P{") >= 0){
					String parName = tmpTitle.substring(tmpTitle.indexOf("$P{")+3, tmpTitle.indexOf("}"));
					
					String parValue = (parametersObject.get(parName)==null)?"":(String)parametersObject.get(parName);
					parValue = parValue.replaceAll("\'", "");
					
					if(parValue.equals("%")) parValue = "";
					int pos = tmpTitle.indexOf("$P{"+parName+"}") + (parName.length()+4);
					titleChart = titleChart.replace("$P{" + parName + "}", parValue);
					tmpTitle = tmpTitle.substring(pos);
				}
				else
					tmpTitle = "";
			}
			setName(titleChart);
		}
		else setName("");

		SourceBean styleTitleSB = (SourceBean)content.getAttribute(STYLE_TITLE);
		if(styleTitleSB!=null){

			String fontS = (String)styleTitleSB.getAttribute(FONT_STYLE);
			String sizeS = (String)styleTitleSB.getAttribute(SIZE_STYLE);
			String colorS = (String)styleTitleSB.getAttribute(COLOR_STYLE);


			try{
				Color color=Color.decode(colorS);
				int size=Integer.valueOf(sizeS).intValue();
				styleTitle=new StyleLabel(fontS,size,color);
				
			}
			catch (Exception e) {
				logger.error("Wrong style Title settings, use default");
			}

		}
		
		SourceBean styleSubTitleSB = (SourceBean)content.getAttribute(STYLE_SUBTITLE);
		if(styleSubTitleSB!=null){

			String subTitle = (String)styleSubTitleSB.getAttribute(NAME_STYLE);
			if(subTitle!=null) {
				String tmpSubTitle = subTitle;
				while (!tmpSubTitle.equals("")){
					if (tmpSubTitle.indexOf("$P{") >= 0){
						String parName = tmpSubTitle.substring(tmpSubTitle.indexOf("$P{")+3, tmpSubTitle.indexOf("}"));
						String parValue = (parametersObject.get(parName)==null)?"":(String)parametersObject.get(parName);
						parValue = parValue.replaceAll("\'", "");
						if(parValue.equals("%")) parValue = "";
						int pos = tmpSubTitle.indexOf("$P{"+parName+"}") + (parName.length()+4);
						subTitle = subTitle.replace("$P{" + parName + "}", parValue);
						tmpSubTitle = tmpSubTitle.substring(pos);
					}
					else
						tmpSubTitle = "";
				}
				setSubName(subTitle);
			}
			else setSubName("");
			
			String fontS = (String)styleSubTitleSB.getAttribute(FONT_STYLE);
			String sizeS = (String)styleSubTitleSB.getAttribute(SIZE_STYLE);
			String colorS = (String)styleSubTitleSB.getAttribute(COLOR_STYLE);


			try{
				Color color=Color.decode(colorS);
				int size=Integer.valueOf(sizeS).intValue();
				styleSubTitle=new StyleLabel(fontS,size,color);				
			}
			catch (Exception e) {
				logger.error("Wrong style SubTitle settings, use default");
			}

		}

		SourceBean styleLabelsSB = (SourceBean)content.getAttribute(STYLE_LABELS_DEFAULT);
		if(styleLabelsSB!=null){

			String fontS = (String)styleLabelsSB.getAttribute(FONT_STYLE);
			if(fontS==null){
				fontS = "Arial";
			}
			String sizeS = (String)styleLabelsSB.getAttribute(SIZE_STYLE);
			if(sizeS==null){
				sizeS = "12";
			}
			String colorS = (String)styleLabelsSB.getAttribute(COLOR_STYLE);
			if(colorS==null){
				colorS = "#000000";
			}
			String orientationS = (String)styleLabelsSB.getAttribute(ORIENTATION_STYLE);
			if(orientationS==null){
				orientationS = "horizontal";
			}

			try{
				Color color=Color.decode(colorS);
				int size=Integer.valueOf(sizeS).intValue();
				defaultLabelsStyle=new StyleLabel(fontS,size,color,orientationS);

			}
			catch (Exception e) {
				logger.error("Wrong style labels settings, use default");
			}

		}else{
			defaultLabelsStyle=new StyleLabel("Arial", 12,Color.BLACK);
		}

		if(content.getAttribute("title_dimension")!=null) 
		{
			String titleD=((String)content.getAttribute(TITLE_DIMENSION));
			titleDimension=Integer.valueOf(titleD).intValue();
		}
		else setTitleDimension(18);


		String colS = (String)content.getAttribute(COLORS_BACKGROUND);
		if(colS!=null) 
		{
			Color col=new Color(Integer.decode(colS).intValue());
			if(col!=null){
				setColor(col);}
			else{
				setColor(Color.white);
			}
		}
		else { 	
			setColor(Color.white);
		}

		String widthS = (String)content.getAttribute(DIMENSION_WIDTH);
		String heightS = (String)content.getAttribute(DIMENSION_HEIGHT);
		if(widthS==null || heightS==null){
			logger.warn("Width or height non defined, use default ones");
			widthS="400";
			heightS="300";
		}

		width=Integer.valueOf(widthS).intValue();
		height=Integer.valueOf(heightS).intValue();

		// get all the data parameters 


		try{					
			Map dataParameters = new HashMap();
			SourceBean dataSB = (SourceBean)content.getAttribute(CONF);
			List dataAttrsList = dataSB.getContainedSourceBeanAttributes();
			Iterator dataAttrsIter = dataAttrsList.iterator();
			while(dataAttrsIter.hasNext()) {
				SourceBeanAttribute paramSBA = (SourceBeanAttribute)dataAttrsIter.next();
				SourceBean param = (SourceBean)paramSBA.getValue();
				String nameParam = (String)param.getAttribute("name");
				String valueParam = (String)param.getAttribute("value");
				dataParameters.put(nameParam, valueParam);
			}


			if(dataParameters.get(CONF_DATASET)!=null && !(((String)dataParameters.get(CONF_DATASET)).equalsIgnoreCase("") )){	
				confDataset=(String)dataParameters.get(CONF_DATASET);
				isLovConfDefined=true;
			}
			else {
				isLovConfDefined=false;
			}

			legend=true;
			if(dataParameters.get(LEGEND)!=null && !(((String)dataParameters.get(LEGEND)).equalsIgnoreCase("") )){	
				String leg=(String)dataParameters.get(LEGEND);
				if(leg.equalsIgnoreCase("false"))
					legend=false;
			}

			legendPosition="bottom";
			if(dataParameters.get(LEGEND_POSITION)!=null && !(((String)dataParameters.get(LEGEND_POSITION)).equalsIgnoreCase("") )){	
				String leg=(String)dataParameters.get(LEGEND_POSITION);
				if(leg.equalsIgnoreCase("bottom") || leg.equalsIgnoreCase("left") || leg.equalsIgnoreCase("right") || leg.equalsIgnoreCase("top"))
					legendPosition=leg;
			}
			
			filter=true;
			if(dataParameters.get(VIEW_FILTER)!=null && !(((String)dataParameters.get(VIEW_FILTER)).equalsIgnoreCase("") )){	
				String fil=(String)dataParameters.get(VIEW_FILTER);
				if(fil.equalsIgnoreCase("false"))
					filter=false;
			}

			slider=true;
			if(dataParameters.get(VIEW_SLIDER)!=null && !(((String)dataParameters.get(VIEW_SLIDER)).equalsIgnoreCase("") )){	
				String sli=(String)dataParameters.get(VIEW_SLIDER);
				if(sli.equalsIgnoreCase("false"))
					slider=false;
			}

			sliderStartFromEnd=false;
			if(dataParameters.get(SLIDER_START_FROM_END)!=null && !(((String)dataParameters.get(SLIDER_START_FROM_END)).equalsIgnoreCase("") )){	
				String sli=(String)dataParameters.get(SLIDER_START_FROM_END);
				if(sli.equalsIgnoreCase("true"))
					sliderStartFromEnd=true;
			}
			
			positionSlider="top";
			if(dataParameters.get(POSITION_SLIDER)!=null && !(((String)dataParameters.get(POSITION_SLIDER)).equalsIgnoreCase("") )){	
				positionSlider=(String)dataParameters.get(POSITION_SLIDER);
			}
			
			//reading series orders if present
			SourceBean sbSerieLabels = (SourceBean)content.getAttribute(SERIES_LABELS);
			// back compatibility
			if(sbSerieLabels==null){
				sbSerieLabels = (SourceBean)content.getAttribute("CONF.SERIES_LABELS");
			}
			if(sbSerieLabels!=null){
				seriesLabelsMap=new LinkedHashMap();
				List atts=sbSerieLabels.getContainedAttributes();
				String serieLabel="";
				for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();
					String serieName=(String)object.getKey();
					serieLabel=new String((String)object.getValue());
					if(serieLabel!=null){
						seriesLabelsMap.put(serieName, serieLabel); 
					}
				}		
			}
			
			SourceBean styleLegendSB = (SourceBean)content.getAttribute(LEGEND_STYLE);
			if(styleLegendSB!=null){

				String fontS = (String)styleLegendSB.getAttribute(FONT_STYLE);
				String sizeS = (String)styleLegendSB.getAttribute(SIZE_STYLE);
				String colorS = (String)styleLegendSB.getAttribute(COLOR_STYLE);


				try{
					Color color=Color.decode(colorS);
					int size=Integer.valueOf(sizeS).intValue();
					styleLegend=new StyleLabel(fontS,size,color);
					
				}
				catch (Exception e) {
					logger.error("Wrong style Legend settings, use default");
				}
			}
			
			
		}
		catch (Exception e) {
			logger.error(e.getCause()+" "+e.getStackTrace());
			logger.error("many error in reading data source parameters",e);
		}


	}

	/**
	 * This function creates the chart object.
	 * 
	 * @param chartTitle the chart title
	 * @param dataset the dataset
	 * 
	 * @return the j free chart
	 */

	public JFreeChart createChart(DatasetMap dataset) {
		return null;
	}

	/**
	 * This function creates the object of the right subtype as specified by type and subtype parameters found in template.
	 * 
	 * @param type the type
	 * @param subtype the subtype
	 * 
	 * @return the chart impl
	 */

	public static ChartImpl createChart(String type,String subtype){
		ChartImpl sbi=null;
		if(type.equals(DIAL_CHART)){
			if(subtype.equalsIgnoreCase(speedometer)){
				sbi=new SBISpeedometer();
			}
			if(subtype.equalsIgnoreCase(speedometerMultiValue)){
				sbi=new SpeedometerMultiValue();
			}
			else if(subtype.equalsIgnoreCase(simpledial)){
				sbi= new SimpleDial();
			}
			else if(subtype.equalsIgnoreCase(thermomether)){
				sbi= new Thermometer();
			}
			else if(subtype.equalsIgnoreCase(meter)){
				sbi= new Meter();
			}
			else if(subtype.equalsIgnoreCase(bullet)){
				sbi= new BulletGraph();
			}
		}
		else if(type.equals(PIECHART)){
			if(subtype.equalsIgnoreCase(simplepie)){
				sbi=new SimplePie();
			}
			if(subtype.equalsIgnoreCase(linkablepie)){
				sbi=new LinkablePie();
			}			
		}

		else if(type.equals(BARCHART)){
			if(subtype.equalsIgnoreCase(simplebar)){
				sbi=new SimpleBar();
			}
			else if(subtype.equalsIgnoreCase(linkablebar)){
				sbi=new LinkableBar();
			}
			else if(subtype.equalsIgnoreCase(overlaid_barline)){
				sbi=new OverlaidBarLine();
			}		
			else if(subtype.equalsIgnoreCase(stacked_bar)){
				sbi=new StackedBar();
			}		
			else if(subtype.equalsIgnoreCase(stacked_bar_group)){
				sbi=new StackedBarGroup();
			}	
			else if(subtype.equalsIgnoreCase(overlaid_stacked_barline)){
				sbi=new OverlaidStackedBarLine();
			}
			else if(subtype.equalsIgnoreCase(combined_category_bar)){
				sbi=new CombinedCategoryBar();
			}	
		}

		else if(type.equals(BOXCHART)){
			if(subtype.equalsIgnoreCase(simplebox)){
				sbi=new SimpleBox();
			}
		}

		else if(type.equals(CLUSTERCHART)){
			if(subtype.equalsIgnoreCase(simplecluster)){
				sbi=new SimpleCluster();
			}
		}
		
		else if(type.equals(XYCHART)){
			if(subtype.equalsIgnoreCase(blockchart)){
				sbi=new BlockChart();
			}
			if(subtype.equalsIgnoreCase(simpleblockchart)){
				sbi=new SimpleBlockChart();
			}
		}
		
		else if(type.equals(SCATTERCHART)){
			if(subtype.equalsIgnoreCase(simplescatter)){
				sbi=new SimpleScatter();
			}
			if(subtype.equalsIgnoreCase(markerscatter)){
				sbi=new MarkerScatter();
			}
		}
		else if(type.equals(TARGETCHART)){
			if(subtype.equalsIgnoreCase(sparkline)){
				sbi=new SparkLine();
			}
			if(subtype.equalsIgnoreCase(winlose)){
				sbi=new WinLose();
			}
		}	
		else if(type.equals(BLOCKCHART)){
			if(subtype.equalsIgnoreCase(simpletimeblock)){
				sbi=new TimeBlockChart();
			}
		}			

		return sbi;
	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getData()
	 */
	public String getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getHeight()
	 */
	public int getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getWidth()
	 */
	public int getWidth() {
		return width;

	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setData(java.lang.String)
	 */
	public void setData(String _data) {
		data=_data;		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setHeight(int)
	 */
	public void setHeight(int _height) {
		height=_height;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setName(java.lang.String)
	 */
	public void setName(String _name) {
		name=_name;		
	}
	
	public void setSubName(String _name) {
		subName=_name;		
	}

	/**
	 * @return the subName
	 */
	public String getSubName() {
		return subName;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setWidth(int)
	 */
	public void setWidth(int _width) {
		width=_width;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#calculateValue()
	 */
	public DatasetMap calculateValue() throws Exception {
		return null;
	}

	/**
	 * Gets the conf dataset.
	 * 
	 * @return the conf dataset
	 */
	public String getConfDataset() {
		return confDataset;
	}

	/**
	 * Sets the conf dataset.
	 * 
	 * @param confDataset the new conf dataset
	 */
	public void setConfDataset(String confDataset) {
		this.confDataset = confDataset; 
	}

	/**
	 * Gets the profile.
	 * 
	 * @return the profile
	 */
	public IEngUserProfile getProfile() {
		return profile;
	}

	/**
	 * Sets the profile.
	 * 
	 * @param profile the new profile
	 */
	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

	/**
	 * Checks if is lov conf defined.
	 * 
	 * @return true, if is lov conf defined
	 */
	public boolean isLovConfDefined() {
		return isLovConfDefined;
	}

	/**
	 * Sets the lov conf defined.
	 * 
	 * @param isLovConfDefined the new lov conf defined
	 */
	public void setLovConfDefined(boolean isLovConfDefined) {
		this.isLovConfDefined = isLovConfDefined;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#isLinkable()
	 */
	public boolean isLinkable() {
		return false;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the subtype.
	 * 
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * Sets the subtype.
	 * 
	 * @param subtype the new subtype
	 */
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#isChangeableView()
	 */
	public boolean isChangeableView() {
		return false;
	}

	/**
	 * Gets the change view label.
	 * 
	 * @param theme the theme
	 * @param i the i
	 * 
	 * @return the change view label
	 */
	public String getChangeViewLabel(String theme, int i) {
		return "";
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setChangeViewChecked(boolean)
	 */
	public void setChangeViewChecked(boolean b) {
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getPossibleChangePars()
	 */
	public List getPossibleChangePars() {
		return new Vector();
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setChangeViewsParameter(java.lang.String, boolean)
	 */
	public void setChangeViewsParameter(String changePar, boolean how) {


	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getChangeViewParameter(java.lang.String)
	 */
	public boolean getChangeViewParameter(String changePar) {
		return false;		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getChangeViewParameterLabel(java.lang.String, int)
	 */
	public String getChangeViewParameterLabel(String changePar, int i) {
		return null;
	}

	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color.
	 * 
	 * @param color the new color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#filterDataset(org.jfree.data.general.Dataset, java.util.HashMap, int, int)
	 */
	public Dataset filterDataset(Dataset dataset, HashMap categories, int catSelected, int numberCatsVisualization) {

		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#isLegend()
	 */
	public boolean isLegend() {
		return legend;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setLegend(boolean)
	 */
	public void setLegend(boolean legend) {
		this.legend = legend;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#getParametersObject()
	 */	
	public Map getParametersObject() {
		return parametersObject;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.IChart#setParametersObject(java.util.Map)
	 */
	public void setParametersObject(Map parametersObject) {
		this.parametersObject = parametersObject;
	}



	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public boolean isSlider() {
		return slider;
	}

	/**
	 * @return the positionSlider
	 */
	public String getPositionSlider() {
		return positionSlider;
	}

	/**
	 * @param positionSlider the positionSlider to set
	 */
	public void setPositionSlider(String positionSlider) {
		this.positionSlider = positionSlider;
	}

	public void setSlider(boolean slider) {
		this.slider = slider;
	}


	public void setTitleParameter(List atts) {
		try{
			String tmpTitle=new String(name);
			if (tmpTitle.indexOf("$F{") >= 0){
				String parName = tmpTitle.substring(tmpTitle.indexOf("$F{")+3, tmpTitle.indexOf("}"));
				logger.debug("parName: " + parName);
				for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

					String nameP=new String(object.getKey());
					String value=new String((String)object.getValue());
					logger.debug("nameP: " + nameP + " - value: "+ value);
					if(nameP.equalsIgnoreCase(parName))
					{
						int pos = tmpTitle.indexOf("$F{"+parName+"}") + (parName.length()+4);
						name = name.replace("$F{" + parName + "}", value);
						tmpTitle = tmpTitle.substring(pos);
					}
				}

			}
		}
		catch (Exception e) {
			logger.error("Error in parameters Title:", e);
		}

	}
	
	public void setSubTitleParameter(List atts) {
		try{
			String tmpTitle=new String(getSubName());
			if (tmpTitle != null && tmpTitle.indexOf("$F{") >= 0){
				String parName = tmpTitle.substring(tmpTitle.indexOf("$F{")+3, tmpTitle.indexOf("}"));

				for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

					String nameP=new String(object.getKey());
					String value=(object.getValue()==null)?" ":new String((String)object.getValue());
					if (nameP.equalsIgnoreCase(parName))
					{
						int pos = tmpTitle.indexOf("$F{"+parName+"}") + (parName.length()+4);
						setSubName(getSubName().replace("$F{" + parName + "}", value));
						tmpTitle = tmpTitle.substring(pos);
					}
				}

			}
		}
		catch (Exception e) {
			logger.error("Error in parameters SubTitle");
		}
	}
	
	public void setSubTitleParameter(String attValue) {
		try{
			String tmpTitle=new String(getSubName());
			if (tmpTitle != null && tmpTitle.indexOf("$F{") >= 0){
				String parName = tmpTitle.substring(tmpTitle.indexOf("$F{")+3, tmpTitle.indexOf("}"));
				int pos = tmpTitle.indexOf("$F{"+parName+"}") + (parName.length()+4);
				setSubName(getSubName().replace("$F{" + parName + "}", attValue));
				tmpTitle = tmpTitle.substring(pos);
			}
		}
		catch (Exception e) {
			logger.error("Error in parameters SubTitle");
		}
	}
	
	public void setTitleParameter(String attValue) {
		try{
			String tmpTitle=new String(getName());
			if (tmpTitle != null && tmpTitle.indexOf("$F{") >= 0){
				String parName = tmpTitle.substring(tmpTitle.indexOf("$F{")+3, tmpTitle.indexOf("}"));
				int pos = tmpTitle.indexOf("$F{"+parName+"}") + (parName.length()+4);
				setSubName(getName().replace("$F{" + parName + "}", attValue));
				tmpTitle = tmpTitle.substring(pos);
			}
		}
		catch (Exception e) {
			logger.error("Error in parameters Title");
		}
	}

	

	public TextTitle setStyleTitle(String title,StyleLabel titleLabel){
		Font font=null;
		Color color=null;


		boolean definedFont=true;
		boolean definedColor=true;

		if(titleLabel!=null ){
			if(titleLabel.getFont()!=null){
				font=titleLabel.getFont();
			}
			else{
				definedFont=false;
			}
			if(titleLabel.getColor()!=null){
				color=titleLabel.getColor();
			}
			else{
				definedColor=false;
			}
		}
		else{
			definedColor=false;
			definedFont=false;
		}

		if(!definedFont)
			font=new Font("Tahoma", Font.BOLD, 18);
		if(!definedColor)
			color=Color.BLACK;

		TextTitle titleText=new TextTitle(title,font,color, RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);

		return titleText;
	}
	

	public int getTitleDimension() {
		return titleDimension;
	}

	public void setTitleDimension(int titleDimension) {
		this.titleDimension = titleDimension;
	}

	public HashMap getSeriesLabels() {
		return seriesLabelsMap;
	}

	public void setSeriesLabels(HashMap seriesLabels) {
		this.seriesLabelsMap = seriesLabels;
	}

	public String getLegendPosition() {
		return legendPosition;
	}

	public void setLegendPosition(String legendPosition) {
		this.legendPosition = legendPosition;
	}
	
	/**
	 * @return the multichart
	 */
	public boolean getMultichart() {
		return multichart;
	}

	/**
	 * @param multichart the multichart to set
	 */
	public void setMultichart(boolean multichart) {
		this.multichart = multichart;
	}

	/**
	 * @return the orientationMultichart
	 */
	public String getOrientationMultichart() {
		return orientationMultichart;
	}

	/**
	 * @param orientationMultichart the orientationMultichart to set
	 */
	public void setOrientationMultichart(String orientationMultichart) {
		this.orientationMultichart = orientationMultichart;
	}


	public void drawLegend(JFreeChart chart){
		//remove ipotetical other legend
		chart.removeLegend();
		BlockContainer wrapper = new BlockContainer(new BorderArrangement());
		wrapper.setFrame(new BlockBorder(1.0, 1.0, 1.0, 1.0));

		/*LabelBlock titleBlock = new LabelBlock("Legend Items:",
				new Font("SansSerif", Font.BOLD, 12));
		titleBlock.setPadding(5, 5, 5, 5);
		wrapper.add(titleBlock, RectangleEdge.TOP);*/

		LegendTitle legend = new LegendTitle(chart.getPlot());
		BlockContainer items = legend.getItemContainer();
		if(styleLegend!=null && styleLegend.getFont()!=null){
			legend.setItemFont(new Font(styleLegend.getFontName(), Font.BOLD, styleLegend.getSize()));
		}
		
		items.setPadding(2, 5, 5, 2);
		wrapper.add(items);
		legend.setWrapper(wrapper);

		if(legendPosition.equalsIgnoreCase("bottom")) legend.setPosition(RectangleEdge.BOTTOM);
		else if(legendPosition.equalsIgnoreCase("left")) legend.setPosition(RectangleEdge.LEFT);
		else if(legendPosition.equalsIgnoreCase("right")) legend.setPosition(RectangleEdge.RIGHT);
		else if(legendPosition.equalsIgnoreCase("top")) legend.setPosition(RectangleEdge.TOP);
		else legend.setPosition(RectangleEdge.BOTTOM);
		
		legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
		chart.addSubtitle(legend);
		
		}

	public void setLocalizedTitle() {
		if(name!=null){
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		//String toSet=msgBuilder.getUserMessage(name, SpagoBIConstants.DEFAULT_USER_BUNDLE, locale);
		String toSet = msgBuilder.getI18nMessage(locale, name);
		setName(toSet);
		}
		return;
	}

	public boolean isSliderStartFromEnd() {
		return sliderStartFromEnd;
	}

	public void setSliderStartFromEnd(boolean sliderStartFromEnd) {
		this.sliderStartFromEnd = sliderStartFromEnd;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	
	
	
	

}
