<md-chips ng-model="activityEventCtrl.event.documents" readonly="true">
	<md-chip-template ng-click="activityEventCtrl.selectedDocument=$chip">
		<strong ng-class="{'selectedDoc' : activityEventCtrl.selectedDocument.label==$chip.label }">{{$chip.label}}</strong>
	</md-chip-template> 
</md-chips>

<p ng-if="!(activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0)"> No documents associated</p>

<div ng-if="activityEventCtrl.selectedDocument!=undefined && activityEventCtrl.selectedDocument.length!=0">

<md-toolbar class="minihead unselectedItem "
		ng-class="activityEventCtrl.selectedDocument.saveassnapshot? 'selectedItem' : 'unselectedItem'">
	
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SaveAsSnapshot")}}:</label>
		<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveassnapshot">
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
			<label>History Length:</label> 
			<input ng-keyup="activityEventCtrl.selectedDocument.snapshothistorylength=activityEventCtrl.onlyNumberConvert(activityEventCtrl.selectedDocument.snapshothistorylength)" ng-model="activityEventCtrl.selectedDocument.snapshothistorylength">
		</md-input-container>
	</md-content>
</div>

<md-toolbar class="minihead unselectedItem"
	ng-class="activityEventCtrl.selectedDocument.saveasfile? 'selectedItem' : 'unselectedItem'">
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SaveAsFile")}}:</label>
		<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.saveasfile">
	</div>
</md-toolbar>

