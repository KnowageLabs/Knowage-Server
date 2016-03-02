<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryHelpOnLine">

<head>

<meta http-equiv="x-ua-compatible" content="IE=EmulateIE9">
<meta name="viewport" content="width=device-width">
<!-- JavaScript -->

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- 	breadCrumb -->
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>

<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/bread-crumb.css">

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


<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/RestService.js"></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/finaluser/glossaryHelpOnline.js"></script>

<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/generalStyle.css">

<style type="text/css">
.halfperc {
	height: 50% !important;
}

.halfperc2 {
	height: calc(50% - 16px);
}

.fullperc2 {
	height: calc(100% - 16px);
}

.datasetcol {
	position: absolute;
	width: 100%;
	bottom: 0;
}

.noPaddingList .md-button {
	padding: 0px !important;
}

md-tabs.singleItem md-ink-bar, md-tabs.singleItem md-tab-item, md-tabs.singleItem md-pagination-wrapper
	{
	width: 100%;
}
</style>

</head>


<body class="bodyStyle" ng-controller="Controller">
	<div layout="row" layout-fill style="position: absolute; height: 100%; padding: 10px;">

		<div flex="30" flex-lg="30" flex-md="40" style="height: 100%"
			ng-if="type!='WORD'">
			<md-tabs class="mini-tabs" ng-class="{'singleItem' : data.length==1}">
				<md-tab ng-repeat="tab in data" label="{{tab.type}}" layout-fill style="height: 100%;">
					<p style="margin: 0; text-align: center; height: 16px; background-color: #E8E8E8">{{tab.title}}</p>
		
					<angular-list layout-fill class="fullperc2"
							ng-class="{ 'halfperc' : (tab.subItemList!=undefined && tab.subItemList.length!=0)  }"
							style="min-height: 0px !important;    position: absolute;"
							id='wordList{{$index}}' ng-model=tab.itemList item-name='WORD'
							click-function="showInfoWORD(item)" highlights-selected-item=true
							show-search-bar=true selected-item=selectedWord>
					</angular-list>
		
					<div class="halfperc2 datasetcol"
							ng-if="tab.subItemList!=undefined && tab.subItemList.length!=0">
						<md-toolbar class="md-blue xs-head">
							<div class="md-toolbar-tools">
								<div ng-if="type!='BUSINESS_CLASS'">
									{{translate.load("sbi.ds.metadata.dataset.title");}}</div>
								<div ng-if="type=='BUSINESS_CLASS'">
									{{translate.load("sbi.glossary.businessclass.column");}}</div>
							</div>
						</md-toolbar>
		
						<md-content layout-padding style="height:Calc(100% - 32px); padding-bottom: 8px;">
		
							<div id="Tree-Word-Dataset" ui-tree="" data-drag-enabled="true"
									data-clone-enabled="true">
								<ol ui-tree-nodes ng-model="tab.subItemList"
										ng-class="{hideChildren: collapsed}">
									<li ng-repeat="item in tab.subItemList" data-nodrag ui-tree-node
											data-collapsed="false" class="noBorder">
										<div class="nodo_logico expander-icon" data-nodrag>
											<div ui-tree-handle class="smallTree" style="cursor: pointer;">
												<md-list> 
													<md-list-item class=" SecondaryOnLeft  "
															ng-click="this.toggle()">
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
			
										<ol ui-tree-nodes="options" ng-model="item.word"
												ng-class="{hideChildren: collapsed}">
											<li ng-repeat="itemW in item.word" ui-tree-node
													data-collapsed="true" class="figlioVisibile">
												<div class="figlio_vocabolo smallTree">
													<md-list class="noPadding"> 
														<md-list-item ng-click="showInfoWORD(itemW)" class="noPaddingList">
															<div class="indicator-child"></div>
					
															<md-icon ng-disabled="true" class="md-secondary sm-font-icon "
																	aria-label="Chat" md-font-icon="fa fa-angle-right "
																	style=" left: 0px;  margin: 5px 0px 0 17px!important; ">
															</md-icon>
					
															<p style="margin-left: 10px;">{{itemW.WORD | uppercase}}</p>
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

		<div flex style="height: 100%; padding-left: 10px;">
			<md-toolbar class="md-blue minihead">
				<div class="md-toolbar-tools">
					<h4 class="md-flex">{{translate.load("sbi.generic.details");}}</h4>
				</div>
			</md-toolbar>

			<md-content class="ToolbarBox miniToolbar infoBox"> 
				<bread-crumb ng-model=storyItem item-name='WORD'
						selected-index='selectedIndex' selected-item='selectedWord'
						control='breadControl'></bread-crumb>

				<p ng-if="selectedWord.noDataFound!=undefined">{{selectedWord.noDataFound}}</p>
				<div layout="column" ng-if="selectedWord!=undefined && selectedWord.noDataFound==undefined">
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
								<li ng-repeat="lnk in selectedWord.LINK "><a
									ng-click="showInfoWORD(lnk,true)"
									style="text-decoration: underline; cursor: pointer;">{{lnk.WORD}}</a>
									<a ng-if="!$last">- </a></li>
							</ul>
						</li>
						<li>
							<span>{{translate.load("sbi.glossary.attributes");}}:</span>
							<ul>
								<li style="display: list-item;"
									ng-repeat="attr in selectedWord.SBI_GL_WORD_ATTR ">{{attr.ATTRIBUTE_NM}}:
									<ul>
										<li>{{attr.VALUE}}</li>
									</ul>
								</li>
							</ul>
						</li>
	
					</ul>
				</div>
			</md-content>

		</div>
	</div>
</body>
</html>
