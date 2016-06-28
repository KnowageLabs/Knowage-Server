<md-dialog aria-label="add Business model" ng-cloak style="min-width:90%; min-height:90%;">
	<form name="newBVForm" layout="column" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>{{translate.load("sbi.meta.new.businessview")}}</h2>
			</div>
		</md-toolbar>
		<md-dialog-content flex > 
			<div class="md-dialog-content" layout="column">
				
			</div>
		</md-dialog-content>
		<md-dialog-actions layout="row">
			<span flex></span>
			<md-button ng-click="cancel()">
				{{translate.load("sbi.general.cancel")}}
			</md-button>
			<md-button   ng-click="create()" ng-disabled="!newBVForm.$valid ">
				{{translate.load("sbi.generic.update")}}
			</md-button>
		</md-dialog-actions>
	</form>
</md-dialog>