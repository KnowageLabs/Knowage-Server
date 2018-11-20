<md-content layout-fill class="cockpitSelectorWidgetSettings">
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);" dataset-type-exclusion="[{type:'SbiSolrDataSet'}]"></dataset-selector>
     		<md-input-container class="md-block" flex>
				<label>{{::translate.load("sbi.cockpit.widgets.selector.column");}}</label>
         		<md-select ng-model="model.content.selectedColumn" ng-model-options="{trackBy: '$value.alias'}" ng-disabled="!model.dataset.dsId">
          			<md-option ng-repeat="column in model.content.columnSelectedOfDataset | filter : {fieldType:'ATTRIBUTE'}" ng-value="column" >
               			{{column.alias}}
          			</md-option>
      			</md-select>
		    </md-input-container>
		    <md-input-container flex="20" class="md-block">
       			<label>{{::translate.load("sbi.cockpit.widgets.table.sorting.order");}}</label>
          		<md-select ng-model="model.content.sortingOrder" ng-disabled="!model.dataset.dsId || !model.content.selectedColumn || isSelectedColumnTemporal()">
          			<md-option></md-option>
           			<md-option value="ASC">{{::translate.load("sbi.cockpit.widgets.table.sorting.asc");}}</md-option>
          			<md-option value="DESC">{{::translate.load("sbi.cockpit.widgets.table.sorting.desc");}}</md-option>
       			</md-select>
		    </md-input-container>
		</md-card-content>
	</md-card>
	<md-card flex>
		<md-subheader>{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.modality')}}</md-subheader>
	        <md-card-content class="alternatedInput noPadding">
	        	<div layout="row" class="selTypes" style="padding:8px;" ng-if="!isSelectedColumnTemporal()">
					<div class="outerIcon" ng-repeat="visType in modalityValue" ng-click="setSelectorType(visType.value)" ng-class="{'selected':model.settings.modalityValue==visType.value}">
						<div class="selTypesIcon" ng-class="visType.value+'Icon'">
							<md-tooltip>{{visType.name}}</md-tooltip>
						</div>
					</div>
				</div>
				<div layout="row" class="selTypes" style="padding:8px;" ng-if="isSelectedColumnTemporal()">
					<div class="outerIcon" ng-repeat="visType in modalityValue" ng-click="setSelectorType(visType.value)" ng-if="visType.temporalAvailable" ng-class="{'selected':model.settings.modalityValue==visType.value}">
						<div class="selTypesIcon" ng-class="visType.value+'TemporalIcon'">
							<md-tooltip>{{visType.name}}</md-tooltip>
						</div>
					</div>
				</div>
		    
		    <md-subheader ng-if="model.settings.modalityValue!='dropdown'">{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.alignment')}}</md-subheader>
		    <md-input-container class="md-block radioContainer" ng-if="model.settings.modalityValue!='dropdown'" layout-padding>
	      		<md-radio-group  layout="row" ng-model="model.settings.modalityView" layout="row" layout-align="start center"> 
	       			<md-radio-button ng-repeat="button in modalityView" ng-value="button.value">
	            		<md-icon md-font-icon="{{button.icon}}" style="text-align:center"></md-icon> {{button.name}}
	       			</md-radio-button>
	   			</md-radio-group>
		    </md-input-container>
		    
		    <md-subheader>{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.options')}}</md-subheader>
		    <div layout="row" style="padding:8px;">
		    	<md-input-container class="md-block" flex ng-if="!isSelectedColumnTemporal()">
					<label>{{::translate.load("sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.default.value")}}</label>
					<md-select  ng-model="model.settings.defaultValue">
						<md-option></md-option>
						<md-option ng-repeat="v in defaultValues" value="{{v.value}}">{{v.name}} </md-option>
					</md-select>
			    </md-input-container>
			    <md-input-container class="md-block" flex ng-if="model.settings.modalityView == 'grid'">
					<label>Grid columns width</label>
					<input ng-model="model.settings.gridColumnsWidth" />
						
			    </md-input-container>
		    </div>
			
		    <md-input-container class="md-block" ng-if="model.settings.defaultValue=='STATIC' && !isSelectedColumnTemporal()">
				<label>{{::translate.load("sbi.cockpit.core.selections.list.columnValues")}}</label>
				<input ng-model="model.settings.staticValues" />
			</md-input-container>
	    </md-card-content>
    </md-card>
</md-content>