<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.behaviouralmodel.check.bo.Check,
                 it.eng.spago.navigation.LightNavigationManager,
                 it.eng.spagobi.commons.dao.DAOFactory,
                 it.eng.spagobi.commons.bo.Domain" %>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>

<% 
    SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailChecksModule"); 
	Check check = (Check)moduleResponse.getAttribute("checkObj");
	String modality = (String)moduleResponse.getAttribute("modality");

	Map formUrlPars = new HashMap();
	formUrlPars.put("PAGE", "DetailModalitiesChecksPage");
	formUrlPars.put("MESSAGEDET", modality);
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String formUrl = urlBuilder.getUrl(request, formUrlPars);
	
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", "ListModalitiesChecksPage");
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);
   	
 %>



<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBIDev.valConst.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a id="submit" href="javascript:document.getElementById('checkForm').submit()"> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBIDev.valConst.saveButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBIDev.valConst.saveButt" />' /> 
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl %>'> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBIDev.valConst.backButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBIDev.valConst.backButt" />' />
			</a>
		</td>
	</tr>
</table>



<div class='div_background' >


 
<div id="checkDiv1" style='display:inline;'>
 
	<form method='POST' action='<%= formUrl %>' name='checkForm1' id='checkForm1'>
 	<input type='hidden' value=<%=(check.getCheckId() != null ? String.valueOf(check.getCheckId().intValue()) : "-1")%> name='id' />

	<div class="div_detail_area_forms">
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.labelField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='50'
			 	  type="text" name="label" value="<%=StringEscapeUtils.escapeHtml(check.getLabel())%>" maxlength="20">
			&nbsp;*
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.nameField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='50' 
			   		type="text" name="name" value="<%=StringEscapeUtils.escapeHtml(check.getName())%>" maxlength="40">
			&nbsp;*
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.descriptionField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='50'
			   		type="text" name="description" value="<%=StringEscapeUtils.escapeHtml(check.getDescription())%>" maxlength="160">
		</div>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.ListConfConst.columnCheckType" />
			</span>
		</div>
		<div class='div_detail_form'>
			&nbsp;
		</div>
    <%  
		String actualValueCD = check.getValueTypeCd();
		java.util.List checksTypes = DAOFactory.getDomainDAO().loadListDomainsByType("CHECK"); 
		java.util.Iterator it = checksTypes.iterator();
		java.util.Iterator it1 = checksTypes.iterator();
		Domain dom = null;
		String valueCD = null;
		String valueID = null;
		String fieldValue1Name = null;
		String fieldValue2Name = null;
		String valueForRaddio = null;	
		int numChecks = 0;
		while (it.hasNext()){
			dom = (it.eng.spagobi.commons.bo.Domain)it.next();
			valueCD = dom.getValueCd();
			valueID = String.valueOf(dom.getValueId());
			valueForRaddio = valueID+";"+valueCD;
			fieldValue1Name = valueCD +"_value1";
			fieldValue2Name = valueCD +"_value2";
			String keyLabel = "";
			numChecks = numChecks +1;
			keyLabel = "SBIDev.valConst." + dom.getValueCd();  
     %>
     	<div class='div_radio_check'>
     		<input type="radio" name="checkType" 
			       value="<%=valueForRaddio%>" <%=(valueCD.equalsIgnoreCase(actualValueCD) ? "checked=checked" : "") %>/>	
     	</div>
		<div class='div_detail_label_check'>			
			<span class='portlet-form-field-label'>
				<spagobi:message key = "<%=keyLabel%>" />
			</span>

		</div>
		<div class='div_detail_form'>
		<% if (actualValueCD != null && valueCD.equalsIgnoreCase(actualValueCD) && (check != null)){ %>
    		<input size='42' class='portlet-form-input-field' type="text" name="<%=fieldValue1Name %>" value="<%=(check.getFirstValue() != null ? StringEscapeUtils.escapeHtml(check.getFirstValue())  : "") %>" maxlength=50/>
    	<% } else { %>
    		<input size='42' class='portlet-form-input-field' type="text" name="<%=fieldValue1Name %>" value="" maxlength=50/>
    	<% } %>
        </div>
        <%
           if(dom.getTranslatedValueName(locale).equalsIgnoreCase("Range")){ 
				keyLabel = "SBIDev.valConst." + dom.getValueCd()+ ".Value2"; %>
     	<div class='div_radio_check'>
			&nbsp;
     	</div> 
     	<div class='div_detail_label_check'>			
			<span class='portlet-form-field-label'>
				<spagobi:message key = "<%=keyLabel%>" />
			</span>
		</div>
		<div class='div_detail_form'>
		<% if (actualValueCD != null && valueCD.equalsIgnoreCase(actualValueCD) && (check != null)){ %>
				<input size='42' class='portlet-form-input-field' type="text" name="<%=fieldValue2Name %>" value="<%=(check.getSecondValue() != null ? StringEscapeUtils.escapeHtml(check.getSecondValue())  : "") %>" maxlength=50/>
    	    <% } else { %>
    	    	<input size='42' class='portlet-form-input-field' type="text" name="<%=fieldValue2Name %>" value="" maxlength=50/>
    	    <% } %> 
    	 </div>      
        <% } // end if is range %>
    <%	} // end while  %>
      


	</div>
	</form>
