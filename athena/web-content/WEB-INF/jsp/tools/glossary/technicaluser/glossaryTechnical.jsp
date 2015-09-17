<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>





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
	
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/AngularList.js"></script>
		
	<!-- context menu -->
	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>
		
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario_tec.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
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






