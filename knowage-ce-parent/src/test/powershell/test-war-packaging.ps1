$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$validator = Join-Path $PSScriptRoot 'verify-war-packaging.ps1'
$fixtures = [System.Collections.Generic.List[string]]::new()

function New-TestWar {
	param([string[]] $Entries, [hashtable] $Contents = @{})

	$file = [System.IO.Path]::GetTempFileName()
	$fixtures.Add($file)
	$stream = [System.IO.File]::Open($file, [System.IO.FileMode]::Create)
	$archive = [System.IO.Compression.ZipArchive]::new($stream, [System.IO.Compression.ZipArchiveMode]::Create)
	try {
		foreach ($name in $Entries) {
			$entry = $archive.CreateEntry($name)
			$content = $entry.Open()
			try {
				if ($Contents.ContainsKey($name)) {
					$bytes = $Contents[$name]
					$content.Write($bytes, 0, $bytes.Length)
				} else { $content.WriteByte(0) }
			} finally { $content.Dispose() }
		}
	} finally {
		$archive.Dispose()
		$stream.Dispose()
	}
	return $file
}

function Assert-Rejected {
	param([string] $File, [string] $Message, [string] $Mode = 'AdditionalWar')

	$rejected = $false
	try {
		$parameters = @{}
		$parameters[$Mode] = $File
		& $validator @parameters
	} catch {
		if ($_.Exception.Message -notlike $Message) { throw }
		$rejected = $true
	}
	if (!$rejected) { throw "Validator accepted invalid payload: $File" }
}

function New-TestJar {
	param([string[]] $Entries, [hashtable] $Contents = @{})
	$memory = [System.IO.MemoryStream]::new()
	$archive = [System.IO.Compression.ZipArchive]::new($memory, [System.IO.Compression.ZipArchiveMode]::Create, $true)
	try {
		foreach ($name in $Entries) {
			$content = $archive.CreateEntry($name).Open()
			try {
				$bytes = [System.Text.Encoding]::UTF8.GetBytes($(if ($Contents.ContainsKey($name)) { $Contents[$name] } else { 'fixture' }))
				$content.Write($bytes, 0, $bytes.Length)
			} finally { $content.Dispose() }
		}
		$archive.Dispose()
		return ,$memory.ToArray()
	} finally {
		$archive.Dispose()
		$memory.Dispose()
	}
}

