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
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO"%>
<%@page import="it.eng.spagobi.commons.bo.Role"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.tools.scheduler.to.JobInfo"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever"%>
<%@page import="it.eng.spagobi.tools.scheduler.Formula"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter"%>

<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("JobManagementModule"); 
    
	JobInfo jobInfo = (JobInfo)aSessionContainer.getAttribute(SpagoBIConstants.JOB_INFO);   
	List jobBiobjects = jobInfo.getDocuments();
	Iterator iterJobBiobjs = jobBiobjects.iterator();
	String allObjIDS = "";
	int index = 0;
	while(iterJobBiobjs.hasNext()) {
		index ++ ;
		BIObject biobj = (BIObject)iterJobBiobjs.next();
		allObjIDS += biobj.getId() + "__" + index + ",";
	}
	allObjIDS = (allObjIDS != null && !allObjIDS.equals(""))?allObjIDS.substring(0,allObjIDS.length()-1):"";
	
	Map backUrlPars = new HashMap();
	backUrlPars.put("LIGHT_NAVIGATOR_BACK_TO", "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
	Map formUrlPars = new HashMap();
	String formUrl = urlBuilder.getUrl(request, formUrlPars);   
	   
	String splitter = ";";
	
	List formulas = Formula.getAvailableFormulas();
%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<!-- ********************** SCRIPT FOR DOCUMENT SELECTION WINDOW **************************** -->

<script>
	var docselwinopen = false;
	var winDS = null;
	
	function opencloseDocumentSelectionWin() {
		if(!docselwinopen){
			docselwinopen = true;
			openDocumentSelectionWin();
		}
	}
	
	function openDocumentSelectionWin(){
		if(winDS==null) {
			winDS = new Window('winDSId', {className: "alphacube", title: "", width:550, height:400, destroyOnClose: true});
	      	winDS.setContent('selectiondocumentdiv', false, false);
	      	winDS.showCenter(true);
	    } else {
	      	winDS.showCenter(true);
	    }
	}
	
	observerWDS = { 
		onClose: function(eventName, win) {
			if (win == winDS) {
				docselwinopen = false;
			}
		}
	}
	
	observerWDS1 = { 
		onDestroy: function(eventName, win) { 
			if (win == winDS) { 
				$('selectiondocumentcontainerdiv').appendChild($('selectiondocumentdiv')); 
				$('selectiondocumentdiv').style.display='none';
				winDS = null; 
				Windows.removeObserver(this); 
			} 
		 } 
	}
	

	Windows.addObserver(observerWDS);
	Windows.addObserver(observerWDS1);
	
</script>

<!-- ********************** SCRIPT FOR TABS **************************** -->
<script>
	tabOpened = ""; 
	indexTabOpened = 0;
	biobidstr = "<%=allObjIDS%>";
	
	function changeTab(biobjid) {
	  	if(tabOpened==biobjid) {
      		return;
    	}
		document.getElementById('areabiobj'+biobjid).style.display="block";
		document.getElementById('areabiobj'+tabOpened).style.display="none";
		document.getElementById('tabbiobj'+biobjid).className="tab selected";
		document.getElementById('tabbiobj'+tabOpened).className="tab";
		tabOpened = biobjid;
		indexTabOpened = tabOpened.substring(tabOpened.lastIndexOf("__")+2);
	}
	
	function removeTab(message) {
		if (tabOpened == "") return;
		
  		if (confirm(message)){
	  		var tmpBiobidstr = "";
	  		var lstIds = biobidstr.split(",");
			for(i=0; i<lstIds.length; i++) {
				if(lstIds[i] != tabOpened){
				   tmpBiobidstr = tmpBiobidstr + lstIds[i] + ',';
				}
			}
			tmpBiobidstr = tmpBiobidstr.substring(0, tmpBiobidstr.length - 1);
			$('selected_biobject_ids').value = tmpBiobidstr;
			
			document.getElementById('formmsg').value='MESSAGE_DOCUMENTS_SELECTED';
			document.getElementById('jobdetailform').submit();
			}
     	return;
	}
	
	function copyTab() {
		if (tabOpened == "") return;
		
		var newTabId = tabOpened.substring(0, tabOpened.lastIndexOf("__")) + "__" + (indexTabOpened+1);
		
		$('selected_biobject_ids').value = biobidstr + "," + newTabId;

		document.getElementById('formmsg').value='MESSAGE_DOCUMENTS_SELECTED';
		document.getElementById('jobdetailform').submit();
		
     	return;
	}
	
	function fillParamCall() {
	
	    biobidstr += (biobidstr.length > 0)?",":"";
		checkBiObjs = document.getElementsByName('biobject');
		for(i=0; i<checkBiObjs.length; i++) {
		    checkBiObj = checkBiObjs[i];
			if(checkBiObj.checked && biobidstr.indexOf(checkBiObj.value + "__") == -1){
				biobidstr = biobidstr + checkBiObj.value + "__"+(i+1) + ",";
			}
		}
		//deletes last "," from string
		biobidstr = (biobidstr == "")?"":biobidstr.substring(0, biobidstr.length -1);
		
		$('selected_biobject_ids').value = biobidstr;
		
		if(winDS!=null){
			winDS.destroy();
		}
		
		document.getElementById('formmsg').value='MESSAGE_DOCUMENTS_SELECTED';
		document.getElementById('jobdetailform').submit();
	}
	
	function saveCall() {
		document.getElementById('formmsg').value='MESSAGE_SAVE_JOB';
		document.getElementById('jobdetailform').submit();
	}
	
</script>

<!-- ********************** SCRIPT FOR PARAMETER LOOKUP **************************** -->
<script>

	var winLRL = null;
	var parfieldName = '';

	function getLovList(idObj, idPar, urlNamePar) {
		$('loadingdiv').style.display='inline';
		
		rolefield = $('role_par_'+idObj+'_'+ indexTabOpened +'_'+urlNamePar);
		parfieldName = 'par_'+idObj+'_' + indexTabOpened + '_'+urlNamePar;
		role = rolefield.value;
		if(role==null) {
			role = rolefield.options[rolefield.selectedIndex].value;
		}
		url = "<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId)%>";
	    pars = "&PAGE=LovLookupAjaxPage";
	    pars +="&roleName="+role;
	    pars += "&parameterId="+idPar;
	    pars += "&parameterFieldName="+parfieldName;
	    pars += "&<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>=TRUE";
		new Ajax.Request(url,
       			{
           			method: 'post',
           			parameters: pars,
       				onSuccess: function(transport){
                           			response = transport.responseText || "";
                           			displayList(response);
                       			},
       				onFailure: showError
       			}
   		);
	}
		
	function displayList(html) {
		winLRL = new Window('winLRLId', {className: "alphacube", title: "", width:650, height:380, destroyOnClose: true});
      	winLRL.setDestroyOnClose();
      	winLRL.setHTMLContent(html);
      	winLRL.showCenter(true);
	}
		
	function showError()  {
		alert('Error while getting values');
	}
	
	    
	observerLRLshow = { 
		onShow: function(eventName, win) {
			if (win == winLRL) {
			    $('loadingdiv').style.display='none';
			    parfield = document.getElementById(parfieldName);
			    parfieldval = parfield.value;
			    parfieldvalues = parfieldval.split(';');
			    checks = document.getElementsByName('rowcheck');
			    for(i=0; i<checks.length; i++) {
			    	check = checks[i];
			    	for(j=0; j<parfieldvalues.length; j++) {
			    		value = parfieldvalues[j];
			    		if(check.value == value) {
			    			check.checked = true;
			    		}
			    	}
			    }
			}
		}
	}
		
	observerLRLclose = { 
		onClose: function(eventName, win) {
			if (win == winLRL) {
				var valuesArray = new Array();
			    parfield = document.getElementById(parfieldName);
			    checks = document.getElementsByName('rowcheck');
			    for(i=0; i<checks.length; i++) {
			    	check = checks[i];
			    	if(check.checked) {
			    		val = check.value;
			    		if (!valuesArray.contains(val)) {
			    			valuesArray.push(val);
			    		}
			    	}
			    }
			    if(valuesArray.length > 0) {
			    	parfield.value = valuesArray.join(';');
			    }
			}
		}
	}
	
	observerLRLdestroy = { 
		onDestroy: function(eventName, win) { 
			if (win == winLRL) { 
				winLRL = null; 
			} 
	 	} 
	}

	Windows.addObserver(observerLRLshow);
	Windows.addObserver(observerLRLclose);
	Windows.addObserver(observerLRLdestroy);

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


<div id='loadingdiv' class='div_loading' >
	<center>
		<br/><br/>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "loading" />	
		</span>
		<br/><br/>
		<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/wapp/loading.gif", currTheme)%>' />
	</center>
</div>

<form id="jobdetailform" method="post" action="<%=formUrl%>" >
	<input type="hidden" name="PAGE" value="JobManagementPage" />
	<input type="hidden" name="<%= LightNavigationManager.LIGHT_NAVIGATOR_DISABLED %>" value="true" />
	<input id="formmsg" type="hidden" name="MESSAGEDET" value="" />
	<input id="splitterparameter" type="hidden" name="splitter" value="<%=splitter%>" />
	<input id="selected_biobject_ids" type="hidden" name="selected_biobject_ids" value="" />

<!-- *********************** PAGE TITLE ****************************** -->

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "scheduler.jobDetail"  bundle="component_scheduler_messages"/>		
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "scheduler.back" bundle="component_scheduler_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/back.png", currTheme)%>' 
      				 alt='<spagobi:message key = "scheduler.back"  bundle="component_scheduler_messages"/>' />
			</a>
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='javascript:saveCall()'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "scheduler.save" bundle="component_scheduler_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/save.png", currTheme)%>' 
      				 alt='<spagobi:message key = "scheduler.save"  bundle="component_scheduler_messages"/>' />
			</a>
		</td>
	</tr>
