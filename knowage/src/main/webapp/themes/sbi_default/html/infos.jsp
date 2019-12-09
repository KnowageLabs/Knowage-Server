<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.knowage.wapp.Version"%>

 <%
 	String userName="";
 	String tenantName="";
	IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	

	if (userProfile!=null){
		userName=(String)((UserProfile)userProfile).getUserName();
		tenantName=(String)((UserProfile)userProfile).getOrganization();
	}

	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	%>
	
<md-dialog id="infosDialog" layout="column">
<form>

	<md-dialog-content layout="column" layout-padding layout-margin>
		<div layout="row" style="width:100%" layout-align="center center">
			<img src="<%=contextName %>/themes/commons/img/defaultTheme/logo.svg" width="200" layout-margin />
		</div>
		 	 
		 	 <div layout-padding>
				<p>Version: <%= Version.getVersionForDatabase() %></p>
				<p >
					Logged User: <%= userName %>
				</p>
				<p >
					Tenant: <%= tenantName %>
				</p>
				<p  >
					Source code available at <a href="http://www.knowage-suite.com" target="_blank">www.knowage-suite.com</a>
				</p>
				
				<p layout-align ="center center" > 
					<i>&#169; 2019 Engineering Ingegneria Informatica S.p.A.</i>
				</p>
			</div>
	</md-dialog-content>
	<div class="md-actions">
		<md-button ng-click="closeDialog()" >
			{{okMessage}}
        </md-button>
     </div>
     </form>
	
</md-dialog>
