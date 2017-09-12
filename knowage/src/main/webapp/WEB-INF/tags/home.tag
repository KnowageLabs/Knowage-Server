<%@tag language="java" pageEncoding="UTF-8" %>
<%@tag import="java.net.URLEncoder"%>
<%@attribute name="spagobiContext" required="true" type="java.lang.String"%>
<%@attribute name="displayBannerAndFooter" required="false" type="java.lang.Boolean"%>
<%@attribute name="theme" required="false" type="java.lang.String"%>
<%@attribute name="iframeStyle" required="false" type="java.lang.String"%>
<%@attribute name="authenticationTicket" required="false" type="java.lang.String"%>

<%
StringBuffer iframeUrl = new StringBuffer();
iframeUrl.append(spagobiContext + "/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE");

if (displayBannerAndFooter != null)	iframeUrl.append("&displayBannerAndFooter=" + displayBannerAndFooter.toString());
if (theme != null)	iframeUrl.append("&theme=" + theme);
if (authenticationTicket != null) iframeUrl.append("&auth_ticket=" + URLEncoder.encode(authenticationTicket));
%>

<iframe src="<%= iframeUrl.toString() %>" style="<%= iframeStyle != null ? iframeStyle : "" %>"></iframe>
