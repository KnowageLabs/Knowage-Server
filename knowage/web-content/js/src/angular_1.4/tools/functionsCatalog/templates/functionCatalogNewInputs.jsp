<md-dialog aria-label="Demo Execution Result">

	<form>
    	<md-toolbar>
	      <div class="md-toolbar-tools">
	        <h2>Insert new Inputs for function</h2>
	        <span flex></span>
	        <md-button class="md-icon-button" ng-click="cancel()">
	        </md-button>
	      </div>
	    </md-toolbar>
		<md-dialog-content style="max-width:100%; max-height:100%; width:100%; height:100%;">	    	
			<div ng-repeat="input in demoData.inputDatasets"> <!-- inputDatasets are the input datasets LABEL!! -->
				<div>
					Demo Dataset Name: {{getDatasetNameByLabel(input.label,datasets);}}
      				<md-input-container class="md-block" flex-gt-sm>
            			<label>Replacing Dataset name</label>
            				<md-select ng-model="replacingDatasetList[input.label]">
              					<md-option ng-repeat="replacingDataset in datasets.item" value="{{replacingDataset}}" > 
                						{{replacingDataset.name}}
              					</md-option>
           					</md-select>
          			</md-input-container>
				</div>	
			</div>
			
			
			<div ng-repeat="input in demoData.inputVariables"> 
				<div>
					Demo Dataset variable: {{input.name}}
					<br/>
					demo value: {{input.value}}
					<md-input-container class="md-block" flex-gt-sm>
            			<label>New variable value</label>
        				<input ng-model="replacingVariableValues[input.name]">
      				</md-input-container>
				</div>
			</div>

			

			<div ng-repeat="output in demoData.outputItems"> <!-- inputDatasets are the input datasets LABEL!! -->
				<div>
      				<div ng-if="output.type.toLowerCase()=='dataset'">
      					<br/>
						Output of type: {{output.type}}<br/>
						Demo Dataset output label: {{output.label}}	
	      				<md-input-container class="md-block" flex-gt-sm>
	            			<label>Replacing Dataset Output Label</label>
	            			<input ng-model="replacingDatasetOutLabels[output.label]">          				
	          			</md-input-container>
	          		</div>
	          			
	          		
	          		<div ng-if="output.type.toLowerCase()=='image'">
	          			<br/>
	          			Output of type: {{output.type}}<br/>
	          			Output label: {{output.label}}  
	          			<!-- <md-input-container class="md-block" flex-gt-sm>
	            			<label>Replacing image label output Value</label>
	            			<input ng-model="replacingImageOutLabels[output.label]">          				
	          			</md-input-container> -->	          		
	          		</div>
	          		
	          		
	          		<div ng-if="output.type.toLowerCase()=='text'">
	          			<br/>
	          			Output of type: {{output.type}}<br/>
	          			Output label: {{output.label}}  
	          			<!-- <md-input-container class="md-block" flex-gt-sm>
	            			<label>Replacing text Output value label</label>
	            			<input ng-model="replacingTextOutLabels[output.label]">          				
	          			</md-input-container> -->
	          		
	          		</div>
				</div>	
			</div>

			<br/>
			<md-button class="md-raised md-primary" ng-click="executeFunction()">Execute</md-button> 
			
			<!-- {{replacingDatasetList}}
			{{replacingVariableValues}}-->
			
			    	
		</md-dialog-content>

  	</form>
</md-dialog>