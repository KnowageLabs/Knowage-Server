/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.utils;


import it.eng.spago.base.SourceBean;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.StackedBarGroup;
import it.eng.spagobi.engines.chart.bo.charttypes.clusterchart.ClusterCharts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYZDataset;

public class DatasetMap {

	HashMap datasets;

	Set<String> series=new TreeSet();
	Integer seriesNumber;
	HashMap categories;
	HashMap subCategories;
	Integer catsnum;
	Integer subcatsnum;
	Integer numberCatVisualization;
	Integer numberSerVisualization;
	String catTitle="category";
	String subcatTitle="subcategory";
	String serTitle="serie";
	int categoryCurrent=0;
	String valueSlider="1";
	String categoryCurrentName;
	Vector selectedSeries;
	Vector selectedCatGroups;
	boolean makeSlider=false;
	String filterStyle = "";
	boolean dynamicNVisualization=false;

	private static transient org.apache.log4j.Logger logger=Logger.getLogger(DatasetMap.class);


	public DatasetMap() {
		this.datasets = new LinkedHashMap();
	}

	public void addDataset(String key, Dataset dataset){
		datasets.put(key, dataset);
	}


	// Call by chart JSP!!!!

	public DatasetMap filteringSimpleBarChart(SourceBean aServiceResponse,HttpServletRequest request, BarCharts sbi, String sbiMode, boolean docComposition){

		AttributesContainer attributesContainerrequest=new AttributesContainer(request);
		AttributesContainer attributesContainerResponse=new AttributesContainer(aServiceResponse);

		DatasetMap dsMap=filteringSimpleBarChartUtil(attributesContainerrequest, attributesContainerResponse, sbi, sbiMode, docComposition);

		return dsMap;
	}

	/**
	 * 
	 * @param aServiceResponse: This is the service response, check it's not null, if it has requested parameters they win against request ones, cause means that chart has been re-executed
	 * @param request
	 * @param sbi
	 * @param sbiMode
	 * @param docComposition
	 * @return
	 */

