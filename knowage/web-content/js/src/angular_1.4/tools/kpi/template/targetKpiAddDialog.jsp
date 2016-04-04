<md-dialog aria-label="Add KPI association" ng-cloak>
	<form>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h1>Add KPI association</h1>
				<span flex></span>
				<md-button class="md-primary" ng-click="ok()">Save</md-button>
				<md-button class="md-primary" ng-click="close()">Close</md-button>
			</div>
		</md-toolbar>
		<md-dialog-content >
			<div class="md-dialog-content">
				<angular-table 
					id="foundKpisTable" ng-model="foundKpis"
					columns='[{"label":"KPI name","name":"name"},{"label":"Category","name":"category"},{"label":"Date","name":"date"},{"label":"Author","name":"author"}]'
					columnsSearch='["name"]' show-search-bar="true"
					multi-select="true" selected-item="selectedKpis"
					scope-functions="tableFunction"
					speed-menu-option="foundActions"
					click-function="alert(item);"></angular-table>
			</div>
		</md-dialog-content>
	</form>
</md-dialog>