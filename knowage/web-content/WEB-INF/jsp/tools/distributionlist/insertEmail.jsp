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
	
	<%@ page         import="it.eng.spagobi.tools.distributionlist.bo.DistributionList,
							 it.eng.spagobi.tools.distributionlist.bo.Email,
	 				         it.eng.spago.navigation.LightNavigationManager,
	 				         java.util.Map,java.util.HashMap,java.util.List,
	 				         java.util.Iterator,
	 				         it.eng.spagobi.commons.bo.Domain,
	 				         it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects,
	 				         it.eng.spagobi.tools.distributionlist.service.DetailDistributionListUserModule" %>
	 				         
	<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
	
	<%
	
		String listPageName="ListDistributionListUserPage";
		SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailDistributionListUserModule");
		if (moduleResponse==null){
		    listPageName="ListDistributionListUserPageWithOutback";
		    moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailDistributionListUserModuleWithOutback");		    
		}
	
	
	    
	    String dlid = (String)moduleResponse.getAttribute("DL_ID");
		DistributionList dl = (DistributionList)moduleResponse.getAttribute("dlObj");
		String modality = "DETAIL_SUBSC" ;
		String subMessageDet = (((String)moduleResponse.getAttribute("SUBMESSAGEDET")==null)?"":(String)moduleResponse.getAttribute("SUBMESSAGEDET"));
		String msgWarningSave = msgBuilder.getMessage("8002", request);
		String email = (String)moduleResponse.getAttribute("EMAIL");
		
		request.setAttribute("dlObj", dl);
		request.setAttribute("DL_ID", dlid);
		request.setAttribute("modality", modality);
		request.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "insertEmailPubJ");
		request.setAttribute("SUBMESSAGEDET",subMessageDet);
		
		
		Map backUrlPars = new HashMap();
		backUrlPars.put("PAGE", listPageName);
		backUrlPars.put("TYPE_LIST", "TYPE_LIST");
		String backUrl = urlBuilder.getUrl(request, backUrlPars);	
	
		Map formUrlPars = new HashMap();
		String formUrl = urlBuilder.getUrl(request, formUrlPars);		

	%>
	
	
	<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
	
	

<form method='POST' action='<%=formUrl%>' id='emailForm' name='emailForm' >
		<%
			  
			  String name = dl.getName();
			   if((name==null) || (name.equalsIgnoreCase("null"))  ) {
				   
				   name = "";
			   }
		%>
		<%
			   if((email==null) || (email.equalsIgnoreCase("null"))  ) {
				   
				   email = "";
			   }
		%>

	
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "SBISet.ListDL.emailInsertTitle"  /> &nbsp; <%=StringEscapeUtils.escapeHtml(name)%>
			</td>
		</tr>
	</table>
	

	<div id='emailinsert' class='div_background' style='padding-top:5px;padding-left:5px;'>
		<br>

	<div class='div_detail_form'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.ListDL.columnNameInsertMail" /> 
			</span>
		</div>
		
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.ListDL.emailInsert" />
			</span>
		</div>

		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" 
				   name="EMAIL" size="50" value="<%=StringEscapeUtils.escapeHtml(email)%>" maxlength="50" /> *
			
			
		</div>
	</div>	
		
	<%  if(!email.equalsIgnoreCase("")) { %>
		<div style='text-align:center;vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "SBISet.ListDL.emailCorrect" /> 
		</div>   
				  
	 <%  }%>
		


<input type='hidden' name='PAGE' value='InsertEmailPage' />
<input type='hidden' name='<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>' value='true' />
<input type='hidden' value='<%=modality%>' name='MESSAGEDET' />	
<input type='hidden' value='<%=subMessageDet%>' name='SUBMESSAGEDET' />
<input type='hidden' value='<%=dlid%>' name='DL_ID' />



<script>
	function saveDL() {	
	     email =  document.forms[0].elements[0].value ;
         if( email == null || email == '' ){ 
         							alert('Email Missing'); 
         							}
         else {	
        var emailPat=/^(.+)@(.+)$/ ;
		var matchArray = email.match(emailPat)
		if (matchArray==null) {
		alert('Email address seems incorrect');
		return false ;
		}						 	  	  
  	  	 document.getElementById('emailForm').submit();
  	  	  }
  	  	  
	}
</script>	
	</form>
	
	<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
	
