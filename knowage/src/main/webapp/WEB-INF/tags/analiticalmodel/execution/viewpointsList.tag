<%@tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/spagobi.tld" prefix="spagobi" %>

<%@attribute name="viewpointsList" required="true" type="java.util.List"%>
<%@attribute name="uuid" required="true" type="java.lang.String"%>

<%@tag import="java.util.Iterator"%>
<%@tag import="it.eng.spagobi.analiticalmodel.document.bo.Viewpoint"%>
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
<%@tag import="it.eng.spago.configuration.ConfigSingleton"%>
<%@tag import="it.eng.spago.base.SourceBean"%>
<%@tag import="it.eng.spago.util.StringUtils"%>
<%@tag import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="it.eng.spago.base.SessionContainer"%>
<%@tag import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="org.apache.commons.lang.StringEscapeUtils"%>


<%
RequestContainer requestContainer = ChannelUtilities.getRequestContainer(request);
IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder(requestContainer.getChannelType());
IEngUserProfile profile = (IEngUserProfile) requestContainer.getSessionContainer().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
SessionContainer permSess=requestContainer.getSessionContainer().getPermanentContainer();

//Set Theme
String currTheme=ThemesManager.getCurrentTheme(requestContainer);
if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();


if (viewpointsList == null || viewpointsList.size() == 0) {
	%>

<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%><div class='portlet-font'><spagobi:message key="SBIDev.docConf.viewPoint.noViewPoints"/></div>
	<%
} else {
    Map deleteVPUrlPars = new HashMap();
    //deleteVPUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
	deleteVPUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);    
    deleteVPUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.VIEWPOINT_ERASE);
    deleteVPUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");
    String deleteVPUrl = urlBuilder.getUrl(request, deleteVPUrlPars);
	%>
	<form method='POST' action='<%= deleteVPUrl %>' id='viewpointsForm<%= uuid %>' name='viewpointsForm<%= uuid %>'>
		<table style='width:100%;' align='left'>
			<thead>
				<tr>
				  <td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				      <spagobi:message key="SBIDev.docConf.viewPoint.name"/>
				  </td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				      <spagobi:message key="SBIDev.docConf.viewPoint.owner"/>
				  </td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				      <spagobi:message key="SBIDev.docConf.viewPoint.description"/>
				  </td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				      <spagobi:message key="SBIDev.docConf.viewPoint.scope"/>
				  </td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				      <spagobi:message key="SBIDev.docConf.viewPoint.dateCreation"/>
				  </td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td align='left' class='portlet-section-header'>&nbsp;</td>
				  <td align='left' class='portlet-section-header'>
				  
				  	<img 
       						src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme) %>' 
       						name='selectDeselectAllImg' alt='<spagobi:message key="SBIDev.docConf.viewPoint.selectAll"/>' 
       						title='<spagobi:message key="SBIDev.docConf.viewPoint.selectAll"/>' 
       						onClick="selectDeselectAllViewpoints<%= uuid %>();" />
						<img 
	       					src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/ico_delete.gif", currTheme) %>' 
	       					alt='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
	       					title='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
	       					onClick="deleteViewpoints<%= uuid %>();" />
	       					
       				<script>
       					function getViewpoints<%= uuid %>() {
       						var toReturn = document.viewpointsForm<%= uuid %>.vpId;
       						if (toReturn != null && !toReturn.length) {
       							var temp = new Array();
       							temp.push(toReturn);
       							toReturn = temp;
       						}
       						return toReturn;
       					}
       				
	       				function deleteViewpoints<%= uuid %>() {
							checks = getViewpoints<%= uuid %>();
							atLeastOneSelected = false;
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									atLeastOneSelected = true;
									break;
								}
							}
							if (!atLeastOneSelected) {
								alert('<spagobi:message key="SBIDev.docConf.viewPoint.noViewPointsSelected" />');
								return;
							}
							var conf = confirm('<spagobi:message key="ConfirmMessages.DeleteViewpoint" />');
							if (conf) {
								document.getElementById('viewpointsForm<%= uuid %>').submit();
							}
						}
						
						selectableViewpoints<%= uuid %> = new Array();
						<%
						if (viewpointsList != null && viewpointsList.size() > 0) {
							Iterator iterVPs =  viewpointsList.iterator();
							while(iterVPs.hasNext()) {
								Viewpoint vp = (Viewpoint) iterVPs.next();
								if (vp.getVpOwner().equals(((UserProfile)profile).getUserId().toString())
										|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
									%>
									selectableViewpoints<%= uuid %>.push(<%= vp.getVpId() %>);
									<%
								}
							}
						}
						%>
						
						selectedViewpoints<%= uuid %> = new Array();
						
						function selectDeselectAllViewpoints<%= uuid %>() {
							if (selectedViewpoints<%= uuid %>.length == 0) {
								selectAllViewpoints<%= uuid %>();
							} else if (selectedViewpoints<%= uuid %>.length == selectableViewpoints<%= uuid %>.length) {
								deselectAllViewpoints<%= uuid %>();
							} else {
								selectAllViewpoints<%= uuid %>();
							}
						}
						
						function selectAllViewpoints<%= uuid %>() {
							checks = getViewpoints<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (!check.checked) {
									check.click();
								}
							}
						}
						
						function deselectAllViewpoints<%= uuid %>() {
							checks = getViewpoints<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									check.click();
								}
							}
						}
					</script>
				  
				  </td>
				</tr>
			</thead>
			<tboby>
	<%
	Iterator iterVP =  viewpointsList.iterator();
    Viewpoint vp = null;
    String ownerVP = null;				    
    String nameVP = null;
    String descrVP = null;
    String scopeVP = null;
    Date creationDateVP = null;
    String execVPUrl = null;
    String viewVPUrl = null;				    				   
	boolean alternate = false;
	String rowClass = null;
		   
    while (iterVP.hasNext()) {
    	vp = (Viewpoint)iterVP.next();
		rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
		alternate = !alternate;
		nameVP = vp.getVpName();
		ownerVP = vp.getVpOwner();						
		descrVP = vp.getVpDesc();
		scopeVP = vp.getVpScope();
		creationDateVP = vp.getVpCreationDate();
		
		Map execVPUrlPars = new HashMap();
		//execVPUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
		execVPUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);    
		execVPUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.VIEWPOINT_EXEC);	
		execVPUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");						
		execVPUrlPars.put("content", vp.getVpValueParams());
		execVPUrlPars.put("vpId",vp.getVpId());
		execVPUrl = urlBuilder.getUrl(request, execVPUrlPars);
			
	    Map viewVPUrlPars = new HashMap();
	    viewVPUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
	    viewVPUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.VIEWPOINT_VIEW);
	    viewVPUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");
	    viewVPUrlPars.put("vpId",vp.getVpId());
	    viewVPUrl = urlBuilder.getUrl(request, viewVPUrlPars);

        ConfigSingleton conf = ConfigSingleton.getInstance();
		String format=GeneralUtilities.getLocaleDateFormat(permSess)+" hh:mm:ss";
        String date = StringUtils.dateToString(creationDateVP, format);
        %>
				<tr class='portlet-font'>
					<td style='vertical-align:middle;' class='<%= rowClass %>'><%= StringEscapeUtils.escapeHtml(nameVP) %></td>
					<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
					<td style='vertical-align:middle;' class='<%= rowClass %>'><%= StringEscapeUtils.escapeHtml(ownerVP) %></td>
					<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
					<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(descrVP) %></td>
					<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
					<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(scopeVP) %></td>
					<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
					<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(date) %></td>
					<td style='vertical-align:middle;' class='<%= rowClass %>' width='40px'>
				    	<a href="javascript:document.location='<%= viewVPUrl.toString() %>';">
				    		<img 
				    			src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/notes.jpg", currTheme) %>' 
				       			name='getViewpoint' alt='<spagobi:message key="SBIDev.docConf.viewPoint.viewButt" />' 
				        		title='<spagobi:message key="SBIDev.docConf.viewPoint.viewButt" />'
				        	/>
				    	</a>
					</td>
					<td style='vertical-align:middle;' class='<%= rowClass %>' width='40px'>
						<a href='<%= execVPUrl %>'>
					 		<img 
					  	   			src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/exec.gif", currTheme) %>'
					  	       		name='execSnap'
					  	        	alt='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />'
					       			title='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />'
							/>
						</a>
					</td>
					<td style='vertical-align:middle;text-align:center;' class='<%= rowClass %>' width='40px'>
					<%
	                if (ownerVP.equals(((UserProfile)profile).getUserId().toString()) || profile.getFunctionalities().contains(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
	                	%>
							<input type="checkbox" name="vpId" id="vpId"
				   					value="<%= vp.getVpId() %>" 
				   					onClick="if (this.checked) {selectedViewpoints<%= uuid %>.push(this.value);} else {selectedViewpoints<%= uuid %>.removeFirst(this.value);}"/>
	                 	<%
	                } else {
	           			%>
           					&nbsp;
           				<%
	                }
					%>
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