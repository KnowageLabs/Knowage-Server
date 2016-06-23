/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*Ext.ns("Sbi.settings.olap");

Sbi.settings.olap= {
		options: {
			OlapOptions: {
				hidden: true
			}
		},
		tools: {
			OlapViewDefinitionTools: {
				hidden: true
			}
		}
		, whatif : {
			timeout : { // Ajax timeout for special services, such as persist transformation (saving the current version or creating a new one)
				persistTransformations : 1800000,  // 1800000 milliseconds = 30 minutes
				persistNewVersionTransformations : 1800000  // 1800000 milliseconds = 30 minutes
			}
		},
		toolbar:{
			OlapToolbar: {
				hideSaveAsWindow: false
			}
		}


};*/
var olapSet = angular.module('olap.settings',[]);

olapSet.service('olapSharedSettings',function(){
		var settings = {
				minSearchLength:4,
				filterDialogWidth:'30%',
				filterDialogHeight:'80%',
				disableManualEditingCC: false
		};
		
		return {
			getSettings:function(){
				return settings;
			}
		};
	});