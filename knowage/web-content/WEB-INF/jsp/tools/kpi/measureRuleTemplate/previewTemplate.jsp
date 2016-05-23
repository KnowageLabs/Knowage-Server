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


<md-content ng-controller="measureRulePreviewController" layout-fill  layout="row">
	<div layout="row" layout-sm="column" id="preview" layout-align="space-around" ng-show="!havePlaceholder() && showCircular" layout-fill>
     	<md-progress-circular md-mode="indeterminate" ></md-progress-circular>
 	</div>
 	<div layout="row" layout-sm="column" id="preview" layout-align="space-around" ng-show="havePlaceholder()" layout-fill>
     	<h3>{{translate.load("sbi.measurerule.valuefilter")}}</h3>
 	</div>
	<angular-table class="relative"  flex id='previewtable' ng-model="detailProperty.previewData.rows"
				columns="detailProperty.previewData.columns" no-pagination="true" full-width>
	</angular-table>
				 
	<md-sidenav class="md-sidenav-rigth md-whiteframe-z2" md-component-id="placeholderFormTab" md-is-locked-open="::havePlaceholder()">
	      <md-toolbar>
	      <div class="md-toolbar-tools" latoyt="row">
	        <h1 flex >{{translate.load("sbi.kpi.filters")}}</h1>
	         <md-button ng-click="loadPreview()">{{translate.load('sbi.generic.run')}}</md-button>
	      </div>
	      </md-toolbar>
	      <md-content layout-margin >   
	         <md-input-container class="md-block" ng-repeat=" plcH in currentRule.placeholders">
	            <label>{{plcH.name}}</label>
	            <input ng-model="plcH.value">
	         </md-input-container>
          
	      </md-content>
	    </md-sidenav>

</md-content>