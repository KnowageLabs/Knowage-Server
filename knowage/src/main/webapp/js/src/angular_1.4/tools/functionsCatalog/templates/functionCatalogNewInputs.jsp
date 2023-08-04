<md-dialog class="replaceDialog">
  	 <md-toolbar> 
  	 	<div class="md-toolbar-tools"> 
  	 		<h2>{{translate.load("sbi.functionscatalog.newinputsdialog.insertnewinputsforfunction")}}</h2>		
 		</div>
 	</md-toolbar>  	 
 	<md-dialog-content style="width:800px;">
 		<div layout="row" layout-align="center">
	 		<div class="kn-info" flex=80>
	 			{{translate.load("sbi.functionscatalog.informations")}}
	 		</div>
 		</div>
 		<div layout="row">
			<md-card flex="50">
				<md-toolbar class="secondaryToolbar"> 
					<div class="md-toolbar-tools"> 
						{{translate.load("sbi.functionscatalog.newinputsdialog.input")}}
					</div> 
				</md-toolbar>
				<div layout="column">
				
					<md-subheader class="md-primary" ng-if="demoData.inputDatasets && demoData.inputDatasets.length > 0">{{translate.load("sbi.functionscatalog.subheader.datasets")}}</md-subheader>
					<div ng-repeat="input in demoData.inputDatasets" class="inputContainer"> 						
  						<md-input-container class="md-block" flex-gt-sm>
        					<label><span>{{translate.load("sbi.functionscatalog.replace")}}</span><span> {{getDatasetNameByLabel(input.label,datasets);}}</span></label>
        					<md-select ng-model="replacingDatasetList[input.label]">
          						<md-option ng-repeat="replacingDataset in datasets.item" value="{{replacingDataset.label}}" > 
            							{{replacingDataset.name}}
          						</md-option>
       						</md-select>
      					</md-input-container>
					</div>		
				
					<md-subheader class="md-primary" ng-if="demoData.inputFiles && demoData.inputFiles.length > 0">{{translate.load("sbi.functionscatalog.subheader.files")}}</md-subheader>
					<div ng-repeat="input in demoData.inputFiles" class="inputContainer"> 
						<label>{{translate.load("sbi.functionscatalog.subheader.files")}} {{input.alias}}</label>
						<file-upload-base64 id="id_file_upload-{{$index}}"  ng-model="replacingFileList[input.alias]">
						</file-upload-base64>											
					</div>
	
					<md-subheader class="md-primary" ng-if="demoData.inputVariables && demoData.inputVariables.length > 0">{{translate.load("sbi.functionscatalog.subheader.variables")}}</md-subheader>
					<div ng-repeat="input in demoData.inputVariables" class="inputContainer"> 
						<md-input-container class="md-block" flex-gt-sm>
           					<label>{{input.name}}</label>
       						<input ng-model="replacingVariableValues[input.name]" placeholder="{{input.value}}">
     					</md-input-container>
					</div>
		
				</div>
			</md-card>
				
			<md-card flex="50">
				<md-toolbar class="secondaryToolbar"> 
					<div class="md-toolbar-tools"> 
						{{translate.load("sbi.functionscatalog.newinputsdialog.output")}}
					</div> 
				</md-toolbar>
				<md-card-content layout="column" class="noPadding">
					<div layout="row" class="outputList" ng-class="{'tallerItem':output.type.toLowerCase()=='dataset'}" ng-repeat="output in demoData.outputItems" ng-if="output.type.toLowerCase()!='file'" layout-align="space-around center">
						<md-icon class="md-avatar fa fa-2x" flex="20" ng-class="{'fa-font':output.type.toLowerCase()=='text','fa-picture-o':output.type.toLowerCase()=='image','fa-database':output.type.toLowerCase()=='dataset'}"></md-icon>
						<div flex="80" layout="column">
				            <h3>{{ output.label}}</h3>
				            <h4>{{ output.type }}</h4>
				            <md-input-container class="md-block" md-no-float ng-if="output.type.toLowerCase()=='dataset'">
	            				<input ng-model="replacingDatasetOutLabels[output.label]" placeholder='{{translate.load("sbi.functionscatalog.newinputsdialog.replacingdatasetoutputlabel")}}'>          				
	          				</md-input-container>
			            </div>
					</div>
				
				</md-card-content>				
			</md-card>
		</div>
	</md-dialog-content>
	<md-dialog-actions layout="row" layout-align="start center">
		<span flex></span>
	    <md-button ng-click="cancel()" class="md-raised">{{translate.load("sbi.generic.cancel")}}</md-button>
	    <md-button class="md-raised md-primary" ng-click="executeFunction()" ng-disabled="isExecuteDisabled()">{{translate.load("sbi.functionscatalog.newinputsdialog.execute")}}</md-button>
	</md-dialog-actions>	
  	  	
</md-dialog>