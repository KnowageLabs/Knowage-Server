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


 <div ng-controller = "formulaController" layout="row" class="formulaController">
 	<md-card style="width:100%;">
 		<md-card-content class="noPadding">
 			<div ui-codemirror="{ onLoad : codemirrorLoaded }" id="code" class="CodeMirrorMathematica" ui-codemirror-opts="codemirrorOptions" ng-model="currentKPI.formula" ></div> 
 		</md-card-content>
 	</md-card>
		
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
</md-dialog-content>
    <md-dialog-actions layout="row">
	<md-button class="dialogButton" ng-click="apply()" md-autofocus>Apply <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
	</md-dialog-actions>
  </form>
</md-dialog>
</script>
 
 
