<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%@include file="/WEB-INF/jsp/tools/glossary/commons/headerInclude.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryHelpOnLine">

<head>
	
	<meta http-equiv="x-ua-compatible" content="IE=EmulateIE9" >
	<meta name="viewport" content="width=device-width">
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
	
	<!-- glossary tree only style -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	
	<!-- context menu -->
	<script type="text/javascript" src="/athena/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/AngularList.js"></script>
	
	
	<!--pagination-->
	<script type="text/javascript" src="/athena/js/lib/angular/pagination/dirPagination.js"></script>
	
<!-- 	breadCrumb -->
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/bread-crumb.css">
	
	
<% 

	String type="",value="",label="",parameter1="",parameter2="";

	if(request.getParameter("DOCUMENT")!=null){
		type="DOCUMENT";
		value=request.getParameter("DOCUMENT");
		label=request.getParameter("LABEL");
		parameter1=request.getParameter("DATASET");
		
	}else if(request.getParameter("DATASET")!=null){
		type="DATASET";
		value=request.getParameter("DATASET");
		label=request.getParameter("LABEL");
	}else if(request.getParameter("DATASET_LABEL")!=null){
		type="DATASET";
		label=request.getParameter("DATASET_LABEL");
		value=null;
	}else if(request.getParameter("WORD")!=null){
		type="WORD";
		label=request.getParameter("WORD");
	}else if(request.getParameter("BUSINESS_CLASS")!=null){
		type="BUSINESS_CLASS";
		label=request.getParameter("BUSINESS_CLASS");
		parameter1=request.getParameter("DATAMART");
	}
	
%>
<script>
var type='<%= type%>';
var value='<%= value%>';
var label='<%= label%>';
var parameter1='<%= parameter1 %>';
var parameter2='<%= parameter2 %>';
</script>
	
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/commons/RestService.js"></script>
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/finaluser/glossaryHelpOnline.js"></script>
	
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	
	<style type="text/css">
	.halfperc{
	height: 50% !important;
	 }
	
	.halfperc2{
	height: calc(50% - 16px ) ;
	}
	
	.fullperc2{
		height: calc(100% - 16px ) ;
	}
	
	.datasetcol{
	    position: absolute;
    width: 100%;
    bottom: 0;}
    
    .noPaddingList .md-button {
    padding: 0px !important;}
    
    md-tabs.singleItem md-ink-bar, md-tabs.singleItem md-tab-item,md-tabs.singleItem md-pagination-wrapper{
    width: 100%;}
 
	</style>
	
</head>


