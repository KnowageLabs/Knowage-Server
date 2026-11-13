param(
	[Parameter(Mandatory = $true)]
	[string[]] $War,
	[Parameter(Mandatory = $true)]
	[string] $Label,
	[Parameter(Mandatory = $true)]
	[string] $OutputFile
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jarCache = @{}

function Get-StreamHash {
	param([System.IO.Stream] $Stream)
	$sha = [System.Security.Cryptography.SHA256]::Create()
	try {
		return [System.BitConverter]::ToString($sha.ComputeHash($Stream)).Replace('-', '').ToLowerInvariant()
	} finally { $sha.Dispose() }
}

function Read-Jar {
	param([System.IO.Compression.ZipArchiveEntry] $Entry)
	$memory = [System.IO.MemoryStream]::new()
	$stream = $Entry.Open()
	try { $stream.CopyTo($memory) } finally { $stream.Dispose() }
	try {
		$memory.Position = 0
		$hash = Get-StreamHash -Stream $memory
		if ($jarCache.ContainsKey($hash)) { return $jarCache[$hash] }
		$memory.Position = 0
		$jar = [System.IO.Compression.ZipArchive]::new($memory, [System.IO.Compression.ZipArchiveMode]::Read, $true)
		try {
			$coordinates = @()
			$classes = [System.Collections.Generic.List[object]]::new()
			foreach ($item in $jar.Entries) {
				if ($item.FullName.EndsWith('.class')) {
					$content = $item.Open()
					try { $classHash = Get-StreamHash -Stream $content } finally { $content.Dispose() }
					$classes.Add([pscustomobject]@{ Name = $item.FullName; Sha256 = $classHash })
				} elseif ($item.FullName -match '^META-INF/maven/[^/]+/[^/]+/pom\.properties$') {
					$reader = [System.IO.StreamReader]::new($item.Open())
					try { $properties = $reader.ReadToEnd() } finally { $reader.Dispose() }
					$values = @{}
					foreach ($line in $properties -split '\r?\n') {
						if ($line -match '^\s*(groupId|artifactId|version)\s*=\s*(.*?)\s*$') {
							$values[$matches[1]] = $matches[2]
						}
					}
					if ($values.groupId -and $values.artifactId -and $values.version) {
						$coordinates += [pscustomobject]@{
							GroupId = $values.groupId; ArtifactId = $values.artifactId
							Version = $values.version; Evidence = $item.FullName
						}
					}
				}
			}
			$result = [pscustomobject]@{ Sha256 = $hash; Coordinates = $coordinates; Classes = $classes.ToArray() }
			$jarCache[$hash] = $result
			return $result
		} finally { $jar.Dispose() }
	} finally { $memory.Dispose() }
}

$results = @()
foreach ($file in $War) {
	$resolved = (Resolve-Path -LiteralPath $file).Path
	$archive = [System.IO.Compression.ZipFile]::OpenRead($resolved)
	try {
		$jars = [System.Collections.Generic.List[object]]::new()
		$classIndex = @{}
		$staticIndex = @{}
		$directories = @{}
		$totalBytes = 0L
		$sourceMapBytes = 0L
		$backendBytes = 0L
		$frontendBytes = 0L
		foreach ($entry in $archive.Entries) {
			$totalBytes += $entry.Length
			if ($entry.FullName.EndsWith('/')) { continue }
			if ($entry.FullName -match '^WEB-INF/lib/[^/]+\.jar$') {
				$backendBytes += $entry.CompressedLength
				$jar = Read-Jar -Entry $entry
				$jars.Add([pscustomobject]@{
					Path = $entry.FullName; Sha256 = $jar.Sha256; Bytes = $entry.Length
					CompressedBytes = $entry.CompressedLength; Coordinates = $jar.Coordinates
					ClassCount = $jar.Classes.Count
				})
				foreach ($class in $jar.Classes) {
					if (!$classIndex.ContainsKey($class.Name)) {
						$classIndex[$class.Name] = [System.Collections.Generic.List[object]]::new()
					}
					$classIndex[$class.Name].Add([pscustomobject]@{ Path = $entry.FullName; Sha256 = $class.Sha256 })
				}
			} elseif ($entry.FullName -match '^WEB-INF/classes/(.+\.class)$') {
				$name = $matches[1]
				$content = $entry.Open()
				try { $hash = Get-StreamHash -Stream $content } finally { $content.Dispose() }
				if (!$classIndex.ContainsKey($name)) {
					$classIndex[$name] = [System.Collections.Generic.List[object]]::new()
				}
				$classIndex[$name].Add([pscustomobject]@{ Path = $entry.FullName; Sha256 = $hash })
			} elseif ($entry.FullName -notmatch '^(WEB-INF|META-INF)/') {
				$frontendBytes += $entry.CompressedLength
				if ($entry.FullName -match '\.(js|css)\.map$') { $sourceMapBytes += $entry.CompressedLength }
				$directory = (($entry.FullName -split '/' | Select-Object -First 2) -join '/')
				if (!$directories.ContainsKey($directory)) {
					$directories[$directory] = [pscustomobject]@{ Path = $directory; Entries = 0; Bytes = 0L; CompressedBytes = 0L }
				}
				$directories[$directory].Entries++
				$directories[$directory].Bytes += $entry.Length
				$directories[$directory].CompressedBytes += $entry.CompressedLength
				$content = $entry.Open()
				try { $hash = Get-StreamHash -Stream $content } finally { $content.Dispose() }
				if (!$staticIndex.ContainsKey($hash)) {
					$staticIndex[$hash] = [System.Collections.Generic.List[string]]::new()
				}
				$staticIndex[$hash].Add($entry.FullName)
			}
		}
		$duplicates = @($classIndex.GetEnumerator() | Where-Object { $_.Value.Count -gt 1 } |
			Sort-Object Key | ForEach-Object {
				[pscustomobject]@{
					Class = $_.Key; Locations = $_.Value.ToArray()
					Identical = @($_.Value.Sha256 | Select-Object -Unique).Count -eq 1
				}
			})
		$coordinateVersions = @{}
		foreach ($jar in $jars) {
			foreach ($coordinate in $jar.Coordinates) {
				$key = $coordinate.GroupId + ':' + $coordinate.ArtifactId
				if (!$coordinateVersions.ContainsKey($key)) { $coordinateVersions[$key] = @() }
				$coordinateVersions[$key] += $coordinate.Version
			}
		}
		$results += [pscustomobject]@{
			File = $resolved; WarSha256 = (Get-FileHash -LiteralPath $resolved -Algorithm SHA256).Hash.ToLowerInvariant()
			WarBytes = (Get-Item -LiteralPath $resolved).Length; UncompressedEntryBytes = $totalBytes
			EntryCount = $archive.Entries.Count; LibraryCompressedBytes = $backendBytes
			StaticCompressedBytes = $frontendBytes; SourceMapCompressedBytes = $sourceMapBytes
			Jars = $jars.ToArray(); DuplicateClasses = $duplicates
			DuplicateJars = @($jars | Group-Object Sha256 | Where-Object Count -gt 1 |
				ForEach-Object { [pscustomobject]@{ Sha256 = $_.Name; Paths = @($_.Group.Path) } })
			VersionConflicts = @($coordinateVersions.GetEnumerator() |
				Where-Object { @($_.Value | Select-Object -Unique).Count -gt 1 } | Sort-Object Key |
				ForEach-Object { [pscustomobject]@{ Coordinate = $_.Key; Versions = @($_.Value | Sort-Object -Unique) } })
			StaticDirectories = @($directories.Values | Sort-Object Path)
			DuplicateStaticFiles = @($staticIndex.GetEnumerator() | Where-Object { $_.Value.Count -gt 1 } |
				Sort-Object Key | ForEach-Object { [pscustomobject]@{ Sha256 = $_.Key; Paths = $_.Value.ToArray() } })
		}
		Write-Host ("Inventoried {0}: {1} JARs, {2} duplicated class paths" -f $resolved, $jars.Count, $duplicates.Count)
	} finally { $archive.Dispose() }
}
$report = [pscustomobject]@{
	Label = $Label
	Limitations = 'Archive evidence only. Maven coordinates are embedded metadata claims (possibly shaded), not resolved provenance. No JVM class loading, dependency-tree resolution, reflection/SPI/JSP or runtime compatibility is proved. Multi-release class paths are recorded separately. WEB-INF/lib-provided is outside the external-container application classpath.'
	Archives = $results
}
$report | ConvertTo-Json -Depth 14 | Set-Content -LiteralPath $OutputFile -Encoding UTF8
