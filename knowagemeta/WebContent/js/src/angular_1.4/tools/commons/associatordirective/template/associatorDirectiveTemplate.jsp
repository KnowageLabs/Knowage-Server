<div layout="row" id="assDirApp">
	<div layout="column" flex class="parametersList">
		<md-toolbar class="secondaryToolbar md-knowage-theme">
		<div class="md-toolbar-tools ">
			<h1>{{sourceColumnLabel}}</h1>
		</div>
		</md-toolbar>
		<md-list class="md-dense" flex>
			<md-list-item
			id="source-{{$index}}" ng-repeat="item in sourceModel" draggable
			item="item"
			ng-click="$event.stopPropagation();$event.preventDefault();">
			{{item[sourceName]}} 
				<md-divider ng-if="!$last"></md-divider>
			</md-list-item>
		</md-list>
	</div>
	<div layout="column" flex class="parametersList">
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
.associator-parameter {
	background-color: #C4DCF3;
	color: rgba(255, 255, 255, 0.87);
	font-size: 15px;
	padding: 5px 16px 0 16px !important;
	font-weight: 400;
	min-height: 33px !important
}
.associator-parameter.highlight-selected-parameter {
	background-color: #a9c3db;
}
.associator-parameter.link {
	color: black;
	background-color: #E6E6E6;
}
.associator-parameter .fa-link {
	color: #3F51B5;
}
.loadingSpinner {
	text-align: center;
	vertical-align: middle;
	width: 100%;
	height: 100%;
}
.openDocIcon {
	margin-top: 6px;
	color: white;
}

.parametersList>div {
	padding: 3px;
}
.parametersList li {
	border: 0;
	padding: 2px;
	border: 0 !important;
}
button.md-raised {
	margin-top: 22px;
}
.parametersList>md-list {
	border: 1px solid #a9c3db;
}
.parametersList>md-list md-list-item.over {
	background-color: rgba(128, 128, 128, 0.32);
	border: 1px dashed;
}
.parametersList>md-list md-list-item.errorClass {
	background-color: rgba(255, 0, 0, 0.29);
	border: 1px dashed red;
}
.parametersList>md-list md-list-item.multyValue>.md-button>._md-list-item-inner
	{
	height: 100% !important;
}
.truncate {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>