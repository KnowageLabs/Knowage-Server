<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="false" 
%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>

<%
HttpSession session = request.getSession(false);
if (session != null) {
	session.invalidate();
}
String sessionCookieName = request.getServletContext().getSessionCookieConfig().getName();
if (sessionCookieName == null) {
	sessionCookieName = "JSESSIONID";
}
Cookie cookie = new Cookie(sessionCookieName, "");
cookie.setHttpOnly(true);
cookie.setPath(KnowageSystemConfiguration.getKnowageCommonjEngineContext());
cookie.setMaxAge(0);
response.addCookie(cookie);
%>