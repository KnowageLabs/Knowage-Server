<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.commons.constants.AdmintoolsConstants,
                 it.eng.spagobi.analiticalmodel.functionalitytree.service.DetailFunctionalityModule,
                 javax.portlet.PortletURL,
                 java.util.List,
                 it.eng.spagobi.commons.bo.Role,
                 it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality,
                 it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule,
                 it.eng.spagobi.commons.constants.SpagoBIConstants,
                 it.eng.spagobi.commons.dao.DAOFactory,
                 it.eng.spago.navigation.LightNavigationManager,
                 it.eng.spagobi.analiticalmodel.document.service.BIObjectsModule" %>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>

<% 
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailFunctionalityModule"); 
    LowFunctionality functionality = (LowFunctionality)moduleResponse.getAttribute(DetailFunctionalityModule.FUNCTIONALITY_OBJ);
	String modality = (String)moduleResponse.getAttribute(AdmintoolsConstants.MODALITY);
	String pathParent = (String) moduleResponse.getAttribute(AdmintoolsConstants.PATH_PARENT);
    LowFunctionality parentFunctionality = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(pathParent, false);
    
	
    Map formUrlPars = new HashMap();
	formUrlPars.put(AdmintoolsConstants.PAGE, DetailFunctionalityModule.MODULE_PAGE);
	formUrlPars.put(AdmintoolsConstants.MESSAGE_DETAIL, modality);
	if (modality.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
		formUrlPars.put(AdmintoolsConstants.PATH_PARENT, pathParent);
    } else {
    	formUrlPars.put(AdmintoolsConstants.FUNCTIONALITY_ID, functionality.getId().toString());
    }
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String formAct = urlBuilder.getUrl(request, formUrlPars);

    
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", BIObjectsModule.MODULE_PAGE);
    backUrlPars.put(SpagoBIConstants.OPERATION, SpagoBIConstants.FUNCTIONALITIES_OPERATION);
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);

    
    List roles = DAOFactory.getRoleDAO().loadAllRoles();
    String[][] sysRoles = new String[roles.size()][3];
    for(int i=0; i<roles.size(); i++) {
    	Role role = (Role)roles.get(i);
    	sysRoles[i][0] = role.getId().toString();
    	sysRoles[i][1] = role.getName();
    	sysRoles[i][2] = role.getDescription();
    	
    }
%>





<form action="<%=formAct%>" method="post" id='formFunct' name = 'formFunct'>

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.Funct.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('formFunct').submit()"> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBISet.Funct.saveButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBISet.Funct.saveButt" />' />
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message 
      			     key = "SBISet.Funct.backButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBISet.Funct.backButt" />'/>
			</a>
		</td>
	</tr>
</table>



<div class='div_background_no_img' style='padding-top:5px;padding-left:5px;' >


    
<div class="div_detail_area_forms">
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.Funct.codefield" />
		</span>
	</div>
	<div class='div_detail_form'>
	<% 
	  String code = functionality.getCode();
	  if((code==null) || (code.equalsIgnoreCase("null"))  ) {
	  	code = "";
	  }
    %>
    	<input class='portlet-form-input-field' type="text" 
	      	   size="50" name="code" id="" value="<%= StringEscapeUtils.escapeHtml(code) %>" />
	    &nbsp;* 
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.Funct.nameField" />
		</span>
	</div>
	<div class='div_detail_form'> 
		<input class='portlet-form-input-field' type="text" 
	      	   size="50" name="name" id="" 
	      	   value="<%= StringEscapeUtils.escapeHtml(functionality.getName()) %>"  />
	   	&nbsp;*	
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.Funct.descriptionField" />
		</span>
	</div>
	<div class='div_detail_form'> 
	<% 
      String desc = functionality.getDescription();
      if( (desc==null) || (desc.equalsIgnoreCase("null"))  ) {
      	desc = "";
      } 
     %>
		<input class='portlet-form-input-field' type="text" 
               size="50" name="description" id="" value="<%= StringEscapeUtils.escapeHtml(desc) %>" />
	</div>
</div>



