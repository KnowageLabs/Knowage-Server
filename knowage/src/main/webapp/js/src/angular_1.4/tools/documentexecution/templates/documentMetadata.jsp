<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

  <% boolean canModify = false;
 	 if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY)) {
 			canModify = true;
  
  } %>
<md-dialog id="metadataDlg" aria-label="{{::metadataDlgCtrl.lblTitle}}" layout="column" flex class="metadataDialog" md-dynamic-height>
		
	
		<md-toolbar >
			<div class="md-toolbar-tools">
		    	<h2>{{::metadataDlgCtrl.lblTitle}}</h2>
	       	</div>
	  	</md-toolbar>
	  	<md-dialog-content>
 		<div class="md-dialog-content">
			<expander-box id="generalMetadata" expanded="true" title="{{::metadataDlgCtrl.lblGeneralMeta}}" toolbar-class="secondaryToolbar"> 
				<md-list flex>
		     		<md-list-item ng-repeat="item in metadataDlgCtrl.generalMetadata">
		        		<span flex="20"><b>{{ ::item.name }}</b></span><span flex>{{ ::item.value }}</span>
		        	</md-list-item>
		     	</md-list>
			</expander-box>
			<expander-box id="shortMetadata" color="white" background-color="rgb(63,81,181)" expanded="false" title="{{::metadataDlgCtrl.lblShortMeta}}" toolbar-class="secondaryToolbar"> 
<!--				<md-list>
					<md-list-item ng-repeat="item in metadataDlgCtrl.shortText">
						<div flex>
		     				<md-input-container>
		     					<label>{{ ::item.name }}</label><input ng-model="item.value"  <%= canModify? "":"readonly" %> >
		     				</md-input-container>
						</div>
	        		</md-list-item>
	        	</md-list>
-->
				<div layout=column layout-padding flex="100">
						<div flex ng-repeat="item in metadataDlgCtrl.shortText" layout=row>
		     				<md-input-container flex>
		     					<label>{{ ::item.name }}</label><input ng-model="item.value"  <%= canModify? "":"readonly" %> >
		     				</md-input-container>
						</div>
				</div>	        		
			</expander-box>
			<expander-box  id="longMetadata" color="white" background-color="rgb(63,81,181)" expanded="true" title="{{::metadataDlgCtrl.lblLongMeta}}" toolbar-class="secondaryToolbar"> 
				<md-tabs class="removeTransition" layout="column" md-border-bottom md-dynamic-height >
					<md-tab flex=100  ng-repeat="item in metadataDlgCtrl.longText" label="{{::item.name}}" md-on-select="metadataDlgCtrl.setTab($index)">
					 <md-tab-body >
					 <!-- workaround to disable wysiwyg if user haven't authorization -->
					 <div  <%= canModify? "style='display:none'":"" %> style="position:absolute; z-index:1000;background:transparent;" layout-fill>
					 
					 </div>
					<wysiwyg-edit ng-if="metadataDlgCtrl.isSelectedTab($index)" content="item.value"></wysiwyg-edit>
					</md-tab-body>
						
					</md-tab>
				</md-tabs>
			</expander-box>
			
			<expander-box  id="attachments" color="white" background-color="rgb(63,81,181)" expanded="false" title="{{::metadataDlgCtrl.lblAttachments}}" toolbar-class="secondaryToolbar"> 
				<!--   metadataDlgCtrl.file: {{metadataDlgCtrl.file}} -->

				<!-- <table flex>
					  <tr ng-repeat="fileMeta in metadataDlgCtrl.file">
					    <td> Metadata name:</td>
					    <td> {{fileMeta.name}}</td>
					    <td> &nbsp; Saved file: </td>
					    <td> {{fileMeta.value}}	 </td>
					    <td> <md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.download(fileMeta.id)">Download</md-button></td> 
					    <td> <file-upload id="id_file_upload-{{$index}}" ng-model="fileMeta.fileToSave" ng-disabled=false></file-upload> </td>
						<td> <md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.uploadFile(fileMeta.fileToSave)">Upload</md-button> </td>							 	
					  </tr>
				</table>	-->		
				
				
				 	
				<table flex>
					  <tr ng-repeat="fileMeta in metadataDlgCtrl.file">
					  <!-- 	<td> <md-input-container><label>Label</label><input ng-model="fileMeta.fileLabel" <%= canModify? "":"readonly" %> ></md-input-container></td> -->
					 <!-- <td>&nbsp;Saved&nbsp;file: </td>-->
					    <td>{{fileMeta.fileName}}</td> 
					 <!-- <td> &nbsp;Save&nbsp;date: </td>-->
					    <td>{{fileMeta.saveDate}}</td>
					    <td><file-upload id="id_file_upload-{{$index}}" ng-model="fileMeta.fileToSave" ng-disabled=false ng-if=<%= canModify %>></file-upload></td>
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.uploadFile(fileMeta.fileToSave)" ng-if=<%= canModify %>>Upload</md-button></td>		
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.download(fileMeta.id,fileMeta.value)" ng-if="fileMeta.fileName" >Download</md-button></td>
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.cleanFile(fileMeta)" ng-if=<%= canModify %>>Clean</md-button></td>						 	
					  </tr>
				</table> 
				
				<!-- 
				
				<div ng-repeat="fileMeta in metadataDlgCtrl.file" layout="column">
					<div layout="row" >
						<div flex="15">
							<md-input-container> <label>Label</label><input ng-model="fileMeta.fileLabel" <%= canModify? "":"readonly" %> ></md-input-container>
						</div>
						<div flex="15">
							{{fileMeta.fileName}}
						</div>
						<div flex="15">
							{{fileMeta.saveDate}}
						</div>
						<div flex="15">
							<file-upload id="id_file_upload-{{$index}}" ng-model="fileMeta.fileToSave" ng-disabled=false ng-if=<%= canModify %>></file-upload>
						</div>
						<div flex="10">
							<md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.uploadFile(fileMeta.fileToSave)" ng-if=<%= canModify %>>Upload</md-button>
						</div>
						<div flex="10">
							<md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.download(fileMeta.id,fileMeta.value)" ng-if="fileMeta.fileName" >Download</md-button>
						</div>
						<div flex="10">
							<md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.cleanFile(fileMeta.id)" ng-if=<%= canModify %>>Clean</md-button>
						</div>
					</div>
				</div>	
					value==savedFile
				-->

			</expander-box>
			
			
			
		</div>


	
	  	</md-dialog-content>
	  	<div class="md-actions" layout="row">
		  	<span flex></span>
		  	<md-button class="md-raised" ng-click="metadataDlgCtrl.close()">
				CLOSE
			</md-button>
	    	<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY)) { %>
			     <md-button aria-label="{{::metadataDlgCtrl.lblSave}}" class="md-primary md-raised" 
					ng-click="metadataDlgCtrl.save()">
					{{::metadataDlgCtrl.lblSave}}
				 </md-button>
			<%} %>
    	</div>
</md-dialog>
			