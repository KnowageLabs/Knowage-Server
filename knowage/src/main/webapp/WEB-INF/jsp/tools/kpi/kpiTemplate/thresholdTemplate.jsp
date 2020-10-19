<md-content layout-fill layout="column" ng-controller="kpiDefinitionThresholdController"> 

<md-card>
<md-card-content>

	<div ng-if="isUsedByAnotherKpi.value==true" layout="row" layout-align="center">
		<div class="kn-info" flex="60">
			<span>{{translate.load("sbi.kpi.threshold.load.reused.title")}} {{ translate.load("sbi.kpi.threshold.load.reused.message")}}</span>
	   		<span flex></span>
	   		<md-button ng-click="cloneThreshold()" class="md-raised" >  {{translate.load("sbi.generic.clone")}} </md-button>
		</div>
	</div>

  <div layout="row">
	  <md-input-container flex class="md-block">
	            <label>{{translate.load("sbi.generic.name")}}</label>
	            <input ng-model="kpi.threshold.name" required >
	  </md-input-container>
  
   		<md-input-container flex class="md-block">
          <label>{{translate.load("sbi.generic.descr")}}</label>
          <input ng-model="kpi.threshold.description" md-maxlength="1000">
        </md-input-container>
    </div>    
      <div layout="row">   
      <md-input-container flex="50" class="md-block" >
	        <label>{{translate.load("sbi.generic.type")}}</label>
	        <md-select ng-model="kpi.threshold.typeId" >
	          <md-option ng-repeat="thresh in thresholdTypeList" value="{{thresh.valueId}}">
	            {{thresh.translatedValueName}}
	          </md-option>
	        </md-select>
      </md-input-container>
        <span flex></span>
       <md-button class="md-raised loadThreshold" aria-label="load" ng-click="openThresholdSidenav()">{{translate.load("sbi.kpi.threshold.load.from.list")}}  </md-button>
       
    </div>
 	<div layout="row">
 		<table class="kn-table" ng-if="kpi.threshold.thresholdValues.length > 0">
			<thead>
				<tr>
					<th></th>
					<th ng-repeat="column in thresholdColumn">{{column.label}}</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="row in kpi.threshold.thresholdValues | orderBy: 'position'">
					<td class="multiTableAction">
						<div layout="row" layout-align="center"> 
		                	<md-button ng-click="move($event,row,'up')" class="md-icon-button" aria-label="up" ng-show="row.position!=0"> 
	               				<md-icon md-font-icon="fa fa-arrow-up"></md-icon>
	          				</md-button>
	          				<md-button ng-click="move($event,row,'down')" class="md-icon-button" aria-label="down" ng-show="row.position!=kpi.threshold.thresholdValues.length-1">
	          					<md-icon md-font-icon="fa fa-arrow-down"></md-icon>
	          				</md-button>
          				</div>
       				</td>
					<td ng-repeat="column in thresholdColumn" ng-class="{'colorPickerTd':column.type=='colorpicker'}">
						
						<md-input-container ng-if="column.type == 'inputtext'" class="noMdError">
					        <input type="text" ng-model="row[column.name]">
					    </md-input-container>
					    
					    <md-input-container ng-if="column.type == 'inputnumber'" class="noMdError">
					        <input type="number" ng-model="row[column.name]">
					    </md-input-container>
					    
					    <md-select ng-if="column.type == 'select'" ng-model="row[column.name]" >
	           				<md-option value=""></md-option>
	           				<md-option ng-repeat="opt in column.values" value="{{opt.valueId}}">
	           					{{opt.translatedValueName}}
	           				</md-option>
           				</md-select>
           				
					    <md-checkbox ng-if="column.type == 'checkbox'" ng-model="row[column.name]"></md-checkbox>
					    
					    <color-picker ng-if="column.type == 'colorpicker'" options="colorPickerProperty" ng-model="row[column.name]"/>
					</td>
					<td class="tableAction">
						<md-button ng-repeat="action in thresholdTableActionButton" class="md-icon-button" ng-click="action.action(row,$event)">
							<md-tooltip>{{action.label}}</md-tooltip>
							<md-icon md-font-icon="{{action.icon}}">
						</md-button>
					</td>
				</tr>
			</tbody>
		</table>
 	</div>
 	
 	<div layout="row"> 
		<span flex></span>
		<md-button   ng-click="addNewThreshold()">{{translate.load("sbi.kpi.threshold.add")}}</md-button>
	</div>
		<md-sidenav class="md-sidenav-right md-whiteframe-z2" layout="column" md-component-id="thresholdTab" >
	      <md-toolbar>
	        <h1 class="md-toolbar-tools">{{translate.load("sbi.thresholds.listTitle")}}</h1>
	      </md-toolbar>
	      <md-input-container class="md-icon-float md-block" style="margin:18px;">
			    <label>Search</label>
			    <md-icon md-font-icon="fa fa-search"></md-icon>
			    <input ng-model="searchParam" type="text">
		    </md-input-container>
		    <md-content>
	      <md-list class="md-dense">
		      <md-list-item class="md-2-line" ng-repeat="tre in thresholdList | filter:searchParam" ng-click="loadSelectedThreshold(tre,listId)">
		          <div class="md-list-item-text">
		            <h3>{{tre.name}} </h3>
		            <p> {{tre.description}} </p>
		          </div>
		          <md-button class="md-secondary md-icon-button" ng-click="doSecondaryAction($event)" ng-if="phone.options.actionIcon" aria-label="call">
		            <md-icon md-svg-icon="{{phone.options.actionIcon}}"></md-icon>
		          </md-button>
		          <md-divider></md-divider>
	        </md-list-item>
      	</md-list>
      	</md-content>
	    </md-sidenav>
	    </md-card-content>
	    </md-card>
</md-content>
