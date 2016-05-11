<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<md-dialog aria-label="{{translate.load('sbi.scheduler.schedulation.detail')}}" style="width: 90%; height: 90%; overflow-y: visible;">
	<md-toolbar>
		<div class="md-toolbar-tools">
			<h2 style="font-size: 20px; text-align: center; width: 100%;">
				{{translate.load("sbi.scheduler.schedulation.detail")}}
			</h2>
		</div>
	</md-toolbar>
	<md-content style="height: 100%;" layout="column">
	<form style="height: 100%; margin-bottom: 0px;" layout="column" name="triggerForm" ng-submit="triggerForm.$valid && activityEventCtrl.saveEvent(triggerForm.$valid,true)" class="wordForm md-padding" novalidate>
		<md-content flex>
		<md-tabs class="mozScroll hideTabs h100" md-border-bottom md-dynamic-height flex>
			<md-tab id="eventTabDetail" label="{{translate.load('sbi.generic.details')}}">
				<div flex> 
					<md-input-container>
						<label>{{translate.load("scheduler.schedname","component_scheduler_messages")}}</label>
						<input ng-model="activityEventCtrl.event.triggerName" name="name" required maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.disableName">
						<div ng-messages="triggerForm.name.$error">
							<div ng-message="required">{{translate.load("sbi.federationdefinition.required")}}</div>
				        </div>
					</md-input-container>
					
					<md-input-container>
						<label>{{translate.load("scheduler.scheddescription","component_scheduler_messages")}}:</label>
						<textarea ng-model="activityEventCtrl.event.triggerDescription" columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
					</md-input-container>

					<div layout="row" layout-align="start center">
						<label>{{translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
						<div layout="column">
							<md-datepicker ng-model="activityEventCtrl.event.startDate" name="startDateField" required md-placeholder={{translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
							<div class="validation-messages" ng-messages="triggerForm.startDateField.$error">
								<div ng-message="valid">{{translate.load("scheduler.invalidDate","component_scheduler_messages")}}</div>
								<div ng-message="required">{{translate.load("scheduler.requiredDate","component_scheduler_messages")}}</div>
							</div>
						</div>
						<label style="margin: 0 20px;">{{translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
						<angular-time-picker id="myTimePicker1" required ng-model="activityEventCtrl.event.startTime"></angular-time-picker>
					</div>

					<div layout="row" layout-align="start center">
						<label style="margin-right: 5px;">{{translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
						<div layout="column">
							<md-datepicker ng-model="activityEventCtrl.event.endDate" name="endDateField" md-placeholder={{translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
							<div class="validation-messages" ng-messages="triggerForm.endDateField.$error">
						 		<div ng-message="valid">{{translate.load("scheduler.invalidDate","component_scheduler_messages")}}</div>
						 	</div>
						</div>
						<label style="margin: 0 20px; margin-right: 26px;">{{translate.load("scheduler.endtime","component_scheduler_messages")}}: </label>
						<angular-time-picker id="myTimePicker2" ng-model="activityEventCtrl.event.endTime"></angular-time-picker>
					</div>
					
					<md-toolbar class="unselectedItem">
						<div class="md-toolbar-tools" layout="row" style="padding-left: 0px;">
							<md-input-container> 
								<label>&nbsp;</label>
								<md-select aria-label="aria-label" ng-model="activityEventCtrl.typeOperation" ng-change="activityEventCtrl.changeTypeOperation();">
									<md-option ng-repeat="type in activityEventCtrl.SCHEDULER_TYPES" value="{{type.value}}">{{type.label}}</md-option>
								</md-select>
							</md-input-container>
						</div>
					</md-toolbar>
					
					<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'event'"	layout-padding class="borderBox">
						<md-input-container> 
							<label>{{translate.load("scheduler.eventType","component_scheduler_messages")}}:</label>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.event_type" ng-change="activityEventCtrl.changeTypeFrequency()" required name={{translate.load("scheduler.repeatinterval","component_scheduler_messages")}}>
								<md-option ng-repeat="eventType in activityEventCtrl.EVENT_TYPES" value="{{eventType.value}}"> {{eventType.label}} </md-option> 
							</md-select> 
						</md-input-container>
	
						<div ng-if="activityEventCtrl.eventSched.event_type=='dataset'">
							<md-toolbar class="md-blue minihead">
								<div class="md-toolbar-tools" layout-wrap>
									<h2>{{translate.load("sbi.kpis.dataset")}}</h2>
								</div>
							</md-toolbar>
	
							<md-content layout-padding class="borderBox"> 
								<md-input-container>
									<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.dataset" ng-change="activityEventCtrl.changeTypeFrequency()" required name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
										<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.id.dsId}}">{{item.label}}</md-option> 
									</md-select> 
								</md-input-container> 
								<md-input-container>
									<label>{{translate.load("sbi.scheduler.schedulation.events.event.frequency")}} ({{translate.load("sbi.kpis.mins")}}) :</label>
									<input type="number" ng-change="activityEventCtrl.changeTypeFrequency()" ng-model="activityEventCtrl.eventSched.frequency"> 
								</md-input-container> 
							</md-content>
						</div>
					</div>

					<div ng-if="activityEventCtrl.shedulerType" layout-padding class="borderBox">
						<div layout="row" class="md-block">
							<span>{{translate.load("scheduler.repeatinterval","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.repetitionKind"
									style="margin:0px" ng-init="activityEventCtrl.getActivityRepetitionKindForScheduler()"
									ng-change="activityEventCtrl.changeTypeFrequency();">
								<md-option ng-repeat="interval in activityEventCtrl.EVENT_INTERVALS" value="{{interval.value}}">{{interval.label}}</md-option>
							</md-select>
						</div>
	
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'minute'" layout="row" ng-init="activityEventCtrl.eventSched.minute_repetition_n = activityEventCtrl.eventSched.minute_repetition_n || 1;">
							<span>{{translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.minute_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
								<md-option ng-repeat="item in activityEventCtrl.getNitem(60)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{translate.load("sbi.kpis.mins")}}</span>
						</div>
						
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'hour'" layout="row" ng-init="activityEventCtrl.eventSched.hour_repetition_n = activityEventCtrl.eventSched.hour_repetition_n || 1;">
							<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.hour_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
								<md-option ng-repeat="item in activityEventCtrl.getNitem(24)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{translate.load("sbi.kpis.hours")}}</span>
						</div>
	
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'day'" layout="row" ng-init="activityEventCtrl.eventSched.day_repetition_n = activityEventCtrl.eventSched.day_repetition_n || 1;">
							<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.day_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect">
								<md-option ng-repeat="item in activityEventCtrl.getNitem(31)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{translate.load("sbi.kpis.days")}}</span>
						</div>
	
						<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'week'" layout="row" class="md-block">
							<div layout="row" ng-repeat="week in activityEventCtrl.WEEKS">
								<md-checkbox aria-label="aria-label" ng-click="activityEventCtrl.toggleWeek(week.value)" ng-checked="activityEventCtrl.isChecked(week.value, activityEventCtrl.event.chrono.parameter.days, (activityEventCtrl.event.chrono.type == 'week'))">
									{{week.label}}
								</md-checkbox>
							</div>
						</div>
	
						<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'month'" layout="row">
							<div layout="column" layout-wrap layout-align="center center">
								<div layout="row" style="margin: 0 15px;">
									<span>{{translate.load("sbi.generic.advanced")}}</span>
									<md-switch style="margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch" aria-label="Switch" ng-model="activityEventCtrl.typeMonth" ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
									</md-switch>
									<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
								</div>
								
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonth==true" ng-init="activityEventCtrl.monthrep_n =activityEventCtrl.monthrep_n || 1;">
									<span class="textspan">{{translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.monthrep_n" class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="item in activityEventCtrl.getNitem(12)" value="{{item}}">{{item}}</md-option> 
									</md-select>
									<span class="textspan">&nbsp; {{translate.load("sbi.kpis.months")}}</span>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonth!=true">
									<span class="textspan">{{translate.load("scheduler.generic.inMonth","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_repetition" style="margin:0px;" multiple='true' ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="month in activityEventCtrl.MONTHS" value="{{month.value}}">{{month.label}}</md-option> 
									</md-select>
								</div>
							</div>
	
							<div layout="column" layout-wrap layout-align="center center">
								<div layout="row" style="margin: 0 15px;">
									<span>{{translate.load("sbi.generic.advanced")}}</span>
									<md-switch style=" margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch" aria-label="Switch" ng-model="activityEventCtrl.typeMonthWeek" ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
									</md-switch>
									<span>{{translate.load("sbi.behavioural.lov.type.simple")}}</span>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonthWeek==true" ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
									<span class="textspan">{{translate.load("scheduler.generic.theDay","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.dayinmonthrep_week" class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="item in activityEventCtrl.getNitem(31)" value="{{item}}">{{item}}</md-option> 
									</md-select>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonthWeek != true" ng-init="activityEventCtrl.month_week_number_repetition = activityEventCtrl.month_week_number_repetition|| '1';">
									<span class="textspan">{{translate.load("scheduler.generic.theWeek","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_number_repetition" style="margin:0px;" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="order in activityEventCtrl.WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option> 
									</md-select>
									
									<span class="textspan">&nbsp; {{translate.load("scheduler.generic.inDay","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_repetition" style="margin:0px;" multiple='true' ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="week in activityEventCtrl.WEEKS" value="{{week.value}}">{{week.label}}</md-option>
									</md-select>
								</div>
							</div>
						</div>
					</div>
				</div>
			</md-tab> 
			<md-tab id="eventTabDocuments" label="{{translate.load('sbi.scheduler.schedulation.events.documentsmanagement')}}">
				<div class="h100">
					<md-chips ng-model="activityEventCtrl.event.documents" readonly="true">
						<md-chip-template ng-click="activityEventCtrl.selectedDocument=$chip">
							<strong ng-class="{'selectedDoc' : activityEventCtrl.selectedDocument.label==$chip.label }">{{$chip.label}}</strong>
						</md-chip-template> 
					</md-chips>
					
					<span ng-if="!(activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0)">
						{{translate.load("scheduler.jobhasnodocument", "component_scheduler_messages")}}</span>
					
					<div class="selected_document" ng-if="activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0">
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem" ng-class="activityEventCtrl.selectedDocument.saveassnapshot? 'selectedItem' : 'unselectedItem'">
								
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveassnapshot">
										{{translate.load("scheduler.saveassnap", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveassnapshot">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.name")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.snapshotname"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
									
									<md-input-container>
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.description")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.snapshotdescription"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
									
									<md-input-container >
										<label>{{translate.load("scheduler.historylength", "component_scheduler_messages")}}:</label>
										<input ng-keyup="activityEventCtrl.selectedDocument.snapshothistorylength=activityEventCtrl.onlyNumberConvert(activityEventCtrl.selectedDocument.snapshothistorylength)" 
												ng-model="activityEventCtrl.selectedDocument.snapshothistorylength">
									</md-input-container>
								</md-content>
							</div>
						</div>
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem"
									ng-class="activityEventCtrl.selectedDocument.saveasfile? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasfile">
										{{translate.load("scheduler.saveasfile", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveasfile">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
										<label>{{translate.load("scheduler.fileName", "component_scheduler_messages")}}:</label> 
										<input ng-model="activityEventCtrl.selectedDocument.fileName" 
												maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
									
									<md-input-container>
										<label>{{translate.load("scheduler.destinationfolder", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.destinationfolder"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipFileDocument">
											{{translate.load("scheduler.zipFileDocument", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
								
									<md-input-container class="subCheckboxRowElement"
											ng-if="activityEventCtrl.selectedDocument.zipFileDocument==true">
										
										<label>{{translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label> 
										<input ng-model="activityEventCtrl.selectedDocument.zipFileName" 
											maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
								</md-content>
							</div>
						</div>
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem"
									ng-class="activityEventCtrl.selectedDocument.saveasdocument? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasdocument">
										{{translate.load("scheduler.saveasdoc", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveasdocument">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.name")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.documentname"
											maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
									
									<md-input-container>
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.description")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.documentdescription"
											maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
								
									<div layout="row" style="align-items: center;">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedFolder">
											{{translate.load("scheduler.fixedFolder", "component_scheduler_messages")}}
										</md-checkbox>
								 		<md-button type="button" id="fixedFolder" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useFixedFolderFlag = !activityEventCtrl.useFixedFolderFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card ng-show="activityEventCtrl.useFixedFolderFlag">
											<span ng-bind-html="activityEventCtrl.useFixedFolderInfo"></span>
										</md-card>
									</div>
								
									<div ng-if="activityEventCtrl.selectedDocument.useFixedFolder==true">
										<img style="margin: 0 0 -5px -6px;" src="${pageContext.request.contextPath}/themes/sbi_default/img/treebase.gif" alt="" /> 
										
										<span>{{translate.load("scheduler.documentstree", "component_scheduler_messages")}}</span>
										
										<div id="docTree" ui-tree="" data-drag-enabled="false"
												data-drag-delay="false" data-empty-placeholder-enabled="false">
											
											<script type="text/ng-template" id="lowFunctionalityTreeNodeTemplate">						
						<div ui-tree-handle layout="row">
							<div class="indicator-child"></div>
							<span class="fa fa-folder-open-o" style="color: turquoise;"></span>
							<md-checkbox md-no-ink style="margin: -3px 0 0 5px;" aria-label="Checkbox 1" 
									ng-click="activityEventCtrl.toggleDocFunct(activityEventCtrl.selectedDocument, elementToIterate.id);"
									ng-checked="activityEventCtrl.isChecked(elementToIterate.id, activityEventCtrl.selectedDocument.funct, true)">
								{{elementToIterate.name}}
							</md-checkbox>
						</div>
						<ol ui-tree-nodes ng-model="elementToIterate" ng-if="elementToIterate.childs">
							<li ng-repeat="elementToIterate in elementToIterate.childs" 
									ui-tree-node class="figlioVisibile" 
									ng-include="'lowFunctionalityTreeNodeTemplate'"></li>
						</ol>
					</script>
											
											<ol id="olchiproot" ui-tree-nodes ng-model="activityEventCtrl.lowFunc">
												<li ng-repeat="elementToIterate in activityEventCtrl.lowFunc" ui-tree-node 
														ng-include="'lowFunctionalityTreeNodeTemplate'"></li>
											</ol>
											
										</div>
									</div>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFolderDataset">
											{{translate.load("scheduler.useFolderDataset", "component_scheduler_messages")}}
										</md-checkbox>
										<md-button type="button" id="useFolderDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useFolderDatasetFlag = !activityEventCtrl.useFolderDatasetFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card ng-show="activityEventCtrl.useFolderDatasetFlag">
											<span ng-bind-html="activityEventCtrl.useFolderDatasetInfo"></span>
										</md-card>
									</div>
								
									<md-input-container class="subCheckboxRowElement" ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}} &nbsp;</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderLabel" required name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
											<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.label}}">{{item.label}}</md-option> 
										</md-select> 
									</md-input-container> 
										
									<md-input-container class="subCheckboxRowElement"
											ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
										<label>{{translate.load("scheduler.folderToDriver", "component_scheduler_messages")}}</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderParameter" required 
												name={{translate.load("sbi.scheduler.schedulation.events.event.parameter")}}>
											<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
										</md-select>
									</md-input-container> 
								</md-content>
							</div>
						</div>
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem"
									ng-class="activityEventCtrl.selectedDocument.sendtojavaclass? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendtojavaclass">
										{{translate.load("scheduler.sendtojavaclass", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.sendtojavaclass">
								<md-content layout-padding class="borderBox"> 
									<md-input-container>
										<label>{{translate.load("scheduler.javaclasspath", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.javaclasspath" maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
								</md-content>
							</div>
						</div>
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem"
									ng-class="activityEventCtrl.selectedDocument.sendmail? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendmail">
										{{translate.load("scheduler.sendmail", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.sendmail">
								<md-content layout-padding class="borderBox">
							
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.uniqueMail">
											{{translate.load("scheduler.uniqueMail", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
							
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipMailDocument">
											{{translate.load("scheduler.zipMailDocument", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
							
									<md-input-container class="subCheckboxRowElement"
											ng-if="activityEventCtrl.selectedDocument.zipMailDocument==true">
										<label>{{translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.zipMailName" maxlength="100"
												ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
							
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedRecipients">
											{{translate.load("scheduler.fixedRecipients", "component_scheduler_messages")}}
										</md-checkbox>
										<md-button type="button" id="useFixedRecipients" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useFixedRecipientsFlag = !activityEventCtrl.useFixedRecipientsFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card ng-show="activityEventCtrl.useFixedRecipientsFlag">
											<span ng-bind-html="activityEventCtrl.useFixedRecipientsInfo"></span>
										</md-card>
									</div>
							
									<md-input-container class="subCheckboxRowElement"
											ng-if="activityEventCtrl.selectedDocument.useFixedRecipients==true">
										<label>{{translate.load("scheduler.mailto", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.mailtos" maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
							
									<div layout="row" class="checkboxRow" ng-if="activityEventCtrl.selectedDocument.parameters.length!=0">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useDataset">
											{{translate.load("scheduler.useDatasetList", "component_scheduler_messages")}}
										</md-checkbox>
										<md-button type="button" id="useDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useDatasetFlag = !activityEventCtrl.useDatasetFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card ng-show="activityEventCtrl.useDatasetFlag">
											<span ng-bind-html="activityEventCtrl.useDatasetInfo"></span>
										</md-card>
									</div>
							
									<md-input-container class="subCheckboxRowElement" ng-if="activityEventCtrl.selectedDocument.useDataset==true">
										<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetLabel" required 
												name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
											<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.label}}">{{item.label}}</md-option>
										</md-select> 
									</md-input-container> 
									
									<md-input-container class="subCheckboxRowElement" ng-if="activityEventCtrl.selectedDocument.useDataset==true">
										<label>{{translate.load("scheduler.mailToDatasetParameter", "component_scheduler_messages")}}</label> 
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetParameter" required name={{translate.load("sbi.scheduler.schedulation.events.event.parameter")}}>
											<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
										</md-select> 
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useExpression">
											{{translate.load("scheduler.useExpression", "component_scheduler_messages")}}
										</md-checkbox>
										<md-button type="button" id="useExpression" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useExpressionFlag = !activityEventCtrl.useExpressionFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
										<md-card ng-show="activityEventCtrl.useExpressionFlag">
											<span ng-bind-html="activityEventCtrl.useExpressionInfo"></span>
										</md-card>
									</div>
								
									<md-input-container class="subCheckboxRowElement"
											ng-if="activityEventCtrl.selectedDocument.useExpression==true">
										<label>{{translate.load("scheduler.mailToExpression", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.expression" maxlength="100"
												ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.reportNameInSubject">
											{{translate.load("scheduler.reportNameInSubject", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
								
									<md-input-container> 
										<label>{{translate.load("scheduler.mailsubject", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.mailsubj"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
									
									<md-input-container>
										<label>{{translate.load("scheduler.fileName", "component_scheduler_messages")}}:</label> 
										<input ng-model="activityEventCtrl.selectedDocument.containedFileName"
												maxlength="100" ng-maxlength="100" md-maxlength="100">
									</md-input-container> 
									
									<md-input-container>
										<label>{{translate.load("scheduler.mailtext", "component_scheduler_messages")}}:</label>
										<textarea ng-model="activityEventCtrl.selectedDocument.mailtxt" columns="1"
												maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea> 
									</md-input-container> 
								</md-content>
							</div>
						</div>
						
						<%
						IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
						if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DISTRIBUTIONLIST_MANAGEMENT)) {%>
						<md-toolbar class="minihead unselectedItem"
								ng-class="activityEventCtrl.selectedDocument.saveasdl? 'selectedItem' : 'unselectedItem'">
							<div class="md-toolbar-tools" layout="row">
								<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasdl">
									{{translate.load("scheduler.distributionlist", "component_scheduler_messages")}}
								</md-checkbox>
							</div>
						</md-toolbar>
						<%} %>
					</div>
				</div>
			</md-tab>
		</md-tabs>
		</md-content>
		<md-content>
			<div class="md-actions" layout="row">
				<md-button type="reset" ng-click="activityEventCtrl.cancel()" class="md-primary">
					{{translate.load("sbi.ds.wizard.cancel")}} 
				</md-button>
				<!-- 
	 			<div ng-if="!triggerForm.$valid">
					<md-icon md-font-icon="fa fa-info-circle" style=" color: #104D71; line-height: 20px;"></md-icon>
					<md-tooltip>
						<ul style="padding: 0px;" >
			 				<li style="display: block;" ng-repeat="(key, errors) in triggerForm.$error track by $index"> 
					 			<ul style="padding: 0px;">
					 				<li style="display: block;" ng-repeat="e in errors">{{e.$name}} <i class="fa fa-arrow-right"></i> <span style="color: red; font-size: 12px; font-weight: 900;">{{ key }}</span>.</li>
						 		</ul>
						 	</li>
						</ul>
					</md-tooltip>
				</div>
				 -->
				<md-button type="submit" class="md-primary" ng-disabled="!triggerForm.$valid">
					{{translate.load("scheduler.save", "component_scheduler_messages")}}
				</md-button>
			</div>
		</md-content>
	</form>
	</md-content>
</md-dialog>