</div>
 


<div id="checkDiv2" style='display:none;'>
	<form method='POST' action='<%= formUrl %>' name='checkForm2' id='checkForm2'>
	<input type='hidden' value=<%=(check.getCheckId() != null ? String.valueOf(check.getCheckId().intValue()) : "")%> name='id' />

	<div class="div_detail_area_forms">
		<div class='div_detail_label_check'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.labelField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='42'
			 	  type="text" name="label" value="<%=StringEscapeUtils.escapeHtml(check.getLabel())%>" maxlength="20">
			&nbsp;*
		</div>
		<div class='div_detail_label_check'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.nameField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='42' 
			   		type="text" name="name" value="<%=StringEscapeUtils.escapeHtml(check.getName())%>" maxlength="40">
			&nbsp;*
		</div>
		<div class='div_detail_label_check'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.valConst.descriptionField" />
			</span>
		</div>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' size='42'
			   		type="text" name="description" value="<%=StringEscapeUtils.escapeHtml(check.getDescription())%>" maxlength="160">
		</div>
		<div class='div_detail_label_check'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.ListConfConst.columnCheckType" />
			</span>
		</div>
		<div class='div_detail_form'>
		<%  
			actualValueCD = check.getValueTypeCd();
			it = checksTypes.iterator();
			int currCheck = 0;
		%>
		 	<select class='portlet-form-field' name="checkType" onchange= "showFields()" >
		<%  while (it.hasNext()){
				dom = (it.eng.spagobi.commons.bo.Domain)it.next();
				valueCD = dom.getValueCd();
				valueID = String.valueOf(dom.getValueId());
				valueForRaddio = valueID+";"+valueCD;
				fieldValue1Name = valueCD +"_value1";
				fieldValue2Name = valueCD +"_value2";
	 	%>     
    			<option value="<%= valueForRaddio  %>" <%if(valueCD.equalsIgnoreCase(actualValueCD)) out.print(" selected='selected' ");%>>
    				<%=dom.getTranslatedValueName(locale) %>
    			</option>
    	<%
    			currCheck = currCheck +1; 
    		}
    	%>
    	  	</select>  
		</div>
		<%
			it1 = checksTypes.iterator();
	        currCheck = 0;
    		while (it1.hasNext()){
    		    dom = (Domain)it1.next();
				valueCD = dom.getValueCd();
			    valueID = String.valueOf(dom.getValueId());
			    valueForRaddio = valueID+";"+valueCD;
			    fieldValue1Name = valueCD +"_value1";
			    fieldValue2Name = valueCD +"_value2";
    			String keyLabel = "";
			    String currDiv = String.valueOf(currCheck); 
		        String divId  = currDiv ;
	            String style = null; 
	            if(valueCD.equalsIgnoreCase(actualValueCD)) {
	               style = "display:inline";
	            } else {
	               style = "display:none";
	            } 
	            keyLabel = "SBIDev.valConst." + dom.getValueCd();
	     %>
	     <div id="<%= valueForRaddio %>" style='<%=style%>'>
	     <div class='div_detail_label_check'  >
			<span class='portlet-form-field-label'>
				<spagobi:message key = "<%=keyLabel%>" />
			</span>
		 </div>
	     <div class='div_detail_form' >
	     <% 
	     	if (actualValueCD != null && valueCD.equalsIgnoreCase(actualValueCD) && (check != null)){ 
	     %>
    		<input class='portlet-form-input-field' type="text" 
    		       name="<%=fieldValue1Name %>"
				   value="<%=(check.getFirstValue() != null ? StringEscapeUtils.escapeHtml(check.getFirstValue())  : "") %>" 
				   size='42'  maxlength=50/>
    	 <% } else { %>
    		<input class='portlet-form-input-field' type="text" name="<%=StringEscapeUtils.escapeHtml(fieldValue1Name) %>"
				   value="" size='42' maxlength=50/>
    	 <% } %>
	     </div>
	     <% if(dom.getTranslatedValueName(locale).equalsIgnoreCase("Range")){ 
	     		keyLabel = "SBIDev.valConst." + dom.getValueCd()+ ".Value2";
	     %>
	      <div class='div_detail_label_check'  >
			<span class='portlet-form-field-label'>
				<spagobi:message key = "<%=keyLabel%>" />
			</span>
		 </div>
	     <div class='div_detail_form' >
	     <% if (actualValueCD != null && valueCD.equalsIgnoreCase(actualValueCD) && (check != null)){ %>
    		<input class='portlet-form-input-field' type="text" name="<%=fieldValue2Name %>"
				   value="<%=(check.getSecondValue() != null ? StringEscapeUtils.escapeHtml(check.getSecondValue())  : "") %>" 
				   size='42' maxlength=50/>
    	 <% } else { %>
    	    <input class='portlet-form-input-field' type="text" 
    	           name="<%=StringEscapeUtils.escapeHtml(fieldValue2Name) %>" value="" size='42'  maxlength=50/>
    	 <% } %>
	     </div>
	     <% } %>
	     </div>
	     <% currCheck = currCheck + 1; 
	     	} 
	     %>
	     		
		
	<div><!-- chiusura div area forms  -->
    </form>
