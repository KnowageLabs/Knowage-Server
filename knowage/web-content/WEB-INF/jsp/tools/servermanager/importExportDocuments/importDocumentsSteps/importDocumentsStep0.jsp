<div layout="row" layout-wrap >
	<file-upload flex id="fileUploadImport" ng-model="IEDConf.fileImport"></file-upload>
	<md-button class="md-fab md-fab-mini"  ng-disabled="isInvalidImportStep0Form();" aria-label="{{translate.load('SBISet.import','component_impexp_messages');}} {{translate.load('sbi.ds.wizard.file');}}" ng-click="importFile()">
       			<md-icon class="fa fa-download center-ico"></md-icon>
   			 </md-button>	 
</div>
<div layout-padding class="associations-container">
	<md-toolbar class="miniheadassociations" >
		<div class="md-toolbar-tools" style="margin-top: 5px;">	
			{{translate.load("impexp.Associations","component_impexp_messages");}}
		</div>
	</md-toolbar>
	
	<md-radio-group ng-model="IEDConf.associations">
    	<md-radio-button value="noAssociations " class="md-primary" >{{translate.load("impexp.withoutAss","component_impexp_messages");}}</md-radio-button>
    	<md-radio-button value="mandatoryAssociations">{{translate.load("impexp.mandatoryAss","component_impexp_messages");}}</md-radio-button>
     	<md-radio-button value="defaultAssociations">{{translate.load("impexp.defaultAss","component_impexp_messages");}}</md-radio-button>
    </md-radio-group>
    <div layout-padding layout="column" layout-wrap ng-if = "IEDConf.associations != 'noAssociations' ">
    	<div layout-xs="column" layout-align-xs="center stretch" layout="row"  layout-align="start center">
	    	<md-input-container flex   class="md-block">
				<label>{{translate.load("impexp.savedAss","component_impexp_messages");}}</label>
				<input type="text" ng-model="IEDConf.fileAssociation.name" ng-disabled="true" aria-label="{{translate.load('impexp.savedAss','component_impexp_messages');}}">
			</md-input-container>
    	
			<md-button class="md-fab md-fab-mini" ng-click="listAssociation()" aria-label="{{translate.load('impexp.listAssFile','component_impexp_messages')}}" >
         				<md-icon class="fa fa-search center-ico"></md-icon>
     				</md-button>
		</div>

    </div>
</div>
