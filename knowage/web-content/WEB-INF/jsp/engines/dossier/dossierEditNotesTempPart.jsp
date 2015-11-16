<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,
				java.util.List,
				java.util.Iterator,
				java.util.Map,
				java.util.HashMap,
				java.util.Set,
				it.eng.spago.base.SourceBean,
				java.util.HashMap,
				it.eng.spagobi.commons.constants.SpagoBIConstants" %>
<%@page import="it.eng.spagobi.engines.dossier.constants.DossierConstants"%>

<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute(DossierConstants.DOSSIER_COLLABORATION_MODULE); 
	String dossierIdStr = (String)moduleResponse.getAttribute(DossierConstants.DOSSIER_ID);
	String indexPart = (String)moduleResponse.getAttribute(DossierConstants.DOSSIER_PART_INDEX);
	String activityKey = (String)moduleResponse.getAttribute(SpagoBIConstants.ACTIVITYKEY);
	Map imageurl = (Map)moduleResponse.getAttribute("mapImageUrls");
    String notes = (String)moduleResponse.getAttribute("notes");
    Iterator iterImgs = null;
    
    // add parameters to back url
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", "WorkflowToDoListPage");
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
	
	// add parameters to save url
	Map saveNoteUrlPars = new HashMap();
	saveNoteUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
	saveNoteUrlPars.put("OPERATION", DossierConstants.OPERATION_SAVE_NOTE);
	saveNoteUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
	saveNoteUrlPars.put(DossierConstants.DOSSIER_PART_INDEX, indexPart);
	saveNoteUrlPars.put(SpagoBIConstants.ACTIVITYKEY, activityKey);
	saveNoteUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");	
	String saveNoteUrl = urlBuilder.getUrl(request, saveNoteUrlPars);
	      
	// add parameter to close url
	Map closeNoteUrlPars = new HashMap();
	closeNoteUrlPars.put("PAGE", "CompleteOrRejectActivityPage");
	closeNoteUrlPars.put("CompletedActivity", "TRUE");
	closeNoteUrlPars.put(SpagoBIConstants.ACTIVITYKEY, activityKey);
	closeNoteUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");	
	String closeNoteUrl = urlBuilder.getUrl(request, closeNoteUrlPars);
	      
%>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "dossier.editnotes"  bundle="component_dossier_messages"/>
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl %>'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "dossier.back" bundle="component_dossier_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/back.png", currTheme)%>' 
      				 alt='<spagobi:message key = "dossier.back"  bundle="component_dossier_messages"/>' />
			</a>
		</td>
		
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href="javascript:document.getElementById('formNotes').submit();"> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "dossier.save" bundle="component_dossier_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/save32.png", currTheme)%>' 
      				 alt='<spagobi:message key = "dossier.save"  bundle="component_dossier_messages"/>' />
			</a>
		</td>

		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=closeNoteUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "dossier.closeDiscussion" bundle="component_dossier_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/closeNotes32.png", currTheme)%>' 
      				 alt='<spagobi:message key = "dossier.closeDiscussion"  bundle="component_dossier_messages"/>' />
			</a>
		</td>
		
	</tr>
</table>



<br/>

	
<spagobi:error/>	


	
	<script>
			
			var tabs = new Array(<%=(imageurl.size() + 1)%>);
			<%
				iterImgs = imageurl.keySet().iterator();
				int prog = 0;
				while(iterImgs.hasNext()){
					String nameImg = (String)iterImgs.next();
			%>
					tabs[<%=prog%>]="tab<%=nameImg%>";
			<%
				prog ++;
				}
			%>
			tabs[<%=prog%>]="tabNote";
			
			
			var divs = new Array(<%=(imageurl.size() + 1)%>);
			<%
				iterImgs = imageurl.keySet().iterator();
				prog = 0;
				while(iterImgs.hasNext()){
					String nameImg = (String)iterImgs.next();
			%>
					divs[<%=prog%>]="div<%=nameImg%>";
			<%
				prog ++;
				}
			%>
			divs[<%=prog%>]="divNote";
			
			
			var selectedName = "Note"
			
			
		function changeTab(name) {
			for(i=0; i<divs.length; i++) {
			    completeTabName = tabs[i];
				completeDivName = divs[i];
				divobj = document.getElementById(completeDivName);
				tabobj = document.getElementById(completeTabName);
				divName = completeDivName.substring(3);
				tabName = completeTabName.substring(3);
				if(divName==name){
					divobj.style.display='inline';
				} else {
				  divobj.style.display='none';
				}
				if(tabName==name){
					tabobj.className='tab selected';
				} else {
				  tabobj.className='tab';
				}
			}
		}
	
	</script>
	
	
	
	<!-- ************************ START BUILT TABS ************** -->
	
	<div style='width:100%;' class='UITabs'>
		<div class="first-tab-level" style="background-color:#f8f8f8">
			<div style="overflow: hidden; width:  100%">
				
			<%
				iterImgs = imageurl.keySet().iterator();
				while(iterImgs.hasNext()){
					String nameImg = (String)iterImgs.next();
					String linkClass = "tab";
			%>
		
				<div id='tab<%=nameImg%>' class='<%=linkClass%>'>
					<a href="javascript:changeTab('<%=nameImg%>')" style="color:black;"> <%=nameImg%> </a>
				</div>
		
			<%	
				}
			%>
						
				<div id='tabNote' class='tab selected'>
					<a href="javascript:changeTab('Note')" style="color:black;">Note</a>
				</div>	
				
			</div>
		</div>
	</div>
	
	<!-- ************************ END BUILT TABS ************** -->
	
		
		
		
		
		
		
	

	<!-- ************************ START BUILT DIVS IMAGE AND NOTE ************** -->	
		
	<div style="width:100%;background-color:#f8f8f8;border:1 solid black;">
	<br/>	
	<%
		iterImgs = imageurl.keySet().iterator();
		while(iterImgs.hasNext()){
			String nameImg = (String)iterImgs.next();
			String url = (String)imageurl.get(nameImg);
	%>
		<div style="display:none;" name="div<%=nameImg%>" id="div<%=nameImg%>">
			<center>
				<img src="<%=url%>" />
			</center>
			<br/>
			<br/>
		</div>
	<%
		}
	%>
	    <div style="display:inline;" name="divNote" id="divNote" >
			<div name="notesdiv" id="notesdiv" >
				<form method="POST" id="formNotes" action="<%=saveNoteUrl%>" >
				<center>
					<b><spagobi:message key = "dossier.notes"  bundle="component_dossier_messages"/></b>
					<br/>
					<textarea name="notes" style="width:1000px;height:350px;"><%=notes%></textarea>
				<center>
				</form>
			</div>
			<br/>
			<br/>
			
		
		</div>
	
	
	<!-- ************************ START BUILT DIVS IMAGE AND NOTE ************** -->	
	
	</div>











