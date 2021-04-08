export function download(content, fileName, contentType, callBack: Function): Function {
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
	} catch (e) {
		console.log(e)
	}

	URL.revokeObjectURL(a.href)

	return callBack
}
