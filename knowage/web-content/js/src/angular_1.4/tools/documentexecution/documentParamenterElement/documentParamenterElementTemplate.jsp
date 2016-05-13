<md-button aria-label="Clear parameter" class="md-button md-icon-button"
		ng-click="documentExecuteServices.resetParameter(parameter)">
	<i class="fa fa-eraser"></i>
</md-button>
<div flex layout-align="start">
	
	
		<%--
		<md-content style="font-size:8px;" ng-if="parameter.typeCode=='MAN_IN' && parameter.valueSelection=='map_in'">{{parameter|json}}</md-content>
		<md-content style="font-size:8px;" ng-cloak>{{parameter|json}}</md-content>
		--%>
		
		<!-- Map input -->
		<section ng-if="parameter.typeCode=='MAN_IN' && parameter.valueSelection=='map_in'" layout="column">
		 	<div layout="row" layout-align="start">
				<md-button ng-click="popupMapParameterDialog(parameter)" ng-required="::parameter.mandatory"
						id="{{::parameter.urlName}}" aria-label="{{parameter.label}}">
					<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'" >{{parameter.label}}</label>
	<!-- 			<i class="fa fa-globe"></i> -->
					<i class="fa fa-external-link"></i>
				</md-button>
			</div>
			<span ng-class="{'layout-padding': parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0}"
					ng-show="(parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0)">
				<md-chips>
					<md-chip ng-repeat="paramVal in parameter.parameterValue">
						{{paramVal}}
					</md-chip>
				</md-chips>
			</span>
			<span ng-class="{'layout-padding': !parameter.multivalue && parameter.parameterValue && parameter.parameterValue != ''}"
					ng-show="(!parameter.multivalue && parameter.parameterValue && parameter.parameterValue != '')">
				<md-chips><md-chip>{{parameter.parameterValue}}</md-chip></md-chips>
			</span> 
		</section>
	
		<!-- lov LOOKUP single and multiple input -->
		<section ng-if="parameter.selectionType=='LOOKUP'">
			<div layout="row" layout-align="start">
				<md-button class="" id="{{::parameter.urlName}}"
						ng-click="popupLookupParameterDialog(parameter)" aria-label="{{parameter.label}}">
					<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'" >{{parameter.label}}</label>
					<i class="fa fa-external-link"></i>
				</md-button>
			</div>
			<span ng-class="{'layout-padding': parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0}"
					ng-show="(parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0)">
				<md-chips>
					<md-chip ng-repeat="paramVal in parameter.parameterValueToShow">
						{{paramVal}}
					</md-chip>
				</md-chips>
			</span>
			<span ng-class="{'layout-padding': !parameter.multivalue && parameter.parameterValue && parameter.parameterValue != ''}"
					ng-show="(!parameter.multivalue && parameter.parameterValue && parameter.parameterValue != '')">
				<md-chips><md-chip>{{parameter.parameterValueToShow}}</md-chip></md-chips>
			</span>
		</section>
		
		<!-- Tree input -->
		<section ng-if="parameter.selectionType=='TREE'" layout="column">
			<div layout="row" layout-align="start">
				<md-button ng-click="getTreeParameterValue()" ng-required="::parameter.mandatory"
						id="{{::parameter.urlName}}" aria-label="{{parameter.label}}">
					<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'" >{{parameter.label}}</label>
<!-- 				<i class="fa fa-sitemap"></i> -->
					<i class="fa fa-external-link"></i>
				</md-button>
			</div>
			
			<!--
 			<span ng-class="{'layout-padding': parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0}"
					ng-show="(parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length > 0)">
			-->
			<span ng-class="{'layout-padding': parameter.parameterValue && parameter.parameterValue.length > 0}"
					ng-show="parameter.parameterValue && parameter.parameterValue.length > 0">
				
				<md-chips ng-model="parameter.parameterValue" readonly="true">
					 <md-chip-template>
				          <strong>{{parameter.parameterDescription[$chip]}}</strong>
			        </md-chip-template>
				</md-chips>
			</span>
			<!--
			<span ng-class="{'layout-padding': !parameter.multivalue && parameter.parameterValue && parameter.parameterValue != ''}"
					ng-show="(!parameter.multivalue && parameter.parameterValue && parameter.parameterValue != '')">
				<md-chips><md-chip>{{parameter.parameterValue.value}}</md-chip></md-chips>
			</span>
			-->
		</section>
				
		<!-- Date -->
		<section ng-if="parameter.type=='DATE' && parameter.selectionType=='' && parameter.valueSelection=='man_in'" layout="column">
