<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@page import="it.eng.spagobi.commons.bo.Role"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse"%>
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject,it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter,it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule,it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO,it.eng.spagobi.commons.dao.DAOFactory,it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter,java.util.List,it.eng.spagobi.commons.constants.ObjectsTreeConstants,it.eng.spagobi.commons.constants.AdmintoolsConstants,it.eng.spagobi.commons.bo.Domain,java.util.Iterator,it.eng.spagobi.engines.config.bo.Engine,it.eng.spagobi.commons.utilities.SpagoBITracer,it.eng.spago.navigation.LightNavigationManager,it.eng.spago.base.SourceBean,java.util.TreeMap,java.util.Collection,java.util.ArrayList,java.util.Date,it.eng.spagobi.commons.utilities.GeneralUtilities,it.eng.spagobi.commons.utilities.ChannelUtilities,java.util.Map,java.util.HashMap,it.eng.spagobi.commons.bo.Subreport,it.eng.spago.security.IEngUserProfile" %>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate"%>
<%@page import="it.eng.spagobi.community.mapping.SbiCommunity"%>
<%@page import="it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="it.eng.spagobi.tools.datasource.bo.DataSource"%>

<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.*"%>
<%@page import="it.eng.spagobi.tools.dataset.dao.IDataSetDAO"%>
<%@page import="it.eng.spagobi.security.SecurityInfoProviderFactory"%>
<%@page import="it.eng.spagobi.security.ISecurityInfoProvider"%>
<%@page import="org.apache.log4j.Logger"%>

<%!private static transient Logger logger = Logger
			.getLogger(DetailBIObjectModule.class);%>
<%
	// GET RESPONSE OBJECTS
	SourceBean moduleResponse = (SourceBean) aServiceResponse
			.getAttribute("DetailBIObjectModule");
	BIObject obj = (BIObject) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_OBJECT);
	List listEngines = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_ENGINES);
	List listTypes = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_OBJ_TYPES);
	List listStates = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_STATES);
	List listDataSource = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_DS);
	if (listDataSource == null)
		listDataSource = new ArrayList();
	List listDataSet = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_DATASET);
	if (listDataSet == null)
		listDataSet = new ArrayList();
	List listCommunities = (List) moduleResponse
			.getAttribute(DetailBIObjectModule.NAME_ATTR_LIST_COMMUNITIES);
	if (listCommunities == null)
		listCommunities = new ArrayList();

	String modality = (String) moduleResponse
			.getAttribute(ObjectsTreeConstants.MODALITY);
	
	IObjTemplateDAO objtempdao = DAOFactory.getObjTemplateDAO();

	// CREATE PAGE URLs
	Map formUrlPars = new HashMap();

	String formUrl = urlBuilder.getUrl(request, formUrlPars);

	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "detailBIObjectPage");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,
			"true");
	backUrlPars.put("MESSAGEDET", "EXIT_FROM_DETAIL");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);

	//boolean flgLoadParDC = moduleResponse.getAttribute(DetailBIObjectModule.LOADING_PARS_DC);
%>

<script>
var versionTemplateChanged = 'false';
var fileUploadChanged = 'false';
var fileDeleted = 'false';

function versionTemplateSelected () {
	versionTemplateChanged = 'true';
}

function fileToUploadInserted() {
	fileUploadChanged = 'true';
	fileDeleted = 'false';
}

function showEngField(docType) {
	var ind = docType.indexOf(",");
	var type = docType.substring(ind+1);
	var engines = document.objectForm.engine.options;
	engines.length = 0;
	<%for (int i = 0; i < listEngines.size(); i++) {
				Engine en = (Engine) listEngines.get(i);
				out.print("var engine_" + i + " = new Option('" + en.getName()
						+ "', '" + en.getId().toString() + "');\n");
				Integer biobjTypeId = en.getBiobjTypeId();
				Domain aDomain = DAOFactory.getDomainDAO().loadDomainById(
						biobjTypeId);
				out.print("if ('" + aDomain.getValueCd() + "' == type) {\n");
				out.print("	engines[engines.length] = engine_" + i + ";\n");
				if (obj.getEngine() != null) {
					out.print("	if ('" + en.getId().toString() + "' == '"
							+ obj.getEngine().getId().toString() + "') {\n");
					out.print("		document.getElementById('doc_engine').selectedIndex = engines.length -1;\n");
					//out.print("  alert("cosa sei tu "+document.getElementById('doc_engine').value);");			
					//out.print("  checkSourceVisibility(document.getElementById('doc_engine').value);");
					out.print("	}\n");
				}
				out.print("}\n");
			}%>
}

		engineSource = new Array();
		engineSet= new Array();
		engineDriver= new Array();
		
	<%for (int i = 0; i < listEngines.size(); i++) {
				Engine en = (Engine) listEngines.get(i);
				String labelEng = en.getLabel();
				Integer idEng = en.getId();
				boolean useDataSource = en.getUseDataSource();
				boolean useDataSet = en.getUseDataSet();
				
				String driver = en.getDriverName();
				%>
		engineSource[<%=idEng%>]=<%=useDataSource%>;
		engineSet[<%=idEng%>]=<%=useDataSet%>;
		engineDriver[<%=idEng%>]="<%=driver%>";

	<%}%>
		
	
function checkSourceVisibility(engineName) {
	var datasource = engineSource[engineName];
	var dataset = engineSet[engineName];;
	// hide template dynamic creation button for dossier and olap document 
	var datasourcecontainer = document.getElementById("datasourcecontainer");

	var datasetcontainer = document.getElementById("datasetcontainer");


	if(datasource=="1") {
		datasourcecontainer.style.display="inline";
	document.getElementById("doc_datasource").disabled=false;
	} else {
		datasourcecontainer.style.display="none";
	document.getElementById("doc_datasource").disabled=true;
	}

	if(dataset=="1") {
		datasetcontainer.style.display="inline";
		document.getElementById("dataset").disabled=false;

	} else {
		datasetcontainer.style.display="none";
		document.getElementById("dataset").disabled=true;
		
	}
	
 }

function checkFormVisibility(docType, engineValue) {
	if(!docType){
		docType = document.getElementById('doc_type').options[pos].value;
	}
	if(!engineValue){
		engineValue=document.getElementById('doc_engine').value;
	}
		
	var ind = docType.indexOf(",");
	var type = docType.substring(ind+1);
	// hide template dynamic creation button for dossier and olap document 
	var divLinkConf = document.getElementById("link_obj_conf");
	if((type=="OLAP" && !(engineDriver[engineValue].toLowerCase().indexOf("what")>-1))|| type=="DOSSIER" || type=="SMART_FILTER" || engineDriver[engineValue].toLowerCase().indexOf("chart.chartdriver")>-1) {
		divLinkConf.style.display="inline";
	} else {
		divLinkConf.style.display="none";
	}
	
}

function saveDocument(goBack) {

	var type = document.getElementById('doc_type').value;
	if (type.match('DOCUMENT_COMPOSITE') != null){
	    var message = "<%=msgBuilder.getMessage("1012",
					"component_spagobidocumentcompositionIE_messages", request)%>";
		if (versionTemplateChanged == 'true' || fileUploadChanged == 'true'){
			versionTemplateChanged = 'false';
			fileUploadChanged = 'false';
			if (confirm(message)) {
				document.getElementById('loadParsDC').name = 'loadParsDC';
				document.getElementById('loadParsDC').value = 'loadParsDC';
			}
		}		
	}
	
	if (goBack == 'true'){
	    document.getElementById('saveAndGoBack').name = 'saveAndGoBack';
		document.getElementById('saveAndGoBack').value = 'saveAndGoBack';		
	}
		
	document.objectForm.submit();
}

</script>

<form method='POST' action='<%=formUrl%>' id = 'objectForm' name='objectForm' enctype="multipart/form-data">

		<input type='hidden' name='PAGE' value='detailBIObjectPage' />
		<input type='hidden' name='<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>' value='true' />



