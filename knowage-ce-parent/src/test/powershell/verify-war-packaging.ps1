param(
	[string] $KnowageWar,
	[string] $CockpitWar,
	[string] $CockpitSourceMaps,
	[string[]] $AdditionalWar
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Test-WarRuntimePayload {
	param([string] $File)

	$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $File).Path)
	try {
		$names = @($archive.Entries | ForEach-Object { $_.FullName })
		$unwanted = @($names | Where-Object {
			$_ -match '\.(js|css)\.map$' -or
			$_ -match '^WEB-INF/lib/(junit[^/]*|mockito-[^/]*|opentest4j-[^/]*|apiguardian-[^/]*|knowage[^/]*-tests)\.jar$' -or
			$_ -match '^WEB-INF/lib/tomcat-embed-[^/]*\.jar$'
		})
		if ($unwanted.Count) {
			throw "Unexpected runtime payload in ${File}: $($unwanted -join ', ')"
		}

		if ($archive.GetEntry('WEB-INF/classes/it/eng/spagobi/engines/talend/TalendEngine.class')) {
			$unwanted = @($names | Where-Object {
				$_ -match '^WEB-INF/lib/(hazelcast|groovy|mongo-java-driver|pdfbox|fontbox|poi|xmlbeans|SparseBitSet|curvesapi|jena)-[^/]*\.jar$'
			})
			if ($unwanted.Count) {
				throw "Unused dataset libraries in ${File}: $($unwanted -join ', ')"
			}
			foreach ($artifact in @('knowage-utils', 'spago-core', 'dom4j', 'jaxws-rt', 'commonj', 'cas-client-core')) {
				if (!@($names | Where-Object { $_ -match ('^WEB-INF/lib/' + [regex]::Escape($artifact) + '-[0-9][^/]*\.jar$') }).Count) {
					throw "Missing runtime dependency ${artifact} in $File"
				}
			}
		}
		Write-Output "PASS: runtime payload $File"
	} finally { $archive.Dispose() }
}

function Test-KnowageWar {
	param([string] $File, [bool] $Cockpit)

	$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $File).Path)
	try {
		$names = @($archive.Entries | ForEach-Object { $_.FullName })
		$unwanted = @($names | Where-Object {
			$_ -match '\.(js|css)\.map$' -or
			$_ -match '^WEB-INF/lib/(jetty-|http2-|netty-tcnative-)' -or
			($Cockpit -and $_ -match '^js/lib/openlayers/(openlayers|3\.x\.x|4\.6\.4)/')
		})
		if ($unwanted.Count) {
			throw "Unexpected runtime payload in ${File}: $($unwanted -join ', ')"
		}
		foreach ($artifact in @('knowage-dataset-sparql', 'jena-arq', 'solr-solrj', 'solr-solrj-streaming', 'solr-solrj-zookeeper')) {
			if (!@($names | Where-Object { $_ -match ('^WEB-INF/lib/' + [regex]::Escape($artifact) + '-[0-9][^/]*\.jar$') }).Count) {
				throw "Missing runtime provider ${artifact} in $File"
			}
		}
		$classes = @(
			'it/eng/spagobi/tools/dataset/bo/SPARQLDataSet.class',
			'it/eng/spagobi/tools/dataset/common/dataproxy/SPARQLDataProxy.class',
			'it/eng/spagobi/tools/dataset/common/datareader/SPARQLDataReader.class'
		)
		$locations = @{}
		foreach ($class in $classes) { $locations[$class] = @() }
		foreach ($entry in $archive.Entries | Where-Object { $_.FullName -match '^WEB-INF/lib/knowage-.*\.jar$' }) {
			$memory = [System.IO.MemoryStream]::new()
			$stream = $entry.Open()
			try {
				$stream.CopyTo($memory)
				$memory.Position = 0
				$jar = [System.IO.Compression.ZipArchive]::new($memory, [System.IO.Compression.ZipArchiveMode]::Read, $true)
				try {
					foreach ($class in $classes) {
						if ($jar.GetEntry($class)) { $locations[$class] += $entry.FullName }
					}
				} finally { $jar.Dispose() }
			} finally {
				$stream.Dispose()
				$memory.Dispose()
			}
		}
		foreach ($class in $classes) {
			if ($locations[$class].Count -ne 1 -or $locations[$class][0] -notmatch '/knowage-dataset-sparql-') {
				throw "SPARQL class missing, duplicated or stale in ${File}: $class [$($locations[$class] -join ', ')]"
			}
		}
		if ($Cockpit) {
			foreach ($asset in @('dist/angular-bundle.js', 'dist/knowagecockpit-lib-bundle.js',
				'dist/knowagecockpit-modules-bundle.js', 'dist/knowagecockpit-modules-styles-bundle.css',
				'js/lib/openlayers/v6.15.1-dist/ol.js')) {
				if (!$archive.GetEntry($asset)) { throw "Missing cockpit asset: $asset" }
			}
		}
		Write-Output "PASS: $File"
	} finally { $archive.Dispose() }
}

if (!$KnowageWar -and !$CockpitWar -and !$AdditionalWar -and !$CockpitSourceMaps) {
	throw 'Provide KnowageWar, CockpitWar, AdditionalWar or CockpitSourceMaps'
}

foreach ($file in @($KnowageWar, $CockpitWar) + @($AdditionalWar) | Where-Object { $_ }) {
	Test-WarRuntimePayload -File $file
}
if ($KnowageWar) { Test-KnowageWar -File $KnowageWar -Cockpit $false }
if ($CockpitWar) { Test-KnowageWar -File $CockpitWar -Cockpit $true }

if ($CockpitSourceMaps) {
	$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $CockpitSourceMaps).Path)
	try {
		$files = @($archive.Entries | Where-Object { !$_.FullName.EndsWith('/') })
		$maps = @($files | Where-Object { $_.FullName -match '\.(js|css)\.map$' })
		$unexpected = @($files | Where-Object {
			$_.FullName -notmatch '\.(js|css)\.map$' -and $_.Name -notmatch '^(LICENSE|LICENCE|COPYING|NOTICE)'
		})
		if (!$maps.Count -or $unexpected.Count) {
			throw 'Source-map archive has no maps or contains non-debug payload'
		}
		Write-Output "PASS: $CockpitSourceMaps"
	} finally { $archive.Dispose() }
}
