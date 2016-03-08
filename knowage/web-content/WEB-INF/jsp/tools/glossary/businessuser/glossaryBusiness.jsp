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


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
// check for user profile autorization
// 		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		boolean canSee=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ROLE_TYPE_USER, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS});
		
%>

 <% if(canSee ){ %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryWordManager">

<head>

	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		   
	<!-- glossary tree -->
	<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/tree-style.css">
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/businessuser/glossary.js"></script>
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/glossary/glossaryCustomStyle.css", currTheme)%>">
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/glossary/generalStyle.css", currTheme)%>">
	
</head>


<body class="bodyStyle">

	<div ng-controller="Controller as ctrl" class="h100">
		

		<div class="preloader" ng-show="ctrl.showPreloader">
			<md-progress-circular class="md-hue-2" md-mode="indeterminate"></md-progress-circular>
		</div>


	<angular-list-detail layout-fill>
		<list label="translate.load('sbi.glossary.word')" new-function="ctrl.newWordFunction">
		<div layout="column" layout-wrap layout-fill class="absolute" >
			<div flex="60" class="wordListBox"> 
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
					
					
				</div>


				<div layout="column" flex layout-wrap class="glossaryListBox">
					<md-toolbar class="md-blue minihead">
					<div class="md-toolbar-tools">

						<div>{{translate.load("sbi.glossary.glossary");}}</div>
						<md-button ng-click="ctrl.createNewGlossary()"
							class="md-fab" aria-label="add word"
							style="position:absolute; right:11px; top:0px;"> <md-icon
							md-font-icon="fa fa-plus "
							style="  margin-top: 6px ; color: white;"></md-icon> 
						</md-button>
					</div>
					</md-toolbar>

					<md-content flex class=" noBorder ">
						<angular-list layout-fill class="absolute"
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
		</list>
		
		<detail label="ctrl.selectedGloss.GLOSSARY_NM==undefined ? translate.load('sbi.glossary.select.messages') :  ctrl.selectedGloss.GLOSSARY_NM"  ng-if="ctrl.activeTab=='Glossari'">
			<md-content layout-fill class="absolute">
<!-- 				 	<p ng-if="ctrl.selectedGloss.GLOSSARY_NM==undefined">{{translate.load("sbi.glossary.select.messages");}}</p> -->
					<md-checkbox ng-model="ctrl.safeMode" aria-label='{{translate.load("sbi.generic.safeMode");}}' class="safeModeCheckBox" 
					ng-if="ctrl.selectedGloss.GLOSSARY_NM!=undefined ">
   			 					{{translate.load("sbi.generic.safeMode");}}
  									 </md-checkbox>
   
						<glossary-tree layout-fill
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
 
 
			</md-content >
		</detail>
		
		<detail label="ctrl.newWord.WORD==undefined ? '' : ctrl.newWord.WORD" ng-if="ctrl.activeTab=='Vocabolo'" disable-save-button="ctrl.newWord.DESCR.length === 0  || ctrl.newWord.WORD.length === 0" save-function="ctrl.addWord" cancel-function="ctrl.cancelAlterWordFunction">
			<md-content flex  style="    border: 1px solid #B0BEC5;    padding: 0px 4px;">
				<form name="wordForm" class="wordForm " novalidate style="    padding-top: 4px;">
					<div layout="row" layout-wrap>
						<div flex="100">
	
								<md-input-container class="small" > <!-- Use floating label instead of placeholder -->
								<label>{{translate.load("sbi.glossary.word");}}</label>
<!-- 										 <md-icon md-font-icon="fa  fa-newspaper-o " class="wo">  </md-icon>  -->
								 <input ng-model="ctrl.newWord.WORD" maxlength="100"type="text"> 
								</md-input-container>
							</div>
						</div>


						<div layout="row" layout-wrap>
							<div flex="100">
							 <md-input-container class="md-block small counter">
						        <label>{{translate.load("sbi.glossary.description");}}</label>
						        <textarea id="descrText" ng-model="ctrl.newWord.DESCR" columns="1" md-maxlength="500" maxlength="500"></textarea>
						      </md-input-container>
							</div>
						</div>
								
						<div layout="row"  layout-wrap>
							<div flex="50">
							 <md-input-container class="small">
						       	 	<label>{{translate.load("sbi.glossary.status");}}</label>
							        <md-select  ng-model="ctrl.newWord.STATE">
							         <md-option value="-1"></md-option>
							          <md-option ng-repeat="st in ctrl.state" value="{{st.VALUE_ID}}">
							           {{translate.load(st.VALUE_NM)}}
							          </md-option>
							        </md-select>
						      	</md-input-container>
							</div>
	
							<div flex="50">
								 <md-input-container class="small">
						       	 	<label> {{translate.load("sbi.glossary.category");}}</label>
							        <md-select  ng-model="ctrl.newWord.CATEGORY">
							         <md-option value="-1"></md-option>
							          <md-option ng-repeat="ct in ctrl.category" value="{{ct.VALUE_ID}}">
							           {{translate.load(ct.VALUE_NM)}}
							          </md-option>
							        </md-select>
						      	</md-input-container>
							</div>
						</div>


						<div layout="row" layout-wrap>
							<div flex="100">
							<md-input-container class="md-block small counter">
						        <label>{{translate.load("sbi.glossary.formula");}}</label>
						        <textarea id="formulaText" ng-model="ctrl.newWord.FORMULA" columns="1" md-maxlength="500" maxlength="500"></textarea>
						      </md-input-container>
						      
							</div>
						</div>

							<div layout="row" layout-wrap class="chipsNewWordDiv">
								<div flex="100">
								
									<md-input-container class="input-chips-container md-block small" ng-class="{ 'md-input-has-value-copy' : ctrl.newWord.LINK.length > 0  }">
							        <label>{{translate.load("sbi.glossary.link");}}</label>
							        
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


							        
							        <div class="linkChips"> 
										<md-contact-chips ng-model="ctrl.newWord.LINK"
											md-contacts="ctrl.querySearch($query)" md-contact-name="WORD" 
											md-require-match="" filter-selected="true">
										
										</md-contact-chips>
									</div>


							      </md-input-container>
							      
								</div>
							</div>

								<div layout="row" layout-wrap>
									
									
									<div flex="40" >
									
										<md-input-container class="small">
									       	 	<label> {{translate.load("sbi.glossary.attributes");}}</label>
										        <md-select  ng-model="ctrl.tmpAttr.Prop" md-on-open="ctrl.loadProperty()"  >
										         <md-option value="-1"></md-option>
										          <md-option ng-value="attr" ng-repeat="attr in ctrl.propertyList">
										         {{attr.ATTRIBUTE_NM}}
										          </md-option>
										        </md-select>
									      	</md-input-container>
									</div>
			
										<div flex>
											<md-input-container class="md-block small counter">
										        <label>{{translate.load("sbi.generic.value");}}</label>
										        <textarea  ng-model="ctrl.tmpAttr.Val" columns="1" md-maxlength="500" maxlength="500"></textarea>
										     </md-input-container>
								      
										</div>
			
										<div flex="initial" layout-align="top center">
										<md-button
											ng-click="ctrl.addProp(ctrl.tmpAttr)"
											ng-disabled=" ctrl.tmpAttr.Prop.length==0 || ctrl.tmpAttr.Prop=='-1' || ctrl.tmpAttr.Prop==null  || ctrl.tmpAttr.Val.length==0 || ctrl.tmpAttr.Val == null "
											class="md-fab   md-mini" aria-label="Aggiungi_Attributo">
										<md-tooltip> {{translate.load("sbi.generic.add");}}  </md-tooltip> 
										<md-icon md-font-icon="fa fa-plus fa-2x" style="   margin-left: 2px;"></md-icon>
										</md-button>
										</div>
			
								</div>

								<div layout="row" layout-wrap>
			
									<div flex="100">
			
										<md-list> 
											<md-list-item class="md-2-line box-list-option" ng-repeat="attr in ctrl.newWord.SBI_GL_WORD_ATTR" layout="row" layout-wrap>
										<div class="md-item-text md-whiteframe-z1"  flex>
											<p class="margin5 wrapText">
												<span>{{attr.ATTRIBUTE_NM}}</span>
											</p>
											
											
											<md-input-container class=" textareaInputBox">
												<textarea  class="attText" style="   padding-top: 0px !important; "ng-model="attr.VALUE" columns="1" maxlength="500">
												</textarea>
											</md-input-container>
			
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
		
		</detail>
		<detail ng-if="false">
		<md-content  layout="column" class="absolute" layout-fill>
			<md-content   layout-fill ng-if="ctrl.activeTab=='Glossari'"> 
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
						<div ng-if="ctrl.activeTab=='Vocabolo'" layout="column" style="    padding: 10px;     height: calc(100% - 20px); ">
							<md-toolbar class="md-blue minihead" >
								<div class="md-toolbar-tools h100" >
									<div style="   text-align: center;    font-size: 30px;">{{translate.load("sbi.glossary.word");}}</div>
									<div  style="position: absolute;right: 0px" class="h100">
										<md-button type="button" tabindex="-1" class="md-raised md-ExtraMini "  style=" margin-top: 2px;" ng-click="ctrl.createNewWord(true)" >
											{{translate.load("sbi.browser.defaultRole.cancel");}}
										</md-button>
										<md-button type="button"ng-click="ctrl.addWord()" class="md-raised md-ExtraMini " style=" margin-top: 2px;"
											ng-disabled="ctrl.newWord.DESCR.length === 0  || ctrl.newWord.WORD.length === 0">{{translate.load("sbi.browser.defaultRole.save");}}
										</md-button>
									</div>
						 	  	</div>
							</md-toolbar>
			
						<md-content flex  style="    border: 1px solid #B0BEC5;    padding: 0px 4px;">

							<form name="wordForm" class="wordForm " novalidate style="    padding-top: 4px;">
					

								<div layout="row" layout-wrap>
									<div flex="100">
			
										<md-input-container class="small" > <!-- Use floating label instead of placeholder -->
										<label>{{translate.load("sbi.glossary.word");}}</label>
<!-- 										 <md-icon md-font-icon="fa  fa-newspaper-o " class="wo">  </md-icon>  -->
										 <input ng-model="ctrl.newWord.WORD" maxlength="100"type="text"> 
										</md-input-container>
									</div>
								</div>


								<div layout="row" layout-wrap>
									<div flex="100">
									 <md-input-container class="md-block small counter">
								        <label>{{translate.load("sbi.glossary.description");}}</label>
								        <textarea id="descrText" ng-model="ctrl.newWord.DESCR" columns="1" md-maxlength="500" maxlength="500"></textarea>
								      </md-input-container>
									</div>
								</div>
								
								<div layout="row"  layout-wrap>
									<div flex="50">
									 <md-input-container class="small">
								       	 	<label>{{translate.load("sbi.glossary.status");}}</label>
									        <md-select  ng-model="ctrl.newWord.STATE">
									         <md-option value="-1"></md-option>
									          <md-option ng-repeat="st in ctrl.state" value="{{st.VALUE_ID}}">
									           {{translate.load(st.VALUE_NM)}}
									          </md-option>
									        </md-select>
								      	</md-input-container>
									</div>
			
									<div flex="50">
										 <md-input-container class="small">
								       	 	<label> {{translate.load("sbi.glossary.category");}}</label>
									        <md-select  ng-model="ctrl.newWord.CATEGORY">
									         <md-option value="-1"></md-option>
									          <md-option ng-repeat="ct in ctrl.category" value="{{ct.VALUE_ID}}">
									           {{translate.load(ct.VALUE_NM)}}
									          </md-option>
									        </md-select>
								      	</md-input-container>
									</div>
								</div>


								<div layout="row" layout-wrap>
									<div flex="100">
									<md-input-container class="md-block small counter">
								        <label>{{translate.load("sbi.glossary.formula");}}</label>
								        <textarea id="formulaText" ng-model="ctrl.newWord.FORMULA" columns="1" md-maxlength="500" maxlength="500"></textarea>
								      </md-input-container>
								      
									</div>
								</div>

								<div layout="row" layout-wrap>
									<div flex="100">
									
										<md-input-container class="input-chips-container md-block small" ng-class="{ 'md-input-has-value-copy' : ctrl.newWord.LINK.length > 0  }">
								        <label>{{translate.load("sbi.glossary.link");}}</label>
								        
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


								        
								        <div class="linkChips"> 
											<md-contact-chips ng-model="ctrl.newWord.LINK"
												md-contacts="ctrl.querySearch($query)" md-contact-name="WORD" 
												md-require-match="" filter-selected="true">
											
											</md-contact-chips>
										</div>


								      </md-input-container>
								      
									</div>
								</div>

								<div layout="row" layout-wrap>
									
									
									<div flex="40" >
									
										<md-input-container class="small">
									       	 	<label> {{translate.load("sbi.glossary.attributes");}}</label>
										        <md-select  ng-model="ctrl.tmpAttr.Prop" md-on-open="ctrl.loadProperty()"  >
										         <md-option value="-1"></md-option>
										          <md-option ng-value="attr" ng-repeat="attr in ctrl.propertyList">
										         {{attr.ATTRIBUTE_NM}}
										          </md-option>
										        </md-select>
									      	</md-input-container>
									</div>
			
										<div flex>
											<md-input-container class="md-block small counter">
										        <label>{{translate.load("sbi.generic.value");}}</label>
										        <textarea  ng-model="ctrl.tmpAttr.Val" columns="1" md-maxlength="500" maxlength="500"></textarea>
										     </md-input-container>
								      
										</div>
			
										<div flex="initial" layout-align="top center">
										<md-button
											ng-click="ctrl.addProp(ctrl.tmpAttr)"
											ng-disabled=" ctrl.tmpAttr.Prop.length==0 || ctrl.tmpAttr.Prop=='-1' || ctrl.tmpAttr.Prop==null  || ctrl.tmpAttr.Val.length==0 || ctrl.tmpAttr.Val == null "
											class="md-fab   md-mini" aria-label="Aggiungi_Attributo">
										<md-tooltip> {{translate.load("sbi.generic.add");}}  </md-tooltip> 
										<md-icon md-font-icon="fa fa-plus fa-2x" style="   margin-left: 2px;"></md-icon>
										</md-button>
										</div>
			
								</div>

								<div layout="row" layout-wrap>
			
									<div flex="100">
			
										<md-list> 
											<md-list-item class="md-2-line box-list-option" ng-repeat="attr in ctrl.newWord.SBI_GL_WORD_ATTR" layout="row" layout-wrap>
										<div class="md-item-text md-whiteframe-z1"  flex>
											<p class="margin5 wrapText">
												<span>{{attr.ATTRIBUTE_NM}}</span>
											</p>
											
											
											<md-input-container class=" textareaInputBox">
												<textarea  class="attText" style="   padding-top: 0px !important; "ng-model="attr.VALUE" columns="1" maxlength="500">
												</textarea>
											</md-input-container>
			
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
		</md-content>
		</detail>
	</angular-list-detail>



</div>


</body>
</html>

<%}else{ %>
NOT AUTHORIZED
<%} %>
