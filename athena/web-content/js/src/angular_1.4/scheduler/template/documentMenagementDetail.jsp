	<md-chips ng-model="activityEvent.event.document" readonly="true">
      <md-chip-template ng-click="activityEvent.selectedDocument=$chip"  >
        <strong ng-class="{'selectedDoc' :activityEvent.selectedDocument.label==$chip.label }">{{$chip.label}}</strong>
      </md-chip-template>
    </md-chips>

    
  <md-toolbar class="minihead unselectedItem " ng-class="activityEvent.selectedDocument.saveassnapshot? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SaveAsSnapshot")}}:</label>
            <md-checkbox  ng-model="activityEvent.selectedDocument.saveassnapshot" >
        </div>
	</md-toolbar>
	<div ng-if="activityEvent.selectedDocument.saveassnapshot">
		<md-content layout-padding class="borderBox"> 
				<md-input-container>
                       <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
                       <input ng-model="activityEvent.selectedDocument.snapshotname"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                    		</md-input-container>
                    		
                    		<md-input-container>
                       <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
                       <input ng-model="activityEvent.selectedDocument.snapshotdescription"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                    		</md-input-container>
                    		
                    		<md-input-container>
                       <label>History Length:</label>
                       <input ng-model="activityEvent.selectedDocument.snapshothistorylength"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                    		</md-input-container>
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.selectedDocument.saveasfile? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SaveAsFile")}}:</label>
                  <md-checkbox ng-model="activityEvent.selectedDocument.saveasfile">
                    </div>
	</md-toolbar>
	<div ng-if="activityEvent.selectedDocument.saveasfile">
		<md-content layout-padding class="borderBox"> 
			<md-input-container>
                      <label>{{translate.load("File Name")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.fileName"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>
                   		
                   		<md-input-container>
                      <label>{{translate.load("Folder Name")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.destinationfolder"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>	
                   		
                   		 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("Saved zipped file")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.zipFileDocument">
                   		 </div>	
                   		
                   		<md-input-container class="subCheckboxRowElement" ng-if="activityEvent.selectedDocument.zipFileDocument==true">
                      <label>{{translate.load("Folder Name")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.zipFileName"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>								


		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.selectedDocument.saveasdocument? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SaveAsDocument")}}:</label>
                  <md-checkbox ng-model="activityEvent.selectedDocument.saveasdocument">
                    </div>
	</md-toolbar>
	<div ng-if="activityEvent.selectedDocument.saveasdocument">
		<md-content layout-padding class="borderBox"> 
			<md-input-container>
                      <label>{{translate.load("sbi.scheduler.activity.events.event.name")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.documentname"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>
                   		
                   		<md-input-container>
                      <label>{{translate.load("sbi.scheduler.activity.events.event.description")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.documentdescription"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>
                   		
                   		 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("fixed folder")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.useFixedFolder">
                   		 </div>	
                   		 
                   		 alberello
                   		 
                   		<div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("folder from dataset")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.useFolderDataset">
                   		 </div>	
                   		
                   		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.useFolderDataset==true">
				<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
				<md-select ng-model="activityEvent.selectedDocument.datasetFolderLabel" >
					<md-option ng-repeat="item in jobDataCtrl.datasets "
						value="{{item.label}}">{{item.label}}</md-option> 
				</md-select> 
			</md-input-container> 
			
			<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.useFolderDataset==true">
				<label>{{translate.load("Driver")}}</label>
				<md-select ng-model="activityEvent.selectedDocument.datasetFolderParameter" >
					<md-option value="driver1">Driver1</md-option> 
					<md-option value="driver2">Driver2</md-option> 
				</md-select> 
			</md-input-container> 
                   		 
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.selectedDocument.sendtojavaclass? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SendToJavaClass")}}:</label>
                  <md-checkbox ng-model="activityEvent.selectedDocument.sendtojavaclass">
                    </div>
	</md-toolbar>
	<div ng-if="activityEvent.selectedDocument.sendtojavaclass">
		<md-content layout-padding class="borderBox"> 
			<md-input-container>
                       <label>{{translate.load("class path")}}:</label>
                       <input ng-model="activityEvent.selectedDocument.javaclasspath"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                    		</md-input-container>
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.selectedDocument.sendmail? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SendEmail")}}:</label>
                  <md-checkbox ng-model="activityEvent.selectedDocument.sendmail">
                    </div>
	</md-toolbar>
	<div ng-if="activityEvent.selectedDocument.sendmail">
		<md-content layout-padding class="borderBox"> 
		
			
			 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("send unique mailfor all scheduler")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.uniqueMail">
                   		 </div>	
                   		 
                   		  <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("send zipped file")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.zipMailDocument">
                   		 </div>	
                   		
                   		<md-input-container   class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.zipMailDocument==true">
                      <label>{{translate.load("zipped file name")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.zipMailName"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>		
                   		
                   		 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("fixed list of recipients")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.useFixedRecipients">
                   		 </div>	
                   		
                   		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.useFixedRecipients==true">
                      <label>{{translate.load("Mail to")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.mailtos"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>		
                   			
                   		 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("Use a Datasetas recipient's list")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.useDataset">
                   		 </div>	
                   		
                   		<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.useDataset==true">
				<label>{{translate.load("sbi.scheduler.activity.events.event.type.dataset")}}</label>
				<md-select ng-model="activityEvent.selectedDocument.datasetLabel" >
					<md-option ng-repeat="item in jobDataCtrl.datasets "
						value="{{item.label}}">{{item.label}}</md-option> 
				</md-select> 
			</md-input-container> 
			
			<md-input-container  class="subCheckboxRowElement"  ng-if="activityEvent.selectedDocument.useDataset==true">
				<label>{{translate.load("Parameter")}}</label>
				<md-select ng-model="activityEvent.selectedDocument.datasetParameter" >
					<md-option value="param1">param1</md-option> 
					<md-option value="param2">param2</md-option> 
				</md-select> 
			</md-input-container> 
					
			 <div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("Use an expression ")}}:</label>
                   <md-checkbox ng-model="activityEvent.selectedDocument.useExpression">
                   		 </div>	
                   		
                   		<md-input-container class="subCheckboxRowElement" ng-if="activityEvent.selectedDocument.useExpression==true">
                      <label>{{translate.load("Expression")}}:</label>
                      <input ng-model="activityEvent.selectedDocument.expression"  maxlength="100" ng-maxlength="100" md-maxlength="100">
                   		</md-input-container>			
		
			<div  layout="row" class="checkboxRow" >
			    <label>{{translate.load("Include report name ")}}:</label>
                 <md-checkbox ng-model="activityEvent.selectedDocument.reportNameInSubject">
            </div>	
		
			<md-input-container >
              <label>{{translate.load("Mail subject")}}:</label>
              <input ng-model="activityEvent.selectedDocument.mailsubj"  maxlength="100" ng-maxlength="100" md-maxlength="100">
           		</md-input-container>	
           		
           		<md-input-container >
              <label>{{translate.load("File name")}}:</label>
              <input ng-model="activityEvent.selectedDocument.containedFileName"  maxlength="100" ng-maxlength="100" md-maxlength="100">
           		</md-input-container>	
           		
           		<md-input-container >
              <label>{{translate.load("Mail text")}}:</label>
              <textarea ng-model="activityEvent.selectedDocument.mailTxt" columns="1" maxlength="500" ng-maxlength="500" md-maxlength="500"></textarea>
           		</md-input-container>	
		
		
		</md-content>
	</div>
	
	<md-toolbar class="minihead unselectedItem" ng-class="activityEvent.selectedDocument.saveasdl? 'selectedItem' : 'unselectedItem'">
		<div class="md-toolbar-tools" layout="row" >
		    <label>{{translate.load("SendToDistributionList")}}:</label>
                  <md-checkbox ng-model="activityEvent.selectedDocument.saveasdl">
                    </div>
	</md-toolbar>
