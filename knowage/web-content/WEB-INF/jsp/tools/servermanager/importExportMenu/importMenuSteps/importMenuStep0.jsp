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


<div layout="row" layout-wrap>
	<file-upload flex id="fileUploadImport" ng-model="IEDConf.fileImport"></file-upload>
	<md-button class="md-fab md-fab-mini"
			ng-disabled="isInvalidImportStep0Form();"
			aria-label="{{translate.load('SBISet.import','component_impexp_messages')}} {{translate.load('sbi.ds.wizard.file')}}"
			ng-click="importFile()"> 
		<md-icon class="fa fa-upload center-ico"></md-icon>
	</md-button>
</div>
<div layout-padding class="associations-container">
	<md-toolbar class="miniheadassociations">
		<div class="md-toolbar-tools" style="margin-top: 5px;">
			{{translate.load('impexp.Associations','component_impexp_messages')}}
		</div>
	</md-toolbar>

	<md-radio-group ng-model="IEDConf.associations"> 
		<md-radio-button value="noAssociations " class="md-primary">
			{{translate.load('impexp.withoutAss','component_impexp_messages')}}
		</md-radio-button>
		<md-radio-button value="mandatoryAssociations">
			{{translate.load('impexp.mandatoryAss','component_impexp_messages')}}
		</md-radio-button>
		<md-radio-button value="defaultAssociations">
			{{translate.load('impexp.defaultAss','component_impexp_messages')}}
		</md-radio-button>
	</md-radio-group>
	<div layout-padding layout="column" layout-wrap
		ng-if="IEDConf.associations != 'noAssociations' ">
		<div layout-xs="column" layout-align-xs="center stretch" layout="row" layout-align="start center">
			<md-input-container flex class="md-block">
				<label>{{translate.load('impexp.savedAss','component_impexp_messages')}}</label>
				<input type="text" ng-model="IEDConf.fileAssociation.name" ng-disabled="true"
						aria-label="{{translate.load('impexp.savedAss','component_impexp_messages')}}">
			</md-input-container>

			<md-button class="md-fab md-fab-mini" ng-click="listAssociation()"
					aria-label="{{translate.load('impexp.listAssFile','component_impexp_messages')}}">
				<md-icon class="fa fa-search center-ico"></md-icon>
			</md-button>
		</div>
		<div layout="row">
			<label>{{translate.load('impexp.savedAss','component_impexp_messages')}}</label>
			<file-upload flex id="AssociationFileUploadImport"
					ng-model="IEDConf.associationsFileImport"></file-upload>
		</div>
	</div>
</div>