<spagobi:error/>

	
<% if(functionality.getCodType().equalsIgnoreCase("LOW_FUNCT")) { 
		LowFunctionality lowFunctionality = (LowFunctionality)functionality;
		String path = lowFunctionality.getPath();
		Role[] devRolesObj = lowFunctionality.getDevRoles();
		String[] devRules = new String[devRolesObj.length];
		for(int i=0; i<devRolesObj.length; i++) {
			devRules[i] = devRolesObj[i].getId().toString();
		}
		Role[] execRolesObj = lowFunctionality.getExecRoles();
		String[] execRules = new String[execRolesObj.length];
		for(int i=0; i<execRolesObj.length; i++) {
			execRules[i] = execRolesObj[i].getId().toString();
		}
		Role[] testRolesObj = lowFunctionality.getTestRoles();
		String[] testRules = new String[testRolesObj.length];
		for(int i=0; i<testRolesObj.length; i++) {
			testRules[i] = testRolesObj[i].getId().toString();
		}
		Role[] createRolesObj = lowFunctionality.getCreateRoles();
		String[] createRules = new String[createRolesObj.length];
		for(int i=0; i<createRolesObj.length; i++) {
			createRules[i] = createRolesObj[i].getId().toString();
		}
%>	
	
<div class="div_functions_role_associations">
	 		<table>
	 				<tr>
	 					<td class='portlet-section-header' align="left">
							<spagobi:message key = "SBISet.Funct.tabCol1" />
						</td>
	 					<td class='portlet-section-header' align="center" width="90px">
							<spagobi:message key = "SBISet.Funct.tabCol2" />
						</td>
	 					<td class='portlet-section-header' align="center" width="90px">
                            <spagobi:message key = "SBISet.Funct.tabCol3" />
                        </td>
	 					<td class='portlet-section-header' align="center" width="90px">
                            <spagobi:message key = "SBISet.Funct.tabCol4" />
                        </td>
	 					<td class='portlet-section-header' align="center" width="90px">
                            <spagobi:message key = "SBISet.Funct.tabCol5" />
                        </td>
	                    <td class='portlet-section-header' align="center" width="90px">
                        	&nbsp;
                        </td> 				
	 				</tr>
	 			     <% 
	 			    	boolean alternate = false;	
	 			     	String rowClass = null;
	 			     	for(int i=0; i<sysRoles.length; i++) { 
	 			            String ruleId = sysRoles[i][0];
	 			            String ruleName = sysRoles[i][1];
	 			            String ruleDescription = sysRoles[i][2];
	 			            boolean isDev = false;
	 			            boolean isTest = false;
	 			            boolean isExec = false;
	 			            boolean isCreate = false;
	 			            boolean isDevParent = false;
	 			            boolean isTestParent = false;
	 			            boolean isExecParent = false;
	 			            boolean isCreateParent = false;
	 			            DetailFunctionalityModule detFunct = new DetailFunctionalityModule();
	 			            for(int j=0; j<devRules.length; j++) {
	 			               if(devRules[j].equals(ruleId)) {
	 			            	   isDev = true; 
	 			               }
	 			            }
		               		if(!modality.equals(AdmintoolsConstants.DETAIL_INS)){
		               			if(detFunct.isParentRule(ruleId,parentFunctionality,SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)){
		               				isDevParent = true;
		               			}
							}
									 			            
	 			            for(int j=0; j<testRules.length; j++) {
	 			               if(testRules[j].equals(ruleId)) {
	 			            	   isTest = true; 
	 			               }
	 			            }
		               		if(!modality.equals(AdmintoolsConstants.DETAIL_INS)){
		               			if(detFunct.isParentRule(ruleId,parentFunctionality,SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)){
		               				isTestParent = true;
		               			}
		            		}
	 			            		
	 			            for(int j=0; j<execRules.length; j++) {
	 			               if(execRules[j].equals(ruleId)) {
	 			            	   isExec = true;
	 			               }
	 			            }
		               		if(!modality.equals(AdmintoolsConstants.DETAIL_INS)){
		               			if(detFunct.isParentRule(ruleId,parentFunctionality,SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)){
		               				isExecParent = true;
		               			}
		           			}
		               		
	 			            for(int j=0; j<createRules.length; j++) {
	 			               if(createRules[j].equals(ruleId)) {
	 			            	   isCreate = true;
	 			               }
	 			            }
		               		if(!modality.equals(AdmintoolsConstants.DETAIL_INS)){
		               			if(detFunct.isParentRule(ruleId,parentFunctionality,SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)){
		               				isCreateParent = true;
		               			}
		           			}
	 			            		
	 			            rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
	 			            alternate = !alternate;
	 			            %>
	 			            
					 <tr onMouseOver="this.bgColor='#F5F6BE'" onMouseOut="this.bgColor='#FFFFFF'">
					 	<td class='portlet-font'><%= ruleName + " (" + ruleDescription + ")" %></td>
					 	
					 	<td align="center">
					 	    <input type="checkbox" name="development" id="development" value="<%=ruleId%>" 
					 	    	<%
					 	    		if(isDev) out.print(" checked='checked' ");
					 	    		else if (!isDevParent && parentFunctionality.getParentId() != null) out.print(" disabled='disabled' ");
					 	    	%> 
					 	    />
					 	</td>
					 	<td align="center">
					 	    <input type="checkbox" name="test" id="test" value="<%=ruleId%>" 
					 	    <%
					 	    	if(isTest) out.print(" checked='checked' "); 
					 	    	else if (!isTestParent && parentFunctionality.getParentId() != null) out.print(" disabled='disabled' ");
					 	    %> 
					 	    />
					 	</td>
					 	<td align="center">
					 	    <input type="checkbox" name="execution" id="execution" value="<%=ruleId%>" 
					 	    <%
					 	    	if(isExec) out.print(" checked='checked' ");
					 	    	else if (!isExecParent && parentFunctionality.getParentId() != null) out.print(" disabled='disabled' "); 
					 	    %> 
					 	    />
					 	</td>
					 	<td align="center">
					 	    <input type="checkbox" name="creation" id="creation" value="<%=ruleId%>" 
					 	    <%
					 	    	if(isCreate) out.print(" checked='checked' ");
					 	    	else if (!isCreateParent && parentFunctionality.getParentId() != null) out.print(" disabled='disabled' "); 
					 	    %> 
					 	    />
					 	</td> 
					    <td>
					    <a onclick = "selectAllInRows('<%=ruleId%>')" 
					       title='<spagobi:message key = "SBISet.Funct.selAllRow" />' 
					       alt='<spagobi:message key = "SBISet.Funct.selAllRow" />'>
					    	<img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
					    </a>
					    <a onclick = "deselectAllInRows('<%=ruleId%>')" title='<spagobi:message key = "SBISet.Funct.deselAllRow" />' alt='<spagobi:message key = "SBISet.Funct.deselAllRow" />'>
					    	<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>'/>
					    </a>
					    </td> 
					 </tr>	
                     <% } %>
                     <tr class='<%=rowClass%>'>
                        <td align="center">&nbsp;</td>       
                        <td align="center">
                        <a onclick = "selectAllInColumns('development')" title='<spagobi:message key = "SBISet.Funct.selAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.selAllColumn" />'>
                        <img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
                        </a>
					    <a onclick = "deselectAllInColumns('development')" title='<spagobi:message key = "SBISet.Funct.deselAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.deselAllColumn" />'>
					    <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>' />
					    </a>
					    </td>
					    <td align="center">
                        <a onclick = "selectAllInColumns('test')" title='<spagobi:message key = "SBISet.Funct.selAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.selAllColumn" />'>
                        <img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
                        </a>
					    <a onclick = "deselectAllInColumns('test')" title='<spagobi:message key = "SBISet.Funct.deselAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.deselAllColumn" />'>
					    <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>'/>
					    </a>
					    </td>
					    <td align="center">
                        <a onclick = "selectAllInColumns('execution')" title='<spagobi:message key = "SBISet.Funct.selAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.selAllColumn" />'>
                        <img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
                        </a>
					    <a onclick = "deselectAllInColumns('execution')" title='<spagobi:message key = "SBISet.Funct.deselAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.deselAllColumn" />'>
					    <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>'/>
					    </a>
					    </td>
					    <td align="center">
                        <a onclick = "selectAllInColumns('creation')" title='<spagobi:message key = "SBISet.Funct.selAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.selAllColumn" />'>
                        <img  src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme)%>'/>
                        </a>
					    <a onclick = "deselectAllInColumns('creation')" title='<spagobi:message key = "SBISet.Funct.deselAllColumn" />' alt='<spagobi:message key = "SBISet.Funct.deselAllColumn" />'>
					    <img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.png", currTheme)%>'/>
					    </a>
					    </td>
						<td align="center">&nbsp;</td>   
                     </tr>
	 		</table>
