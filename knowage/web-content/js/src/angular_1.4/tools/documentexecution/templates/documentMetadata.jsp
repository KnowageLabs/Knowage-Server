<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<md-dialog id="metadataDlg" aria-label="{{::metadataDlgCtrl.lblTitle}}" style="width: 80%; height: 90%; overflow-y: visible;" layout="column">
		<md-dialog-content layout="column" flex>
	
		<md-toolbar class="secondaryToolbar">
			<div class="md-toolbar-tools">
		    	<h2 style="    font-size: 20px;    text-align: center;    width: 100%;">{{::metadataDlgCtrl.lblTitle}}</h2>
		      	<span flex></span>
		    	<md-button class="md-icon-button" ng-click="metadataDlgCtrl.close()">
		          <md-icon md-font-icon="fa fa-times closeIcon" aria-label="Close dialog"></md-icon>
		        </md-button>
	       	</div>
	  	</md-toolbar>
 		<md-content  flex layout-margin>
			<expander-box id="generalMetadata" expanded="true" title="metadataDlgCtrl.lblGeneralMeta" toolbar-class="ternaryToolbar"> 
				<md-list flex>
		     		<md-list-item ng-repeat="item in metadataDlgCtrl.generalMetadata">
		        		<span flex="20"><b>{{ ::item.name }}</b></span><span flex>{{ ::item.value }}</span>
		        	</md-list-item>
		     	</md-list>
			</expander-box>
			<expander-box id="shortMetadata" color="white" background-color="rgb(63,81,181)" expanded="false" title="metadataDlgCtrl.lblShortMeta" toolbar-class="ternaryToolbar"> 
				<md-list-item ng-repeat="item in metadataDlgCtrl.shortText">
					<div flex>
		     			<md-input-container>
		     				<label>{{ ::item.name }}</label><input ng-model="item.value">
		     			</md-input-container>
					</div>
	        	</md-list-item>
			</expander-box>
			<expander-box  id="longMetadata" color="white" background-color="rgb(63,81,181)" expanded="false" title="metadataDlgCtrl.lblLongMeta" toolbar-class="ternaryToolbar"> 
				<md-tabs class="removeTransition" layout="column" md-border-bottom md-dynamic-height >
					<md-tab flex=200  ng-repeat="item in metadataDlgCtrl.longText" label="{{::item.name}}" md-on-select="metadataDlgCtrl.setTab($index)">
					 <md-tab-body >
					<wysiwyg-edit ng-if="metadataDlgCtrl.isSelectedTab($index)" content="item.value"></wysiwyg-edit>
					</md-tab-body>
						
					</md-tab>
				</md-tabs>
			</expander-box>
			  </md-content>

	<div layout="row">
	<span flex></span>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY)) { %>
		      	<md-button aria-label="{{::metadataDlgCtrl.lblSave}}" class="md-raised" 
					ng-click="metadataDlgCtrl.save()">
					{{::metadataDlgCtrl.lblSave}}
			 	</md-button>
			 	<%} %>
	</div>
	  	</md-dialog-content>
</md-dialog>
			<!-- 
			<md-tabs md-dynamic-height md-border-bottom>
		        <md-tab label="{{::metadataDlgCtrl.lblGeneralMeta}}">
		        	<md-content class="md-padding">
		        		<md-list flex>
				     		<md-list-item ng-repeat="item in metadataDlgCtrl.generalMetadata">
				        		<span flex="20">{{ ::item.name }}</span><span flex>{{ ::item.value }}</span>
				        	</md-list-item>
				     	</md-list>
		        	</md-content>
		        </md-tab>
		        <md-tab label="{{::metadataDlgCtrl.lblShortMeta}}">
		        	<md-content class="md-padding">
		        		<md-list flex>
				     		<md-list-item ng-repeat="item in metadataDlgCtrl.shortText">
				     			<md-input-container>
				     				<label><b>{{ ::item.name }}</b></label><input ng-model="item.value">
				     			</md-input-container>
				        	</md-list-item>
				     	</md-list>
		        	</md-content> 
		        </md-tab>
		        <md-tab label="{{::metadataDlgCtrl.lblLongMeta}}">
		        	<md-content class="md-padding">
		        		<md-tabs md-border-bottom md-autoselect md-align-tabs="bottom">
							<md-tab ng-repeat="item in metadataDlgCtrl.longText" label="{{::item.name}}">
								<md-input-container class="md-block">
						        	<label>{{::item.name}}</label>
						        	<textarea ng-model="item.value" columns="1" md-maxlength="150" rows="5"></textarea>
						        </md-input-container>
							</md-tab>
						</md-tabs>
		        	</md-content> 
		        </md-tab>
		    </md-tabs>
		 -->