	public DatasetMap filteringSimpleBarChartUtil(AttributesContainer requestCont,AttributesContainer responseCont, BarCharts sbi, String sbiMode, boolean docComposition){
		logger.debug("IN");


		DefaultCategoryDataset dataset=(DefaultCategoryDataset)datasets.get("1");
		Dataset copyDataset=null;
		DatasetMap newDatasetMap=null;
		boolean notDisappearSlider=false;   // if n_visualization>=number total categories do not make slider disappear
		try {
			copyDataset = (DefaultCategoryDataset)dataset.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("error copying dataset");
			e.printStackTrace();
		}
		try{
			series=new TreeSet(((DefaultCategoryDataset)dataset).getRowKeys());

			//fill the serieNumber MAP by mapping each serie name to its position in the dataset, needed to recover right colors when redrawing
			/*	for(int i=0;i<series.size();i++){
			String s=(String)series.get(i);
			sbi.putSeriesNumber(s,(i+1));
		}*/

			categories=(HashMap)((BarCharts)sbi).getCategories();
			catsnum=new Integer(sbi.getCategoriesNumber());

			//See if numberCatVisualization has to be updated
			if(requestCont.getAttribute("n_visualization")!=null){
				String nVis=(String)requestCont.getAttribute("n_visualization");
				Integer catD=Integer.valueOf(nVis);
				if(catD.equals(0))catD=new Integer(catsnum);
				sbi.setNumberCatVisualization(catD);
				if(catD>=catsnum)notDisappearSlider=true;
			}


			numberCatVisualization=sbi.getNumberCatVisualization();
			numberSerVisualization=sbi.getNumberSerVisualization();

			catTitle=sbi.getCategoryLabel();
			serTitle=sbi.getValueLabel();

			// Check first on serviceResponse
			// if slider specifies a category than set view from that point; but if categoryAll is present wins over slider 
			//Should consider starting point when startFromEnd is true


			if(!responseCont.isNull() && responseCont.getAttribute("category")!=null){ // lastChange
				String catS=(String)responseCont.getAttribute("category");
				logger.debug("category specified in module response by slider "+catS);
				Double catD=Double.valueOf(catS);
				categoryCurrent=catD.intValue();
			}
			else if(requestCont.getParameter("categoryAll")!=null){  // lastChange
				logger.debug("All categories have to be shown");
				categoryCurrent=0;			
			}
			else if(requestCont.getParameter("category")!=null){
				String catS=(String)requestCont.getParameter("category");
				logger.debug("category specified in request by slider "+catS);
				Double catD=Double.valueOf(catS);
				categoryCurrent=catD.intValue();
			}
			else{ //else set view from first category or fromlast if starFromEnd is tru
				logger.debug("no particulary category specified by slider: startFromEnd option is "+sbi.isSliderStartFromEnd());
				if(sbi.isSliderStartFromEnd()==true){
					// Category current is: number categories - categories to visualize + 1
					categoryCurrent=sbi.getCategoriesNumber()-(numberCatVisualization!=null ? numberCatVisualization.intValue() : 1) +1 ;
				}
				else{
					categoryCurrent=1;
				}
			}

			valueSlider=(new Integer(categoryCurrent)).toString();
			HashMap cats=(HashMap)((BarCharts)sbi).getCategories();


			if(categoryCurrent!=0 ){   // attention
				categoryCurrentName=(String)cats.get(new Integer(categoryCurrent));
				logger.debug("current category "+categoryCurrentName);
				copyDataset=(DefaultCategoryDataset)sbi.filterDataset(copyDataset,categories,categoryCurrent,numberCatVisualization.intValue());				
			}
			else{
				logger.debug("current category is the first");
				categoryCurrentName="All";
			}



			// CHECK IF THERE IS TO FILTER CAT_GROUPS
			selectedCatGroups=new Vector();
			logger.debug("check particular category groups");
			if(sbi.isFilterCatGroups()==true){

				if(requestCont.getParameter("cat_group")!=null){
					// Check if particular cat_groups has been chosen


					Object[] cio=requestCont.getParameterValues("cat_group");
					//Convert array in vector
					for(int i=0;i<cio.length;i++){
						selectedCatGroups.add(cio[i].toString());
					}
				}
				else{
					selectedCatGroups.add("allgroups");
				}
				// if selectedSerie contains allseries 
				if(selectedCatGroups.contains("allgroups")){
					((BarCharts)sbi).setCurrentCatGroups(null);
				}
				else{	
					copyDataset=sbi.filterDatasetCatGroups(copyDataset,selectedCatGroups);	

				}
			}
			else selectedCatGroups.add("allgroups");


			// CHECK IF THERE IS TO FILTER SERIES
			logger.debug("check if has to filter series");
			selectedSeries=new Vector();
			if(sbi.isFilterSeries()==true){
				// Check if particular series has been chosen

				if(requestCont.getParameter("serie")!=null){
					Object[] cio=requestCont.getParameterValues("serie");
					//Convert array in vector
					for(int i=0;i<cio.length;i++){
						selectedSeries.add(cio[i].toString());
					}
				}
				else{
					//if(!sbiMode.equalsIgnoreCase("WEB") && !docComposition)
					//if(!sbiMode.equalsIgnoreCase("WEB") || docComposition)
					selectedSeries.add("allseries");
				}


				// if selectedSerie contains allseries 
				if(selectedSeries.contains("allseries")){
					((BarCharts)sbi).setCurrentSeries(null);
				}
				else{	
					copyDataset=sbi.filterDatasetSeries(copyDataset,selectedSeries);	

				}
			}
			else selectedSeries.add("allseries");

			// consider if drawing the slider
			if(sbi.isFilterCategories()==true && (catsnum.intValue())>numberCatVisualization.intValue()){
				logger.debug("slider is to be drawn");
				makeSlider=true;	    	
			}
			else if(sbi.isFilterCategories()==true && notDisappearSlider==true){
				logger.debug("slider is to be drawn");
				makeSlider=true;	    	
			}

			//gets the filter's style
			filterStyle=sbi.getFilterStyle();

			if(copyDataset==null){copyDataset=dataset;}

			newDatasetMap=this.copyDatasetMap(copyDataset);
		}
		catch (Exception e) {
			logger.error("Error while filtering simple Chart ",e);
		}

		// set if is dynamic Categories selection
		dynamicNVisualization=sbi.isDynamicNumberCatVisualization();		

		logger.debug("OUT");
		return newDatasetMap;

	}




