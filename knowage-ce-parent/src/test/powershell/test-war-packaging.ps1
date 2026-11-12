$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$validator = Join-Path $PSScriptRoot 'verify-war-packaging.ps1'
$fixtures = [System.Collections.Generic.List[string]]::new()

function New-TestWar {
	param([string[]] $Entries)

	$file = [System.IO.Path]::GetTempFileName()
	$fixtures.Add($file)
	$stream = [System.IO.File]::Open($file, [System.IO.FileMode]::Create)
	$archive = [System.IO.Compression.ZipArchive]::new($stream, [System.IO.Compression.ZipArchiveMode]::Create)
	try {
		foreach ($name in $Entries) {
			$entry = $archive.CreateEntry($name)
			$content = $entry.Open()
			try { $content.WriteByte(0) } finally { $content.Dispose() }
		}
	} finally {
		$archive.Dispose()
		$stream.Dispose()
	}
	return $file
}

function Assert-Rejected {
	param([string] $File, [string] $Message)

	$rejected = $false
	try {
		& $validator -AdditionalWar $File
	} catch {
		if ($_.Exception.Message -notlike $Message) { throw }
		$rejected = $true
	}
	if (!$rejected) { throw "Validator accepted invalid payload: $File" }
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
	Write-Output 'PASS: CE/EE payload policy, multiple WARs, debug/license assets and Talend dependency boundaries'
} finally {
	foreach ($file in $fixtures) { Remove-Item -LiteralPath $file }
}