<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBIDev.docConf.docDet.title" />
		</td>
		
		<%
					if (modality.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_MOD)) {
				%>
		<td class='header-button-column-portlet-section'>
			<input type="hidden" name="" value="" id="loadLinksLookup" />
			<a href='javascript:checkDocumentType("<spagobi:message key = "SBIDev.docConf.docDet.saveBeforeLinksConfig" />");'> 
			<img style="margin-top:2px;height:21px;" name='links' id='links' class='header-button-image-portlet-section'
				   src='<%=urlBuilder.getResourceLinkByTheme(request,
						"/img/links.jpg", currTheme)%>'
      		 title='<spagobi:message key = "SBIDev.docConf.docDet.linkButton" />' 
      		 alt='<spagobi:message key = "SBIDev.docConf.docDet.linkButton" />' />
			</a>
		</td>
		<%
			}
		%>
		<td class='header-button-column-portlet-section'>
		<input type="hidden" name="" value="" id="loadParsDC" />
			<a href='javascript:saveDocument("false");'> 
			<img name='save' id='save' class='header-button-image-portlet-section'
				   src='<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/save.png", currTheme)%>'
      		 title='<spagobi:message key = "SBIDev.docConf.docDet.saveButt" />' 
      		 alt='<spagobi:message key = "SBIDev.docConf.docDet.saveButt" />' />
			</a>
		<!-- 
			<input type='image' name='save' id='save' value='true' class='header-button-image-portlet-section'
				src='<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/save.png", currTheme)%>'
      				title='<spagobi:message key = "SBIDev.docConf.docDet.saveButt" />' alt='<spagobi:message key = "SBIDev.docConf.docDet.saveButt" />'
			/>
			-->
		</td>
		<td class='header-button-column-portlet-section'>
		<input type="hidden" name="" value="" id="saveAndGoBack" />
		<a href='javascript:saveDocument("true");'> 
			<img name='isaveAndGoBack' id='isaveAndGoBack' class='header-button-image-portlet-section'
				   src='<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/saveAndGoBack.png", currTheme)%>'
      		 title='<spagobi:message key = "SBIDev.docConf.docDet.saveAndGoBackButt" />' 
      		 alt='<spagobi:message key = "SBIDev.docConf.docDet.saveAndGoBackButt" />' />
			</a>
			<!-- 
			<input type='image' name='saveAndGoBack' id='saveAndGoBack' value='true' class='header-button-image-portlet-section'
				src='<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/saveAndGoBack.png", currTheme)%>'
      				title='<spagobi:message key = "SBIDev.docConf.docDet.saveAndGoBackButt" />' alt='<spagobi:message key = "SBIDev.docConf.docDet.saveAndGoBackButt" />'
			/> 
			-->
		</td>
		<td class='header-button-column-portlet-section'>
			<%
				if (modality.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_MOD)) {
			%>
				<a href='javascript:saveAndGoBackConfirm("<spagobi:message key = "SBIDev.docConf.docDet.saveConfirm" />","<%=backUrl%>")'> 
			<%
 				} else {
 			%>
				<a href='<%=backUrl%>'>
			<%
				}
			%>
      				<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIDev.docConf.docDet.backButt" />' 
      				src='<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/back.png", currTheme)%>' alt='<spagobi:message key = "SBIDev.docConf.docDet.backButt"/>' />
			</a>
		</td>
	</tr>
</table>

<spagobi:error/>

<input type='hidden' value='<%=obj.getId()%>' name='id' />
<input type='hidden' value='<%=modality%>' name='MESSAGEDET' />
<input type='hidden' value='' name='' id='saveBIObjectParameter'/>
	
<%-- if(modality.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_MOD)) { %>
	<input type='hidden' value='<%= obj.getPath() %>' name='<%= ObjectsTreeConstants.PATH %>' />
<% } --%>
	