</table>


<!-- *********************** FIRST FORM ****************************** -->

<br/>

<div class="div_form_container" >
	<div class="div_form_margin" >
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "scheduler.jobName"  bundle="component_scheduler_messages"/>
				</span>
			</div>
			<%
				String readonly  = "";
				String jobName = jobInfo.getJobName();
				if(jobName!=null) {
					jobName = jobName.trim();
					if(!jobName.equals(""))
						readonly = " readonly ";
				}
			%>
			<div class='div_form_field'>
				<input class='portlet-form-input-field' type="text" name="jobname" 
			      	   size="50" value="<%=StringEscapeUtils.escapeHtml(jobInfo.getJobName())%>"  <%=readonly%> >
			    &nbsp;*
			</div>
		</div>
		<div class="div_form_row" >
			<div class='div_form_label'>
				<span class='portlet-form-field-label'>
					<spagobi:message key = "scheduler.jobDescription"  bundle="component_scheduler_messages"/>
				</span>
			</div>
			<div class='div_form_field'>
				<input class='portlet-form-input-field' type="text" 
					   name="jobdescription" size="50" value="<%=StringEscapeUtils.escapeHtml(jobInfo.getJobDescription())%>" >
			</div>
		</div>
	</div>
</div>

<div style='clear:left;'></div>