<div ng-if="activityEventCtrl.selectedDocument.saveasfile">
	<md-content layout-padding class="borderBox"> 
		<md-input-container>
			<label>{{translate.load("File Name")}}:</label> 
			<input ng-model="activityEventCtrl.selectedDocument.fileName" 
				maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
		</md-input-container>
		
		<md-input-container>
			<label>{{translate.load("Folder Name")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.destinationfolder"
				maxlength="100" ng-maxlength="100" md-maxlength="100"> 
		</md-input-container>
	
		<div layout="row" class="checkboxRow">
			<label>{{translate.load("Saved zipped file")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.zipFileDocument">
		</div>
	
		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.zipFileDocument==true">
			
			<label>{{translate.load("Folder Name")}}:</label> 
			<input ng-model="activityEventCtrl.selectedDocument.zipFileName" 
				maxlength="100"	ng-maxlength="100" md-maxlength="100"> 
		</md-input-container> 
	</md-content>
</div>

<md-toolbar class="minihead unselectedItem"
		ng-class="activityEventCtrl.selectedDocument.saveasdocument? 'selectedItem' : 'unselectedItem'">
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SaveAsDocument")}}:</label>
		<md-checkbox aria-label="aria-label"
			ng-model="activityEventCtrl.selectedDocument.saveasdocument">
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
			<label>{{translate.load("fixed folder")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.useFixedFolder">
		</div>
	
	<div ng-if="activityEventCtrl.selectedDocument.useFixedFolder==true">
		<img style="margin: 0 0 -5px -6px;" src="/athena/themes/sbi_default/img/treebase.gif" alt="" /> 
		
		<span>Documents tree</span>
		
		<div id="docTree" ui-tree="" data-drag-enabled="false"
				data-drag-delay="false" data-empty-placeholder-enabled="false">
			<ol id="olchiproot" ui-tree-nodes ng-model="activityEventCtrl.lowFunc">
				<li ng-repeat="subItem in activityEventCtrl.lowFunc" ui-tree-node
					ng-include="'/athena/js/src/angular_1.4/tools/commons/templates/lowFunctionalityTreeNode.html'"></li>
			</ol>
		</div>
	</div>
	
		<div layout="row" class="checkboxRow">
			<label>{{translate.load("folder from dataset")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.useFolderDataset">
		</div>
	
		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
			<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
			<md-select aria-label="aria-label"
					ng-model="activityEventCtrl.selectedDocument.datasetFolderLabel">
				<md-option ng-repeat="item in activityEventCtrl.datasets " value="{{item.label}}">{{item.label}}</md-option> 
			</md-select> 
		</md-input-container> 
			
		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.useFolderDataset==true">
			<label>{{translate.load("Driver")}}</label>
			<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetFolderParameter">
			<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
			</md-select>
		</md-input-container> 
	</md-content>
</div>

<md-toolbar class="minihead unselectedItem"
		ng-class="activityEventCtrl.selectedDocument.sendtojavaclass? 'selectedItem' : 'unselectedItem'">
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SendToJavaClass")}}:</label>
		<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendtojavaclass">
	</div>
</md-toolbar>

<div ng-if="activityEventCtrl.selectedDocument.sendtojavaclass">
	<md-content layout-padding class="borderBox"> 
		<md-input-container>
			<label>{{translate.load("class path")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.javaclasspath"
					maxlength="100" ng-maxlength="100" md-maxlength="100"> 
		</md-input-container> 
	</md-content>
</div>

<md-toolbar class="minihead unselectedItem"
		ng-class="activityEventCtrl.selectedDocument.sendmail? 'selectedItem' : 'unselectedItem'">
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SendEmail")}}:</label>
		<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.sendmail">
	</div>
</md-toolbar>

<div ng-if="activityEventCtrl.selectedDocument.sendmail">
	<md-content layout-padding class="borderBox">

		<div layout="row" class="checkboxRow">
			<label>{{translate.load("send unique mailfor all scheduler")}}:</label>
			<md-checkbox aria-label="aria-label"
				 ng-model="activityEventCtrl.selectedDocument.uniqueMail">
		</div>

		<div layout="row" class="checkboxRow">
			<label>{{translate.load("send zipped file")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.zipMailDocument">
		</div>

		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.zipMailDocument==true">
			<label>{{translate.load("zipped file name")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.zipMailName" maxlength="100"
					ng-maxlength="100" md-maxlength="100"> 
		</md-input-container>

		<div layout="row" class="checkboxRow">
			<label>{{translate.load("fixed list of recipients")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.useFixedRecipients">
		</div>

		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.useFixedRecipients==true">
			<label>{{translate.load("Mail to")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.mailtos" maxlength="100"
				ng-maxlength="100" md-maxlength="100"> 
		</md-input-container>

		<div layout="row" class="checkboxRow">
			<label>{{translate.load("Use a Datasetas recipient's list")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.useDataset">
		</div>

		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.useDataset==true">
			<label>{{translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}}</label>
			<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetLabel">
				<md-option ng-repeat="item in activityEventCtrl.datasets " 
						value="{{item.label}}">{{item.label}}</md-option>
			</md-select> 
		</md-input-container> 
		
		<md-input-container class="subCheckboxRowElement" ng-if="activityEventCtrl.selectedDocument.useDataset==true">
			<label>{{translate.load("Parameter")}}</label> 
			<md-select aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.datasetParameter">
				<md-option ng-repeat=" par in activityEventCtrl.selectedDocument.parameters" value="{{par}}">{{par}}</md-option>
			</md-select> 
		</md-input-container>
	
		<div layout="row" class="checkboxRow">
			<label>{{translate.load("Use an expression ")}}:</label>
			<md-checkbox aria-label="aria-label"
				ng-model="activityEventCtrl.selectedDocument.useExpression">
		</div>
	
		<md-input-container class="subCheckboxRowElement"
				ng-if="activityEventCtrl.selectedDocument.useExpression==true">
			<label>{{translate.load("Expression")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.expression" maxlength="100"
					ng-maxlength="100" md-maxlength="100"> 
		</md-input-container>
	
		<div layout="row" class="checkboxRow">
			<label>{{translate.load("Include report name ")}}:</label>
			<md-checkbox aria-label="aria-label" ng-model="activityEventCtrl.selectedDocument.reportNameInSubject">
		</div>
	
		<md-input-container> 
			<label>{{translate.load("Mail subject")}}:</label>
			<input ng-model="activityEventCtrl.selectedDocument.mailsubj"
					maxlength="100" ng-maxlength="100" md-maxlength="100"> 
		</md-input-container> 
		
		<md-input-container>
			<label>{{translate.load("File name")}}:</label> 
			<input ng-model="activityEventCtrl.selectedDocument.containedFileName"
				maxlength="100" ng-maxlength="100" md-maxlength="100">
		</md-input-container> 
		
		<md-input-container>
			<label>{{translate.load("Mail text")}}:</label>
			<textarea ng-model="activityEventCtrl.selectedDocument.mailtxt" columns="1"
					maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea> 
		</md-input-container> 
			
	</md-content>
</div>

<md-toolbar class="minihead unselectedItem"
		ng-class="activityEventCtrl.selectedDocument.saveasdl? 'selectedItem' : 'unselectedItem'">
	<div class="md-toolbar-tools" layout="row">
		<label>{{translate.load("SendToDistributionList")}}:</label>
		<md-checkbox aria-label="aria-label"
			ng-model="activityEventCtrl.selectedDocument.saveasdl">
	</div>
</md-toolbar>

</div>