<table width="100%" cellspacing="0" border="0" id = "fieldsTable" >
	<tr>
		<td>
			<div class="div_detail_area_forms" id='biobjectForm' name='biobjectForm'>
			
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.labelField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<input class='portlet-form-input-field' type="text" style='width:230px;' 
							name="label" id="doc_label" value="<%=StringEscapeUtils.escapeHtml(obj.getLabel())%>" maxlength="100" />
					&nbsp;*
				</div>
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.nameField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<input class='portlet-form-input-field' type="text" style='width:230px;' 
							name="name" id="doc_name" value="<%=StringEscapeUtils.escapeHtml(obj.getName())%>" maxlength="200" />
					&nbsp;*
				</div>
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key ="SBIDev.docConf.docDet.descriptionField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<%
						String desc = obj.getDescription();
						if (desc == null) {
							desc = "";
						}
					%>
					<input class='portlet-form-input-field' style='width:230px;' type="text" 
 							name="description" id="doc_description" value="<%=StringEscapeUtils.escapeHtml(desc)%>" maxlength="160" />
				</div>
				<div class='div_detail_label' style='display:none;'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.relNameField" />
					</span>
				</div>
				<div class='div_detail_form' style='display:none;'>
					<%
						String relName = obj.getRelName();
						if (relName == null) {
							relName = "";
						}
					%>
					<input class='portlet-form-input-field' style='width:230px;' type="text" 
							name="relname" id="doc_relname" value="<%=StringEscapeUtils.escapeHtml(relName)%>" maxlength="400" />
				</div>
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.typeField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<select class='portlet-form-input-field' style='width:230px;' 
							name="type" id="doc_type" 
							onchange = 'showEngField(this.value);checkFormVisibility(this.value);checkSourceVisibility(document.getElementById("doc_engine").value)'>
		      		<%
		      			Iterator iterdom = listTypes.iterator();
		      			while (iterdom.hasNext()) {
		      				Domain type = (Domain) iterdom.next();
		      				String BIobjTypecode = obj.getBiObjectTypeCode();
		      				String currTypecode = type.getValueCd();
		      				boolean isType = false;
		      				if (BIobjTypecode.equals(currTypecode)) {
		      					isType = true;
		      				}
		      		%>
		      			<option value="<%=type.getValueId() + "," + type.getValueCd()%>"<%if (isType)
					out.print(" selected='selected' ");%>><%=type.getTranslatedValueName(locale)%></option>
		      		<%
		      			}
		      		%>
		      		</select>
				</div>
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.engineField" />
					</span>
				</div>
				
			
				<div class='div_detail_form'>
		      		<select class='portlet-form-input-field' style='width:230px;' 
							name="engine" id="doc_engine" onchange = 'javascript:checkSourceVisibility(this.value);javascript:checkFormVisibility(null,this.value);' >
					<%
						boolean initialUseDataSet = false;
						boolean initialUseDataSource = false;

						Iterator itereng = listEngines.iterator();
						while (itereng.hasNext()) {
							Engine engine = (Engine) itereng.next();
							String objEngName = (obj.getEngine() != null ? obj.getEngine()
									.getName() : null);
							String currEngName = engine.getName();
							boolean isEngine = false;
							if ( //(!obj.getBiObjectTypeCode().equals("DATAMART")) && 
									//(!obj.getBiObjectTypeCode().equals("DASH")) && 
							(objEngName != null) && objEngName.equals(currEngName)) {
								isEngine = true;
								initialUseDataSet = (obj.getEngine()).getUseDataSet();
								initialUseDataSource = (obj.getEngine()).getUseDataSource();
							}
					%>
		      			<option value="<%=engine.getId().toString()%>"<%if (isEngine)
					out.print(" selected='selected' ");%>><%=engine.getName()%></option>
		      		<%
		      			}
		      			String styleSource = "style=\"display:none;\"";
		      			String disableSource = "disabled";
		      			if (initialUseDataSource == true) {
		      				styleSource = "style=\"display:inline;\"";
		      				disableSource = "";
		      			}

		      			String styleSet = "style=\"display:none;\"";
		      			String disableSet = "disabled";
		      			if (initialUseDataSet == true) {
		      				styleSet = "style=\"display:inline;\"";
		      				disableSet = "";
		      			}
		      		%>
		      		</select>
				</div> 
				
			<div id="datasourcecontainer" <%=styleSource%> >	
				
				<div class='div_detail_label' id="datasourceLabel" >
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBISet.eng.dataSource" />
					</span>
				</div>
				
			
				<div class='div_detail_form' id="datasourceForm" >
		      		<select class='portlet-form-input-field' style='width:230px;' 
							name="datasource" id="doc_datasource" <%=disableSource%> >
							<option></option>
					<%
						Iterator iterds = listDataSource.iterator();
						while (iterds.hasNext()) {
							DataSource ds = (DataSource) iterds.next();
							Integer objDsId = obj.getDataSourceId();
							Integer currDsId = new Integer(ds.getDsId());
							boolean isDs = false;
							if (objDsId != null && objDsId.equals(currDsId)) {
								isDs = true;
							}
					%>
		      			<option value="<%=String.valueOf(ds.getDsId())%>"<%if (isDs)
					out.print(" selected='selected' ");%>><%=ds.getLabel()%></option>
		      		<%
		      			}
		      		%>
		      		</select>
				</div>
			</div>
			
			<div id="datasetcontainer" <%=styleSet%> >	
				
				<div class='div_detail_label' id="datasetLabel" >
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBISet.eng.dataSet" />
					</span>
				</div>
				
			<%
								String url = GeneralUtilities.getSpagoBiHost()
															+ GeneralUtilities.getSpagoBiContext()
															+ GeneralUtilities.getSpagoAdapterHttpUrl() + "?"
															+ "PAGE=SelectDatasetLookupPage&NEW_SESSION=TRUE&"
															+ LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";

													String currDataSetLabel = "";
													Integer currDataSetId = null;
													String currDataSetIdValue = "";
													if (obj.getDataSetId() != null) {
														currDataSetId = obj.getDataSetId();
														currDataSetIdValue = currDataSetId.toString();
														IDataSetDAO dao = DAOFactory.getDataSetDAO();
														dao.setUserProfile(userProfile);								
														IDataSet dataSet = dao.loadDataSetById(currDataSetId);
														if (dataSet != null) {
															currDataSetLabel = dataSet.getLabel();
														}
													}
							%>
				<div class='div_detail_form' id="datasetForm" >
				  	<input type="hidden" name="dataset" id="dataset" value="<%=currDataSetIdValue%>" <%=disableSet%> />	
												
					<input class='portlet-form-input-field' style='width:230px;' type="text"  readonly="readonly"
									name="datasetReadLabel" id="datasetReadLabel" value="<%=StringEscapeUtils.escapeHtml(currDataSetLabel)%>" maxlength="400" /> *
				
					<a href='javascript:void(0);' id="datasetLink">
						<img src="<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/detail.gif", currTheme)%>" title="Lookup" alt="Lookup" />
					</a> 	
				</div>
			</div>
			
		<script>
			var win_dataset;
			Ext.get('datasetLink').on('click', function(){
			if(!win_dataset){
				win_dataset = new Ext.Window({
				id:'popup_dataset',
				title:'dataset',
				bodyCfg:{
					tag:'div', 
					cls:'x-panel-body', 
					children:[{tag:'iframe', 
								name: 'iframe_par_dataset',        			
								id  : 'iframe_par_dataset',        			
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
				win_dataset.show();
				}
				);
				
		</script>
			


			<!-- DISPLAY COMBO FOR STATE SELECTION -->
			<!-- IF THE USER IS A DEV ACTOR THE COMBO FOR THE STATE SELECTION CONTAINS ONLY A VALUE
     			 (development) SO IS NOT USEFUL TO SHOW IT	 -->
            <%
            	if (userProfile
            			.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_STATE_MANAGEMENT)) {
            %>
            	<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.stateField" />
					</span>
				</div>  
 				<div class='div_detail_form'>
					<select class='portlet-form-input-field' style='width:230px;' name="state" id="doc_state">
		      			<%
		      				Iterator iterstates = listStates.iterator();
		      					while (iterstates.hasNext()) {
		      						Domain state = (Domain) iterstates.next();
		      						String objState = obj.getStateCode();
		      						String currState = state.getValueCd();
		      						boolean isState = false;
		      						if (objState.equals(currState)) {
		      							isState = true;
		      						}
		      			%>
		      				<option value="<%=state.getValueId() + "," + state.getValueCd()%>"<%if (isState)
						out.print(" selected='selected' ");%>><%=state.getTranslatedValueName(locale)%></option>
		      			<%
		      				}
		      			%>
		      		</select>	
				</div>             	
            <%
             	            	} else {
             	            %>
	            <div class='div_detail_label' style='display:none;'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.stateField" />
						</span>
				</div>
				<div class='div_detail_form' style='display:none;'>
						<select class='portlet-form-input-field' style='width:230px;' name="state" id="doc_state"> 
			      			<%
 			      				Iterator iterstates = listStates.iterator();
 			      					while (iterstates.hasNext()) {
 			      						Domain state = (Domain) iterstates.next();
 			      						String objState = obj.getStateCode();
 			      						String currState = state.getValueCd();
 			      						boolean isState = false;
 			      						if (objState.equals(currState)) {
 			      							isState = true;
 			      						}
 			      						if (state.getValueCd().equalsIgnoreCase("DEV")) {
 			      			%>
			      						<option value="<%=state.getValueId() + "," + state.getValueCd()%>"<%if (isState)
							out.print(" selected='selected' ");%>><%=state.getTranslatedValueName(locale)%></option>
			      					<%
			      						}
			      							}
			      					%>
			      		  </select>  
				</div>  
		    <%
  		    	}
  		    %>
                <div class='div_detail_label' id="community" >
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBISet.eng.community" />
					</span>
				</div>
				
			
				<div class='div_detail_form' id="communityForm" >
		      		<select class='portlet-form-input-field' style='width:230px;' 
							name="community" id="doc_community"  >
							<option></option>
					<%
						Iterator iterComm = listCommunities.iterator();
						while (iterComm.hasNext()) {
							SbiCommunity comm = (SbiCommunity) iterComm.next();
							String functCd = comm.getFunctCode();
							String commName = comm.getName();
							LowFunctionality commFunction = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(functCd, false);
							Integer idCommFunct = commFunction.getId();
							boolean isComm = false;
							List functionalities = obj.getFunctionalities();
							for (Iterator it = functionalities.iterator(); it.hasNext(); ) {
								Integer functId = (Integer) it.next();
								
	      						if (idCommFunct.equals(functId)) {
	      							isComm = true;
	      						}
								
							}

					%>
		      			<option value="<%=functCd%>"<%if (isComm)
							out.print(" selected='selected' ");%>><%=commName%></option>
		      		<%
		      			}
		      		%>
		      		</select>
				</div>			
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key ="SBIDev.docConf.docDet.refreshField" />
					</span>
				</div>
				
				<div class='div_detail_form'>
				<%
					Integer refresh = obj.getRefreshSeconds();
					if (refresh == null) {
						refresh = new Integer(0);
					}
					if (userProfile
							.isAbleToExecuteAction(SpagoBIConstants.MODIFY_REFRESH)) {
				%>
					<input class='portlet-form-input-field' style='width:230px;' type="text" 
 							name="refreshseconds" id="doc_refresh" value="<%=StringEscapeUtils.escapeHtml(refresh.toString())%>" maxlength="160" />
						<%
							} else {
						%>
						<%=refresh%>
						<input type="hidden" name="refreshseconds" value="<%=StringEscapeUtils.escapeHtml(refresh.toString())%>"/>
					<%
						}
					%>
				</div>     
                   
                   
                   
                   
                   
                        
            <!-- DISPLAY RADIO BUTTON FOR CRYPT SELECTION -->
			<!-- FOR THE CURRENT RELEASE THIS RADIO IS HIDDEN -->
		    	<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.criptableField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<%
						boolean isCrypt = false;
						int cript = obj.getEncrypt().intValue();
						if (cript > 0) {
							isCrypt = true;
						}
					%> 
		      	   	<input type="radio" name="criptable" value="1" <%if (isCrypt) {
				out.println(" checked='checked' ");
			}%> >
							<span class="portlet-font">True</span>
					</input>
		      	   	<input type="radio" name="criptable" value="0" <%if (!isCrypt) {
				out.println(" checked='checked' ");
			}%> >
							<span class="portlet-font">False</span>
					</input>
				</div>


			<!-- DISPLAY RADIO BUTTON FOR VISIBLE SELECTION -->
		    	<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.visibleField" />
					</span>
				</div>
				<div class='div_detail_form'>
					<%
						boolean isVisible = false;

						int visible = (obj.getVisible() != null) ? obj.getVisible()
								.intValue() : 1;
						if (visible > 0) {
							isVisible = true;
						}
					%> 
				   	<input type="radio" name="visible" value="1" <%if (isVisible) {
				out.println(" checked='checked' ");
			}%>>
							<span class="portlet-font">True</span>
					</input>
		      		<input type="radio" name="visible" value="0" <%if (!isVisible) {
				out.println(" checked='checked' ");
			}%>>
							<span class="portlet-font">False</span>
					</input>
				</div>

				<!-- FIELD FOR VISIBILITY CONSTRAINTS USING USER PROFILE ATTRIBUTES -->
				<div class='div_detail_label'>
					<span class='portlet-form-field-label'>
						<spagobi:message key = "SBIDev.docConf.docDet.visibilityRulesField" />
					</span>
				</div>
				<%
					List attributeNames = null;
					try {
						ISecurityInfoProvider portalSecurityProvider = SecurityInfoProviderFactory
								.getPortalSecurityProvider();
						attributeNames = portalSecurityProvider
								.getAllProfileAttributesNames();
					} catch (Exception e) {
						logger.error(
								"detailBIObject.jsp: Error while retrieving the list of available profile attributes",
								e);
						attributeNames = new ArrayList();
					}
					String profiledVisibilityRules = obj.getProfiledVisibility();
				%>
				<div class='div_detail_form' style="height:100px">
					<table style="width: 50%;">
						<tr>
							<td colspan="2">
								<textarea id="profileVisibility" name="profileVisibility" rows="3" cols="35" readonly><%=profiledVisibilityRules != null ? profiledVisibilityRules
					: ""%></textarea>
							</td>
						</tr>
						<tr>
							<td>
								<select name="attributeName" id="attributeName" class='portlet-form-input-field' style='width:80px;'>
									<option value=""></option>
									<%
										Iterator attributeNamesIter = attributeNames.iterator();
										while (attributeNamesIter.hasNext()) {
											String profAttrName = (String) attributeNamesIter.next();
									%>
								 	<option value="<%=profAttrName%>"><%=profAttrName%></option>
								 	<%
								 		}
								 	%>
								</select>
								<span style="font-size: 7pt;">=</span>
								<input type="text" name="attributeValue" id="attributeValue" class='portlet-form-input-field' style='width:80px;' />
								<img src="<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/attach.gif", currTheme)%>" 
										alt="<spagobi:message key = "SBIDev.docConf.docDet.addRule" />" 
										title="<spagobi:message key = "SBIDev.docConf.docDet.addRule" />" 
										onclick="addConstraint()"/>
							</td>
							<td>
								<img src="<%=urlBuilder.getResourceLinkByTheme(request,
					"/img/clear16.gif", currTheme)%>" 
										alt="<spagobi:message key = "SBIDev.docConf.docDet.eraseRules" />" 
										title="<spagobi:message key = "SBIDev.docConf.docDet.eraseRules" />" 
										onclick="clearConstraints()"/>
							</td>
						</tr>
					</table>
				</div>
				<script>
				function addConstraint(){
					
					valore = document.getElementById('attributeValue').value;
					if (valore == null || valore == '') {
						alert('Missing value');
						return;
					}
					combo = document.getElementById('attributeName');
					attributo = combo.options[combo.selectedIndex].value;
					if (attributo == null || attributo == '') {
						alert('Missing profile attribute');
						return;
					}
					addToTextArea(attributo + ' = ' + valore);
				}
				
				function addToTextArea(constraint){
					str = document.getElementById('profileVisibility').innerHTML;
					if (str == '') str = constraint;
					else str = str + ' AND ' + constraint;
					document.getElementById('profileVisibility').innerHTML = str;
				}
				
				function clearConstraints(){
					document.getElementById('profileVisibility').innerHTML='';
				}
				</script>
				
				
				<%
				
                String deletePreviewFileActionURL = GeneralUtilities
                .getSpagoBIProfileBaseUrl(userUniqueIdentifier);
				
				String deletePreviewFileActionPars = "&ACTION_NAME=DELETE_PREVIEW_FILE_ACTION"
                + "&DOCUMENT_ID="+obj.getId()+"&"
                + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED
                + "=TRUE";

				%>
				
				 <!-- DISPLAY FORM FOR PREVIEW FILE  UPLOAD -->
				<div id="preview_upload">
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.previewField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<input class='portlet-form-input-field' type="file" 
			      		       name="previewFile" id="previewFile" onchange='fileToUploadInserted()' />

<% if(obj.getPreviewFile() != null) {%>

                    <a id="deletePreviewLink" href='javascript:void(0);'>
                        <img src="<%=urlBuilder.getResourceLinkByTheme(request,
                    "/img/delete.gif", currTheme)%>" title="Delete preview file" alt="Delete preview" />
                    </a> 		

<% } %>


					</div>
				</div>	

        <script>
        <% if(obj.getPreviewFile()!= null){ %>
            Ext.get('deletePreviewLink').on('click', function(){
                Ext.MessageBox.confirm(
           	       		"<%=msgBuilder.getMessage("SBISet.devObjects.confirmDeletePreviewTitle", locale)%>"
                        , "<%=msgBuilder.getMessage("SBISet.devObjects.confirmDeletePreviewDescr", locale)%>"
                        , function(btn, text) {
                        	if ( btn == 'yes' ) {
                                fileDeleted = true; 
                                document.getElementById('previewFile').value = '';
                        		Ext.Ajax.request({
                        	        url: '<%=deletePreviewFileActionURL%>',
                        	        method: 'post',
                        	        success: function (result, request) {
                        	            response = result.responseText || "";
                                        document.getElementById('deletePreviewLink').style.visibility = "hidden";

                        	            Ext.MessageBox.show({
                        	                title: "<spagobi:message key = 'SBIDev.docConf.docDet.deletedPreviewTitle' />",
                        	                msg: "<spagobi:message key = 'SBIDev.docConf.docDet.deletedPreviewMsg' />",
                        	                buttons: Ext.MessageBox.OK
                        	            });
                        	            
                        	            
                        	        },
                        	        params: '<%=deletePreviewFileActionPars%>',
                        	        failure: function (result, request) {
                                        response = result.responseText || "";

                                        Ext.MessageBox.show({
                                            title: "<spagobi:message key = 'SBIDev.docConf.docDet.notDeletedPreviewTitle' />",
                                            msg: "<spagobi:message key = 'SBIDev.docConf.docDet.notDeletedPreviewMsg' />",
                                            buttons: Ext.MessageBox.ERROR
                                        });
                                        
                                        
                                    }
                        	    });
                        	}
                        }
               		, this
                );
          	});
        <% }%>
            
        </script>



				<!-- DISPLAY FORM FOR TEMPLATE  UPLOAD -->
				<div id="form_upload">
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.templateField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<input class='portlet-form-input-field' type="file" 
			      		       name="uploadFile" id="uploadFile" onchange='fileToUploadInserted()' />
					</div>
				</div>	
				
				
				
			    <!-- TEMPLATE LABEL AND BUTTONS FOR DOSSIER AND OLAP -->
				 <%
			     String styleDivLinkConf = " ";
			     String BIobjTypecode = obj.getBiObjectTypeCode();
			     String EngineDriverClass = null;
			     if(obj!=null && obj.getEngine()!=null){
			     	EngineDriverClass = obj.getEngine().getDriverName();
			     }
			     
			     if (BIobjTypecode.equalsIgnoreCase("DOSSIER")
			       || (BIobjTypecode.equalsIgnoreCase("OLAP") && ! EngineDriverClass.equals("it.eng.spagobi.engines.drivers.whatif.WhatIfDriver"))
			       || BIobjTypecode.equalsIgnoreCase("SMART_FILTER")
			       || BIobjTypecode.equalsIgnoreCase("CHART"))
			     	styleDivLinkConf = " style='display:inline' ";
			     else
			     	styleDivLinkConf = " style='display:none' ";
			    %>
			    <!-- LINK FOR OBJECT CONFIGURATION -->
			    <div id="link_obj_conf" <%=styleDivLinkConf%>>
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.templateBuild" />
						</span>
					</div>
					<div class='div_detail_form'>
					<%
						boolean hasTemplates = false;
					
						List templates = objtempdao.getBIObjectTemplateList(obj.getId());
						if ((templates != null) && !templates.isEmpty()) {
							hasTemplates = true;
						}
						if (!hasTemplates) {
							String hrefConf = "";
							if (!modality.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_INS)) {
								Map confUrlPars = new HashMap();
								confUrlPars.put(SpagoBIConstants.PAGE,
										SpagoBIConstants.DOCUMENT_TEMPLATE_BUILD);
								confUrlPars.put(SpagoBIConstants.MESSAGEDET,
										SpagoBIConstants.NEW_DOCUMENT_TEMPLATE);
								confUrlPars.put(ObjectsTreeConstants.OBJECT_ID, obj.getId()
										.toString());
								hrefConf = urlBuilder.getUrl(request, confUrlPars);
											
								/*
									If the current document does not have dataset (user did not
									pick one), the warning will appear in the form of the browser
									popup with the message that warns about the necessity of having
									a dataset for the chart document.
									
									@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
								*/
								if (currDataSetIdValue.toString().equals(""))
								{
									hrefConf = "javascript:alert('"
											+ msgBuilder.getMessage(
													"sbi.detailbiobj.datasetNotChosen",
													"messages", request) + "')";
								}

							} else {
								
								hrefConf = "javascript:alert('"
										+ msgBuilder.getMessage(
												"sbi.detailbiobj.objectnotsaved",
												"messages", request) + "')";
							}
					%>		
									
							<a href="<%=hrefConf%>">
							
								<img class='header-button-image-portlet-section' 
	      				 			 title='<spagobi:message key = "sbi.detailbiobj.generateNewTemplate" />' 
	      				 			 src='<%=urlBuilder.getResourceLinkByTheme(request,
						"/img/createTemplate.jpg", currTheme)%>' 
	      				 			 alt='<spagobi:message key = "sbi.detailbiobj.generateNewTemplate"  />' />
							</a>
							
					<%
						} else { // if(!hasTemplates)
							
							Map editUrlPars = new HashMap();
							editUrlPars.put(SpagoBIConstants.PAGE,
									SpagoBIConstants.DOCUMENT_TEMPLATE_BUILD);
							editUrlPars.put(SpagoBIConstants.MESSAGEDET,
									SpagoBIConstants.EDIT_DOCUMENT_TEMPLATE);
							editUrlPars.put(ObjectsTreeConstants.OBJECT_ID, obj.getId()
									.toString());
							String editUrlStr = urlBuilder.getUrl(request, editUrlPars);
					%>
							<a href="<%=editUrlStr%>">
								<img class='header-button-image-portlet-section' 
	      				 			 title='<spagobi:message key = "sbi.detailbiobj.editTemplate" />' 
	      				 			 src='<%=urlBuilder.getResourceLinkByTheme(request,
						"/img/createTemplate.jpg", currTheme)%>' 
	      				 			 alt='<spagobi:message key = "sbi.detailbiobj.editTemplate"  />' />
							</a> 	
					<%
 							}
 						%>
					</div>
	        </div>

	        
	        <!-- engine list and template buttons adjustment based on the document type -->
	        <script>
	        	var pos = document.getElementById('doc_type').selectedIndex;
	        	typeValue = document.getElementById('doc_type').options[pos].value;
				showEngField(typeValue);
				engineValue=document.getElementById('doc_engine').value;
	        	checkFormVisibility(typeValue, engineValue);
	        	
	        	checkSourceVisibility(engineValue);	 						
			</script>
			
	</div>
	<!-- CLOSE COLUMN WITH DATA FORM  -->
	</td>
	
	
	
	<!-- OPEN COLUMN WITH TREE FUNCTIONALITIES (INSERT MODE) OR TEMPLATE VERSION (MODIFY MODE)  -->	     
	<td align="left">
		<div style='padding:5px;'>
			<a class="portlet-form-field-label" style="text-decoration:none;" 
			   onmouseover="this.style.color='#074BF8';"
			   onmouseout="this.style.color='#074B88';" 
			   href='javascript:void(0)' onclick='switchView()' id='switchView'>
				<spagobi:message key = "SBIDev.docConf.docDet.showTemplates" />
			</a>
		</div>

	<script>
	function switchView() {
		
		var treeView = document.getElementById('folderTree').style.display;
		
		if (treeView == 'inline') {
			document.getElementById('folderTree').style.display = 'none';
			document.getElementById('versionTable').style.display = 'inline';
			document.getElementById('switchView').innerHTML = '<spagobi:message key = "SBIDev.docConf.docDet.showFolderTree" />';
		}
		else {
			document.getElementById('folderTree').style.display = 'inline';
			document.getElementById('versionTable').style.display = 'none';
			document.getElementById('switchView').innerHTML = '<spagobi:message key = "SBIDev.docConf.docDet.showTemplates" />';
		}
	}
	</script>
	
    	<div style='padding:5px;display:inline;' id='folderTree'>
    		<spagobi:treeObjects moduleName="DetailBIObjectModule"  
    			 htmlGeneratorClass="it.eng.spagobi.analiticalmodel.functionalitytree.presentation.FunctionalitiesTreeInsertObjectHtmlGenerator" />    	
    	</div>
	
	
    	<div style='padding-left:5px;padding-right:5px;padding-bottom:5px;display:none;' id='versionTable'>
			<span class='portlet-form-field-label'>
				<spagobi:message key = "SBIDev.docConf.docDet.templateVersionField" />
			</span>
		<div style='border: 1px solid black;max-height:160px;overflow:auto;'>
			
			<table> 
				<%
 					templates = objtempdao.getBIObjectTemplateList(obj.getId());
 					ObjTemplate currTemplate = objtempdao.getBIObjectActiveTemplate(obj
 							.getId());
 					if (templates == null)
 						templates = new ArrayList();
 					Integer curVer = null;
 					if (currTemplate != null)
 						curVer = currTemplate.getProg();
 					int numTemp = templates.size();
 					if (numTemp == 0) {
 						out.print("<tr class='portlet-section-body'>");
 						out.print("<td class='portlet-font'>No Version Found</td></tr>");
 					}
 					// loads subReports id, if any
 					List subReports = DAOFactory.getSubreportDAO()
 							.loadSubreportsByMasterRptId(obj.getId());

 					Iterator iterTemp = templates.iterator();
 					while (iterTemp.hasNext()) {
 						ObjTemplate tempVer = (ObjTemplate) iterTemp.next();
 						String checkStr = " ";
 						boolean isCurrentVer = false;
 						if (curVer.equals(tempVer.getProg())) {
 							checkStr = " checked='checked' ";
 							isCurrentVer = true;
 						}
 						out.print("<tr class='portlet-section-body'>");
 						out.print("<td class='portlet-font'>Version "
 								+ tempVer.getProg() + "</td>");
 						Date creDate = tempVer.getCreationDate();
 						Calendar creCal = new GregorianCalendar();
 						creCal.setTime(creDate);
 						String creDateStr = creCal.get(Calendar.DAY_OF_MONTH) + "/"
 								+ (creCal.get(Calendar.MONTH) + 1) + "/"
 								+ creCal.get(Calendar.YEAR) + "  "
 								+ creCal.get(Calendar.HOUR_OF_DAY) + ":"
 								+ (creCal.get(Calendar.MINUTE) < 10 ? "0" : "")
 								+ creCal.get(Calendar.MINUTE);
 						out.print("<td class='portlet-font'>" + creDateStr + "</td>");
 						out.print("<td class='portlet-font'>" + tempVer.getName()
 								+ "</td>");

 						Map eraseVerUrlPars = new HashMap();
 						eraseVerUrlPars.put("PAGE", "detailBIObjectPage");
 						eraseVerUrlPars.put("MESSAGEDET",
 								SpagoBIConstants.ERASE_VERSION);
 						eraseVerUrlPars.put(SpagoBIConstants.TEMPLATE_ID,
 								tempVer.getId());
 						eraseVerUrlPars.put(AdmintoolsConstants.OBJECT_ID, obj.getId()
 								.toString());
 						eraseVerUrlPars
 								.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,
 										"true");
 						String eraseVerUrlStr = urlBuilder.getUrl(request,
 								eraseVerUrlPars);

 						String pathTemp = obj.getPath() + "/template";
 						//String downl = GeneralUtilities.getSpagoBIProfileBaseUrl(userId) + 
 						String downl = GeneralUtilities
 								.getSpagoBIProfileBaseUrl(userUniqueIdentifier)
 								+ "&ACTION_NAME=DOWNLOAD_BIOBJ_TEMPLATE&TEMP_ID="
 								+ tempVer.getId()
 								+ "&"
 								+ LightNavigationManager.LIGHT_NAVIGATOR_DISABLED
 								+ "=TRUE";

 						if (isCurrentVer) {
 							out.print("<td class='portlet-font' >&nbsp;</td>");
 						} else {
 							out.print("<td class='portlet-font' ><a href='javascript:deleteVersionConfirm(\""
 									+ msgBuilder
 											.getMessage(
 													"SBIDev.docConf.docDet.deleteVersionConfirm",
 													"messages", request)
 									+ "\", \""
 									+ eraseVerUrlStr
 									+ "\")' style='font-size:9px;' >"
 									+ msgBuilder.getMessage(
 											"SBIDev.docConf.execBIObject.eraseLink",
 											"messages", request) + "</a></td>");
 						}
 						if (!isCurrentVer || subReports == null
 								|| subReports.size() == 0) {
 							out.print("<td class='portlet-font' ><a href='"
 									+ downl
 									+ "' style='font-size:9px;' >"
 									+ msgBuilder.getMessage(
 											"SBIDev.docConf.execBIObject.downloadLink",
 											"messages", request) + "</a></td>");
 						} else {
 							//String downloadAlsoLinkedTemplateUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(userId)+ "&ACTION_NAME=DOWNLOAD_BIOBJ_TEMPLATE&operation=downloadAll&biobjectId=" +
 							String downloadAlsoLinkedTemplateUrl = GeneralUtilities
 									.getSpagoBIProfileBaseUrl(userUniqueIdentifier)
 									+ "&ACTION_NAME=DOWNLOAD_BIOBJ_TEMPLATE&operation=downloadAll&biobjectId="
 									+ obj.getId().toString()
 									+ "&"
 									+ LightNavigationManager.LIGHT_NAVIGATOR_DISABLED
 									+ "=TRUE&fileName=template.zip";
 							String downloadAlsoLinkedTemplateMsg = msgBuilder
 									.getMessage(
 											"SBIDev.docConf.docDet.downloadAlsoLinkedTemplates",
 											"messages", request);
 							out.print("<td class='portlet-font' ><a href='javascript:downloadAlsoLinkedTemplatesConfirm(\""
 									+ downloadAlsoLinkedTemplateMsg
 									+ "\",\""
 									+ downloadAlsoLinkedTemplateUrl
 									+ "\", \""
 									+ downl
 									+ "\")' style='font-size:9px;' >"
 									+ msgBuilder.getMessage(
 											"SBIDev.docConf.execBIObject.downloadLink",
 											"messages", request) + "</a></td>");
 						}

 						if (numTemp > 1) {
 							out.print("<td class='portlet-font'><input type='radio' value='"
 									+ tempVer.getId()
 									+ " 'name='versionTemplate' onchange='versionTemplateSelected()' "
 									+ checkStr + " /></td></tr>");
 						} else {
 							out.print("<td class='portlet-font'>&nbsp;</td></tr>");
 						}

 					}
 				%>    
		      	</table>

		</div>
	</div>

     	</td>
      </tr>
   </table>   <!-- CLOSE TABLE FORM ON LEFT AND VERSION ON RIGHT  -->

	


