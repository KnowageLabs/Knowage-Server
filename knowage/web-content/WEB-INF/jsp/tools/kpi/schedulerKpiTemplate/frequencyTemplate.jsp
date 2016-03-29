<div layout-fill ng-controller="frequencyController">
	<md-content class="h100" > 
		<md-input-container class="md-block">
			<label>{{translate.load("scheduler.schedname","component_scheduler_messages")}}:</label>
			<input ng-model="frequency.name" name={{translate.load("scheduler.schedname","component_scheduler_messages")}} required maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.disableName"> 
		</md-input-container>
		<md-input-container class="md-block">
			<label>{{translate.load("scheduler.scheddescription","component_scheduler_messages")}}:</label>
			<textarea ng-model="frequency.descr"
			columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
		</md-input-container>

		<div layout="row" class="checkboxRow">
			<label>{{translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
			<md-datepicker ng-model="frequency.startDate" name="startDateField" required md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
			 <div class="validation-messages" ng-messages="startDateField.$error">
				 <div ng-message="valid">{{translate.load("scheduler.invalidDate","component_scheduler_messages")}}</div>
				 <div ng-message="required">{{translate.load("scheduler.requiredDate","component_scheduler_messages")}}</div>
			</div> 	
			<label style="margin: 0 20px;">{{translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
			<angular-time-picker id="myTimePicker1" required ng-model="frequency.startTime"></angular-time-picker>
		</div>

		<div layout="row" class="checkboxRow">
			<label style="margin-right: 5px;">{{translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
			<md-datepicker ng-model="frequency.endDate" name="endDateField" md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
			
			<label style="margin: 0 20px; margin-right: 26px;">{{translate.load("scheduler.endtime","component_scheduler_messages")}}: </label>
			<angular-time-picker id="myTimePicker2" ng-model="frequency.endTime"></angular-time-picker>
		</div>

		<md-toolbar class="unselectedItem" 
				ng-class="frequency.type != 'single'? 'selectedItem' : 'unselectedItem'"
				style="height: 50px;  min-height: 30px;">
			<div class="md-toolbar-tools" layout="row" style="padding-left: 0px;">
				<md-input-container class="md-block"> 
					<label>{{translate.load("sbi.generic.type")}}</label>
					<md-select aria-label="aria-label" ng-model="frequency.type"> 
						<md-option ng-repeat="type in typeToolbar" value="{{type.value}}">{{type.label}}</md-option> 
					</md-select> 
				</md-input-container>
			</div>
		</md-toolbar>
		
		<div ng-if="frequency.type=='scheduler'">
			<div layout="row" style="margin-bottom: 15px;">
				<span class="textspan">{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}</span>
				<md-select aria-label="aria-label" ng-model="frequency.selectInterval"
				style="margin:0px" > 
					<md-option ng-repeat="interval in intervals " value="{{interval.value}}">{{interval.label}}</md-option> 
				</md-select>
			</div>
			
			<!-- type of interval -->
			<div ng-if="frequency.selectInterval=='minute'" layout="row" ng-init="1">
				<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
				<md-select aria-label="aria-label" ng-model="frequency.value.minute" class="numberSelect"> 
						<md-option ng-repeat="item in getNitem(60)" value="{{item}}">{{item}}</md-option>
				</md-select>
				<span class="textspan">{{translate.load("sbi.kpis.mins")}}</span>
			</div>
			<div ng-if="frequency.selectInterval=='hour'" layout="row" ng-init="1">
				<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
				<md-select aria-label="aria-label" ng-model="frequency.value.hour" class="numberSelect"> 
						<md-option ng-repeat="item in getNitem(24)" value="{{item}}">{{item}}</md-option>
				</md-select>
				<span class="textspan">{{translate.load("sbi.kpis.hours")}}</span>
			</div>
			<div ng-if="frequency.selectInterval=='day'" layout="row" ng-init="1">
				<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
				<md-select aria-label="aria-label" ng-model="frequency.value.day" class="numberSelect"> 
						<md-option ng-repeat="item in getNitem(31)" value="{{item}}">{{item}}</md-option>
				</md-select>
				<span class="textspan">{{translate.load("sbi.kpis.days")}}</span>
			</div>
			<div ng-if="frequency.selectInterval=='week'" layout="row" ng-init="1">
				<div layout="row" ng-repeat="week in WEEKS">
					<label>{{week.label}}:</label>
					<md-checkbox aria-label="aria-label" ng-click="toggleWeek(week.value,selectedWeek)"
					ng-checked="exists(week.value, selectedWeek)">
					</md-checkbox>
				</div>
			</div>
			<div ng-if="frequency.selectInterval=='month'" layout="row">
					<div layout="column" layout-wrap layout-align="center center">
										<div layout="row"  style="margin: 0 15px;">
											<span>{{translate.load("sbi.generic.advanced")}}</span>
											<md-switch style="margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()"
													class="greenSwitch" aria-label="Switch " ng-model="activityEventCtrl.typeMonth"
													ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
											</md-switch>
											<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
										</div>
										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth==true"
												ng-init="1;">
											<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label"
													ng-model="frequency.value.month" class="numberSelect">
												<md-option ng-repeat="item in getNitem(12) " value="{{item}}">{{item}}</md-option> 
											</md-select>
											<span class="textspan">{{translate.load("sbi.kpis.months")}}</span>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonth!=true">
											<span class="textspan">{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label"
													ng-model="frequency.value.month" style="margin:0px;" multiple='true'
													>
												<md-option ng-repeat="month in MONTHS " value="{{month.value}}">{{month.label}}</md-option> 
											</md-select>
										</div>
									</div>

									<div layout="column" layout-wrap layout-align="center center">
										<div layout="row"  style="margin: 0 15px;">
											<span>{{translate.load("sbi.generic.advanced")}}</span>
											<md-switch style=" margin: 0px 10px 17px 10px;"
													ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch"
													aria-label="Switch " ng-model="activityEventCtrl.typeMonthWeek"
													ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
											</md-switch>
											<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek==true"
												ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
											<span class="textspan">{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label" ng-model="frequency.value.week"
													class="numberSelect" >
												<md-option ng-repeat="item in getNitem(31) " value="{{item}}">{{item}}</md-option> 
											</md-select>
										</div>

										<div layout="row" class="alignedCheckbox" ng-if="activityEventCtrl.typeMonthWeek != true"
												ng-init="1">
											<span class="textspan">{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label" ng-model="frequency.value.week"
													style="margin:0px;">
												<md-option ng-repeat="order in WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option> 
											</md-select>
											
											<span class="textspan">{{translate.load("scheduler.generic.inDay","component_scheduler_messages")}}</span>
											<md-select aria-label="aria-label" ng-model="frequency.value.day"
													style="margin:0px;" multiple='true'
													ng-change="activityEventCtrl.toggleMonthScheduler()">
												<md-option ng-repeat="week in WEEKS " value="{{week.value}}">{{week.label}}</md-option>
											</md-select>
										</div>
									</div>
			</div>
			
			
		</div>
							
</md-content>
</div>