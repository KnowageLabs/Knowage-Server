<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.tools.scheduler.to.TriggerInfo"%>
<%@page import="it.eng.spagobi.tools.scheduler.to.JobInfo"%>
<%@page import="it.eng.spagobi.tools.scheduler.to.DispatchContext"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.commons.dao.IDomainDAO"%>
<%@page import="it.eng.spagobi.tools.distributionlist.bo.DistributionList"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="it.eng.spago.util.StringUtils"%>
<%@page import="java.text.ParseException"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.IDataSet"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>

<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("TriggerManagementModule"); 
	TriggerInfo triggerInfo = (TriggerInfo)aSessionContainer.getAttribute(SpagoBIConstants.TRIGGER_INFO);
	JobInfo jobInfo = triggerInfo.getJobInfo();
	List jobBiobjects = jobInfo.getDocuments();
	Map saveOptions = triggerInfo.getSaveOptions();
	
	Map backUrlPars = new HashMap();
	backUrlPars.put("LIGHT_NAVIGATOR_BACK_TO", "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
	Map formUrlPars = new HashMap();
	//formUrlPars.put("PAGE", "TriggerManagementPage");
	//formUrlPars.put("MESSAGEDET", SpagoBIConstants.MESSAGE_SAVE_SCHEDULE);
	//formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	String formUrl = urlBuilder.getUrl(request, formUrlPars);  
	String origStartStr = triggerInfo.getStartDate();
	
	String origStartTime = triggerInfo.getStartTime();

	SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy hh:mm");
	long origTime = 0;
	if(origStartStr != null && !origStartStr.trim().equals("")){
		Date dd = sdf.parse(origStartStr+" "+origStartTime);
		origTime = dd.getTime();
	}
	String message = msgBuilder.getMessage("scheduler.reschedule.date.alert", "component_scheduler_messages", request);
%>

<!-- ********************** SCRIPT FOR DOJO **************************** -->

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "/js/dojo/dojo.js" )%>"></script>

<script type="text/javascript">
       dojo.require("dojo.widget.DropdownDatePicker");
       dojo.require("dojo.widget.DropdownTimePicker");
</script>


<!-- ********************** SCRIPT FOR TABS **************************** -->

<script>
	tabOpened = ""; 
	indexTabOpened = 0;
	
	function changeTab(biobjid) {
		if(tabOpened==biobjid) {
      		return;
    	}
		document.getElementById('areabiobj'+biobjid).style.display="inline";
		document.getElementById('areabiobj'+tabOpened).style.display="none";
		document.getElementById('tabbiobj'+biobjid).className="tab selected";
		document.getElementById('tabbiobj'+tabOpened).className="tab";
		tabOpened = biobjid;
	}
	
</script>


<!-- ********************** SCRIPT FOR SAVE **************************** -->

