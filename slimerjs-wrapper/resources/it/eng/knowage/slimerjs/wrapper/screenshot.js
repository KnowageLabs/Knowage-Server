var url = "http://localhost:8080/knowagecockpitengine/api/1.0/pages/execute?EXPORT_MODE=true&user_id=biadmin&SPAGOBI_AUDIT_ID=28&DOCUMENT_LABEL=TestAssociative4Datasets&DOCUMENT_COMMUNITIES=%5B%5D&knowage_sys_country=US&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=admin&SBICONTEXT=%2Fknowage&knowage_sys_language=en&DOCUMENT_FUNCTIONALITIES=%5B3%5D&SBI_COUNTRY=US&DOCUMENT_AUTHOR=biadmin&DOCUMENT_DESCRIPTION=&document=3&IS_TECHNICAL_USER=true&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&SBI_LANGUAGE=en&DOCUMENT_NAME=TestAssociative4Datasets&NEW_SESSION=TRUE&DOCUMENT_IS_PUBLIC=false&DOCUMENT_VERSION=7&SBI_HOST=http%3A%2F%2Flocalhost%3A8080&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=d5e92863395511e7b26e6db3022acd33&EDIT_MODE=null&timereloadurl=1494842648408"
var page = require('webpage').create();
page.viewportSize = { width: 1600, height: 1200 };
page.settings.userName = 'biadmin';
page.settings.password = 'biadmin';

page.open(url, function() {
	just_wait();
});

function just_wait() {
    setTimeout(function() {
            page.render('C:\\temp\\screenshot.png');
            slimer.exit();
    }, 60000);
}