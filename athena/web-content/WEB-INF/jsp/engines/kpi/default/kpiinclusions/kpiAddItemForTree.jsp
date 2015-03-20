<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%!//START RECURSIVE METHOD TO CREATE TREE STRUCTURE
//PERCENTAGE WIDTH OF EVERY COLUMN (SUM MUST BE 100%)
private static final String MODEL_COL_W = "53";
private static final String META_COL_W = "4";
private static final String VAL_COL_W = "9";
private static final String WEIGHT_COL_W = "5";
private static final String CHART_OR_IMAGE_COL_W = "22";
private static final String TREND_COL_W = "3";
private static final String DOC_COL_W = "2";
private static final String ALARM_COL_W = "2";
//in case both Chart and image are visible
private static final String CHART_COL_W = "15";
private static final String IMG_COL_W = "7";
//images path
private static final String ALARM_IMG_PATH = "/img/kpi/alarm.gif";
private static final String INFO_IMG_PATH = "/img/kpi/info.gif";
private static final String DOCLINKED_IMG_PATH = "/img/kpi/linkedDoc.gif";
private static final String TREND_IMG_PATH = "/img/kpi/trend.jpg";
//css classes
private static final String td_css_class = "kpi_td";
private static final String div_css_class = "kpi_div";
private static final String td_al_right_css_class = "kpi_td_right";
private static final String td_al_left_css_class = "kpi_td_left";
private static final String tr_even_css_class = "kpi_line_section_even";
private static final String tr_odd_css_class = "kpi_line_section_odd";
private static final String semaphor_div_css_class = "kpi_semaphore";
private static final String toggle_kpi_css_class = "toggleKPI";


