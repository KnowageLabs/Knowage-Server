var width = system.args[ 1 ],
  height = system.args[ 2 ],
  viewportWidth = system.args[ 3 ],
  viewportHeight = system.args[ 4 ],
  marginTop = system.args[ 5 ],
  marginRight = system.args[ 6 ],
  marginBottom = system.args[ 7 ],
  marginLeft = system.args[ 8 ],
  headerHeight = system.args[ 9 ],
  headerFunctionFile = system.args[ 10 ],
  footerHeight = system.args[ 11 ],
  footerFunctionFile = system.args[ 12 ],
  operatingSystem = system.args[ 13 ],
  sourceUrl = system.args[ 14 ],
  targetPath = system.args[ 15 ],
  jsWait = +system.args[ 16 ],
  jsInterval = +system.args[ 17 ],
  renderId = system.args[ 18 ],
  sheets = +system.args[ 19 ];


// this function writes the arguments to stdout
var log = function () {
  Array.prototype.slice.call(arguments).map(system.stdout.writeLine);
};

// this function writes the arguments to the stderr
var err = function () {
  Array.prototype.slice.call(arguments).map(system.stderr.writeLine);
};

// this function checks to see if javascript is finished executing on the page
var done = function (page) {
  return page.evaluate(function () {
    var PageRendered = window.PageRendered;
    return (typeof PageRendered === 'undefined') ||
      (typeof PageRendered === 'boolean' && PageRendered === true) ||
      (typeof PageRendered === 'function' && PageRendered());
  });
};

// this sets a zoom on the page because of the dpi differences between windows and unix
var setZoom = function (page) {
  if (operatingSystem !== "WINDOWS") {
    try {
      log('Setting zoom on HTML to 0.75');
      page.evaluate(function () {
        document.body.style.zoom = 0.75;
      });
    } catch (error) {
      err('Failed to set zoom on HTML file: ', error);
      slimer.exit(2);
    }
  }
};

// this function renders the page
var renderPage = function (page) {
  setZoom(page);

  // render the page
  try {
    log('Rendering PNG to target folder: ' + targetFolder);
    page.render(targetFolder"/"+page.renderId+".png");
  } catch (error) {
    err('Failed to render PNG: ' + error);
    slimer.exit(3);
  }

  slimer.exit(0);
};

var waited = 0;
var renderIfDone = function renderIfDone(page) {
  if (done(page)) {
    renderPage(page);
  } else {
    if (waited > jsWait) {
      err('Timed out on JavaScript execution');
      slimer.exit(6);
    }
    log('Waiting an additional ' + jsInterval + 'ms');
    waited += jsInterval;
    setTimeout(renderIfDone, jsInterval);
  }
};

applySettingOnPage(page, renderId) {	
	page.renderId = renderId;
	page.viewportSize = { width: viewportWidth, height: viewportHeight };	
	// when finishing loading the resources
	// start the timer for js execution to complete
	page.onLoadFinished = renderIfDone;
}

var urls = new Map();
for(int i=0; i<sheets; i++) {
	urlMap.set(renderId+"_"+i, sourceUrl+"&sheet="+i);
}

var queue = [];
urls.forEach(function(renderId, url) {
    var p = new Promise(function(resolve, reject) {
        var page = require('webpage').create();
        applySettingOnPage(page, renderId);
        page.open(url)
            .then(function(status) {
                if (status == "success") {
                    var title = page.title;
                    log("Page title of " + url + " : " + title);
                    renderIfDone(page);
                } else {
                    log("Sorry, the page is not loaded for " + url);
                    reject(new Error("Some problem occurred with " + url));
                }
            });
    });
    queue.push(p);
}, urls);

Promise.all(queue).then(function(values) {
    log(values);
    slimer.exit();
});