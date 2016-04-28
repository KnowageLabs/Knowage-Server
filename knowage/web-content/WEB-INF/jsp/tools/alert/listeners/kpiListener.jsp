
	<md-content flex layout="column" ng-controller="alertKpiDefinitionController">
	
		<md-input-container>
		      <label>{{translate.load("select kpi***")}}</label>
		      <md-select ng-model="currentKpiAlarm.kpi"  ng-model-options="{trackBy: '$value.id'}">
		        <md-option ng-repeat="kpi in kpiList" ng-value="kpi"  ng-click="loadSelectedKpi(kpi)">
		          {{kpi.name}}
		        </md-option>
		      </md-select>
	    </md-input-container> 
	 

  		
  		<md-content ng-if="currentKpiAlarm.kpi!=undefined"  layout="column" layout-margin flex class="md-whiteframe-1dp noPadding"> 
			<md-toolbar class="md-hue-2">
	      		<div class="md-toolbar-tools"> 
	        		<h2> 
	        			<span>{{translate.load("List action **")}}</span>
	        		</h2>  
	        		<span flex></span>
	        		  <md-button ng-click="addAction()">
	        		  	{{translate.load("Add action**")}}
	        		  </md-button>
	      		</div>
	      	</md-toolbar>
	       	<md-content   layout="row" layout-wrap>
	       	<md-whiteframe class="md-whiteframe-3dp actionTabs" layout-margin ng-repeat=" action in currentKpiAlarm.action ">
			 	<md-toolbar>
			    	<div class="md-toolbar-tools" > 
				        <span>{{action.type}}</span>
					</div>
			    </md-toolbar> 
			<md-content layout-margin layout="column">
				<div layout="row" ng-repeat="threshVal in action.threshold">
					<div style="width:20px; height:20px; margin-right: 5px;" ng-style="{'background-color':threshVal.color}"></div>
		          	<span flex>{{threshVal.label}}</span>
		          	<span ng-if="threshVal.severityCd!=undefined">({{threshVal.severityCd}})</span>
				</div>
				
			</md-content>
			</md-whiteframe>
	       	 
	       	</md-content>
				 					
		</md-content>
  
	</md-content>



<style>
.noPadding{
padding: 0px!important;}

.actionTabs{
width: 200px}
</style>
