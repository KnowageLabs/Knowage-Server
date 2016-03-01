 <div layout-fill ng-controller = "formulaController" layout="row">
 <md-whiteframe class="md-whiteframe-2dp relative" layout-fill layout-margin flex  >
		<div ui-codemirror="{ onLoad : codemirrorLoaded }" id="code" class="absolute CodeMirrorMathematica" layout-fill ui-codemirror-opts="codemirrorOptions" ng-model="currentKPI.formula"></div> 
 </md-whiteframe>
</div>
 
 
 <script type="text/ng-template" id="dialog1.tmpl.html">
<md-dialog aria-label="Select Function"  ng-cloak>
  <form>
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>Select type Function for {{token}}</h1>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="close()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
		 <md-radio-group  ng-model="selectedFunctionalities">
     		<md-radio-button  value="SUM" >SUM</md-radio-button>
     		<md-radio-button  value="MAX"> MAX </md-radio-button>
      		<md-radio-button  value="MIN">MIN</md-radio-button>
			<md-radio-button  value="COUNT">COUNT</md-radio-button>
   		 </md-radio-group>
     </div>
    
	<div class="footer">
	<md-button class="dialogButton" ng-click="apply()" md-autofocus>Apply <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
	</div>
   	 </md-dialog-content>
  </form>
</md-dialog>
</script>
 
 