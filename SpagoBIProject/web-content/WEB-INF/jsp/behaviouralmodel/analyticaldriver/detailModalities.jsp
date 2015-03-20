<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 



<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter,
				 it.eng.spagobi.commons.constants.AdmintoolsConstants,
                 it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue,
                 it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO,
                 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter,
                 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse,
                 it.eng.spagobi.commons.bo.Role,
                 java.util.ArrayList,
                 java.util.List,
                 javax.portlet.PortletURL,
                 it.eng.spagobi.commons.dao.IDomainDAO,
       			 it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO,
                 it.eng.spagobi.behaviouralmodel.check.bo.Check,
                 java.util.Iterator,
                 it.eng.spagobi.commons.bo.Domain,
                 it.eng.spagobi.commons.dao.IRoleDAO,
                 it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO,
				 it.eng.spagobi.commons.dao.DAOFactory,
				 it.eng.spago.navigation.LightNavigationManager" %>
                 
<%
    SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailModalitiesModule"); 
	ParameterUse param = (ParameterUse)moduleResponse.getAttribute("modalitiesObj");
	String modality = (String)moduleResponse.getAttribute("modality");
	String parId = (String)aServiceRequest.getAttribute("PAR_ID");
	String loadFromLookup = (String)moduleResponse.getAttribute("LOAD_FROM_LOOKUP");
	if(loadFromLookup != null){
	parId = (String) (String)moduleResponse.getAttribute("PAR_ID");
	}
%>
<% 
   Integer parIdInt = new Integer(parId);
   Parameter paramDetail = new Parameter();
   paramDetail = DAOFactory.getParameterDAO().loadForDetailByParameterID(parIdInt);
   String paramLabel = paramDetail.getLabel();
   String paramName = paramDetail.getName();
   String paramDescription = paramDetail.getDescription();
   String paramType = paramDetail.getType();
	  String readonly = "readonly" ;
	    boolean isreadonly = true;
	   if (userProfile.isAbleToExecuteAction(SpagoBIConstants.PARAMETER_MANAGEMENT)){
	   	isreadonly = false;
	   	readonly = "";
	   }
   
