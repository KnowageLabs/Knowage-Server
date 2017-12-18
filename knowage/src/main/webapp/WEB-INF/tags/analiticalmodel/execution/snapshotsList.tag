<%@tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/spagobi.tld" prefix="spagobi" %>

<%@attribute name="snapshotsList" required="true" type="java.util.List"%>
<%@attribute name="uuid" required="true" type="java.lang.String"%>

<%@tag import="java.util.Iterator"%>
<%@tag import="it.eng.spagobi.analiticalmodel.document.bo.Snapshot"%>
<%@tag import="it.eng.spago.base.SessionContainer"%>
<%@tag import="it.eng.spago.base.RequestContainer"%>
<%@tag import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@tag import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@tag import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@tag import="it.eng.spago.security.IEngUserProfile"%>
<%@tag import="java.util.Map"%>
<%@tag import="java.util.HashMap"%>
<%@tag import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>
<%@tag import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@tag import="it.eng.spago.navigation.LightNavigationManager"%>
<%@tag import="java.util.Date"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="it.eng.spago.util.StringUtils"%>
<%@tag import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="org.apache.commons.lang.StringEscapeUtils"%>

<%
RequestContainer requestContainer = ChannelUtilities.getRequestContainer(request);
IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder(requestContainer.getChannelType());
IEngUserProfile profile = (IEngUserProfile) requestContainer.getSessionContainer().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
SessionContainer permSess=requestContainer.getSessionContainer().getPermanentContainer();

// Set Theme
String currTheme=ThemesManager.getCurrentTheme(requestContainer);
if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();



