<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.engines.config.bo.Engine,
                 it.eng.spagobi.commons.dao.DAOFactory,
                 it.eng.spagobi.commons.bo.Domain,
                 it.eng.spagobi.tools.datasource.bo.DataSource,
                 it.eng.spago.navigation.LightNavigationManager,
                 java.util.Map,
                 java.util.HashMap" %>

<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DetailEngineModule"); 
	Engine engine = (Engine)moduleResponse.getAttribute("engineObj");
	String modality = (String)moduleResponse.getAttribute("modality");
 
	Map formUrlPars = new HashMap();
	formUrlPars.put("PAGE", "detailEnginePage");
	formUrlPars.put("MESSAGEDET", modality);
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String formUrl = urlBuilder.getUrl(request, formUrlPars);
	
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", "ListEnginesPage");
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);

%>
		

<form method='POST' action='<%=formUrl%>' id='engineForm' name='engineForm'>
<input type='hidden' value='<%=engine.getId()%>' name='id' />



<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.eng.titleMenu" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section' id='testButton'>
				<a href="javascript:testEngineConnection()"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "SBIDev.predLov.TestBeforeSaveLbl" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/test.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "SBIDev.predLov.TestBeforeSaveLbl" />' 
	      			/> 
				</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('engineForm').submit()"> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBISet.eng.saveButt" />' src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' alt='<spagobi:message key = "SBISet.eng.saveButt" />' /> 
			</a>
		</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBISet.eng.backButt" />' src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' alt='<spagobi:message key = "SBISet.eng.backButt" />' />
			</a>
		</td>
	</tr>
</table>



<div class='div_background' style='padding-top:5px;padding-left:5px;'>


