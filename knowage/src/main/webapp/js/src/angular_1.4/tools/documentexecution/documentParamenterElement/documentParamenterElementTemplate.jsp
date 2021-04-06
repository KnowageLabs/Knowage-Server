<div flex layout-align="start" class="parametersSidenav">
	
	<!-- Map input -->		
	<div ng-if="parameter.typeCode=='MAN_IN' && parameter.valueSelection=='map_in' && parameter.showOnPanel=='true'" layout="column">
	 	<div layout="row" layout-align="start">
	 		<label ng-class="{'requiredField':parameter.showMapDriver && showRequiredFieldMessage(parameter), 'norequiredField': !(parameter.showMapDriver && showRequiredFieldMessage(parameter)), 'mandatory':parameter.mandatory}" flex = 80 ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}
					<md-tooltip ng-if="!parameter.showMapDriver"> {{sbiModule_translate.load("sbi.execution.parametersselection.message.parametersdisabled")}} </md-tooltip>
				</label>
			<md-button ng-disabled="!parameter.showMapDriver" ng-click="popupMapParameterDialog(parameter)" ng-required="::parameter.mandatory"	id="{{::parameter.urlName}}" aria-label="{{i18n.getI18N(parameter.label)}}">
				<i class="fa fa-external-link"></i>
			</md-button>
		</div>
		<span ng-class="{'layout-padding': parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0}" ng-show="(parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0)">
			<md-chips>
				<md-chip ng-repeat="paramVal in parameter.parameterValue">
					{{paramVal}}
				</md-chip>
			</md-chips>
		</span>
		<span ng-class="{'layout-padding': !parameter.multivalue && parameter.parameterValue && parameter.parameterValue != ''}" ng-show="(!parameter.multivalue && parameter.parameterValue && parameter.parameterValue != '')">
			<md-chips>
				<md-chip>{{parameter.parameterValue}}</md-chip>
			</md-chips>
		</span> 
	</div>
	
	<!-- lov LOOKUP single and multiple input -->
	<div ng-if="parameter.selectionType=='LOOKUP'  && parameter.showOnPanel=='true'" layout="column" class="lookupParameter" layout-margin>
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex = 80 ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>

		<div layout="row" layout-align="start center">
			<md-button ng-disabled="datasetSettings" class="md-icon-button" id="{{::parameter.urlName}}" ng-click="popupLookupParameterDialog(parameter)">
				<md-icon md-font-icon="fa fa-external-link"></md-icon>
			</md-button>
			<div flex ng-if="isArray(parameter.parameterValue) && parameter.parameterValue.length > 0">
				<md-chips ng-model="parameter.parameterValue" readonly="true">
					<md-chip-template>
						<strong>{{ descriptionOf($chip) }}</strong>
					</md-chip-template>
				</md-chips>
			</div>
			<div flex ng-if="!isArray(parameter.parameterValue) && !isBlank(parameter.parameterValue)">
				<md-chips>
					<md-chip><strong>{{parameter.parameterValue}}</strong></md-chip>
				</md-chips>
			</div>
		</div>
	</div>
	

	<!-- Tree input -->	
	<div ng-if="parameter.selectionType=='TREE'  && parameter.showOnPanel=='true'" layout="column" class="lookupParameter" layout-margin>
 		<div layout="row" layout-align="start center" class="labelContainer"> 
 		<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)" class="iconWidth-16">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
			
 		</div>
		<div layout="row" layout-align="start center">
			<md-button class="md-icon-button" id="{{::parameter.urlName}}" ng-click="getTreeParameterValue()">
				<md-icon md-font-icon="fa fa-external-link"></md-icon>
			</md-button>
			<div flex>
				<md-chips ng-model="parameter.parameterDescription" readonly="true">
					<md-chip-template>
						<strong>{{$chip}}</strong>
					</md-chip-template>
				</md-chips>
			</div>
		</div>
	</div>
			
	<!-- Date -->
	<div ng-if="parameter.type=='DATE' && parameter.selectionType=='' && parameter.valueSelection=='man_in'  && parameter.showOnPanel=='true'" layout="column" class="datePickerParameter" layout-margin>
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>
		<md-datepicker ng-model="parameter.parameterValue" md-max-date="parameter.maxValue" style="margin-right:0;max-height:90px;" layout="row" layout-align="start center"></md-datepicker>
	</div>		
	
	<!-- Date RANGE-->
	<section ng-if="parameter.type=='DATE_RANGE' && parameter.selectionType=='' && parameter.valueSelection=='man_in'  && parameter.showOnPanel=='true'" layout="column" class="datePickerParameter" layout-margin>
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>
		<md-datepicker ng-model="parameter.parameterValue"  md-placeholder="{{i18n.getI18n(parameter.label)}}" ></md-datepicker>
		<md-select ng-model="parameter.datarange.opt"> 
			<md-option></md-option>
			<md-option ng-repeat="defaultParameter in parameter.defaultValues" ng-value="defaultParameter.value" >
				{{defaultParameter.label}}
			</md-option>
		</md-select>	
		<md-input-container class="md-block">{{sbiModule_translate.load("sbi.generic.to")}} : {{endDateRange(parameter.datarange.opt,parameter)}}</md-input-container>	
	</section>	
	
	<!-- manual number input -->
	<div class="textInput" ng-if="parameter.type=='NUM' && parameter.selectionType=='' && parameter.valueSelection=='man_in'  && parameter.showOnPanel=='true'" layout="column" layout-margin>
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>
		<md-input-container class="md-block noMargin" flex>
			<input ng-disabled="datasetSettings" class="input_class" ng-model="parameter.parameterValue"  ng-required="::parameter.mandatory" type="number" />	
		</md-input-container>
	</div>
	
	<!-- manual text input -->
	<div ng-if="parameter.type=='STRING' && parameter.selectionType=='' && parameter.valueSelection=='man_in'  && parameter.showOnPanel=='true'" layout="column" layout-margin class="textInput">
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}} </label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>
		<md-input-container class="md-block noMargin" flex>
			<input ng-disabled="datasetSettings" class="input_class" ng-model="parameter.parameterValue"  ng-required="::parameter.mandatory" />
		</md-input-container>
	</div>
	
	<!-- lov list radio input -->
	<div ng-if="parameter.selectionType=='LIST' && !parameter.multivalue  && parameter.showOnPanel=='true'" layout="column" layout-margin class="radioParameter">
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label> 
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		
		</div>
		<md-radio-group ng-model="parameter.parameterValue" ng-required="::parameter.mandatory">
			<md-radio-button ng-disabled="datasetSettings" class="md-primary" ng-repeat="defaultParameter in parameter.defaultValues" ng-if="defaultParameter.isEnabled" value="{{::defaultParameter.value}}" ng-click="toggleRadioParameter(defaultParameter.value, defaultParameter.description, parameter)">
				{{::defaultParameter.label}} 
			</md-radio-button>
		</md-radio-group>
	</div>
	
	<!-- lov list multiple input -->
	<div ng-if="parameter.selectionType=='LIST' && parameter.multivalue  && parameter.showOnPanel=='true'" layout="column" layout-margin class="checkBoxParameter">
		<div layout="row" layout-align="start center" class="labelContainer">
			<label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label>
			<md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter, false)">
				<md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
			</md-icon>
		 
		</div>
		<div ng-repeat="defaultParameter in parameter.defaultValues" ng-if="defaultParameter.isEnabled">
			<md-checkbox ng-disabled="datasetSettings" class="md-primary" value="{{::defaultParameter.value}}" 
					ng-checked="checkboxParameterExists(defaultParameter.value, parameter)" ng-click="toggleCheckboxParameter(defaultParameter.value, defaultParameter.description, parameter)" >
				{{::defaultParameter.label}}
			</md-checkbox>
		</div>
	</div>

	<!-- lov combobox single and multiple input -->
	
	<div ng-if="parameter.selectionType=='COMBOBOX'  && parameter.showOnPanel=='true'" layout="column" layout-margin class="selectParameter">
		<div layout="row" layout-align="start center" class="labelContainer">
                                       <label ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory}" flex ng-style="{'font-style': isEmpty(parameter.dependsOn) ? '' : 'italic','font-weight': isEmpty(parameter.dependsOn) ? '' : 'bold'}">{{i18n.getI18n(parameter.label)}}</label>
                       <md-icon md-font-icon="fa fa-eraser" ng-click="resetParameter(parameter)">
                               <md-tooltip md-delay="1000">{{sbiModule_translate.load("sbi.execution.parametersselection.parameter.clear")}}</md-tooltip>
                       </md-icon>
        </div>
        <md-input-container class="md-block" ng-if="parameter.selectionType=='COMBOBOX'  && parameter.showOnPanel=='true'">
			<!-- multiple -->
			<md-select ng-disabled="datasetSettings" ng-model="parameter.parameterValue" multiple ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory} "
		 		ng-change="toggleComboParameter(parameter)"	ng-if="showDefaultValueAreValid(parameter) && parameter.multivalue" > 
				<md-option ng-repeat="defaultParameter in parameter.defaultValues" ng-value="defaultParameter.value" ng-if="defaultParameter.isEnabled">
					{{defaultParameter.label}}
				</md-option>
			</md-select>
			<!-- single -->
			<md-select ng-disabled="datasetSettings" ng-model="parameter.parameterValue" ng-class="{'requiredField':showRequiredFieldMessage(parameter), 'norequiredField': !showRequiredFieldMessage(parameter), 'mandatory':parameter.mandatory} "
		        ng-change="toggleComboParameter(parameter)" ng-if="showDefaultValueAreValid(parameter) && !parameter.multivalue  && parameter.showOnPanel=='true'"> 
				<md-option ng-repeat="defaultParameter in parameter.defaultValues" ng-value="defaultParameter.value" ng-if="defaultParameter.isEnabled">
					{{defaultParameter.label}}
				</md-option>
			</md-select>
		</md-input-container>
	</div>
	
	<!-- "DEFAULT VALID MESSAGE " message -->
	<div ng-if="!showDefaultValueAreValid(parameter)">
	 	<div ng-message="required">{{sbiModule_translate.load("sbi.execution.parametersselection.defaulLovDuplicateVaue")}}</div>
	</div>
</div>
<md-divider></md-divider>