if (snapshotsList == null || snapshotsList.size() == 0) {
	%>

<div class='portlet-font'><spagobi:message key="SBIDev.docConf.snapshots.nosnapshots"/></div>
	<%
} else {
    Map deleteSnapUrlPars = new HashMap();
    //deleteSnapUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
    deleteSnapUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);
    deleteSnapUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.ERASE_SNAPSHOT_MESSAGE);
    deleteSnapUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");
    String deleteSnapUrl = urlBuilder.getUrl(request, deleteSnapUrlPars);
	
	%>
	<form method='POST' action='<%= deleteSnapUrl %>' id='snapshotsForm<%= uuid %>' name='snapshotsForm<%= uuid %>'>
	<table style='width:100%;' align='left'>
		<thead>
			<tr>
				<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
					<spagobi:message key="SBIDev.docConf.snapshots.name"/>
				</td>
				<td align='left' class='portlet-section-header'>&nbsp;</td>
				<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
					<spagobi:message key="SBIDev.docConf.snapshots.description"/>
				</td>
		   		<td align='left' class='portlet-section-header'>&nbsp;</td>
		   		<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
		   			<spagobi:message key="SBIDev.docConf.snapshots.dateCreation"/>
		   		</td>
		   		<td align='left' class='portlet-section-header'>&nbsp;</td>
		   		<td align='left' class='portlet-section-header'>&nbsp;</td>
		   		<td align='left' class='portlet-section-header'>
		   		<%
		   		if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
		        	%>
	        		&nbsp;
	        		<%
	        	} else {
	        		%>
					<img 
       					src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif",currTheme ) %>' 
       					alt='<spagobi:message key="SBIDev.docConf.snapshots.selectAll"/>' 
       					title='<spagobi:message key="SBIDev.docConf.snapshots.selectAll"/>' 
       					onClick="selectDeselectAllSnapshots<%= uuid %>();" />
					<img 
       					src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/ico_delete.gif", currTheme) %>' 
       					alt='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
       					title='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
       					onClick="deleteSnapshots<%= uuid %>();" />
       				<script>
       					function getSnapshots<%= uuid %>() {
       						var toReturn = document.snapshotsForm<%= uuid %>.<%= SpagoBIConstants.SNAPSHOT_ID %>;
       						if (toReturn != null && !toReturn.length) {
       							var temp = new Array();
       							temp.push(toReturn);
       							toReturn = temp;
       						}
       						return toReturn;
       					}
       					
	       				function deleteSnapshots<%= uuid %>() {
							checks = getSnapshots<%= uuid %>();
							atLeastOneSelected = false;
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									atLeastOneSelected = true;
									break;
								}
							}
							if (!atLeastOneSelected) {
								alert('<spagobi:message key="SBIDev.docConf.snapshots.noSnapshotsSelected" />');
								return;
							}
							var conf = confirm('<spagobi:message key="ConfirmMessages.DeleteSnapshot" />');
							if (conf) {
								document.getElementById('snapshotsForm<%= uuid %>').submit();
							}
						}

						selectedSnapshots<%= uuid %> = new Array();
						
						function selectDeselectAllSnapshots<%= uuid %>() {
							if (selectedSnapshots<%= uuid %>.length == 0) {
								selectAllSnapshots<%= uuid %>();
							} else if (selectedSnapshots<%= uuid %>.length == <%= snapshotsList.size() %>) {
								deselectAllSnapshots<%= uuid %>();
							} else {
								selectAllSnapshots<%= uuid %>();
							}
						}
						
						function selectAllSnapshots<%= uuid %>() {
							checks = getSnapshots<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (!check.checked) {
									check.click();
								}
							}
						}
						
						function deselectAllSnapshots<%= uuid %>() {
							checks = getSnapshots<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									check.click();
								}
							}
						}
					</script>
				<%
	        	}
				%>
		   		</td>
	 		</tr>
		</thead>
		<tboby>
	<%
	Iterator iterSnap =  snapshotsList.iterator();
    Snapshot snap = null;
    String nameSnap = null;
    String descrSnap = null;
    Date creationDate = null;
    String execSnapUrl = null;
	boolean alternate = false;
	String rowClass = null;
	   
    while(iterSnap.hasNext()) {
    	snap = (Snapshot)iterSnap.next();
		rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
		alternate = !alternate;
		nameSnap = snap.getName();
		descrSnap = snap.getDescription();
		creationDate = snap.getDateCreation();
		String format=GeneralUtilities.getLocaleDateFormat(permSess)+" hh:mm:ss";
    	String dateValue = StringUtils.dateToString(creationDate, format);
  
		
		Map execSnapUrlPars = new HashMap();
//		execSnapUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
		execSnapUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);  		    
		execSnapUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.EXEC_SNAPSHOT_MESSAGE);
	    execSnapUrlPars.put(SpagoBIConstants.SNAPSHOT_ID, snap.getId());
	    execSnapUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");
	    execSnapUrl = urlBuilder.getUrl(request, execSnapUrlPars);
		
        %>
	    <tr class='portlet-font'>
	    	<td style='vertical-align:middle;' class='<%= rowClass %>'><%= StringEscapeUtils.escapeHtml(nameSnap) %></td>
	    	<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
	    	<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(descrSnap) %></td>
	    	<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
	    	<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(dateValue) %></td>
	    	<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
        	<td style='vertical-align:middle;' class='<%= rowClass %>' width='40px'>
        		<a href='<%= execSnapUrl %>'>
        			<img 
						src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/exec.gif", currTheme) %>' 
						name='execSnap' 
						alt='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt"/>' 
						title='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt"/>' />
        		</a>
       		</td>
	    	<td style='vertical-align:middle;text-align:center;' class='<%= rowClass %>' width='40px' >
	    <%
        if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
        	%>
        		&nbsp;
        	<%
        } else {
        	%>
				<input type="checkbox" name="<%= SpagoBIConstants.SNAPSHOT_ID %>" id="<%= SpagoBIConstants.SNAPSHOT_ID %>"
					   value="<%= snap.getId() %>" onClick="if (this.checked) {selectedSnapshots<%= uuid %>.push(this.value);} else {selectedSnapshots<%= uuid %>.removeFirst(this.value);}"/>
        	<%
        }
	    %>
        	</td>
     	</tr>
     	<%
    }
    %>
    </tboby>
	</table>
	</form>
    <%
}
%>