<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

 <%



 	String userName="";
	IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	

	if (userProfile!=null){
		userName=(String)((UserProfile)userProfile).getUserName();
	}

	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	%>
	
<md-dialog id="infosDialog">
	<md-dialog-content flex>
		<md-toolbar class="minihead">
			<div class="md-toolbar-tools">
				  <md-content style="float:left;background-color: inherit;">&nbsp;
				 	 <img src="<%=contextName %>/themes/sbi_default/img/wapp/logoKnowage.png" />
				  </md-content>
			</div>
		</md-toolbar> 
		<md-content layout="column" layout-align="space-between stretch">
			<md-content layout="column" layout-align="space-around stretch"> 
				<md-content  style="margin-left:5px;margin-right:20px">
					Version: 1.0
				</md-content>
				<md-content  style="margin-left:5px;margin-right:20px">
					Logged User: <%= userName %>
				</md-content>
				<md-content  style="margin-left:5px;margin-right:20px">
					Source code available at <a href="http://www.knowage-suite.com">www.knowage-suite.com</a>
				</md-content>
				
				<md-content layout="row" layout-align ="center center" style="margin-left:5px;margin-right:20px"> 
					<i>Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.</i>
				</md-content>
			</md-content>
			</br>
			<md-content layout = "row" layout-align="end center" style="width: 100%">
				<md-button type="button" class="md-ExtraMini"	ng-click="closeDialog()">{{okMessage}}</md-button>
			</md-content>
		</md-content>
	</md-dialog-content>
</md-dialog>
