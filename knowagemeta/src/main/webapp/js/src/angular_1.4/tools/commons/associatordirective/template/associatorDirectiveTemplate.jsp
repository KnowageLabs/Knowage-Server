<div layout="row" id="assDirApp" class="kn-associatorDirective" layout-align="center start">
	<div layout="column" flex class="parametersList">
		<md-toolbar class="secondaryToolbar md-knowage-theme">
		<div class="md-toolbar-tools ">
			<h1>{{sourceColumnLabel}}</h1>
		</div>
		</md-toolbar>
		<md-list class="md-dense" flex>
			<md-list-item
			id="source-{{$index}}" ng-repeat="item in sourceModel" draggable="true"
			item="item"
			ng-click="">
			{{item[sourceName]}} 
				<md-divider ng-if="!$last"></md-divider>
			</md-list-item>
		</md-list>
	</div>
	<div layout="column" flex class="parametersList" >
		<md-toolbar class="secondaryToolbar md-knowage-theme">
		<div class="md-toolbar-tools ">
			<h1>{{targetColumnLabel}}</h1>
		</div>
		</md-toolbar>
		<md-list class="md-dense" flex> <md-list-item
		class="secondary-button-padding" id="target-{{$index}}"
		ng-repeat="item in targetModel" droppable item="item" ng-click="null"
		layout="row"> <span flex=40 class="truncate">{{item[targetName]}}</span>
		<span flex ng-if="item[associatedItem].length>0"><i
		class="fa fa-link" aria-hidden="true"></i></span> <span flex=40
		layout="column"> <span layout="row"
			ng-repeat="linkedItem in item[associatedItem]"> <span
			class="truncate">{{getAssociatedParentName(linkedItem)}}{{linkedItem[sourceName]}}</span>
			<md-button ng-if="item[associatedItem].length>1"
			class="md-icon-button " aria-label="delete relationship"
			ng-click="deleteRelationship(item,$index)"> <md-icon
			md-font-icon="fa fa-times"></md-icon> </md-button>
		</span>
	</span> <md-button ng-if="item[associatedItem].length>0"
	class="md-secondary md-icon-button " aria-label="delete relationship"
	ng-click="deleteRelationship(item)"> <md-icon
	md-font-icon="fa fa-trash"></md-icon> </md-button> <md-divider ng-if="!$last"></md-divider>
	</md-list-item> </md-list>
</div>
</div>
<style>

</style>