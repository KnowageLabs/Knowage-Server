<md-content layout-fill class="cockpitSelectorWidgetSettings">
    <md-card>
     	<md-card-content layout="row" layout-align="space-around center">
     		<dataset-selector flex ng-model=model.dataset.dsId on-change="resetValue(dsId);"></dataset-selector>  	
		</md-card-content>
	</md-card>
	<md-card>
	<form name=selectorForm >
		
        <div layout="row" ng-show="!showCircularcolumns.value && model.content.columnSelectedOfDataset.length>0">
	        <md-card flex= "50" >
		        <md-card-title>
		              <md-card-title-text layout="row">
		                    <span flex class="md-headline">{{translate.load("sbi.cockpit.widgets.selector.selectcolumn");}}</span>
		                    <span flex></span>  
		              </md-card-title-text>
		        </md-card-title>
		        <md-card-content>
					<md-input-container  class="md-block" ng-required="true">
		      			<label>{{translate.load("sbi.cockpit.widgets.selector.column.label");}}</label>
		         		<input ng-model="model.settings.label" ng-required="true">
		         		<div  ng-messages="selectorForm.lbl.$error" ng-show="!model.settings.label">
							<div ng-message="required">{{translate.load("sbi.generic.reqired")}}</div>
				 		</div>
				    </md-input-container>
			    </md-card-content>
			</md-card>
			<md-card flex ="50" >
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
			            		{{button.name}}
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
		</div>
</form>
    </md-card>

</md-content>