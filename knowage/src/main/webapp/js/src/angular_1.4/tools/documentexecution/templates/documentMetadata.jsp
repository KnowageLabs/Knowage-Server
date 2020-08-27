<%@ page language="java" pageEncoding="UTF-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% String contextName = KnowageSystemConfiguration.getKnowageContext(); %>

	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

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
	  	
	  	<md-content>
	  		<md-card ng-if="metadataDlgCtrl.generalMetadata.length>0" style="background-color: #fafafa">
	 			<md-subheader >{{::metadataDlgCtrl.translate.load('sbi.execution.metadata.documentdetails')}}</md-subheader>
	 			<div layout="row" style="padding:8px" layout-wrap ng-if="metadataDlgCtrl.generalMetadata.length>0">
	 				  <md-input-container ng-class="{'flex':$index != 3, 'flex-100': $index == 3}" ng-repeat="item in metadataDlgCtrl.generalMetadata track by $index" ng-if="item.value && !$last">
				        <label style="color: #ccc">{{ ::item.name }}</label>
				        <textarea ng-model="item.value" rows="2" ng-if="$index == 3" readonly></textarea>
				        <input ng-model="item.value" type="text" ng-if="$index != 3" readonly/>
				      </md-input-container>
	 			</div>
	 		</md-card>
	 		<md-card  ng-if="metadataDlgCtrl.shortText.length>0 || metadataDlgCtrl.longText.length>0" class="customMetadata">
	 			<md-subheader>{{::metadataDlgCtrl.translate.load('sbi.execution.metadata.custom')}}</md-subheader>
	 			<div layout="row" style="padding:8px" layout-wrap ng-if="metadataDlgCtrl.shortText.length>0">
	 				<md-input-container flex="33" ng-repeat="item in metadataDlgCtrl.shortText track by $index" >
				        <label>{{ ::item.name }}</label>
				        <input ng-model="item.value" type="text" <%= canModify? "":"readonly" %> />
				      </md-input-container>
	 			</div>
	 			<md-tabs class="removeTransition" layout="column" md-border-bottom md-dynamic-height ng-if="metadataDlgCtrl.longText.length>0">
					<md-tab flex=100  ng-repeat="item in metadataDlgCtrl.longText" label="{{::item.name}}" md-on-select="metadataDlgCtrl.setTab($index)">
					 <md-tab-body >
					 <!-- workaround to disable wysiwyg if user haven't authorization -->
					 <div  <%= canModify? "style='display:none'":"" %> style="position:absolute; z-index:1000;background:transparent;" layout-fill>
					 
					 </div>
					<wysiwyg-edit ng-if="metadataDlgCtrl.isSelectedTab($index)" content="item.value" style="max-height: 250px;"></wysiwyg-edit>
					</md-tab-body>
						
					</md-tab>
				</md-tabs>
			</md-card>
			<md-card ng-if="metadataDlgCtrl.file.length>0">
				<md-subheader>{{::metadataDlgCtrl.lblAttachments}}</md-subheader>
				<table style="width:100%;padding:8px;">
					  <tr ng-repeat="fileMeta in metadataDlgCtrl.file">
					    <td>{{fileMeta.fileName}}</td> 
					    <td>{{fileMeta.saveDate}}</td>
					    <td><file-upload id="id_file_upload-{{$index}}" ng-model="fileMeta.fileToSave" ng-disabled=false ng-if=<%= canModify %>></file-upload></td>
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.uploadFile(fileMeta.fileToSave)" ng-if=<%= canModify %>>Upload</md-button></td>		
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.download(fileMeta.id,fileMeta.value)" ng-if="fileMeta.fileName" >Download</md-button></td>
						<td><md-button class="md-ExtraMini md-raised " ng-click="metadataDlgCtrl.cleanFile(fileMeta)" ng-if=<%= canModify %>>Clean</md-button></td>						 	
					  </tr>
				</table> 
			</md-card>
	  	</md-content>
	  	<div class="md-actions" layout="row">
		  	<span flex></span>
		  	<md-button class="md-raised" ng-click="metadataDlgCtrl.close()">
				{{::metadataDlgCtrl.translate.load('sbi.general.close')}}
			</md-button>
	    	<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY)) { %>
			     <md-button aria-label="{{::metadataDlgCtrl.lblSave}}" class="md-primary md-raised" 
					ng-click="metadataDlgCtrl.save()">
					{{::metadataDlgCtrl.lblSave}}
				 </md-button>
			<%} %>
    	</div>
</md-dialog>
			