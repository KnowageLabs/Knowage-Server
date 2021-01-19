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
angular.module('cockpitModule').factory('cockpitModule_highchartsLocales',function(sbiModule_translate){
	return{	
    	lang: {
        	months: [
            sbiModule_translate.load('kn.date.january'),
            sbiModule_translate.load('kn.date.february'), 
            sbiModule_translate.load('kn.date.march'), 
            sbiModule_translate.load('kn.date.april'),
            sbiModule_translate.load('kn.date.may'), 
            sbiModule_translate.load('kn.date.june'), 
            sbiModule_translate.load('kn.date.july'), 
            sbiModule_translate.load('kn.date.august'),
            sbiModule_translate.load('kn.date.september'), 
            sbiModule_translate.load('kn.date.october'), 
            sbiModule_translate.load('kn.date.november'), 
            sbiModule_translate.load('kn.date.december')
       				 ],
			shortMonths: [
            sbiModule_translate.load('kn.date.short.january'),
            sbiModule_translate.load('kn.date.short.february'), 
            sbiModule_translate.load('kn.date.short.march'), 
            sbiModule_translate.load('kn.date.short.april'),
            sbiModule_translate.load('kn.date.short.may'), 
            sbiModule_translate.load('kn.date.short.june'), 
            sbiModule_translate.load('kn.date.short.july'), 
            sbiModule_translate.load('kn.date.short.august'),
            sbiModule_translate.load('kn.date.short.september'), 
            sbiModule_translate.load('kn.date.short.october'), 
            sbiModule_translate.load('kn.date.short.november'), 
            sbiModule_translate.load('kn.date.short.december')
       				 ],
        	weekdays: [
            sbiModule_translate.load('kn.date.sunday'),
            sbiModule_translate.load('kn.date.monday'), 
            sbiModule_translate.load('kn.date.tuesday'), 
            sbiModule_translate.load('kn.date.wednesday'),
            sbiModule_translate.load('kn.date.thursday'), 
            sbiModule_translate.load('kn.date.friday'), 
            sbiModule_translate.load('kn.date.saturday')
      				  ]
   		 	}
		
	}
})