<br/>


<!-- *********************** ERROR TAG ****************************** -->

<spagobi:error/>




<!-- *********************** TABS ****************************** -->



<div>
	<table>
		<tr> 
			<td class='titlebar_level_2_text_section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "scheduler.documentparameters"  bundle="component_scheduler_messages"/>	
			</td>
			<td class='titlebar_level_2_empty_section_bis'>
				<a href='javascript:opencloseDocumentSelectionWin()'> 
	      			<img class='header-button-image-portlet-section_bis' 
	      				 title='<spagobi:message key = "scheduler.addocument" bundle="component_scheduler_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/edit_add.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "scheduler.addocument"  bundle="component_scheduler_messages"/>' />
				</a>
			</td>
			<td class='titlebar_level_2_empty_section_bis'>&nbsp;</td>
			<td class='titlebar_level_2_empty_section_bis'>
				<a href="javascript:removeTab('<spagobi:message key="scheduler.DeleteDocumentConfirm" bundle="component_scheduler_messages"/>')"> 
	      			<img class='header-button-image-portlet-section_bis' 
	      				 title='<spagobi:message key = "scheduler.removedocument" bundle="component_scheduler_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/edit_remove.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "scheduler.removedocument"  bundle="component_scheduler_messages"/>' />
				</a>
			</td>
			<td class='titlebar_level_2_empty_section_bis'>&nbsp;</td>
			<td class='titlebar_level_2_empty_section_bis'>
				<a href="javascript:copyTab()"> 
	      			<img class='header-button-image-portlet-section_bis' 
	      				 title='<spagobi:message key = "scheduler.copydocument" bundle="component_scheduler_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/copy_schedule.gif", currTheme)%>' 
	      				 alt='<spagobi:message key = "scheduler.copydocument"  bundle="component_scheduler_messages"/>' />
				</a>
			</td>
			<td class='titlebar_level_2_empty_section_bis'>&nbsp;</td>
		</tr>
	</table>
	
	
	<div style='width:100%;visibility:visible;' class='UITabs'>
		<div class="first-tab-level" style="background-color:#f8f8f8">
			<div style="overflow: hidden; width:  100%">
	
	<%
		if(jobBiobjects.size()==0){
	%>
				<br/>
				<spagobi:message key = "scheduler.nodocumentSelected"  bundle="component_scheduler_messages"/>
				<br/>
	<%			
		} else {
			iterJobBiobjs = jobBiobjects.iterator();
	   	 	index = 0;
	    	String tabClass = "tab selected"; 
			while(iterJobBiobjs.hasNext()) {
				BIObject biobj = (BIObject)iterJobBiobjs.next();
				
				String biobjName = biobj.getName();
				if(index > 0) {
					tabClass = "tab"; 
				}
				index ++;
	%>
				<div id="tabbiobj<%=biobj.getId()%>__<%=index%>"  class='<%= tabClass%>'>
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
	
	
<!-- *********************** TAB FORMS ****************************** -->	
	
	<%
		IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
		IParameterDAO pardao = DAOFactory.getParameterDAO();
		iterJobBiobjs = jobBiobjects.iterator();
	    index = 0;
	    String setTabOpened = "";
	    String setIndexTabOpened = "";
	    String displaytab = "inline";
		while(iterJobBiobjs.hasNext()) {
			BIObject biobj = (BIObject)iterJobBiobjs.next();
			List pars = biobj.getBiObjectParameters();
			if(index > 0) {
				displaytab = "none";
			} else {
				setTabOpened = "<script>tabOpened = '"+biobj.getId()+"__"+(index+1)+"';</script>";
				setIndexTabOpened = "<script>indexTabOpened = "+(index+1)+";</script>";
			}
			index ++;
	%>
	
	<%=setTabOpened%>
	<%=setIndexTabOpened%>
	<div width="100%" id="areabiobj<%=biobj.getId()%>__<%=index%>" style="display:<%=displaytab%>;" >
	
		<%
			if( (pars==null) || (pars.size()==0) ) {
		%>		
			
      	<br/>
		
		<div class='div_form_container' >
		   <div class='div_form_margin'>
		   		<div class='div_form_row' >
				   <div class='div_form_message nowraptext'>
				       	<span class='portlet-form-field-label'>
		        	      <spagobi:message key = "scheduler.noparameter"  bundle="component_scheduler_messages"/>
		            	</span>
		         	</div>
		         </div>
			</div>
		</div>
		<br/>
		<%
      		} else {
      	%>
			<br>
			<div class='div_form_container' >
				<div class='div_form_margin'>
			
				<%
				Iterator iterPars = pars.iterator();
				while(iterPars.hasNext()) {
					BIObjectParameter biobjpar = (BIObjectParameter)iterPars.next();
					ParameterValuesRetriever strategy = biobjpar.getParameterValuesRetriever();
					String concatenatedValue = "";
					List values = biobjpar.getParameterValues();
					if(values!=null) {
						Iterator itervalues = values.iterator();
						while(itervalues.hasNext()) {
							String value = (String)itervalues.next();
							concatenatedValue += value + splitter;
						}
						if(concatenatedValue.length()>0) {
							concatenatedValue = concatenatedValue.substring(0, concatenatedValue.length() - 1);
						}						
					}		
				%>		
					<div class='div_form_row' >
						<div class='div_form_label'>
							<span class='portlet-form-field-label'>
				            	<%=biobjpar.getLabel()%>
				          	</span>
				    	</div>
				    	
				    	<div style="float:left;width:500px;margin-bottom:30px;">

				    		<spagobi:message key = "scheduler.parameterValuesStrategyQuestion"  bundle="component_scheduler_messages"/>
				    		<br>
				    		<div style="height: 2px" >&nbsp;</div>
						  	<select name='<%="par_"+biobj.getId()+"_"+index+"_"+biobjpar.getParameterUrlName()+"_strategy"%>'
										id='<%="par_"+biobj.getId()+"_"+index+"_"+biobjpar.getParameterUrlName()+"_strategy"%>'
										onChange="<%="change_"+biobj.getId()+"_"+index+"_"+biobjpar.getParameterUrlName()+"_strategy(this.selectedIndex);"%>">
								<option value='fixedValues' <%= strategy == null ? "selected='selected'" : "" %>>
									<spagobi:message key = "scheduler.fixedValuesStrategy"  bundle="component_scheduler_messages"/>
								</option>
								<option value='loadAtRuntime' <%= (strategy != null && strategy instanceof RuntimeLoadingParameterValuesRetriever) ? "selected='selected'" : "" %>>
									<spagobi:message key = "scheduler.loadAtRuntimeStrategy"  bundle="component_scheduler_messages"/>
								</option>
								<% 
								Integer parId = biobjpar.getParID();
								Parameter parameter = pardao.loadForDetailByParameterID(parId);
								if (parameter.isTemporal()) { %>
									<option value='useFormula' <%= (strategy != null && strategy instanceof FormulaParameterValuesRetriever) ? "selected='selected'" : "" %>>
										<spagobi:message key = "scheduler.useFormulaStrategy"  bundle="component_scheduler_messages"/>
									</option>
								<% } %>
							</select>
							
							
								
							
							<script>
							function <%="change_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_strategy"%>(index) {
								if (index == 0) {
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_fixedValues"%>').style.display='block';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_loadAtRuntime"%>').style.display='none';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_useFormula"%>').style.display='none';
								}
								if (index == 1) {
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_fixedValues"%>').style.display='none';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_loadAtRuntime"%>').style.display='block';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_useFormula"%>').style.display='none';
								}
								if (index == 2) {
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_fixedValues"%>').style.display='none';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_loadAtRuntime"%>').style.display='none';
									$('<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_useFormula"%>').style.display='block';
								}
							}
							</script>
							
							
							
				    		<div name='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_fixedValues"%>'
				    				id='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_fixedValues"%>'
				    				style="margin-top:10px;display:<%= strategy == null ? "block" : "none" %>" >
				    			<div style="height: 2px" >&nbsp;</div>
							
							<%
							  		List roles = biobjdao.getCorrectRolesForExecution(biobj.getId(), userProfile);
							  		if(roles.size()>0) {
							  	%>
							  	(<spagobi:message key = "scheduler.usingrole"  bundle="component_scheduler_messages"/> 
								&nbsp;
								<select name='role_par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())%>'
										id='role_par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())%>' >
									<% 
										Iterator iterRoles = roles.iterator(); 
										while(iterRoles.hasNext()) {
											String role = (String)iterRoles.next();
									%>
									<option value='<%=role%>'><%=StringEscapeUtils.escapeHtml(role)%></option>
									<%
										}
									%>
								</select>)
								<%
						  		} // if(roles.size()>0)
								%>
								<br>
							<div style="height: 2px" >&nbsp;</div>
								
				    				
					    		<input class='portlet-form-input-field' 
							  	       id="<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())%>"
							  	       name="<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())%>" 
							  	       type="text" value="<%=StringEscapeUtils.escapeHtml(concatenatedValue)%>" size="50" autocomplete="off" />
							  	&nbsp;&nbsp;&nbsp;
							  	
							  	
							  	<a style='text-decoration:none;' href="javascript:getLovList('<%=biobj.getId()%>', '<%=biobjpar.getParID()%>', '<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())%>')">
							  		<img title='<spagobi:message key = "scheduler.fillparameter"  bundle="component_scheduler_messages"/>' 
	      				 				src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme)%>' 
	      				 				alt='<spagobi:message key = "scheduler.fillparameter"  bundle="component_scheduler_messages"/>' />
							  	</a>
							  	<br>
							  	<br>
							  
							  	
							</div>	
								
				    		
				    		
				    		<div name='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_loadAtRuntime"%>'
				    				id='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_loadAtRuntime"%>'
				    				style="margin-top:10px;display:<%= (strategy != null && strategy instanceof RuntimeLoadingParameterValuesRetriever) ? "block" : "none" %>" >
				    				
									<spagobi:message key = "scheduler.loadAtRuntimeRole"  bundle="component_scheduler_messages"/>
									<select name='par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName()) + "_loadWithRole"%>'
											id='par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName()) + "_loadWithRole"%>' >
										<% 	
											String roleToBeUsed = null;
											if (strategy != null && strategy instanceof RuntimeLoadingParameterValuesRetriever) {
												roleToBeUsed = ((RuntimeLoadingParameterValuesRetriever) strategy).getRoleToBeUsed();
											}
											Iterator iterRoles = roles.iterator(); 
											while(iterRoles.hasNext()) {
												String role = (String)iterRoles.next();
										%>
										<option value='<%=role%>' <%= role.equals(roleToBeUsed) ? "selected='selected'" : "" %>><%=role%></option>
										<%
											}
										%>
									</select>
									
								
				    		</div>
				    		
				    		<div name='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_useFormula"%>'
				    				id='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_useFormula"%>'
				    				style="margin-top:10px;display:<%= (strategy != null && strategy instanceof FormulaParameterValuesRetriever) ? "block" : "none" %>" >
				    				
				    				<spagobi:message key = "scheduler.formulaName"  bundle="component_scheduler_messages"/>
									<select name='par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName()) + "_formula"%>'
											id='par_<%=biobj.getId()%>_<%=index%>_<%=StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName()) + "_formula"%>' >
										<% 	
											String formulaToBeUsed = null;
											if (strategy != null && strategy instanceof FormulaParameterValuesRetriever) {
												Formula f = ((FormulaParameterValuesRetriever) strategy).getFormula();
												formulaToBeUsed = f.getName();
											}
											Iterator formulasIt = formulas.iterator(); 
											while(formulasIt.hasNext()) {
												Formula f = (Formula) formulasIt.next();
												String formulaName = f.getName();
												String formulaDescription = f.getDescription();
												if (formulaDescription.startsWith("#")) {
													formulaDescription = msgBuilder.getMessage(formulaDescription.substring(1), "component_scheduler_messages", request);
												}
										%>
										<option value='<%=StringEscapeUtils.escapeHtml(formulaName)%>' <%= formulaName.equals(formulaToBeUsed) ? "selected='selected'" : "" %>><%=StringEscapeUtils.escapeHtml(formulaDescription)%></option>
										<%
											}
										%>
									</select>
				    		</div>
				    	
				    	
							  	<div>
							  	<select name='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_Iterative"%>'
											id='<%="par_"+biobj.getId()+"_"+index+"_"+StringEscapeUtils.escapeHtml(biobjpar.getParameterUrlName())+"_Iterative"%>'>
									<option value='false'><spagobi:message key = "scheduler.doNotIterateOnParameterValues"  bundle="component_scheduler_messages"/></option>
									<option value='true' <%= biobjpar.isIterative() ? "selected='selected'" : "" %>>
										<spagobi:message key = "scheduler.iterateOnParameterValues"  bundle="component_scheduler_messages"/>
									</option>
								</select>
								</div>
				    	
				    	
						</div>
					</div>
					<div style="clear:left;"></div>
		
		<%
				} // end while
		%>
				</div>
			</div>
			<br/>
		<%
			} // enf if
		%>
		</div>
	
	
	
	
	<%	
		}
	%>
	


<!-- *********************** DIV SELECT DOCUMENT (HIDDEN) ****************************** -->



<div id="selectiondocumentcontainerdiv">
	<div id="selectiondocumentdiv" style="display:none;width:97%;" >
		<table>
			<tr> 
				<td class='titlebar_level_2_text_section' style='vertical-align:middle;padding-left:5px;'>
					<spagobi:message key = "scheduler.documentselection"  bundle="component_scheduler_messages"/>		
				</td>
				<td class='titlebar_level_2_empty_section'>&nbsp;</td>
				<td class='titlebar_level_2_button_section'>
					<a href='javascript:fillParamCall()'> 
		      			<img class='header-button-image-portlet-section' 
		      				 title='<spagobi:message key = "scheduler.save" bundle="component_scheduler_messages" />' 
		      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/save.png", currTheme)%>' 
		      				 alt='<spagobi:message key = "scheduler.save"  bundle="component_scheduler_messages"/>' />
					</a>
				</td>
			</tr>
		</table>
		<spagobi:treeObjects moduleName="JobManagementModule"  
							 htmlGeneratorClass="it.eng.spagobi.tools.scheduler.gui.SchedulerTreeHtmlGenerator" />
		<br/>
	</div>
</div>



</form>

 
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>