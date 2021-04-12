export function download(content, fileName, contentType): Promise<any> {
	return new Promise((resolve, reject) => {
		var a = document.createElement('a')
		if (contentType) {
			var file = new Blob([content], { type: contentType })
			a.href = URL.createObjectURL(file)
		} else {
			a.setAttribute('href', content)
			document.body.appendChild(a)
		}

		if (fileName) a.setAttribute('download', fileName)
		try {
			a.click()
			/* URL.revokeObjectURL(a.href) */
			resolve(true)
		} catch (e) {
			reject(e)
		}
	})
}
