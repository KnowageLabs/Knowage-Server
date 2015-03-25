<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
	<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
	
	
	
	<%@ page         import="it.eng.spagobi.tools.distributionlist.bo.DistributionList,
							 it.eng.spagobi.tools.distributionlist.bo.Email,
	 				         it.eng.spago.navigation.LightNavigationManager,
	 				         java.util.Map,java.util.HashMap,java.util.List,
	 				         java.util.Iterator,
	 				         it.eng.spagobi.commons.bo.Domain,
	 				         it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects,
	 				         it.eng.spagobi.tools.distributionlist.service.DetailDistributionListModule" %>
	<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
	 				         
	<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
	<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
	
	<%
		SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailDistributionListModule"); 
		DistributionList dl = (DistributionList)moduleResponse.getAttribute("dlObj");
		List listDialects = (List) moduleResponse.getAttribute(DetailDistributionListModule.NAME_ATTR_LIST_DIALECTS);
		
		String modality = (String)moduleResponse.getAttribute("modality");
		String subMessageDet = ((String)moduleResponse.getAttribute("SUBMESSAGEDET")==null)?"":(String)moduleResponse.getAttribute("SUBMESSAGEDET");
		String msgWarningSave = msgBuilder.getMessage("8002", request);
		
		Map formUrlPars = new HashMap();
			formUrlPars.put("PAGE", "DetailDistributionListPage");	
  			formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");	
		String formUrl = urlBuilder.getUrl(request, formUrlPars);
		
		Map backUrlPars = new HashMap();
		backUrlPars.put("PAGE", "ListDistributionListPage");
		backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
		String backUrl = urlBuilder.getUrl(request, backUrlPars);		
	%>
	
	

<form method='POST' action='<%=formUrl%>' id='dlForm' name='dlForm' >

	<!-- 
		<input type='hidden' name='PAGE' value='DetailDistributionListPage' />
		<input type='hidden' name='<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>' value='true' />  -->

	<input type='hidden' value='<%=modality%>' name='modality' />	
	<input type='hidden' value='<%=modality%>' name='MESSAGEDET' />	
	<input type='hidden' value='<%=subMessageDet%>' name='SUBMESSAGEDET' />
	<input type='hidden' value='<%=dl.getId()%>' name='id' />
	
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' 
			    style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "SBISet.ListDL.TitleDetail"  />
			</td>
			<td class='header-button-column-portlet-section'>
				<a href="javascript:saveDL('SAVE')"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "SBISet.ListDL.saveButton" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "SBISet.ListDL.saveButton"/>' 
	      			/> 
				</a>
			</td>		 
			<td class='header-button-column-portlet-section'>
				<input type='image' name='saveAndGoBack' id='saveAndGoBack' onClick="javascript:saveDL('SAVEBACK')" class='header-button-image-portlet-section'
				       src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/saveAndGoBack.png", currTheme)%>' 
      				   title='<spagobi:message key = "SBISet.ListDL.saveBackButton" />'  
                       alt='<spagobi:message key = "SBISet.ListDL.saveBackButton" />' 
			   />
			</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:goBack("<%=msgWarningSave%>", "<%=backUrl%>")'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "SBISet.ListDL.backButton"  />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "SBISet.ListDL.backButton" />' 
	      			/>
				</a>
			</td>		
		</tr>
	</table>
	
	
	<div class='div_background' style='padding-top:5px;padding-left:5px;'>
	
	<table width="100%" cellspacing="0" border="0" id = "fieldsTable" >
	<tr>
	  <td>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.ListDL.columnName" />
			</span>
		</div>
		<%
			  String name = dl.getName();
			   if((name==null) || (name.equalsIgnoreCase("null"))  ) {
				   name = "";
			   }
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" 
				   name="NAME" size="50" value="<%=StringEscapeUtils.escapeHtml(name)%>" maxlength="50" />
			&nbsp;*
		</div>
		
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>	
				<spagobi:message key = "SBISet.ListDL.columnDescr" />
			</span>
		</div>
		<div class='div_detail_form'>
		<%
			   String descr = dl.getDescr();
			   if((descr==null) || (descr.equalsIgnoreCase("null"))  ) {
			   	   descr = "";
			   }
		%>
			<input class='portlet-form-input-field' type="text" name="DESCR" 
				   size="50" value="<%= StringEscapeUtils.escapeHtml(descr) %>" maxlength="160" />
		</div>
	
	</td><!-- CLOSE COLUMN WITH DATA FORM  -->
		
		
		<spagobi:error/>
	</tr>
	</table>   <!-- CLOSE TABLE FORM ON LEFT AND VERSION ON RIGHT  -->
	<BR>
	
