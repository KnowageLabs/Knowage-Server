<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.commons.constants.AdmintoolsConstants,

                 java.util.List,
                 it.eng.spagobi.commons.bo.Role,
                 it.eng.spagobi.commons.dao.DAOFactory,
                 it.eng.spago.navigation.LightNavigationManager" %>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.wapp.bo.Menu"%>
<%@page import="it.eng.spagobi.wapp.services.TreeMenuModule"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="java.io.File"%>
<%@page import="it.eng.spagobi.wapp.dao.MenuDAOImpl"%>
<%@page import="java.util.Vector"%>
<%@page import="it.eng.spago.error.EMFUserError"%>
<%@page import="it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality"%>
<%@page import="it.eng.spago.util.JavaScript"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.wapp.services.DetailMenuModule"%>

<% 
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailMenuModule"); 
    Menu menu = (Menu)moduleResponse.getAttribute(DetailMenuModule.MENU);
	String modality = (String)moduleResponse.getAttribute(AdmintoolsConstants.MODALITY);
	String parentId = (String) moduleResponse.getAttribute(DetailMenuModule.PARENT_ID);
	Menu parentMenu=null;
	if(parentId!=null)
	parentMenu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(parentId));
    
	
    Map formUrlPars = new HashMap();
	formUrlPars.put(AdmintoolsConstants.PAGE, DetailMenuModule.MODULE_PAGE);
	formUrlPars.put(AdmintoolsConstants.MESSAGE_DETAIL, modality);
	if (modality.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
		if(parentId!=null){
		formUrlPars.put(DetailMenuModule.PARENT_ID, parentId);
	}
	} else {
    	formUrlPars.put(DetailMenuModule.MENU_ID, menu.getMenuId().toString());
    }
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String formAct = urlBuilder.getUrl(request, formUrlPars);

    
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", TreeMenuModule.MODULE_PAGE);
    backUrlPars.put(SpagoBIConstants.OPERATION, SpagoBIConstants.FUNCTIONALITIES_OPERATION);
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);

    
    List allRoles = DAOFactory.getRoleDAO().loadAllRoles();
    String[][] sysRoles = new String[allRoles.size()][3];
    for(int i=0; i<allRoles.size(); i++) {
    	Role role = (Role)allRoles.get(i);
    	sysRoles[i][0] = role.getId().toString();
    	sysRoles[i][1] = role.getName();
    	sysRoles[i][2] = role.getDescription();
    	
    }
%>

<%@page import="it.eng.spagobi.commons.utilities.SpagoBIUtilities"%>
<form action="<%=formAct%>" method="post" id='formFunct' name = 'formFunct'>

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.menu.detailtitle" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('formFunct').submit()"> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBISet.detailMenu.saveButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBISet.detailMenu.saveButt" />' />
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message 
      			     key = "SBISet.detailMenu.backButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBISet.detailMenu.backButt" />'/>
			</a>
		</td>
	</tr>
</table>



