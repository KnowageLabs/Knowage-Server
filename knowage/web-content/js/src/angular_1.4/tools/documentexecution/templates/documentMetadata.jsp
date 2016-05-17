<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<md-dialog id="metadataDlg" aria-label="{{::metadataDlgCtrl.lblTitle}}" layout="column" flex class="metadataDialog">
		
	
		<md-toolbar >
			<div class="md-toolbar-tools">
		    	<h2>{{::metadataDlgCtrl.lblTitle}}</h2>
	       	</div>
	  	</md-toolbar>
	  	<md-dialog-content>
 		<div class="md-dialog-content">
			<expander-box id="generalMetadata" expanded="true" title="metadataDlgCtrl.lblGeneralMeta" toolbar-class="secondaryToolbar"> 
				<md-list flex>
		     		<md-list-item ng-repeat="item in metadataDlgCtrl.generalMetadata">
		        		<span flex="20"><b>{{ ::item.name }}</b></span><span flex>{{ ::item.value }}</span>
		        	</md-list-item>
		     	</md-list>
			</expander-box>
			<expander-box id="shortMetadata" color="white" background-color="rgb(63,81,181)" expanded="false" title="metadataDlgCtrl.lblShortMeta" toolbar-class="secondaryToolbar"> 
				<md-list-item ng-repeat="item in metadataDlgCtrl.shortText">
					<div flex>
		     			<md-input-container>
		     				<label>{{ ::item.name }}</label><input ng-model="item.value">
		     			</md-input-container>
					</div>
	        	</md-list-item>
			</expander-box>
			<expander-box  id="longMetadata" color="white" background-color="rgb(63,81,181)" expanded="false" title="metadataDlgCtrl.lblLongMeta" toolbar-class="secondaryToolbar"> 
				<md-tabs class="removeTransition" layout="column" md-border-bottom md-dynamic-height >
					<md-tab flex=200  ng-repeat="item in metadataDlgCtrl.longText" label="{{::item.name}}" md-on-select="metadataDlgCtrl.setTab($index)">
					 <md-tab-body >
					<wysiwyg-edit ng-if="metadataDlgCtrl.isSelectedTab($index)" content="item.value"></wysiwyg-edit>
					</md-tab-body>
						
					</md-tab>
				</md-tabs>
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
			