//Method to construct a row of the Kpi Model Tree
public StringBuffer addItemForTree(String lineTagId,ExecutionInstance execInstance,String userId,int recursionLevel, 
		Boolean isEvenLine,HttpServletRequest httpReq,KpiLine line, StringBuffer _htmlStream,
		KpiLineVisibilityOptions options, String currTheme, HashMap parametersMap, Resource r, Date d, String metadata_publisher_Name,String trend_publisher_Name, String parsToDetailDocs) {
	
	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder();
	UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
	UUID uuid = uuidGen.generateTimeBasedUUID();
	String requestIdentity = uuid.toString();
	requestIdentity = requestIdentity.replaceAll("-", "");
	
	//IMAGE URLS
	String alarmImgSrc = urlBuilder.getResourceLinkByTheme(httpReq, ALARM_IMG_PATH,currTheme);
	String infoImgSrc = urlBuilder.getResourceLinkByTheme(httpReq, INFO_IMG_PATH,currTheme);
	String docImgSrc = urlBuilder.getResourceLinkByTheme(httpReq, DOCLINKED_IMG_PATH,currTheme);
	String trendImgSrc = urlBuilder.getResourceLinkByTheme(httpReq, TREND_IMG_PATH,currTheme);
	//IMAGE URLS
	
	//START VARIABLES INITIALIZATION
	String modelName = line.getModelNodeName();
	String modelCode = line.getModelInstanceCode();
	String timeRangeFrom = null;
	String timeRangeTo = null;	
	KpiValue kpiVal = line.getValue();
	String value = null;
	Float kpiValue =null;
	Double weightValue = null;
	List documents = line.getDocuments();
	Boolean alarm = line.getAlarm();
	List children = line.getChildren();
	String tab_name = "KPI_TABLE";
	boolean hasChildren = false;
	String scaleCode = "";
	String scalename = "";
	//END VARIABLES INITIALIZATION
	
	//START GETTING INTERNATIONALIZED MESSAGES
	String periodValid = msgBuilder.getMessage("sbi.kpi.validPeriod", httpReq);
	if (kpiVal !=null ){
		periodValid = periodValid.replaceAll("%0", kpiVal.getBeginDate().toString());
		periodValid = periodValid.replaceAll("%1", kpiVal.getEndDate().toString());		    
	}
	String weightTitle = msgBuilder.getMessage("sbi.kpi.weight", httpReq);
	String docLinkedTitle = msgBuilder.getMessage("sbi.kpi.docLinked", httpReq);
	String alarmTitle = msgBuilder.getMessage("sbi.kpi.alarmControl", httpReq);
	String trendTitle = msgBuilder.getMessage("sbi.kpi.trend", httpReq);
	//END GETTING INTERNATIONALIZED MESSAGES
	
	//START EVALUATING REAL VARIABLE VALUES
	if (parametersMap!=null && !parametersMap.isEmpty() && parametersMap.containsKey("TimeRangeFrom")){
	  timeRangeFrom = (String) parametersMap.get("TimeRangeFrom");
	}
	if (parametersMap!=null && !parametersMap.isEmpty() && parametersMap.containsKey("TimeRangeTo")){
	  timeRangeTo = (String) parametersMap.get("TimeRangeTo");
	}
	if(modelCode!=null && !modelCode.equals("")){
		modelName = modelCode+" - "+ modelName;
	}
	
	if (kpiVal!=null){
		value = kpiVal.getValue();
		scaleCode = kpiVal.getScaleCode();
		scalename = kpiVal.getScaleName();
		if(value!=null){
			Double val = new Double(value);
			kpiValue = new Float( val.floatValue());
			weightValue = kpiVal.getWeight();
			if(options.getWeighted_values().booleanValue()){
				kpiValue =new Float(val.doubleValue()*weightValue.doubleValue());
			}
		}	
	}	
	
	if(r!=null){
		tab_name = tab_name+r.getId();
	}			
	
	if (children!=null && !children.isEmpty()){
		hasChildren = true;
	}		
	
	//END EVALUATING REAL VARIABLE VALUES
	
	//BEGIN ROW
	if(isEvenLine.booleanValue()){
		 _htmlStream.append("	<tr class='"+tr_even_css_class+"' id='"+lineTagId+"' >\n");
	}else{
		_htmlStream.append("	<tr class='"+tr_odd_css_class+"' id='"+lineTagId+"' >\n");
	}
	
	//START ADDING COLUMNS
	_htmlStream = addModelNameColumn(_htmlStream,kpiVal,recursionLevel, modelName, options.getDisplay_semaphore().booleanValue(), hasChildren, lineTagId, tab_name);
	
	_htmlStream = addMetadataIconColumn(_htmlStream, recursionLevel, requestIdentity, kpiVal, options.getWeighted_values(), line.getModelInstanceNodeId(), userId,infoImgSrc,metadata_publisher_Name);	
	
	_htmlStream = addKpiValueColumn(_htmlStream, kpiValue,scaleCode , kpiVal, periodValid);
	
	_htmlStream = addKpiWeightColumn(_htmlStream, options.getDisplay_weight().booleanValue(), weightValue, weightTitle);
	
	_htmlStream = addBulletChartAndOrThresholdImageColumn( _htmlStream, options.getDisplay_bullet_chart().booleanValue(),options.getDisplay_threshold_image().booleanValue(),options.getShow_axis().booleanValue(),line, requestIdentity);
			
	_htmlStream = addTrendColumn( _htmlStream,recursionLevel, requestIdentity,userId,kpiVal,r,timeRangeTo,timeRangeFrom,d, trendTitle,trendImgSrc,trend_publisher_Name);
	
	_htmlStream = addDocumentDetailColumn( _htmlStream, execInstance,documents,kpiVal,timeRangeFrom, timeRangeTo,d, docLinkedTitle, docImgSrc, r,parsToDetailDocs);

	_htmlStream = addAlarmColumn(_htmlStream, alarm.booleanValue(), options.getDisplay_alarm().booleanValue(), alarmImgSrc,alarmTitle);
	//END ADDING COLUMNS
	
   _htmlStream.append("	</tr>\n");
   //END ROW

   //START ADDING TREE LEAVES
   if (children!=null && !children.isEmpty()){
	   children = orderChildren(new ArrayList(),children);
	   recursionLevel ++;
	   Iterator childIt = children.iterator();
	   while (childIt.hasNext()){
		   KpiLine l = (KpiLine)childIt.next();
		   String idTemp = lineTagId+"_child"+children.indexOf(l);
		   if (isEvenLine.booleanValue()){			   
			   addItemForTree(idTemp,execInstance,userId,recursionLevel,Boolean.FALSE,httpReq, l,_htmlStream,options,currTheme,parametersMap,r,d,metadata_publisher_Name,trend_publisher_Name,parsToDetailDocs);
		   }else{
			   addItemForTree(idTemp,execInstance,userId,recursionLevel,Boolean.TRUE,httpReq, l,_htmlStream,options,currTheme,parametersMap,r,d,metadata_publisher_Name,trend_publisher_Name,parsToDetailDocs);
		   }  
	   }
   } 
   //END ADDING TREE LEAVES
   return _htmlStream;
}