<div class="div_detail_area_forms" >
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.LabelField" />
		</span>
	</div>
	<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="text" name="label" 
	      	   size="50" value="<%=StringEscapeUtils.escapeHtml(engine.getLabel())%>" maxlength="20">
	    &nbsp;*
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.NameField" />
		</span>
	</div>
	<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="text" 
			   name="name" size="50" value="<%=StringEscapeUtils.escapeHtml(engine.getName())%>" maxlength="45">
		&nbsp;*
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>	
			<spagobi:message key = "SBISet.eng.descriptionField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<% String desc = engine.getDescription();
	   if((desc==null) || (desc.equalsIgnoreCase("null"))  ) {
	   	   desc = "";
	   } 
	%>
		<input class='portlet-form-input-field' type="text" name="description" 
			   size="50" value="<%= StringEscapeUtils.escapeHtml(desc) %>" maxlength="130">
	</div>
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.biobjTypeField" />
		</span>
	</div>	
	<div class='div_detail_form'>
		<select class='portlet-form-field' name="biobjTypeId" >
			<%
			java.util.List biobjTypes = DAOFactory.getDomainDAO().loadListDomainsByType("BIOBJ_TYPE");
			java.util.Iterator it = biobjTypes.iterator();
			while (it.hasNext()) {
				Domain domain = (Domain) it.next();
				String valueId = String.valueOf(domain.getValueId());
				String actualValueId = engine.getBiobjTypeId().toString();
			 	%>     
    				<option value="<%= valueId  %>" <% if (valueId.equalsIgnoreCase(actualValueId)) out.print(" selected='selected' ");%>>
    					<%=domain.getTranslatedValueName(locale)%>
    				</option>
    				<%
			}
			%>
		</select>
	</div>
	
	
		<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.engineTypeField" />
		</span>
	</div>	
	<div class='div_detail_form'>
		<select class='portlet-form-input-field' 
		name="engineTypeId" 
		onchange= "changeEngineType(this.options[this.selectedIndex].id)" 
		id="engineType">
			<%
			java.util.List engineTypes = DAOFactory.getDomainDAO().loadListDomainsByType("ENGINE_TYPE");
			java.util.Iterator engineTypesIt = engineTypes.iterator();
			String engineType = "EXT"; // default value
			while (engineTypesIt.hasNext()) {
				Domain domain = (Domain) engineTypesIt.next();
				String valueId = String.valueOf(domain.getValueId());
				String actualValueId = engine.getEngineTypeId().toString();
				String selected = "";
				if (valueId.equalsIgnoreCase(actualValueId)) {
					selected = "selected='selected'";
					engineType = domain.getValueCd();
				}
			 	%>     
    				<option value="<%= valueId  %>"  id="<%=domain.getValueCd()%>" <%= selected %>>
    					 <%=domain.getTranslatedValueName(locale)%>    				
    				</option>
    				<%
			}
			%>
		</select>
	</div>
	
			<%boolean useDataSet=engine.getUseDataSet();
	%>
		<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.useDataSet" />
		</span>
	</div>		
		<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="checkbox" name="useDataSet" id="useDataSet"
			   value="true" <%=(useDataSet==true ? "checked='checked'" : "")%>/>
	</div>
	
	
	
	<%
	boolean useDataSource=engine.getUseDataSource();
	%>
	
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.useDataSource" />
		</span>
	</div>	
		<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="checkbox" name="useDataSource" id="useDataSource"
			   value="true" <%=(useDataSource==true ? "checked='checked'" : "")%> onclick = "manualInputSelection=this.value;"/>
			   
	</div>
	

	
	<div id="className" style='display:<%= ("INT".equalsIgnoreCase(engineType)) ? "inline;" : "none;" %>'>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.eng.classNameField" />
			</span>
		</div>
		<% 
		String engineClassName = engine.getClassName();
	   	if ((engineClassName == null) || (engineClassName.equalsIgnoreCase("null"))  ) {
	   		engineClassName = "";
	   	} 
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" name="className"  
			   	size="50" value="<%=StringEscapeUtils.escapeHtml(engineClassName)%>" maxlength="260">
			&nbsp;*			   
		</div>
	</div>
	<div id="url" style='display:<%= ("EXT".equalsIgnoreCase(engineType)) ? "inline;" : "none;" %>'>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.eng.urlField" />
			</span>
		</div>
		<% 
		String engineUrl = engine.getUrl();
	   	if ((engineUrl == null) || (engineUrl.equalsIgnoreCase("null"))  ) {
	   		engineUrl = "";
	   	} 
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" name="url"  
			   	size="50" value="<%=StringEscapeUtils.escapeHtml(engineUrl)%>" maxlength="260">
			&nbsp;*
		</div>
	</div>
	<div id="secondaryUrl" style='display:<%= ("EXT".equalsIgnoreCase(engineType)) ? "inline;" : "none;" %>'>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.eng.secondaryUrlField" />
			</span>
		</div>
		<div class='div_detail_form'>
		<% String secUrl = engine.getSecondaryUrl();
		   if( (secUrl==null) || (secUrl.equalsIgnoreCase("null")) ) {
		   		secUrl = "";
		   } 
		%>
			<input class='portlet-form-input-field' type="text" name="secondaryUrl" 
	                size="50" value="<%=StringEscapeUtils.escapeHtml(secUrl)%>" maxlength="260">
		</div>
	</div>
	<div id="driverName" style='display:<%= ("EXT".equalsIgnoreCase(engineType)) ? "inline;" : "none;" %>'>
		<div class='div_detail_label'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBISet.eng.driverNameField" />
			</span>
		</div>
		<% 
		String engineDriver = engine.getDriverName();
	   	if ((engineDriver == null) || (engineDriver.equalsIgnoreCase("null"))  ) {
	   		engineDriver = "";
	   	} 
		%>
		<div class='div_detail_form'>
			<input class='portlet-form-input-field' type="text" name="driverName"
					size="50" value="<%=StringEscapeUtils.escapeHtml(engineDriver)%>" maxlength="260">
			&nbsp;*				
		</div>
	</div>
	<div class='div_detail_label' style='display:none;'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.dirUploadField" />
		</span>
	</div>
	<!-- 
	<div class='div_detail_form' style='display:none;'>
	<% String dirUpl = engine.getDirUpload();
	   if( (dirUpl==null) || (dirUpl.equalsIgnoreCase("null")) ) {
	   		dirUpl = "";
	   } 
	%>
		<input class='portlet-form-input-field' type="text" name="dirUpload" 
			   size="50" value="<%=dirUpl%>" maxlength="260">
	</div>
	<div class='div_detail_label' style='display:none;'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.dirUsableField" />
		</span>
	</div>
	<div class='div_detail_form' style='display:none;'>
	<% String dirUse = engine.getDirUsable();
	   if( (dirUse==null) || (dirUse.equalsIgnoreCase("null")) ) {
	   		dirUse = "";
	    } 
	%>
		<input class='portlet-form-input-field' type="text" name="dirUsable" 
			   size="50" value="<%=dirUse%>" maxlength="260">
	</div> -->
	<div class='div_detail_label' style='display:none;'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBISet.eng.criptableField" />
		</span>
	</div>
	<div class='div_detail_form' style='display:none;'>
	<% boolean isCrypt = false;
	   int cript = engine.getCriptable().intValue();
	   if(cript > 0) { isCrypt = true; }
	%>
		<input type="radio" name="criptable" value="1" 
               <% if(isCrypt) { out.println(" checked='checked' "); } %> >
               True
        </input>
        <input type="radio" name="criptable" value="0" 
               <% if(!isCrypt) { out.println(" checked='checked' "); } %> >
               False
        </input> 
	</div>
