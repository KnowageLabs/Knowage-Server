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
					<label>{{translate.load("sbi.kpi.widget.target")}}</label>
					<input class="input_class" ng-model="targetValue" disabled> 
				</md-input-container>
			</div>
			<div flex>
					<md-input-container class="small counter" class="small counter" >
						<label>{{translate.load("sbi.kpi.widget.kpi")}}</label>
						<input class="input_class" ng-model="value" required> 
					</md-input-container>
			</div>	
		</div>
		
		<div layout="row">
		  <md-input-container flex class="md-block">
          <label>{{translate.load("sbi.kpi.widget.comment")}}</label>
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
					<label>{{translate.load("sbi.kpi.widget.day")}}</label>
					<input class="input_class" ng-model="valueSeries.theDay" disabled> 
				</md-input-container>
			</div>
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>{{translate.load("sbi.kpi.widget.month")}}</label>
					<input class="input_class" ng-model="valueSeries.theMonth" disabled> 
				</md-input-container>
			</div>
			</div>
		<div layout="row">
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>{{translate.load("sbi.kpi.widget.quarter")}}</label>
					<input class="input_class" ng-model="valueSeries.theQuarter" disabled> 
				</md-input-container>
			</div>
			<div flex>
				<md-input-container class="small counter" class="small counter" >
					<label>{{translate.load("sbi.kpi.widget.year")}}</label>
					<input class="input_class" ng-model="valueSeries.theYear" disabled> 
				</md-input-container>
			</div>
			</div>

		
	</div>
		<div class="footer" layout="row">
		<span flex></span>
		<md-button class="dialogButton" ng-click="apply()"  md-autofocus>{{translate.load("sbi.general.save")}} <md-icon md-font-icon="fa fa-check buttonIcon" aria-label="apply"></md-icon></md-button>
		</div>
   	 </md-dialog-content>
  </md-whiteframe>       
  </md-dialog>
  