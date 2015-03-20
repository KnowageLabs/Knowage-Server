<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,it.eng.spagobi.tools.importexport.ImportExportConstants,java.util.List,java.util.Map,java.util.Set,java.util.Iterator,it.eng.spagobi.tools.importexport.*,it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov,it.eng.spagobi.commons.metadata.SbiExtRoles,it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects,it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters,it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions,it.eng.spagobi.engines.config.metadata.SbiEngines,it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks,it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse,it.eng.spagobi.tools.importexport.IImportManager" %>
<%@page import="java.util.HashMap"%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>


<%!

		private String cutLabel(String label) {
				if (label == null) return null;
				if(label.length() > 38) {
					label = label.substring(0, 38);
					label += " ...";
				}
				return label;
		}

%>

<%  
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("ImportExportModule"); 
	IImportManager impMan = (IImportManager)aSessionContainer.getAttribute(ImportExportConstants.IMPORT_MANAGER);
    MetadataAssociations metaAss = impMan.getMetadataAssociation();
	
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", "ImportExportPage");
    backUrlPars.put("MESSAGEDET", ImportExportConstants.IMPEXP_BACK_METADATA_ASS);
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);
    
    Map exitUrlPars = new HashMap();
    exitUrlPars.put("PAGE", "ImportExportPage");
    exitUrlPars.put("MESSAGEDET", ImportExportConstants.IMPEXP_EXIT);
    exitUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String exitUrl = urlBuilder.getUrl(request, exitUrlPars);

    Map formUrlPars = new HashMap();
    formUrlPars.put("PAGE", "ImportExportPage");
    formUrlPars.put("MESSAGEDET", ImportExportConstants.IMPEXP_METADATA_ASS);
    formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    String formUrl = urlBuilder.getUrl(request, formUrlPars);
   
%>

<script>

	var infopanelopen = false;
	var winInfo = null;
	
	function opencloseInfoPanel() {
		if(!infopanelopen){
			infopanelopen = true;
		 	openInfo();
		 }
	}
	
	function openInfo(){
		if(winInfo==null) {
		 	winInfo = new Window('winInfo', {className: "alphacube", title:"<spagobi:message key="help"  bundle="messages"/>", width:680, height:150, destroyOnClose: false});
		 	winInfo.setContent('infodiv', false, false);
		 	winInfo.showCenter(true);
		 } else {
			winInfo.showCenter(true);
		 }
	}
	
	observerWInfo = { onClose: function(eventName, win) {
			if (win == winInfo) {
			 	infopanelopen = false;
			 }
		}
	}
	
	Windows.addObserver(observerWInfo);

</script>




<div id='infodiv' style='display:none;'>
	<ul style="color:#074B88;">
			<li><spagobi:message key = "SBISet.impexp.metadatarule1"  bundle="component_impexp_messages"/></li>
			<li><spagobi:message key = "SBISet.impexp.metadatarule2"  bundle="component_impexp_messages"/></li>
			<li><spagobi:message key = "SBISet.impexp.metadatarule3"  bundle="component_impexp_messages"/></li>
			<li><spagobi:message key = "SBISet.impexp.metadatarule4"  bundle="component_impexp_messages"/></li>
	</ul>
</div>	





