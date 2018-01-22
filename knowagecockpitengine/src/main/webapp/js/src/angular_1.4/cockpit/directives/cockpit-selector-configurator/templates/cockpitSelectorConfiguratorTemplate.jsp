<md-content layout-fill class="cockpitSelectorWidgetSettings">
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);"></dataset-selector>  	
     		<md-input-container class="md-block" ng-if="model.dataset.dsId" flex>
				<label>{{translate.load("sbi.cockpit.widgets.selector.column");}}</label>
         		<md-select ng-model="model.content.selectedColumn" ng-model-options="{trackBy: '$value.alias'}" >
          			<md-option ng-repeat="column in model.content.copyColumnSelectedOfDataset" ng-value="column" >
               			{{column.alias}}
          			</md-option>
      			</md-select>
		    </md-input-container>
		</md-card-content>
	</md-card>
	<md-card flex>
		<md-card-title>
           	<md-card-title-text layout="row">
               	<span flex class="md-headline">{{translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.modality')}}</span>
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
		    <md-input-container  class="md-block radioContainer" >
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
			<md-input-container  class="md-block" >
				<label>{{translate.load("sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.default.value")}}</label>
				<md-select  ng-model="model.settings.defaultValue">
					<md-option ng-repeat="v in defaultValues" value="{{v.value}}">{{v.name}} </md-option>
				</md-select>
	
		    </md-input-container>
		    <md-input-container  class="md-block" ng-if="model.settings.defaultValue=='STATIC'">
				<label>static</label>
				<input ng-model="model.settings.staticValue">
			</md-input-container>
	    </md-card-content>
    </md-card>
</md-content>