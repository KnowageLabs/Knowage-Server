<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="AIDA_GESTIONE-VOCABOLI">

<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">


<link rel="stylesheet"
	href="https://fonts.googleapis.com/css?family=RobotoDraft:300,400,500,700,400italic">

<link rel="stylesheet"
	href="https://rawgit.com/angular/bower-material/master/angular-material.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular.js"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular-animate.min.js"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular-aria.min.js"></script>
<script
	src="https://rawgit.com/angular/bower-material/master/angular-material.js"></script>

<!-- <script -->
<!-- 	src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular.min.js"></script> -->


<link rel="stylesheet" type="text/css"
	href="/athena/themes/glossary/css/Stili.css">
<link rel="stylesheet" type="text/css"
	href="/athena/themes/glossary/css/gestione_glossario.css">
<link rel="stylesheet"
	href="/athena/js/glossary/angulartree/angular-ui-tree.min.css">
<script type="text/javascript"
	src="/athena/js/glossary/angulartree/angular-ui-tree.js"></script>
<script type="text/javascript"
	src="/athena/js/glossary/contextmenu/ng-context-menu.min.js"></script>

<script type="text/javascript" src="/athena/js/glossary/glossary.js"></script>




<body>


	<div ng-controller="Controller as ctrl">


		<div layout="row" layout-wrap>

			<!-- 			LEFT BOX WORD -->
			<div flex="25" class="leftBox_word">
				<div layout="column">




					<md-toolbar class="md-blue">
					<div class="md-toolbar-tools">

						<div layout="row" layout-wrap class="search_box">
							<div flex="30">


								<md-button ng-click="ctrl.createNewWord()"
									class="md-fab   md-mini" aria-label="add word"
									style="margin: 18px;  margin-left: 0px; box-shadow: none ;  background-color: transparent !important;">
								<md-icon md-font-icon="fa fa-plus fa-2x"
									style="  color: greenyellow;"></md-icon> </md-button>




							</div>

							<div flex="70">


								<md-input-container md-no-float
									style="padding-bottom: 17px; padding-top: 17px;">
								<md-icon md-font-icon="fa fa-search "
									style="  margin-top: 18px;  margin-left: 13px; color: white"></md-icon>
								<input ng-model="filter_word.WORD" type="text"
									placeholder="Search"> </md-input-container>


							</div>

						</div>
					</div>
					</md-toolbar>
					<md-content layout-padding>

					<div ui-tree="ctrl.TreeOptionsWord" data-drag-enabled="true"
						data-drag-delay="500" data-clone-enabled="true">

						<ol ui-tree-nodes ng-model="ctrl.words" data-nodrop-enabled="true">
							<li ui-tree-node
								ng-repeat="word in ctrl.words | filter:filter_word:strict ">
								<div ui-tree-handle style="border: none;"">
									<md-list> <md-list-item ng-click="1==1"
										ng-repeat="n in [1]" context-menu
										data-target="WordMenu-{{word.WORD}}" class="smallListItem"
										ng-class="{ 'highlight': highlight, 'expanded' : expanded }">

									<p>{{ word.WORD | uppercase }}</p>

									<!-- 					menu contestuale -->
									<div class="dropdown position-fixed" style="z-index: 999;"
										id="WordMenu-{{ word.WORD }}">
										<md-list class="dropdown-menu" role="menu"> <md-list-item
											ng-click='panel.highlight = true ;ctrl.modifyWord(word)'
											role="menuitem" tabindex="1">
										<p>Modifica</p>
										</md-list-item> <md-list-item
											ng-click='panel.highlight = false; ctrl.deleteWord(word)'
											role="menuitem" tabindex="1">
										<p>Elimina</p>
										</md-list-item> </md-list>
									</div>


									</md-list-item> </md-list>
								</div>
							</li>
						</ol>
					</div>
					</md-content>




				</div>






			</div>
			<div flex="75" offset="25" style="padding-bottom: 100px;">

				<md-content> <md-tabs md-dynamic-height
					md-border-bottom> <md-tab label="Glossari"
					md-on-select="ctrl.activeTab='Glossari'"
					md-active="ctrl.activeTab=='Glossari'"> <md-content
					class="md-padding" style="padding-bottom: 150px;"> <!-- 					<md-toolbar> -->
				<!-- 				<div class="md-toolbar-tools"> --> <!-- 					<md-button ng-click="ctrl.createNewGlossary($event)" -->
				<!-- 						class="md-fab   md-mini" aria-label="add word" --> <!-- 						style="margin: 18px;  margin-left: 0px; box-shadow: none ;  background-color: transparent !important;"> -->
				<!-- 					<md-icon md-font-icon="fa fa-plus fa-2x" --> <!-- 						style="  color: greenyellow;"></md-icon> </md-button> -->

				<!-- 				</div> --> <!-- 				</md-toolbar>  --> <md-content
					layout-padding>

				<div layout="row" layout-wrap>
					<div flex="25">

						<md-list> <md-list-item
							ng-click="ctrl.createNewGlossary($event)">
						<p style="color: green;">+ Aggiungi</p>
						</md-list-item> <md-list-item class="smallListItem"
							ng-click="ctrl.selectedGloss=gloss"
							ng-repeat="gloss in ctrl.glossary ">

						<div context-menu data-target="Gloss-{{ gloss.GLOSSARY_NM}}"
							ng-class="{ 'highlight': highlight, 'expanded' : expanded }"
							style="width: 100%;">
							<p>{{ gloss.GLOSSARY_NM | uppercase }}</p>
						</div>

						<!-- 					menu contestuale glossario -->
						<div class="dropdown position-fixed" style="z-index: 999;"
							id="Gloss-{{  gloss.GLOSSARY_NM }}">
							<md-list class="dropdown-menu" role="menu"
								style="  margin-top: -49px;  margin-left: -275px;"> <md-list-item
								ng-click='ctrl.newSubItemRootGloss(ctrl.selectedGloss)'
								role="menuitem" tabindex="1">
							<p>Aggiungi Nodo Logico</p>
							</md-list-item> <md-list-item
								ng-click='ctrl.newSubItemRootGloss(ctrl.selectedGloss)'
								role="menuitem" tabindex="1">
							<p>Aggiungi Vocabolo</p>
							</md-list-item> <md-list-item ng-click='ctrl.deleteGloss(ctrl.selectedGloss)'
								role="menuitem" tabindex="1">
							<p>Elimina</p>
							</md-list-item> </md-list>
						</div>

						<!-- 						fine menu contestuale albero --> </md-list-item> </md-list>

					</div>
					<div flex="75">




						<!-- Nested list template -->
						<script type="text/ng-template" id="items_renderer.html">
			  <div context-menu data-target="WordTree-{{item.CONTENT_NM}}"	ng-class="{ 'highlight': highlight, 'expanded' : expanded }">
	
               	<div ui-tree-handle  > 


		<div ng-if=" item.CONTENT_NM != undefined" class="nodo_logico">
						<md-list> 
						<md-list-item	class="SecondaryOnLeft" ng-click="1==1" >	

						<p style="  margin-left: 30px;"><input class="transparent_input" type="text" name="nome" ng-model="item.CONTENT_NM"   > </p>
						<md-icon class="md-secondary sm-font-icon " 	ng-click="toggle(this)" aria-label="Chat"	md-font-icon="fa fa-angle-down " style=" right: 0px;"  ng-show="!collapsed"></md-icon>
						<md-icon class=" sm-font-icon expandericon"	ng-click="toggle(this)" aria-label="Chat2"	md-font-icon="fa fa-angle-right "  ng-show="collapsed"></md-icon>
						</md-list-item> 

						</md-list>
				 

		</div>

			<div ng-if="item.WORD_ID!= undefined " class="figlio_vocabolo" >
						<md-list> 
						<md-list-item	class="SecondaryOnLeft" ng-click="1==1" >	

						<p style="  margin-left: 30px;" >{{item.WORD}}</p>
						<md-icon class="md-secondary sm-font-icon " 	ng-click="toggle(this)" aria-label="Chat"	md-font-icon="fa fa-angle-down " style=" right: 0px;"  ng-show="!collapsed"></md-icon>
						<md-icon class=" sm-font-icon expandericon"	ng-click="toggle(this)" aria-label="Chat2"	md-font-icon="fa fa-angle-right "  ng-show="collapsed"></md-icon>
						</md-list-item> 

						</md-list>
			</div>


				 </div>


				  </div>


						<!-- 					menu contestuale albero -->
						<div class="dropdown position-fixed" style="z-index: 999;"
							id="WordTree-{{ item.CONTENT_NM }}">
							<md-list class="dropdown-menu" role="menu"
								style="  margin-top: -49px;  margin-left: -275px;"> <md-list-item
								ng-click='ctrl.newSubItem(this,item)' role="menuitem"
								tabindex="1" ng-if=" item.CONTENT_NM != undefined  && ctrl.hasVocabolaryChild(item)">
							<p>Nuovo Nodo Logico</p>
							</md-list-item>

 							<md-list-item
								ng-click='ctrl.newSubItem(this,item)' role="menuitem"
								tabindex="2" ng-if=" item.CONTENT_NM != undefined">
							<p>Nuovo Vocabolo</p>
							</md-list-item>  <md-list-item ng-click='ctrl.remove(this)' role="menuitem"
								tabindex="3">
							<p>Elimina</p>
							</md-list-item> </md-list>
						</div>

