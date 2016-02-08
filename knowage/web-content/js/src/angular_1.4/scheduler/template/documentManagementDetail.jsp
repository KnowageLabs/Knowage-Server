<md-chips ng-model="activityEventCtrl.event.documents" readonly="true">
	<md-chip-template ng-click="activityEventCtrl.selectedDocument=$chip">
		<strong ng-class="{'selectedDoc' : activityEventCtrl.selectedDocument.label==$chip.label }">{{$chip.label}}</strong>
	</md-chip-template> 
</md-chips>

<p ng-if="!(activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0)">
	{{translate.load("scheduler.jobhasnodocument", "component_scheduler_messages")}}</p>

<div class="selected_document" ng-if="activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0">

	<md-toolbar class="minihead unselectedItem "
			ng-class="activityEventCtrl.selectedDocument.saveassnapshot? 'selectedItem' : 'unselectedItem'">
		
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.saveassnap", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label" 
					ng-model="activityEventCtrl.selectedDocument.saveassnapshot"></md-checkbox>
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
	
	<md-toolbar class="minihead unselectedItem"
			ng-class="activityEventCtrl.selectedDocument.saveasfile? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.saveasfile", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasfile"></md-checkbox>
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
				<label>{{translate.load("scheduler.zipFileDocument", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label"
						ng-model="activityEventCtrl.selectedDocument.zipFileDocument"></md-checkbox>
			</div>
		
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.zipFileDocument==true">
				
				<label>{{translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label> 
				<input ng-model="activityEventCtrl.selectedDocument.zipFileName" 
					maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
			</md-input-container> 
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem"
			ng-class="activityEventCtrl.selectedDocument.saveasdocument? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.saveasdoc", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.saveasdocument"></md-checkbox>
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
		
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.fixedFolder", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedFolder"></md-checkbox>
				<md-button type="button" id="fixedFolder" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.showInfoBox(translate.load('scheduler.fixedFolder', 'component_scheduler_messages'),translate.load('scheduler.help.useFixedFolder', 'component_scheduler_messages'),'fixedFolder')"  >
         			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
        		</md-button>
			</div>
		
			<div ng-if="activityEventCtrl.selectedDocument.useFixedFolder==true">
				<img style="margin: 0 0 -5px -6px;" src="${pageContext.request.contextPath}/themes/sbi_default/img/treebase.gif" alt="" /> 
				
				<span>{{translate.load("scheduler.documentstree", "component_scheduler_messages")}}</span>
				
				<div id="docTree" ui-tree="" data-drag-enabled="false"
						data-drag-delay="false" data-empty-placeholder-enabled="false">
					
					<script type="text/ng-template" id="lowFunctionalityTreeNodeTemplate">						
						<div ui-tree-handle layout="row">
							<div class="indicator-child "></div>
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
				<label>{{translate.load("scheduler.useFolderDataset", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFolderDataset"></md-checkbox>
				<md-button type="button" id="useFolderDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.showInfoBox(translate.load('scheduler.useFolderDataset', 'component_scheduler_messages'),translate.load('scheduler.help.useFolderDataset', 'component_scheduler_messages'),'useFolderDataset')" >
         			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
        		</md-button>
			</div>
		
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
				<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
				<md-select aria-label="aria-label"
						ng-model="activityEventCtrl.selectedDocument.datasetFolderLabel" required name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
					<md-option ng-repeat="item in activityEventCtrl.datasets " value="{{item.label}}">{{item.label}}</md-option> 
				</md-select> 
			</md-input-container> 
				
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
				<label>{{translate.load("scheduler.folderToDriver", "component_scheduler_messages")}}</label>
				<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderParameter " required 
						name={{translate.load("sbi.scheduler.schedulation.events.event.parameter")}}>
					<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
				</md-select>
			</md-input-container> 
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem"
			ng-class="activityEventCtrl.selectedDocument.sendtojavaclass? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.sendtojavaclass", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendtojavaclass"></md-checkbox>
		</div>
	</md-toolbar>
	
	<div ng-if="activityEventCtrl.selectedDocument.sendtojavaclass">
		<md-content layout-padding class="borderBox"> 
			<md-input-container>
				<label>{{translate.load("scheduler.javaclasspath", "component_scheduler_messages")}}:</label>
				<input ng-model="activityEventCtrl.selectedDocument.javaclasspath"
						maxlength="100" ng-maxlength="100" md-maxlength="100"> 
			</md-input-container> 
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem"
			ng-class="activityEventCtrl.selectedDocument.sendmail? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.sendmail", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendmail"></md-checkbox>
		</div>
	</md-toolbar>
	
	<div ng-if="activityEventCtrl.selectedDocument.sendmail">
		<md-content layout-padding class="borderBox">
	
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.uniqueMail", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label"
					 ng-model="activityEventCtrl.selectedDocument.uniqueMail">
			</div>
	
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.zipMailDocument", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipMailDocument">
			</div>
	
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.zipMailDocument==true">
				<label>{{translate.load("scheduler.zipFileName", "component_scheduler_messages")}}:</label>
				<input ng-model="activityEventCtrl.selectedDocument.zipMailName" maxlength="100"
						ng-maxlength="100" md-maxlength="100"> 
			</md-input-container>
	
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.fixedRecipients", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useFixedRecipients"></md-checkbox>
				<md-button type="button" id="useFixedRecipients" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.showInfoBox(translate.load('scheduler.fixedRecipients', 'component_scheduler_messages'),translate.load('scheduler.help.useFixedRecipients', 'component_scheduler_messages'),'useFixedRecipients')" >
         			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
        		</md-button>
			</div>
	
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.useFixedRecipients==true">
				<label>{{translate.load("scheduler.mailto", "component_scheduler_messages")}}:</label>
				<input ng-model="activityEventCtrl.selectedDocument.mailtos" maxlength="100" ng-maxlength="100" md-maxlength="100"> 
			</md-input-container>
	
			<div layout="row" class="checkboxRow" ng-if="activityEventCtrl.selectedDocument.parameters.length!=0">
				<label>{{translate.load("scheduler.useDatasetList", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useDataset"></md-checkbox>
				<md-button type="button" id="useDataset" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.showInfoBox(translate.load('scheduler.useDatasetList', 'component_scheduler_messages'),translate.load('scheduler.help.useDataset', 'component_scheduler_messages'),'useDataset')" >
         			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
        		</md-button>
			</div>
	
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.useDataset==true">
				<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
				<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetLabel" required 
						name={{translate.load("sbi.scheduler.schedulation.events.event.dataset")}}>
					<md-option ng-repeat="item in activityEventCtrl.datasets " 
							value="{{item.label}}">{{item.label}}</md-option>
				</md-select> 
			</md-input-container> 
			
			<md-input-container class="subCheckboxRowElement" ng-if="activityEventCtrl.selectedDocument.useDataset==true ">
				<label>{{translate.load("scheduler.mailToDatasetParameter", "component_scheduler_messages")}}</label> 
				<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetParameter" required name={{translate.load("sbi.scheduler.schedulation.events.event.parameter")}}>
					<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
				</md-select> 
			</md-input-container>
		
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.useExpression", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.useExpression"></md-checkbox>
				<md-button type="button" id="useExpression" class="md-icon-button md-mini" aria-label="help" ng-click="activityEventCtrl.showInfoBox(translate.load('scheduler.useExpression', 'component_scheduler_messages'),translate.load('scheduler.help.useExpression', 'component_scheduler_messages'),'useExpression')" >
         			<md-icon md-font-icon="fa fa-info-circle fa-2x" ></md-icon>
        		</md-button>
			</div>
		
			<md-input-container class="subCheckboxRowElement"
					ng-if="activityEventCtrl.selectedDocument.useExpression==true">
				<label>{{translate.load("scheduler.mailToExpression", "component_scheduler_messages")}}:</label>
				<input ng-model="activityEventCtrl.selectedDocument.expression" maxlength="100"
						ng-maxlength="100" md-maxlength="100"> 
			</md-input-container>
		
			<div layout="row" class="checkboxRow">
				<label>{{translate.load("scheduler.reportNameInSubject", "component_scheduler_messages")}}:</label>
				<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.reportNameInSubject">
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
	
	<md-toolbar class="minihead unselectedItem"
			ng-class="activityEventCtrl.selectedDocument.saveasdl? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row">
			<label>{{translate.load("scheduler.distributionlist", "component_scheduler_messages")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasdl"></md-checkbox>
		</div>
	</md-toolbar>
</div>