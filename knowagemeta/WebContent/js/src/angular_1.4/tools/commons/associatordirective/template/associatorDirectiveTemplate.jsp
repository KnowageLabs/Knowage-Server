

		<div layout="row" id="assDirApp">
			<div layout="column" flex="50" class="parametersList">
				<md-toolbar class="secondaryToolbar md-knowage-theme" >
					<div class="md-toolbar-tools ">
						<h1>{{translate.load("sbi.meta.business.relationship.source.attributes");}}</h1>
					</div>
				</md-toolbar> 
					<md-list flex>
        				<md-list-item id="source-{{$index}}" draggable="true" ondragstart="angular.element(document.getElementById('assDirApp')).scope().drag(event)"    ng-repeat="item in sourceModel" ng-click="null">
        				 {{item[sourceName]}}
			    		</md-list-item>
			    	</md-list>
			</div>
			<div layout="column" flex class="parametersList" >
				<md-toolbar class="secondaryToolbar md-knowage-theme" >
					<div class="md-toolbar-tools ">
						<h1>{{translate.load("sbi.meta.business.relationship.target.attributes");}}</h1>
					</div>
				</md-toolbar> 
					<md-list flex>
        				<md-list-item class="secondary-button-padding" id="target-{{$index}}" ondrop="angular.element(document.getElementById('assDirApp')).scope().drop(event)" ondragover="angular.element(document.getElementById('assDirApp')).scope().allowDrop(event)" 
        				  ng-repeat="item in targetModel" ng-click="null" layout="row" >
	        				<span flex=40>{{item[targetName]}}</span>
	        				<span flex ng-if="item[associatedItem].length>0" ><i class="fa fa-link" aria-hidden="true"></i></span>
	        				<span flex=40 ng-if="item[associatedItem].length>0" >{{item[associatedItem][0][sourceName]}}</span>
	        				 <md-button  ng-if="item[associatedItem].length>0" class="md-secondary md-icon-button " ng-click="">
	        				 <md-icon md-font-icon="fa fa-trash"></md-icon>
        				 </md-button>
        				</md-list-item>
			    	</md-list>
			</div>
		</div>
		
		<style>
			.associator-parameter{background-color: #C4DCF3; color: rgba(255,255,255,0.87);font-size:15px;padding:5px 16px 0 16px!important;font-weight:400;min-height:33px!important}
			.associator-parameter.highlight-selected-parameter {background-color: #a9c3db;}
			.associator-parameter.link {color: black; background-color: #E6E6E6;}
			.associator-parameter .fa-link {color: #3F51B5;}
			.loadingSpinner {text-align:center;vertical-align:middle;width:100%;height:100%;}
			.openDocIcon {margin-top: 6px; color: white;}
			.parametersList {padding:10px;}
			.parametersList > div {padding:3px;}
			.parametersList li {border:0;padding: 2px;border:0!important;}
			button.md-raised {margin-top:22px;}
		</style>