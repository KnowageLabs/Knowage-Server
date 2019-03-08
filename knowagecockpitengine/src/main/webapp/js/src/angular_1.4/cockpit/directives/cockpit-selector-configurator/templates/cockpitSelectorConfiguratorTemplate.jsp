<md-card class="cockpitSelectorWidgetSettings">
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
      		<md-select ng-model="model.content.sortingOrder" ng-disabled="!model.dataset.dsId || !model.content.selectedColumn">
      			<md-option></md-option>
       			<md-option value="ASC">{{::translate.load("sbi.cockpit.widgets.table.sorting.asc");}}</md-option>
      			<md-option value="DESC">{{::translate.load("sbi.cockpit.widgets.table.sorting.desc");}}</md-option>
   			</md-select>
	    </md-input-container>
	</md-card-content>
</md-card>
<md-card>
	<md-card-title>
       	<md-card-title-text layout="row">
       		<span flex class="md-headline">{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.modality')}}</span>
           	<span flex></span>  
     	</md-card-title-text>
	</md-card-title>
   	<md-card-content class="alternatedInput">
		<md-input-container class="md-block radioContainer">
      			<md-radio-group layout="row" ng-model="model.settings.modalityValue" layout="row" layout-align="start center"> 
       			<md-radio-button ng-repeat="button in modalityValue" ng-value="button.value">
           			{{button.name}}
       			</md-radio-button>
  				</md-radio-group>
    	</md-input-container>
    	<md-input-container  class="md-block radioContainer">
       		<md-radio-group layout="row" ng-model="model.settings.modalityPresent" layout="row" layout-align="start center"> 
       			<md-radio-button ng-repeat="button in modalityPresent" ng-value="button.value">
           			{{button.name}}
       			</md-radio-button>
   			</md-radio-group>
    	</md-input-container>
	    <md-input-container  class="md-block radioContainer" ng-if="model.settings.modalityPresent=='LIST'">
      		<md-radio-group  layout="row" ng-model="model.settings.modalityView" layout="row" layout-align="start center"> 
       			<md-radio-button ng-repeat="button in modalityView" ng-value="button.value">
            		<md-icon md-font-icon="{{button.icon}}" style="text-align:center"></md-icon> {{button.name}}
       			</md-radio-button>
   			</md-radio-group>
	    </md-input-container>
    
		<div layout="row">
		 	<md-input-container  class="md-block" flex>
				<label>{{::translate.load("sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.default.value")}}</label>
				<md-select  ng-model="model.settings.defaultValue">
					<md-option></md-option>
					<md-option ng-repeat="v in defaultValues" value="{{v.value}}">{{v.name}} </md-option>
				</md-select>
		  	</md-input-container>
		  	<md-input-container  class="md-block" flex ng-if="model.settings.modalityView == 'grid'">
				<label>{{::translate.load('kn.cockpit.selector.designer.columnsWidth')}}</label>
				<input ng-model="model.settings.gridColumnsWidth" />
			</md-input-container>
	    	<md-input-container  class="md-block" ng-if="model.settings.defaultValue=='STATIC'" flex>
				<label>{{::translate.load("sbi.cockpit.core.selections.list.columnValues")}}</label>
				<input ng-model="model.settings.staticValues" />
			</md-input-container>
			<md-checkbox ng-model="model.settings.wrapText" flex="20" layout-align="start center" layout="row">
            	{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.wraptext')}}
         	</md-checkbox>
         	<md-checkbox ng-model="model.settings.hideDisabled" flex="20" layout-align="start center" layout="row">
            	{{::translate.load('kn.cockpit.selector.designer.hideDisabled')}}
         	</md-checkbox>
		</div>
		
		
    </md-card-content>
</md-card>