<script>



	function saveCall() {
		var form = document.getElementById('triggerdetailform');
		var origDt = <%=origTime%>;
		if(origDt != 0){
			var newDt = form.startdate.value;
			var newTm = form.starttime.value;
			if(newTm.indexOf('+') != -1){
				newTm = newTm.substr(0, newTm.indexOf('+'));
			}
	
			var str = newDt+' '+ newTm;
			var newTimestamp = Date.parseDate(str, 'd/m/Y g:i:s');
			var oldDate = new Date(origDt);
			
			var answer = false;
			//current date
			var currentDt = new Date();
			if(newTimestamp.getElapsed(oldDate) == 0 || newTimestamp < currentDt ){
				answer = confirm('<%=message%>');
				if(!answer){			
					chronStr = getRepetitionString();	
					$('chronstring').value=chronStr;
					document.getElementById('triggerdetailform').submit();
					return;
				}else{			
					return;
				}
			}
		}
		chronStr = getRepetitionString();	
		$('chronstring').value=chronStr;
		document.getElementById('triggerdetailform').submit();
	}

    function getRepetitionString() {
        repStr = '';
    	if($('single_repetitionKind').checked) {
    		repStr = repStr + 'single{}';
    	}
    	if($('minute_repetitionKind').checked) {
    		repStr = repStr + 'minute{';
    		rep_n = $('minute_repetition_n').options[$('minute_repetition_n').selectedIndex].value;
    		repStr = repStr + 'numRepetition='+rep_n;
    		repStr = repStr + '}';
    	}
    	if($('hour_repetitionKind').checked) {
    		repStr = repStr + 'hour{';
    		rep_n = $('hour_repetition_n').options[$('hour_repetition_n').selectedIndex].value;
    		repStr = repStr + 'numRepetition='+rep_n;
    		repStr = repStr + '}';
    	}
    	if($('day_repetitionKind').checked) {
    		repStr = repStr + 'day{';
    		rep_n = $('day_repetition_n').options[$('day_repetition_n').selectedIndex].value;
    		repStr = repStr + 'numRepetition='+rep_n;
    		repStr = repStr + '}';
    	}
    	if($('week_repetitionKind').checked) {
    		repStr = repStr + 'week{';
    		rep_n = $('week_repetition_n').options[$('week_repetition_n').selectedIndex].value;
    		repStr = repStr + 'numRepetition='+rep_n+';days=';
    		
    		if($('day_in_week_rep_sun').checked) {
    			repStr = repStr + 'SUN,';
    		}
    		if($('day_in_week_rep_mon').checked) {
    			repStr = repStr + 'MON,';
    		}
    		if($('day_in_week_rep_tue').checked) {
    			repStr = repStr + 'TUE,';
    		}
    		if($('day_in_week_rep_wed').checked) {
    			repStr = repStr + 'WED,';
    		}
    		if($('day_in_week_rep_thu').checked) {
    			repStr = repStr + 'THU,';
    		}
    		if($('day_in_week_rep_fri').checked) {
    			repStr = repStr + 'FRI,';
    		}
    		if($('day_in_week_rep_sat').checked) {
    			repStr = repStr + 'SAT,';
    		}
    		repStr = repStr + '}';
    	}
    	if($('month_repetitionKind').checked) {
    		repStr = repStr + 'month{';
    		if($('month_selection_interval').checked) {
    			rep_n = $('monthrep_n').options[$('monthrep_n').selectedIndex].value;
    			repStr = repStr + 'numRepetition='+rep_n+';';
    			repStr = repStr + 'months=NONE;';
    		}
    		if($('month_selection_checks').checked) {
    			repStr = repStr + 'numRepetition=0;';
    			repStr = repStr + 'months=';
    			if($('monthrep_jan').checked) repStr = repStr + 'JAN,';
    			if($('monthrep_feb').checked) repStr = repStr + 'FEB,';
    			if($('monthrep_mar').checked) repStr = repStr + 'MAR,';
    			if($('monthrep_apr').checked) repStr = repStr + 'APR,';
    			if($('monthrep_may').checked) repStr = repStr + 'MAY,';
    			if($('monthrep_jun').checked) repStr = repStr + 'JUN,';
    			if($('monthrep_jul').checked) repStr = repStr + 'JUL,';
    			if($('monthrep_aug').checked) repStr = repStr + 'AUG,';
    			if($('monthrep_sep').checked) repStr = repStr + 'SEP,';
    			if($('monthrep_oct').checked) repStr = repStr + 'OCT,';
    			if($('monthrep_nov').checked) repStr = repStr + 'NOV,';
    			if($('monthrep_dic').checked) repStr = repStr + 'DIC,';	
    			repStr = repStr + ';';
    		}
    		if($('dayinmonth_selection_interval').checked) {
    			rep_n = $('dayinmonthrep_n').options[$('dayinmonthrep_n').selectedIndex].value;
    			repStr = repStr + 'dayRepetition='+rep_n+';';
    			repStr = repStr + 'weeks=NONE;';
    			repStr = repStr + 'days=NONE;';
    		}
    		if($('dayinmonth_selection_checks').checked) {
    			repStr = repStr + 'dayRepetition=0;';
    			repStr = repStr + 'weeks=';
    			weekstr = '';
    			if($('dayinmonthrep_week1').checked) weekstr = weekstr + '1';
    			if($('dayinmonthrep_week2').checked) weekstr = weekstr + '2';
    			if($('dayinmonthrep_week3').checked) weekstr = weekstr + '3';
    			if($('dayinmonthrep_week4').checked) weekstr = weekstr + '4';
    			if($('dayinmonthrep_weekL').checked) weekstr = weekstr + 'L';
    			if(weekstr=='') weekstr='NONE';
    			repStr = repStr + weekstr + ';';
    			repStr = repStr + 'days=';
    			daystr = '';
    			if($('dayinmonthrep_sun').checked) daystr = daystr + 'SUN,';
    			if($('dayinmonthrep_mon').checked) daystr = daystr + 'MON,';
    			if($('dayinmonthrep_tue').checked) daystr = daystr + 'TUE,';
    			if($('dayinmonthrep_wed').checked) daystr = daystr + 'WED,';
    			if($('dayinmonthrep_thu').checked) daystr = daystr + 'THU,';
    			if($('dayinmonthrep_fri').checked) daystr = daystr + 'FRI,';
    			if($('dayinmonthrep_sat').checked) daystr = daystr + 'SAT,';
    			if(daystr=='') daystr='NONE';
    			repStr = repStr + daystr + ';';
    		}
    		repStr = repStr + '}';
    	}
    	return repStr;
    }
    
    
    
    
    function selectOption(selobj, val) {
    	opts = selobj.options;
    	indsel = 0;
    	for(i=0; i<opts.length; i++) {
    		opt = opts[i];
    		if(opt.value == val) {
    			indsel= i;
    		}
    	}
    	selobj.selectedIndex=indsel;
    }
    
    
    
    
    
    function fillFormFromRepetitionString(repStr) {

    	type = '';
    	params = '';
    	if(repStr.indexOf('{')!=-1) {
    		indFirstBra = repStr.indexOf('{');
    		type = repStr.substring(0, indFirstBra);
    		params = repStr.substring((indFirstBra+1), (repStr.length-1));
    	} else {
    		return;
    	}
    	if(type=='single') {
    		$('single_repetitionKind').checked=true;
    	}
    	if(type=='minute') {
    		$('minute_repetitionKind').checked=true;
    		indeq = params.indexOf('=');
    		numrep = params.substring(indeq+1);
    		selectOption($('minute_repetition_n'), numrep);
    		openrepetitionform('minute');
    	}
    	if(type=='hour') {
    		$('hour_repetitionKind').checked=true;
    		indeq = params.indexOf('=');
    		numrep = params.substring(indeq+1);
    		selectOption($('hour_repetition_n'), numrep);
    		openrepetitionform('hour');
    	}
    	if(type=='day') {
    		$('day_repetitionKind').checked=true;
    		indeq = params.indexOf('=');
    		numrep = params.substring(indeq+1);
    		selectOption($('day_repetition_n'), numrep);
    		openrepetitionform('day');
    	}
    	if(type=='week') {
    		$('week_repetitionKind').checked=true;
    		indeq = params.indexOf('=');
    		indsplit = params.indexOf(';');
    		ind2eq = params.indexOf('=', (indeq + 1));
    		numrep = params.substring((indeq+1), indsplit);
    		daysstr = params.substring(ind2eq+1);
    		openrepetitionform('week');
    		selectOption($('week_repetition_n'), numrep);
    		days = daysstr.split(',');
    		for(j=0; j<days.length; j++) {
    			day = days[j];
    			if((day!=null) && (day!='')) {
    				if(day=='SUN') $('day_in_week_rep_sun').checked = 'true'; 
    				if(day=='MON') $('day_in_week_rep_mon').checked = 'true'; 
    				if(day=='TUE') $('day_in_week_rep_tue').checked = 'true'; 
    				if(day=='WED') $('day_in_week_rep_wed').checked = 'true'; 
    				if(day=='THU') $('day_in_week_rep_thu').checked = 'true'; 
    				if(day=='FRI') $('day_in_week_rep_fri').checked = 'true'; 
    				if(day=='SAT') $('day_in_week_rep_sat').checked = 'true';
    			}
    		}
    	}
    	if(type=='month') {
    		$('month_repetitionKind').checked=true;
    		openrepetitionform('month');
    		parchuncks = params.split(';');
	    	for(ind=0; ind<parchuncks.length; ind++) {
	    		parchunk = parchuncks[ind];
	    		singleparchunks = parchunk.split('=');
	    		key = singleparchunks[0];
	    		value = singleparchunks[1];
	    		if(key=='numRepetition') {
	    			if(value!='0') {
	    				$('month_selection_interval').checked = true;
	    				selectOption($('monthrep_n'), value);
	    			} 
	    		}
	    		if(key=='months'){
	    			if(value!='NONE') {
	    				$('month_selection_checks').checked = true;
	    				months = value.split(',');
	    				for(j=0; j<months.length; j++) {
    						month = months[j];
    						if((month!=null) && (month!='')) {
			    				if(month=='JAN') $('monthrep_jan').checked = 'true';
			    				if(month=='FEB') $('monthrep_feb').checked = 'true';
			    				if(month=='MAR') $('monthrep_mar').checked = 'true';
			    				if(month=='APR') $('monthrep_apr').checked = 'true';
			    				if(month=='MAY') $('monthrep_may').checked = 'true';
			    				if(month=='JUN') $('monthrep_jun').checked = 'true';
			    				if(month=='JUL') $('monthrep_jul').checked = 'true';
			    				if(month=='AUG') $('monthrep_aug').checked = 'true';
			    				if(month=='SEP') $('monthrep_sep').checked = 'true';
			    				if(month=='OCT') $('monthrep_oct').checked = 'true';
			    				if(month=='NOV') $('monthrep_nov').checked = 'true';
			    				if(month=='DIC') $('monthrep_dic').checked = 'true';
    						}
    					}
	    			}
	    		}
	    		if(key=='dayRepetition') {
	    			if(value!='0') {
	    				$('dayinmonth_selection_interval').checked = true;
	    				selectOption($('dayinmonthrep_n'), value);
	    			} else {
	    				$('dayinmonth_selection_checks').checked = true;
	    			}
	    		}
	    		if(key=='weeks'){
	    			if(value!='NONE') {
	    				$('dayinmonth_selection_checks').checked = true;
	    				if(value=='1') $('dayinmonthrep_week1').checked = 'true';
			    		if(value=='2') $('dayinmonthrep_week2').checked = 'true';
			    		if(value=='3') $('dayinmonthrep_week3').checked = 'true';
			    		if(value=='4') $('dayinmonthrep_week4').checked = 'true';
			    		if(value=='L') $('dayinmonthrep_weekL').checked = 'true';
			    	}
			    }
			    if(key=='days'){
	    			if(value!='NONE') {
	    				$('dayinmonth_selection_checks').checked = true;
	    				days = value.split(',');
	    				for(j=0; j<days.length; j++) {
    						day = days[j];
    						if((day!=null) && (day!='')) {
			    				if(day=='SUN') $('dayinmonthrep_sun').checked = 'true';
			    				if(day=='MON') $('dayinmonthrep_mon').checked = 'true';
			    				if(day=='TUE') $('dayinmonthrep_tue').checked = 'true';
			    				if(day=='WED') $('dayinmonthrep_wed').checked = 'true';
			    				if(day=='THU') $('dayinmonthrep_thu').checked = 'true';
			    				if(day=='FRI') $('dayinmonthrep_fri').checked = 'true';
			    				if(day=='SAT') $('dayinmonthrep_sat').checked = 'true';
    						}
    					}
	    			}
	    		}
    		}
    	}
    }

</script>


<!-- ********************** SCRIPT FOR REPETITION FORMS **************************** -->

<script>

	function openrepetitionform(namerepetition) {
		$('minute_repetitionDiv_lbl').style.display='none';
		$('minute_repetitionDiv_form').style.display='none';
		$('hour_repetitionDiv_lbl').style.display='none';
		$('hour_repetitionDiv_form').style.display='none';
		$('day_repetitionDiv_lbl').style.display='none';
		$('day_repetitionDiv_form').style.display='none';
		$('week_repetitionDiv_lbl').style.display='none';
		$('week_repetitionDiv_form').style.display='none';
		$('month_repetitionDiv_form').style.display='none';
		$('month_repetitionDiv_lbl').style.display='none';
		
		divlbl = document.getElementById(namerepetition+'_repetitionDiv_lbl');
		if(divlbl!=null) {
			divlbl.style.display='inline';
		}
		divform = document.getElementById(namerepetition+'_repetitionDiv_form');
		if(divform!=null) {
			divform.style.display='inline';
		}
	}