</div>



<spagobi:error/>


</div>


</form>

<script>

	function changeEngineType(value){
		if (value == 'EXT') {
			document.getElementById('url').style.display = 'inline';
			document.getElementById('secondaryUrl').style.display = 'inline';
			document.getElementById('driverName').style.display = 'inline';
			document.getElementById('className').style.display = 'none';		
		}
		if (value == 'INT') {
			document.getElementById('url').style.display = 'none';
			document.getElementById('secondaryUrl').style.display = 'none';
			document.getElementById('driverName').style.display = 'none';
			document.getElementById('className').style.display = 'inline';		
		}
	}

	function testEngineConnection() {
	
		var extEngType = document.engineForm.engineType.selectedIndex;
	    var url = document.engineForm.url.value;
	    var driverName = document.engineForm.driverName.value;
	    var className = document.engineForm.className.value;

		if ( extEngType==0 ){	
			if ( !url ){	
			Ext.MessageBox.show({
				msg: 'No url',
				buttons: Ext.MessageBox.OK,
				width:150
			});
			return;
			}
			if ( !driverName ){	
			Ext.MessageBox.show({
				msg: 'No driverName',
				buttons: Ext.MessageBox.OK,
				width:150
			});
			return;
			}	  
		}
		else {	
			if ( !className ){	
			Ext.MessageBox.show({
				msg: 'No className',
				buttons: Ext.MessageBox.OK,
				width:150
			});
			return;
			}
		}
	
		var urll;
		if ( extEngType==0 ){
		    urll= url+'Test?';
			Ext.Ajax.request({
 				 url: urll,
 				 method: 'get',
				 success: function (result, request) {
					response = result.responseText || "";
					if (response=="sbi.connTestOk") {
						url2="<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId) + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE"%>";
						url2 += "&ACTION_NAME=TEST_ENGINE";
						url2 += "&driverName="+driverName;
						Ext.Ajax.request({
		 				 url: url2,
		 				 method: 'get',
						 success: function (result, request) {
							response = result.responseText || "";
							showEngineTestResult(response);
						 },
				 		 failure: somethingWentWrong
					    });
					}else{
						showEngineTestResult(response);
					}
				},
	 		 failure: somethingWentWrong
		    });
			 
		}else{
		    urll="<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userId) + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE"%>";
			urll += "&ACTION_NAME=TEST_ENGINE";
			urll += "&className="+className;
			Ext.Ajax.request({
 				 url: urll,
 				 method: 'get',
				 success: function (result, request) {
					response = result.responseText || "";
					showEngineTestResult(response);
				},
	 		 failure: somethingWentWrong
		    });
		}	
			  	 
	  }
		
	function showEngineTestResult(response) {
		var iconRememberMe;
		if (response=="sbi.connTestOk") {
			response = "<spagobi:message key="sbi.connTestOk" />";
			iconRememberMe = Ext.MessageBox.INFO;
		}
		else if (response=="ClassNameError") {
			response = "<spagobi:message key="sbi.engineClassTestError"/>";
			iconRememberMe = Ext.MessageBox.ERROR;
		}
		else{
		    response = "<spagobi:message key="sbi.engineTestError"/>";
			iconRememberMe = Ext.MessageBox.ERROR;
		}
		Ext.MessageBox.show({
			title: 'Status',
			msg: response,
			buttons: Ext.MessageBox.OK,
			width:250,
			icon: iconRememberMe
		});
	}
	
	function somethingWentWrong() {
		alert('<spagobi:message key="sbi.connTestError" />');
	}

</script>
 
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>


