<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/includeMessageResource.jspf"%>


<%
// check for user profile autorization
		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL});
		}
// 		System.out.println("User canSee? -------> "+canSee);
// 		System.out.println("User canSeeAdmin? -------> "+canSeeAdmin);
%>

<% if(canSee ){ %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryTecnicalFunctionality">

<head>
	
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>


	<!-- glossary tree -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>
	
		
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario_tec.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/technicaluser/glossaryTec.js"></script>
	
	
</head>


<body class="bodyStyle" style="overflow: hidden !important;">
	
	
<div ng-controller="Controller_tec as global" class="h100">
  <md-content  class="glossaryTec">
    <md-tabs  md-border-bottom class="mini-tabs" style="  min-height: 40px;">
     
      
      <md-tab label='{{translate.load("sbi.glossary.glossary");}}' md-on-select="global.init('glossTreePage')" >
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
 		
    
   <% if( canSeeAdmin){ %>
  	  <md-tab label='{{translate.load("sbi.generic.navigation");}}' md-on-select="global.init('navigation')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/glossary_navigation.jspf"%>
        </md-content>
      </md-tab>
   <%} %>
      
     
      <md-tab label='{{translate.load("sbi.generic.document.management");}}' md-on-select="global.init('docAssoc')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/documents_and_wordsAssociations.jspf"%>
        </md-content>
      </md-tab>
     
      
   <% if(canSeeAdmin){ %>
      <md-tab label='{{translate.load("sbi.generic.dataset.management");}}' md-on-select="global.init('datasetAssoc')">
        <md-content class="abs100">
         <%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/dataset_and_wordsAssociations.jspf"%>
        </md-content>
      </md-tab>
   <%} %>  
  
  
  
    </md-tabs>
  </md-content>
</div>
	

</body>
</html>



<%}else{ %>

UNAUTHORIZED

<%} %>






