<div layout-fill ng-controller="frequencyController" class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame " layout-margin layout-wrap layout-padding> 
		<div layout = "row" layout-wrap>
			<div flex=10>
				<h5>{{translate.load("scheduler.startdate","component_scheduler_messages")}}</h5>
			</div>
			<div flex=20>
				<md-datepicker ng-model="frequency.startDate" name="startDateField"
				required md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
				<div class="validation-messages" ng-messages="startDateField.$error">
				<div ng-message="valid">{{translate.load("scheduler.invalidDate","component_scheduler_messages")}}</div>
				<div ng-message="required">{{translate.load("scheduler.requiredDate","component_scheduler_messages")}}</div>
				</div>
			</div>
			<div flex=10>
				<h5 >{{translate.load("scheduler.starttime","component_scheduler_messages")}}</h5>
			</div>
			<div flex=10>
				<angular-time-picker id="myTimePicker1" required
				ng-model="frequency.startTime"></angular-time-picker>
			</div>
			<div flex></div>
		</div>
		<div layout = "row" layout-wrap>
			<div flex=10>
				<h5>{{translate.load("scheduler.enddate","component_scheduler_messages")}}</h5>
			</div>
			<div flex=20>
				<md-datepicker ng-model="frequency.endDate" name="endDateField"
			md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
			</div>
			<div flex=10>
				<h5 >{{translate.load("scheduler.endtime","component_scheduler_messages")}}</h5>
			</div>
			<div flex=10>
				<angular-time-picker id="myTimePicker2" ng-model="frequency.endTime"></angular-time-picker>
			</div>
			<div flex></div>
		</div>
		
			<div layout="row" ng-if="frequency.type=='scheduler'">
				<div flex=15 ><h5>{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}</h5></div>
				<div flex>
				<md-select aria-label="aria-label"
					ng-model="frequency.selectInterval">
				<md-option ng-repeat="interval in intervals "
					value="{{interval.value}}">{{interval.label}}</md-option> </md-select>
					</div>
					<div flex></div>
			</div>
			<div ng-if="frequency.selectInterval=='minute'" layout="row"
			ng-init="1">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.minute"
				class="numberSelect "> <md-option
				ng-repeat="item in getNitem(60)" value="{{item}}">{{item}}</md-option>
			</md-select></div>
			<div flex=15><h5>{{translate.load("sbi.kpis.mins")}}</h5></div>
			<div flex></div>
		</div>
		<div ng-if="frequency.selectInterval=='hour'" layout="row" ng-init="1">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.hour"
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(24)" value="{{item}}">{{item}}</md-option>
			</md-select></div>
			<div flex=15><h5>{{translate.load("sbi.kpis.hours")}}</h5></div>
			<div flex></div>
		</div>
		<div ng-if="frequency.selectInterval=='day'" layout="row" ng-init="1">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.day"
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(31)" value="{{item}}">{{item}}</md-option>
			</md-select></div>
			<div flex=15><h5>{{translate.load("sbi.kpis.days")}}</h5></div>
			<div flex></div>
		</div>
		<div ng-if="frequency.selectInterval=='week'" layout="row" ng-init="1">
			<div layout="row" ng-repeat="week in WEEKS">
				<h5>{{week.label}}</h5>
				<md-checkbox aria-label="aria-label"
					ng-click="toggleWeek(week.value,selectedWeek)"
					ng-checked="exists(week.value, selectedWeek)"> </md-checkbox>
			</div>
		</div>
		<div ng-if="frequency.selectInterval=='month'" layout="row">
			<div layout="column" flex=30 layout-wrap layout-align="center center">
				<div layout="row" flex>
					<h5>{{translate.load("sbi.generic.advanced")}}</h5>
					<md-switch
						ng-change="activityEventCtrl.toggleMonthScheduler()"
						class="greenSwitch" aria-label="Switch "
						ng-model="activityEventCtrl.typeMonth"
						ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
					</md-switch>
					<h5>{{translate.load("sbi.behavioural.lov.type.simple")}}</h5>
				</div>
				<div layout="row" class="alignedCheckbox" flex
					ng-if="activityEventCtrl.typeMonth==true" ng-init="1;">
					<h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month"
						class="numberSelect"> <md-option
						ng-repeat="item in getNitem(12) " value="{{item}}">{{item}}</md-option>
					</md-select>
					<h5>{{translate.load("sbi.kpis.months")}}</h5>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonth!=true">
					<h5>{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month"
						multiple='true'> <md-option
						ng-repeat="month in MONTHS " value="{{month.value}}">{{month.label}}</md-option>
					</md-select>
				</div>
			</div>

			<div layout="column" flex=30 layout-wrap layout-align="center center">
				<div layout="row" >
					<h5>{{translate.load("sbi.generic.advanced")}}</h5>
					<md-switch 
						ng-change="activityEventCtrl.toggleMonthScheduler()"
						class="greenSwitch" aria-label="Switch "
						ng-model="activityEventCtrl.typeMonthWeek"
						ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
					</md-switch>
					<h5>{{translate.load("sbi.behavioural.lov.type.simple")}}</h5>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonthWeek==true"
					ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
					<h5>{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.week"
						class="numberSelect"> <md-option
						ng-repeat="item in getNitem(31) " value="{{item}}">{{item}}</md-option>
					</md-select>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonthWeek != true" ng-init="1">
					<h5>{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.week"> <md-option
						ng-repeat="order in WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option>
					</md-select>

					<h5>{{translate.load("scheduler.generic.inDay","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.day"
						multiple='true'
						ng-change="activityEventCtrl.toggleMonthScheduler()">
					<md-option ng-repeat="week in WEEKS " value="{{week.value}}">{{week.label}}</md-option>
					</md-select>
				</div>
			</div>
		</div>
		
		
	</md-whiteframe>
</div>