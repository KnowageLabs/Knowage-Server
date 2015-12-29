Ext.ns("Sbi.settings.olap");

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


};