<%
		if (modality.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_INS)) {
	%>
</form>


<%
	} else if (modality
			.equalsIgnoreCase(ObjectsTreeConstants.DETAIL_MOD)) {
		BIObjectParameter objPar = (BIObjectParameter) moduleResponse
				.getAttribute(DetailBIObjectModule.NAME_ATTR_OBJECT_PAR);
%>

	<!--  <a style="margin: 0px 0px 5px 10px;" id="parDiv_" name="parDiv_" > 
		<span class='portlet-form-field-label'>
			<spagobi:message key="SBIDev.docConf.confPar"  />
		</span>
	</a> -->
	
<br>
<br>		

	
<div id="par_<%=obj.getId().toString()%>"  >

<div style='width:100%;visibility:visible;' class='UITabs' id='tabPanelWithJavascript' name='tabPanelWithJavascript'>
	<div class="first-tab-level" style="background-color:#f8f8f8">
		<div style="overflow: hidden; width:  100%">
			<input type='hidden' id='selected_obj_par_id' name='' value=''/>
<%
	List biObjParams = obj.getBiObjectParameters();
		String obj_par_idStr = (String) moduleResponse
				.getAttribute("selected_obj_par_id");
		Integer obj_par_idInt = new Integer(obj_par_idStr);
		int obj_par_id = Integer.parseInt(obj_par_idStr);
		String linkClass = "tab";
		boolean foundSelectedParId = false;
		for (int i = 0; i < biObjParams.size(); i++) {
			BIObjectParameter biObjPar = (BIObjectParameter) biObjParams
					.get(i);
			if (biObjPar.getId().equals(obj_par_idInt)) {
				linkClass = "tab selected";
				foundSelectedParId = true;
			} else
				linkClass = "tab";
%>
					<div class='<%=linkClass%>'>
						<a href='javascript:changeBIParameter("<%=biObjPar.getId().toString()%>", "<spagobi:message key = "SBIDev.docConf.docDetParam.saveBIParameterConfirm" />")'
						   style="color:black;"> 
							<%=StringEscapeUtils.escapeHtml(biObjPar.getLabel())%>
						</a>
					</div>
<%
	}
		if (obj_par_id < 0 || !foundSelectedParId)
			linkClass = "tab selected";
		else
			linkClass = "tab";
%>
					<div class='<%=linkClass%>'>
						<a href='javascript:changeBIParameter("-1", "<spagobi:message key = "SBIDev.docConf.docDetParam.saveBIParameterConfirm" />")'
						   style="color:black;"> 
							<spagobi:message key = "SBIDev.docConf.docDet.newBIParameter" />
					    </a>
					</div>
			
                <!-- Display combo to choose parameter region, among north or east  -->
                
                <%
                String region = obj.getParametersRegion();
                %>
                
 
                <div style='text-align:right;'>
                     <span class='portlet-form-field-label' style='line-height: 20px'>
                        <spagobi:message key = "SBIDev.docConf.docDet.parametersRegion" />
                    </span>
                    <select class='portlet-form-input-field' style='width:130px;' name="parametersRegion" id="doc_parameters_region">
    
                        <option value = "north" <%if (region != null && region.equalsIgnoreCase("north")) out.print(" selected='selected' ");%>> <%=msgBuilder.getMessage( "SBIDev.docConf.docDet.parametersRegion.north",locale)%> </option>
                        <option value = "east" <%if (region != null && region.equalsIgnoreCase("east")) out.print(" selected='selected' ");%>> <%=msgBuilder.getMessage( "SBIDev.docConf.docDet.parametersRegion.east",locale)%> </option>    
    
                    </select>   
                </div>
			
			
			</div>
		</div>
	</div>


<script>

<%BIObject initialBIObject = (BIObject) aSessionContainer
						.getAttribute("initial_BIObject");
				if (initialBIObject == null)
					initialBIObject = obj;
				BIObjectParameter initialBIObjectParameter = (BIObjectParameter) aSessionContainer
						.getAttribute("initial_BIObjectParameter");
				if (initialBIObjectParameter == null)
					initialBIObjectParameter = objPar;%>

function isBIObjectFormChanged() {
	
	var biobjFormModified = 'false';
	
	var label = document.getElementById('doc_label').value;
	var name = document.getElementById('doc_name').value;
	var description = document.getElementById('doc_description').value;	
	//var relName = document.getElementById('doc_relname').value;
	var type = document.getElementById('doc_type').value;
	var engine = document.getElementById('doc_engine').value;
	var datasource = document.getElementById('doc_datasource').value;
	//var dataset = document.getElementById('dataset').value;
	//var datasetLabel=document.getElementById('datasetLabel').value;
	var state = document.getElementById('doc_state').value;
    var parametersRegion = document.getElementById('doc_parameters_region').value;
  
	if ((label != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObject.getLabel()))%>')
		|| (name != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObject.getName()))%>')
		|| (description != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObject.getDescription()))%>')
		//|| (relName != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObject.getRelName()))%>')
		|| (type != '<%=initialBIObject.getBiObjectTypeID() + ","
						+ initialBIObject.getBiObjectTypeCode()%>')
		|| (engine != '<%=initialBIObject.getEngine().getId()%>')
		|| (datasource != '<%=initialBIObject.getDataSourceId() != null ? initialBIObject
						.getDataSourceId().toString() : ""%>')
		|| (state != '<%=initialBIObject.getStateID() + ","
						+ initialBIObject.getStateCode()%>') 
        || (parametersRegion != '<%=msgBuilder.getMessage(
        		                                   initialBIObject.getParametersRegion() != null ? initialBIObject.getParametersRegion() 
        		                                		   : "", locale)
        		                                		   %>') 
     	|| (versionTemplateChanged == 'true')
		|| (fileUploadChanged == 'true') 
		){
			
		biobjFormModified = 'true';
	}
	
	return biobjFormModified;
	
}

function isBIParameterFormChanged () {
	
	var biobjParFormModified = 'false';
	
	var objParLabel = document.getElementById('objParLabel').value;
	var par_Id = document.getElementById('par_Id').value;
	var parurl_nm = document.getElementById('parurl_nm').value;
	var view_fl = document.getElementById('view_fl').checked ? 1 : 0;
	
	var modes = document.getElementsByName('mult_fl');
	var mult_fl;
	for(var i = 0; i < modes.length; i++){
	    if(modes[i].checked){
	    	mult_fl = modes[i].value;
	    }
	}

	var reqs = document.getElementsByName('req_fl');
	var req_fl;
	for(var i = 0; i < reqs.length; i++){
		if(reqs[i].checked){
	        req_fl = reqs[i].value;
	    }
	 }
	    
    var priorityV = document.getElementsByName('priority');
    if(!priorityV.selectedIndex){
        priorityV = priorityV[0];	
    }
    var priorityIndex = priorityV.selectedIndex;
    var priorityOption = priorityV.options;
    var priority = priorityOption[priorityIndex].value;

    var colspanV = document.getElementsByName('colspan');
    
    var colspan = false;
    if(colspanV){
        if(colspanV.selectedIndex == undefined){
        	   colspanV = colspanV[0];   
        	    }
        if(colspanV && colspanV.selectedIndex != undefined){
            var colspanIndex = colspanV.selectedIndex;
            var colspanOption = colspanV.options;
            colspan = colspanOption[colspanIndex].value;
        }
    }
    
    var sliderThick = false;
    var sliderV = document.getElementById('doc_thickPerc');
    if(sliderV){
    	sliderThick = sliderV.value;
    }
    
    
	if ((objParLabel != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObjectParameter.getLabel()))%>')
		|| (par_Id != '<%=(initialBIObjectParameter.getParID() == null || initialBIObjectParameter
						.getParID().intValue() == -1) ? ""
						: initialBIObjectParameter.getParID().toString()%>')
		|| (parurl_nm != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
						.escapeHtml(initialBIObjectParameter
								.getParameterUrlName()))%>') 
		|| (view_fl != '<%=initialBIObjectParameter.getVisible()%>') 
        || (mult_fl != '<%=initialBIObjectParameter.isMultivalue() ? 1 : 0%>') 	
        || (req_fl != '<%=initialBIObjectParameter.isRequired() ? 1 : 0%>')  
        || (priority != '<%=initialBIObjectParameter.getPriority()%>')  
        || (colspan != false && colspan != '<%=initialBIObjectParameter.getColSpan()%>')  
        || (sliderThick != false && sliderThick != '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils
            .escapeHtml(initialBIObjectParameter.getThickPerc() != null ?  initialBIObjectParameter.getThickPerc().toString() : "0"))%>') 
	)
	{
		biobjParFormModified = 'true';
	}
	
	return biobjParFormModified;
	
}