</div>

<% } %>	 		
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
function selectAllInRows (rowId){
	var checkDevCollection = document.forms.formFunct.elements['development'];
	var checkTestCollection = document.forms.formFunct.elements['test'];
	var checkExecCollection = document.forms.formFunct.elements['execution'];
	var checkCreateCollection = document.forms.formFunct.elements['creation'];
	
	for(var i=0; i<checkDevCollection.length; i++){
		if(checkDevCollection[i].value == rowId && !checkDevCollection[i].checked){
			checkDevCollection[i].click();
		}
	}
	for(var j=0; j<checkTestCollection.length; j++){
		if(checkTestCollection[j].value == rowId && !checkTestCollection[j].checked){
			checkTestCollection[j].click();
		}
	}
	for(var k=0; k<checkExecCollection.length; k++){
		if(checkExecCollection[k].value == rowId && !checkExecCollection[k].checked){
			checkExecCollection[k].click();
		}
	}
	for(var l=0; l<checkCreateCollection.length; l++){
		if(checkCreateCollection[l].value == rowId && !checkCreateCollection[l].checked){
			checkCreateCollection[l].click();
		}
	}
}

function deselectAllInRows (rowId){
	var checkDevCollection = document.forms.formFunct.elements['development'];
	var checkTestCollection = document.forms.formFunct.elements['test'];
	var checkExecCollection = document.forms.formFunct.elements['execution'];
	var checkCreateCollection = document.forms.formFunct.elements['creation'];
	
	for(var i=0; i<checkDevCollection.length; i++){
		if(checkDevCollection[i].value == rowId && checkDevCollection[i].checked){
			checkDevCollection[i].click();
		}
	}
	for(var j=0; j<checkTestCollection.length; j++){
		if(checkTestCollection[j].value == rowId && checkTestCollection[j].checked){
			checkTestCollection[j].click();
		}
	}
	for(var k=0; k<checkExecCollection.length; k++){
		if(checkExecCollection[k].value == rowId && checkExecCollection[k].checked){
			checkExecCollection[k].click();
		}
	}
	for(var l=0; l<checkCreateCollection.length; l++){
		if(checkCreateCollection[l].value == rowId && checkCreateCollection[l].checked){
			checkCreateCollection[l].click();
		}
	}
}
</script>