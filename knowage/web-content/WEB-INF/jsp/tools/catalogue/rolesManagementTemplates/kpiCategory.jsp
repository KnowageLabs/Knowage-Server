<md-content flex  class="ToolbarBox miniToolbar noBorder mozTable" ng-controller="kpiCategoryController">
<md-card> 
		<md-toolbar	class="miniheadimportexport md-toolbar-tools secondaryToolbar">
			{{translate.load("sbi.roles.listcategory")}}
		</md-toolbar>
	<div layout="row" >
	<angular-table
		layout-fill id="rolekpiCategory_id" ng-model="listCategories"
		columns='[{"label":"NAME","name":"VALUE_NM","size":"50px"} ]'
				selected-item="categoriesSelected" highlights-selected-item=true
				multi-select="true"> </angular-table>
	<!--  <div flex=50 >
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
	</div>-->
	</div>
</md-card>	
</md-content>	
			