function changeBIParameter (objParId, message) {
	
	var biobjParFormModified = isBIParameterFormChanged();
	
	document.getElementById('selected_obj_par_id').name = 'selected_obj_par_id';
	document.getElementById('selected_obj_par_id').value = objParId;
	
	if (biobjParFormModified == 'true') 
	{
		if (confirm(message))
		{
			document.getElementById('saveBIObjectParameter').name = 'saveBIObjectParameter';
			document.getElementById('saveBIObjectParameter').value= 'yes';
		}
		else
		{
			document.getElementById('saveBIObjectParameter').name = 'saveBIObjectParameter';
			document.getElementById('saveBIObjectParameter').value= 'no';
		}
	}
	
	document.getElementById('objectForm').submit();
}

function saveAndGoBackConfirm(message, url){
	
		var biobjFormModified = isBIObjectFormChanged();
		var biobjParFormModified = isBIParameterFormChanged();
		if (biobjFormModified == 'true' || biobjParFormModified == 'true') {
			if (confirm(message)) {
				
				document.getElementById('saveAndGoBack').click();
			} else {
				location.href = url;
			}
		} else {
			location.href = url;
		}
}

function deleteVersionConfirm(message, url){
	if (confirm(message)){
            location.href = url;
        }
}

function deleteBIParameterConfirm (message) {
	if (confirm(message)) {
		document.getElementById('deleteBIObjectParameter').name = 'deleteBIObjectParameter';
		document.getElementById('deleteBIObjectParameter').value = '<%=objPar.getId()%>';
        	document.getElementById('objectForm').submit();
        }
}

