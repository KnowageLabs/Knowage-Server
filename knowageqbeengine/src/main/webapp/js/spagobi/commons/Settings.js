/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/




Ext.ns("Sbi.settings");

Sbi.settings.qbe = {
		relationshipswizard:{
			pathSeparator: ' -- ',
			useRelation: false
		},

		queryBuilderPanel: {
			enableTreeToolbar: true,
			enableTreeTbPinBtn: true,
			enableTreeTbUnpinBtn: true,
			enableTreeTbSaveBtn: true,

			enableQueryTbExecuteBtn: true,
			enableQueryTbSaveBtn: false,
			enableQueryTbValidateBtn: false,

			enableCatalogueTbDeleteBtn: true,
			enableCatalogueTbAddBtn: true,
			enableCatalogueTbInsertBtn: true,
			enableViewQueryButton: true,

			queryWidowTabs: ['sql','jpql']
		}

		, selectGridPanel: {
			gridHeight: 250
			, enableTbAddCalculatedBtn: true
			, enableTbHideNonvisibleBtn: true
			, enableTbAddCalculatedBtn: true
			, enableTbDeleteAllBtn: true
			, columns : {
				'entity': {hideable: true, hidden: false, sortable: false}
				, 'field': {hideable: true, hidden: false, sortable: false}
				, 'alias': {hideable: true, hidden: false, sortable: false}
				, 'funct': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'group': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'order': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'visible': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'include': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'filter': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'having': {hideable: true, hidden: false, width: 50, sortable: false}
				, 'delete': {hideable: true, hidden: false, sortable: false}
			}
		}

		, filterGridPanel: {
			gridHeight: 250
			, enableTbExpWizardBtn: true
			, enableTbRemoveAllFilterBtn: true
			, enableTbAddFilterBtn: true
			, enableRowRemoveBtn: true
			, columns : {
				'filterId': {hideable: true, hidden: false, sortable: false, editable: true}
				, 'filterDescripion': {hideable: true, hidden: true, sortable: false, editable: true}
				, 'leftOperandDescription': {hideable: false, hidden: false, sortable: false, editable: true}
				, 'leftOperandType': {hideable: true, hidden: true, sortable: false, editable: true}
				, 'operator': {hideable: false, hidden: false, sortable: false, editable: true}
				, 'rightOperandDescription': {hideable: false, hidden: false, sortable: false, editable: true}
				, 'rightOperandType': {hideable: true, hidden: true, sortable: false, editable: true}
				, 'booleanConnector': {hideable: true, hidden: false, sortable: false, editable: true}
				, 'deleteButton': {hideable: true, hidden: false, sortable: false, editable: true}
				, 'promptable': {hideable: true, hidden: false, sortable: false, editable: true}
			}
			, lookupValuesSeparator: ' ---- '
		}

		, havingGridPanel: {
			gridHeight: 250
		}

		, dataMartStructurePanel: {
			enableTreeContextMenu: true
		}

		, crossTab: {
			 columnWidth: 80
			, rowHeight: 25
			, fontSize: 10
			, percentageFontSize: 9
			, naCellValue: "0"
		}

};

Sbi.settings.formviewer = {
		staticClosedXORFiltersPanel: {
			width: 300
			//, height: 150
		}
		, staticClosedOnOffFiltersPanel: {
			width: 300
			//, height: 150
		}
		, staticOpenFiltersPanel: {
			valueDelimiter: '--!;;;;;!--'
		}
};
