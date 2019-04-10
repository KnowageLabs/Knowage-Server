/* Knowage, Open Source Business Intelligence suite
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
(function () {
angular.module('sbiModule')
.factory('knModule_selections',
	function(sbiModule_translate) {
		return {
			"font-size-old":[
				{"label":sbiModule_translate.load("kn.options.fontsize.xsmall"),"value":"8px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.small"),"value":"10px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.medium"),"value":"14px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.extended"),"value":"16px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.large"),"value":"18px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.xlarge"),"value":"24px"},
				{"label":sbiModule_translate.load("kn.options.fontsize.xxlarge"),"value":"40px"}],
			"font-size":[
				{"label":sbiModule_translate.load("kn.options.fontsize.xsmall"),"value":".6rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.small"),"value":".8rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.medium"),"value":"1rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.extended"),"value":"1.1rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.large"),"value":"1.3rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.xlarge"),"value":"1.8rem"},
				{"label":sbiModule_translate.load("kn.options.fontsize.xxlarge"),"value":"3rem"}],
			"text-decoration":[
				{"label":sbiModule_translate.load("kn.options.textdecoration.overline"),"value":"overline"},
				{"label":sbiModule_translate.load("kn.options.textdecoration.underline"),"value":"underline"},
				{"label":sbiModule_translate.load("kn.options.textdecoration.linethrough"),"value":"line-through"}],
			"font-weight":[
				{"label":sbiModule_translate.load("kn.options.fontweight.normal"),"value":"normal"},
				{"label":sbiModule_translate.load("kn.options.fontweight.bold"),"value":"bold"},
				{"label":sbiModule_translate.load("kn.options.fontweight.light"),"value":"lighter"}],
			"font-family":[
				{"label":sbiModule_translate.load("kn.options.fontfamily.roboto"),"value":"roboto"},
				{"label":sbiModule_translate.load("kn.options.fontfamily.arial"),"value":"arial"},
				{"label":sbiModule_translate.load("kn.options.fontfamily.couriernew"),"value":"courier-new"},
				{"label":sbiModule_translate.load("kn.options.fontfamily.tahoma"),"value":"tahoma"},
				{"label":sbiModule_translate.load("kn.options.fontfamily.timesnewroman"),"value":"times-new-roman"},
				{"label":sbiModule_translate.load("kn.options.fontfamily.verdana"),"value":"verdana"}],
			"aggregation-function":[
				{"label":sbiModule_translate.load("kn.options.aggregation.none"),"value":"NONE"},
				{"label":sbiModule_translate.load("kn.options.aggregation.sum"),"value":"SUM"},
				{"label":sbiModule_translate.load("kn.options.aggregation.avg"),"value":"AVG"},
				{"label":sbiModule_translate.load("kn.options.aggregation.max"),"value":"MAX"},
				{"label":sbiModule_translate.load("kn.options.aggregation.min"),"value":"MIN"},
				{"label":sbiModule_translate.load("kn.options.aggregation.count"),"value":"COUNT"},
				{"label":sbiModule_translate.load("kn.options.aggregation.countdistinct"),"value":"COUNT_DISTINCT"}],
			"text-align":[
				{"label":sbiModule_translate.load("kn.options.textalign.left"),"value":"left"},
				{"label":sbiModule_translate.load("kn.options.textalign.right"),"value":"right"},
				{"label":sbiModule_translate.load("kn.options.textalign.center"),"value":"center"}],
			"vertical-align":[
				{"label":sbiModule_translate.load("kn.options.verticalalign.top"),"value":"top"},
				{"label":sbiModule_translate.load("kn.options.verticalalign.middle"),"value":"middle"},
				{"label":sbiModule_translate.load("kn.options.verticalalign.bottom"),"value":"bottom"}]}
	});
})();