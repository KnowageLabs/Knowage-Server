export function download(content, fileName, contentType): Promise<any> {
	return new Promise((resolve, reject) => {
		var href = ''
		if (contentType) {
			var file = new Blob([content], { type: contentType })
			href = window.URL.createObjectURL(file)
		} else {
			href = content
		}

		try {
			window.open(href)
			resolve(true)
		} catch (e) {
			reject(e)
		}
	})
}
