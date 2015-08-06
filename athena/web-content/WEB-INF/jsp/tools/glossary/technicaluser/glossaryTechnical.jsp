<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.PortletUtilities"%>

<%
	RequestContainer aRequestContainer = null;
	ResponseContainer aResponseContainer = null;
	SessionContainer aSessionContainer = null;
	IUrlBuilder urlBuilder = null;
	IMessageBuilder msgBuilder = null;
	String sbiMode = null;
		
	// case of portlet mode
	aRequestContainer = RequestContainerPortletAccess.getRequestContainer(request);
	aResponseContainer = ResponseContainerPortletAccess.getResponseContainer(request);
	if (aRequestContainer == null) {
		// case of web mode
		aRequestContainer = RequestContainer.getRequestContainer();
		if(aRequestContainer == null){
			//case of REST 
			aRequestContainer = RequestContainerAccess.getRequestContainer(request);
		}
		aResponseContainer = ResponseContainer.getResponseContainer();
		if(aResponseContainer == null){
			//case of REST
			aResponseContainer = ResponseContainerAccess.getResponseContainer(request);
		}
	}
	
	String channelType = aRequestContainer.getChannelType();
	if ("PORTLET".equalsIgnoreCase(channelType)) sbiMode = "PORTLET";
	else sbiMode = "WEB";
	
	// create url builder 
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
	
	// create message builder
	msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	// get other spago object
	SourceBean aServiceRequest = aRequestContainer.getServiceRequest();
	SourceBean aServiceResponse = aResponseContainer.getServiceResponse();
	aSessionContainer = aRequestContainer.getSessionContainer();
	SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
	
	// If Language is alredy defined keep it
	String curr_language=(String)permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
	String curr_country=(String)permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
	Locale locale = null;
	
	if (curr_language != null && curr_country != null	&& !curr_language.equals("") && !curr_country.equals("")) {
		locale = new Locale(curr_language, curr_country, "");
	} else {
		if (sbiMode.equals("PORTLET")) {
			locale = PortletUtilities.getLocaleForMessage();
		} else {
			locale = MessageBuilder.getBrowserLocaleFromSpago();
		}
	}
	
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
	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">
	
	
	<!-- angular reference-->
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="/athena/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
	
	
	<!-- angular-material-->
	<link rel="stylesheet" href="/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
	
	<!-- angular tree -->
	<link rel="stylesheet" 	href="/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="/athena/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
	<!-- glossary tree -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>
	
	
	<!-- context menu -->
	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>
	
		
	<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
	
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario_tec.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/technicaluser/glossaryTec.js"></script>
	
	
</head>


<body class="bodyStyle" style="overflow: hidden !important;"">
<%-- <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/prova.jspf"%> --%>
	
	
<div ng-controller="Controller_tec as global" class="h100">
  <md-content  class="glossaryTec">
    <md-tabs  md-border-bottom class="mini-tabs" style="  min-height: 40px;">
     
      <md-tab label='{{translate.load("sbi.glossary.glossary");}}' >
        <md-content class="abs100">
        	<glossary-tree
						tree-id="GlossTree" 
						tree-options=ctrl.TreeOptions 
						show-root=false
						glossary={}
						show-select-glossary=false
						show-search-bar=true
						show-info=true
						>
					</glossary-tree>
        </md-content>
      </md-tab>
     
       <md-tab label='{{translate.load("sbi.generic.navigation");}}' md-on-select="global.init('navigation')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/glossary_navigation.jspf"%>
        </md-content>
      </md-tab>
      
      <md-tab label='{{translate.load("sbi.generic.document.management");}}' md-on-select="global.init('docAssoc')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/documents_and_wordsAssociations.jspf"%>
        </md-content>
      </md-tab>
      
      <md-tab label='{{translate.load("sbi.generic.dataset.management");}}' md-on-select="global.init('datasetAssoc')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/dataset_and_wordsAssociations.jspf"%>
        </md-content>
      </md-tab>
      
    </md-tabs>
  </md-content>
</div>
	

</body>
</html>










