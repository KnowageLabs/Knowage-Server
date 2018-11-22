/*
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
 */

angular.module('cockpitModule').factory('cockpitModule_generalOptions',function(sbiModule_translate){
	return{
		//Deprecated dimensioning to let the user choose between px,rem and %
		fontSizeDimension: [
			{label:'',value:''},
			{label:'8px',value:'8px'},
			{label:'10px',value:'10px'},
			{label:'12px',value:'12px'},
			{label:'14px',value:'14px'},
			{label:'16px',value:'16px'},
			{label:'20px',value:'20px'},
			{label:'24px',value:'24px'}
		],
		//
		textDecoration:	[
			{label:'',value:''},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontDecoration.overline"),value:'overline'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontDecoration.underline"),value:'underline'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontDecoration.lineThrough"),value:'line-through'}
		],
		fontWeight:	[
			{label:'',value:''},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontWeight.regular"),value:'regular'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontWeight.bold"),value:'bold'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontWeight.light"),value:'light'}
		],
		fontStyle: [
			{label:'',value:''},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontStyle.normal"),value:'normal'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontStyle.italic"),value:'italic'},
			{label:sbiModule_translate.load("sbi.cockpit.style.fontStyle.oblique"),value:'oblique'},
		],
		fontFamily: [
			{label:'',value:''},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.roboto"), value:'roboto'},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.arial"),value:'arial'},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.curierNew"),value:'courier-new'},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.tahoma"),value:'tahoma'},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.timesNewRoman"),value:'times-new-roman'},
	        {label:sbiModule_translate.load("sbi.cockpit.style.font.verdana"),value:'verdana'}
	     ],
	     aggregationFunctions:[
	    	 {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.none"),value:"NONE"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.sum"),value:"SUM"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.avg"),value:"AVG"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.max"),value:"MAX"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.min"),value:"MIN"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.count"),value:"COUNT"},
             {label:sbiModule_translate.load("sbi.qbe.selectgridpanel.aggfunc.name.countdistinct"),value:"COUNT_DISTINCT"}
         ],
         textAlign:	[
			{label:'',value:''},
			{label:sbiModule_translate.load("sbi.cockpit.style.textAlign.left"),value:'left'},
			{label:sbiModule_translate.load("sbi.cockpit.style.textAlign.right"),value:'right'},
			{label:sbiModule_translate.load("sbi.cockpit.style.textAlign.center"),value:'center'}
		 ],
		 textVerticalAlign:	[
			 {label:'',value:''},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textVerticalAlign.top"),value:'top'},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textVerticalAlign.middle"),value:'middle'},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textVerticalAlign.bottom"),value:'bottom'}
		 ],
		 flexJustifyContent:	[
			 {label:'',value:''},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textAlign.left"),value:'flex-start'},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textAlign.center"),value:'center'},
			 {label:sbiModule_translate.load("sbi.cockpit.style.textAlign.right"),value:'flex-end'}
		 ],
		 fieldsTypes:	[
			 {label:'string',value:'java.lang.String'},
			 {label:'integer',value:'java.lang.Integer'},
			 {label:'integer',value:'java.lang.Long'},
			 {label:'integer',value:'java.math.BigInteger'},
			 {label:'float',value:'java.math.BigDecimal'},
			 {label:'float',value:'java.lang.Double'},
			 {label:'float',value:'java.lang.Float'},
			 {label:'date',value:'java.sql.Date'},
			 {label:'date',value:'java.util.Date'},
			 {label:'time',value:'java.sql.Time'},
			 {label:'timestamp',value:'java.sql.Timestamp'},
			 {label:'timestamp',value:'oracle.sql.TIMESTAMP'},
			 {label:'blob',value:'oracle.sql.BLOB'},
			 {label:'clob',value:'oracle.sql.CLOB'}
		 ]
	}
});