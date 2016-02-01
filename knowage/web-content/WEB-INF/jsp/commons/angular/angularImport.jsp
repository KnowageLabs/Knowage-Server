<%-- 
	This files must be use in every 
	SpagoBI${pageContext.request.contextPath}/Knowage page that 
	makes use of AngularJS  
--%>

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">

<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

<!-- angular reference-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-sanitize.min.js"></script>


<!-- angular-material-->
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>

<!-- angular tree -->
<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>

<!-- angular list -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/angular-list.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/AngularList.js"></script>		

<!-- context menu -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>

<!--pagination-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>

<!-- angular table -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

<!-- document tree -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>

<!-- component tree -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/component-tree/componentTree.js"></script>

<!-- file upload -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/upload-file/FileUpload.js"></script>

<!-- 	angular time picker -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

<!-- 	angular list dewtail template -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js"></script>

<!-- deprecated angular 2 col -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js"></script>

<!-- toastr -->

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/catalogue/css/angular-toastr.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-toastr.tpls.js"></script>		

<style>

    .md-container {
      position: static;
      display: -webkit-flex;
      display: -ms-flexbox;
      display: flex;
      box-sizing: border-box;
      -webkit-flex-direction: column;
      -ms-flex-direction: column;
      flex-direction: column;
/*       z-index: 60; */
      bottom: 0;
/*       overflow: auto; */
    }

    /* ===============
      queste classi sono per visualizzare le icone di fontawesome come anche lo stile che ne carica il cdn
      diteci voi a quale libreria rifarci
     =================*/

    .s64 {
      font-size:64px;
    }

    .s32 {
      
      font-size:24px;

    }

    md-icon.fa {
      display:block;
      padding-left:0px;
    }

    md-icon.s32 span {
      padding-left:8px;
    }
    
    .h100{
    height: 100%;}

.w100{
width: 100%}
.absolute{
	position:absolute;
}
  </style>

<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
	
