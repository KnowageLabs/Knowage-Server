<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<md-dialog class="kn-newsDialog">
	<md-dialog-content layout="column" style="">
		<md-toolbar>
      		<div class="md-toolbar-tools">
        		<h2 flex>{{::translate.load('sbi.home.news')}}</h2>
			</div>
		</md-toolbar>
		<div ng-show="news.length == 0" class="noNews">
			<div class="emptyIconSvg">
			</div>
			<div class="emptyIconText">
				{{::translate.load('sbi.home.news.nonews')}}
			</div>
		</div>
		 <!--  md-tabs ng-if="news.length > 0">
	      <md-tab label="{{category.label}}" ng-repeat="category in news" -->
	        <md-content ng-if="news.length > 0">
	         <md-list flex class="noPadding">
        			<md-list-item class="md-2-line" ng-class="{'newMessage': news.unread}" ng-repeat-start="n in news | orderBy: 'time'" ng-click="openDetail(n, $index)">
          				<div class="md-avatar fa fa-2x fa-envelope" ></div>
			          	<div class="md-list-item-text" layout="column">
			            	<h3>{{ n.title }}</h3>
			            	<p>{{ n.description }}</p>
			          	</div>
			          	<md-divider ></md-divider>
			        </md-list-item>
			        <div class="newsContainer" ng-repeat-end ng-if="n.opened" ng-bind-html="n.html | trustAsHtml">
			        	
			        </div>
        		</md-list>
	        </md-content>
      	<!--  /md-tab>
      </md-tabs-->
      
	</md-dialog-content>
	<md-dialog-actions>
		<md-button class="md-raised md-primary" ng-click="closeDialog()" >
			{{::translate.load('sbi.generic.close')}}
        </md-button>
	</md-dialog-actions>
</md-dialog>
