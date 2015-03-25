<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.tools.importexport.ImportExportConstants" %>
<%@page import="it.eng.spagobi.tools.importexport.ImportResultInfo"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.tools.importexport.bo.AssociationFile"%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<%  
	String exportFilePath = (String)aServiceRequest.getAttribute(ImportExportConstants.EXPORT_FILE_PATH);
	ImportResultInfo iri = (ImportResultInfo)aServiceRequest.getAttribute(ImportExportConstants.IMPORT_RESULT_INFO);

   	Map backUrlPars = new HashMap();
	backUrlPars.put("ACTION_NAME", "START_ACTION");
	backUrlPars.put("PUBLISHER_NAME", "LoginSBIToolsPublisher");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
   
	Map formExportUrlPars = new HashMap();
	String formExportUrl = urlBuilder.getUrl(request, formExportUrlPars);
   
	Map formImportUrlPars = new HashMap();
	String formImportUrl = urlBuilder.getUrl(request, formImportUrlPars);
  
	String downloadUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(userId);
	downloadUrl += "&ACTION_NAME=DOWNLOAD_FILE_ACTION";
	if((exportFilePath!=null) && !exportFilePath.trim().equalsIgnoreCase("") ) {
		downloadUrl += "&OPERATION=downloadExportFile&FILE_NAME="+  exportFilePath;
	}
   
%>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.importexport" bundle="component_impexp_messages"/>
		</td>
		<%if(ChannelUtilities.isPortletRunning()) { %>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "Sbi.back" bundle="component_impexp_messages" />' 
      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/back.png", currTheme)%>' 
      				 alt='<spagobi:message key = "Sbi.back"  bundle="component_impexp_messages"/>' />
			</a>
		</td>
		<% } %>
	</tr>
</table>

<script>
	function submitExportForm() {
		cleanBIObjectsIdsInput();
		if (document.getElementById('<%= ImportExportConstants.OBJECT_ID %>').value == '') {
			alert('No documents selected!!');
			return;
		}
		var divprog = document.getElementById('divProgress');
		divprog.style.display='inline';
		document.getElementById('exportForm').submit();
		var divprog = document.getElementById('divDownload');
		divprog.style.display='none';
	}
	
	function submitDownloadForm(actionurl) {
		downform = document.getElementById('downForm');
		var divdown = document.getElementById('divDownload');
		divdown.style.display='none';
		downform.submit();
	}
	
	function cleanBIObjectsIdsInput() {
		biobjectsIdArray = new Array();
		var checks = document.getElementsByName('<%= ImportExportConstants.OBJECT_ID_PATHFUNCT %>');
		for(var i=0; i< checks.length; i++){
			check = checks[i];
			if (check.checked) {
				value = check.value;
				chuncks = value.split('_');
				biobjectsIdArray.push(chuncks[0]);
			}
		}
		document.getElementById('<%= ImportExportConstants.OBJECT_ID %>').value=biobjectsIdArray.join(';');
	}
	
</script>