	public DatasetMap copyDatasetMap(Dataset dataset){
		logger.debug("IN");
		DatasetMap copy=new DatasetMap();

		copy.setSeries(this.series);
		copy.setSeriesNumber(this.seriesNumber);
		copy.setCategories(this.getCategories());
		copy.setCatsnum(this.getCatsnum());
		copy.setNumberCatVisualization(this.getNumberCatVisualization());
		copy.setNumberSerVisualization(this.getNumberSerVisualization());
		copy.setCatTitle(this.getCatTitle());
		copy.setSerTitle(this.getSerTitle());
		copy.setCategoryCurrent(this.getCategoryCurrent());
		copy.setValueSlider(this.getValueSlider());
		copy.setCategoryCurrentName(this.getCategoryCurrentName());		
		copy.setSelectedSeries(this.getSelectedSeries());
		copy.setMakeSlider(this.isMakeSlider());
		copy.selectedCatGroups=this.getSelectedCatGroups();
		copy.addDataset("1", dataset);
		logger.debug("OUT");
		return copy;

	}




	public DatasetMap filteringMultiDatasetBarChart(SourceBean aServiceResponse,HttpServletRequest request, BarCharts sbi, String sbiMode, boolean docComposition){

		AttributesContainer attributesContainerrequest=new AttributesContainer(request);
		AttributesContainer attributesContainerResponse=new AttributesContainer(aServiceResponse);

		DatasetMap dsMap=filteringMultiDatasetBarChartUtil(attributesContainerrequest, attributesContainerResponse, sbi, sbiMode, docComposition);

		return dsMap;
	}