</script>



<!-- ********************** PAGE STYLES **************************** -->

<STYLE>
	
	.div_form_container {
    	border: 1px solid #cccccc;
    	background-color:#fafafa;
    	float: left;
	}
	
	.div_form_margin {
		margin: 5px;
		float: left;
	}
	
	.div_form_row {
		clear: both;
		padding-bottom:5px;
	}
	
	.div_form_label {	
		float: left;
		width:150px;
		margin-right:20px;
	}
	
	.div_form_label_selector {	
		clear: left;
		margin-right:20px;
	}
	
	.div_form_field {
	}
    
    .div_form_message {	
		float: left;
		margin:20px;
	}
	
    .nowraptext {
    	white-space:nowrap;
    }
    
    .div_loading {
        width:20%;
    	position:absolute;
    	left:20%;
    	top:40%;
    	border:1px solid #bbbbbb;
    	background:#eeeeee;
    	padding-left:100px;padding-right:100px;
    	display:none;
    }
    
</STYLE>

<!-- *********************** START HTML CODE ****************************** -->


<form id="triggerdetailform" method="post" action="<%=formUrl%>" >
	
	<input type="hidden" name="PAGE" value="TriggerManagementPage" />
	<input type="hidden" name="MESSAGEDET" value="<%=SpagoBIConstants.MESSAGE_SAVE_SCHEDULE%>" />
	<input type="hidden" name="<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>" value="TRUE" />

	<table class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "scheduler.scheduledetail"  bundle="component_scheduler_messages"/>		
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='<%=backUrl%>'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "scheduler.back" bundle="component_scheduler_messages" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "scheduler.back"  bundle="component_scheduler_messages"/>' />
				</a>
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:saveCall()'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "scheduler.save" bundle="component_scheduler_messages" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/save.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "scheduler.save"  bundle="component_scheduler_messages"/>' />
				</a>
			</td>
		</tr>
	</table>

	<br/>

	<input type='hidden' value='' id='chronstring' name='chronstring' />

	<div class="div_form_container" >
		<div class="div_form_margin" >
			<div class="div_form_row" >
				<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.schedname" bundle="component_scheduler_messages" />
					</span>
				</div>
				<%
					String readonly  = "";
						String trigName = triggerInfo.getTriggerName();
						if(trigName!=null) {
							trigName = trigName.trim();
							if(!trigName.equals("")) {
								readonly = " readonly ";
							} else {
								Calendar cal = new GregorianCalendar();
							    int hour24 = cal.get(Calendar.HOUR_OF_DAY);     
							    int min = cal.get(Calendar.MINUTE);             
							    int sec = cal.get(Calendar.SECOND);   
							    int nameL = jobInfo.getJobName().length();
							    if(nameL<=40){
							    	trigName = jobInfo.getJobName() + "_" + hour24 + "" + min + "" + sec; 
							    }else{
									trigName = jobInfo.getJobName().substring(0,39) + "_" + hour24 + "" + min + "" + sec; 
							    }
							}
						}
						String saveFormat = "dd/MM/yyyy";
						String startDate = triggerInfo.getStartDate();
						String trigStartDate = "";
						
						SimpleDateFormat f =  new SimpleDateFormat();
						f.applyPattern(saveFormat);
						Date d = new Date();
						try {if (!startDate.equals("")){
								d = f.parse(startDate);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if (!startDate.equals("")){
							
							String datePickerFormat = "MM/dd/yyyy";
							trigStartDate = StringUtils.dateToString(d, datePickerFormat);
						}else {trigStartDate = startDate ; }
				%>
				<div class='div_form_field'>
					<input id="triggername" value="<%=StringEscapeUtils.escapeHtml(trigName)%>" type="text" name="triggername" size="50" <%=readonly%> />
				    &nbsp;*
				</div>
			</div>
			<div class="div_form_row" >	
				<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.scheddescription" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div class='div_form_field'>
					<input type="text" value="<%=StringEscapeUtils.escapeHtml(triggerInfo.getTriggerDescription())%>" name="triggerdescription" size="50"/>
					&nbsp;
				</div>
		     </div>
		     <div class="div_form_row" >
			 	<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.startdate" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div class='div_form_field'>
					<input type="text" value="<%=StringEscapeUtils.escapeHtml(trigStartDate)%>" 
			    		   name="startdate" id="startdate"  
			    		   saveFormat="<%=saveFormat%>"  displayFormat="<%=saveFormat%>"
			       		   dojoType="dropdowndatepicker" widgetId="startDateWidget" />
						   &nbsp;*
				</div>	
			</div>
		    <div class="div_form_row" >	
		     	<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.starttime" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div class='div_form_field'>
					<input type="text" value="<%=StringEscapeUtils.escapeHtml(triggerInfo.getStartTime())%>" 
					       name="starttime" id="starttime"  
					       dojoType="dropdowntimepicker" widgetId="startTimeWidget" />
					&nbsp;*
				</div>
			</div>
			<div class="div_form_row" >	
				<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.enddate" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div class='div_form_field'>
				 	<input type="text" value="<%=StringEscapeUtils.escapeHtml(triggerInfo.getEndDateRFC3339())%>" 
					       name="enddate" id="enddate"  
					       dojoType="dropdowndatepicker" widgetId="endDateWidget" />
					&nbsp;
				</div>
			</div>
			<div class="div_form_row" >	
				<div class='div_form_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.endtime" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div class='div_form_field'>
					<input type="text" value="<%=StringEscapeUtils.escapeHtml(triggerInfo.getEndTime())%>" 
					       name="endtime" id="endtime"  
					       dojoType="dropdowntimepicker" widgetId="endTimeWidget" />
					&nbsp;
				</div>
			</div>
			<%
				String repInterv = triggerInfo.getRepeatInterval();
				if(repInterv!=null) {
				   	if(repInterv.trim().equals("0")) {
				   		repInterv = "";
				   	}
				} else {
				   	repInterv = "";
				}
			%>
			<input type="hidden" value="<%=StringEscapeUtils.escapeHtml(repInterv)%>" name="repeatInterval" />
			
			
			
			<!-- ******* single execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='single_repetitionKind' name='repetitionKind' value='single' 
		     		       type="radio" onclick="openrepetitionform('single')" checked='checked' />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.singleExec" bundle="component_scheduler_messages" />
					</span>
				</div>
			</div>
			
			
			
			<!-- ******* per minute execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='minute_repetitionKind' name='repetitionKind' value='minute' 
		     		       type="radio" onclick="openrepetitionform('minute')" />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.minuteExec" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div id='minute_repetitionDiv_lbl' style='display:none;' class='div_form_label'>
					&nbsp;
				</div>
				<div id='minute_repetitionDiv_form' style='display:none;' class="div_form_container" >
					<div class="div_form_margin" >
						<div class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.everyNMinutes" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='minute_repetition_n' id='minute_repetition_n' >
								<%
									for(int i=1; i<=60; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
					</div>
				</div>
				<div style='clear:left;'></div>
			</div>
			
			
			
			<!-- ******* per hour execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='hour_repetitionKind' name='repetitionKind' value='hour' 
		     		       type="radio" onclick="openrepetitionform('hour')" />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.hourExec" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div id='hour_repetitionDiv_lbl' style='display:none;' class='div_form_label'>
					&nbsp;
				</div>
				<div id='hour_repetitionDiv_form'  style='display:none;' class="div_form_container" >
					<div class="div_form_margin" >
						<div class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.everyNHours" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='hour_repetition_n' id='hour_repetition_n' >
								<%
									for(int i=1; i<=24; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
					</div>
				</div>
				<div style='clear:left;'></div>
			</div>
			
			
			
			<!-- ******* per day execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='day_repetitionKind' name='repetitionKind' value='day' 
		     		       type="radio" onclick="openrepetitionform('day')" />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.dayExec" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div id='day_repetitionDiv_lbl' style='display:none;' class='div_form_label'>
					&nbsp;
				</div>
				<div id='day_repetitionDiv_form' style='display:none;' class="div_form_container" >
					<div class="div_form_margin" >
						<div class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.everyNDays" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='day_repetition_n' id='day_repetition_n' >
								<%
									for(int i=1; i<=31; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
					</div>
				</div>
				<div style='clear:left;'></div>
			</div>
			
			
			<!-- ******* per week execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='week_repetitionKind' name='repetitionKind' value='week' 
		     		       type="radio" onclick="openrepetitionform('week')" />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.weekExec" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div id='week_repetitionDiv_lbl' class='div_form_label' style='display:none;'>
					&nbsp;
				</div>
				<div id='week_repetitionDiv_form' style='display:none;' class="div_form_container" >
					<div class="div_form_margin" >
						<!-- the following form row is hidden because quartz is not able to manage n week   -->
						<div style='display:none;' class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.everyNWeeks" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='week_repetition_n' id='week_repetition_n' >
								<%
									for(int i=1; i<=52; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
						<div class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.inDays" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<input id='day_in_week_rep_sun' type='checkbox' value='SUN'><spagobi:message key="scheduler.sun" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_mon' type='checkbox' value='MON'><spagobi:message key="scheduler.mon" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_tue' type='checkbox' value='TUE'><spagobi:message key="scheduler.tue" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_wed' type='checkbox' value='WED'><spagobi:message key="scheduler.wed" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_thu' type='checkbox' value='THU'><spagobi:message key="scheduler.thu" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_fri' type='checkbox' value='FRI'><spagobi:message key="scheduler.fri" bundle="component_scheduler_messages" />
								<input id='day_in_week_rep_sat' type='checkbox' value='SAT'><spagobi:message key="scheduler.sat" bundle="component_scheduler_messages" />
							</div>	
						</div>
					</div>
				</div>
				<div style='clear:left;'></div>
			</div>
			
			
			
			<!-- ******* per month execution **************** -->
			<div class="div_form_row" >	
		     	<div class='div_form_label_selector'>
		     		<input id='month_repetitionKind' name='repetitionKind' value='month' 
		     		       type="radio" onclick="openrepetitionform('month')" />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.monthExec" bundle="component_scheduler_messages" />
					</span>
				</div>
				<div id='month_repetitionDiv_lbl' class='div_form_label' style='display:none;'>
					&nbsp;
				</div>
				<div  id='month_repetitionDiv_form' style='display:none;' class="div_form_container" >
					<div class="div_form_margin" >
						<div class="div_form_row" >
							<div class='div_form_label'>
								<input id='month_selection_interval' name='month_selection' type='radio' checked='true'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.everyNMonth" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='monthrep_n' id='monthrep_n' >
								<%
									for(int i=1; i<=12; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
						<div class="div_form_row" >
							<div class='div_form_label'>
								<input id='month_selection_checks' name='month_selection' type='radio'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.inMonths" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<input name='monthrep_jan' id='monthrep_jan' type='checkbox' value='JAN'><spagobi:message key="scheduler.jan" bundle="component_scheduler_messages" />
								<input name='monthrep_feb' id='monthrep_feb' type='checkbox' value='FEB'><spagobi:message key="scheduler.feb" bundle="component_scheduler_messages" />
								<input name='monthrep_mar' id='monthrep_mar' type='checkbox' value='MAR'><spagobi:message key="scheduler.mar" bundle="component_scheduler_messages" />
								<input name='monthrep_apr' id='monthrep_apr' type='checkbox' value='APR'><spagobi:message key="scheduler.apr" bundle="component_scheduler_messages" />
								<input name='monthrep_may' id='monthrep_may' type='checkbox' value='MAY'><spagobi:message key="scheduler.may" bundle="component_scheduler_messages" />
								<input name='monthrep_jun' id='monthrep_jun' type='checkbox' value='JUN'><spagobi:message key="scheduler.jun" bundle="component_scheduler_messages" />
								<input name='monthrep_jul' id='monthrep_jul' type='checkbox' value='JUL'><spagobi:message key="scheduler.jul" bundle="component_scheduler_messages" />
								<input name='monthrep_aug' id='monthrep_aug' type='checkbox' value='AUG'><spagobi:message key="scheduler.aug" bundle="component_scheduler_messages" />
								<input name='monthrep_sep' id='monthrep_sep' type='checkbox' value='SEP'><spagobi:message key="scheduler.sep" bundle="component_scheduler_messages" />
								<input name='monthrep_oct' id='monthrep_oct' type='checkbox' value='OCT'><spagobi:message key="scheduler.oct" bundle="component_scheduler_messages" />
								<input name='monthrep_nov' id='monthrep_nov' type='checkbox' value='NOV'><spagobi:message key="scheduler.nov" bundle="component_scheduler_messages" />
								<input name='monthrep_dic' id='monthrep_dic' type='checkbox' value='DIC'><spagobi:message key="scheduler.dic" bundle="component_scheduler_messages" />			
							</div>	
						</div>
						<br/>
						<div class="div_form_row" >
							<div class='div_form_label'>
								<input id='dayinmonth_selection_interval' name='dayinmonth_selection' type='radio' checked='true'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.theDay" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<select name='dayinmonthrep_n' id='dayinmonthrep_n' >
								<%
									for(int i=1; i<=31; i++) {
															out.write("<option value='"+i+"'>"+i+"</option>");
														}
								%>
								</select>
							</div>	
						</div>
						<div class="div_form_row" >
							<div class='div_form_label'>
							    <input id='dayinmonth_selection_checks' name='dayinmonth_selection' type='radio'>
								<span class='portlet-form-field-label'>
									<spagobi:message key="scheduler.inWeeks" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<input name='dayinmonthrep_week' id='dayinmonthrep_week1' type='radio' value='1'><spagobi:message key="scheduler.firstweek" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_week' id='dayinmonthrep_week2' type='radio' value='2'><spagobi:message key="scheduler.secondweek" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_week' id='dayinmonthrep_week3' type='radio' value='3'><spagobi:message key="scheduler.thirdweek" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_week' id='dayinmonthrep_week4' type='radio' value='4'><spagobi:message key="scheduler.fourthweek" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_week' id='dayinmonthrep_weekL' type='radio' value='L'><spagobi:message key="scheduler.lastweek" bundle="component_scheduler_messages" />
							</div>	
						</div>
						<div class="div_form_row" >
							<div class='div_form_label'>
								<span class='portlet-form-field-label'>
									&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<spagobi:message key="scheduler.inDays" bundle="component_scheduler_messages" />
								</span>
							</div>
							<div class='div_form_field'>
								<input name='dayinmonthrep_sun' id='dayinmonthrep_sun' type='checkbox' value='SUN'><spagobi:message key="scheduler.sun" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_mon' id='dayinmonthrep_mon' type='checkbox' value='MON'><spagobi:message key="scheduler.mon" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_tue' id='dayinmonthrep_tue' type='checkbox' value='TUE'><spagobi:message key="scheduler.tue" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_wed' id='dayinmonthrep_wed' type='checkbox' value='WED'><spagobi:message key="scheduler.wed" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_thu' id='dayinmonthrep_thu' type='checkbox' value='THU'><spagobi:message key="scheduler.thu" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_fri' id='dayinmonthrep_fri' type='checkbox' value='FRI'><spagobi:message key="scheduler.fri" bundle="component_scheduler_messages" />
								<input name='dayinmonthrep_sat' id='dayinmonthrep_sat' type='checkbox' value='SAT'><spagobi:message key="scheduler.sat" bundle="component_scheduler_messages" />
							</div>	
						</div>
					</div>
				</div>
				<div style='clear:left;'></div>
			</div>
			
			
		</div>
	</div>
		     

	<div style='clear:left;'></div>
	<br/>

	<script>
		fillFormFromRepetitionString('<%=triggerInfo.getChronString()%>');
	</script>

	<spagobi:error/>

	<table>
		<tr> 
			<td class='titlebar_level_2_text_section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key="scheduler.saveoptions" bundle="component_scheduler_messages" />
			</td>
		</tr>
	</table>

	<div style='width:100%;visibility:visible;' class='UITabs'>
		<div class="first-tab-level" style="background-color:#f8f8f8">
			<div style="overflow: hidden; width:  100%">
	
	<%
			if(jobBiobjects.size()==0){
		%>
				<br/>
				<spagobi:message key = "scheduler.jobhasnodocument"  bundle="component_scheduler_messages"/>
				<br/>
	<%
		} else {
		Iterator iterJobBiobjs = jobBiobjects.iterator();
		    	int index = 0;
		    	String tabClass = "tab selected"; 
		while(iterJobBiobjs.hasNext()) {
			BIObject biobj = (BIObject)iterJobBiobjs.next();
			String biobjName = biobj.getName();
			if(index > 0) {
				tabClass = "tab"; 
			}
			index ++;
	%>
				<div id="tabbiobj<%=biobj.getId()%>__<%=index%>"  class='<%=tabClass%>'>
					<a href="javascript:changeTab('<%=biobj.getId()%>__<%=index%>')" style="color:black;"> 
							<%=StringEscapeUtils.escapeHtml(biobjName)%>
					</a>
				</div>
	<%
		}
			}
	%>
			</div>
		</div>
	</div>	


	<%
			Iterator iterJobBiobjs = jobBiobjects.iterator();
			    int index = 0;
			    String setTabOpened = "";
			    String setIndexTabOpened = "";
			    String displaytab = "inline";
				while(iterJobBiobjs.hasNext()) {
			BIObject biobj = (BIObject)iterJobBiobjs.next();
			 
			if(index > 0) {
				displaytab = "none";
			} else {
				setTabOpened = "<script>tabOpened = '"+biobj.getId()+"__"+(index+1)+"';</script>";
				setIndexTabOpened = "<script>indexTabOpened = "+(index+1)+";</script>";
			}
			index ++;
			
			String treeName = "tree_" + biobj.getId() + "__" + index;
			Integer biobjid = biobj.getId();
			DispatchContext sInfo = (DispatchContext)saveOptions.get(biobjid+"__"+index);
		%>
	
	<%=setTabOpened%>
	<%=setIndexTabOpened%>
	<div width="100%" id="areabiobj<%=biobj.getId()%>__<%=index%>" style="display:<%=displaytab%>;" >
		<br/>
		<div class="div_detail_area_forms_scheduler" >    	
        
    <!-- ======================================================================================== -->
	<!-- SAVE AS SNAPSHOT														  		  -->
	<!-- ======================================================================================== --> 	
	<input type="checkbox" id="saveassnapshot_<%=biobj.getId()%>__<%=index%>" name="saveassnapshot_<%=biobj.getId()%>__<%=index%>" 
               <%if(sInfo.isSnapshootDispatchChannelEnabled()){out.write(" checked='checked' " );}%> />
			  <span class='portlet-form-field-label'>
					<spagobi:message key="scheduler.saveassnap" bundle="component_scheduler_messages" />
			  </span>
		<div id="snapshot_<%=biobj.getId()%>__<%=index%>"  style="margin-left:50px;margin-top:10px;">
            <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.storename" bundle="component_scheduler_messages" />
		        </span>
	        </div>
            <div class='div_detail_form'>
		        <input type="text" id="snaphotname" value="<%=StringEscapeUtils.escapeHtml(sInfo.getSnapshotName())%>"
		               name="snapshotname_<%=biobj.getId()%>__<%=index%>" size="35"/>
	        </div>
	        <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.storedescr" bundle="component_scheduler_messages" />
		        </span>
	         </div>
	         <div class='div_detail_form'>
		        <input type="text" value="<%=StringEscapeUtils.escapeHtml(sInfo.getSnapshotDescription())%>"
		               name="snapshotdescription_<%=biobj.getId()%>__<%=index%>" size="35"/>
	         </div>
      	     <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.historylength" bundle="component_scheduler_messages" />
		        </span>
	         </div>
	         <div class='div_detail_form'>
		        <input type="text" name="snapshothistorylength_<%=biobj.getId()%>__<%=index%>" 
		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getSnapshotHistoryLength())%>" size="35"/>
	         </div>		       
        </div>
        
	<script>  
	toggle('snapshot_<%=biobj.getId()%>__<%=index%>', 'saveassnapshot_<%=biobj.getId()%>__<%=index%>', <%=sInfo.isSnapshootDispatchChannelEnabled()%> );
	</script> 

	<!-- ======================================================================================== -->
	<!-- SAVE AS FILE 																			  -->
	<!-- ======================================================================================== -->
	<div> &nbsp;</div>		
    <br/>
 	<input type="checkbox" id="saveasfile_<%=biobj.getId()%>__<%=index%>" name="saveasfile_<%=biobj.getId()%>__<%=index%>" 
	<%if(sInfo.isFileSystemDispatchChannelEnabled()){out.write(" checked='checked' " );}%> />
	<span class='portlet-form-field-label'>
		<spagobi:message key="scheduler.saveasfile" bundle="component_scheduler_messages" />
	</span>
	<div id="file_<%=biobj.getId()%>__<%=index%>"  style="margin-left:50px;margin-top:10px;">
	    <div class='div_detail_label_scheduler'>
			<span class='portlet-form-field-label'>
			    <spagobi:message key="scheduler.destinationfolder" bundle="component_scheduler_messages" />
			</span>
		</div>
        <div class='div_detail_form'>
			<input type="text" id="destinationfolder" value="<%=StringEscapeUtils.escapeHtml(sInfo.getDestinationFolder())%>"
		           name="destinationfolder_<%=biobj.getId()%>__<%=index%>" size="35"/>
	   	</div>
	
        <input  type="checkbox" name="zipFileDocument_<%=biobj.getId()%>__<%=index%>" value="true"
                 <%= sInfo.isZipFileDocument() ? "checked='checked'" : "" %> />
           <span class='portlet-form-field-label'>
                     <spagobi:message key="scheduler.zipFileDocument" bundle="component_scheduler_messages" />
           </span>


    <div id="fileName_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">

	      <div class='div_detail_label_scheduler'>
              <span class='portlet-form-field-label'>
                   <spagobi:message key="scheduler.zipFileName" bundle="component_scheduler_messages"/>
              </span>
        </div>
        <div class='div_detail_form'>
                    <input type="text" name="zipFileName_<%=biobj.getId()%>__<%=index%>" 
                           value="<%=StringEscapeUtils.escapeHtml(sInfo.getZipFileName() != null ? sInfo.getZipFileName() : "")%>" size="60" />
        </div>
	
	</div>
	     

        <div class='div_detail_label_scheduler'>
              <span class='portlet-form-field-label'>
                   <spagobi:message key="scheduler.fileName" bundle="component_scheduler_messages"/>
              </span>
        </div>
        
         <div class='div_detail_form'>
                     <input type="text" name="fileName_<%=biobj.getId()%>__<%=index%>" 
                           value="<%=StringEscapeUtils.escapeHtml(sInfo.getFileName() != null ? sInfo.getFileName() : "")%>" size="60" />
         </div>
    	
	
	
	</div>
	
	<script>  
	toggle('file_<%=biobj.getId()%>__<%=index%>', 'saveasfile_<%=biobj.getId()%>__<%=index%>', <%=sInfo.isFileSystemDispatchChannelEnabled()%> );
	</script> 
	
	<!-- ======================================================================================== -->
	<!-- SAVE AS DOCUMENT 																		  -->
	<!-- ======================================================================================== -->
	<div> &nbsp;</div>		
    <br/>
		<input type="checkbox" id="saveasdocument_<%=biobj.getId()%>__<%=index%>"  name="saveasdocument_<%=biobj.getId()%>__<%=index%>" 
		       <%if( sInfo.isFunctionalityTreeDispatchChannelEnabled() ){out.write(" checked='checked' " );}%> />
		<span class='portlet-form-field-label'>
			<spagobi:message key="scheduler.saveasdoc" bundle="component_scheduler_messages" />
		</span>
		
		<div id="document_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
			
	        <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.storename" bundle="component_scheduler_messages" />
		        </span>
	        </div>
            <div class='div_detail_form'>
		        <input type="text" name="documentname_<%=biobj.getId()%>__<%=index%>" 
		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getDocumentName())%>" size="35"/>
	        </div>
	        <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.storedescr" bundle="component_scheduler_messages" />
		        </span>
	        </div>
	        <div class='div_detail_form'>
		        <input type="text" name="documentdescription_<%=biobj.getId()%>__<%=index%>" 
		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getDocumentDescription())%>" size="35"/>
	        </div>   
	       
	        <input  type="checkbox" name="useFixedFolder_<%=biobj.getId()%>__<%=index%>" value="true"
					<%=sInfo.isUseFixedFolder() ? "checked='checked'" : ""%> />
			<span class='portlet-form-field-label'>
				<spagobi:message key="scheduler.fixedFolder" bundle="component_scheduler_messages" />
			</span>
			<a href="javascript:void(0);" id="folderTo_<%=biobj.getId()%>__<%=index%>_help">
	      			<img title="<spagobi:message key = "scheduler.help" bundle="component_scheduler_messages" />" 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/question.gif", currTheme)%>' 
	      				 alt="<spagobi:message key = "scheduler.help"  bundle="component_scheduler_messages"/>" 
	      				 style="vertical-align:bottom;" />
	      	</a>
  			<script type="text/javascript">
      			var fixedFolderHelpWin;
      			Ext.get('folderTo_<%=biobj.getId()%>__<%=index%>_help').on('click', function(){
      				if(!fixedFolderHelpWin){
      					fixedFolderHelpWin = new Ext.Window({
      						id:'fixedFolderHelpWin',
    					contentEl:'scheduler.help.useFixedFolder',
    					width:350,
    					height:100,
    					title: "<spagobi:message key = "scheduler.fixedFolder" bundle="component_scheduler_messages" />"
      					});
      				};
      				fixedFolderHelpWin.show();
      			});
  			</script>
	        <spagobi:treeObjects moduleName="TriggerManagementModule"  
								htmlGeneratorClass="it.eng.spagobi.tools.scheduler.gui.SelectFunctionalityTreeHtmlGenerator" 
								treeName="<%=treeName%>" />

		    <input  type="checkbox" name="useFolderDataset_<%=biobj.getId()%>__<%=index%>" value="true"
		               <%=sInfo.isUseFolderDataSet() ? "checked='checked'" :""%> />
			<span class='portlet-form-field-label'>
				<spagobi:message key="scheduler.useFolderDataset" bundle="component_scheduler_messages" />
			</span>
			<a href="javascript:void(0);" id="folderToDataset_<%=biobj.getId()%>__<%=index%>_help">
		    			<img title="<spagobi:message key = "scheduler.help" bundle="component_scheduler_messages" />" 
		    				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/question.gif", currTheme)%>' 
		    				 alt="<spagobi:message key = "scheduler.help"  bundle="component_scheduler_messages"/>" 
		    				 style="vertical-align:bottom;" />
			</a>
			<script type="text/javascript">
					var folderToDatasetHelpWin;
					Ext.get('folderToDataset_<%=biobj.getId()%>__<%=index%>_help').on('click', function(){
							if(!folderToDatasetHelpWin){
								folderToDatasetHelpWin = new Ext.Window({
									id:'folderToDatasetHelpWin',
									contentEl:'scheduler.help.useFolderDataset',
									width:350,
									height:100,
									title: "<spagobi:message key = "scheduler.useFolderDataset" bundle="component_scheduler_messages" />"
									});
							};
							folderToDatasetHelpWin.show();
						});
			</script>
			<div id="folderDataset_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
		  		<div  class='div_detail_label_scheduler'>
						<span class='portlet-form-field-label'>
							<spagobi:message key="scheduler.folderToDatasetParameter" bundle="component_scheduler_messages" />
						</span>
	 	    	</div>
	  	  		<div class='div_detail_form'>
				  	<select name='datasetFolderLabel_<%=biobj.getId()%>__<%=index%>'>
						<option></option>
		  		        <%
		  		        	String dsFolderLabel = sInfo.getDataSetFolderLabel();
		  		        		  		        List allFolderDatasets = (List) moduleResponse.getAttribute(SpagoBIConstants.DATASETS_LIST);
		  		        		  		        if (allFolderDatasets != null && !allFolderDatasets.isEmpty()) {
		  		        	  		        Iterator dsIt = allFolderDatasets.iterator();
		  		        	  		        while (dsIt.hasNext()) {
		  		        	  		        	IDataSet ds = (IDataSet) dsIt.next();
		  		        %>
			  		        	<option value='<%=StringEscapeUtils.escapeHtml(ds.getLabel())%>' <%=ds.getLabel().equalsIgnoreCase(dsFolderLabel) ? "selected='selected'" : ""%>>
			  		        		<%=StringEscapeUtils.escapeHtml(ds.getName())%>
			  		        	</option>
			  		        	<%
			  		        		}
			  		        			  		        }
			  		        	%>
					    </select>
		  	    </div>
		  		<div  class='div_detail_label_scheduler'>
						<span class='portlet-form-field-label'>
							<spagobi:message key="scheduler.folderToDriver" bundle="component_scheduler_messages" />
						</span>
	 	    	</div>
	  	  		<div class='div_detail_form'>
				  	<select name='datasetFolderParameter_<%=biobj.getId()%>__<%=index%>'>
				  		<option></option>
		  		        <%
		  		        	List folderParameters = biobj.getBiObjectParameters();
		  		        		  		      	if (folderParameters != null && !folderParameters.isEmpty()) {
		  		        	  		        String parameterLabel = sInfo.getDataSetFolderParameterLabel();
		  		        	  		        Iterator parametersIt = folderParameters.iterator();
		  		        	  		        while (parametersIt.hasNext()) {
		  		        	  		        	BIObjectParameter aParameter = (BIObjectParameter) parametersIt.next();
		  		        %>
			  		        	<option value='<%=StringEscapeUtils.escapeHtml(aParameter.getLabel())%>' <%=aParameter.getLabel().equalsIgnoreCase(parameterLabel) ? "selected='selected'" : ""%>>
			  		        		<%=StringEscapeUtils.escapeHtml(aParameter.getLabel())%>
			  		        	</option>
			  		        	<%
			  		        		}
			  		        			  		      	}
			  		        	%>
					   </select>
		  	    </div>
	      	</div>
    	</div>
	<script>
	toggle('document_<%=biobj.getId()%>__<%=index%>', 'saveasdocument_<%=biobj.getId()%>__<%=index%>', <%=sInfo.isFunctionalityTreeDispatchChannelEnabled()%>);
	</script>  
		

	<!-- ======================================================================================== -->
	<!-- SAVE AS JAVA CLASS																		  -->
	<!-- ======================================================================================== -->
    <div> &nbsp;</div>			
    <br/>
    <input type="checkbox" id="sendtojavaclass_<%=biobj.getId()%>__<%=index%>" name="sendtojavaclass_<%=biobj.getId()%>__<%=index%>" 
               <%if(sInfo.isJavaClassDispatchChannelEnabled()){out.write(" checked='checked' " );}%> />
			  <span class='portlet-form-field-label'>
					<spagobi:message key="scheduler.sendtojavaclass" bundle="component_scheduler_messages" />
			  </span>
		<div id="javaclass_<%=biobj.getId()%>__<%=index%>"  style="margin-left:50px;margin-top:10px;">
            <div class='div_detail_label_scheduler'>
		        <span class='portlet-form-field-label'>
			         <spagobi:message key="scheduler.javaclasspath" bundle="component_scheduler_messages" />
		        </span>
	        </div>
            <div class='div_detail_form'>
		        <input type="text" id="javaclasspath" value="<%=StringEscapeUtils.escapeHtml(sInfo.getJavaClassPath())%>"
		               name="javaclasspath_<%=biobj.getId()%>__<%=index%>" size="35"/>
	        </div>
        </div>
	<script>  
	toggle('javaclass_<%=biobj.getId()%>__<%=index%>', 'sendtojavaclass_<%=biobj.getId()%>__<%=index%>', <%=sInfo.isJavaClassDispatchChannelEnabled()%> );
	</script>

    <!-- ======================================================================================== -->
	<!-- SAVE AS MAIL																	  		  -->
	<!-- ======================================================================================== -->
	<div> &nbsp;</div>		
    <br/>
	<input type="checkbox" id="sendmail_<%=biobj.getId()%>__<%=index%>"   name="sendmail_<%=biobj.getId()%>__<%=index%>" 
				       <%if(sInfo.isMailDispatchChannelEnabled()){out.write(" checked='checked' " );} %> />
				<span class='portlet-form-field-label'>
					<spagobi:message key="scheduler.sendmail" bundle="component_scheduler_messages" />
				</span>
				<div id="mail_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;"> 
				
				
				<input id="uniqueMail_<%=biobj.getId()%>__<%=index%>" type="checkbox" name="uniqueMail_<%=biobj.getId()%>__<%=index%>" value="true"
                        <%= sInfo.isUniqueMail() ? "checked='checked'" : "" %> />
                     <span class='portlet-form-field-label'>
                     <spagobi:message key="scheduler.uniqueMail" bundle="component_scheduler_messages" />
                  </span>
                <br/>
                <br/>       
				
                  <input id="zipMailDocument_<%=biobj.getId()%>__<%=index%>" type="checkbox" name="zipMailDocument_<%=biobj.getId()%>__<%=index%>" value="true"
                        <%= sInfo.isZipMailDocument() ? "checked='checked'" : "" %> />
        
                  <span class='portlet-form-field-label'>
                     <spagobi:message key="scheduler.zipMailDocument" bundle="component_scheduler_messages" />
                  </span>
    	          <div id="zipMailName_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
                        <div class='div_detail_label_scheduler'>
                            <span class='portlet-form-field-label'>
                                <spagobi:message key="scheduler.zipMailName" bundle="component_scheduler_messages" />
                            </span>
                        </div>
                        <div class='div_detail_form'>
                                <input  type="text" name="zipMailName_<%=biobj.getId()%>__<%=index%>" 
                                       value="<%=StringEscapeUtils.escapeHtml(sInfo.getZipMailName() != null ? sInfo.getZipMailName() : "")%>" size="35" />
                        </div>
                  </div>
				
				
					<input  type="checkbox" name="useFixedRecipients_<%=biobj.getId()%>__<%=index%>" value="true"
						<%= sInfo.isUseFixedRecipients() ? "checked='checked'" : "" %> />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.fixedRecipients" bundle="component_scheduler_messages" />
					</span>
					<a href="javascript:void(0);" id="mailtos_<%=biobj.getId()%>__<%=index%>_help">
		      			<img title="<spagobi:message key = "scheduler.help" bundle="component_scheduler_messages" />" 
		      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/question.gif", currTheme)%>' 
		      				 alt="<spagobi:message key = "scheduler.help"  bundle="component_scheduler_messages"/>" 
		      				 style="vertical-align:bottom;" />
	      			</a>
	      			<script type="text/javascript">
	      			var fixedRecipientsHelpWin;
	      			Ext.get('mailtos_<%=biobj.getId()%>__<%=index%>_help').on('click', function(){
	      				if(!fixedRecipientsHelpWin){
	      					fixedRecipientsHelpWin = new Ext.Window({
	      						id:'fixedRecipientsHelpWin',
								contentEl:'scheduler.help.useFixedRecipients',
								width:350,
								height:100,
								title: "<spagobi:message key = "scheduler.fixedRecipients" bundle="component_scheduler_messages" />"
	      					});
	      				};
	      				fixedRecipientsHelpWin.show();
	      			});
	      			</script>
					<div id="fixedRecipients_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
					  	<div class='div_detail_label_scheduler'>
							<span class='portlet-form-field-label'>
								<spagobi:message key="scheduler.mailto" bundle="component_scheduler_messages" />
							</span>
			  	      	</div>
				  	    <div class='div_detail_form'>
				  		        <input  type="text" name="mailtos_<%=biobj.getId()%>__<%=index%>" 
				  		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getMailTos())%>" size="35" />
				  	    </div>
					</div>
					
					<input  type="checkbox" name="useDataset_<%=biobj.getId()%>__<%=index%>" value="true"
	 		               <%= sInfo.isUseDataSet() ? "checked='checked'" :"" %> />
					<span class='portlet-form-field-label'>
						<spagobi:message key="scheduler.useDataset" bundle="component_scheduler_messages" />
					</span>
					<a href="javascript:void(0);" id="mailToDataset_<%=biobj.getId()%>__<%=index%>_help">
		      			<img title="<spagobi:message key = "scheduler.help" bundle="component_scheduler_messages" />" 
		      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/question.gif", currTheme)%>' 
		      				 alt="<spagobi:message key = "scheduler.help"  bundle="component_scheduler_messages"/>" 
		      				 style="vertical-align:bottom;" />
	      			</a>
	      			<script type="text/javascript">
	      			var mailToDatasetHelpWin;
	      			Ext.get('mailToDataset_<%=biobj.getId()%>__<%=index%>_help').on('click', function(){
	      				if(!mailToDatasetHelpWin){
	      					mailToDatasetHelpWin = new Ext.Window({
	      						id:'mailToDatasetHelpWin',
								contentEl:'scheduler.help.useDataset',
								width:350,
								height:100,
								title: "<spagobi:message key = "scheduler.useDataset" bundle="component_scheduler_messages" />"
	      					});
	      				};
	      				mailToDatasetHelpWin.show();
	      			});
	      			</script>
					<div id="dataset_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
					  	<div  class='div_detail_label_scheduler'>
							<span class='portlet-form-field-label'>
								<spagobi:message key="scheduler.mailToDataset" bundle="component_scheduler_messages" />
							</span>
			  	      	</div>
				  	    <div class='div_detail_form'>
						  	<select name='datasetLabel_<%=biobj.getId()%>__<%=index%>'>
								<option></option>
				  		        <%
				  		        String dsLabel = sInfo.getDataSetLabel();
				  		        List allDatasets = (List) moduleResponse.getAttribute(SpagoBIConstants.DATASETS_LIST);
				  		        if (allDatasets != null && !allDatasets.isEmpty()) {
					  		        Iterator dsIt = allDatasets.iterator();
					  		        while (dsIt.hasNext()) {
					  		        	IDataSet ds = (IDataSet) dsIt.next();
					  		        	%>
					  		        	<option value='<%= StringEscapeUtils.escapeHtml(ds.getLabel()) %>' <%= ds.getLabel().equalsIgnoreCase(dsLabel) ? "selected='selected'" : ""%>>
					  		        		<%= StringEscapeUtils.escapeHtml(ds.getName()) %>
					  		        	</option>
					  		        	<%
					  		        }
				  		        }
				  		        %>
							</select>
				  	    </div>
					  	<div  class='div_detail_label_scheduler'>
							<span class='portlet-form-field-label'>
								<spagobi:message key="scheduler.mailToDatasetParameter" bundle="component_scheduler_messages" />
							</span>
			  	      	</div>
				  	    <div class='div_detail_form'>
						  	<select name='datasetParameter_<%=biobj.getId()%>__<%=index%>'>
						  		<option></option>
				  		        <%
				  		        List parameters = biobj.getBiObjectParameters();
				  		      	if (parameters != null && !parameters.isEmpty()) {
					  		        String parameterLabel = sInfo.getDataSetParameterLabel();
					  		        Iterator parametersIt = parameters.iterator();
					  		        while (parametersIt.hasNext()) {
					  		        	BIObjectParameter aParameter = (BIObjectParameter) parametersIt.next();
					  		        	%>
					  		        	<option value='<%= StringEscapeUtils.escapeHtml(aParameter.getLabel()) %>' <%= aParameter.getLabel().equalsIgnoreCase(parameterLabel) ? "selected='selected'" : ""%>>
					  		        		<%= StringEscapeUtils.escapeHtml(aParameter.getLabel()) %>
					  		        	</option>
					  		        	<%
					  		        }
				  		      	}
				  		        %>
							</select>
				  	    </div>
		 	      	 </div>

			  	
				<input  type="checkbox" name="useExpression_<%=biobj.getId()%>__<%=index%>" value="true"
					<%= sInfo.isUseExpression() ? "checked='checked'" : "" %> />
				<span class='portlet-form-field-label'>
					<spagobi:message key="scheduler.useExpression" bundle="component_scheduler_messages" />
				</span>
				<a href="javascript:void(0);" id="mailToExpression_<%=biobj.getId()%>__<%=index%>_help">
	      			<img title="<spagobi:message key = "scheduler.help" bundle="component_scheduler_messages" />" 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/question.gif", currTheme)%>' 
	      				 alt="<spagobi:message key = "scheduler.help"  bundle="component_scheduler_messages"/>" 
	      				 style="vertical-align:bottom;" />
      			</a>
      			<script type="text/javascript">
      			var mailToExpressionHelpWin;
      			Ext.get('mailToExpression_<%=biobj.getId()%>__<%=index%>_help').on('click', function(){
      				if(!mailToExpressionHelpWin){
      					mailToExpressionHelpWin = new Ext.Window({
      						id:'mailToExpressionHelpWin',
							contentEl:'scheduler.help.useExpression',
							width:350,
							height:100,
							title: "<spagobi:message key = "scheduler.useExpression" bundle="component_scheduler_messages" />"
      					});
      				};
      				mailToExpressionHelpWin.show();
      			});
      			</script>
				<div id="expression_<%=biobj.getId()%>__<%=index%>" style="margin-left:50px;margin-top:10px;">
				  	<div class='div_detail_label_scheduler'>
						<span class='portlet-form-field-label'>
							<spagobi:message key="scheduler.mailToExpression" bundle="component_scheduler_messages" />
						</span>
		  	      	</div>
			  	    <div class='div_detail_form'>
			  		        <input  type="text" name="expression_<%=biobj.getId()%>__<%=index%>" 
			  		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getExpression())%>" size="35" />
			  	    </div>
				</div>
			  
			  	    
	  	    <div class='div_detail_label_scheduler'>
  		      <span class='portlet-form-field-label'>
  			       <spagobi:message key="Mail subject" bundle="component_scheduler_messages"/>
  		      </span>
  	      	</div>
	  	    <div class='div_detail_form'>
	  		        <input type="text" name="mailsubj_<%=biobj.getId()%>__<%=index%>" 
	  		               value="<%=StringEscapeUtils.escapeHtml(sInfo.getMailSubj())%>" size="60" />
	  	    </div>
	  	    
           <div class='div_detail_form'>
               <input  type="checkbox" name="reportNameInSubject_<%=biobj.getId()%>__<%=index%>" value="true"
                 <%= sInfo.isReportNameInSubject() ? "checked='checked'" : "" %> />
                   <span class='portlet-form-field-label'>
                       <spagobi:message key="scheduler.reportNameInSubject" bundle="component_scheduler_messages" />
                   </span>
	  	    </div>
	  	    
	  	    <div class='div_detail_label_scheduler'>
              <span class='portlet-form-field-label'>
                   <spagobi:message key="scheduler.containedFileName" bundle="component_scheduler_messages"/>
              </span>
            </div>
            <div class='div_detail_form'>
                    <input type="text" name="containedFileName_<%=biobj.getId()%>__<%=index%>" 
                           value="<%=StringEscapeUtils.escapeHtml(sInfo.getContainedFileName() != null ? sInfo.getContainedFileName() : "")%>" size="60" />
            </div>

            	  	    
	  	    <div class='div_detail_label_scheduler'>
  		      <span class='portlet-form-field-label'>
  			       <spagobi:message key="Mail text" bundle="component_scheduler_messages"/>
  		      </span>
  	      	</div>
	  	    <div class='div_detail_form' style="height:125px;">
	  	    		<textarea rows="8" cols="60" name="mailtxt_<%=biobj.getId()%>__<%=index%>" style="font-size:9pt"><%=sInfo.getMailTxt()%></textarea>
	  	    </div>





  	       </div>
  	       
            
            

	<script>
	toggle('mail_<%=biobj.getId()%>__<%=index%>', 'sendmail_<%=biobj.getId()%>__<%=index%>', <%= sInfo.isMailDispatchChannelEnabled() %>);
	</script> 

	<!-- ======================================================================================== -->
	<!-- SAVE AS DISTRIBUTION LIST														  		  -->
	<!-- ======================================================================================== --> 	
	<div> &nbsp;
	</div>	    
  	<br/>

	<input type="checkbox" id="saveasdl_<%=biobj.getId()%>__<%=index%>" name="saveasdl_<%=biobj.getId()%>__<%=index%>" 
				       <%if(sInfo.isDistributionListDispatchChannelEnabled()){out.write(" checked='checked' " );}%>/>
				<span class='portlet-form-field-label'>
					<spagobi:message key="scheduler.distributionlist" bundle="component_scheduler_messages" />
				</span>
	   <br/>	
	    <br/>
		<div id="dl_<%=biobj.getId()%>__<%=index%>"  >	
		<table style='width:80%;margin-top:1px' id ="dlTable" >
	
		<!-- LIST OF DISTRIBUTION LISTS  -->
		<%
			List dlist = DAOFactory.getDistributionListDAO().loadAllDistributionLists();	
			if(!dlist.isEmpty()){
		%>	
	<tr>	
	<td> &nbsp;
	  </td>
	  <td class='portlet-section-header' style='text-align:left'> &nbsp; &nbsp;  
				<spagobi:message key = "SBISet.ListDL.columnName" />
	  </td>			

	  <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.columnDescr" />		
	  </td>
	  
	</tr>				
		
		<%
									Iterator it2 = dlist.iterator();
									while(it2.hasNext()){
										
										DistributionList dl = (DistributionList)it2.next();
										int listID = dl.getId();
										String listName = dl.getName();
										String listDescr = dl.getDescr();
										if((listName==null) || (listName.equalsIgnoreCase("null"))  ) {
											listName = "";
										   }
										if((listDescr==null) || (listDescr.equalsIgnoreCase("null"))  ) {
											listDescr = "";
										   }
								%>
		 <tr class='portlet-font'>
		 	<td  style='vertical-align:right;text-align:right;'> <input type="checkbox" name="sendtodl_<%=listID%>_<%=biobj.getId()%>__<%=index%>"  value=<%=listID%>
               	<%if(sInfo.getDlIds().contains(new Integer(listID))){out.write(" checked='checked' " );}%> />
			</td>
		 	<td class='portlet-section-body' style='vertical-align:middle;text-align:left;'> &nbsp; &nbsp;  <%=StringEscapeUtils.escapeHtml(listName)%>	 			
			</td>	
			<td class='portlet-section-body' style='vertical-align:middle;text-align:left;'><%=StringEscapeUtils.escapeHtml(listDescr)%>
			</td>
	    </tr>
				   		
		<%
				   					}
				   				%>
	<%
		}
	%>	
  	</table>
  </div>
  
   </div>						
<script>
toggle('dl_<%=biobj.getId()%>__<%=index%>', 'saveasdl_<%=biobj.getId()%>__<%=index%>', <%=sInfo.isDistributionListDispatchChannelEnabled()%> );
</script> 
  	    <div> &nbsp;
		</div>	
  	    <br/>
  	    

	</div>
	
	<%	
		}
	%>
	




</form>

<div style="display:none;" >
	<div id="scheduler.help.useFixedRecipients">
		<spagobi:message key="scheduler.help.useFixedRecipients" bundle="component_scheduler_messages" />
	</div>
	<div id="scheduler.help.useDataset">
		<spagobi:message key="scheduler.help.useDataset" bundle="component_scheduler_messages" />
	</div>
	<div id="scheduler.help.useExpression">
		<spagobi:message key="scheduler.help.useExpression" bundle="component_scheduler_messages" />
	</div>
	<div id="scheduler.help.useFixedFolder">
		<spagobi:message key="scheduler.help.useFixedFolder" bundle="component_scheduler_messages" />
	</div>
	<div id="scheduler.help.useFolderDataset">
		<spagobi:message key="scheduler.help.useFolderDataset" bundle="component_scheduler_messages" />
	</div>
</div>