<!-- 						fine menu contestuale albero -->

			
				
                <ol ng-if=" item.CONTENT_NM != undefined" ui-tree-nodes="options" ng-model="item.CHILD" ng-class="{hideChildren: collapsed}">
				<li ng-repeat="item in item.CHILD" ui-tree-node ng-include="'items_renderer.html'" class="figlioVisibile"></li>
				<li  ng-repeat="n in [1]" data-nodrag ui-tree-node class="addFiglioBox" ></li>				
</ol>
				
              </script>



						<div ui-tree="ctrl.TreeOptions" data-drag-enabled="true"
							data-drag-delay="500">
							<ol ui-tree-nodes ng-model="ctrl.selectedGloss">
								<div context-menu
									data-target="WordTreeRoot-{{ctrl.selectedGloss.GLOSSARY_NM}}"
									ng-class="{ 'highlight': highlight, 'expanded' : expanded }">
									<li ui-tree-node data-nodrag ng-repeat="n in [1]">
										<div ui-tree-handle>
											<p>{{ctrl.selectedGloss.GLOSSARY_NM}}</p>
										</div>
								</div>


								<!-- 					menu contestuale radice albero -->
								<div class="dropdown position-fixed" style="z-index: 999;"
									id="WordTreeRoot-{{ ctrl.selectedGloss.GLOSSARY_NM }}">
									<md-list class="dropdown-menu" role="menu"
										style="  margin-top: -49px;  margin-left: -275px;">
									<md-list-item
										ng-click='ctrl.newSubItemRootGloss(ctrl.selectedGloss)'
										role="menuitem" tabindex="1">
									<p>Aggiungi Nodo Logico</p>
									</md-list-item> <md-list-item
										ng-click='ctrl.newSubItemRootGloss(ctrl.selectedGloss)'
										role="menuitem" tabindex="1">
									<p>Aggiungi Vocabolo</p>
									</md-list-item> <md-list-item ng-click='ctrl.deleteGloss(ctrl.selectedGloss)'
										role="menuitem" tabindex="1">
									<p>Elimina</p>
									</md-list-item> </md-list>
								</div>

								<!-- 						fine menu contestuale albero -->




								</li>

								<ol ui-tree-nodes="options"
									ng-model="ctrl.selectedGloss.SBI_GL_CONTENTS"
									ng-class="{hideChildren: collapsed}">
									<li ng-repeat="item in ctrl.selectedGloss.SBI_GL_CONTENTS"
										ui-tree-node ng-include="'items_renderer.html'"></li>

									<li ng-if="ctrl.selectedGloss.SBI_GL_CONTENTS.length == 0 "
										ng-repeat="n in [1]" data-nodrag ui-tree-node
										class="addFiglioBox"></li>
								</ol>
							</ol>




						</div>









					</div>

				</div>

				</md-content> </md-content> </md-tab> <!-- 				<md-tab label="Vocabolo"> <md-content class="md-padding" -->
				<!-- 					new-word-form newWord="ctrl.newWord"> </md-content> </md-tab> </md-tabs> </md-content> -->

				<md-tab label="Vocabolo" selected="true"
					md-on-select="ctrl.activeTab='Vocabolo'"
					md-active="ctrl.activeTab=='Vocabolo'"> <md-content
					class="md-padding">


				<form name="wordForm" class="wordForm md-padding" novalidate
					ng-submit=" ctrl.newWord.DESCR.length > 0  &&  ctrl.newWord.WORD.length > 0 && ctrl.addWord(ctrl.words)">

					<div layout="row" layout-wrap>
						<div flex="100">

							<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
							<label>Word</label> <md-icon md-font-icon="fa  fa-newspaper-o "
								class="wo"></md-icon> <input ng-model="ctrl.newWord.WORD"
								type="text"> </md-input-container>
						</div>
					</div>


					<div layout="row" layout-wrap>
						<div flex="100">
							<md-input-container class="md-icon-float"
								ng-class="{ 'md-input-hasnt-value' : ctrl.newWord.DESCR.length === 0  }">
							<!-- Use floating label instead of placeholder --> <label>Descrizione</label>
							<md-icon md-font-icon="fa  fa-file-text-o " class="des"></md-icon>
							<textarea ng-model="ctrl.newWord.DESCR" columns="1"
								md-maxlength="150"></textarea> </md-input-container>
						</div>
					</div>

					<div layout="row" layout-wrap>
						<div flex="50">
							<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
							<label>Stato</label> <md-icon md-font-icon="fa fa-spinner "
								class="stato"></md-icon> <input ng-model="ctrl.newWord.STATE"
								type="text"> </md-input-container>
						</div>

						<div flex="50">
							<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
							<label>Categoria</label> <md-icon md-font-icon="fa fa-flag-o "
								class="categoria"> </md-icon> <input
								ng-model="ctrl.newWord.CATEGORY" type="text"> </md-input-container>

						</div>
					</div>


					<div layout="row" layout-wrap>
						<div flex="100">
							<md-input-container class="md-icon-float"
								ng-class="{ 'md-input-hasnt-value' : ctrl.newWord.FORMULA.length === 0  }">
							<!-- Use floating label instead of placeholder --> <label>Formula</label>
							<md-icon md-font-icon="fa fa-superscript " class="formu"></md-icon>
							<textarea ng-model="ctrl.newWord.FORMULA" columns="1"
								md-maxlength="150"></textarea> </md-input-container>
						</div>
					</div>





					<div layout="row" layout-wrap>
						<div flex="100">
							<md-input-container class="md-icon-float"
								ng-class="{ 'md-input-has-value-copy' : ctrl.newWord.LINK.length > 0  }">
							<div>
								<div ui-tree="ctrl.TreeOptionsChips" data-drag-enabled="true"
									data-drag-delay="500" data-empty-placeholder-enabled="false"
									class="chipsTree">
									<ol id="olchiproot" ui-tree-nodes ng-model="ctrl.newWord.LINK"
										data-empty-placeholder-enabled="false">
										<li ng-repeat="n in [1]" data-nodrag ui-tree-node
											style="height: 0px; min-height: 0px;">

											<div class="angular-ui-tree-empty"></div>
										</li>

									</ol>
								</div>
							</div>

							<label>link</label> <md-icon md-font-icon="fa fa-link "
								class="lin"></md-icon>

							<div class="linkChips">
								<md-contact-chips ng-model="ctrl.newWord.LINK"
									md-contacts="ctrl.querySearch($query)" md-contact-name="WORD"
									md-require-match="" filter-selected="true">
								<md-chip-template> <strong>{{$chip.WORD
									| uppercase}}</strong> </md-chip-template> </md-contact-chips>
							</div>

							</md-input-container>
						</div>
					</div>




					<div layout="row" layout-wrap>
						<div flex="30" style="padding-right: 40px;">
							<div layout="column" layout-wrap>


								
								<md-autocomplete   style="  min-width: 0;"flex="" md-input-name="autocompleteField"
									md-no-cache="ctrl.noCache" 
									md-search-text="ctrl.tmpAttr.Prop"
									md-selected-item="ctrl.selectedItem"
									md-items="item in ctrl.querySearchProp(ctrl.tmpAttr.Prop)"
									md-item-text="item.ATTRIBUTE_NM" md-require-match=""
									md-floating-label="Proprietà"> 
								<md-item-template>
								<span md-highlight-text="ctrl.tmpAttr.Prop">{{item.ATTRIBUTE_NM}}</span>
								</md-item-template> </md-autocomplete>



								<md-button ng-click="ctrl.addProp(ctrl.tmpAttr)"
									ng-disabled="!ctrl.propPresent(ctrl.tmpAttr.Prop) || ctrl.tmpAttr.Val.length==0 || ctrl.tmpAttr.Val == null "
									class="md-fab   md-mini" aria-label="Aggiungi_Attributo"
									
									style="background-color: rgb(67, 85, 182) !important;    margin: -27px;  margin-left: 100%;">

								<md-tooltip> Aggiungi Attributo </md-tooltip> <md-icon
									md-font-icon="fa fa-angle-double-right fa-2x"
									style="  color: greenyellow;   margin-bottom: 5px;  margin-left: 5px;"></md-icon>
								</md-button>



								<md-input-container >
								<!-- Use floating label instead of placeholder --> <label>Valore</label>
								<input ng-model="ctrl.tmpAttr.Val" type="text" >
								</md-input-container>

							</div>
						</div>


						<div flex="70">

							<md-list> <md-list-item
								class="md-2-line box-list-option" ng-repeat="veg in ctrl.newWord.SBI_GL_WORD_ATTR"
								layout="row" layout-wrap>
							<div class="md-item-text md-whiteframe-z1" flex>
								<p class="margin5">
									<input class="transparent_input smallFont" ng-model="veg.Prop"
										type="text">
								</p>
								<p class="margin5">
									<input class="transparent_input smallFont "
										ng-model="veg.Val" type="text">
								</p>

								<md-button ng-click="1==1" class="md-fab   md-ExtraMini"
									aria-label="add word"
									style="  background-color: rgb(221, 0, 0) !important;
  margin-top: -49px;
  margin-left: 89%;
  border-radius: 0px;
  border-bottom-left-radius: 18px;">
								<md-icon md-font-icon="fa fa-times" style="  color: white;"></md-icon>
								</md-button>


							</div>
							</md-list-item> </md-list>

						</div>
					</div>


					<div layout="row" layout-align="end end">
						<md-button type="button" tabindex="-1" class="md-raised"
							ng-click="ctrl.createNewWord('reset')" ng-show="ctrl.isEmpty()">Annulla</md-button>
						<md-button type="submit" class="md-raised"
							ng-disabled="ctrl.newWord.DESCR.length === 0  || ctrl.newWord.WORD.length === 0">Salva</md-button>
					</div>
				</form>


				</md-content> </md-tab> </md-tabs> </md-content>

			</div>



		</div>

 {{ctrl.selectedItem}}{{ctrl.newWord}}

	</div>


</body>
</html>