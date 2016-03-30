<md-dialog>
	<md-whiteframe class="md-whiteframe-4dp layout-padding" layout-margin>
		<angular-table 
			id="foundKpisTable" ng-model="foundKpis"
			columns='[{"label":"KPI name","name":"name"},{"label":"Category","name":"category"},{"label":"Date","name":"date"},{"label":"Author","name":"author"}]'
			columnsSearch='["name"]' show-search-bar="true"
			multi-select="true" selected-item="selectedKpis"
			scope-functions="tableFunction"
			speed-menu-option="foundActions"
			click-function="alert(item);"> </angular-table>
	</md-whiteframe>
	<md-actions layout="row">
		<md-button class="md-mini" flex ng-click="close()">Cancel</md-button>
		<md-button class="md-mini" flex ng-click="ok()">Select</md-button>
	</md-actions>
</md-dialog>