	public DatasetMap filteringMultiDatasetBarChartUtil(AttributesContainer requestCont,AttributesContainer responseCont, BarCharts sbi, String sbiMode, boolean docComposition){
		logger.debug("IN");
		DatasetMap newDatasetMap=new DatasetMap();
		boolean notDisappearSlider=false;   // if n_visualization>=number total categories do not make slider disappear

		series=new LinkedHashSet<String>();
		//series=new TreeSet<String>();


		for (Iterator iterator = datasets.keySet().iterator(); iterator.hasNext();) {
			String  key = (String ) iterator.next();

			DefaultCategoryDataset dataset=(DefaultCategoryDataset) datasets.get(key);  // this is the old dataset to filter


			Dataset copyDataset=null;
			try {
				copyDataset = (DefaultCategoryDataset)dataset.clone();	// clone dataset
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// all series present in the dataset
			// add found series if the number is less then the max number of visualization
			int contSer = 0;

			for (Iterator iterator2 = (((DefaultCategoryDataset)dataset).getRowKeys()).iterator(); iterator2.hasNext();) {
				if (this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization()){
					String serie = (String) iterator2.next();
					if(!series.contains(serie)){
						series.add(serie);
						contSer++;
					}
				}
				else if (this.getNumberSerVisualization() == 0){
					String serie = (String) iterator2.next();
					if(!series.contains(serie)){
						series.add(serie);
					}
				}
			}


			categories=(HashMap)((BarCharts)sbi).getCategories();
			catsnum=new Integer(sbi.getCategoriesNumber());

			//See if numberCatVisualization has to be updated
			if(responseCont.getAttribute("n_visualization")!=null){
				String nVis=(String)responseCont.getAttribute("n_visualization");
				Integer catD=Integer.valueOf(nVis);
				if(catD.equals(0))catD=new Integer(catsnum);
				sbi.setNumberCatVisualization(catD);
				if(catD>=catsnum)notDisappearSlider=true;
			}

			numberCatVisualization=sbi.getNumberCatVisualization();
			numberSerVisualization=sbi.getNumberSerVisualization();

			catTitle=sbi.getCategoryLabel();
			serTitle=sbi.getValueLabel();

			// if slider specifies a category than set view from that point
			if(responseCont!=null && responseCont.getAttribute("category")!=null){ // lastChange
				String catS=(String)responseCont.getAttribute("category");
				logger.debug("category specified in module response by slider "+catS);
				Double catD=Double.valueOf(catS);
				categoryCurrent=catD.intValue();
			} 
			else if(requestCont.getParameter("categoryAll")!=null){
				logger.debug("All categories have to be shown");
				categoryCurrent=0;			
			}
			else if(requestCont.getParameter("category")!=null){
				String catS=(String)requestCont.getParameter("category");
				logger.debug("category specified in request by slider "+catS);
				Double catD=Double.valueOf(catS);
				categoryCurrent=catD.intValue();
			}
			else{ //else set view from first category
				logger.debug("no particulary category specified by slider: startFromEnd option is "+sbi.isSliderStartFromEnd());
				if(sbi.isSliderStartFromEnd()==true){
					// Category current is: number categories - categories to visualize + 1
					categoryCurrent=sbi.getCategoriesNumber()-(numberCatVisualization!=null ? numberCatVisualization.intValue() : 1) +1 ;
				}
				else{
					categoryCurrent=1;
				}
			}

			valueSlider=(new Integer(categoryCurrent)).toString();
			HashMap cats=(HashMap)((BarCharts)sbi).getCategories();


			if(categoryCurrent!=0){
				categoryCurrentName=(String)cats.get(new Integer(categoryCurrent));
				logger.debug("current category "+categoryCurrentName);
				copyDataset=(DefaultCategoryDataset)sbi.filterDataset(copyDataset,categories,categoryCurrent,numberCatVisualization.intValue());				
			}
			else{
				categoryCurrentName="All";
				valueSlider="1";
			}

			// Check if particular series has been chosen
			selectedSeries=new Vector();
			if(requestCont.getParameter("serie")!=null){
				Object[] cio=requestCont.getParameterValues("serie");
				//Convert array in vector
				for(int i=0;i<cio.length;i++){
					selectedSeries.add(cio[i].toString());
				}
			}
			else{
				//if(!sbiMode.equalsIgnoreCase("WEB") && !docComposition)
				selectedSeries.add("allseries");
			}


			// if selectedSerie contains allseries 
			if(selectedSeries.contains("allseries")){
				((BarCharts)sbi).setCurrentSeries(null);
			}
			else{	
				copyDataset=sbi.filterDatasetSeries(copyDataset,selectedSeries);	

			}
			// consider if drawing the slider
			if((catsnum.intValue())>numberCatVisualization.intValue()){
				logger.debug("slider is to be drawn");
				makeSlider=true;	    	
			}
			else if(sbi.isFilterCategories()==true && notDisappearSlider==true){
				logger.debug("slider is to be drawn");
				makeSlider=true;	    	
			}



			//gets the filter's style
			filterStyle=sbi.getFilterStyle();

			newDatasetMap.getDatasets().put(key, copyDataset);

		}

		dynamicNVisualization=sbi.isDynamicNumberCatVisualization();		

		// if seriesOrder is defined re-define the order!
		if(((BarCharts)sbi).getSeriesOrder()!=null){
			LinkedHashSet<String> newOrderedSet=new LinkedHashSet<String>();
			LinkedHashSet<String> seriesTemp=new LinkedHashSet<String>(series);
			ArrayList<String> order=((BarCharts)sbi).getSeriesOrder();
			for (Iterator iterator = order.iterator(); iterator.hasNext();) {
				String element = (String) iterator.next();
				if(seriesTemp.contains(element)){
					newOrderedSet.add(element);
				}
			}
			for (Iterator iterator = seriesTemp.iterator(); iterator.hasNext();) {
				String element2 = (String) iterator.next();
				if(!newOrderedSet.contains(element2)){
					newOrderedSet.add(element2);
				}
			}
			series=newOrderedSet;
		}

		logger.debug("OUT");
		return newDatasetMap;

	}



	public DatasetMap filteringClusterChart(HttpServletRequest request, ClusterCharts sbi, String sbiMode, boolean docComposition){

		AttributesContainer attributesContainerrequest=new AttributesContainer(request);

		DatasetMap dsMap=filteringClusterChartUtil(attributesContainerrequest, sbi, sbiMode, docComposition);

		return dsMap;
	}



	public DatasetMap filteringClusterChartUtil(AttributesContainer requestCont, ClusterCharts sbi, String sbiMode, boolean docComposition){
		logger.debug("IN");
		DefaultXYZDataset dataset=(DefaultXYZDataset)datasets.get("1");
		DefaultXYZDataset copyDataset=null;
		try {
			copyDataset = (DefaultXYZDataset)dataset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// get the selected series from request
		selectedSeries=new Vector();
		if(requestCont.getParameter("serie")!=null){
			Object[] cio=requestCont.getParameterValues("serie");
			//Convert array in vector
			for(int i=0;i<cio.length;i++){
				selectedSeries.add(cio[i].toString());
			}
		}
		else{
			//if(!sbiMode.equalsIgnoreCase("WEB") && !docComposition)
			selectedSeries.add("allseries");
		}

		int numSeries=dataset.getSeriesCount();

		// fill the vector containing current series
		series=new TreeSet();
		for(int i=0;i<numSeries;i++){
			if (this.getNumberSerVisualization() > 0 && i < this.getNumberSerVisualization()){
				String nome=(String)dataset.getSeriesKey(i);
				series.add(nome);	
			}
			else if (this.getNumberSerVisualization() == 0){ //tutte le serie
				String nome=(String)dataset.getSeriesKey(i);
				series.add(nome);	
			}

		}

		// if all series selected return the copy of dataset
		if(selectedSeries.contains("allseries")){
			DatasetMap newDatasetMap=this.copyDatasetMap(copyDataset);
			return newDatasetMap;
		}


		// if not all series limits to selected ones	
		for(int i=0;i<numSeries;i++){
			String nome=(String)dataset.getSeriesKey(i);

			if(!selectedSeries.contains(nome)){
				copyDataset.removeSeries(nome);
				//series.remove(nome);
			}

		}


		DatasetMap newDatasetMap=this.copyDatasetMap(copyDataset);
		logger.debug("OUT");
		return newDatasetMap;

	}

	
	
	public DatasetMap filteringGroupedBarChart(SourceBean aServiceResponse,HttpServletRequest request, StackedBarGroup sbi, String sbiMode, boolean docComposition){

		AttributesContainer attributesContainerrequest=new AttributesContainer(request);
		AttributesContainer attributesContainerResponse=new AttributesContainer(aServiceResponse);

		DatasetMap dsMap=filteringSimpleBarChartUtil(attributesContainerrequest, attributesContainerResponse, sbi, sbiMode, docComposition);

		return dsMap;
	}

	
	

	public DatasetMap filteringGroupedBarChartUtil(AttributesContainer requestCont,AttributesContainer responseCont, StackedBarGroup sbi, String sbiMode, boolean docComposition){
		logger.debug("IN");
		DefaultCategoryDataset dataset=(DefaultCategoryDataset)datasets.get("1");
		Dataset copyDataset=null;
		boolean notDisappearSlider=false;   // if n_visualization>=number total categories do not make slider disappear

		try {
			copyDataset = (DefaultCategoryDataset)dataset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		series=new TreeSet();
		//	series=new TreeSet(((DefaultCategoryDataset)dataset).getRowKeys());

		int contSer = 0;
		for (Iterator iterator2 = (((DefaultCategoryDataset)dataset).getRowKeys()).iterator(); iterator2.hasNext();) {
			if (this.getNumberSerVisualization() > 0 && contSer < this.getNumberSerVisualization()){
				String serie = (String) iterator2.next();
				if(!series.contains(serie)){
					series.add(serie);
					contSer++;
				}
			}
			else if (this.getNumberSerVisualization() == 0 ){
				String serie = (String) iterator2.next();
				if(!series.contains(serie))
					series.add(serie);
			}

		}


		//fill the serieNumber MAP by mapping each serie name to its position in the dataset, needed to recover right colors when redrawing
		/*	for(int i=0;i<series.size();i++){
			String s=(String)series.get(i);
			sbi.putSeriesNumber(s,(i+1));
		}*/

		categories=(HashMap)((BarCharts)sbi).getCategories();
		catsnum=new Integer(sbi.getRealCatNumber());

		//See if numberCatVisualization has to be updated
		if(responseCont.getAttribute("n_visualization")!=null){
			String nVis=(String)responseCont.getAttribute("n_visualization");
			Integer catD=Integer.valueOf(nVis);
			if(catD.equals(0))catD=new Integer(catsnum);
			sbi.setNumberCatVisualization(catD);
			if(catD>=catsnum)notDisappearSlider=true;
		}


		numberCatVisualization=sbi.getNumberCatVisualization();
		numberSerVisualization=sbi.getNumberSerVisualization(); 

		subCategories=(HashMap)((StackedBarGroup)sbi).getSubCategories();


		catTitle=sbi.getCategoryLabel();
		subcatTitle = sbi.getSubCategoryLabel();
		serTitle=sbi.getValueLabel();


		// if slider specifies a category than set view from that point

		if(responseCont!=null && responseCont.getAttribute("category")!=null){ // lastChange
			String catS=(String)responseCont.getAttribute("category");
			logger.debug("category specified in module response by slider "+catS);
			Double catD=Double.valueOf(catS);
			categoryCurrent=catD.intValue();
		}
		else if(requestCont.getParameter("categoryAll")!=null){
			logger.debug("All categories have to be shown");
			categoryCurrent=0;			
		}		
		else if(requestCont.getParameter("category")!=null){
			String catS=(String)requestCont.getParameter("category");
			logger.debug("category specified in request by slider "+catS);
			Double catD=Double.valueOf(catS);
			categoryCurrent=catD.intValue();
		}
		else{ //else set view from first category
			logger.debug("no particulary category specified by slider: startFromEnd option is "+sbi.isSliderStartFromEnd());
			if(sbi.isSliderStartFromEnd()==true){
				// Category current is: number categories - categories to visualize + 1
				categoryCurrent=sbi.getCategoriesNumber()-(numberCatVisualization!=null ? numberCatVisualization.intValue() : 1) +1 ;
			}
			else{
				categoryCurrent=1;
			}
		}
		valueSlider=(new Integer(categoryCurrent)).toString();
		HashMap cats=(HashMap)((BarCharts)sbi).getCategories();


		if(categoryCurrent!=0){
			categoryCurrentName=(String)cats.get(new Integer(categoryCurrent));
			logger.debug("current category "+categoryCurrentName);
			copyDataset=(DefaultCategoryDataset)sbi.filterDataset(copyDataset,categories,categoryCurrent,numberCatVisualization.intValue());				
		}
		else{
			categoryCurrentName="All";
			valueSlider="1";
		}

		// Check if particular series has been chosen
		selectedSeries=new Vector();
		if(requestCont.getParameter("serie")!=null){
			Object[] cio=requestCont.getParameterValues("serie");
			//Convert array in vector
			for(int i=0;i<cio.length;i++){
				selectedSeries.add(cio[i].toString());
			}
		}
		else{
			//if(!sbiMode.equalsIgnoreCase("WEB") || docComposition)
			selectedSeries.add("allseries");
		}


		// if selectedSerie contains allseries 
		if(selectedSeries.contains("allseries")){
			((BarCharts)sbi).setCurrentSeries(null);
		}
		else{	
			copyDataset=sbi.filterDatasetSeries(copyDataset,selectedSeries);	

		}
		// consider if drawing the slider
		if(sbi.isFilterCategories()==true && (catsnum.intValue())>numberCatVisualization.intValue()){
			makeSlider=true;	    	
		}
		else if(sbi.isFilterCategories()==true && notDisappearSlider==true){
			logger.debug("slider is to be drawn");
			makeSlider=true;	    	
		}

		//gets the filter's style
		filterStyle=sbi.getFilterStyle();

		if(copyDataset==null){copyDataset=dataset;}

		DatasetMap newDatasetMap=this.copyDatasetMap(copyDataset);


		// set if is dynamic Categories selection
		dynamicNVisualization=sbi.isDynamicNumberCatVisualization();		


		logger.debug("OUT");

		return newDatasetMap;

	}


	public HashMap getDatasets() {
		return datasets;
	}

	public void setDatasets(HashMap datasets) {
		this.datasets = datasets;
	}


	/**
	 * Called by chart.jsp to build the url for series filter
	 */

	public String getSerieUlr(String refreshUrl, Map refreshUrlPars){
		String toReturn=refreshUrl;
		refreshUrl+="&"+LightNavigationManager.LIGHT_NAVIGATOR_DISABLED+"=true";
		if(!refreshUrlPars.containsKey("category")){
			refreshUrl+="&category="+getCategoryCurrent();
		}
		//if(isDynamicNVisualization()==true){
		//}
		// checked all cats
		if(getCategoryCurrent()==0 && !refreshUrlPars.containsKey("cat_group")){
			//refreshUrl+="&categoryAll=0";			
		}
		return refreshUrl;
	}



	public String getCategoriesGroupUrl(String refreshUrl, Map refreshUrlPars){
		String toReturn=refreshUrl;
		refreshUrl+="&"+LightNavigationManager.LIGHT_NAVIGATOR_DISABLED+"=true";

		for(Iterator iterator = refreshUrlPars.keySet().iterator(); iterator.hasNext();)
		{
			String name = (String) iterator.next();
			String value=(refreshUrlPars.get(name)).toString();
			refreshUrl="&"+name+"="+value;
		}
		return refreshUrl;
	}




	public Set<String> getSeries() {
		return series;
	}

	public void setSeries(Set<String> series) {
		this.series = series;
	}

	public Integer getSeriesNumber() {
		return seriesNumber;
	}

	public void setSeriesNumber(Integer seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public HashMap getCategories() {
		return categories;
	}

	public void setCategories(HashMap categories) {
		this.categories = categories;
	}

	public Integer getCatsnum() {
		return catsnum;
	}

	public void setCatsnum(Integer catsnum) {
		this.catsnum = catsnum;
	}

	public Integer getNumberCatVisualization() {
		return numberCatVisualization;
	}

	public void setNumberCatVisualization(Integer numberCatVisualization) {
		this.numberCatVisualization = numberCatVisualization;
	}

	public String getCatTitle() {
		return catTitle;
	}

	public void setCatTitle(String catTitle) {
		this.catTitle = catTitle;
	}

	public String getSerTitle() {
		return serTitle;
	}

	public void setSerTitle(String serTitle) {
		this.serTitle = serTitle;
	}

	public int getCategoryCurrent() {
		return categoryCurrent;
	}

	public void setCategoryCurrent(int categoryCurrent) {
		this.categoryCurrent = categoryCurrent;
	}

	public String getValueSlider() {
		return valueSlider;
	}

	public void setValueSlider(String valueSlider) {
		this.valueSlider = valueSlider;
	}

	public String getCategoryCurrentName() {
		return categoryCurrentName;
	}

	public void setCategoryCurrentName(String categoryCurrentName) {
		this.categoryCurrentName = categoryCurrentName;
	}

	public Vector getSelectedSeries() {
		return selectedSeries;
	}

	public void setSelectedSeries(Vector selectedSeries) {
		this.selectedSeries = selectedSeries;
	}

	public boolean isMakeSlider() {
		return makeSlider;
	}

	public void setMakeSlider(boolean makeSlider) {
		this.makeSlider = makeSlider;
	}

	public Vector getSelectedCatGroups() {
		return selectedCatGroups;
	}

	public void setSelectedCatGroups(Vector selectedCatGroups) {
		this.selectedCatGroups = selectedCatGroups;
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

	public boolean isDynamicNVisualization() {
		return dynamicNVisualization;
	}

	public void setDynamicNVisualization(boolean dynamicNVisualization) {
		this.dynamicNVisualization = dynamicNVisualization;
	}



}
