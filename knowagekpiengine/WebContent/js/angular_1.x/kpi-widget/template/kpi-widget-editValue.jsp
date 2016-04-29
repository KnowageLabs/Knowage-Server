<md-dialog aria-label="Select Function" flex ng-cloak>
  <md-whiteframe class="md-whiteframe-4dp  "  > 
    <md-toolbar>
      <div class="md-toolbar-tools">
        <h1>{{label}}</h1>
        <span flex></span>
        <md-button class="md-icon-button" ng-click="close()">
          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
        </md-button>
      </div>
	
    </md-toolbar>
    <md-dialog-content >
     <div class="md-dialog-content">
     <div  layout="column">
     	<div layout="row">
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>Target Value</label>
					<input class="input_class" ng-model="targetValue" disabled> 
				</md-input-container>
			</div>
			<div flex>
					<md-input-container class="small counter" class="small counter" >
						<label>Kpi Value</label>
						<input class="input_class" ng-model="value" required> 
					</md-input-container>
			</div>	
		</div>
		
		<div layout="row">
		  <md-input-container flex class="md-block">
          <label>Comment</label>
          <textarea ng-model="valueSeries.manualNote" md-maxlength="150" rows="5" md-select-on-focus required></textarea>
        </md-input-container>
		
		</div> 
     	<div layout="row" ng-repeat="item in array">
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>{{item.label}}</label>
					<input class="input_class" ng-model="item.value" disabled> 
				</md-input-container>
			</div>
		</div>
	     <div layout="row">
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>Day</label>
					<input class="input_class" ng-model="valueSeries.theDay" disabled> 
				</md-input-container>
			</div>
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>Month</label>
					<input class="input_class" ng-model="valueSeries.theMonth" disabled> 
				</md-input-container>
			</div>
			</div>
		<div layout="row">
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>Quarter</label>
					<input class="input_class" ng-model="valueSeries.theQuarter" disabled> 
				</md-input-container>
			</div>
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>Year</label>
					<input class="input_class" ng-model="valueSeries.theYear" disabled> 
				</md-input-container>
			</div>
			</div>

		
	</div>
		<div class="footer" layout="row">
		<span flex></span>
		<md-button class="dialogButton" ng-click="apply()"  md-autofocus>Save <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
		</div>
   	 </md-dialog-content>
  </md-whiteframe>       
  </md-dialog>
  