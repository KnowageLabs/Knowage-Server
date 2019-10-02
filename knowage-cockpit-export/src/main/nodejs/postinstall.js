const exec = require('child_process').exec;
const puppeteer = require('puppeteer');

// Get Chromium version from puppeteer
const puppeteerPkg = require('puppeteer/package.json');
const revision = puppeteerPkg.puppeteer.chromium_revision;

// Get proxy configuration from NPM
exec('npm config get http-proxy', function(err, stdout, stderr) {
	process.env.HTTPS_PROXY = stdout;
});
exec('npm config get https-proxy', function(err, stdout, stderr) {
	process.env.HTTP_PROXY = stdout;
});

[ "linux", "win64" ].forEach(function(curr) {
  console.log("Download Chromium for " + curr + " architecture");

  const browserFetcher = puppeteer.createBrowserFetcher({ platform: curr });

  const revisionInfo = browserFetcher.revisionInfo(revision);

  browserFetcher.download(revisionInfo.revision)
      .then(() => browserFetcher.localRevisions())
      .then(onSuccess)
      .catch(onError);

  function onSuccess(localRevisions) {
    console.log('Chromium downloaded to ' + revisionInfo.folderPath);
    localRevisions = localRevisions.filter(revision => revision !== revisionInfo.revision);
    // Remove previous chromium revisions.
    const cleanupOldVersions = localRevisions.map(revision => browserFetcher.remove(revision));
    return Promise.all([...cleanupOldVersions]);
  }

  /**
   * @param {!Error} error
   */
  function onError(error) {
    console.error(`ERROR: Failed to download Chromium r${revision}! Set "PUPPETEER_SKIP_CHROMIUM_DOWNLOAD" env variable to skip download.`);
    console.error(error);
    process.exit(1);
  }

});