function verifyDependencies() {
	
	<%List correlations = DAOFactory.getObjParuseDAO()
						.loadObjParuses(objPar.getId());
				if (correlations != null && correlations.size() > 0) {%>
		document.getElementById('parameterCannotBeChanged').style.display = 'inline';
		<%} else {%>
		document.getElementById('loadParametersLookup').name = 'loadParametersLookup';
		document.getElementById('loadParametersLookup').value = 'loadParametersLookup';
		//document.getElementById('save').click();
		document.objectForm.submit();
		
		<%}%>
}

function saveBIParameterConfirm (message) {
	
	var biobjParFormModified = isBIParameterFormChanged();

	if (biobjParFormModified == 'true') 
	{
		if (confirm(message))
		{
			document.getElementById('saveBIObjectParameter').name = 'saveBIObjectParameter';
			document.getElementById('saveBIObjectParameter').value= 'yes';
		}
		else
		{
			document.getElementById('saveBIObjectParameter').name = 'saveBIObjectParameter';
			document.getElementById('saveBIObjectParameter').value= 'no';
		}
	}
	
	document.getElementById('goToDependenciesPage').name = 'goToDependenciesPage';
	document.getElementById('goToDependenciesPage').value= 'goToDependenciesPage';
	document.objectForm.submit();
	//document.getElementById('save').click();
}

