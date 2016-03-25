<md-button aria-label="Clear parameter" class="md-button md-fab md-mini"
		ng-click="documentExecuteServices.resetParameter(parameter)">
	<i class="fa fa-eraser"></i>
</md-button>
<div flex layout-align="start">
	<md-input-container>
		
		<!-- Tree input -->
		<section ng-if="parameter.selectionType=='TREE'" layout="column">
			<div layout="row" layout-align="start">
				<md-button ng-click="getTreeParameterValue()" ng-required="::parameter.mandatory"
						id="{{::parameter.urlName}}" aria-label="{{parameter.label}}">
					<label>{{parameter.label}}</label>
					<i class="fa fa-sitemap"></i>
				</md-button>
			</div>
			
			<md-whiteframe class="md-whiteframe-1dp" layout layout-align="center center">
				<span ng-bind-html="documentExecuteServices.showParameterHtml(parameter)"></span>
			</md-whiteframe>
		</section>
				
								
		<!-- manual number input -->
		<label ng-if="parameter.type=='NUM' && parameter.selectionType==''" >
			{{parameter.label}}</label>
		<input class="input_class" ng-model="parameter.parameterValue" 
				ng-required="::parameter.mandatory" type="number"
				ng-if="parameter.type=='NUM' && parameter.selectionType==''" >	
		
		<!-- manual text input -->
		<label ng-if="parameter.type=='STRING' && parameter.selectionType==''" >
			{{parameter.label}}</label>
		<input class="input_class" ng-model="parameter.parameterValue" 
				ng-required="::parameter.mandatory"
				ng-if="parameter.type=='STRING' && parameter.selectionType==''">
		
		<!-- lov list single input -->
		<section ng-if="parameter.selectionType=='LIST' && !parameter.multivalue">
			<label>{{parameter.label}}</label>
			<md-radio-group ng-model="parameter.parameterValue" ng-required="::parameter.mandatory">
				<md-radio-button class="md-primary" ng-repeat="defaultParameter in parameter.defaultValues" value="{{::defaultParameter.value}}">
					{{::defaultParameter.label}}
				</md-radio-button>
			</md-radio-group>
		</section>
		
		<!-- lov list multiple input -->
		<section ng-if="parameter.selectionType=='LIST' && parameter.multivalue">
			<label>{{parameter.label}}</label>
			<div ng-repeat="defaultParameter in parameter.defaultValues">
				<md-checkbox class="md-primary" value="{{::defaultParameter.value}}" ng-model="defaultParameter.isSelected"
						ng-change="toggleCheckboxParameter(parameter, defaultParameter)">
					{{::defaultParameter.label}}
				</md-checkbox>
			</div>
		</section>
		
		<!-- lov combobox single and multiple input -->
		<label ng-if="parameter.selectionType=='COMBOBOX'">
			{{parameter.label}}</label>
		<!-- multiple -->
		<md-select ng-model="parameter.parameterValue" multiple
			 	ng-if="parameter.selectionType=='COMBOBOX' && parameter.multivalue"> 
			<md-option ng-repeat="defaultParameter in parameter.defaultValues" value="{{::defaultParameter.value}}" ng-if="defaultParameter.isEnabled">
				{{::defaultParameter.label}}
			</md-option>
		</md-select>
		<!-- single -->
		<md-select ng-model="parameter.parameterValue"
			 	ng-if="parameter.selectionType=='COMBOBOX' && !parameter.multivalue"> 
			<md-option></md-option>
			<md-option ng-repeat="defaultParameter in parameter.defaultValues" value="{{::defaultParameter.value}}" ng-if="defaultParameter.isEnabled">
				{{::defaultParameter.label}}
			</md-option>
		</md-select>
	
	
		<!-- lov LOOKUP single and multiple input -->
		<section ng-if="parameter.selectionType=='LOOKUP'">
			<div layout="row" layout-align="start">
				<md-button class="" id="{{::parameter.urlName}}"
						ng-click="popupLookupParameterDialog(parameter)" aria-label="{{parameter.label}}">
					<label>{{parameter.label}}</label>
					<i class="fa fa-search-plus"></i>
				</md-button>
			</div>
		 	<md-whiteframe class="md-whiteframe-1dp" layout layout-align="start center">
				<span ng-class="{'layout-padding': (parameter.parameterValue.length > 0)}"
					ng-bind-html="documentExecuteServices.showParameterHtml(parameter)"></span>
			</md-whiteframe>
		</section>
		
		<!-- "required" message -->
		<div ng-if="showRequiredFieldMessage(parameter)">
		 	<div ng-message="required">Parameter is required.</div>
		</div>
		
		<%--
		<md-content style="font-size:8px;" ng-if="parameter.selectionType=='TREE'">{{parameter|json}}</md-content>
		--%>
		
	</md-input-container>
</div>