<form method='POST' action='<%=formUrl%>' id='connAssForm' name='connAssForm'>

	<table class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key = "SBISet.metadataConflicts"  bundle="component_impexp_messages"/>
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:opencloseInfoPanel()'> 
			    	<img class='header-button-image-portlet-section' 
			    		title='<spagobi:message key="help"  bundle="messages"/>' 
			    		src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/question32.gif", currTheme)%>' 
			    		alt='<spagobi:message key="help"  bundle="messages"/>' />
				</a>
			</td>		
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='javascript:document.getElementById("connAssForm").submit()'> 
			    	<img class='header-button-image-portlet-section' 
			    		title='<spagobi:message key="Sbi.next"  bundle="component_impexp_messages"/>' 
			    		src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/next.gif", currTheme)%>' 
			    		alt='<spagobi:message key="Sbi.next"  bundle="component_impexp_messages"/>' />
				</a>
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='<%=backUrl%>'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "Sbi.back"  bundle="component_impexp_messages"/>' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "Sbi.back"  bundle="component_impexp_messages"/>' />
				</a>
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href='<%=exitUrl%>'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "Sbi.exit"  bundle="component_impexp_messages"/>' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/stop.gif", currTheme)%>' 
	      				 alt='<spagobi:message key = "Sbi.exit"  bundle="component_impexp_messages"/>' />
				</a>
			</td>
		</tr>
	</table>


	<spagobi:message key = "impexp.overwrite"  bundle="component_impexp_messages"/>: 
	<select name="overwrite">
		<option value="false">
			<spagobi:message key="impexp.overwrite.no" bundle="component_impexp_messages"/>
		</option>
		<option value="true">
			<spagobi:message key="impexp.overwrite.yes" bundle="component_impexp_messages"/>
		</option>
	</select>


	<div class="div_background_no_img">
		<div class="box padding5" >
			<%if(!metaAss.getLovIDAssociation().keySet().isEmpty()) { %>
			<table>
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.lovs"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key = "SBISet.impexp.exportedLovs"  bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key = "SBISet.impexp.currentLovs"  bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map lovsAss = metaAss.getLovAssociation();
					Set lovsExp = lovsAss.keySet();
				    Iterator iterExp =  lovsExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiLov lovExp = (SbiLov)iterExp.next();
				    	SbiLov lovExist = (SbiLov)lovsAss.get(lovExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=lovExp.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"> <%=lovExp.getName()%> </div>
						<div style="width:100%;overflow:auto;font-size:12px;"> <%=lovExp.getDescr()%> </div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=lovExist.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=lovExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=lovExist.getDescr()%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getFunctIDAssociation().keySet().isEmpty()) { %>
			<table >
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.functionalities"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedFunctionalities"  bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentFunctionalities"  bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map functsAss = metaAss.getFunctAssociation();
					Set functsExp = functsAss.keySet();
				    Iterator iterExp =  functsExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiFunctions functExp = (SbiFunctions)iterExp.next();
				    	SbiFunctions functExist = (SbiFunctions)functsAss.get(functExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=functExp.getCode()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=functExp.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=functExp.getDescr()%></div>
						<div alt="<%=functExist.getPath()%>" title="<%=functExist.getPath()%>"  style="width:100%;overflow:auto;font-size:12px;"><%=cutLabel(functExp.getPath())%></div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=functExist.getCode()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=functExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=functExist.getDescr()%></div>
						<div alt="<%=functExist.getPath()%>" title="<%=functExist.getPath()%>" style="width:100%;overflow:auto;font-size:12px;"><%=cutLabel(functExist.getPath())%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getEngineIDAssociation().keySet().isEmpty()) { %>
			<table>
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.engines"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedEngines"  bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentEngines" bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map engsAss = metaAss.getEngineAssociation();
					Set engsExp = engsAss.keySet();
				    Iterator iterExp =  engsExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiEngines engExp = (SbiEngines)iterExp.next();
				    	SbiEngines engExist = (SbiEngines)engsAss.get(engExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=engExp.getLabel()%></span><br/>
						<div alt="<%=engExist.getName()%>" style="width:100%;overflow:auto;font-size:12px;" title="<%=engExist.getName()%>" ><%=engExp.getName() != null ? cutLabel(engExp.getName()) : ""%></div>
						<div alt="<%=engExp.getDescr()%>" title="<%=engExp.getDescr()%>" style="width:100%;overflow:auto;font-size:12px;"><%=engExp.getDescr() != null ? cutLabel(engExp.getDescr()) : ""%></div>
						<div alt="<%=engExist.getMainUrl()%>" title="<%=engExist.getMainUrl()%>" style="width:100%;overflow:auto;font-size:12px;"><%=engExp.getMainUrl() != null ? cutLabel(engExp.getMainUrl()) : ""%></div>
					</td>
					<td style="overflow:hidden;">
						<span class='portlet-form-field-label'><%=engExist.getLabel()%></span><br/>
						<div alt="<%=engExist.getName()%>" style="width:100%;overflow:auto;font-size:12px;" title="<%=engExist.getName()%>" ><%=engExist.getName() != null ? cutLabel(engExist.getName()) : ""%></div>
						<div alt="<%=engExp.getDescr()%>" title="<%=engExp.getDescr()%>" style="width:100%;overflow:auto;font-size:12px;"><%=engExist.getDescr() != null ? cutLabel(engExist.getDescr()) : ""%></div>
						<div alt="<%=engExist.getMainUrl()%>" title="<%=engExist.getMainUrl()%>" style="width:100%;overflow:auto;font-size:12px;"><%=engExist.getMainUrl() != null ? cutLabel(engExist.getMainUrl()) : ""%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getCheckIDAssociation().keySet().isEmpty()) { %>
			<table >
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.checks"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedChecks" bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentChecks" bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map checksAss = metaAss.getCheckAssociation();
					Set checksExp = checksAss.keySet();
				    Iterator iterExp =  checksExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiChecks checkExp = (SbiChecks)iterExp.next();
				    	SbiChecks checkExist = (SbiChecks)checksAss.get(checkExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=checkExp.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=checkExp.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=checkExp.getDescr()%></div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=checkExist.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=checkExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=checkExist.getDescr()%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getParameterIDAssociation().keySet().isEmpty()) { %>
			<table >
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.parameters"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedParameters" bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentParameters" bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map paramsAss = metaAss.getParameterAssociation();
					Set paramsExp = paramsAss.keySet();
				    Iterator iterExp =  paramsExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiParameters paramExp = (SbiParameters)iterExp.next();
				    	SbiParameters paramExist = (SbiParameters)paramsAss.get(paramExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=paramExp.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paramExp.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paramExp.getDescr()%></div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=paramExist.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paramExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paramExist.getDescr()%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getParuseIDAssociation().keySet().isEmpty()) { %>
			<table >
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.paruses"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedParuses"  bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentParuses"  bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map parusesAss = metaAss.getParuseAssociation();
					Set parusesExp = parusesAss.keySet();
				    Iterator iterExp =  parusesExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiParuse paruseExp = (SbiParuse)iterExp.next();
				    	SbiParuse paruseExist = (SbiParuse)parusesAss.get(paruseExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=paruseExp.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paruseExp.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paruseExp.getDescr()%></div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=paruseExist.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paruseExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=paruseExist.getDescr()%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
			<%if(!metaAss.getBIobjIDAssociation().keySet().isEmpty()) { %>
			<table >
				<tr>
					<td class='portlet-section-header' colspan="2"><spagobi:message key = "Sbi.objects"  bundle="component_impexp_messages"/></td>
				</tr>
				<tr>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.exportedObjects" bundle="component_impexp_messages"/></td>
					<td class='portlet-section-header'><spagobi:message key="SBISet.impexp.currentObjects" bundle="component_impexp_messages"/></td>
				</tr>
				<%
					Map biobjsAss = metaAss.getBIObjAssociation();
					Set biobjsExp = biobjsAss.keySet();
				    Iterator iterExp =  biobjsExp.iterator();
				    while(iterExp.hasNext()) {
				    	SbiObjects biobjExp = (SbiObjects)iterExp.next();
				    	SbiObjects biobjExist = (SbiObjects)biobjsAss.get(biobjExp);
				%>
				<tr>
					<td>
						<span class='portlet-form-field-label'><%=biobjExp.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=biobjExp.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=biobjExp.getDescr()%></div>
					</td>
					<td>
						<span class='portlet-form-field-label'><%=biobjExist.getLabel()%></span><br/>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=biobjExist.getName()%></div>
						<div style="width:100%;overflow:auto;font-size:12px;"><%=biobjExist.getDescr()%></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<%
				    }
				%>
			</table>
			<% } %>
		</div>
	</div>

</form>
	
