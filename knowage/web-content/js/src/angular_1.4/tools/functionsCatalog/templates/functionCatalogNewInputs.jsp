<md-dialog aria-label='{{translate.load("sbi.functionscatalog.newinputsdialog.demoexecutionresult")}}' style="max-width: 100%;
    max-height: 100%;
    width: 80%;
    height: 80%;
    border-radius: 0;">
  	
  	 <md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> <h2>{{translate.load("sbi.functionscatalog.newinputsdialog.insertnewinputsforfunction")}}</h2>	<div flex></div>	<md-button class="md-raised md-primary" ng-click="executeFunction()" ng-disabled="isExecuteDisabled()">{{translate.load("sbi.functionscatalog.newinputsdialog.execute")}}</md-button>		</div></md-toolbar>  	 
  	 <div layout="row">
		<md-card flex="50">
			<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> {{translate.load("sbi.functionscatalog.newinputsdialog.input")}}</div> </md-toolbar>
			<md-card-content layout="column">

					
					
					
						<div ng-repeat="input in demoData.inputDatasets"> 
							<div>
								{{translate.load("sbi.functionscatalog.newinputsdialog.demodatasetname")}}&nbsp;{{getDatasetNameByLabel(input.label,datasets);}}
	      						<md-input-container class="md-block" flex-gt-sm>
	            					<label>{{translate.load("sbi.functionscatalog.newinputsdialog.replacingdatasetname")}}</label>
	            					<md-select ng-model="replacingDatasetList[input.label]">
	              						<md-option ng-repeat="replacingDataset in datasets.item" value="{{replacingDataset.label}}" > 
	                							{{replacingDataset.name}}
	              						</md-option>
	           						</md-select>
	          					</md-input-container>
							</div>	
						</div>		
					
						<div ng-repeat="input in demoData.inputFiles"> 
								<div>
									<!--  {{translate.load("sbi.functionscatalog.newinputsdialog.demofilename")}}&nbsp;{{input.name}} -->
		      						<div class="md-block" flex-gt-sm>
		            					<label>{{translate.load("sbi.functionscatalog.newinputsdialog.replacingfilename")}}</label>

										<file-upload-base64 id="id_file_upload-{{$index}}"  ng-model="replacingFileList[input.alias]"></file-upload-base64>												
										



		          					</div>
								</div>	
						</div>

			


					<div ng-repeat="input in demoData.inputVariables"> 
						<div>
							{{translate.load("sbi.functionscatalog.newinputsdialog.demodatasetvariable")}}&nbsp;{{input.name}}
							<br/>
							{{translate.load("sbi.functionscatalog.newinputsdialog.demovalue")}}&nbsp;{{input.value}}
							<md-input-container class="md-block" flex-gt-sm>
            					<label>{{translate.load("sbi.functionscatalog.newinputsdialog.newvariablevalue")}}</label>
        						<input ng-model="replacingVariableValues[input.name]">
      						</md-input-container>
						</div>
					</div>

					
			</md-card-content>
		</md-card>
			
		<md-card flex="50">
			<md-toolbar class="md-knowage-theme"> <div class="md-toolbar-tools"> {{translate.load("sbi.functionscatalog.newinputsdialog.output")}}</div> </md-toolbar>
			<md-card-content layout="column">		
	
				<div ng-repeat="output in demoData.outputItems"> 
					<div>
      					<div ng-if="output.type.toLowerCase()=='dataset'">
      						<br/>
							{{translate.load("sbi.functionscatalog.newinputsdialog.outputoftype")}}&nbsp;{{output.type}}
							<br/>
							{{translate.load("sbi.functionscatalog.newinputsdialog.demodatasetoutputlabel")}}&nbsp;{{output.label}}	
	      					<md-input-container class="md-block" flex-gt-sm>
	            				<label>{{translate.load("sbi.functionscatalog.newinputsdialog.replacingdatasetoutputlabel")}}</label>
	            				<input ng-model="replacingDatasetOutLabels[output.label]">          				
	          				</md-input-container>
	          			</div>
	          			
	          		
	          			<div ng-if="output.type.toLowerCase()=='image'">
	          				<br/>
	          				{{translate.load("sbi.functionscatalog.newinputsdialog.outputoftype")}}&nbsp;{{output.type}}
	          				<br/>
	          				{{translate.load("sbi.functionscatalog.newinputsdialog.outputlabel")}}&nbsp;{{output.label}}  	          		
	          			</div>
	          		
	          		
	          			<div ng-if="output.type.toLowerCase()=='text'">
	          				<br/>
	          				{{translate.load("sbi.functionscatalog.newinputsdialog.outputoftype")}}&nbsp;{{output.type}}
	          				<br/>
	          				{{translate.load("sbi.functionscatalog.newinputsdialog.outputlabel")}}&nbsp;{{output.label}}
	          			</div>
					</div>	
				</div>
			
			</md-card-content>				
		
		</md-card>
			
	</div>	
  	  	
  	
</md-dialog>