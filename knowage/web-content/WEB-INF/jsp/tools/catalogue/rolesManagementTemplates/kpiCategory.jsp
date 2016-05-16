<md-content flex  class="ToolbarBox miniToolbar noBorder mozTable" ng-controller="kpiCategoryController">
<md-whiteframe class="md-whiteframe-4dp filterWhiteFrame " layout-margin> 
		<md-toolbar	class="miniheadimportexport secondaryToolbar">
			<div class="md-toolbar-tools">
				<h2 class="md-flex">{{translate.load("sbi.roles.listcategory")}}</h2>
			</div>
		</md-toolbar>
	<div layout="row" >
	<div flex=50 >
		<md-whiteframe class="md-whiteframe-4dp" layout-margin > 
		<div id="lista">
			<div layout="row" layout-wrap>
				<div>
					<md-checkbox ng-checked="flagCheck" ng-click="selectAll()">
					<h4>{{translate.load("sbi.importusers.selectall")}}</h4>
					</md-checkbox>

				</div>
			</div>
			<div layout="row" layout-wrap flex>
				<div flex="90" ng-repeat="us in listCategories">
					<md-checkbox ng-checked="exists(us, categoriesSelected)"
						ng-click="toggle(us, categoriesSelected)"> {{ us.VALUE_CD}} </md-checkbox>

				</div>
			</div>
		</div>
		</md-checkbox>
		</md-whiteframe>
	</div>
	</div>
</md-whiteframe>	
</md-content>	
			