<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
  


<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.Title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('execRolesForm<%=requestIdentity%>').submit()"> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.execButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/exec.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.execButt" />' /> 
			</a>
		</td>
		<%
			if (modality == null || !modality.equalsIgnoreCase(SpagoBIConstants.SINGLE_OBJECT_EXECUTION_MODALITY)) {
				// build the back url 
				
				Map backUrlPars = new HashMap();
			    backUrlPars.put("PAGE", "BIObjectsPage");
			    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
			    String backUrl = urlBuilder.getUrl(request, backUrlPars);

				%>
				<td class='header-button-column-portlet-section'>
					<a href='<%= backUrl %>'> 
						<img class='header-button-image-portlet-section' 
							title='<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.backButt" />' 
							src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
							alt='<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.backButt" />' />
					</a>
				</td>
				<%
			}
		%>
	</tr>
</table>

<div class='div_background_no_img' >

 	<div class="div_detail_area_forms">
	 	<div class='div_detail_label'>
	 		<span class='portlet-form-field-label'>
	 			<spagobi:message key = "SBIDev.docConf.execBIObject.selRoles.roleField" />:
	 		</span>
	 	</div>
	 	<div class='div_detail_form'> 
	 	    <select class='portlet-form-field' name="<%= SpagoBIConstants.ROLE %>"  style="width:200px;">
	 	    <% 
	 	       while(iterroles.hasNext()) {
	 	       	String role = (String)iterroles.next(); 
	 	    %> 
	 		 	<option value="<%= role %>"><%= role %></option>	
	 	    <% 
	 	    	}
	 	    %>
	 	    </select> 
	 	</div>
	 </div>	
	 		
</div>

</form>
