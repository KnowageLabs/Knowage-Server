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

export function downloadDirectFromResponse(response) {
    var contentDisposition = response.headers['content-disposition']
    var fileAndExtension = contentDisposition.match(/(?!([\b attachment;filename= \b])).*(?=)/g)[0]
    var completeFileName = fileAndExtension.replaceAll('"', '').replaceAll(';', '')
    downloadDirect(response.data, completeFileName, response.headers['content-type'])
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
