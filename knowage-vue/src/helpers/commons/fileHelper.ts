export function download(content, fileName, contentType): void {
	var a = document.createElement('a')
	if (contentType) {
		var file = new Blob([content], { type: contentType })
		a.href = URL.createObjectURL(file)
	} else {
		a.setAttribute('href', process.env.VUE_APP_API_URL + content)
		document.body.appendChild(a)
	}

	if (fileName) a.setAttribute('download', fileName)
	a.click()
}
