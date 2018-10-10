describe('workspace - organizer', function() {

	afterEach(function() {
		browser.sleep(50);
	});

  it('should try to login', function() {
	browser.ignoreSynchronization=true;
   browser.get('/knowage');
   browser.driver.findElement(by.id('userID')).sendKeys('biuser');
   browser.driver.findElement(by.id('password')).sendKeys('biuser');
   browser.driver.findElement(by.css('.btn-signin')).click();
  });

 it('should click on hamburger menu', function() {

   element(by.css('.menuKnowage')).click();
   browser.sleep(1000);
  });

 it('should open Workspace',function(){
    element(by.css('[title="My workspace"]')).click();
    browser.waitForAngular();
 });

  it('should open documents',function(){
    var driver = browser.driver;
    var loc = by.tagName('iframe');
    var el = driver.findElement(loc);
    browser.switchTo().frame(el);
    element(by.id('Documents')).click();
    browser.sleep(2000);
 });

 it('should open new form for creating folders',function(){
	 element(by.id('addNewFolder')).click();
	 browser.sleep(2000);
});

	it('should fill form for creating folders',function(){
		var d = new Date();
		element(by.model("folder.code")).sendKeys("code-example-test"+d.getTime());
		browser.sleep(1000);
		element(by.model("folder.name")).sendKeys("name-example-test"+d.getTime());
		browser.sleep(1000);
		element(by.model("folder.descr")).sendKeys("descr-example-test"+d.getTime());
		browser.sleep(1000);
	});

	it('should close dialog for creating a new document',function(){
		element(by.id('saveNewFolderBtn')).click();
		browser.sleep(3000);
	});

});
