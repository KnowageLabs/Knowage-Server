<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.message.MessageBundle"%>
<%
	Locale locale =  MessageBundle.getUserLocale(); 
	IUrlBuilder urlBuilder=UrlBuilderFactory.getUrlBuilder("WEB");
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="AIDA_GLOSSARY_TECNICAL_USER">

<head>
	
	<meta http-equiv="x-ua-compatible" content="IE=EmulateIE9" >
	
	<!-- JavaScript --> 
 <!--[if IE 8]> 
 <script src="http://code.jquery.com/jquery-1.11.1.min.js"></script> 
 <script src="http://cdnjs.cloudflare.com/ajax/libs/es5-shim/3.4.0/es5-shim.min.js"></script> 
 <![endif]--> 
	
	<script type="text/javascript" src="/athena/js/glossary/angular/angular.js"></script>
	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="/athena/js/glossary/angular/material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="/athena/js/glossary/angular/material_0.10.0/angular-material.js"></script>
	
	<script type="text/javascript" 	src="/athena/js/glossary/angular/angular-animate.min.js"></script>
	<script type="text/javascript" src="/athena/js/glossary/angular/angular-aria.min.js"></script>
	
	
 	<link rel="stylesheet"  href="/athena/js/glossary/angulartree/angular-ui-tree.min.css"> 
 	<script type="text/javascript"  src="/athena/js/glossary/angulartree/angular-ui-tree.js"></script> 
 	
	<script type="text/javascript" src="/athena/js/glossary/contextmenu/ng-context-menu.min.js"></script>
	<script type="text/javascript"
		src="/athena/js/glossary/pagination/dirPagination.js"></script>
	
	<%@ include file="/WEB-INF/jsp/tools/glossary/template.jsp"%>
	<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
	
	
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario_tec.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	<script type="text/javascript" src="/athena/js/glossary/RestService.js"></script>
	<script type="text/javascript" src="/athena/js/glossary/glossaryTec.js"></script>
	
</head>


<body class="bodyStyle" style="overflow: hidden !important;"">

	
	
<div ng-controller="Controller_tec as global" class="h100">
  <md-content  class="glossaryTec">
    <md-tabs  md-border-bottom class="mini-tabs" style="  min-height: 40px;">
     
       <md-tab label="NAVIGAZIONE">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/glossary_navigation.jspf"%>
        </md-content>
      </md-tab>
      
      <md-tab label="GESTIONE" >
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/management_associations_between_documents_and_words.jspf"%>
        </md-content>
      </md-tab>
      
    </md-tabs>
  </md-content>
</div>
	

</body>
</html>