//Method to add the Model Name column with or without the semaphor
private StringBuffer addModelNameColumn( StringBuffer _htmlStream, KpiValue kpiVal, int recursionLevel, String modelName,boolean withSemaphore, boolean hasChildren, String lineTagId, String tab_name){

	String semaphorColorHex = null;
	if ( kpiVal!=null && kpiVal.getValue() != null) {
		Color semaphorColor = null;
		ThresholdValue t = kpiVal.getThresholdOfValue();
		if(t!=null){
			semaphorColor = t.getColor();
			semaphorColorHex ="rgb("+semaphorColor.getRed()+", "+semaphorColor.getGreen()+", "+semaphorColor.getBlue()+")" ;	
		}
	}
	
	if(withSemaphore && semaphorColorHex!=null){
		if (hasChildren){
			_htmlStream.append("<td width='"+MODEL_COL_W+"%' class='"+td_css_class+"' ><div class='"+semaphor_div_css_class+"' style=\"margin-left: "+20*recursionLevel+"px;background-color:"+semaphorColorHex+"\"></div><div  class='"+div_css_class+"'><span class='"+toggle_kpi_css_class+"' onclick=\"javascript:toggleHideChild('"+lineTagId+"','"+tab_name+"');\">&nbsp;</span>"+modelName+"</div></td>\n");
		}else{
			_htmlStream.append("<td width='"+MODEL_COL_W+"%' class='"+td_css_class+"' ><div class='"+semaphor_div_css_class+"' style=\"margin-left: "+20*recursionLevel+"px;background-color:"+semaphorColorHex+"\"></div><div  class='"+div_css_class+"'>"+modelName+"</div></td>\n");
		}
	}else{
		if (hasChildren){
			_htmlStream.append("<td width='"+MODEL_COL_W+"%'  class='"+td_css_class+"' ><div class='"+div_css_class+"'><div style='MARGIN-LEFT: "+20*recursionLevel+"px;text-align:left;' class='"+div_css_class+"'><span class='"+toggle_kpi_css_class+"' onclick=\"javascript:toggleHideChild('"+lineTagId+"','"+tab_name+"');\">&nbsp;</span>"+modelName+"</div></div></td>\n");
		}else{
			_htmlStream.append("<td width='"+MODEL_COL_W+"%'  class='"+td_css_class+"' ><div class='"+div_css_class+"'><div style='MARGIN-LEFT: "+20*recursionLevel+"px;text-align:left;' class='"+div_css_class+"'>"+modelName+"</div></div></td>\n");
		}
	}
	return _htmlStream;
}

//Method to add the Metadata button column 
private StringBuffer addMetadataIconColumn(StringBuffer _htmlStream,int recursionLevel,String requestIdentity,KpiValue kpiVal, Boolean isWeightedValue,Integer modelInstanceNodeId, String userId,String infoImgSrc,String metadata_publisher_Name){
	String valueDescr = "";
	if(kpiVal !=null && kpiVal.getKpiInstanceId()!=null){
		if(kpiVal.getValueDescr()!=null){
			valueDescr = kpiVal.getValueDescr();		
		}
		HashMap execUrlParMap = createParMapForMetadataWindow(kpiVal,modelInstanceNodeId,isWeightedValue.booleanValue(),metadata_publisher_Name);
		String metadataPopupUrl = createUrl_popup(execUrlParMap,userId);
		
		_htmlStream.append("<td  width='"+META_COL_W+"%'  class='"+td_css_class+"' title='"+valueDescr+"' style='text-align:center;' ><div class='"+div_css_class+"'  ><a id='linkMetadata_"+requestIdentity+"_"+recursionLevel+"' ><img src=\""+infoImgSrc+"\" /></div></a></td>\n");
		_htmlStream = addScriptForMetadataOpening(_htmlStream,recursionLevel,requestIdentity, metadataPopupUrl);
		
	}else{
		_htmlStream.append("<td width='"+META_COL_W+"%' class='"+td_css_class+"' title='"+valueDescr+"' ><div ></div></td>\n");				
	}
	return _htmlStream;
}

