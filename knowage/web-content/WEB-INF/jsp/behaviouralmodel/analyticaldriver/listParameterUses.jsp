<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


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