function checkDocumentType(message) {
	
	var type = document.getElementById('doc_type').value;
	if (type.match('REPORT') != null || type.match('DATA_MINING') != null) {
		var biobjFormModified = isBIObjectFormChanged();
		var biobjParFormModified = isBIParameterFormChanged();
		if (biobjFormModified == 'true' || biobjParFormModified == 'true') {
			if (confirm(message)) {
				document.getElementById('loadLinksLookup').name = 'loadLinksLookup';
				document.getElementById('loadLinksLookup').value = 'loadLinksLookup';
				//document.getElementById('save').click();
				document.objectForm.submit();
			}
		} else {
			document.getElementById('loadLinksLookup').name = 'loadLinksLookup';
			document.getElementById('loadLinksLookup').value = 'loadLinksLookup';
			
			//document.getElementById('save').click();
			document.objectForm.submit();
		}
	} else {
		alert('<spagobi:message key = "SBIDev.docConf.docDet.noPermissibleLinks" />');
	}
}

function downloadAlsoLinkedTemplatesConfirm(message, urlYes, urlNo){
	if (confirm(message)){
		location.href = urlYes;
    } else {
    	location.href = urlNo;
    }
}
</script>

		      
<table  class='header-sub-table-portlet-section' >		
	<tr class='header-sub-row-portlet-section'>
		<%
			if (obj_par_id != -1) {
		%>
		<td class='header-sub-title-column-portlet-section'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<%
			int objParPriority = objPar.getPriority().intValue();
					if (objParPriority > 1) {
		%>
		<td class='header-button-column-portlet-section'>
			<a href='javascript:void(0);'
			   onclick='document.getElementById("priority").selectedIndex=<%=objParPriority - 2%>;document.getElementById("objectForm").submit();'>
				<img 	src= '<%=urlBuilder.getResourceLinkByTheme(request,
								"/img/Back.gif", currTheme)%>'
					title='<spagobi:message key = "SBIDev.docConf.docDetParam.increasePriority" />' 
					alt='<spagobi:message key = "SBIDev.docConf.docDetParam.increasePriority" />'
				/>
			</a>
		</td>
		<%
			}
		%>
		<%
			if (objParPriority < biObjParams.size()) {
		%>
		<td class='header-button-column-portlet-section'>
			<a href='javascript:void(0);' 
			   onclick='document.getElementById("priority").selectedIndex=<%=objParPriority%>;document.getElementById("objectForm").submit();'>
				<img 	src= '<%=urlBuilder.getResourceLinkByTheme(request,
								"/img/Forward.gif", currTheme)%>'
					title='<spagobi:message key = "SBIDev.docConf.docDetParam.reducePriority" />' 
					alt='<spagobi:message key = "SBIDev.docConf.docDetParam.reducePriority" />'
				/>
			</a>
		</td>
		<%
			}
		%>
		<%
			if (biObjParams != null && biObjParams.size() > 1) {
		%>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:saveBIParameterConfirm("<spagobi:message key="SBIDev.docConf.docDetParam.saveBIParameterConfirm"/>")'>
					<img 	src= '<%=urlBuilder.getResourceLinkByTheme(request,
								"/img/Class.gif", currTheme)%>'
						title='<spagobi:message key = "SBIDev.docConf.docDetParam.parametersCorrelationManagement" />'
						alt='<spagobi:message key = "SBIDev.docConf.docDetParam.parametersCorrelationManagement" />'
					/>
				</a>
			</td>
		<%
			}
		%>
		<td class='header-button-column-portlet-section'>
			<a href='javascript:deleteBIParameterConfirm("<spagobi:message key="SBIDev.docConf.docDetParam.deleteBIParameterConfirm"/>")'>
				<img 	src= '<%=urlBuilder.getResourceLinkByTheme(request,
							"/img/erase.gif", currTheme)%>'
					title='<spagobi:message key = "SBIDev.docConf.docDetParam.eraseButt" />' alt='<spagobi:message key = "SBIDev.docConf.docDetParam.eraseButt" />'
				/>
			</a>
		</td>
		<%
			} else {
		%>
			<td class='header-sub-title-column-portlet-section-no-buttons'>
				<spagobi:message key = "SBIDev.docConf.docDetParam.title" />
			</td>
		<%
			}
		%>
	</tr>
</table>



<input type='hidden' name='objParId' value='<%=objPar.getId() != null ? objPar.getId().toString()
						: "-1"%>' />
<input type='hidden' name='' value='' id='deleteBIObjectParameter' />
<input type='hidden' name='' value='' id='goToDependenciesPage' />
		

