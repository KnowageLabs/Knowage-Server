<md-dialog aria-label="Calculated field Manager"   ng-cloak layout="column" style="min-width:90%; min-height:90%;height:90%;">
  	<md-toolbar>
      <div class="md-toolbar-tools">
        <h2>{{translate.load('sbi.meta.model.calculatedfield')}}</h2>
       </div>
    </md-toolbar>
	<form name="newCFForm" layout="column" flex>
    <md-dialog-content flex layout >
		<div class="md-dialog-content" flex layout="row">
		
			<md-whiteframe class="md-whiteframe-1dp" layout="column" style="min-width:200px">
				<md-toolbar>
			      <div class="md-toolbar-tools">
			        <h2>{{translate.load('sbi.meta.columns')}}</h2>
			       </div>
			    </md-toolbar>
				<div flex  style="overflow: auto;" >
					<md-list flex>
			   		   	<md-list-item ng-repeat="col in selectedBusinessModel.simpleBusinessColumns" ng-click="addCol(col)">
			   		   		{{col.name}}
			   		   		<md-divider></md-divider>
			   		   	</md-list-item>
		   		   </md-list>
				</div>
			</md-whiteframe>
		
	   		 

	   		<div flex layout="column" layout-margin>
	   			<div layout="row">
		   			 <md-input-container  flex class="md-block" >
			            <label>{{translate.load('sbi.generic.name')}}</label>
			            <input ng-model="calcField.name" required>
			          </md-input-container>
			          <md-input-container flex class="md-block" >
			            <label>{{translate.load('sbi.generic.type')}}</label>
			            <md-select ng-model="calcField.dataType">
			              <md-option ng-repeat="cfType in type" ng-value="cfType.name">
			                {{cfType.label}}
			              </md-option>
			            </md-select>
			          </md-input-container>
			          <md-input-container flex class="md-block" >
			            <label>{{translate.load('sbi.generic.columnType')}}</label>
			            <md-select ng-model="calcField.columnType">
			              <md-option ng-repeat="colunmType in columnTypes" ng-value="colunmType.name">
			                {{colunmType.label}}
			              </md-option>
			            </md-select>
			          </md-input-container>
	   			</div>
	   			
	   			<md-whiteframe class="md-whiteframe-1dp" layout="column" flex>
					<md-toolbar>
				      <div class="md-toolbar-tools">
				        <h2>{{translate.load('sbi.kpi.formula')}}</h2>
				       </div>
				    </md-toolbar>
				
					<div layout="column" flex>
						<div layout="row">
							<div layout="row" flex >
				   				 <md-button class="md-raised" flex aria-label="{{func.name}}" ng-repeat="func in functions" ng-click="addFunc(func)">
				   				 	{{func.label}}
				   				 	<md-tooltip md-delay=700>
							        	{{func.name}}
							        </md-tooltip>
				   				 </md-button>
				   			</div>
				   			<div layout="row" flex>
				   				 <md-button class="md-raised" flex aria-label="{{func.name}}" ng-repeat="func in dateFunctions" ng-click="addFunc(func)">
				   				 	{{func.label}}
				   				 	<md-tooltip md-delay=700>
							        	{{func.name}}
							        </md-tooltip>
				   				 </md-button>
				   			</div>
				   			
						</div>
						<div layout="row">
				   			<div layout="row" flex>
				   				 <md-button class="md-raised" flex aria-label="{{func.name}}" ng-repeat="func in commonDateFunctions" ng-click="addFunc(func)">
				   				 	{{func.label}}
				   				 	<md-tooltip md-delay=700>
							        	{{func.name}}
							        </md-tooltip>
				   				 </md-button>
				   			</div>						   								
						</div>
						<div layout="row">
				   			<div layout="row" flex>
				   				 <md-button class="md-raised" flex aria-label="{{func.name}}" ng-repeat="func in stringFunctions" ng-click="addFunc(func)">
				   				 	{{func.label}}
				   				 	<md-tooltip md-delay=700>
							        	{{func.name}}
							        </md-tooltip>
				   				 </md-button>
				   			</div>						
						</div>
						<div layout="row">
				   			<div layout="row" flex>
				   				 <md-button class="md-raised" flex aria-label="{{func.name}}" ng-repeat="func in customFunctions" ng-click="addFunc(func)">
				   				 	{{func.label}}
				   				 	<md-tooltip md-delay=700>
							        	{{func.name}}
							        </md-tooltip>
				   				 </md-button>
				   			</div>						
						</div>
						<textarea flex ng-model="calcField.expression"  rows=4  md-select-on-focus></textarea>
						
					</div>
				</md-whiteframe>
	   			
		   		
		   		
				
		   		
		   		
	   		</div>
		</div>
	</md-dialog-content>
			
	 <md-dialog-actions layout="row">
            <span flex></span>
	      <md-button ng-click="cancel()">
	       {{translate.load('sbi.general.cancel')}}
	      </md-button>
	      <md-button ng-click="createCalculatedField()" ng-disabled="!newCFForm.$valid">
	        {{translate.load('sbi.generic.create')}}
	      </md-button>
	 </md-dialog-actions>
		</form>		
</md-dialog>