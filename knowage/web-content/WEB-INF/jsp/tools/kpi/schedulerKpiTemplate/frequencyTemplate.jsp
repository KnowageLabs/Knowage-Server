<div layout-fill ng-controller="frequencyController" class="overflow" layout="column">
	<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame " layout-margin layout-wrap layout-padding> 
		<div layout = "row" layout-wrap>
			<div flex=10>
				<h5>{{translate.load("scheduler.startdate","component_scheduler_messages")}}</h5>
			</div>
			<div flex=20>
				<md-datepicker ng-model="selectedScheduler.startDate" name="startDateField"
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
				ng-model="selectedScheduler.startTime"></angular-time-picker>
			</div>
			<div flex></div>
		</div>
		<div layout = "row" layout-wrap>
			<div flex=10>
				<h5>{{translate.load("scheduler.enddate","component_scheduler_messages")}}</h5>
			</div>
			<div flex=20>
				<md-datepicker ng-model="selectedScheduler.endDate" name="endDateField"
			md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
			</div>
			<div flex=10>
				<h5 >{{translate.load("scheduler.endtime","component_scheduler_messages")}}</h5>
			</div>
			<div flex=10>
				<angular-time-picker id="myTimePicker2" ng-model="selectedScheduler.endTime"></angular-time-picker>
			</div>
			<div flex></div>
		</div>
		
			<div layout="row" ng-if="frequency.type=='scheduler'">
				<div flex=15 ><h5>{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}</h5></div>
				<div flex>
				<md-select aria-label="aria-label" ng-model="frequency.selectInterval" ng-change="changeTypeFrequency()">
				<md-option ng-repeat="interval in intervals "
					value="{{interval.value}}">{{interval.label}}</md-option> </md-select>
					</div>
					<div flex></div>
			</div>
			<div ng-if="frequency.selectInterval=='minute'" layout="row" ng-init="frequency.value.minute=frequency.value.minute==undefined ? 1 : frequency.value.minute">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.minute"  ng-change="changeTypeFrequency()"
				class="numberSelect "> <md-option
				ng-repeat="item in getNitem(60)" value="{{item}}">{{item}}</md-option>
			</md-select></div>
			<div flex=15><h5>{{translate.load("sbi.kpis.mins")}}</h5></div>
			<div flex></div>
		</div>
		<div ng-if="frequency.selectInterval=='hour'" layout="row" ng-init="frequency.value.hour=frequency.value.hour==undefined ? 1 : frequency.value.hour">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.hour" ng-change="changeTypeFrequency()" 
				class="numberSelect"> <md-option
				ng-repeat="item in getNitem(24)" value="{{item}}">{{item}}</md-option>
			</md-select></div>
			<div flex=15><h5>{{translate.load("sbi.kpis.hours")}}</h5></div>
			<div flex></div>
		</div>
		<div ng-if="frequency.selectInterval=='day'" layout="row" ng-init="frequency.value.day=frequency.value.day==undefined ? 1 : frequency.value.day">
			<div flex=10><h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5></div>
			<div flex=15><md-select aria-label="aria-label" ng-model="frequency.value.day" ng-change="changeTypeFrequency()"
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
			<div layout="column" flex layout-wrap layout-align="center center">
				<div layout="row" flex>
					<h5>{{translate.load("sbi.generic.advanced")}}</h5>
					<md-switch
						ng-change="toggleMonthScheduler()" class="greenSwitch" aria-label="Switch "
						 ng-model="typeMonth"ng-init="typeMonthEdit()">
					</md-switch>
					<h5>{{translate.load("sbi.behavioural.lov.type.simple")}}</h5>
				</div>
				<div layout="row" class="alignedCheckbox" flex ng-if="typeMonth==true" 
					ng-init="frequency.value.month_rep=frequency.value.month_rep==undefined ? 1 : frequency.value.month_rep">
					<h5>{{translate.load("scheduler.generic.every","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month_rep"
						class="numberSelect" ng-change="toggleMonthScheduler()"> 
						<md-option
						ng-repeat="item in getNitem(12) " value="{{item}}">{{item}}</md-option>
					</md-select>
					<h5>{{translate.load("sbi.kpis.months")}}</h5>
				</div>

				<div layout="row" class="alignedCheckbox" ng-if="typeMonth!=true">
					<h5>{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month_repetition"
						multiple='true' ng-change="toggleMonthScheduler()"> <md-option
						ng-repeat="month in MONTHS " value="{{month.value}}">{{month.label}}</md-option>
					</md-select>
				</div>
			</div>

			<div layout="column" flex layout-wrap layout-align="center center">
				<div layout="row" >
					<h5>{{translate.load("sbi.generic.advanced")}}</h5>
					<md-switch 
						class="greenSwitch" aria-label="Switch " ng-model="typeMonthWeek"
						ng-init="typeMonthWeekEdit()" ng-change="toggleMonthScheduler()">
					</md-switch>
					<h5>{{translate.load("sbi.behavioural.lov.type.simple")}}</h5>
				</div>

				<div layout="row" class="alignedCheckbox" ng-if="typeMonthWeek==true"
					ng-init="frequency.value.dayinmonthrep_week = frequency.value.dayinmonthrep_week == undefined ? 1 : frequency.value.dayinmonthrep_week">
					<h5>{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.dayinmonthrep_week" ng-change="toggleMonthScheduler()"
						class="numberSelect"> <md-option
						ng-repeat="item in getNitem(31) " value="{{item}}">{{item}}</md-option>
					</md-select>
				</div>

				<div layout="row" class="alignedCheckbox" ng-if="typeMonthWeek != true" >
					<h5>{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month_week_number_repetition" ng-change="toggleMonthScheduler()"> <md-option
						ng-repeat="order in WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option>
					</md-select>

					<h5>{{translate.load("scheduler.generic.inDay","component_scheduler_messages")}}</h5>
					<md-select aria-label="aria-label" ng-model="frequency.value.month_week_repetition"
						multiple='true'	ng-change="toggleMonthScheduler()">
					<md-option ng-repeat="week in WEEKS " value="{{week.value}}">{{week.label}}</md-option>
					</md-select>
				</div>
			</div>
		</div>
		
		
	</md-whiteframe>
</div>