try {
	$application = @(
		'WEB-INF/classes/Application.class',
		'WEB-INF/lib/application-1.0.jar',
		'META-INF/LICENSE',
		'assets/config.map',
		'assets/application.js'
	)
	$clean = New-TestWar -Entries $application
	& $validator -AdditionalWar $clean

	foreach ($unwanted in @(
		'assets/application.js.map',
		'assets/application.css.map',
		'WEB-INF/lib/junit-jupiter-api-5.8.2.jar',
		'WEB-INF/lib/knowage-api-9.2.0-SNAPSHOT-tests.jar',
		'WEB-INF/lib/tomcat-embed-core-9.0.83.jar'
	)) {
		$file = New-TestWar -Entries ($application + $unwanted)
		Assert-Rejected -File $file -Message 'Unexpected runtime payload*'
	}

	$talend = @(
		'WEB-INF/classes/it/eng/spagobi/engines/talend/TalendEngine.class',
		'WEB-INF/lib/knowage-utils-9.2.0-SNAPSHOT.jar',
		'WEB-INF/lib/spago-core-2.3.0.jar',
		'WEB-INF/lib/dom4j-1.6.1.jar',
		'WEB-INF/lib/jaxws-rt-2.3.6.jar',
		'WEB-INF/lib/commonj-1.0.jar',
		'WEB-INF/lib/cas-client-core-3.5.0.jar'
	)
	$cleanTalend = New-TestWar -Entries $talend
	& $validator -AdditionalWar $cleanTalend
	& $validator -AdditionalWar @($clean, $cleanTalend)
	$file = New-TestWar -Entries ($talend + 'WEB-INF/lib/hazelcast-5.3.6.jar')
	Assert-Rejected -File $file -Message 'Unused dataset libraries*'
	$file = New-TestWar -Entries ($talend | Where-Object { $_ -notlike '*jaxws-rt*' })
	Assert-Rejected -File $file -Message 'Missing runtime dependency jaxws-rt*'

	$classes = @(
		'it/eng/spagobi/tools/dataset/bo/SPARQLDataSet.class',
		'it/eng/spagobi/tools/dataset/common/dataproxy/SPARQLDataProxy.class',
		'it/eng/spagobi/tools/dataset/common/datareader/SPARQLDataReader.class'
	)
	$provider = 'WEB-INF/lib/knowage-dataset-sparql-9.2.0-SNAPSHOT.jar'
	$jena = 'WEB-INF/lib/jena-arq-5.0.0.jar'
	$solr = 'WEB-INF/lib/solr-solrj-9.7.0.jar'
	$core = @($provider, $jena, $solr)
	$contents = @{
		$provider = New-TestJar -Entries $classes
		$jena = New-TestJar -Entries @('org/apache/jena/query/ResultSet.class')
		$solr = New-TestJar -Entries @('org/apache/solr/client/solrj/SolrClient.class')
	}
	$file = New-TestWar -Entries $core -Contents $contents
	& $validator -KnowageWar $file
	foreach ($location in @("WEB-INF/classes/$($classes[0])", 'WEB-INF/lib/unrelated-vendor-1.0.jar')) {
		$contents[$location] = $(if ($location.EndsWith('.jar')) { New-TestJar -Entries @($classes[0]) } else { [byte[]]@(1) })
		$file = New-TestWar -Entries ($core + $location) -Contents $contents
		Assert-Rejected -File $file -Mode 'KnowageWar' -Message 'SPARQL class missing, duplicated or stale*'
	}
	$file = New-TestWar -Entries ($core | Where-Object { $_ -ne $jena }) -Contents $contents
	Assert-Rejected -File $file -Mode 'KnowageWar' -Message 'Missing runtime provider jena-arq*'
	$contents[$provider] = New-TestJar -Entries ($classes + $classes[0])
	$file = New-TestWar -Entries $core -Contents $contents
	Assert-Rejected -File $file -Mode 'KnowageWar' -Message 'SPARQL class missing, duplicated or stale*'
	$contents[$provider] = New-TestJar -Entries $classes
	$assets = @(
		'dist/angular-bundle.js', 'dist/knowagecockpit-lib-bundle.js',
		'dist/knowagecockpit-modules-bundle.js', 'dist/knowagecockpit-modules-styles-bundle.css',
		'js/lib/openlayers/v6.15.1-dist/ol.js'
	)
	$file = New-TestWar -Entries ($core + $assets) -Contents $contents
	& $validator -CockpitWar $file
	$file = New-TestWar -Entries ($core + $assets[1..4]) -Contents $contents
	Assert-Rejected -File $file -Mode 'CockpitWar' -Message 'Missing cockpit asset*'
	$file = New-TestWar -Entries ($core + $assets + 'js/lib/openlayers/3.x.x/ol.js') -Contents $contents
	Assert-Rejected -File $file -Mode 'CockpitWar' -Message 'Unexpected runtime payload*'

	$debug = New-TestWar -Entries @('dist/application.js.map', 'asset-manifest.json', 'LICENSE')
	& $validator -CockpitSourceMaps $debug
	$debug = New-TestWar -Entries @('dist/application.js.map', 'dist/application.js')
	Assert-Rejected -File $debug -Mode 'CockpitSourceMaps' -Message 'Source-map archive has no maps or contains non-debug payload'

	$metadata = 'META-INF/maven/example/shared/pom.properties'
	$first = New-TestJar -Entries @('example/Shared.class', $metadata) -Contents @{ $metadata = "groupId=example`nartifactId=shared`nversion=1.0`n" }
	$second = New-TestJar -Entries @('example/Shared.class', $metadata) -Contents @{ $metadata = "groupId=example`nartifactId=shared`nversion=2.0`n" }
	$file = New-TestWar -Entries @(
		'WEB-INF/lib/first.jar', 'WEB-INF/lib/second.jar', 'WEB-INF/lib/identical.jar',
		'WEB-INF/classes/example/Shared.class', 'assets/one.js', 'assets/copy.js'
	) -Contents @{
		'WEB-INF/lib/first.jar' = $first
		'WEB-INF/lib/identical.jar' = $first
		'WEB-INF/lib/second.jar' = $second
	}
	$reportFile = [System.IO.Path]::GetTempFileName()
	$fixtures.Add($reportFile)
	& (Join-Path $PSScriptRoot 'inventory-wars.ps1') -War $file -Label 'synthetic-fixtures-not-a-build' -OutputFile $reportFile
	$report = Get-Content -LiteralPath $reportFile -Raw | ConvertFrom-Json
	$result = $report.Archives[0]
	if ($result.Jars.Count -ne 3 -or $result.DuplicateJars.Count -ne 1 -or
		$result.VersionConflicts.Count -ne 1 -or $result.DuplicateStaticFiles.Count -ne 1 -or
		$result.DuplicateClasses.Count -ne 1 -or $result.DuplicateClasses[0].Locations.Count -ne 4 -or
		$result.DuplicateClasses[0].Identical -or $result.UncompressedEntryBytes -le 0 -or
		$result.WarSha256.Length -ne 64) { throw 'Incorrect archive inventory' }
	Write-Output 'PASS: generic/main/cockpit/debug policies; classes in arbitrary JARs and WEB-INF/classes; duplicate/version/static inventory'
} finally {
	foreach ($file in $fixtures) { Remove-Item -LiteralPath $file }
}