%>
<% 
   PortletURL formUrl = renderResponse.createActionURL();
   formUrl.setParameter("PAGE", "DetailModalitiesPage");
   formUrl.setParameter("MESSAGEDET", modality);
   formUrl.setParameter(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   
   PortletURL backUrl = renderResponse.createActionURL();
   backUrl.setParameter("PAGE", "ListParameterUsesPage");
   backUrl.setParameter("ID_DOMAIN", param.getId().toString());
   backUrl.setParameter(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
   
   IRoleDAO rDao = DAOFactory.getRoleDAO();
   
   List roles = null;
   if(modality.equals(AdmintoolsConstants.DETAIL_MOD)) {
   	roles = rDao.loadAllFreeRolesForDetail(param.getUseID());
   }
   if(modality.equals(AdmintoolsConstants.DETAIL_INS)) {
   	roles = rDao.loadAllFreeRolesForInsert(new Integer(parId));
   }
   String[][] sysRoles = new String[roles.size()][3];
   for(int i=0; i<roles.size(); i++) {
    	Role role = (Role)roles.get(i);
    	sysRoles[i][0] = role.getId().toString();
    	sysRoles[i][1] = role.getName();
    	sysRoles[i][2] = role.getDescription();
    } 
   
   // get all possible roles
   List allRoles = null;
   allRoles = rDao.loadAllRoles();
   String[][] allSysRoles = new String[allRoles.size()][3];
   for(int i=0; i<allRoles.size(); i++) {
    	Role role = (Role)allRoles.get(i);
    	allSysRoles[i][0] = role.getId().toString();
    	allSysRoles[i][1] = role.getName();
    	allSysRoles[i][2] = role.getDescription();
    } 
   
   // get system checks
   ICheckDAO checkdao = DAOFactory.getChecksDAO();
   List checks = checkdao.loadAllChecks();
   String[][] sysChecks = new String[checks.size()][3];
   for(int i=0; i<checks.size(); i++) {
    	Check check = (Check)checks.get(i);
    	sysChecks[i][0]= check.getCheckId().toString();
    	sysChecks[i][1] = check.getName();
    	sysChecks[i][2] = check.getDescription();
    } 
    
   // list of modalityValues
   IModalitiesValueDAO aModalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
   List allModalitiesValues = aModalitiesValueDAO.loadAllModalitiesValueOrderByCode();  
   
   IDomainDAO domaindao = DAOFactory.getDomainDAO() ;
   List typeLov = domaindao.loadListDomainsByType("INPUT_TYPE");
   String visibleColumns = ("LABEL,NAME,DESCRIPTION,TYPE");

%>    

<table width="100%"  style="margin-top:3px; margin-left:3px; margin-right:3px; margin-bottom:5px;">
  	<tr height='1'>
  		<td width="23%"></td>
  		<td style="width:3px;"></td>
  		<td width="12%"></td>
  		<td width="15%"></td>
  		<td width="15%"></td>
  		<td width="35%"></td>
  	</tr>
  	<tr height = "20">
  		<td class='portlet-section-subheader' style='text-align:center;vertical-align:bottom;'>
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo1" />
  		</td>
  		<td style="width:3px;"></td>
  		<td class='portlet-section-body' style='border-top: 1px solid #CCCCCC;'>
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo.Label"/>: 
  		</td>
  		<td class='portlet-section-alternate' style='border-top: 1px solid #CCCCCC;'>
  			<%=StringEscapeUtils.escapeHtml(paramLabel) %>
  		</td>
  		<td class='portlet-section-body' style='border-top: 1px solid #CCCCCC;'>
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo.Name"/>: 
  		</td>
  		<td class='portlet-section-alternate' style='border-top: 1px solid #CCCCCC;'>
  			<%=StringEscapeUtils.escapeHtml(paramName) %>
  		</td>
  	</tr>
  	<tr height = "20">
  		<td class='portlet-section-subheader' style='text-align:center;vertical-align:top;'>
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo2" />
  		</td>
  		<td style="width:3px;"></td>
  		<td class='portlet-section-body' >
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo.Type"/>: 
  		</td>
  		<td class = 'portlet-section-alternate'>
  			<%=StringEscapeUtils.escapeHtml(paramType) %>
  		</td>
  		<td class='portlet-section-body'>
  			<spagobi:message key = "SBIDev.ListParamUse.parInfo.Description"/>: 
  		</td>
  		<td class = 'portlet-section-alternate'>
  			<%=StringEscapeUtils.escapeHtml(paramDescription) %>
  		</td>
  	</tr>
</table>

<form method='POST' action='<%= formUrl.toString() %>' id ='modalitiesForm' name='modalitiesForm'>

<%--table width='100%' cellspacing='0' border='0'>		
	<tr height='40'>
		<th align='center'><spagobi:message key = "SBIDev.paramUse.title" /></th>
	</tr>
</table--%>


<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'>
			<spagobi:message key = "SBIDev.paramUse.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('modalitiesForm').submit()"> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIDev.paramUse.saveButt" />' src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' alt='<spagobi:message key = "SBIDev.paramUse.saveButt" />' /> 
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl.toString() %>'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIDev.paramUse.backButt" />' src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' alt='<spagobi:message key = "SBIDev.paramUse.backButt" />' />
			</a>
		</td>
	</tr>
</table>

<input type='hidden' value='<%= (param!=null ? String.valueOf(param.getUseID()) : "") %>' name='useId' />
<% if (parId != null){ %>
<input type='hidden' value='<%=parId %>' name='par_id' />
<% } %>
<input type='hidden' value='<%=typeLov %>' name='lov_list' />
<input type='hidden' value='<%=visibleColumns %>' name='visibleColumns' />

<table width="100%" cellspacing="0" border="0" >
  	<tr height='1'>
  		<td width="1px"><span>&nbsp;</span></td>
  		<td width="7%"><span>&nbsp;</span></td>
  		<td width="20px"><span>&nbsp;</span></td>
  		<td><span>&nbsp;</span></td>
  	</tr>
    <tr height='25'>
      	<td>&nbsp;</td>
      	<td align='right' class='portlet-form-field-label' ><spagobi:message key = "SBIDev.paramUse.labelField" /></td>
      	<td>&nbsp;</td>
      	<td><input class='portlet-form-input-field' type="text" <%=readonly%> name="label" size="50" value="<%=StringEscapeUtils.escapeHtml(param.getLabel())%>" maxlength="20">&nbsp;*</td>
    </tr>
    <tr height='25'>
      	<td>&nbsp;</td>
      	<td align='right' class='portlet-form-field-label' ><spagobi:message key = "SBIDev.paramUse.nameField" /></td>
      	<td>&nbsp;</td>
      	<td><input class='portlet-form-input-field' <%=readonly%> type="text" name="name" size="50" value="<%=StringEscapeUtils.escapeHtml(param.getName())%>" maxlength="40">&nbsp;*</td>
    </tr>
    <tr height='25'>
      	<td>&nbsp;</td>
      	<td align='right' class='portlet-form-field-label'><spagobi:message key = "SBIDev.paramUse.descriptionField" /></td>
      	<td>&nbsp;</td>
      	<%
      		String desc = param.getDescription();
      		if(desc==null) {
      			desc = "";
      		} 
       	%>
      	<td ><input class='portlet-form-input-field' <%=readonly%> type="text" name="description" size="50" value="<%=StringEscapeUtils.escapeHtml(desc)%>" maxlength="160"></td>
    </tr>
    <tr height='25'>
    <td>&nbsp;</td>
  				<td align='right' class='portlet-form-field-label' >
  				<spagobi:message key = "SBIDev.ListParamUse.parInfo.Name"/>
  				</td>
  				<td>&nbsp;</td>
  				<%String lovName = null;
  				  Integer idLov = null;
  				  idLov = param.getIdLov();
  				  Integer idLovInit = new Integer(-1);
  				  if(!idLov.toString().equals(idLovInit.toString())){
  				  ModalitiesValue modVal  = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByID(idLov);
  				  lovName = modVal.getName();
  				  }
  				 %>
  				<td><input class='portlet-form-input-field' <%=readonly%> type="text" name="lovName" size="50" value="<%= lovName != null ? StringEscapeUtils.escapeHtml(lovName) : "" %>" maxlength="100" readonly>
  				<input type='hidden' <%=readonly%> value='<%=idLov.intValue() != -1 ? idLov.toString() : "" %>' name='idLov' />
  				
  				<%PortletURL lovLookupURL = renderResponse.createActionURL();
  				lovLookupURL.setParameter("PAGE", "lovLookupPage"); 
  				
  				//lovLookupURL.setParameter("LOV_LIST",typeLov); %>
  				
  				&nbsp;<input type='image' <%=readonly%> name="loadLovLookup" value="LovLookup" 
				src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme)%>' 
				title='Lov Lookup'  
				alt='Lov Lookup' 
		        />
  				</td>
  				</tr>
</table>

<br/>
<table width="100%" cellspacing="0" border="1" >
  	<tr height='1'>
  		<%-- <td><span>&nbsp; </span></td> 
  		--%>
  		<td>
  		
  	    	<table width="100%">
  	    		<tr >
  	    			<td colspan="3" align="left" class='portlet-section-header'>
  	    				<spagobi:message key = "SBIDev.paramUse.valTab3" />
  	    			</td>
  	    		</tr>
  	    		<% 
  	    		    List roleAssociated = param.getAssociatedRoles();
  	    		    int count = 1;
  	    		    for(int i=0; i<allSysRoles.length; i++) { 
                        if(count==1) {
                          out.print("<tr class='portlet-font'>");
                        }
                        boolean isRole = false;
                        boolean isFree = false;
                        String roleId = allSysRoles[i][0].toString();
                        if (roleAssociated != null){
                        	Role tmpRoleAssociated = null;
                        	for(int j=0; j<roleAssociated.size(); j++) {
                        		tmpRoleAssociated = (Role)roleAssociated.get(j);
                   		    	if(roleId.equals(tmpRoleAssociated.getId().toString())) 
                   			   		isRole = true; 
                   			}
                   		}		    
  	    		 		for (int k=0; k<sysRoles.length;k++){
  	    		 			String id = sysRoles[k][0].toString();
  	    		 			if (id.equals(roleId)){
  	    		 				isFree = true;
  	    		 			}
  	    		 		}
  	    		 		out.print("<td class='portlet-section-body'>");
  	    		 		out.print("   <input type='checkbox' name='idExtRole' "+readonly+" value='"+roleId+"' ");
  	    		 		if(isRole) {
  	    		 			out.print(" checked='checked' ");
  	    		 		}
  	    		 		if(!isFree) {
  	    		 			out.print(" disabled='disabled' ");
  	    		 		}
  	    		 		out.print("></input>" + allSysRoles[i][1]);
  	    		 		out.print("</td>");
  	    		 		if((count < 3) && (i==(allSysRoles.length-1))){
  	    		 		  int numcol = 3-count;
  	    		 		  int num;
  	    		 		  for (num = 0; num <numcol; num++){
  	    		 		  	out.print("<td class='portlet-section-body'>");
  	    		 		    out.print("</td>");
  	    		 		  }out.print("</tr>");
  	    		 		  } 
  	    		 		if( (count==3) || (i==(allSysRoles.length-1)) ) {
  	    		 		 	
  	    		 		 	out.print("</tr>");
  	    		 		 	count = 1;
  	    		 		} 
  	    		 		else {
  	    		 		 	count ++;
  	    		 		 }
  	    		  }
  	    		%>
  	    	</table> 
  	    	
  		</td>
  		
  	</tr>
</table>
<br/>

<table width="100%" cellspacing="0" border="1" >
  	<tr height='1'>
  		<%-- <td><span>&nbsp; </span></td>--%>
  		<td>
  		
  	    	<table width="100%">
  	    		<tr >
  	    			<td colspan="3" align="left" class='portlet-section-header'>
  	    				<spagobi:message key = "SBIDev.paramUse.valTab2" />
  	    			</td>
  	    		</tr>
  	    		<% 
  	    		    List listChecks = param.getAssociatedChecks();
  	    		    Check tmpCheck = null;
  	    		    int counter = 1;
  	    		    for(int i=0; i<sysChecks.length; i++) { 
                        if(counter==1) {
                          out.print("<tr class='portlet-font'>");
                        }
                        boolean isCheck = false;
                        String checkId = sysChecks[i][0].toString();
                         //the list checks is not loaded at the moment
                        if (listChecks != null){
                        	for(int j=0; j<listChecks.size(); j++) {
                          		tmpCheck = (Check)listChecks.get(j);
                   		  		if(checkId.equals(tmpCheck.getCheckId().toString())) 
                   					isCheck = true; 
                   			}
                   		}	    
  	    		 		out.print("<td class='portlet-section-body'>");
  	    		 		out.print("   <input type='checkbox' name='idCheck' "+readonly+" value='"+checkId+"' ");
  	    		 		if(isCheck) {
  	    		 			out.print(" checked='checked' ");
  	    		 		}
  	    		 		out.print(">" + sysChecks[i][1] + "</input>" );
  	    		 		out.print("</td>");
  	    		 		if((counter < 3) && (i==(sysChecks.length-1))){
  	    		 		  int numcol = 3-counter;
  	    		 		  int num;
  	    		 		  for (num = 0; num <numcol; num++){
  	    		 		  out.print("<td class='portlet-section-body'>");
  	    		 		  out.print("</td>");  
  	    		 		  }out.print("</tr>");
  	    		 		  } 
  	    		 		
  	    		 		if( (counter==3) || (i==(sysChecks.length-1)) ) {
  	    		 		 	out.print("</tr>");
  	    		 		 	counter = 1;
  	    		 		} else {
  	    		 		 	counter ++;
  	    		 		 }
  	    		  }
  	    		%>
  	    	</table>   	
  		</td>
  	</tr>
</table>

</form>

<!--br/>
<br/-->

<%--div style="width:100%;">
	 		<table width="100%">
      	    	<tr>
      	    	    <td>&nbsp;</td>
      	    	 	<td width="80px">
      	    	 	    <input type='image' src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/img/save.png")%>' name='save' alt='save'/> 
					</td>
					</form>
					<td width='30px'>&nbsp;</td>
					<td width="80px">
					    <a href= '<%= formUrl1.toString() %>' class='portlet-menu-item' >
      						<img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/img/back.png")%>' alt='Back' />
						</a> 
					</td>
					<td>&nbsp;</td>
      	    	</tr>
      	    	<tr>
      	    		<td>&nbsp;</td>
      	    		<td width="80px">
      	    	 	    <a href="javascript:document.getElementById('modalitiesForm').submit()" > 
      	    	 	        <spagobi:message key = "SBIDev.paramUse.saveButt" /> 
      	    	 	    </a> 
					</td>
					<td width='30px'>&nbsp;</td>
					<td width="80px">
						<a href='<%= formUrl1.toString() %>'>
							<spagobi:message key = "SBIDev.paramUse.backButt" />
						</a>
					</td>
      	    	</tr>
      	    </table>
</div--%>

<!--br/><br/-->
