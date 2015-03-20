
<html>
 <head>


<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>

 </head>
 
 
 <%



 	String userName="";
	IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	

	if (userProfile!=null){
		userName=(String)((UserProfile)userProfile).getUserName();
	}

	
	%>
 
<body>
<div style="float:left">&nbsp;<img src="/SpagoBI/themes/sbi_default/img/wapp/Logo_SpagoBI.gif"/></div>
<br/>
<br/>
<div></div>
<div style="margin-top: 40px">
Version: 5.1.0
</div>
<div style="margin-top: 5px">
Logged User: <%= userName %>
</div>

<div style="position: absolute; bottom:0px; text-align: center"> 
<i>Copyright (C) 2013 Engineering Ingegneria Informatica S.p.A.</i>
</div>

</body>
 </html>