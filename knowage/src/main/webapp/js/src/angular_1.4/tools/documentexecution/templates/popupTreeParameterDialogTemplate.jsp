<md-dialog md-theme="{{::paramDialogCtrl.theme}}" style="height:95%; width:95%; max-width: 100%; max-height: 100%;" ng-cloak
		aria-label="{{::paramDialogCtrl.dialogTitle}}" ng-class="dialog.css" layout="column">
	<md-toolbar>
		<div class="md-toolbar-tools">
			<h2>{{ ::paramDialogCtrl.dialogTitle }}</h2>
		</div>
	</md-toolbar>
	
	<md-dialog-content class="md-dialog-content" role="document" tabIndex="-1" flex>
		<div class="md-dialog-content-body"	md-template="::paramDialogCtrl.mdContent"></div>
		
		<!-- div layout="row" layout-align="start center" class="kn-treePath">
			<span ng-repeat="path in paramDialogCtrl.tempParameter.treePath">{{path}} <i class="fa fa-chevron-right" ng-if="!$last"></i></span>
		</div-->

		<component-tree ng-model="paramDialogCtrl.tempParameter.children" subnode-key="children" 
				text-to-show-key="description" drag-enabled="false"
				multi-select="::paramDialogCtrl.tempParameter.multivalue"
				click-function="paramDialogCtrl.setTreeParameterValue(node)"
				expand-on-click=true
				server-loading=true
				is-folder-fn="paramDialogCtrl.isFolderFn(node)"
				is-open-folder-fn="paramDialogCtrl.isOpenFolderFn(node)"
				is-leaf-fn="paramDialogCtrl.isDocumentFn(node)"
				folder-icon-fn="paramDialogCtrl.getFolderIconClass(node)"
				open-folder-icon-fn="paramDialogCtrl.getOpenFolderIconClass(node)"
				show-node-checkbox-fn="paramDialogCtrl.showNodeCheckBoxFn(node)"
				is-internal-selection-allowed="paramDialogCtrl.allowInternalNodeSelection"
				selected-item="paramDialogCtrl.tempParameter.parameterDescription"
				dynamic-tree
				not-hide-on-load="true"
				/>
	</md-dialog-content>
	
	<div class="md-actions">
		<md-button ng-click="paramDialogCtrl.abort()" class="md-raised">
			{{ paramDialogCtrl.dialogCancelLabel }}</md-button>
				
		<md-button ng-disabled="paramDialogCtrl.disableButton()" ng-click="paramDialogCtrl.save()" class="md-raised md-primary">
			{{ paramDialogCtrl.dialogSaveLabel }}</md-button>
	</div>
</md-dialog>