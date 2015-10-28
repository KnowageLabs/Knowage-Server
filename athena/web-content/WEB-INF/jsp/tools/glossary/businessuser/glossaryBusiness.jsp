<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/includeMessageResource.jspf"%>

<%
// check for user profile autorization
		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		boolean canSee=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ROLE_TYPE_USER, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS});
		
%>

 <% if(canSee ){ %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryWordManager">

<head>

	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	

	<!-- glossary tree -->
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/tree-style.css">
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>
	
	<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/glossary/businessuser/glossary.js"></script>
	
	
</head>


<body class="bodyStyle">

	<div ng-controller="Controller as ctrl" class="h100">
		

		<div class="preloader" ng-show="ctrl.showPreloader">
			<md-progress-circular class="md-hue-2" md-mode="indeterminate"></md-progress-circular>
		</div>



		<div layout="row" layout-wrap class="h100">

			<!-- 			LEFT BOX WORD -->
			<div flex="20"  class="leftBox_word" >


				<div layout="column" class="wordListBox" style="height: 60%;">

					<md-toolbar class="md-blue minihead ">
					<div class="md-toolbar-tools">

						<div>{{translate.load("sbi.glossary.word");}}</div>
						<md-button ng-click="ctrl.createNewWord(false)"
							class="md-fab   md-ExtraMini addButton" aria-label="add word"
							style="position:absolute; right:11px; top:0px;"> <md-icon
							md-font-icon="fa fa-plus"
							style="  margin-top: 6px ; color: white;"></md-icon> </md-button>
					</div>
					</md-toolbar>
					
					<md-content layout-padding class="ToolbarBox miniToolbar noBorder ">
						<angular-list layout-fill 
						id='word' 
						enable-drag=true
						enable-clone=true
						drag-drop-options=ctrl.TreeOptionsWord
                		ng-model=ctrl.words
                		item-name='WORD'
                		show-search-bar=true  
                		search-function="ctrl.WordLike(searchValue,itemsPerPage)"
                		show-search-preloader="ctrl.showSearchPreloader"
                		page-canged-function="ctrl.pageChanged(newPageNumber,itemsPerPage,searchValue)"
                		total-item-count=ctrl.totalWord
                		menu-option=ctrl.menuOpt
                		>
                		</angular-list>
					</md-content>
					
					
				</div>


				<div layout="column" layout-wrap class="glossaryListBox">
					<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">

						<div>{{translate.load("sbi.glossary.glossary");}}</div>
						<md-button ng-click="ctrl.createNewGlossary($event)"
							class="md-fab   md-ExtraMini addButton" aria-label="add word"
							style="position:absolute; right:11px; top:0px;"> <md-icon
							md-font-icon="fa fa-plus "
							style="  margin-top: 6px ; color: white;"></md-icon> 
						</md-button>
					</div>
					</md-toolbar>

					<md-content layout-padding class="ToolbarBox miniToolbar noBorder ">
						<angular-list layout-fill 
						id='glossary' 
                		ng-model=ctrl.glossary
                		item-name='GLOSSARY_NM'
                		menu-option=ctrl.glossMenuOpt
                		click-function="ctrl.showClickedGlossary(item)"
                		speed-menu-option=ctrl.glossSpeedMenuOpt
                		no-pagination=true
                		/>
                		
					</md-content>


				</div>



			</div>
			
			<div flex  class="rightBox_GLOSS  h100">
				<md-content  layout="column" flex>
<!-- 					<md-content  class="h100"> -->
				 	<md-tabs  md-dynamic-height  class="hideTabs h100" md-border-bottom > 
						<md-tab  label="Glossari"	md-on-select="ctrl.activeTab='Glossari'"	md-active="ctrl.activeTab=='Glossari'">
							<md-content  style="padding-left: 20px;"> 
								<div layout="row" layout-wrap>
									<div flex="100">
										<p ng-if="ctrl.selectedGloss.GLOSSARY_NM==undefined">{{translate.load("sbi.glossary.select.messages");}}</p>
										<md-checkbox ng-model="ctrl.safeMode" aria-label='{{translate.load("sbi.generic.safeMode");}}' class="safeModeCheckBox" 
										ng-if="ctrl.selectedGloss.GLOSSARY_NM!=undefined ">
        			 					{{translate.load("sbi.generic.safeMode");}}
       									 </md-checkbox>
        
											<glossary-tree
											tree-id="GlossTree" 
											tree-options=ctrl.TreeOptions 
											glossary=ctrl.selectedGloss 
											add-child="ctrl.newSubItem(scope,parent)"
											add-word="ctrl.createNewWord(reset,parent)"
											remove-child="ctrl.removeContents(item)"
											modify-child="ctrl.newSubItem(scope,parent,modCont)"
											modify-glossary="ctrl.createNewGlossary(event,glossary)"
											clone-glossary="ctrl.CloneGloss(event,glossary)"
											delete-glossary="ctrl.deleteGlossary(glossary)"
											 drag-logical-node=true
					        				drag-word-node=true
					        				show-info-menu=true
											ng-if="ctrl.selectedGloss.GLOSSARY_NM!=undefined ">
											</glossary-tree>

									</div>
								</div>
							</md-content>
						</md-tab> 

						<md-tab  label="Vocabolo" selected="true" md-on-select="ctrl.activeTab='Vocabolo'"	md-active="ctrl.activeTab=='Vocabolo'" style="margin:10px;"> 
						<div layout="column" style="    padding: 10px;     height: calc(100% - 20px); ">
							<md-toolbar class="md-blue minihead" >
								<div class="md-toolbar-tools h100" >
									<div style="   text-align: center;    font-size: 30px;">{{translate.load("sbi.glossary.word");}}</div>
									<div  style="position: absolute;right: 0px" class="h100">
										<md-button type="button" tabindex="-1" class="md-raised md-ExtraMini "  style=" margin-top: 2px;" ng-click="ctrl.createNewWord(true)" >
											{{translate.load("sbi.browser.defaultRole.cancel");}}
										</md-button>
										<md-button type="button"ng-click="ctrl.addWord(ctrl.words)" class="md-raised md-ExtraMini " style=" margin-top: 2px;"
											ng-disabled="ctrl.newWord.DESCR.length === 0  || ctrl.newWord.WORD.length === 0">{{translate.load("sbi.browser.defaultRole.save");}}
										</md-button>
									</div>
						 	  	</div>
							</md-toolbar>
			
						<md-content flex  style="    border: 1px solid #B0BEC5;    padding: 0px 4px;">

							<form name="wordForm" class="wordForm " novalidate style="    padding-top: 4px;">
					

								<div layout="row" layout-wrap>
									<div flex="100">
			
										<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
										<label>{{translate.load("sbi.glossary.word");}}</label> <md-icon md-font-icon="fa  fa-newspaper-o "
											class="wo"></md-icon> <input ng-model="ctrl.newWord.WORD" maxlength="100"
											type="text"> </md-input-container>
									</div>
								</div>


								<div layout="row" layout-wrap>
									<div flex="100">
										<md-input-container class="md-icon-float textareaInputBox"
											ng-class="{ 'md-input-hasnt-value' : ctrl.newWord.DESCR.length === 0  }">
										<!-- Use floating label instead of placeholder --> <label>{{translate.load("sbi.glossary.description");}}</label>
										<md-icon md-font-icon="fa  fa-file-text-o " class="des"></md-icon>
										<textarea id="descrText" ng-model="ctrl.newWord.DESCR" columns="1"
											md-maxlength="500" maxlength="500"></textarea> </md-input-container>
									</div>
								</div>
								
								<div layout="row"  layout-wrap>
									<div flex="50">
								<md-input-container class="md-icon-float " > 
									<label class="selectLabel" ng-hide="ctrl.newWord.STATE==-1 || ctrl.newWord.STATE.length==0 || ctrl.newWord.STATE==undefined" >State</label>
											<md-icon md-font-icon="fa fa-spinner " class="stato"></md-icon> 
											<md-select 	placeholder='{{translate.load("sbi.generic.select");}} {{translate.load("sbi.glossary.status");}}' ng-model="ctrl.newWord.STATE">
												 <md-option value="-1">{{translate.load("sbi.generic.select");}} {{translate.load("sbi.glossary.status");}}</md-option>
												 <md-option ng-repeat="st in ctrl.state" value="{{st.VALUE_ID}}">{{translate.load(st.VALUE_NM)}}</md-option>
											</md-select> </md-input-container>
									</div>
			
									<div flex="50">
										<md-input-container class="md-icon-float"> 
									<label class="selectLabel" ng-hide="ctrl.newWord.CATEGORY==-1 || ctrl.newWord.CATEGORY.length==0 || ctrl.newWord.CATEGORY==undefined" >Category</label>
									
									<md-icon md-font-icon="fa fa-flag-o " class="categoria"> </md-icon>
									<md-select
											placeholder='{{translate.load("sbi.generic.select");}} {{translate.load("sbi.glossary.category");}}' ng-model="ctrl.newWord.CATEGORY">
											 <md-option value="-1">{{translate.load("sbi.generic.select");}} {{translate.load("sbi.glossary.category");}}</md-option>
											 <md-option
											ng-repeat="ct in ctrl.category" value="{{ct.VALUE_ID}}"> {{translate.load(ct.VALUE_NM)}}</md-option>
										</md-select>
									
								
									</div>
								</div>


								<div layout="row" layout-wrap>
									<div flex="100">
										<md-input-container class="md-icon-float textareaInputBox"
											ng-class="{ 'md-input-hasnt-value' : ctrl.newWord.FORMULA.length === 0  }">
										<!-- Use floating label instead of placeholder --> <label>{{translate.load("sbi.glossary.formula");}}</label>
										<md-icon md-font-icon="fa fa-superscript " class="formu"></md-icon>
										<textarea id="formulaText" ng-model="ctrl.newWord.FORMULA" columns="1"
											md-maxlength="500" maxlength="500"></textarea> </md-input-container>
									</div>
								</div>





								<div layout="row" layout-wrap>
									<div flex="100">
										<md-input-container class="md-icon-float"
											ng-class="{ 'md-input-has-value-copy' : ctrl.newWord.LINK.length > 0  }">
										<div>
											<div  id="chipsTree" ui-tree="ctrl.TreeOptionsChips"
												data-drag-enabled="true" data-drag-delay="500"
												data-empty-placeholder-enabled="false" class="chipsTree">
												<ol id="olchiproot" ui-tree-nodes ng-model="ctrl.newWord.LINK"
													data-empty-placeholder-enabled="false">
													<li ng-repeat="n in [1]" data-nodrag ui-tree-node
														style="height: 0px; min-height: 0px;">
			
														<div class="angular-ui-tree-empty"></div>
													</li>
			
												</ol>
											</div>
										</div>
			
										<label>{{translate.load("sbi.glossary.link");}}</label> <md-icon md-font-icon="fa fa-link "
											class="lin"></md-icon>
			
										<div class="linkChips">
											<md-contact-chips ng-model="ctrl.newWord.LINK"
												md-contacts="ctrl.querySearch($query)" md-contact-name="WORD" 
												md-require-match="" filter-selected="true">
			<!-- 								<md-chip-template > <strong>{{$chip.WORD | uppercase}}</strong> </md-chip-template>  -->
											
											</md-contact-chips>
										</div>
			
										</md-input-container>
									</div>
								</div>

								<div layout="row" layout-wrap>
									
									
									<div flex="40" style="height: 40px;" layout="row">
									<md-icon md-font-icon="fa fa-folder-o " class="stato"
											style="  margin-top: 25px;"></md-icon>
											
											
									 	<md-select placeholder='{{translate.load("sbi.generic.select");}} {{translate.load("sbi.glossary.attributes");}}' ng-model="ctrl.tmpAttr.Prop" md-on-open="ctrl.loadProperty()" style="    width: 100%;    margin-left: 24px;">
  									   		 <md-option ng-value="attr" ng-repeat="attr in ctrl.propertyList">
  									   		 	{{attr.ATTRIBUTE_NM}}
  									   		 </md-option>
    								 	</md-select>
	
									</div>
			

										<div flex="40">
										<md-input-container class=" attr_Value md-icon-float textareaInputBox" ng-class="{ 'md-input-hasnt-value' : ( ctrl.tmpAttr.Val.length === 0 ||ctrl.tmpAttr.Val == null)  }"> 
										<label>{{translate.load("sbi.generic.value");}}</label> <textarea ng-model="ctrl.tmpAttr.Val" maxlength="500"
											></textarea> </md-input-container>
									
										</div>
			
										<div flex="20" layout="row" layout-align="center center"	>
										<md-button
											ng-click="ctrl.addProp(ctrl.tmpAttr)"
											ng-disabled=" ctrl.tmpAttr.Prop.length==0 || ctrl.tmpAttr.Prop==null  || ctrl.tmpAttr.Val.length==0 || ctrl.tmpAttr.Val == null "
											class="md-fab   md-mini" aria-label="Aggiungi_Attributo">
										<md-tooltip> {{translate.load("sbi.generic.add");}} {{translate.load("sbi.glossary.attributes");}} </md-tooltip> <md-icon
											md-font-icon="fa fa-plus fa-2x" style="   margin-left: 2px;"></md-icon>
										</md-button>
										</div>
			
								</div>

								<div layout="row" layout-wrap>
			
			
			
									<div flex="100">
			
										<md-list> <md-list-item
											class="md-2-line box-list-option"
											ng-repeat="attr in ctrl.newWord.SBI_GL_WORD_ATTR" layout="row"
											layout-wrap>
										<div class="md-item-text md-whiteframe-z1"
											 flex>
											<p class="margin5 wrapText">
												<span>{{attr.ATTRIBUTE_NM}}</span>
												<!-- 									<input class="transparent_input smallFont"	ng-model="attr.ATTRIBUTE_NM" type="text">  -->
			
											</p>
											
											
											<md-input-container class=" textareaInputBox"		>
											<textarea  class="attText" style="   padding-top: 0px !important; "ng-model="attr.VALUE" columns="1"
											 maxlength="500"></textarea></md-input-container>
			
											<md-button ng-click="ctrl.removeProp(attr)"
												class="md-fab   md-ExtraMini" aria-label="add word"
												style="    background-color: rgb(176, 190, 197) !important;
			  										border-radius: 0px;
			 										 position: absolute;
			  											top: 0;
			  											right: 0;
			  											margin: 0;">
											<md-icon md-font-icon="fa fa-times" style="color: rgb(0, 0, 0);  margin-top: 5px;"></md-icon>
											</md-button>
			
			
										</div>
										</md-list-item> </md-list>
			
									</div>
								</div>


						</form>
					</md-content> 
					</div>
				</md-tab> 
			</md-tabs> 
		</md-content>

	</div>
   </div>
</div>


</body>
</html>

<%}else{ %>
<%@include file="/WEB-INF/jsp/tools/geo/mapPage.jsp"%>
<%} %>