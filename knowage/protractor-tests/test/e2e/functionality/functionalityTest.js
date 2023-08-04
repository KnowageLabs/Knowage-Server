describe('functionality-test', function() {

	afterEach(function() {
		browser.sleep(2000);
	});

	it('should try to login', function() {
	
	browser.ignoreSynchronization=true;
	 browser.get('https://spagobitest:48081/knowage');
		browser.sleep(10000);
	 browser.driver.findElement(by.id('password')).sendKeys('test_functionality_admin');
	 browser.driver.findElement(by.id('username')).sendKeys('test_functionality_admin');
	 browser.driver.findElement(by.css('.btn-signin')).click();
	});

	it('should try to open functionality catalogue', function() {
		browser.ignoreSynchronization=true;
		 browser.get('https://spagobitest:48081/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/catalogue/functionalitiesManagement.jsp?LIGHT_NAVIGATOR_RESET_INSERT=TRUE');
		 
		});
	

	

	it('checks that getting functionalities work',function(){
	
		
		expect(element.all(by.repeater('folder in folders')).count()).toBeGreaterThan(0);
	});
	it('open details for one folder',function(){
		
	element(by.cssContainingText('.ng-binding', 'Funct1-1')).click();
	

	});
	it('should click + button',function(){
		element(by.css('[class="md-fab md-fab-top-right md-button ng-scope md-knowage-theme md-ink-ripple"]')).click();
		
		
	});
	 
	it('should add  details for folder',function(){

		element(by.model("selectedFolder.code")).clear().sendKeys("new test1");

		element(by.model("selectedFolder.name")).clear().sendKeys("test name");

		element(by.model("selectedFolder.description")).clear().sendKeys("test descr");

		element.all(by.repeater('menuItem in speedMenuOption')).get(0).click();
	});
	
	it('should close dialog for updating a folder',function(){
		 
		element(by.css('[ng-click="saveFuncName()"]')).click();
		
		
	});
	
	
	/*it('open details for one folder',function(){
		
		element(by.cssContainingText('.ng-binding', 'test name')).click();
		browser.sleep(5000);

		});
		
	it('should add  details for folder',function(){
  
		element(by.model("selectedFolder.description")).clear().sendKeys("test update descr");
		browser.sleep(1000);
		 
	});
	it('should close dialog for updating a folder',function(){
		 
		element(by.css('[ng-click="saveFuncName()"]')).click();
		browser.sleep(5000);
		
		
		
	});
	
	it('delete folder',function(){
		element(by.cssContainingText('.ng-binding', 'Funct1-1')).element(by.xpath("../../..")).click();
	
		 browser.sleep(5000);
	});*/
});
