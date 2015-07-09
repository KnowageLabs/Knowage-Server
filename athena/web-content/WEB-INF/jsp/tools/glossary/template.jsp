<%-- <%@ page language="java" contentType="text/html; charset=ISO-8859-1" --%>
<%--     pageEncoding="ISO-8859-1"%> --%>
<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> -->
<!-- <html> -->
<!-- <head> -->
<!-- <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"> -->
<!-- <title>Insert title here</title> -->
<!-- </head> -->
<!-- <body> -->

<!-- </body> -->
<!-- </html> -->



<script type="text/ng-template" id="dialog-new-glossary.html">
<md-dialog aria-label="New glossary" style="width: 80%;  overflow-y: visible;">

  <form name="glossaryForm" class="wordForm md-padding" novalidate    >
  <md-dialog-content class="sticky-container">
    <md-header class="md-sticky-no-effect">{{gloCtrl.headerTitle}}</md-header>
   

	<div layout="row" layout-wrap>
		<div flex="100">

			<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
			<label>Nome</label> <md-icon md-font-icon="fa  fa-sitemap "	class="wo2" ></md-icon> 
			<input ng-model="gloCtrl.newGloss.GLOSSARY_NM" type="text" maxlength="100" > </md-input-container>
		</div>
	</div>
	
	<div layout="row" layout-wrap>
		<div flex="100">

			<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
			<label>Codice</label> <md-icon md-font-icon="fa   fa-terminal "	class="wo3" ></md-icon> 
			<input ng-model="gloCtrl.newGloss.GLOSSARY_CD" type="text" maxlength="30" > </md-input-container>
		</div>
	</div>
	
	
	<div layout="row" layout-wrap>
		<div flex="100">
			<md-input-container class="md-icon-float" ng-class="{ 'md-input-hasnt-value' : gloCtrl.newGloss.GLOSSARY_DS.length === 0  }"> <!-- Use floating label instead of placeholder -->
			<label>Descrizione</label> 
			<md-icon md-font-icon="fa  fa-file-text-o "	class="formu" ></md-icon>	
				 <textarea ng-model="gloCtrl.newGloss.GLOSSARY_DS" columns="1" md-maxlength="500" maxlength="500"  ></textarea>
				 </md-input-container>
		</div>
	</div>
	

  </md-dialog-content>
     
  <div class="md-actions" layout="row">
   
    <md-button ng-click="gloCtrl.annulla()" class="md-primary">
    Annulla
    </md-button>
  
    <md-button ng-click="gloCtrl.submit()" class="md-primary" ng-disabled="gloCtrl.newGloss.GLOSSARY_NM.length === 0 "  >
     Salva
    </md-button>
  </div>
  
  
  </form>
</md-dialog>
</script>

<script type="text/ng-template" id="new.logical.node.dialog.html">

<md-dialog aria-label="nuovo nodo logico " class="newLogicalNode" id="xoooox" style="width: 80%;  overflow-y: visible;">

<md-toolbar>
    <div class="md-toolbar-tools">
      <h2>{{renCtrl.headerTitle}}</h2>
      <span flex></span>
    
    </div>
  </md-toolbar>

 <md-dialog-content>
   

<div layout="row" layout-wrap>
		<div flex="100">

			<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
			<label>Nome</label> <md-icon md-font-icon="fa  fa-sitemap "	class="wo2" ></md-icon> 
			<input ng-model="renCtrl.tmpNW.CONTENT_NM" type="text" maxlength="100" autofocus> </md-input-container>
		</div>
	</div>
	
	<div layout="row" layout-wrap>
		<div flex="100">

			<md-input-container class="md-icon-float"> <!-- Use floating label instead of placeholder -->
			<label>Codice</label> <md-icon md-font-icon="fa   fa-terminal "	class="wo3" ></md-icon> 
			<input ng-model="renCtrl.tmpNW.CONTENT_CD" type="text" maxlength="30" > </md-input-container>
		</div>
	</div>
	
	
	<div layout="row" layout-wrap>
		<div flex="100">
			<md-input-container class="md-icon-float" ng-class="{ 'md-input-hasnt-value' : renCtrl.tmpNW.CONTENT_DS.length === 0  }"> 
			<label>Descrizione</label> 
			<md-icon md-font-icon="fa  fa-file-text-o "	class="formu" ></md-icon>	
				 <textarea ng-model="renCtrl.tmpNW.CONTENT_DS" columns="1" md-maxlength="500" maxlength="500" style="  margin-left: 35px;" ></textarea>
				 </md-input-container>
		</div>
	</div>




  </md-dialog-content>



