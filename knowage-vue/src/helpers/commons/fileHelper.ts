export function downloadPromise(content, fileName, contentType): Promise<any> {
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

export function downloadDirect(jsonData, filename, contentType) {
	let blob = new Blob([jsonData], { type: contentType })
	if (navigator.msSaveBlob) {
		// IE 10+
		navigator.msSaveBlob(blob, filename)
	} else {
		let link = document.createElement('a')
		if (link.download !== undefined) {
			// feature detection
			// Browsers that support HTML5 download attribute
			let url = URL.createObjectURL(blob)
			link.setAttribute('href', url)
			link.setAttribute('download', filename)
			link.style.visibility = 'hidden'
			document.body.appendChild(link)
			link.click()
			document.body.removeChild(link)
		}
	}
}

export function getBlob(response, fileName, fileExtension) {
	console.log(fileName)
	console.log(fileExtension)
	var data = response.data
	var mimeType = response.headers['content-type']
	var contentDisposition = response.headers['content-disposition']
	var fileAndExtension = contentDisposition.match(/(?!([\b attachment;filename= \b])).*(?=)/g)[0]
	var blob = new Blob([data], { type: mimeType })
	var urlCreator = window.URL || window.webkitURL /* || window.mozURL || window.msURL */
	var url = urlCreator !== undefined ? urlCreator.createObjectURL(blob) : JSON.stringify(blob)

	if (window.navigator.msSaveBlob) {
		window.navigator.msSaveBlob(blob, fileAndExtension) // The user only has the option of clicking the Save button.
	} else {
		var link = document.createElement('a')
		document.body.appendChild(link)
		link.download = fileAndExtension
		link.href = url
		link.target = '_blank'
		link.click()

		document.body.removeChild(link)
	}
}