<div class="div_background_no_img">


	<spagobi:error/>

 

  	
	<div style="float:left;width:50%;" class="div_detail_area_forms">
		<form method='POST' action='<%=formExportUrl%>' id='exportForm' name='exportForm'>
	  		<input type="hidden" name="PAGE" value="ImportExportPage" />
	  		<input type="hidden" name="MESSAGEDET" value="Export" />
	  		<input type="hidden" name="<%= ImportExportConstants.OBJECT_ID %>" id="<%= ImportExportConstants.OBJECT_ID %>" value="" />
	
			<div class='portlet-section-header' style="float:left;width:88%;">	
					<spagobi:message key = "SBISet.export" bundle="component_impexp_messages"/>
			</div>
			<div style="float:left;width:10%;">
			  <center>
				<a href="javascript:submitExportForm();">
						<img src= '<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/export32.png", currTheme) %>'
							title='<spagobi:message key = "SBISet.export" bundle="component_impexp_messages"/>' 
							alt='<spagobi:message key = "SBISet.export" bundle="component_impexp_messages"/>' />
				</a>
			  </center>
			</div>
			<div id="divProgress"  
				 style="clear:left;margin-left:15px;padding-top:15px;display:none;color:#074B88;">
				<spagobi:message key = "SBISet.importexport.opProg" bundle="component_impexp_messages"/>
			</div>
			<div id="divDownload" style="clear:left;display:none;">
				 <div style="padding-left:15px;padding-right:15px;color:#074B88;">
					<spagobi:message key = "SBISet.importexport.opComplete"  bundle="component_impexp_messages"/>
					<a style='text-decoration:none;color:#CC0000;' href="javascript:submitDownloadForm()">
						<spagobi:message key = "Sbi.download" bundle="component_impexp_messages"/>
					</a>
				</div>
				<div style="padding-left:15px;padding-right:15px;color:#074B88;">
					<spagobi:message key = "SBISet.importexport.exportCompleteResourcesWarning" bundle="component_impexp_messages"/>
				</div>
			</div>
			<div style="clear:left;margin-left:15px;padding-top:10px;">
				<spagobi:message key = "SBISet.importexport.nameExp" bundle="component_impexp_messages"/>
				: 
				<input type="text" name="exportFileName" size="30" />
	            <br/>
	            <input type="checkbox" name="exportSubObj" />
				<spagobi:message key = "SBISet.importexport.expSubView" bundle="component_impexp_messages"/>
				<br/>
				<input type="checkbox" name="exportSnapshots" />	
				<spagobi:message key = "SBISet.importexport.expSnapshots" bundle="component_impexp_messages"/>
				<%-- 
				<br/>
				<input type="checkbox" name="exportResources" />	
				<spagobi:message key = "SBISet.importexport.expResources" bundle="component_impexp_messages"/>
				--%>
			</div>
		
		</form>
		
		<div style="clear:left;margin-bottom:10px;">
			<spagobi:treeObjects moduleName="TreeObjectsModule"  
				htmlGeneratorClass="it.eng.spagobi.tools.importexport.publishers.AdminExportTreeHtmlGenerator" />
		</div>
	</div>
	



	<form method='POST' action='<%=downloadUrl%>' id='downForm' name='downForm'>
	</form>

    <form method='POST' action='<%=formImportUrl%>' id='importForm' name='importForm' enctype="multipart/form-data">
	<input type="hidden" name="PAGE" value="ImportExportPage" />
	<input type='hidden' name='MESSAGEDET' value='Import' />
 	<div style="float:left;width:45%" class="div_detail_area_forms">
		<div class='portlet-section-header' style="float:left;width:78%;">
				<spagobi:message key = "SBISet.import" bundle="component_impexp_messages"/>
		</div>
		<div style="float:left;width:20%;">
		  <center>
			<a class='link_without_dec' style="text-decoration:none;" href="javascript:document.getElementById('importForm').submit()">
					<img src= '<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/importexport32.gif", currTheme) %>'
						title='<spagobi:message key = "SBISet.import" bundle="component_impexp_messages"/>' 
						alt='<spagobi:message key = "SBISet.import" bundle="component_impexp_messages"/>' />
				</a>
			&nbsp;
			<a class='link_without_dec' href="javascript:showAssList('MANAGE')">
					<img src= '<%= urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/association32.jpg", currTheme) %>'
						width="28px" height="28px"
						title='<spagobi:message key = "impexp.manageAss" bundle="component_impexp_messages"/>' 
						alt='<spagobi:message key = "impexp.manageAss" bundle="component_impexp_messages"/>' />
			</a>
			</center>
		</div>
		<div style="clear:left;margin-bottom:10px;padding-top:10px;">
			<spagobi:message key = "SBISet.importexport.fileArchive" bundle="component_impexp_messages"/>:		
			<input type="file"  name="exportedArchive" />
			<br/>
			<br/>
			<fieldset class='fieldset'>
				<legend class='form_legend'>
					<spagobi:message key = "impexp.Associations" bundle="component_impexp_messages"/>
				</legend>
				<input type="radio"  name="importAssociationKind" CHECKED value="noassociations"  
			       onclick="document.getElementById('rowChoseAssFile').style.display='none'"/>
			    <spagobi:message key = "impexp.withoutAss" bundle="component_impexp_messages"/>
			    <br/>
			    <input type="radio"  name="importAssociationKind" value="predefinedassociations"
			       onclick="document.getElementById('rowChoseAssFile').style.display='inline' "/>
			    <spagobi:message key = "impexp.mandatoryAss" bundle="component_impexp_messages"/>
			    <br/>     
			    <input type="radio"  name="importAssociationKind" value="defaultassociations"
			       onclick="document.getElementById('rowChoseAssFile').style.display='inline'" /> 
			    <spagobi:message key = "impexp.defaultAss" bundle="component_impexp_messages"/>
			    
			    <br/>
			    <br/>
			    
			    <div id="rowChoseAssFile" style='display:none;'>
			    	<div style="width:150px;float:left;">
				    	<spagobi:message key = "impexp.savedAss" bundle="component_impexp_messages"/>: &nbsp;&nbsp;
			    	</div>
		    		<input type="text" id="textReadOnlyAssName" name="textReadOnlyAssName" readonly value="" />
	    			<input type="hidden" id="hidAssId" name="hidAssId" value="" />
	    			<a class='link_without_dec' href="javascript:showAssList('SELECT')" style="text-decoration:none;">
	    				<img src= '<%= urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme) %>'
							 title='<spagobi:message key = "impexp.selectFromList" bundle="component_impexp_messages"/>' 
							alt='<spagobi:message key = "impexp.selectFromList" bundle="component_impexp_messages"/>' />
					</a>
					<div style="clear:left;"></div>
					<div style="width:150px;float:left;">
						<spagobi:message key = "SBISet.importexport.fileAssociation" bundle="component_impexp_messages"/>: &nbsp;&nbsp;
					</div>
					<input type="file"  name="associationsFile" />
				    <br/>
				    <br/>
			    </div>
			</fieldset>
		
		</div>
		
		<%
		if(iri!=null) {
		%>	
		<div id="divImportResult" style="clear:left;color:#074B88;">	 		 
			<%
				String logFileName = iri.getLogFileName();
				if( (logFileName!=null) && !logFileName.equals("") ) {	
					 String downloadLogUrl = ChannelUtilities.getSpagoBIContextName(request);
					 downloadLogUrl += "/servlet/AdapterHTTP?ACTION_NAME=DOWNLOAD_FILE_ACTION";
					 downloadLogUrl += "&OPERATION=downloadLogFile&FILE_NAME=" + logFileName + "&FOLDER_NAME=" + iri.getFolderName();
			%>
			<spagobi:message key = "SBISet.importexport.opComplete" bundle="component_impexp_messages"/>
			<ul>
				<li>
					<a style='text-decoration:none;color:#CC0000;' href='<%=downloadLogUrl%>'>
						<spagobi:message key = "Sbi.downloadLog" bundle="component_impexp_messages"/>
					</a>
				</li>
			<% 	}
				String assFileName = iri.getAssociationsFileName();
				if( (assFileName!=null) && !assFileName.equals("") ) {	
					 String downloadAssUrl = ChannelUtilities.getSpagoBIContextName(request);
					 downloadAssUrl += "/servlet/AdapterHTTP?ACTION_NAME=DOWNLOAD_FILE_ACTION";
					 downloadAssUrl += "&OPERATION=downloadAssociationFile&FILE_NAME=" + assFileName + "&FOLDER_NAME=" + iri.getFolderName();	
		    %>
				<li>
					<a style='text-decoration:none;color:#CC0000;' href='<%=downloadAssUrl%>'>
						<spagobi:message key = "Sbi.downloadAss" bundle="component_impexp_messages"/>
					</a>
				</li>
				<li>
					<a style='text-decoration:none;color:#CC0000;' href='javascript:openCloseSaveAssForm()'>
						<spagobi:message key = "impexp.saveAss" bundle="component_impexp_messages"/>
					</a>
				</li>
			</ul>
			<% } %>
			<%--	
				Map manualTasks = iri.getManualTasks();
				if(!manualTasks.isEmpty()) {
			%>
			<br/>
			<br/>
			<span class="portlet-form-field-label" style="color:#CC0000;">
				<spagobi:message key = "impexp.manualtask.exists" bundle="component_impexp_messages"/>
			</span>
			<ul>
			<% 	
					Set keys = manualTasks.keySet();
			    	Iterator keysIter = keys.iterator();
			    	while(keysIter.hasNext()){
			    		String key = (String)keysIter.next();
			    		String path = (String)manualTasks.get(key);
			    		String downloadManualTaskUrl = ChannelUtilities.getSpagoBIContextName(request);
			    		downloadManualTaskUrl += "/servlet/AdapterHTTP?ACTION_NAME=DOWNLOAD_FILE_ACTION";
			    		downloadManualTaskUrl += "&OPERATION=downloadManualTask&PATH=" + path;	
		    %>
		    	<li>
		    		<a style='text-decoration:none;color:#074B88;font-size:9px;' href='<%=downloadManualTaskUrl%>'>
						<%=key%>
					</a>
		    	</li>
		    <%
			   	 	}
			%>
			</ul>
			<%
				}
			--%>
		</div>
		<%
		}
		%>
	</div>
	</form>


	<div style="clear:left;">
			&nbsp;
	</div>


