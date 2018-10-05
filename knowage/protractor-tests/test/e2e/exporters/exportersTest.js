describe('Exporters', function() {
	
	afterEach(function() {
		browser.sleep(500);
	});
	
	it('should try to login', function() {
		browser.ignoreSyncronization = true;
		browser.waitForAngularEnabled(false);
		browser.get('/knowage');
		
		browser.sleep(500);
		browser.driver.findElement(by.id('userID')).sendKeys('biadmin');
		browser.driver.findElement(by.id('password')).sendKeys('biadmin');
		browser.driver.findElement(by.css('.btn-signin')).click();
	});
	
	it('should click on .menuKnowage and open menu', function() {
	 element(by.css('.menuKnowage')).click();
	 element(by.id('contieniLinkAdmin')).getAttribute('class').then(function(attr) {
	 expect(attr).toContain('open');
	 });
	 });
	
	 it('should open EXPORTERS MENU', function() {
		element(by.linkText('Exporters menu')).click();
	 });	 

	 it('should open div for adding new exporters and add new exporter', function() {
		 var driver = browser.driver;
		 var loc = by.tagName('iframe');
		 var el = driver.findElement(loc);
		 browser.switchTo().frame(el);
		 var oldCount;
		 element.all(by.repeater('exporter in myExporters')).count().then(function(count) {
			 oldCount = count;
		 });
		 
		 element(by.css('[class="md-fab md-fab-top-right md-button ng-scope md-knowage-theme md-ink-ripple"]')).click();
		 element( by.model('selectedExporter.engineId')).click();
		 browser.sleep(1000);		  
			element.all(by.repeater('engine in engines')).get(0).click();
		 browser.sleep(1000);		 
		 element( by.model('selectedExporter.domainId')).click();
		 browser.sleep(1000);		 
			element.all(by.repeater('domain in domains')).get(0).click();
		 browser.sleep(1000);
		 
		 expect( element(by.css('[ng-click="saveFuncName()"]')).isPresent()).toBeTruthy(); 
		 driver.findElement(by.css('[ng-click="saveFuncName()"]')).click();
		 browser.sleep(1000);
		 
		 element.all(by.repeater('exporter in myExporters')).count().then(function(count) {
			 expect(count).toBeGreaterThan(oldCount);
		 });
		 
		 browser.switchTo().defaultContent();
		 browser.waitForAngular();
	 }); 
	 
	 it('should search for exporter', function() {
		 var driver = browser.driver;
		 var loc = by.tagName('iframe');
		 var el = driver.findElement(loc);
		 browser.switchTo().frame(el);
		 
		 browser.sleep(500);
		 expect( element(by.model("searchVal")).isPresent()).toBeTruthy();
		 element(by.css('[ng-keyup="searchItem(searchVal)"]')).click();
		 element(by.model("searchVal")).click();
		 browser.sleep(500);
		 element(by.model("searchVal")).sendKeys("PDF");
		 browser.sleep(500);
		 browser.sleep(1000);
		 element.all(by.repeater('row in ngModel')).getText().then(function(text) {
		   expect(text[0]).toContain('PDF');
		 });
		 browser.sleep(1500);
     });
	
	 it('should delete new added exporter', function() {
		 var oldCount;
		 element.all(by.repeater('exporter in myExporters')).count().then(function(count) {
			 oldCount = count;
		 });
		
		 var exporter = element(by.cssContainingText('[class="animate-repeat-tablerow ng-scope"]', 'PDF'));
		
		 exporter.element(by.css('[aria-label="Delete"]')).click();
		 browser.sleep(700);
		
		 element(by.css('[ng-click="dialog.hide()"]')).click();
		 browser.sleep(2000);
		
		 element.all(by.repeater('exporter in myExporters')).count().then(function(count) {
			 expect(count).toBeLessThan(oldCount);
		 });
		
		 browser.waitForAngular();	
	 });	
	 
	 it('checks that getting exporters work', function(){			
			element.all(by.repeater('exporter in myExporters')).count().then(function(count) {
				expect(count).toBeGreaterThan(0);
			});	 
	  });	
});