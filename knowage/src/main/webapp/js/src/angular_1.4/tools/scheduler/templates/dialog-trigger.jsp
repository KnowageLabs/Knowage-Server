<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<md-dialog aria-label="{{::translate.load('sbi.scheduler.schedulation.detail')}}" style="width:80%" class="kn-scheduler-dialog">
	<md-toolbar>
		<div class="md-toolbar-tools">
			<h2>
				{{::translate.load("sbi.scheduler.schedulation.detail")}}
			</h2>
		</div>
	</md-toolbar>
	
	<md-dialog-content layout="column" style="max-height:810px; ">
	
		<md-tabs class="mozScroll hideTabs" md-dynamic-height md-border-bottom>
			<md-tab id="eventTabDetail" label="{{::translate.load('sbi.scheduler.schedulation.events.detail')}}">
				<md-content class="md-padding internalContent" layout="column" >
					<md-input-container  class="md-block">
						<label>{{::translate.load("scheduler.schedname","component_scheduler_messages")}}</label>
						<input ng-model="activityEventCtrl.event.triggerName" name="name" maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.disableName">
					</md-input-container>
					
					<md-input-container  class="md-block">
						<label>{{::translate.load("scheduler.scheddescription","component_scheduler_messages")}}:</label>
						<textarea ng-model="activityEventCtrl.event.triggerDescription" columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
					</md-input-container>

					<div layout="row" layout-align="center center">
						<label>{{::translate.load("scheduler.startdate","component_scheduler_messages")}}:</label>
						<div layout="column">
							<md-datepicker md-open-on-focus="" ng-model="activityEventCtrl.event._startDate" name="startDateField" md-placeholder={{::translate.load("scheduler.startdate","component_scheduler_messages")}}></md-datepicker>
						</div>
						<label style="margin: 0 20px;">{{::translate.load("scheduler.starttime","component_scheduler_messages")}}:</label>
						<angular-time-picker id="myTimePicker1" ng-model="activityEventCtrl.event._startTime"></angular-time-picker>
					</div>

					<div layout="row" layout-align="center center">
						<label style="margin-right: 5px;">{{::translate.load("scheduler.enddate","component_scheduler_messages")}}:</label>
						<div layout="column">
							<md-datepicker md-open-on-focus="" ng-model="activityEventCtrl.event._endDate" name="endDateField" md-placeholder={{::translate.load("scheduler.enddate","component_scheduler_messages")}}></md-datepicker>
						</div>
						<label style="margin: 0 20px; margin-right: 26px;">{{::translate.load("scheduler.endtime","component_scheduler_messages")}}: </label>
						<angular-time-picker id="myTimePicker2" ng-model="activityEventCtrl.event._endTime"></angular-time-picker>
					</div>
					
					<md-toolbar class="unselectedItem" flex>
						<div class="md-toolbar-tools" layout="row" style="padding-left: 0px;">
							<md-input-container class="md-block"> 
								<label>&nbsp;</label>
								<md-select aria-label="aria-label" ng-model="activityEventCtrl.typeOperation" ng-change="activityEventCtrl.changeTypeOperation();">
									<md-option ng-repeat="type in activityEventCtrl.SCHEDULER_TYPES" value="{{type.value}}">{{type.label}}</md-option>
								</md-select>
							</md-input-container>
						</div>
					</md-toolbar>
					
					<div ng-if="activityEventCtrl.eventSched.repetitionKind == 'event'"	layout-padding class="borderBox" flex>
						<md-input-container class="md-block"> 
							<label>{{::translate.load("scheduler.eventType","component_scheduler_messages")}}:</label>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.event_type" ng-change="activityEventCtrl.changeTypeFrequency()" name={{::translate.load("scheduler.repeatinterval","component_scheduler_messages")}}>
								<md-option ng-repeat="eventType in activityEventCtrl.EVENT_TYPES" value="{{eventType.value}}"> {{eventType.label}} </md-option> 
							</md-select> 
						</md-input-container>
	
						<div ng-if="activityEventCtrl.eventSched.event_type=='dataset'">
							<md-toolbar class="md-blue minihead">
								<div class="md-toolbar-tools" layout-wrap>
									<h2>{{::translate.load("sbi.kpis.dataset")}}</h2>
								</div>
							</md-toolbar>
	
							<md-content layout-padding > 
								<md-input-container class="md-block">
									<label>{{::translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.dataset" ng-change="activityEventCtrl.changeTypeFrequency()" name={{::translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
										<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.id.dsId}}">{{item.label}}</md-option> 
									</md-select> 
								</md-input-container> 
								<md-input-container class="md-block">
									<label>{{::translate.load("sbi.scheduler.schedulation.events.event.frequency")}} ({{::translate.load("sbi.kpis.mins")}}) :</label>
									<input type="number" ng-change="activityEventCtrl.changeTypeFrequency()" ng-model="activityEventCtrl.eventSched.frequency"> 
								</md-input-container> 
							</md-content>
						</div>
					</div>

					<div ng-if="activityEventCtrl.shedulerType" layout-padding class="borderBox" flex>
						<div layout="row" class="md-block">
							<span>{{::translate.load("scheduler.repeatinterval","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.repetitionKind"
									style="margin:0px" ng-init="activityEventCtrl.getActivityRepetitionKindForScheduler()"
									ng-change="activityEventCtrl.changeTypeFrequency();">
								<md-option ng-repeat="interval in activityEventCtrl.EVENT_INTERVALS" value="{{interval.value}}">{{interval.label}}</md-option>
							</md-select>
						</div>
	
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'minute'" layout="row" ng-init="activityEventCtrl.eventSched.minute_repetition_n = activityEventCtrl.eventSched.minute_repetition_n || 1;">
							<span>{{::translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.minute_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
								<md-option ng-repeat="item in activityEventCtrl.getNitem(60)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{::translate.load("sbi.kpis.mins")}}</span>
						</div>
						
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'hour'" layout="row" ng-init="activityEventCtrl.eventSched.hour_repetition_n = activityEventCtrl.eventSched.hour_repetition_n || 1;">
							<span class="textspan">{{::translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.hour_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect"> 
								<md-option ng-repeat="item in activityEventCtrl.getNitem(24)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{::translate.load("sbi.kpis.hours")}}</span>
						</div>
	
						<div class="md-block" ng-if="activityEventCtrl.eventSched.repetitionKind == 'day'" layout="row" ng-init="activityEventCtrl.eventSched.day_repetition_n = activityEventCtrl.eventSched.day_repetition_n || 1;">
							<span class="textspan">{{::translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
							<md-select aria-label="aria-label" ng-model="activityEventCtrl.eventSched.day_repetition_n" ng-change="activityEventCtrl.changeTypeFrequency();" class="numberSelect">
								<md-option ng-repeat="item in activityEventCtrl.getNitem(31)" value="{{item}}">{{item}}</md-option>
							</md-select>
							<span class="textspan">&nbsp; {{::translate.load("sbi.kpis.days")}}</span>
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
									<span>{{::translate.load("sbi.generic.advanced")}}</span>
									<md-switch style="margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch" aria-label="Switch" ng-model="activityEventCtrl.typeMonth" ng-init="activityEventCtrl.typeMonth=activityEventCtrl.typeMonth!=undefined? activityEventCtrl.typeMonth : true ;">
									</md-switch>
									<span>{{::translate.load("sbi.behavioural.lov.type.simple")}}</span>
								</div>
								
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonth==true" ng-init="activityEventCtrl.monthrep_n =activityEventCtrl.monthrep_n || 1;">
									<span class="textspan">{{::translate.load("scheduler.generic.every","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.monthrep_n" class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="item in activityEventCtrl.getNitem(12)" value="{{item}}">{{item}}</md-option> 
									</md-select>
									<span class="textspan">&nbsp; {{::translate.load("sbi.kpis.months")}}</span>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonth!=true">
									<span class="textspan">{{::translate.load("scheduler.generic.inMonth","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_repetition" style="margin:0px;" multiple='true' ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="month in activityEventCtrl.MONTHS" value="{{month.value}}">{{month.label}}</md-option> 
									</md-select>
								</div>
							</div>
	
							<div layout="column" layout-wrap layout-align="center center">
								<div layout="row" style="margin: 0 15px;">
									<span>{{::translate.load("sbi.generic.advanced")}}</span>
									<md-switch style=" margin: 0px 10px 17px 10px;" ng-change="activityEventCtrl.toggleMonthScheduler()" class="greenSwitch" aria-label="Switch" ng-model="activityEventCtrl.typeMonthWeek" ng-init="activityEventCtrl.typeMonthWeek = activityEventCtrl.typeMonthWeek!=undefined? activityEventCtrl.typeMonthWeek : true">
									</md-switch>
									<span>{{::translate.load("sbi.behavioural.lov.type.simple")}}</span>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonthWeek==true" ng-init="activityEventCtrl.dayinmonthrep_week = activityEventCtrl.dayinmonthrep_week || 1;">
									<span class="textspan">{{::translate.load("scheduler.generic.theDay","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.dayinmonthrep_week" class="numberSelect" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="item in activityEventCtrl.getNitem(31)" value="{{item}}">{{item}}</md-option> 
									</md-select>
								</div>
	
								<div layout="row" class="md-block" ng-if="activityEventCtrl.typeMonthWeek != true" ng-init="activityEventCtrl.month_week_number_repetition = activityEventCtrl.month_week_number_repetition|| '1';">
									<span class="textspan">{{::translate.load("scheduler.generic.theWeek","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_number_repetition" style="margin:0px;" ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="order in activityEventCtrl.WEEKS_ORDER" value="{{order.value}}">{{order.label}}</md-option> 
									</md-select>
									
									<span class="textspan">&nbsp; {{::translate.load("scheduler.generic.inDay","component_scheduler_messages")}} &nbsp;</span>
									<md-select aria-label="aria-label" ng-model="activityEventCtrl.month_week_repetition" style="margin:0px;" multiple='true' ng-change="activityEventCtrl.toggleMonthScheduler()">
										<md-option ng-repeat="week in activityEventCtrl.WEEKS" value="{{week.value}}">{{week.label}}</md-option>
									</md-select>
								</div>
							</div>
						</div>
					</div>
					</md-content>
			</md-tab> 
			<md-tab id="eventTabDocuments" label="{{::translate.load('sbi.scheduler.schedulation.events.documentsmanagement')}}">
				<md-content layout="column" class="md-padding">
					<md-chips ng-model="activityEventCtrl.event.documents" readonly="true">
						<md-chip-template ng-click="activityEventCtrl.selectedDocument=$chip">
							<strong ng-class="{'selectedDoc' : activityEventCtrl.selectedDocument.label==$chip.label }">{{$chip.label}}</strong>
						</md-chip-template> 
					</md-chips>
					
					<span ng-if="!(activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0)">
						{{::translate.load("scheduler.jobhasnodocument", "component_scheduler_messages")}}</span>
					
					<div class="selected_document" ng-if="activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0">
						
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem" ng-class="activityEventCtrl.selectedDocument.saveassnapshot? 'selectedItem' : 'unselectedItem'">
								
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveassnapshot">
										{{::translate.load("scheduler.saveassnap", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveassnapshot">
								<md-content layout-padding class="borderBox"> 
									<md-input-container class="md-block">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.name")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.snapshotname"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
									
									<md-input-container class="md-block">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.description")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.snapshotdescription"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
									
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.historylength", "component_scheduler_messages")}}:</label>
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
										{{::translate.load("scheduler.saveasfile", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveasfile">
								<md-content layout-padding class="borderBox"> 
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.fileName", "component_scheduler_messages")}}:</label> 
										<input ng-model="activityEventCtrl.selectedDocument.fileName" 
												maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
									
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.destinationfolder", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.destinationfolder"
												maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipFileDocument">
											{{::translate.load("scheduler.zipFileDocument", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
								
									<md-input-container class="subCheckboxRowElement md-block"
											ng-if="activityEventCtrl.selectedDocument.zipFileDocument==true">
										
										<label>{{::translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label> 
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
										{{::translate.load("scheduler.saveasdoc", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.saveasdocument">
								<md-content layout-padding class="borderBox"> 
									<md-input-container class="md-block">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.name")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.documentname"
											maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
									
									<md-input-container class="md-block">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.description")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.documentdescription"
											maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container>
								
									<div layout="row" style="align-items: center;">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedFolder">
											{{::translate.load("scheduler.fixedFolder", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
								
									<div ng-if="activityEventCtrl.selectedDocument.useFixedFolder==true">
										<img style="margin: 0 0 -5px -6px;" src="<%=urlBuilder.getResourceLink(request,"themes/sbi_default/img/treebase.gif")%>" alt="" /> 
										
										<span>{{::translate.load("scheduler.documentstree", "component_scheduler_messages")}}</span>
										
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
															ng-include="'lowFunctionalityTreeNodeTemplate'">
													</li>
												</ol>
											</script>
											
											<ol id="olchiproot" ui-tree-nodes ng-model="activityEventCtrl.lowFunc">
												<li ng-repeat="elementToIterate in activityEventCtrl.lowFunc" ui-tree-node 
														ng-include="'lowFunctionalityTreeNodeTemplate'"></li>
											</ol>
											
										</div>
									</div>
								
									<div layout="row" class="checkboxRow" ng-if="activityEventCtrl.selectedDocument.parameters.length!=0">
										<md-checkbox style="margin-bottom: 0px;" aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFolderDataset">
											{{::translate.load("scheduler.useFolderDataset", "component_scheduler_messages")}}
										</md-checkbox>
										<div flex></div>
										<md-button type="button" id="useFolderDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useFolderDatasetFlag = !activityEventCtrl.useFolderDatasetFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card flex ng-show="activityEventCtrl.useFolderDatasetFlag">
											<span ng-bind-html="activityEventCtrl.useFolderDatasetInfo"></span>
										</md-card>
									</div>
								
									<md-input-container class="subCheckboxRowElement md-block" ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}} &nbsp;</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderLabel" name={{::translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
											<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.label}}">{{item.label}}</md-option> 
										</md-select> 
									</md-input-container> 
										
									<md-input-container class="subCheckboxRowElement md-block"
											ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
										<label>{{::translate.load("scheduler.folderToDriver", "component_scheduler_messages")}}</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderParameter" name={{::translate.load("sbi.scheduler.schedulation.events.event.parameter")}}>
											<md-option ng-repeat="par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
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
										{{::translate.load("scheduler.sendtojavaclass", "component_scheduler_messages")}}
									</md-checkbox>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.sendtojavaclass">
								<md-content layout-padding class="borderBox"> 
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.javaclasspath", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.javaclasspath" maxlength="100" ng-maxlength="100" md-maxlength="100"> 
									</md-input-container> 
								</md-content>
							</div>
						</div>
						
						<%

						if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SCHEDULING_DISTRIBUTED_OUTPUT)) {%>
						<div style="margin-bottom: 6px;">
							<md-toolbar class="minihead unselectedItem"
									ng-class="activityEventCtrl.selectedDocument.sendmail? 'selectedItem' : 'unselectedItem'">
								<div class="md-toolbar-tools" layout="row">
									<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendmail">
										{{::translate.load("scheduler.sendmail", "component_scheduler_messages")}}
									</md-checkbox>
									<md-icon md-font-icon="fa fa-lock" ng-show="activityEventCtrl.isUniqueMailSettedInAnotherDoc()"></md-icon>
								</div>
							</md-toolbar>
							<div ng-if="activityEventCtrl.selectedDocument.sendmail">
								<md-content layout-padding class="borderBox">
								
									<div layout="row" class="checkboxRow">
										<md-checkbox style="margin-bottom: 0px;" aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedRecipients"
												ng-change="activityEventCtrl.updateUseFixedRecipients()" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.fixedRecipients", "component_scheduler_messages")}}
										</md-checkbox>
										<div flex></div>
										<md-button type="button" id="useFixedRecipients" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useFixedRecipientsFlag = !activityEventCtrl.useFixedRecipientsFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card flex ng-show="activityEventCtrl.useFixedRecipientsFlag">
											<span ng-bind-html="activityEventCtrl.useFixedRecipientsInfo"></span>
										</md-card>
									</div>
							
									<md-input-container class="subCheckboxRowElement md-block"
											ng-if="activityEventCtrl.selectedDocument.useFixedRecipients==true">
										<label>{{::translate.load("scheduler.mailto", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.mailtos" maxlength="1000" ng-maxlength="1000" md-maxlength="1000" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
									</md-input-container>
							
									<div layout="row" class="checkboxRow" ng-if="activityEventCtrl.selectedDocument.parameters.length!=0">
										<md-checkbox style="margin-bottom: 0px;" aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useDataset"
												ng-change="activityEventCtrl.updateUseDataset()" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.useDatasetList", "component_scheduler_messages")}}
										</md-checkbox>
										<div flex></div>
										<md-button type="button" id="useDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useDatasetFlag = !activityEventCtrl.useDatasetFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
								 		<md-card flex ng-show="activityEventCtrl.useDatasetFlag">
											<span ng-bind-html="activityEventCtrl.useDatasetInfo"></span>
										</md-card>
									</div>
							
									<md-input-container class="subCheckboxRowElement md-block" ng-if="activityEventCtrl.selectedDocument.useDataset==true">
										<label>{{::translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetLabel" name={{::translate.load("sbi.scheduler.schedulation.events.event.dataset")}} ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											<md-option ng-repeat="item in activityEventCtrl.datasets" value="{{item.label}}">{{item.label}}</md-option>
										</md-select> 
									</md-input-container> 
									
									<md-input-container class="subCheckboxRowElement md-block" ng-if="activityEventCtrl.selectedDocument.useDataset==true">
										<label>{{::translate.load("scheduler.mailToDatasetParameter", "component_scheduler_messages")}}</label> 
										<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetParameter" name={{::translate.load("sbi.scheduler.schedulation.events.event.parameter")}} ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
										</md-select> 
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox style="margin-bottom: 0px;" aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useExpression"
												ng-change="activityEventCtrl.updateUseExpression()" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.useExpression", "component_scheduler_messages")}}
										</md-checkbox>
										<div flex></div>
										<md-button type="button" id="useExpression" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.useExpressionFlag = !activityEventCtrl.useExpressionFlag" >
								 			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
								 		</md-button>
										<md-card flex ng-show="activityEventCtrl.useExpressionFlag">
											<span ng-bind-html="activityEventCtrl.useExpressionInfo"></span>
										</md-card>
									</div>
								
									<md-input-container class="subCheckboxRowElement md-block"
											ng-if="activityEventCtrl.selectedDocument.useExpression==true">
										<label>{{::translate.load("scheduler.mailToExpression", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.expression" maxlength="100"
												ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
									</md-input-container>
							
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.uniqueMail" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.uniqueMail", "component_scheduler_messages")}}
										</md-checkbox>
									</div>

									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipMailDocument" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.zipMailDocument", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
							
									<md-input-container class="subCheckboxRowElement md-block"
											ng-if="activityEventCtrl.selectedDocument.zipMailDocument==true">
										<label>{{::translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.zipMailName" maxlength="100"
												ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
									</md-input-container>
								
									<div layout="row" class="checkboxRow">
										<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.reportNameInSubject" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
											{{::translate.load("scheduler.reportNameInSubject", "component_scheduler_messages")}}
										</md-checkbox>
									</div>
								
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.mailsubject", "component_scheduler_messages")}}:</label>
										<input ng-model="activityEventCtrl.selectedDocument.mailsubj"
												maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
									</md-input-container> 
									
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.fileName", "component_scheduler_messages")}}:</label> 
										<input ng-model="activityEventCtrl.selectedDocument.containedFileName"
												maxlength="100" ng-maxlength="100" md-maxlength="100" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
									</md-input-container> 
									
									<md-input-container class="md-block">
										<label>{{::translate.load("scheduler.mailtext", "component_scheduler_messages")}}:</label>
										<textarea ng-model="activityEventCtrl.selectedDocument.mailtxt" columns="1"
												maxlength="2000" ng-maxlength="2000" md-maxlength="2000" ng-disabled="activityEventCtrl.isUniqueMailSettedInAnotherDoc()">
                                        </textarea>
									</md-input-container> 
								</md-content>
							</div>
						</div>
						<%} %>
						<%
						if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DISTRIBUTIONLIST_MANAGEMENT) && userProfile.isAbleToExecuteAction(SpagoBIConstants.SCHEDULING_DISTRIBUTED_OUTPUT)) {%>
						<!-- 
						<md-toolbar class="minihead unselectedItem"
								ng-class="activityEventCtrl.selectedDocument.saveasdl? 'selectedItem' : 'unselectedItem'">
							<div class="md-toolbar-tools" layout="row">
								<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasdl">
									{{::translate.load("scheduler.distributionlist", "component_scheduler_messages")}}
								</md-checkbox>
							</div>
						</md-toolbar>
						-->
						<%} %>
					</div>
				</md-content>
			</md-tab>
		</md-tabs>
		
	
	</md-dialog-content>
	
	<md-dialog-actions>
		<md-button ng-click="activityEventCtrl.cancel()">
			{{::translate.load("sbi.ds.wizard.cancel")}} 
		</md-button>

		<md-button ng-click="activityEventCtrl.saveEvent()">
			{{::translate.load("scheduler.save", "component_scheduler_messages")}}
		</md-button>
	</md-dialog-actions>
</md-dialog>