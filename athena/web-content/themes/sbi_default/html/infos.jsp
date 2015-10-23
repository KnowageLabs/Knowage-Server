
<html>
 <head>


<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

 </head>
 
 
 <%



 	String userName="";
	IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	

	if (userProfile!=null){
		userName=(String)((UserProfile)userProfile).getUserName();
	}

	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	%>
 
<body>
<div style="float:left">&nbsp;<img src="<%=contextName %>/themes/sbi_default/img/wapp/logo_knowage_small.png"/></div>
<br/>
<br/>
<div></div>
<div style="margin-top: 40px">
Version: 1.0
</div>
<div style="margin-top: 5px">
Logged User: <%= userName %>
</div>

<div style="position: absolute; bottom:0px; text-align: center"> 
<i>Copyright (C) 2014 Engineering Ingegneria Informatica S.p.A.</i>
</div>

</body>
 </html>