<!-- ======================================================================================  -->
<!-- PARAMETERS PANEL  																		 -->
<!-- ======================================================================================  -->
<div class="div_detail_area_sub_forms">
	
	<!-- TITLE -->
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.labelField" />
		</span>
	</div>
	<div class='div_detail_form'>
		<input class='portlet-form-input-field' type="text" name="objParLabel" 
			   id="objParLabel" size="42" value="<%=StringEscapeUtils.escapeHtml(objPar.getLabel())%>" maxlength="40" />
		&nbsp;*
	</div>
	
	<!-- ANALTYCAL DRIVER -->
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.paramField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<%
		IParameterDAO param = DAOFactory.getParameterDAO();
			String objParId = (objPar.getParameter() != null ? objPar
					.getParameter().getId().toString() : null);
			List list = param.loadAllParameters();
			Parameter parameter = null;
			for (int i = 0; i < list.size(); i++) {
				Parameter parameterTemp = (Parameter) list.get(i);
				String curr_parId = parameterTemp.getId().toString();
				if (curr_parId.equals(objParId)) {
					parameter = parameterTemp;
					break;
				}
			}
	%> 
    <input type='hidden' id='par_Id' 
			   value='<%=parameter != null ? parameter.getId().toString() : ""%>' name='par_Id' />	 
		<input class='portlet-form-input-field' type="text" id="parameterName" size="42" 
	    	   name="parameterName" value='<%=parameter != null ? StringEscapeUtils
						.escapeHtml(parameter.getName()) : ""%>' 
			   	maxlength="100" readonly />


  		&nbsp;*&nbsp;
		<a style="text-decoration:none;"  href="javascript:verifyDependencies();">
			<img src='<%=urlBuilder.getResourceLinkByTheme(request,
						"/img/detail.gif", currTheme)%>' 
			   title='<spagobi:message key = "SBIDev.docConf.docDetParam.parametersLookupList" />' 
			   alt='<spagobi:message key = "SBIDev.docConf.docDetParam.parametersLookupList" />' />
		</a>
			   
		<input type='hidden' name='' value='' id='loadParametersLookup' />
	</div>
	
	
	<!-- URL NAME -->
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.parurl_nmField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<%
		String urlName = objPar.getParameterUrlName();
			if (urlName == null) {
				urlName = "";
			}
	%>
			<input class='portlet-form-input-field' type="text" size="42"
				name="parurl_nm" id="parurl_nm"
				value="<%=StringEscapeUtils.escapeHtml(urlName)%>" maxlength="20" />&nbsp;&nbsp;*
	</div>
	
	<!-- PRIORITY -->
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.priorityField" />
		</span>
	</div>
	<div class='div_detail_form'>
		<select class='portlet-form-input-field' name="priority" id="priority">
		<%
			int objParsnumber = biObjParams.size();
				for (int i = 0; i < objParsnumber; i++) {
		%>
				<option value="<%=i + 1%>" <%if (objPar.getPriority() != null
							&& objPar.getPriority().intValue() == i + 1)
						out.print(" selected='selected' ");%> ><%=i + 1%></option>
				<%
					}
						if (obj_par_id < 0) {
				%>
				<option value="<%=objParsnumber + 1%>" selected="selected"><%=objParsnumber + 1%></option>
				<%
					} else
				%>
		</select>
	</div>
	
	<!-- VISIBLE -->
	<div class='div_detail_label'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.view_flField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<%
		isVisible = false;
			visible = objPar.getVisible().intValue();
			if (visible > 0) {
				isVisible = true;
			}
	%> 
		<input class='portlet-form-input-field' type="checkbox" name="view_fl" id="view_fl" 
			   value="1" <%=(isVisible ? "checked='checked'" : "")%>/>
	</div>
	
	<!-- MANDATORY -->
	<div class='div_detail_label' >
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.req_flField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<%
		boolean isRequired = false;
			int required = objPar.getRequired().intValue();
			if (required > 0) {
				isRequired = true;
			}
	%> 
    	<input type="radio" name="req_fl" value="1" <%if (isRequired) {
					out.println(" checked='checked' ");
				}%> >True</input>
    	<input type="radio" name="req_fl" value="0" <%if (!isRequired) {
					out.println(" checked='checked' ");
				}%> >False</input>
	</div>
	
	<!-- MULTISELECTION  -->
	<div class='div_detail_label' >
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.mult_flField" />
		</span>
	</div>
	<div class='div_detail_form'>
	<%
		boolean isMultivalue = objPar.isMultivalue();
		
	%> 
    	<input type="radio" name="mult_fl" value="1" <%if (isMultivalue) {
					out.println(" checked='checked' ");
				}%> >True</input>
    	<input type="radio" name="mult_fl" value="0" <%if (!isMultivalue) {
					out.println(" checked='checked' ");
				}%> >False</input>
	</div>
	
<% 
// if parameters is of type slider or combo two more settings are proposed to user



ParameterUse parUse = null;
boolean isSlider = false;
boolean isCombo = false;

// in case of new Bi Obj parameter is not slider
if(objPar.getParID() == -1)  {
	isSlider = false;
    isCombo = false;
}
else{
   for(int i = 0; i<userProfile.getRoles().size() && (isSlider == false || isCombo == false); i++){
    String role = (String)((ArrayList)userProfile.getRoles()).get(i);
    // check if adding slider configurations
    
   ParameterUse use = DAOFactory.getParameterUseDAO().loadByParameterIdandRole(objPar.getParID(), role);
   if(use != null){
       String selType = use.getSelectionType();
       if(selType != null && (selType.equalsIgnoreCase("SLIDER")) ){
    	    isSlider = true;
    	    }
       if(selType != null && (selType.equalsIgnoreCase("COMBOBOX")) ){
           isCombo = true;
           }

    }
   }
}

// For slider and combo
if(isSlider || isCombo){
    ArrayList roles = (ArrayList)userProfile.getRoles();
    Integer colspan = objPar.getColSpan() != null ? objPar.getColSpan() : 1;
    Integer thickPerc = objPar.getThickPerc() != null ? objPar.getThickPerc() : 0;
%>
   <div class='div_detail_label'>
        <span class='portlet-form-field-label' title="My tip">
            <spagobi:message key = "SBIDev.docConf.docDet.colSpan" />
        </span>
   </div>
     <div class='div_detail_form'>
        <select class='portlet-form-input-field' style='width:130px;' name="colspan" id="doc_colspan">
            <option value = "1" <%if (colspan == 1) out.print(" selected='selected' ");%>> 1 </option>
            <option value = "2" <%if (colspan == 2) out.print(" selected='selected' ");%>> 2 </option>    
            <option value = "3" <%if (colspan == 3) out.print(" selected='selected' ");%>> 3 </option>    
        </select>   

        <img src="<%=urlBuilder.getResourceLinkByTheme(request,
                		  "/img/info16.png", currTheme)%>" title='<spagobi:message key = "SBIDev.docConf.docDet.colSpanInfo" />'  alt="Colspan" />
    </div>
<% // only for slider
if(isSlider){
%>
   <div class='div_detail_label'>
        <span class='portlet-form-field-label'>
            <spagobi:message key = "SBIDev.docConf.docDet.thickPerc" />
        </span>
   </div>
     <div class='div_detail_form'>
        <input type="text" value="<%=thickPerc%>" style="width:130px;"  name="thickPerc" id="doc_thickPerc" />
    
            <img src="<%=urlBuilder.getResourceLinkByTheme(request,
                          "/img/info16.png", currTheme)%>" title='<spagobi:message key = "SBIDev.docConf.docDet.thickPercInfo" />' alt="ThickPercentage" />
    
    </div>
<% 
} // END SLIDER CASE

} // END SLIDER OR COMBO CASE

// END SLIDER CONFIGURATION
%>




	<!-- deprecati ... -->
	<div class='div_detail_label' style='display:none;'>
		<span class='portlet-form-field-label'>
			<spagobi:message key = "SBIDev.docConf.docDetParam.mod_flField" />
		</span>
	</div>
	<div class='div_detail_form' style='display:none;'>
	<%
		boolean isModifiable = false;
			int modifiable = objPar.getModifiable().intValue();
			if (modifiable > 0) {
				isModifiable = true;
			}
	%> 
    	<input type="radio" name="mod_fl" value="1" <%if (isModifiable) {
					out.println(" checked='checked' ");
				}%> >True</input>
    	<input type="radio" name="mod_fl" value="0" <%if (!isModifiable) {
					out.println(" checked='checked' ");
				}%> >False</input>
	</div>
</div>

</div>

</div>	





</form>


<%
	}
%>

<div id = 'parameterCannotBeChanged' class='portlet-msg-error' style='display:none;'>
	<spagobi:message key = "SBIDev.docConf.docDetParam.cannotChangeParameter" />
</div>