<% if (modality!= null && modality.equalsIgnoreCase("DETAIL_MOD")){ %>
<table style='width:98%;vertical-align:middle;margin-top:1px' >
<tr>
	<td>
	<table style='width:90%;vertical-align:middle;margin-top:1px' id ="userTable" >
		<tr class='header-row-portlet-section'>	
			<td class='header-title-column-portlet-section-nogrey' style='text-align:center;vertical-align:middle'>
					<spagobi:message key = "SBISet.ListDL.relatedUsers" />
			</td>
		</tr>
	</table>
		<table style='width:90%;margin-top:1px' id ="usersTable" >
	<tr>	
	  <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.columnUser" />
	  </td>			

	  <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.columnEmail" />		
	  </td>
	</tr>
	<!-- LIST OF USERS AND RESPECTIVE E_MAILS FOR A DISTRIBUTION LIST  -->
	<%
			List users = dl.getEmails();
			if(users!= null && !users.isEmpty()){
	%>		

	
		<%
			Iterator it = users.iterator();
			while(it.hasNext()){
				
				Email user=(Email)it.next();
				String userNameDL = user.getUserId();
				if((userNameDL==null) || (userNameDL.equalsIgnoreCase("null"))  ) {
					userNameDL = "";
				   }
				String userEmail = user.getEmail();
				if((userEmail==null) || (userEmail.equalsIgnoreCase("null"))  ) {
					   userEmail = "";
				   }				
		 %>
				
		<tr class='portlet-font'>
		 	<td class='portlet-section-body' style='vertical-align:left;text-align:left;'><%=StringEscapeUtils.escapeHtml(userNameDL) %>	 			
			</td>	
			<td class='portlet-section-body' style='vertical-align:left;text-align:left;'><%=StringEscapeUtils.escapeHtml(userEmail) %>
			</td>
	    </tr>
										
		<% } %>

	<% } %>
  <!-- CLOSE LIST OF USERS AND RESPECTIVE E_MAILS FOR A DISTRIBUTION LIST  -->
	</table>
</td>
	
<td>	
	<table style='width:75%;vertical-align:middle;margin-top:1px' id ="userTable" >
		<tr class='header-row-portlet-section'>			
			<td class='header-title-column-portlet-section-nogrey' style='text-align:center;vertical-align:middle'>
					<spagobi:message key = "SBISet.ListDL.relatedDoc" />
			</td>
		</tr>
	</table>	
	
	<table style='width:75%;margin-top:1px' id ="documentsTable" >
	<!-- LIST OF DOCUMENTS RELATED TO A DISTRIBUTION LIST  -->
	<tr>	
	  <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.columnDocName" />
	  </td>			

	  <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.columnDocDescr" />		
	  </td>
	  		 <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.ScheduleStart" />			

	 </td>
	 	 <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.ScheduleEnd" />	
	 </td>
	 <td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDL.ScheduleFrequence" />			

	 </td>
	</tr>	
	
	<%
			List documents = dl.getDocuments();
			if(documents!=null && !documents.isEmpty()){

			Iterator it2 = documents.iterator();
			while(it2.hasNext()){
				
				BIObject bo = (BIObject)it2.next();
				String docName = bo.getName();
				String docDescr = bo.getDescription();
				if((docName==null) || (docName.equalsIgnoreCase("null"))  ) {
					docName = "";
				   }
				if((docDescr==null) || (docDescr.equalsIgnoreCase("null"))  ) {
					docDescr = "";
				   }
				List xmls = DAOFactory.getDistributionListDAO().getXmlRelated(dl , bo.getId().intValue());
				if (! xmls.isEmpty()){
					Iterator xit = xmls.iterator();
					while (xit.hasNext()){
						String xml = (String) xit.next();						
						SourceBean sbOrig = SourceBean.fromXMLString(xml);
						
						String schedStart = (String)sbOrig.getAttribute("startDate");
						String temp1 = (String)sbOrig.getAttribute("startTime");
						String schedStartTime = temp1.substring(0,temp1.indexOf("+"));
						String schedBegin = schedStart + " " + schedStartTime ;
						
						String schedE = "";					
						String schedEndTime = "";
						if ( sbOrig.getAttribute("endDate") != null) schedE = (String)sbOrig.getAttribute("endDate");					
						if ( sbOrig.getAttribute("endTime") != null) {
							String temp = (String)sbOrig.getAttribute("endTime");
							schedEndTime = temp.substring(0,temp.indexOf("+"));	
						}
						String schedEnd = schedE + " " + schedEndTime ;
						
						String frequency = "";
						String temp = (String)sbOrig.getAttribute("chronString");
						int index = temp.indexOf("{");
						int end = temp.indexOf("}");
						String every = temp.substring(0,index);
						String content = temp.substring(index+1, end);
						
						if (every.equals("single")){
							frequency = msgBuilder.getMessage("sbi.frequency.singleEx", "messages", request)+" ";
						}
						if (every.equals("minute")){
							
							int begin = content.indexOf("=");
							String numRep = content.substring(begin+1);
							if (numRep.equals("1")){
								frequency = msgBuilder.getMessage("sbi.frequency.everyMin", "messages", request)+" ";
							}
							else {frequency = msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+numRep+" "+msgBuilder.getMessage("sbi.frequency.minutes", "messages", request)+" ";}
						}
						if (every.equals("hour")){
							
							int begin = content.indexOf("=");
							String numRep = content.substring(begin+1);
							if (numRep.equals("1")){
								frequency = msgBuilder.getMessage("sbi.frequency.everyH", "messages", request)+" ";
							}
							else
							{frequency = msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+numRep+" "+msgBuilder.getMessage("sbi.frequency.hours", "messages", request)+" ";}
						}
						if (every.equals("day")){
							
							int begin = content.indexOf("=");
							String numRep = content.substring(begin+1);
							if (numRep.equals("1")){
								frequency = msgBuilder.getMessage("sbi.frequency.everyD", "messages", request)+" ";
							}
							else
							{frequency = msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+numRep+" "+msgBuilder.getMessage("sbi.frequency.days", "messages", request)+" ";}
						}
						
						if (every.equals("week")){
							String[] params = content.split(";");
							int l = params.length ;
							String numRep = "";
							String days = "";
							for (int i =0;i<l;i++){
								String param = params[i];
								int begin = param.indexOf("=");
								
								if (param.startsWith("numRepetition")){		
									numRep = param.substring(begin+1);
								}
									
								if (param.startsWith("days")){
									days = param.substring(begin+1);
								}
							}
							days = days.replace(',',';');
							if (numRep.equals("1")){
								frequency = msgBuilder.getMessage("sbi.frequency.everyW", "messages", request)+" "+msgBuilder.getMessage("sbi.frequency.Days", "messages", request)+" "+days;
							}
							else
							{frequency = msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+numRep+" "+msgBuilder.getMessage("sbi.frequency.weeks", "messages", request)+" "+msgBuilder.getMessage("sbi.frequency.Days", "messages", request)+" "+days+" ";}
						}	
							
						if (every.equals("month")){	
							
							String[] params = content.split(";");
							String numRep = "";
							String months = "";
							String dayRep = "";
							String weeks = "";
							String days = "";
							int l = params.length ;
							for (int i =0;i<l;i++){
								String param = params[i];
								int begin = param.indexOf("=");
								
								if (param.startsWith("numRepetition")){	
									numRep = param.substring(begin+1);
								}
								if (param.startsWith("months")){
									months = param.substring(begin+1);
								}
								if (param.startsWith("dayRepetition")){
									dayRep = param.substring(begin+1);
								}
								if (param.startsWith("weeks")){
									weeks = param.substring(begin+1);
								}
								if (param.startsWith("days")){
									days = param.substring(begin+1);
								}
							}
							if (numRep.equals("0")){
								months = months.replace(',',';');
								frequency = "Months: "+months+"." ;
							}
							else if (!numRep.equals("0")){
								if (numRep.equals("1")){
									frequency = msgBuilder.getMessage("sbi.frequency.everyMonth", "messages", request)+" ";
								}
								else
								{frequency = msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+numRep+" "+ msgBuilder.getMessage("sbi.frequency.months", "messages", request)+" " ;}
							}	
							
							if (dayRep.equals("0")){
								if (weeks.equals("NONE")){
									days = days.replace(',',';');
									frequency = frequency + msgBuilder.getMessage("sbi.frequency.Days", "messages", request)+" "+days +" ";
								}	
								else if (!weeks.equals("NONE")){
									weeks = weeks.replace(',',';');
									if (weeks.equals("L")){
										frequency = frequency + msgBuilder.getMessage("sbi.frequency.Week", "messages", request)+" "+msgBuilder.getMessage("sbi.frequency.last", "messages", request)+" " ;
									}
									else if (weeks.equals("F")){
										frequency = frequency + msgBuilder.getMessage("sbi.frequency.Week", "messages", request)+" "+msgBuilder.getMessage("sbi.frequency.first", "messages", request) +" ";
									}
									else
									{frequency = frequency + " Week: "+weeks +" ";}
									if (!days.equals("NONE")){
										days = days.replace(',',';');
										frequency = frequency + msgBuilder.getMessage("sbi.frequency.Days", "messages", request)+" "+days +" ";
									}	
								}	
								
							}
							else if (!dayRep.equals("0")){
								days = days.replace(',',';');
								if (dayRep.equals("1")){
									frequency = msgBuilder.getMessage("sbi.frequency.everyD", "messages", request)+" ";
								}
								else
								{frequency = frequency + msgBuilder.getMessage("sbi.frequency.every", "messages", request)+" "+dayRep+" "+msgBuilder.getMessage("sbi.frequency.days", "messages", request)+" " ;}
							}
							
						}	
		 %>
		 <tr class='portlet-font'>
		 	<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>		 	
			<%=StringEscapeUtils.escapeHtml(docName)%>
			</td>	
			<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>
			<%=StringEscapeUtils.escapeHtml(docDescr) %>
			</td>
			 <td class='portlet-section-body' style='vertical-align:left;text-align:left;'>	
					<%=StringEscapeUtils.escapeHtml(schedBegin) %>				   				
				   	</td>	
				   	<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>	
					<%=StringEscapeUtils.escapeHtml(schedEnd) %>				   				
				   	</td>
					<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>	
					<%=StringEscapeUtils.escapeHtml(frequency) %>				   				
				   	</td>
	    </tr>
				    		
	<%}} } %>
	
								
	<% } %>
<!-- CLOSE LIST OF DOCUMENTS RELATED TO A DISTRIBUTION LIST  -->
<spagobi:error/>
	
	</table> 
	</td>
</tr>	
</table>
	<% } %>	
	</div>  

	<script>
	
	function isDlFormChanged () {
	
	var bFormModified = 'false';
		
	var name = document.dlForm.NAME.value;
	var description = document.dlForm.DESCR.value;	
	
	if ((name != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(dl.getName()))%>')
		|| (description != '<%=(StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(dl.getDescr()))==null)?"":StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(dl.getDescr()))%>')) {
			
		bFormModified = 'true';
	}
	
	return bFormModified;
	
	}

	
	function goBack(message, url) {
	  
	  var bFormModified = isDlFormChanged();
	  
	  if (bFormModified == 'true'){
	  	  if (confirm(message)) {
	  	      document.getElementById('saveAndGoBack').click(); 
	  	  } else {
			location.href = url;	
    	  }	         
       } else {
			location.href = url;
       }	  
	}
	
	function saveDL(type) {	
  	  	  document.dlForm.SUBMESSAGEDET.value=type;
  	  	  if (type == 'SAVE')
      		  document.getElementById('dlForm').submit();
	}
	</script>
	</form>
	<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
	