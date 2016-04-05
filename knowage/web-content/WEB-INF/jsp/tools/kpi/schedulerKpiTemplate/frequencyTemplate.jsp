<div layout-fill ng-controller="frequencyController">
	<md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin layout="column" flex>

<table style="border-size:0">
	<tr>
	<td>
		<label>{{translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
	</td>
	<td>
		<md-datepicker ng-model="frequency.startDate" name="startDateField"
			required md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
	</td>
	<td>
		<div class="validation-messages" ng-messages="startDateField.$error">
			<div ng-message="valid">{{translate.load("scheduler.invalidDate","component_scheduler_messages")}}</div>
			<div ng-message="required">{{translate.load("scheduler.requiredDate","component_scheduler_messages")}}</div>
		</div>
		<label >{{translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
	</td>
	<td>
		<angular-time-picker id="myTimePicker1" required
			ng-model="frequency.startTime"></angular-time-picker>
	</div>
	</td>
	</tr>

	<tr>
	<td>
		<label>{{translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
	</td>
	<td>
		<md-datepicker ng-model="frequency.endDate" name="endDateField"
			md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
	</td>
	<td>
		<label>{{translate.load("scheduler.endtime","component_scheduler_messages")}}:
		</label>
	</td>
	<td>
		<angular-time-picker id="myTimePicker2" ng-model="frequency.endTime"></angular-time-picker>
	</td>
	</tr>


	<tr>
	<td colspan="4">
		<div ng-if="frequency.type=='scheduler'">
		<div layout="row">
			<span class="textspan" >{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}</span>
			<md-select aria-label="aria-label"
				ng-model="frequency.selectInterval">
			<md-option ng-repeat="interval in intervals "
				value="{{interval.value}}">{{interval.label}}</md-option> </md-select>
		</div>
<md-card ng-if="frequency.selectInterval">
		<!-- type of interval -->
		<div ng-if="frequency.selectInterval=='minute'" layout="row"
			ng-init="1">
			<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
			<md-select aria-label="aria-label" ng-model="frequency.value.minute"
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(60)" value="{{item}}">{{item}}</md-option>
			</md-select>
			<span class="textspan">{{translate.load("sbi.kpis.mins")}}</span>
		</div>
		<div ng-if="frequency.selectInterval=='hour'" layout="row" ng-init="1">
			<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
			<md-select aria-label="aria-label" ng-model="frequency.value.hour"
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(24)" value="{{item}}">{{item}}</md-option>
			</md-select>
			<span class="textspan">{{translate.load("sbi.kpis.hours")}}</span>
		</div>
		<div ng-if="frequency.selectInterval=='day'" layout="row" ng-init="1">
			<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
			<md-select aria-label="aria-label" ng-model="frequency.value.day"
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(31)" value="{{item}}">{{item}}</md-option>
			</md-select>
			<span class="textspan">{{translate.load("sbi.kpis.days")}}</span>
		</div>
		<div ng-if="frequency.selectInterval=='week'" layout="row" ng-init="1">
			<div layout="row" ng-repeat="week in WEEKS">
				<label>{{week.label}}:</label>
				<md-checkbox aria-label="aria-label"
					ng-click="toggleWeek(week.value,selectedWeek)"
					ng-checked="exists(week.value, selectedWeek)"> </md-checkbox>
			</div>
		</div>
		<div ng-if="frequency.selectInterval=='month'" layout="row">
			<div layout="column" layout-wrap layout-align="center center">
				<div layout="row" >
					<span>{{translate.load("sbi.generic.advanced")}}</span>
					<md-switch
						ng-change="activityEventCtrl.toggleMonthScheduler()"
						class="greenSwitch" aria-label="Switch "
						ng-model="activityEventCtrl.typeMonth"
						ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
					</md-switch>
					<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
				</div>
				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonth==true" ng-init="1;">
					<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</span>
					<md-select aria-label="aria-label" ng-model="frequency.value.month"
						class="numberSelect"> <md-option
						ng-repeat="item in getNitem(12) " value="{{item}}">{{item}}</md-option>
					</md-select>
					<span class="textspan">{{translate.load("sbi.kpis.months")}}</span>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonth!=true">
					<span class="textspan">{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}}</span>
					<md-select aria-label="aria-label" ng-model="frequency.value.month"
						multiple='true'> <md-option
						ng-repeat="month in MONTHS " value="{{month.value}}">{{month.label}}</md-option>
					</md-select>
				</div>
			</div>

			<div layout="column" layout-wrap layout-align="center center">
				<div layout="row" >
					<span>{{translate.load("sbi.generic.advanced")}}</span>
					<md-switch 
						ng-change="activityEventCtrl.toggleMonthScheduler()"
						class="greenSwitch" aria-label="Switch "
						ng-model="activityEventCtrl.typeMonthWeek"
						ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
					</md-switch>
					<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonthWeek==true"
					ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
					<span class="textspan">{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}}</span>
					<md-select aria-label="aria-label" ng-model="frequency.value.week"
						class="numberSelect"> <md-option
						ng-repeat="item in getNitem(31) " value="{{item}}">{{item}}</md-option>
					</md-select>
				</div>

				<div layout="row" class="alignedCheckbox"
					ng-if="activityEventCtrl.typeMonthWeek != true" ng-init="1">
					<span class="textspan">{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}}</span>
					<md-select aria-label="aria-label" ng-model="frequency.value.week"> <md-option
						ng-repeat="order in WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option>
					</md-select>

					<span class="textspan">{{translate.load("scheduler.generic.inDay","component_scheduler_messages")}}</span>
					<md-select aria-label="aria-label" ng-model="frequency.value.day"
						multiple='true'
						ng-change="activityEventCtrl.toggleMonthScheduler()">
					<md-option ng-repeat="week in WEEKS " value="{{week.value}}">{{week.label}}</md-option>
					</md-select>
				</div>
			</div>
		</div>
</md-card>
	</div>

	</td>
	</tr>
	</table>
	</md-content>
	</md-whiteframe>
</div>