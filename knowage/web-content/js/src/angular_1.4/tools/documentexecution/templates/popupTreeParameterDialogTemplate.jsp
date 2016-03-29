<md-dialog md-theme="{{::paramDialogCtrl.theme}}"
		aria-label="{{::paramDialogCtrl.dialogTitle}}" ng-class="dialog.css">
	<md-toolbar>
		<div class="md-title" layout-fill>
			<h2 class="md-title" layout-margin>
				{{ ::paramDialogCtrl.dialogTitle }}
			</h2>
		</div>
	</md-toolbar>
	
	<!--
	<md-content style="font-size:8px;">{{paramDialogCtrl.tempParameter|json}}</md-content>
	-->
	
	<md-dialog-content class="md-dialog-content" role="document" tabIndex="-1">
		<div class="md-dialog-content-body"	md-template="::paramDialogCtrl.mdContent"></div>
		
		<component-tree ng-model="paramDialogCtrl.tempParameter.children" subnode-key="children" 
				text-to-show-key="value" drag-enabled="false"
				multi-select="::paramDialogCtrl.tempParameter.multivalue"
				click-function="paramDialogCtrl.setTreeParameterValue(item)"
				is-folder-fn="paramDialogCtrl.isFolderFn(node)"
				is-open-folder-fn="paramDialogCtrl.isOpenFolderFn(node)"
				is-document-fn="paramDialogCtrl.isDocumentFn(node)"
				show-node-check-box-fn="paramDialogCtrl.showNodeCheckBoxFn(node)"
				dynamic-tree
				/>
	</md-dialog-content>
	
	<div class="md-actions">
		<md-button ng-click="paramDialogCtrl.abort()" class="md-primary">
			{{ paramDialogCtrl.dialogCancelLabel }}</md-button>
				
		<md-button ng-click="paramDialogCtrl.save()" class="md-primary">
			{{ paramDialogCtrl.dialogSaveLabel }}</md-button>
	</div>
</md-dialog>