<!-- 			<div layout="row" layout-align="start"> -->
				<label 	ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'" >
					{{parameter.label}}
					</label>
<!-- 			</div> -->
<!-- 				<span style="padding-top: 12;"> -->
					<md-datepicker ng-model="parameter.parameterValue"  md-placeholder="{{parameter.label}}" >
					</md-datepicker>
<!-- 				</span> -->
		</section>		
	
		<!-- manual number input -->
		<md-input-container class="md-block" ng-if="parameter.type=='NUM' && parameter.selectionType=='' && parameter.valueSelection=='man_in'">
			<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'">
				{{parameter.label}}
			</label>
			<input class="input_class" ng-model="parameter.parameterValue"  ng-required="::parameter.mandatory" type="number">	
		</md-input-container>
		
		<!-- manual text input -->
		<md-input-container class="md-block" ng-if="parameter.type=='STRING' && parameter.selectionType=='' && parameter.valueSelection=='man_in'">
			<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'">
				{{parameter.label}}</label>
			<input class="input_class" ng-model="parameter.parameterValue"  ng-required="::parameter.mandatory" >
		</md-input-container>
		
		<!-- lov list single input -->
		<section ng-if="parameter.selectionType=='LIST' && !parameter.multivalue">
			<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'" >{{parameter.label}}</label>
			<md-radio-group ng-model="parameter.parameterValue" ng-required="::parameter.mandatory">
				<md-radio-button class="md-primary" ng-repeat="defaultParameter in parameter.defaultValues" ng-if="defaultParameter.isEnabled" value="{{::defaultParameter.value}}">
					{{::defaultParameter.label}} 
				</md-radio-button>
			</md-radio-group>
		</section>
		
		<!-- lov list multiple input -->
		<section ng-if="parameter.selectionType=='LIST' && parameter.multivalue">
			<label ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField'">{{parameter.label}}</label>
			<div ng-repeat="defaultParameter in parameter.defaultValues">
				<md-checkbox class="md-primary" value="{{::defaultParameter.value}}" ng-if="defaultParameter.isEnabled"
						ng-checked="checkboxParameterExists(defaultParameter.value, parameter)" ng-click="toggleCheckboxParameter(defaultParameter.value, parameter)" >
					{{::defaultParameter.label}}
				</md-checkbox>
			</div>
		</section>
		
		<!-- lov combobox single and multiple input -->
		<md-input-container class="md-block">
			<label ng-if="parameter.selectionType=='COMBOBOX'" ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField' ">
			 {{parameter.label}}</label>
		
		
			<!-- multiple -->
			<md-select ng-model="parameter.parameterValue" multiple ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField' " 
				 	ng-if="showDefaultValueAreValid(parameter) && parameter.selectionType=='COMBOBOX' && parameter.multivalue" > 
				<md-option ng-repeat="defaultParameter in parameter.defaultValues" ng-value="defaultParameter.value"
					  ng-if="defaultParameter.isEnabled">
					{{::defaultParameter.label}}
				</md-option>
			</md-select>
			<!-- single -->
			<md-select ng-model="parameter.parameterValue" ng-class="showRequiredFieldMessage(parameter) ? 'requiredField' : 'norequiredField' " 
				 	ng-if="showDefaultValueAreValid(parameter) && parameter.selectionType=='COMBOBOX' && !parameter.multivalue"> 
				<md-option></md-option>
				<md-option ng-repeat="defaultParameter in parameter.defaultValues" ng-value="defaultParameter.value" ng-if="defaultParameter.isEnabled">
					{{::defaultParameter.label}}
				</md-option>
			</md-select>
		
		</md-input-container>
	
	<!-- "DEFAULT VALID MESSAGE " message -->
	<div ng-if="!showDefaultValueAreValid(parameter)">
	 	<div ng-message="required">{{sbiModule_translate.load("sbi.execution.parametersselection.defaulLovDuplicateVaue")}}</div>
	</div>
</div>
<md-divider></md-divider>