<div class='div_background_no_img' id='' name='' >


    
<div class="div_detail_area_forms" id='menuForm' name='menuForm' >

	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.detailMenu.nameField" />
		</span>
	</div>
	<div class='div_detail_form'> 
		<input class='portlet-form-input-field' type="text" 
	      	   size="50" name="name" id="" 
	      	   value="<%= StringEscapeUtils.escapeHtml(menu.getName()) %>"  />
	   	&nbsp;*	
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.detailMenu.descriptionField" />
		</span>
	</div>
	<div class='div_detail_form'> 
	<% 
      String desc = menu.getDescr();
      if( (desc==null) || (desc.equalsIgnoreCase("null"))  ) {
      	desc = "";	
      } 
     %>
		<input class='portlet-form-input-field' type="text" 
               size="50" name="description" id="" value="<%= StringEscapeUtils.escapeHtml(desc) %>" />
	</div>


	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.menu.viewDocumentIcons" />
		</span>
		</div>
		<div class='div_detail_form'> 
		<input class='portlet-form-input-field' type="checkbox" 
	      	   size="50" name="viewicons" id="" 
	      	   value="true" <%if(menu.isViewIcons()){%> checked="checked" <%}%>/>
		</div>
		
		<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.menu.nodeContent" />
		</span>
		</div>
		<div class='div_detail_form'> 
		<select name="nodeContent" onchange="changeDiv(this.value)" class='portlet-form-input-field'>
			<option value="nodeEmpty"><spagobi:message key="SBISet.menu.nodeEmpty" /></option>
			<option value="nodeDocument" <%= menu.getObjId() != null ? "selected" : ""%>><spagobi:message key="SBISet.menu.nodeDocument" /></option>
			<option value="nodeStaticPage" <%= menu.getStaticPage() != null && !menu.getStaticPage().trim().equals("") ? "selected" : ""%>><spagobi:message key="SBISet.menu.nodeStaticPage" /></option>
			<option value="nodeFunctionality" <%= menu.getFunctionality() != null && !menu.getFunctionality().trim().equals("") ? "selected" : ""%>><spagobi:message key="SBISet.menu.nodeFunctionality" /></option>
			<option value="nodeExternalApp" <%= menu.getExternalApplicationUrl() != null && !menu.getExternalApplicationUrl().trim().equals("") ? "selected" : ""%>><spagobi:message key="SBISet.menu.nodeExternalApp" /></option>
		</select>
		</div>

		<%
		String toggledDivs = null;
		String currentVisibleDiv = null;
		toggledDivs = "['nodeDocument','nodeStaticPage', 'nodeFunctionality', 'nodeExternalApp']";
		currentVisibleDiv = "'nodeEmpty'";
		if (menu.getObjId() != null) {
			toggledDivs = "['nodeEmpty','nodeStaticPage', 'nodeFunctionality', 'nodeExternalApp']";
			currentVisibleDiv = "'nodeDocument'";
		}
		if (menu.getStaticPage() != null && !menu.getStaticPage().trim().equals("")) {
			toggledDivs = "['nodeEmpty','nodeDocument', 'nodeFunctionality', 'nodeExternalApp']";
			currentVisibleDiv = "'nodeStaticPage'";
		}
		if (menu.getFunctionality() != null && !menu.getFunctionality().trim().equals("")) {
			toggledDivs = "['nodeEmpty','nodeDocument','nodeStaticPage', 'nodeExternalApp']";
			currentVisibleDiv = "'nodeFunctionality'";
		}
		if (menu.getExternalApplicationUrl() != null && !menu.getExternalApplicationUrl().trim().equals("")) {
			toggledDivs = "['nodeEmpty','nodeDocument','nodeStaticPage', 'nodeFunctionality']";
			currentVisibleDiv = "'nodeExternalApp'";
		}
		%>

		<script type="text/javascript">
		var currentDiv = <%= currentVisibleDiv %>;
		var biobjectId = null;
		
		
		Ext.onReady(function() {
			divs = <%= toggledDivs %>;
			toggleDivs(divs);
		});
		
		function changeDiv(value) {
			divs = [currentDiv, value];
			toggleDivs(divs);
			currentDiv = value;
		}
		
		function toggleDivs(divs) {
			for (i = 0; i < divs.length ; i++) {
				element = Ext.get(divs[i]);
				element.enableDisplayMode("block");
				element.toggle(true);
			}
		}
		
		function loadObjectDetails(objId) {
			if (biobjectId != objId) {
				var errorImg = document.getElementById('docNotFoundErrorImg');
				if (errorImg) errorImg.style.display = 'none';
				loadSubObjects(objId);
				loadSnapshots(objId);
				biobjectId = objId;
			}
		}
		
		function loadSubObjects(objId) {
			var params;
			Ext.Ajax.request({
				url: '<%= request.getContextPath() + GeneralUtilities.getSpagoAdapterHttpUrl() %>?ACTION_NAME=GET_PUBLIC_SUBOBJECTS_INFO&<%=SpagoBIConstants.OBJECT_ID%>=' + objId + '&<%= LightNavigationManager.LIGHT_NAVIGATOR_DISABLED %>=TRUE',
				method: 'get',
				success: function (result, request) {
					response = result.responseText || "";
					showSubobjectsOptions(response);
				},
				params: params,
				failure: somethingWentWrong
			});
		}
		
		function loadSnapshots(objId) {
			var params;
			Ext.Ajax.request({
				url: '<%= request.getContextPath() + GeneralUtilities.getSpagoAdapterHttpUrl() %>?ACTION_NAME=GET_SNAPSHOTS_INFO&<%=SpagoBIConstants.OBJECT_ID%>=' + objId + '&<%= LightNavigationManager.LIGHT_NAVIGATOR_DISABLED %>=TRUE',
				method: 'get',
				success: function (result, request) {
					response = result.responseText || "";
					showSnapshotsOptions(response);
				},
				params: params,
				failure: somethingWentWrong
			});
		}
		
		function clearOptions(elementId) {
			subobjectSelect = document.getElementById(elementId);
			initialLenght = subobjectSelect.options.length;
			for (i = 0; i < initialLenght; i++) {
				subobjectSelect.remove(0);
			}
		}
		
		function showSubobjectsOptions(response) {
			clearOptions('subobjectName');
			subobjectSelect = document.getElementById('subobjectName');
			addEmptyOptionToSelect(subobjectSelect);
			optionsArray = response.split(';;');
			if (optionsArray.length == 1 && (optionsArray[0] == null || optionsArray[0] == '')) {
				document.getElementById('subobjectDiv').style.display = 'none';
			} else {
				document.getElementById('subobjectDiv').style.display = 'inline';
			}
			for (i = 0; i < optionsArray.length; i++) {
				option = optionsArray[i];
				if (option != null && option != '') {
					str = "subobject = " + option + ";";
					eval(str);
					subobjectOption = document.createElement('option');
					subobjectOption.value = subobject.name;
					subobjectOption.text = subobject.name;
					if (biobjectId == <%= menu.getObjId() %> && subobject.name == '<%= StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(menu.getSubObjName())) %>') {
						subobjectOption.selected = true;
					}
					if (subobject.description != null && subobject.description != '') {
						subobjectOption.text += ' (' + subobject.description + ')';
					}
					addOptionToSelect(subobjectOption, subobjectSelect);
				}
			}
		}
		
		function addEmptyOptionToSelect(selectObj) {
			emptyOption = document.createElement('option');
			emptyOption.value = '';
			emptyOption.text = '';
			addOptionToSelect(emptyOption, selectObj);
		}
		
		function addOptionToSelect(optionObj, selectObj) {
			try {
				selectObj.add(optionObj,null); // standards compliant
			} catch(ex) {
				selectObj.add(optionObj); // IE only
			}
		}
		
		snapshotsHistoriesHolder = new Array();
		
		function showSnapshotsOptions(response) {
			clearOptions('snapshotName');
			snapshotSelect = document.getElementById('snapshotName');
			addEmptyOptionToSelect(snapshotSelect);
			optionsArray = response.split(';;');
			if (optionsArray.length == 1 && (optionsArray[0] == null || optionsArray[0] == '')) {
				document.getElementById('snapshotDiv').style.display = 'none';
			} else {
				document.getElementById('snapshotDiv').style.display = 'inline';
			}
			for (i = 0; i < optionsArray.length; i++) {
				option = optionsArray[i];
				if (option != null && option != '') {
					str = "snapshot = " + option + ";";
					eval(str);
					snapshotOption = document.createElement('option');
					snapshotOption.value = snapshot.name;
					snapshotOption.text = snapshot.name;
					if (biobjectId == <%= menu.getObjId() %> && snapshot.name == '<%= StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(menu.getSnapshotName())) %>') {
						snapshotOption.selected = true;
					}
					if (snapshot.description != null && snapshot.description != '') {
						snapshotOption.text += ' (' + snapshot.description + ')';
					}
					addOptionToSelect(snapshotOption, snapshotSelect);
					historyLength = snapshot.historyLength;
					snapshotsHistoriesHolder[snapshot.name] = historyLength;
					if (snapshotOption.selected) {
						if (historyLength > 20) historyLength = 20;
						showSnapshotsHistoryOptions(historyLength, snapshot.name);
					}
				}
			}
		}
		
		function showSnapshotsHistoryOptions(historyLength, snapshotName) {
			clearOptions('snapshotHistory');
			snapshotHistorySelect = document.getElementById('snapshotHistory');
			addEmptyOptionToSelect(snapshotHistorySelect);
			for (j = 0; j < historyLength; j++) {
				historyOption = document.createElement('option');
				historyOption.value = j;
				historyOption.text = j;
				if (biobjectId == <%= menu.getObjId() %> && snapshotName == '<%= StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(menu.getSnapshotName())) %>' && j == <%= menu.getSnapshotHistory() %>) {
					historyOption.selected = true;
				}
				addOptionToSelect(historyOption, snapshotHistorySelect);
			}
		}
		
		function changeSnapshotHistoryOptions(snapshotName) {
			showSnapshotsHistoryOptions(snapshotsHistoriesHolder[snapshotName], snapshotName);
		}
		
		function somethingWentWrong() {
			alert('Something went wrong during ajax call');
		}
		
		</script>

		<%	
		String url=request.getContextPath() + GeneralUtilities.getSpagoAdapterHttpUrl() + "?PAGE=DocumentLookupPage&NEW_SESSION=TRUE&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
		boolean biobjectFound = true;
	 	String objId="";
		String objName="";	 
	 	if (menu.getObjId()!=null) {
			Integer id=menu.getObjId();	
			objId = id.toString();
			BIObject obj = null;
			try {
				obj=DAOFactory.getBIObjectDAO().loadBIObjectById(id);
				objName=obj.getName();
			} catch (EMFUserError error) {
				biobjectFound = false;
			}
		}
		%>

		<%
		// if the menu contains a document reference and this document exists, load document information (subobjects, snapshots)
		if (menu.getObjId() != null && biobjectFound) {
			%>
			<script>
			loadObjectDetails(<%= menu.getObjId() %>);
			</script>
			<%
		}
		%>


	<div id="nodeEmpty">
	</div>
	
	<div id="nodeDocument">
		<div class='div_detail_label'>	 
			 <span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.detailMenu.relatedDoc" />
			</span>
		</div>
		<div class='div_detail_form' id="documentForm" >
					 	<input type="hidden" name="menu_obj" id="menu_obj" value="<%=objId%>"/>	
													
						<input class='portlet-form-input-field' type="text" size="50" readonly="readonly" 
										name="documentReadLabel" id="documentReadLabel" value="<%=StringEscapeUtils.escapeHtml(objName)%>" maxlength="400" /> 
						<%
						if (!biobjectFound) {
							%>
							<img id="docNotFoundErrorImg" name="docNotFoundErrorImg" style="display:inline;"
								src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/error16.gif", currTheme) %>" 
								title="<spagobi:message key = "SBISet.detailMenu.relatedDocNotFound" />" 
								alt="<spagobi:message key = "SBISet.detailMenu.relatedDocNotFound" />" />
							<%
						}
						%>
						<a href='javascript:void(0);' id="documentLink" style="text-decoration:none;">
							<img src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme) %>" title="Lookup" alt="Lookup" />
						</a>
		</div>
				
			<script>
				var win_document;
				Ext.get('documentLink').on('click', function(){
					if(!win_document){
						win_document = new Ext.Window({
						id:'popup_document',
						title:'<spagobi:message key = "SBISet.detailMenu.relatedDoc" />',
						bodyCfg:{
							tag:'div', 
							cls:'x-panel-body', 
							children:[{tag:'iframe', 
										name: 'iframe_par_document',        			
										id  : 'iframe_par_document',        			
										src: '<%=url%>',   
										frameBorder:0,
										width:'100%',
										height:'100%',
										style: {overflow:'auto'}  
										}]
								},
							layout:'fit',
							width:800,
							height:320,
							closeAction:'hide',
							plain: true
							});
					};
					win_document.show();
				});
					
			</script>
		
		<%-- Document parameters --%> 
		<div class='div_detail_label'>	 
			 <span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.detailMenu.parameters" />
			</span>
		</div>
		<div class='div_detail_form'> 
			<input class='portlet-form-input-field' type="text" 
		      	   size="50" maxlength="400" name="objParameters" id="" 
		      	   value="<%= menu.getObjParameters() != null ? StringEscapeUtils.escapeHtml(menu.getObjParameters()) : "" %>"  />
		</div>
		<%-- End Document parameters --%> 
		
		<%-- Subobject --%>
		<div id="subobjectDiv" name="subobjectDiv" style="display: none"> 
			<div class='div_detail_label'>	 
				 <span class='portlet-form-field-label'>
					<spagobi:message key = "SBISet.detailMenu.subobject" />
				</span>
			</div>
			<div class='div_detail_form'>
				<select name="subobjectName" id="subobjectName" class='portlet-form-input-field'>
				</select>
			</div>
		</div>
		<%-- End Subobject --%> 
	
		 
		<div id="snapshotDiv" name="snapshotDiv" style="display: none"> 
			<%-- Snaphost name --%>
			<div class='div_detail_label'>	 
				 <span class='portlet-form-field-label'>
					<spagobi:message key = "SBISet.detailMenu.snapshotName" />
				</span>
			</div>
			<div class='div_detail_form'> 
				<select name="snapshotName" id="snapshotName" class='portlet-form-input-field' onchange="changeSnapshotHistoryOptions(this.value)">
				</select>
			</div>
			<%-- End Snaphost name --%> 
		
			<%-- Snaphost history --%> 
			<div class='div_detail_label'>	 
				 <span class='portlet-form-field-label'>
					<spagobi:message key = "SBISet.detailMenu.snapshotHistory" />
				</span>
			</div>
			<div class='div_detail_form'>
				<select name="snapshotHistory" id="snapshotHistory" class='portlet-form-input-field'>
				</select>
			</div>
			<%-- End Snaphost history --%>
		</div>
		
		<%-- Hide toolbar option --%>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.menu.hideToolbar" />
			</span>
		</div>
		<div class='div_detail_form'> 
			<input class='portlet-form-input-field' type="checkbox" onclick="hideSliderOption(this.checked);"
	      	   size="50" name="hideToolbar" id="" 
	      	   value="true" <%if(menu.getHideToolbar()){%> checked="checked" <%}%>/>
		</div>	
		<%-- End hide toolbar option --%>
		
		<%-- Hide sliders option --%>
		<div class='div_detail_label'>
			<span class='<%= menu.getHideToolbar() ? "portlet-form-field-label-disabled" : "portlet-form-field-label" %>'
					id='hideSlidersSpan' name='hideSlidersSpan' >
				<spagobi:message key = "SBISet.menu.hideSliders" />
			</span>
		</div>
		<div class='div_detail_form'> 
			<input class='portlet-form-input-field' type="checkbox" 
	      	   size="50" name="hideSliders" id="hideSlidersCheck" 
	      	   value="true" <%if(menu.getHideSliders()){%> checked="checked" <%}%> <%= menu.getHideToolbar() ? "disabled" : "" %> />
		</div>
		<script>
		function hideSliderOption(checked) {
			document.getElementById('hideSlidersCheck').disabled = checked;
			if (checked) {
				document.getElementById('hideSlidersSpan').className = 'portlet-form-field-label-disabled';
			} else {
				document.getElementById('hideSlidersSpan').className = 'portlet-form-field-label';
			}
		}
		</script>
		<%-- End hide sliders option --%>
		
	</div>
	
	
	<div id="nodeStaticPage">
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.menu.staticPage" />
			</span>
		</div>
		<div class='div_detail_form'> 
			<select name="staticpage" size="1" class='portlet-form-input-field'>
				<option value=""> </option>
					<%//Insert all options (only HTML files) 
					SingletonConfig configSingleton = SingletonConfig.getInstance();
					String path = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
					String resourcePath= SpagoBIUtilities.readJndiResource(path);
					resourcePath+="/static_menu";
					String currentStaticPage=menu.getStaticPage();
					File dir=new File(resourcePath);
					if (dir!=null && dir.isDirectory()){
						// get all avalaible files
						String[] files=dir.list();
						String selected="";
						for(int i=0;i<files.length;i++) {
							String fileName=files[i];
							String ext=fileName.substring(fileName.indexOf(".")+1);
							selected="";
							if(ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")){
								if(currentStaticPage!=null && fileName.equals(currentStaticPage)) selected="selected='selected'";
								%>
								<option value="<%=StringEscapeUtils.escapeHtml(fileName)%>" <%=selected%>><%=StringEscapeUtils.escapeHtml(fileName)%></option>
								<%
							} 
						}
					}

					%>
			</select>	
		</div>
	</div>
	
	<div id="nodeExternalApp">
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.menu.externalAppUrl" />
			</span>
		</div>
		<%
		String externalAppUrl = menu.getExternalApplicationUrl() != null ? menu.getExternalApplicationUrl() : "";
		%>
		<div class='div_detail_form'> 
			<input class='portlet-form-input-field' type="text" size="50" 
				name="EXT_APP_URL" id="EXT_APP_URL" value="<%=StringEscapeUtils.escapeHtml(externalAppUrl)%>" maxlength="1000" /> 
		</div>
	</div>
	 
	<div id="nodeFunctionality">
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.menu.functionality" />
			</span>
		</div>
		<div class='div_detail_form'> 
			<select name="functionality" size="1" class='portlet-form-input-field' onchange="changeInitialPathVisibility(this);">
				<option value=""> </option>
				<option value="<%= SpagoBIConstants.DOCUMENT_BROWSER_USER %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.DOCUMENT_BROWSER_USER)) ? "selected='selected'" : ""%>>
					<spagobi:message key="menu.Browser" />
				</option>
				<!--  
				<option value="<%= SpagoBIConstants.DOCUMENT_MANAGEMENT_USER %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)) ? "selected='selected'" : ""%>>
					<spagobi:message key="tree.objectstree.name" />
				-->	
				</option>
				<option value="<%= SpagoBIConstants.WORKLIST_MANAGEMENT %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.WORKLIST_MANAGEMENT)) ? "selected='selected'" : ""%>>
					<spagobi:message key="menu.Worklist" />
				</option>
				<option value="<%= SpagoBIConstants.HOTLINK_MANAGEMENT %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.HOTLINK_MANAGEMENT)) ? "selected='selected'" : ""%>>
					<spagobi:message key="menu.HotLink" />
				</option>
				<option value="<%= SpagoBIConstants.DISTRIBUTIONLIST_USER %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.DISTRIBUTIONLIST_USER)) ? "selected='selected'" : ""%>>
					<spagobi:message key="menu.DistributionListList" />
				</option>
				<option value="<%= SpagoBIConstants.EVENTS_MANAGEMENT %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.EVENTS_MANAGEMENT)) ? "selected='selected'" : ""%>>
					<spagobi:message key="menu.Events" />
				</option>
                <option value="<%= SpagoBIConstants.FINAL_USERS_MANAGEMENT %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.FINAL_USERS_MANAGEMENT)) ? "selected='selected'" : ""%>>
                    <spagobi:message key="menu.Users" />
                </option>
                <option value="<%= SpagoBIConstants.CREATE_WORKSHEET_FROM_DATASET_USER %>" <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.CREATE_WORKSHEET_FROM_DATASET_USER)) ? "selected='selected'" : ""%>>
                    <spagobi:message key="menu.WorksheetFromDataset" />
                </option>
			</select>
		</div>
		
		<script>
		function changeInitialPathVisibility(selectObj) {
			var initialPathDiv = document.getElementById('initialPathDiv');
			var selectedOption = selectObj.options[selectObj.selectedIndex];
			if (selectedOption.value == '<%= SpagoBIConstants.DOCUMENT_BROWSER_USER %>') { 
				initialPathDiv.style.display = 'inline';
			} else {
				initialPathDiv.style.display = 'none';
			}
		}
		</script>
		
		<%-- Documents tree initial path div --%>
		<div id="initialPathDiv" name="initialPathDiv" style="display: <%= (menu.getFunctionality() != null && menu.getFunctionality().equals(SpagoBIConstants.DOCUMENT_BROWSER_USER)) ? "inline" : "none" %>"> 
			<div class='div_detail_label'>	 
				 <span class='portlet-form-field-label'>
					<spagobi:message key = "SBISet.menu.initialPath" />
				</span>
			</div>
			<div class='div_detail_form'>
				<input class='portlet-form-input-field' type="text" size="50" readonly="readonly" onchange="checkForErrorImg();"
						name="initialPath" id="initialPath" value="<%= menu.getInitialPath() != null ? StringEscapeUtils.escapeHtml(menu.getInitialPath()) : "" %>" maxlength="400" />
				<a href='javascript:void(0);' id="initialPathLink" style="text-decoration:none;">
					<img src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme) %>" title="Lookup" alt="Lookup" />
				</a>
				<%
				ILowFunctionalityDAO functionalityDAO = DAOFactory.getLowFunctionalityDAO();
				List folders = functionalityDAO.loadAllLowFunctionalities(false);
				// if the menu has a initial path set, checks if it exists
				if (menu.getInitialPath() != null && !menu.getInitialPath().equals("")) {
					Iterator fodlersIter = folders.iterator();
					boolean pathFound = false;
					while (fodlersIter.hasNext()) {
						LowFunctionality folder = (LowFunctionality) fodlersIter.next();
						pathFound = folder.getPath().equals(menu.getInitialPath());
						if (pathFound) break;
					}
					if (!pathFound) {
						%>
						<img id="pathNotFoundErrorImg" name="pathNotFoundErrorImg" style="display:inline;"
							src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/error16.gif", currTheme) %>" 
							title="<spagobi:message key = "SBISet.detailMenu.relatedPathNotFound" />" 
							alt="<spagobi:message key = "SBISet.detailMenu.relatedPathNotFound" />" />
						<%
					}
				}
				%>
			</div>
		</div>
		
		<script>
		var win_initialPath;
		Ext.get('initialPathLink').on('click', function(){
			if(!win_initialPath){
				win_initialPath = new Ext.Window({
					id:'popup_initialPath',
					title:"<%= JavaScript.escapeText(msgBuilder.getMessage("SBISet.menu.selectInitialPath", "messages", request)) %>",
					contentEl: 'divInitialPathSelection',
					layout:'fit',
					width:500,
					height:350,
					closeAction:'hide',
					plain: true,
					autoScroll: true,
					maximizable: true,
					style: 'background-color: white;'
				});
			};
			win_initialPath.show();
		});
		</script>
		
		<script>
		function checkForErrorImg() {
			var errorPathImg = document.getElementById('pathNotFoundErrorImg');
			if (errorPathImg) errorPathImg.style.display = 'none';
		}
		</script>
		<%-- End documents tree initial path div --%>
		
	</div>

	<!-- Div for initial path selection -->
	<div style="display:none">
		<div id="divInitialPathSelection" name="divInitialPathSelection" style='background-color:white;'>
		</div>
		<LINK rel='StyleSheet' href='<%= urlBuilder.getResourceLinkByTheme(request, "/css/dtree.css", currTheme) %>' type='text/css' />
		<SCRIPT language='JavaScript' src='<%= urlBuilder.getResourceLink(request, "/js/dtree.js" ) %>'></SCRIPT>
		<%
		String nameTree = msgBuilder.getMessage("tree.functtree.name" ,"messages", request);
		%>
		<script language="JavaScript1.2">
	   	var nameTree = 'treeFunct';
	   	treeFunct = new dTree('treeFunct', '<%= request.getContextPath() %>');
	   	treeFunct.config.useSelection = false;
	   	treeFunct.add(-100,-1,'<%= StringEscapeUtils.escapeJavaScript(nameTree) %>');
	   	<%
	   	Iterator fodlersIter = folders.iterator();
	   	String imgFolder = urlBuilder.getResourceLinkByTheme(request, "/img/treefolder.gif", currTheme);
		String imgFolderOp = urlBuilder.getResourceLinkByTheme(request, "/img/treefolderopen.gif", currTheme);
	   	while (fodlersIter.hasNext()) {
			LowFunctionality folder = (LowFunctionality) fodlersIter.next();
	   		boolean isUserFunct = folder.getCodType().equals("USER_FUNCT");
	   		if (isUserFunct) {
	   			continue;
	   		}
			boolean isRoot = (folder.getParentId() == null || folder.getParentId().intValue()==0);
			String nameLabel = folder.getName();
			String name = msgBuilder.getMessage(nameLabel, "messages", request);
			String folderId="";
			if (folder!=null && folder.getId()!=null){
				folderId=folder.getId().toString();
			}
			String folderIdParent="";
			if (folder!=null && folder.getParentId()!=null){
				folderIdParent=folder.getParentId().toString();
			}			
			%>
			treeFunct.add(<%=folderId%>, <%= isRoot ? "-100" : folderIdParent %>,"<%= JavaScript.escapeText(name) %>", 
				'javascript:setInitialpath(\'<%= folder.getPath() %>\')', '', '', '<%=imgFolder %>', '<%=imgFolderOp %>', '', '');
			<%
	   	}
	   	%>
	   	document.getElementById('divInitialPathSelection').innerHTML = treeFunct;
	   	
	   	function setInitialpath(initialPath) {
	   		document.getElementById('initialPath').value = initialPath;
	   		win_initialPath.hide();
	   	}
	   	
	   	function linkEmpty() {
	   	}
		</script>
	</div>
	<!-- End div for initial path selection -->