<div class="md-actions" layout="row">
    
 <md-button ng-click="renCtrl.annulla()" class="md-primary" tabindex="-1">
      Annulla
    </md-button>
    <md-button ng-disabled="renCtrl.tmpNW.CONTENT_NM.length === 0 " ng-click="renCtrl.salva()" class="md-primary" >
     Salva
    </md-button>
   
  </div>



</md-dialog>
</script>

<script type="text/ng-template" id="dirPagination.tpl.html">

<ul class="pagination" ng-if="1 < pages.length">
    <li ng-if="boundaryLinks" ng-class="{ disabled : pagination.current == 1 }">
        <a href="" ng-click="setCurrent(1)"> << </a>
    </li>
    <li ng-if="directionLinks" ng-class="{ disabled : pagination.current == 1 }" class="ng-scope">
        <a href="" ng-click="setCurrent(pagination.current - 1)" class="ng-binding"> < </a>
    </li>
    <li ng-repeat="pageNumber in pages track by $index" ng-class="{ active : pagination.current == pageNumber, disabled : pageNumber == '...' }">
        <a href="" ng-click="setCurrent(pageNumber)">{{ pageNumber }}</a>
    </li>

    <li ng-if="directionLinks" ng-class="{ disabled : pagination.current == pagination.last }" class="ng-scope">
        <a href="" ng-click="setCurrent(pagination.current + 1)" class="ng-binding"> > </a>
    </li>
    <li ng-if="boundaryLinks"  ng-class="{ disabled : pagination.current == pagination.last }">
        <a href="" ng-click="setCurrent(pagination.last)"> >> </a>
    </li>
</ul>
			</script>

<!-- Nested list template in glossary business -->
<script type="text/ng-template" id="items_renderer.html">
									

<div context-menu data-target="WordTree-{{item.$$hashKey}}" ng-class="{ 'highlight': highlight, 'expanded' : expanded }">
	<div ng-if=" item.CONTENT_NM != undefined" class="nodo_logico dragged-icon">
		<div ui-tree-handle>
			<i class="fa fa-bars fa-2x"></i>
			<md-list>
				<md-list-item  class="SecondaryOnLeft" ng-click="ctrl.toggle(this,item,ctrl.selectedGloss)" ng-init="item.preloader=false"> 
			 		<p>{{item.CONTENT_NM}}</p>
					<md-icon ng-disabled="true" class="md-secondary sm-font-icon "
						aria-label="Chat" md-font-icon="fa fa-angle-down "
						style=" left: 0px;  margin: 3px 0px 0 17px!important;"
						ng-show="!collapsed">
					</md-icon>
					<md-icon ng-disabled="true"
						class=" sm-font-icon expandericon" aria-label="Chat2"
						md-font-icon="fa fa-angle-right " ng-show="collapsed">
					</md-icon> 
					<md-progress-circular
						md-diameter="20" ng-show="item.preloader" class="md-hue-2"
						style="  left: 50%;   margin-top: -14px; margin-left: -25px; position:absolute "
						md-mode="indeterminate">
					</md-progress-circular>
				 </md-list-item>
			</md-list>
		</div>
	</div>

	<div ng-if="item.WORD_ID!= undefined " class="figlio_vocabolo dragged-icon expander-icon ">
		<md-list class="noPadding"> 
		<md-list-item>
		<i class="  fa fa-bars fa-2x" ></i>
		<p style="margin-left: 30px;">{{item.WORD}}</p>
		</md-list-item> 
		</md-list>
	</div>
