/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
 Ext.ns("Sbi.formbuilder.template");

Sbi.formbuilder.template = {
	 "staticClosedFilters": [
	    {
	    	"title": "Sesso",
		    "singleSelection": true,
		    "allowNoSelection": true,
		    "noSelectionText": "Tutti",
		    "filters": [
		       {
		    	   "text": "Maschio",
		           "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):gender",
		           "operator": "EQUALS TO",
		           "rightOperandValue": "M"
		       },
		       {
		    	   "text": "Femmina",
		           "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):gender",
		           "operator": "EQUALS TO",
		           "rightOperandValue": "F"
		       }
		    ]
        },		                         
		{
        	"title": "Stato civile",
		    "singleSelection": true,
		    "allowNoSelection": false,
		    "filters": [
			    {
			    	"text": "Sposato",
			        "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):maritalStatus",
			        "operator": "EQUALS TO",
			        "rightOperandValue": "M"
			    },
			    {
			    	"text": "Single",
			        "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):maritalStatus",
			        "operator": "EQUALS TO",
			        "rightOperandValue": "S"
			    }
		    ]
		},		                         
		{
			"title": "Altri filtri",
		    "singleSelection": false,
		    "filters": [
		        {
		        	"text": "Ecludi quelli che non hanno figli a carico",
		            "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):numChildrenAtHome",
		            "operator": "NOT EQUALS TO",
		            "rightOperandValue": 0
		        },
		        {
		        	"text": "Ecludi quelli che non sono proprietari di una casa",
		            "leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):houseowner",
		            "operator": "EQUALS TO",
		            "rightOperandValue": "Y"
		        },
		        {
		        	"text": "Escludi gli under 40 e gli over 60",
		            "expression": [
			            {
			            	"leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):birthdate",
			                "operator": "GREATER THAN",
			                "rightOperandValue": "\'1950-01-01\'"
			            },
			            "AND",
			            {
			            	"leftOperandValue": "it.eng.spagobi.SalesFact1998::customer(customer_id):birthdate",
			                "operator": "LESS THAN",
			                "rightOperandValue": "\'1970-01-01\'"
			            }
		            ]
		          }
		    ]
	   }
	] 
	
    , "staticOpenFilters": [

        {
            "text": "Tipo carta",
            "field": "it.eng.spagobi.SalesFact1998::customer(customer_id):memberCard",
            "operator": "EQUALS TO",
            "singleSelection": false,
            "maxSelectedNumber": 3
        },

        {
            "text": "Qualifica",
            "field": "it.eng.spagobi.SalesFact1998::customer(customer_id):education",
            "operator": "EQUALS TO",
            "singleSelection": true
        },

        {
            "text": "Reddito annuo",
            "field": "it.eng.spagobi.SalesFact1998::customer(customer_id):yearlyIncome",
            "operator": "IN",
            "singleSelection": false,
            "maxSelectedNumber": 3
        },
        
        {
            "text": "Nati dopo il",
            "field": "it.eng.spagobi.SalesFact1998::customer(customer_id):birthdate",
            "operator": "GREATER THAN",
            "singleSelection": true
        }
        
    ]
    
    , "dynamicFilters": [
         {
             "operator": "EQUALS TO",
             "admissibleFields": [
                {"field": "it.eng.spagobi.SalesFact1998::customer(customer_id):numCarsOwned", "text": "Numero autovetture"}             
             ]
         },
         {
             "operator": "BETWEEN",
             "admissibleFields": [
                {"field": "it.eng.spagobi.SalesFact1998::customer(customer_id):birthdate", "text": "Data di nascita"}             
             ]
         },
         {
             "operator": "BETWEEN",
             "admissibleFields": [
                {"field": "it.eng.spagobi.SalesFact1998::customer(customer_id):lname", "text": "Cognome"}             
             ]
         }
     ]
    
    , "groupingVariables": [
        {
        	"admissibleFields": [
        	      {"field": "it.eng.spagobi.SalesFact1998::customer(customer_id):city", "text": "Citta"}
            ]
        }, {
        	"admissibleFields": [
                  {"field": "it.eng.spagobi.SalesFact1998::customer(customer_id):stateProvince", "text": "Provincia"}
            ]
        }
    ]
	
	
}