<%
	if((exportFilePath!=null) && !exportFilePath.trim().equalsIgnoreCase("") ) {
%>
	<script>
		var divprog = document.getElementById('divProgress');
		divprog.style.display='none';
		var divprog = document.getElementById('divDownload');
		divprog.style.display='inline';
	</script>
<% 
	}
%>

	
	<%
	AssociationFile assFile = (AssociationFile) aServiceRequest.getAttribute(ImportExportConstants.IMPORT_ASSOCIATION_FILE);
	String associationName = (assFile != null) ? assFile.getName() : "";
	String associationDescription = (assFile != null) ? assFile.getDescription() : "";
	%>
	<div id="divSaveAssFileForm" name="divSaveAssFileForm" style="display:none;" >
		<table>
			<tr height='25px'>
				<td>&nbsp;&nbsp;
					<spagobi:message key = "impexp.name" bundle="component_impexp_messages"/>:
				</td>
				<td><input type="text" name="NAME" id="nameNewAssToSave" value="<%=StringEscapeUtils.escapeHtml(associationName)%>"/></td>
			</tr>
			<tr height='25px'>
				<td>&nbsp;&nbsp;
					<spagobi:message key = "impexp.description" bundle="component_impexp_messages"/>:
				</td>
				<td><input type="text" name="DESCRIPTION" id="descriptionNewAssToSave" value="<%=StringEscapeUtils.escapeHtml(associationDescription)%>"/></td>
			</tr>
			<tr height='45px' valign='middle'>
				<td>&nbsp;</td>
				<td>
				   <a class='link_without_dec' href='javascript:checkIfExists()'>
					   <img src= '<%= urlBuilder.getResourceLinkByTheme(request, "/img/Save.gif", currTheme) %>'
							title='<spagobi:message key = "impexp.save" bundle="component_impexp_messages"/>' 
							alt='<spagobi:message key = "impexp.save" bundle="component_impexp_messages"/>' />
					</a>
				</td>
			</tr>
			<tr height='45px' valign='middle'>
				<td>&nbsp;</td>
				<td><div id="divshowresultsave">&nbsp;</div></td>
			</tr>
		</table>
	</div>
	

	<script>
	
	    winlistass = null;

		function showAssList(modality) {
			if(winlistass!=null) return;
			getlisturl = "<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId)%>";
			getlisturl += "&ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION&MESSAGE=GET_ASSOCIATION_FILE_LIST";
			getlisturl += "&language=<%=locale.getLanguage()%>&country=<%=locale.getCountry()%>";
			getlisturl += "&MODALITY=" + modality;
			winlistass = new Window('win_list_ass', {className: "alphacube", title: "<spagobi:message key = "impexp.listAssFile" bundle="component_impexp_messages"/>", width:550, height:350, hideEffect:Element.hide, showEffect:Element.show, destroyOnClose:true});
       		winlistass.setURL(getlisturl);
       		winlistass.setDestroyOnClose();
       		winlistass.showCenter();
	 	} 
	    
	    
	    winsaveass = null;
	    
	    function openCloseSaveAssForm() {
			winsaveass = new Window('win_save_ass', {className: "alphacube", title: "<spagobi:message key = "impexp.saveAss" bundle="component_impexp_messages"/>", width:300, height:130, hideEffect:Element.hide, showEffect:Element.show, destroyOnClose:true});
       		winsaveass.setContent('divSaveAssFileForm', false, false);
       		winsaveass.setDestroyOnClose();
       		winsaveass.showCenter();
		}
		
		
		observerClose = {
      		onClose: function(eventName, win) {
        		if(win == winlistass) {
          			winlistass = null;
        		}
        		if(win == winsaveass) {
        			document.getElementById('divSaveAssFileForm').style.display='none';
        		}
     		 }
   	 	}
   		Windows.addObserver(observerClose);
			
		
		function selectAssFile(idass, nameass) {
	    	text = document.getElementById('textReadOnlyAssName');
	    	hid = document.getElementById('hidAssId');
	        text.value=nameass;
	        hid.value=idass;
	        Windows.closeAll();
	    }
		
		
		function checkIfExists() {
			nameass = document.getElementById('nameNewAssToSave').value;
			if (nameass==""){
				alert('<spagobi:message key = "Sbi.saving.nameNotSpecified" bundle="component_impexp_messages"/>');
				return;
			} 
			// check if the association file alreay exists
			checkAssUrl = "<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId)%>";
			pars = "&ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION&MESSAGE=CHECK_IF_EXISTS&ID=" + document.getElementById('nameNewAssToSave').value;
			new Ajax.Request(checkAssUrl,
          		{
            		method: 'post',
            		parameters: pars,
            		onSuccess: function(transport){
                    	        	response = transport.responseText || "";
                        	    	saveAss(response);
                        	   },
            		onFailure: somethingWentWrongSaveAss,
            		asynchronous: false
         		 }
       		 );
		}	
		
		function saveAss(exists) {
			if (exists != "true" || confirm("<spagobi:message key = "Sbi.saving.alreadyExisting" bundle="component_impexp_messages"/>")) {
	       		saveAssUrl = "<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId)%>";
				pars = "&ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION&MESSAGE=SAVE_ASSOCIATION_FILE&OVERWRITE=TRUE";
				pars += "&language=<%=locale.getLanguage()%>&country=<%=locale.getCountry()%>";
				<%
					if( (iri!=null) && (iri.getAssociationsFileName()!=null) && !iri.getAssociationsFileName().equals("") ) {	
						String associationFileName = iri.getAssociationsFileName();
				%>
				pars += "&FILE_NAME=<%=associationFileName%>&FOLDER_NAME=<%=iri.getFolderName()%>";
				<%
					}
				%>
				nameass = document.getElementById('nameNewAssToSave').value;
				if(nameass==""){
					alert('<spagobi:message key = "Sbi.saving.nameNotSpecified" bundle="component_impexp_messages"/>');
					return;
				} 
				pars += "&NAME=" + nameass;
				descriptionass = document.getElementById('descriptionNewAssToSave').value;
				pars += "&DESCRIPTION=" + descriptionass;
				new Ajax.Request(saveAssUrl,
	          		{
	            		method: 'post',
	            		parameters: pars,
	            		onSuccess: function(transport){
	                    	        	response = transport.responseText || "";
	                        	    	showSaveAssResult(response);
	                        	   },
	            		onFailure: somethingWentWrongSaveAss
	         		 }
	       		 );
       		 }
	 	} 
	 	
	 	function somethingWentWrongSaveAss() {
	        divres = document.getElementById('divshowresultsave');
			divres.innerHTML="Non è possibile invocare il servizio di salvataggio";
     	}
     
	    function showSaveAssResult(response) {
			divres = document.getElementById('divshowresultsave');
			divres.innerHTML=response;
	    }
	    
	    
	</script>




</div>