<body class="bodyStyle" ng-controller="Controller  ">


	<div  layout="row" layout-fill style="position: absolute;  height: 100%; padding: 10px;" >
	
		<div flex="30" flex-lg="30" flex-md="40" style="height: 100% " ng-if="type!='WORD'">
			<md-tabs  class="mini-tabs" ng-class="{'singleItem' : data.length==1}">
				<md-tab  ng-repeat="tab in data" label="{{tab.type}}" layout-fill style="height: 100%;">
		 		
		 		<p style=" margin: 0;   text-align: center; height: 16px;     background-color: #E8E8E8">{{tab.title}}</p>
		 		
				<angular-list layout-fill class="fullperc2"   ng-class="{ 'halfperc' : (tab.subItemList!=undefined && tab.subItemList.length!=0)  }" style="min-height: 0px !important;    position: absolute;"
						id='wordList{{$index}}' 
                		ng-model=tab.itemList
                		item-name='WORD'
                		click-function="showInfoWORD(item)"
                		highlights-selected-item=true
                		show-search-bar=true
                		selected-item=selectedWord
                		>
				</angular-list>
			
				<div class="halfperc2 datasetcol" ng-if="tab.subItemList!=undefined && tab.subItemList.length!=0">
					<md-toolbar class="md-blue xs-head">
					<div class="md-toolbar-tools">
						<div ng-if="type!='BUSINESS_CLASS'"> {{translate.load("sbi.ds.metadata.dataset.title");}}</div>
						<div ng-if="type=='BUSINESS_CLASS'"> {{translate.load("sbi.glossary.businessclass.column");}}</div>
					</div>
					</md-toolbar>

					<md-content layout-padding style="height:Calc(100% - 32px); padding-bottom: 8px;"> 
			
						<div   id="Tree-Word-Dataset"  ui-tree="" data-drag-enabled="true"  data-clone-enabled="true">
							<ol ui-tree-nodes ng-model="tab.subItemList" 	ng-class="{hideChildren: collapsed}">
								<li ng-repeat="item in tab.subItemList" data-nodrag ui-tree-node data-collapsed="false" class="noBorder">
									<div class="nodo_logico expander-icon" data-nodrag>
										<div ui-tree-handle class="smallTree" style=" cursor: pointer;">
											<md-list>
												<md-list-item  class=" SecondaryOnLeft  " ng-click="this.toggle()"> 
													<div class="indicator-child"></div>
													
													<p style="font-weight: bold;">{{item.alias | uppercase}}</p>
						
													<md-icon ng-disabled="true" class="md-secondary sm-font-icon "
														aria-label="Chat" md-font-icon="fa fa-angle-down "
														style=" left: 0px;  margin: 5px 0px 0 17px!important; "
														ng-show="!collapsed">
													</md-icon>
										
													<md-icon ng-disabled="true"
														class=" sm-font-icon expandericon" aria-label="Chat2"
														md-font-icon="fa fa-angle-right " ng-show="collapsed">
													</md-icon> 
									
												</md-list-item>
								
											</md-list>
										</div>
									</div>
			
									<ol	ui-tree-nodes="options" ng-model="item.word" ng-class="{hideChildren: collapsed}">
										<li ng-repeat="itemW in item.word" ui-tree-node data-collapsed="true" class="figlioVisibile">
											<div  class="figlio_vocabolo smallTree">
												<md-list class="noPadding"> 
													<md-list-item ng-click="showInfoWORD(itemW)" class="noPaddingList">
														<div class="indicator-child"></div>
														
														<md-icon ng-disabled="true" class="md-secondary sm-font-icon "
															aria-label="Chat" md-font-icon="fa fa-angle-right "
															style=" left: 0px;  margin: 5px 0px 0 17px!important; " >
														</md-icon>
													
														<p style="margin-left: 10px;" >{{itemW.WORD | uppercase}}</p>
											 		</md-list-item> 
												</md-list>
											</div>
										</li>
									</ol>
			
								</li>
							</ol>
						</div>
					</md-content>
				</div>
			
		
		
		
			</md-tab>
			
		</md-tabs>


		</div>
		
		<div flex  style="height: 100%   ;  padding-left: 10px;">

			<md-toolbar class="md-blue minihead">
			<div class="md-toolbar-tools">
				<h4 class="md-flex">{{translate.load("sbi.generic.details");}}</h4>
			</div>
			</md-toolbar>
			
			
			<md-content class="ToolbarBox miniToolbar infoBox" >

		
			<bread-crumb ng-model=storyItem item-name='WORD' selected-index='selectedIndex' selected-item='selectedWord'  control='breadControl'>
			</bread-crumb>
		
			<p ng-if="selectedWord.noDataFound!=undefined">{{selectedWord.noDataFound}}</p>
			<div  layout="column" ng-if="selectedWord!=undefined && selectedWord.noDataFound==undefined">
				<ul>
					<li><span>{{translate.load("sbi.glossary.word");}}:</span>
					<p>{{selectedWord.WORD}}</p></li>
					<li><span>{{translate.load("sbi.glossary.status");}}:</span>
					<p ng-if="selectedWord.STATE_NM==undefined"></p>
						<p ng-if="selectedWord.STATE_NM!=undefined">
							{{translate.load(selectedWord.STATE_NM);}}</p>
						</p></li>
					<li><span>{{translate.load("sbi.glossary.category");}}:</span>
					<p ng-if="selectedWord.CATEGORY_NM==undefined"></p>
						<p ng-if="selectedWord.CATEGORY_NM!=undefined">
							{{translate.load(selectedWord.CATEGORY_NM);}}</p>
						</p></li>
					<li><span>{{translate.load("sbi.glossary.description");}}:</span>
					<p>{{selectedWord.DESCR}}</p></li>
					<li><span>{{translate.load("sbi.glossary.formula");}}:</span>
					<p>{{selectedWord.FORMULA}}</p></li>
					<li class="sub-list"><span>{{translate.load("sbi.glossary.link");}}:</span>
						<ul>
							<li ng-repeat="lnk in selectedWord.LINK " ><a ng-click="showInfoWORD(lnk,true)" style="text-decoration: underline;cursor: pointer;">{{lnk.WORD}}</a> <a ng-if="!$last">- </a></li>
						</ul></li>
					<li><span>{{translate.load("sbi.glossary.attributes");}}:</span>
						<ul>
							<li style="display: list-item;"
								ng-repeat="attr in selectedWord.SBI_GL_WORD_ATTR ">{{attr.ATTRIBUTE_NM}}:
								<ul>
									<li>{{attr.VALUE}}</li>
								</ul>
							</li>
						</ul></li>

			</ul>
			</div>
			</md-content>

		</div>
	</div>

</body>
</html>