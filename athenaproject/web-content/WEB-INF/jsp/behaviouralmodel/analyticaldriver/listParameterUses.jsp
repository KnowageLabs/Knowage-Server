<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  



<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter,
				 it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO,
				 it.eng.spagobi.commons.dao.DAOFactory" %>

<% String par_id = (String)aServiceRequest.getAttribute("ID_DOMAIN"); 
   Integer parId = new Integer(par_id);
   Parameter param = new Parameter();
   param = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
   String paramLabel = param.getLabel();
   String paramName = param.getName();
   String paramDescription = param.getDescription();
   String paramType = param.getType();
   
%>
	
  	<table width="100%"  style="margin-top:3px; margin-left:3px; margin-right:3px; margin-bottom:5px;">
  		<tr height='1px'>
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
  				<%=paramLabel %>
  			</td>
  			<td class='portlet-section-body' style='border-top: 1px solid #CCCCCC;'>
  				<spagobi:message key = "SBIDev.ListParamUse.parInfo.Name"/>: 
  			</td>
  			<td class = 'portlet-section-alternate' style='border-top: 1px solid #CCCCCC;'>
  				<%=paramName %>
  			</td>
  		</tr>
  		<tr height = "20">
  			<td class='portlet-section-subheader' style='text-align:center;vertical-align:top;'>
  				<spagobi:message key = "SBIDev.ListParamUse.parInfo2" />
  			</td>
  		    <td style="width:3px;"></td>
  			<td class='portlet-section-body'>
  				<spagobi:message key = "SBIDev.ListParamUse.parInfo.Type"/>: 
  			</td>
  			<td class = 'portlet-section-alternate'>
  				<%=paramType %>
  			</td>
  			<td class='portlet-section-body' >
  				<spagobi:message key = "SBIDev.ListParamUse.parInfo.Description"/>: 
  			</td>
  			<td class = 'portlet-section-alternate'>
  				<%=paramDescription %>
  			</td>
  		</tr>
  </table>
  
<spagobi:list moduleName="ListParameterUsesModule" />