//Method to add the Kpi Value column
private StringBuffer addKpiValueColumn(StringBuffer _htmlStream,Float kpiValue,String scaleCode,KpiValue kpiVal,String periodValid){
	if (kpiValue!= null && kpiVal !=null && scaleCode!=null){
		String scaleToAppend = "";
		if(scaleCode.trim().equalsIgnoreCase("Ratio scale")){
			scaleToAppend = " (%)";
		}else if(scaleCode.trim().equalsIgnoreCase("Day scale")){

			scaleToAppend = " (gg)";
		}// else to be implemented for other types...
		_htmlStream.append("<td  width='"+VAL_COL_W+"%' title='"+periodValid+"' class='"+td_al_left_css_class+"' ><div  class='"+div_css_class+"'>"+kpiValue.toString()+scaleToAppend+"</div></td>\n");
	}else if(kpiValue!= null){
		_htmlStream.append("<td  width='"+VAL_COL_W+"%' title='"+periodValid+"' class='"+td_al_left_css_class+"' ><div  class='"+div_css_class+"'>"+kpiValue.toString()+"</div></td>\n");
	}else{
		_htmlStream.append("<td  width='"+VAL_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
	}		
	return _htmlStream;
}

//Method to add the kpi Weight column
private StringBuffer addKpiWeightColumn(StringBuffer _htmlStream, boolean displayWeight,Double weightValue,String weightTitle){
	if (displayWeight && weightValue!=null){
		_htmlStream.append("<td width='"+WEIGHT_COL_W+"%' title='"+weightTitle+"' class='"+td_al_left_css_class+"'  ><div  class='"+div_css_class+"'>["+weightValue.toString()+"]</div></td>\n");
	}else{
		_htmlStream.append("<td width='"+WEIGHT_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
	}
	return _htmlStream;
}

//Method to add the Chart and/or the threshold image column
private StringBuffer addBulletChartAndOrThresholdImageColumn(StringBuffer _htmlStream, boolean displayBulletChart,boolean displayThresholdImage,boolean showAxis,KpiLine line, String requestIdentity){
	
	if (line.getValue()!=null && line.getValue().getValue()!=null && line.getValue().getThresholdValues()!=null && !line.getValue().getThresholdValues().isEmpty()){
		if (displayBulletChart && displayThresholdImage ){	
			
			String urlChartPng = constructChartUrl(line,requestIdentity,showAxis,line.getValue());
			//_htmlStream.append("<td width='"+CHART_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='kpi_bulletchart'><img style=\"align:left;\" id=\"image\" src=\""+urlChartPng+"\" BORDER=\"1\" alt=\"Error in displaying the chart\" USEMAP=\"#chart\"/></div></td>\n");
			_htmlStream.append("<td width='"+CHART_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='kpi_bulletchart'><img style=\"align:left;\" id=\"image\" src=\""+urlChartPng+"\" BORDER=\"1\" USEMAP=\"#chart\"/></div></td>\n");
			
			ThresholdValue tOfVal = line.getThresholdOfValue();
			if (tOfVal!=null && tOfVal.getPosition()!=null && tOfVal.getThresholdCode()!=null){	
				String urlImagePng = constructImgUrl(tOfVal);
				//_htmlStream.append("<td width='"+IMG_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='kpi_image'><img style=\"align:left;\" id=\"image\" src=\""+urlImagePng+"\" alt=\"Error in displaying the chart\" USEMAP=\"#chart\" BORDER=\"1\" /></div></td>\n");
				_htmlStream.append("<td width='"+IMG_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='kpi_image'><img style=\"align:left;\" id=\"image\" src=\""+urlImagePng+"\" USEMAP=\"#chart\" BORDER=\"1\" /></div></td>\n");
			}				
		}else if(displayBulletChart && !displayThresholdImage){
			
			String urlChartPng = constructChartUrl(line,requestIdentity,showAxis, line.getValue());
			//_htmlStream.append("<td width='"+CHART_OR_IMAGE_COL_W+"%' class='"+td_al_left_css_class+"'  ><div class='kpi_bulletchart'><img style=\"align:left;\" id=\"image\" src=\""+urlChartPng+"\" BORDER=\"1\" alt=\"Error in displaying the chart\" USEMAP=\"#chart\"/></div></td>\n");				
			_htmlStream.append("<td width='"+CHART_OR_IMAGE_COL_W+"%' class='"+td_al_left_css_class+"'  ><div class='kpi_bulletchart'><img style=\"align:left;\" id=\"image\" src=\""+urlChartPng+"\" BORDER=\"1\" USEMAP=\"#chart\"/></div></td>\n");				
			
		}else if(!displayBulletChart && displayThresholdImage){
			
			ThresholdValue tOfVal = line.getThresholdOfValue();
			if (tOfVal!=null && tOfVal.getPosition()!=null && tOfVal.getThresholdCode()!=null){
				String urlImagePng = constructImgUrl(tOfVal);
				//_htmlStream.append("<td width='"+CHART_OR_IMAGE_COL_W+"%' class='"+td_al_left_css_class+"'  ><div class='kpi_image'><img style=\"align:left;\" id=\"image\" src=\""+urlImagePng+"\" alt=\"Error in displaying the chart\" USEMAP=\"#chart\" BORDER=\"1\" /></div></td>\n");				
				_htmlStream.append("<td width='"+CHART_OR_IMAGE_COL_W+"%' class='"+td_al_left_css_class+"'  ><div class='kpi_image'><img style=\"align:left;\" id=\"image\" src=\""+urlImagePng+"\" USEMAP=\"#chart\" BORDER=\"1\" /></div></td>\n");				
				
			}
		}
	}else{
		if (displayBulletChart && displayThresholdImage){
			_htmlStream.append("<td width='"+CHART_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
			_htmlStream.append("<td width='"+IMG_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
		}else{
			_htmlStream.append("<td width='"+CHART_OR_IMAGE_COL_W+"%' class='"+td_al_left_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
		}
	}
	return _htmlStream;
}

//Method to add the Trend button column
private StringBuffer addTrendColumn(StringBuffer _htmlStream,int recursionLevel,String requestIdentity,String userId, KpiValue kpiVal,Resource r,String timeRangeTo, String timeRangeFrom, Date d, String trendTitle, String trendImgSrc, String trend_publisher_Name){
	
	if(kpiVal!=null && kpiVal.getValue()!= null){		
		HashMap execUrlParMap = createParMapForTrendWindow(r,d,timeRangeFrom,timeRangeTo,kpiVal,trend_publisher_Name);
		String trendPopupUrl = createUrl_popup(execUrlParMap,userId);
		_htmlStream.append("<td width='"+TREND_COL_W+"%' class='"+td_css_class+"' title=\""+trendTitle+"\" style='text-align:center;' ><div class='"+div_css_class+"' ><a id='linkDetail_"+requestIdentity+"_"+recursionLevel+"' ><img  src=\""+trendImgSrc+"\" /></div></a></td>\n");
		_htmlStream = addScriptForTrendWindowOpening(_htmlStream,recursionLevel,requestIdentity, trendPopupUrl);
	}else{
		_htmlStream.append("<td width='"+TREND_COL_W+"%' class='"+td_css_class+"' ><div></div></td>\n");
	}
	return _htmlStream;
}

//Method to add the Document detail button column
private StringBuffer addDocumentDetailColumn(StringBuffer _htmlStream, ExecutionInstance execInstance,List documents,KpiValue kpiVal,String timeRangeFrom, String timeRangeTo,Date d, String docTitle, String docImgSrc, Resource r, String parsToDetailDocs){
	
	if (documents!=null && !documents.isEmpty()){
		_htmlStream.append("<td width='"+DOC_COL_W+"%'  class='"+td_css_class+"' ><div class='"+div_css_class+"'  >\n");
		Iterator it = documents.iterator();
		while(it.hasNext()){
			String docLabel =(String)it.next();
			String parameters = createParStringForCrossNavigation(execInstance, timeRangeFrom, timeRangeTo, r, kpiVal, d, parsToDetailDocs);		
			String docHref="javascript:parent.execCrossNavigation(this.name,'"+docLabel+"','"+parameters+"');";
			_htmlStream.append("<a  title='"+docLabel+"' href=\""+docHref+"\"> <img  src=\""+docImgSrc+"\" alt=\""+docLabel+"\" /></a>\n");				
		}
		_htmlStream.append("</div></td>\n");
	}else{
		_htmlStream.append("<td width='"+DOC_COL_W+"%' class='"+td_al_right_css_class+"' ><div style='align:left;' class='"+div_css_class+"'></div></td>\n");
	}
	return _htmlStream;
}

//Method to add the Alarm icon column
private StringBuffer addAlarmColumn(StringBuffer _htmlStream,boolean existentAlarm, boolean displayAlarm,String alarmImgSrc,String alarmTitle){
	
	if (displayAlarm){
		if(existentAlarm) _htmlStream.append("<td width='"+ALARM_COL_W+"%' title='"+alarmTitle+"' style='text-align:center;' class='"+td_al_right_css_class+"' ><div class='"+div_css_class+"' ><img  src=\""+alarmImgSrc+"\" alt=\"Kpi under Alarm Control\" /></div></td>\n");
		else _htmlStream.append("<td width='"+ALARM_COL_W+"%' class='"+td_al_right_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
	}else{
		_htmlStream.append("<td width='"+ALARM_COL_W+"%' class='"+td_al_right_css_class+"' ><div class='"+div_css_class+"'></div></td>\n");
	}
	return _htmlStream;
}

//Method that creates the parameters map for the metadata url 
private HashMap createParMapForMetadataWindow(KpiValue kpiVal,Integer modelInstanceNodeId,boolean isWeightedValue,String metadata_publisher_Name){
	
	HashMap execUrlParMap = new HashMap();
	String valueDescr = "";
	if(kpiVal.getValueDescr()!=null){
		valueDescr = kpiVal.getValueDescr();		
	}		
	execUrlParMap.put(ObjectsTreeConstants.ACTION, "GET_KPI_METADATA");
	execUrlParMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	execUrlParMap.put("metadata_publisher_Name", metadata_publisher_Name);	
	execUrlParMap.put("KPI_VALUE_DESCR", valueDescr);	
	if (kpiVal!=null){
		execUrlParMap.put("KPI_BEGIN_DATE",kpiVal.getBeginDate() !=null ? kpiVal.getBeginDate().toString():"");
		execUrlParMap.put("KPI_END_DATE",kpiVal.getEndDate() !=null ? kpiVal.getEndDate().toString():"");
		execUrlParMap.put("KPI_INST_ID", kpiVal.getKpiInstanceId()!=null ? kpiVal.getKpiInstanceId().toString():"");
		execUrlParMap.put("KPI_VALUE",kpiVal.getValue()!=null ? kpiVal.getValue():"");
		execUrlParMap.put("KPI_WEIGHT",kpiVal.getWeight()!=null ? kpiVal.getWeight().toString():"");
		execUrlParMap.put("WEIGHTED_VALUE",new Boolean(isWeightedValue));
		execUrlParMap.put("KPI_TARGET",kpiVal.getTarget()!=null ? kpiVal.getTarget().toString():"");
	}		
	execUrlParMap.put("KPI_MODEL_INST_ID",modelInstanceNodeId!=null ? modelInstanceNodeId.toString():"");		
	
	return execUrlParMap;
}

//Method that creates the parameters map for the trend url 
private HashMap createParMapForTrendWindow(Resource r,Date d,String timeRangeFrom, String timeRangeTo, KpiValue kpiVal,String trend_publisher_Name){
	
	HashMap execUrlParMap = new HashMap();
	String format = (SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
	SimpleDateFormat f = new SimpleDateFormat();
	f.applyPattern(format);	
	
	execUrlParMap.put(ObjectsTreeConstants.ACTION, "GET_TREND");
	execUrlParMap.put("LIGHT_NAVIGATOR_DISABLED", "true");
	execUrlParMap.put("trend_publisher_Name", trend_publisher_Name);	
	
	if (r!=null){
		execUrlParMap.put("RESOURCE_ID",r.getId()!=null ? r.getId().toString(): "");
		execUrlParMap.put("RESOURCE_NAME", r.getName());
	}		
	if (d!=null){	
	    String dat = f.format(d);
	    execUrlParMap.put("END_DATE", dat);						
	}
	if (timeRangeFrom!=null && timeRangeTo!=null){
		try{
			Date timeR_From = f.parse(timeRangeFrom);
			Date timeR_To = f.parse(timeRangeTo);
			if (timeR_From.before(timeR_To)){
				execUrlParMap.put("TimeRangeFrom", timeRangeFrom);	
				execUrlParMap.put("TimeRangeTo", timeRangeTo);	
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	if (kpiVal!=null){
		execUrlParMap.put("KPI_INST_ID",kpiVal.getKpiInstanceId()!=null ? kpiVal.getKpiInstanceId().toString():"");
	}

	return execUrlParMap;
}

//Method that creates the parameters String for the detail document cross navigation
private String createParStringForCrossNavigation(ExecutionInstance execInstance,String timeRangeFrom, String timeRangeTo, Resource r, KpiValue kpiVal, Date d, String parsToDetailDocs){
	String parameters = "";
	
	String executionFlowId = execInstance.getFlowId();
	String uuidPrincip = execInstance.getExecutionId();
	String format = (SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
	SimpleDateFormat f = new SimpleDateFormat();
	f.applyPattern(format);
	if(parsToDetailDocs!= null && parsToDetailDocs!=""){
		parameters += parsToDetailDocs;
	}
	parameters +="EXECUTION_FLOW_ID="+executionFlowId;
	parameters +="&SOURCE_EXECUTION_ID="+uuidPrincip;
	if(kpiVal!=null){
		parameters +="&KPI_VALUE_ID="+(kpiVal.getKpiValueId()!=null? kpiVal.getKpiValueId().toString():"-1");	
	}
	
	if (r!=null){
		parameters +="&ParKpiResource="+r.getName();
	}	
	if (d!=null){						
	    String dat = f.format(d);
	    parameters +="&ParKpiDate="+dat;
	}
	if (timeRangeFrom!=null && timeRangeTo!=null){
		try {
			Date timeR_From = f.parse(timeRangeFrom);
			Date timeR_To = f.parse(timeRangeTo);
			if (timeR_From.before(timeR_To)){
				 parameters +="&TimeRangeFrom="+timeRangeFrom;
				 parameters +="&TimeRangeTo="+timeRangeTo;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	return parameters;
}

//Method that constructs the EXT script to open the metadata window
private StringBuffer addScriptForMetadataOpening(StringBuffer _htmlStream,int recursionLevel,String requestIdentity, String metadataPopupUrl){
	
	// insert javascript for open popup window for the trend
    _htmlStream.append(" <script>\n");
    _htmlStream.append("   var winMetadata"+requestIdentity+"_"+recursionLevel+"; \n");
    _htmlStream.append("Ext.get('linkMetadata_"+requestIdentity+"_"+recursionLevel+"').on('click', function(){ \n");
    _htmlStream.append("   if ( winMetadata"+requestIdentity+"_"+recursionLevel+" == null ) {winMetadata"+requestIdentity+"_"+recursionLevel+"=new Ext.Window({id:'winMetadata"+requestIdentity+"_"+recursionLevel+"',\n");
    _htmlStream.append("            bodyCfg:{ \n" );
    _htmlStream.append("                tag:'div' \n");
    _htmlStream.append("                ,cls:'x-panel-body' \n");
    _htmlStream.append("               ,children:[{ \n");
    _htmlStream.append("                    tag:'iframe', \n");
    _htmlStream.append("                    name: 'dynamicIframe"+requestIdentity+"_"+recursionLevel+"', \n");
    _htmlStream.append("                    id  : 'dynamicIframe"+requestIdentity+"_"+recursionLevel+"', \n");
    _htmlStream.append("                    src: '"+metadataPopupUrl+"', \n");
    _htmlStream.append("                    frameBorder:0, \n");
    _htmlStream.append("                    width:'100%', \n");
    _htmlStream.append("                    height:'100%', \n");
    _htmlStream.append("                    style: {overflow:'auto'}  \n ");        
    _htmlStream.append("               }] \n");
    _htmlStream.append("            }, \n");
    _htmlStream.append("            modal: true,\n");
    _htmlStream.append("            layout:'fit',\n");
	_htmlStream.append("            height:380,\n");
	_htmlStream.append("            width:500,\n");
	_htmlStream.append("            closeAction:'hide',\n");
    _htmlStream.append("            scripts: true, \n");
    _htmlStream.append("            plain: true \n");       
    _htmlStream.append("        }); }; \n");
    _htmlStream.append("   winMetadata"+requestIdentity+"_"+recursionLevel+".show(); \n");
    _htmlStream.append("	} \n");
    _htmlStream.append(");\n");
    _htmlStream.append(" </script>\n");
	return _htmlStream;
}

//Method that constructs the EXT script to open the trend window
private StringBuffer addScriptForTrendWindowOpening(StringBuffer _htmlStream,int recursionLevel,String requestIdentity, String trendPopupUrl){
	// insert javascript for open popup window for the trend
    _htmlStream.append(" <script>\n");
    _htmlStream.append("   var win"+requestIdentity+"_"+recursionLevel+"; \n");
    _htmlStream.append("Ext.get('linkDetail_"+requestIdentity+"_"+recursionLevel+"').on('click', function(){ \n");
    _htmlStream.append("   if ( win"+requestIdentity+"_"+recursionLevel+" == null ) {win"+requestIdentity+"_"+recursionLevel+"=new Ext.Window({id:'win"+requestIdentity+"_"+recursionLevel+"',\n");
    _htmlStream.append("            bodyCfg:{ \n" );
    _htmlStream.append("                tag:'div' \n");
    _htmlStream.append("                ,cls:'x-panel-body' \n");
    _htmlStream.append("               ,children:[{ \n");
    _htmlStream.append("                    tag:'iframe', \n");
    _htmlStream.append("                    name: 'dynamicIframe"+requestIdentity+"_"+recursionLevel+"', \n");
    _htmlStream.append("                    id  : 'dynamicIframe"+requestIdentity+"_"+recursionLevel+"', \n");
    _htmlStream.append("                    src: '"+trendPopupUrl+"', \n");
    _htmlStream.append("                    frameBorder:0, \n");
    _htmlStream.append("                    width:'100%', \n");
    _htmlStream.append("                    height:'100%', \n");
    _htmlStream.append("                    style: {overflow:'auto'}  \n ");        
    _htmlStream.append("               }] \n");
    _htmlStream.append("            }, \n");
    _htmlStream.append("            modal: true,\n");
    _htmlStream.append("            layout:'fit',\n");
	_htmlStream.append("            height:360,\n");
	_htmlStream.append("            width:500,\n");
	_htmlStream.append("            closeAction:'hide',\n");
    _htmlStream.append("            scripts: true, \n");
    _htmlStream.append("            plain: true \n");       
    _htmlStream.append("        }); }; \n");
    _htmlStream.append("   win"+requestIdentity+"_"+recursionLevel+".show(); \n");
    _htmlStream.append("	} \n");
    _htmlStream.append(");\n");
    _htmlStream.append(" </script>\n");
    return _htmlStream;
}

//Method that saves the chart image in a temporary directory and creates the url to retrieve it 
private String constructChartUrl(KpiLine line, String requestIdentity,boolean showAxis, KpiValue kpiVal){
	
	String urlChartPng = "";
	if ( kpiVal!=null &&  kpiVal.getValue()!= null && kpiVal.getThresholdValues()!=null && !kpiVal.getThresholdValues().isEmpty()) {

		List thresholdValues = kpiVal.getThresholdValues();			
		// String chartType = value.getChartType(); 
		String chartType = "BulletGraph";
		Double val = new Double(kpiVal.getValue());
		Double target = kpiVal.getTarget();
		ChartImpl sbi = ChartImpl.createChart(chartType);
		sbi.setValueDataSet(val);
		if (target != null) {
			sbi.setTarget(target);
		}
		sbi.setShowAxis(showAxis);	
		sbi.setThresholdValues(thresholdValues);
	
		JFreeChart chart = sbi.createChart();
		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
		String path_param = requestIdentity;
		String dir=System.getProperty("java.io.tmpdir");
		String path=dir+"/"+requestIdentity+".png";
		java.io.File file1 = new java.io.File(path);
		try {
			if(!showAxis){
				ChartUtilities.saveChartAsPNG(file1, chart, 200, 16, info);
			}else{
				ChartUtilities.saveChartAsPNG(file1, chart, 200, 30, info);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		urlChartPng=GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
		"?ACTION_NAME=GET_PNG2&NEW_SESSION=TRUE&path="+path_param+"&LIGHT_NAVIGATOR_DISABLED=TRUE";
    }
	return urlChartPng;
}

//Method that constructs the url to retrieve the threshold image 
private String constructImgUrl(ThresholdValue tOfVal){
	String urlImagePng = "";
	String fileName ="position_"+tOfVal.getPosition().intValue();
	String dirName = tOfVal.getThresholdCode();
	urlImagePng=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
	"?ACTION_NAME=GET_THR_IMAGE&NEW_SESSION=TRUE&fileName="+fileName+"&dirName="+dirName+"&LIGHT_NAVIGATOR_DISABLED=TRUE";
	return urlImagePng;
}

//Method that constructs the parameter string starting from an HashMap
public String createUrl_popup(HashMap paramsMap, String userid) {

	String url = GeneralUtilities.getSpagoBIProfileBaseUrl(userid);
	if (paramsMap != null){
		Iterator keysIt = paramsMap.keySet().iterator();
		String paramName = null;
		Object paramValue = null;
		while (keysIt.hasNext()){
			paramName = (String)keysIt.next();
			paramValue = paramsMap.get(paramName); 
			url += "&"+paramName+"="+paramValue.toString();
		}
	}
	return url;
}

//Method that oreders children in Model node code's alphabetical order
public List orderChildren(List ordered, List notordered) {
	
	List toReturn = ordered;
	List temp = new ArrayList();
	KpiLine l = null;
	if(notordered!=null && !notordered.isEmpty()){
		Iterator it = notordered.iterator();
		while(it.hasNext()){
			KpiLine k = (KpiLine)it.next();
			if(l==null){
				l = k ;
			}else{
				if (k!=null && k.compareTo(l)<=0){
					temp.add(l);
					l = k;
				}else{
					temp.add(k);
				}
			}
		}
		toReturn.add(l);
		toReturn = orderChildren(toReturn,temp);
	}
	return toReturn;
}%>