<spagobi:error/>


<% 
	// Get the roloes
	Integer id = menu.getMenuId();
	Role[] menuRolesObj = menu.getRoles();
	int iLength = menuRolesObj.length;
	String[] menuRoles = new String[iLength];
	for(int i=0; i<menuRolesObj.length; i++) {
		menuRoles[i] = menuRolesObj[i].getId().toString();
	}

%>	
	
	<div class="div_functions_role_associations">
 		<table>
 				<tr>
 					<td class='portlet-section-header' align="left">
						<spagobi:message key = "SBISet.detailMenu.tabCol1" />
					</td>
 					<td class='portlet-section-header' align="center" width="90px">
						<spagobi:message key = "SBISet.detailMenu.tabCol2" />
					</td>				
 				</tr>
 			     <% 
 			     	MenuDAOImpl menuDao = (MenuDAOImpl) DAOFactory.getMenuDAO();
 			     	for(int i=0; i < sysRoles.length; i++) {   // for all the possible roles
 			            String roleId = sysRoles[i][0];
 			            String roleName = sysRoles[i][1];
 			            String roleDescription = sysRoles[i][2];
 			            boolean isAssociatedToCurrentMenu = false;		
						boolean isAssociatedToParentMenu = false;
						
 			            for (int j=0; j<menuRoles.length; j++) {   // set if role in iteration is one of menu's
 			               if(menuRoles[j].equals(roleId)) { isAssociatedToCurrentMenu = true; break; }
 			            }
	               		if (parentMenu != null) {
	               			// check if the parent menu node is associated to the current role
	               			Role[] parentRoles = parentMenu.getRoles();
	               			for (int c = 0; c < parentRoles.length; c++) {
	               				Role aParentRole = parentRoles[c];
	               				if (aParentRole.getId().toString().equals(roleId)) {
	               					isAssociatedToParentMenu = true;
	               					break;
	               				}
	               			}
		               	} else {
		               		// the current menu node has no parent node
		               		isAssociatedToParentMenu = true;
		               	}
 			            %>
 			            
				 <tr onMouseOver="this.bgColor='#F5F6BE'" onMouseOut="this.bgColor='#FFFFFF'">
				 	<td nowrap class='portlet-font'><%= roleName + " (" + roleDescription + ")" %></td>
				 	
				 	<td align="center">
				 	    <input type="checkbox" name="ROLES" id="ROLES" value="<%=roleId%>" 
				 	    	<%
				 	    	if (modality.equals(AdmintoolsConstants.DETAIL_INS)){
				 	    		// when inserting a new node in the menu, by default it is associated to all parent node's roles
				 	    		if (isAssociatedToCurrentMenu)	out.print(" checked='checked' ");
				 	    		else out.print(" disabled='disabled' ");				
				 	    	} else if(modality.equals(AdmintoolsConstants.DETAIL_MOD)) {
				 	    		if (isAssociatedToCurrentMenu) {
				 	    			out.print(" checked='checked' ");
				 	    		} else {
									if (!isAssociatedToParentMenu) {
										out.print(" disabled='disabled' "); 
									}
				 	    		}
				 	    	}
				 	    	%> 
				 	    />
				 	</td>
				
				 </tr>	
                    <% } %>
                    <tr>
                       <td align="center">&nbsp;</td>       
                       <td align="center">
                       	<a onclick = "selectAllInColumns('ROLES')" title='<spagobi:message key = "SBISet.detailMenu.selAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.selAllColumn" />'>
                       		<img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
                       	</a>
				    	<a onclick = "deselectAllInColumns('ROLES')" title='<spagobi:message key = "SBISet.detailMenu.deselAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.deselAllColumn" />'>
				    		<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>' />
				    	</a>
				    </td>
                    </tr>
 		</table>
</div>

		
</form>
    


</div>    
    

<script>
function selectAllInColumns (columnName){
var checkCollection = document.forms.formFunct.elements[columnName];
for (var i = 0; i< checkCollection.length; i++){
if(!checkCollection[i].checked){
checkCollection[i].click();
}
}
}

function deselectAllInColumns (columnName){
var checkCollection = document.forms.formFunct.elements[columnName];
for (var i = 0; i< checkCollection.length; i++){
if(checkCollection[i].checked){
checkCollection[i].click();
}
}
}



</script>