</div>
<!-- 					menu contestuale albero -->
<div class="dropdown position-fixed" style="z-index: 999; margin-left: calc(-25% - 20px); width: 200px;" id="WordTree-{{ item.$$hashKey }}">
	<md-list class="dropdown-menu bottomBorder" role="menu">
		<md-list-item ng-click='ctrl.newSubItem(this,item)' role="menuitem" tabindex="1" ng-if="!item.HAVE_WORD_CHILD && item.CONTENT_NM != undefined  ">
 		<p>Nuovo Nodo Logico</p>
    	</md-list-item>

 		<md-list-item ng-click='ctrl.createNewWord(false,this)' role="menuitem" tabindex="2" ng-if=" item.CONTENT_NM != undefined && !item.HAVE_CONTENTS_CHILD">
		<p>Nuovo Vocabolo</p>
		</md-list-item>

		<md-list-item ng-click='ctrl.removeContents(this)' role="menuitem" tabindex="3">
		<p>Elimina</p>
		</md-list-item>
	
		<md-list-item ng-click='ctrl.newSubItem(this,item,true)' role="menuitem" ng-if=" item.CONTENT_NM != undefined" tabindex="4">
		<p>Modifica</p>
		</md-list-item> 
	</md-list>
</div>
<!-- 						fine menu contestuale albero -->
<ol ng-if=" item.CONTENT_NM != undefined"
	ng-init="item.CHILD=item.CHILD!=undefined?item.CHILD:[]"
	ui-tree-nodes="options" ng-model="item.CHILD"
	ng-class="{hideChildren: collapsed}">
	<li ng-repeat="item in item.CHILD" ui-tree-node data-collapsed="true"
		ng-include="'items_renderer.html'" class="figlioVisibile"></li>
	<li ng-repeat="n in [1]"   ui-tree-node class="addFiglioBox"></li>
</ol>
</script>
    
    
    <!-- Nested list template in glossary tec-->
<script type="text/ng-template" id="tree_glossary_tec.html">
									

<div ng-if=" item.CONTENT_NM != undefined" class="nodo_logico expander-icon" data-nodrag>
		
	<div ui-tree-handle class="smallTree" style="  cursor: pointer;">
	
									
		<md-list>
			<md-list-item  class="SecondaryOnLeft"
				ng-click="ctrl.toggle(this,item,ctrl.selectedGloss)"
				ng-init="item.preloader=false"> 
			
			<div class="indicator-child"></div>

			<p >{{item.CONTENT_NM}}</p>

			<md-icon ng-disabled="true" class="md-secondary sm-font-icon "
				aria-label="Chat" md-font-icon="fa fa-angle-down "
				style=" left: 0px;  margin: 5px 0px 0 17px!important; "
				ng-show="!collapsed">
			</md-icon>
				
			<md-icon ng-disabled="true"
				class=" sm-font-icon expandericon" aria-label="Chat2"
				md-font-icon="fa fa-angle-right " ng-show="collapsed">
			</md-icon> 
			
			<md-progress-circular
				md-diameter="20" ng-show="item.preloader" class="md-hue-2"
				style="  left: 50%;  margin-left: -25px;   margin-top: -14px; position:absolute "
				md-mode="indeterminate">
			</md-progress-circular>
			
			</md-list-item>
		
		</md-list>
	</div>
</div>

	<div ng-if="item.WORD_ID!= undefined " class="figlio_vocabolo dragged-icon smallTree">
		<md-list class="noPadding"> 
		<md-list-item>
	<i class=" fa fa-bars fa-2x" ></i>
		<p style="margin-left: 30px;">{{item.WORD}}</p>
		</md-list-item> 
		</md-list>
	</div>





<ol ng-if=" item.CONTENT_NM != undefined"
	ng-init="item.CHILD=item.CHILD!=undefined?item.CHILD:[]"
	ui-tree-nodes="options" ng-model="item.CHILD"
	ng-class="{hideChildren: collapsed}">
	<li ng-repeat="item in item.CHILD" ui-tree-node data-collapsed="true"
		ng-include="'tree_glossary_tec.html'" class="figlioVisibile"></li>
</ol>

</script>
                