<md-content layout-fill class="cockpitSelectorWidgetSettings">
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);"></dataset-selector>
     		<md-input-container class="md-block" flex>
				<label>{{::translate.load("sbi.cockpit.widgets.selector.column");}}</label>
         		<md-select ng-model="tempSelectedColumn" ng-change="changeColumn()" ng-disabled="!model.dataset.dsId">
          			<md-option ng-repeat="column in model.content.columnSelectedOfDataset | filter : {fieldType:'ATTRIBUTE'}" ng-value="column.alias" >
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
	        <md-card-content class="noPadding">
	        	<div layout="row" class="selTypes" style="padding:8px;" ng-if="!isSelectedColumnTemporal()">
					<div class="outerIcon" ng-repeat="visType in modalityValue" ng-click="setSelectorType(visType.value)" ng-class="{'selected':model.settings.modalityValue==visType.value}">
						<div class="selTypesIcon" ng-class="visType.value+'Icon'">
							<span class="svgFallBackText">{{visType.name}}</span>
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
		    
		    <div ng-if="model.settings.modalityValue!='dropdown' && model.settings.modalityValue!='multiDropdown' && !(isSelectedColumnTemporal() && model.settings.modalityValue == 'singleValue' )">
			    <md-subheader >{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.alignment')}}</md-subheader>
			    <md-input-container class="md-block radioContainer" layout-padding>
		      		<md-radio-group  layout="row" ng-model="model.settings.modalityView" layout="row" layout-align="start center"> 
		       			<md-radio-button ng-repeat="button in modalityView" ng-value="button.value">
		            		<md-icon md-font-icon="{{button.icon}}" style="text-align:center"></md-icon> {{button.name}}
		       			</md-radio-button>
		   			</md-radio-group>
			    </md-input-container>
		    </div>
		    
		    <md-subheader>{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.options')}}</md-subheader>
		    <div layout="row" layout-align="start center" ng-show="isSelectedColumnTemporal()">
				<md-input-container>
					<label>{{::translate.load("sbi.cockpit.widgets.selector.startdate")}}</label>
					<md-datepicker name="startDate" ng-change="setToDate('start')" ng-model="model.settings.defaultStartDate" md-max-date="model.settings.defaultEndDate"></md-datepicker>
				</md-input-container>
				<md-input-container>
					<label>{{::translate.load("sbi.cockpit.widgets.selector.enddate")}}</label>
					<md-datepicker name="endDate" ng-change="setToDate('end')" ng-model="model.settings.defaultEndDate" md-min-date="model.settings.defaultStartDate"></md-datepicker>
				</md-input-container>
			</div>
			
		    <div layout="row" style="padding:8px;">
			    	<md-input-container class="md-block" flex ng-if="!isSelectedColumnTemporal()">
						<label>{{::translate.load("sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.select.default.value")}}</label>
						<md-select  ng-model="model.settings.defaultValue">
							<md-option></md-option>
							<md-option ng-repeat="v in defaultValues" value="{{v.value}}">{{v.name}} </md-option>
						</md-select>
				    </md-input-container>
				    <md-input-container class="md-block" flex ng-if="model.settings.modalityView == 'grid'">
						<label>{{::translate.load('kn.cockpit.selector.designer.columnsWidth')}}</label>
						<input ng-model="model.settings.gridColumnsWidth" />
							
				    </md-input-container>
				    <md-input-container class="md-block" ng-if="model.settings.defaultValue=='STATIC' && !isSelectedColumnTemporal()">
						<label>{{::translate.load("sbi.cockpit.core.selections.list.columnValues")}}</label>
						<input ng-model="model.settings.staticValues" ng-required="model.settings.defaultValue=='STATIC'"/>
					</md-input-container>
				    <md-checkbox ng-model="model.settings.wrapText" flex="20" layout-align="start center" layout="row">
		            	{{::translate.load('sbi.cockpit.widgets.selector.selectordesignerpanel.selectoroptions.wraptext')}}
		         	</md-checkbox>
		         	<md-checkbox ng-model="model.settings.hideDisabled" flex="20" layout-align="start center" layout="row">
		            	{{::translate.load('kn.cockpit.selector.designer.hideDisabled')}}
		         	</md-checkbox>
		         	 <md-checkbox ng-disabled="model.settings.hideDisabled" ng-model="model.settings.enableAll" flex="20" layout-align="start center" layout="row">
		            	{{::translate.load('kn.cockpit.selector.designer.enableAll')}}
		         	</md-checkbox>
		    </div>
			
	    </md-card-content>
    </md-card>
</md-content>