</div> <!-- chiusura div 2 --> 









<script type="text/javascript">
	
	function change(container, container1) {
 		var cont = document.getElementById(container);
 		if(cont.style.display=='none'){
 			cont.style.display = 'inline';
 		} else {
 			cont.style.display = 'none';
 		}
    	var cont1 = document.getElementById(container1);
 		if(cont1.style.display=='none'){
 			cont1.style.display = 'inline';
 		} else {
 			cont1.style.display = 'none';
 		}
	}


	function showFields(){
		var cont = document.checkForm2.checkType.selectedIndex;
		var cont1 = document.checkForm2.checkType[cont].value;
		var selectDiv = document.getElementById(cont1);
		selectDiv.style.display = 'inline';
		var lenArray = document.checkForm2.checkType.options.length;
		for(var i=0; i<lenArray;i++) {
			var currIndex = document.checkForm2.checkType[i].value;
			var currDiv = document.getElementById(currIndex);
			if(currDiv != selectDiv) {
				currDiv.style.display = 'none';
			}
		}   
	}


	function setForm(){
		var currDiv = document.getElementById('checkDiv1');
		var submit =  document.getElementById('submit');
		if(currDiv.style.display == 'inline'){
			var href = "javascript:document.getElementById('checkForm1').submit()";
			submit.href = href;
		}
		else{
			var  href1 = "javascript:document.getElementById('checkForm2').submit()";
			submit.href = href1;
		}
	}
</script>




<script>
 	change ('checkDiv1', 'checkDiv2');
 	setForm();
</script>




<spagobi:error/>




</div> <!-- close background --> 
