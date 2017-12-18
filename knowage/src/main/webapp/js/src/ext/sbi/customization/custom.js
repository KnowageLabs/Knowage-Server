	/**
	 
	Ext.override(Sbi.execution.ParametersPanel, {
		
		getHelpMessageForPage2: function(executionInstance, thereAreParametersToBeFilled) {
			var toReturn = null;
			var doc = executionInstance.document;
			if (doc.typeCode == 'DATAMART' && this.baseConfig.subobject == undefined) {
				if (Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality')) {
					if (!thereAreParametersToBeFilled) {
						toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.powerUserMessageWithoutParameters');
					} else {
						toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.powerUserMessageWithParameters');
					}
				} else {
					if (!thereAreParametersToBeFilled) {
						toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.readOnlyUserMessageWithoutParameters');
					} else {
						toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.readOnlyUserMessageWithParameters');
					}
				}
			} else {
				if (!thereAreParametersToBeFilled) {
					toReturn = LN('sbi.execution.parametersselection.message.page2.execute');
				} else {
					var day=false;
					for(p in this.fields) {
						if(this.fields[p].name=='DAY'){
							day = true;
							break;
						}
					}
					if(day){
						toReturn = LN('sbi.execution.parametersselection.message.page2.fillFormAndExecute')+ LN('sbi.execution.parametersselection.message.page2.fillFormAndExecute.additionalinformation1');
					}else{
						toReturn = LN('sbi.execution.parametersselection.message.page2.fillFormAndExecute')+ LN('sbi.execution.parametersselection.message.page2.fillFormAndExecute.additionalinformation2');
					}
					
				}
			}
			return toReturn;
		}
	 
	});*/