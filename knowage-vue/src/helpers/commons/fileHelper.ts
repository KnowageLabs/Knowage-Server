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

export function downloadDirectFromResponseWithCustomName(response, customFilename) {
    downloadDirect(response.data, customFilename, response.headers['content-type'])
}

export function downloadDirect(jsonData, filename, contentType) {
    console.log(filename)
    console.log(contentType)
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

            if (fnBrowserDetect() !== 'firefox') {
                let lastDot = filename.lastIndexOf('.')
                if (lastDot != -1) {
                    let fileNameWithoutExtension = filename.substring(0, lastDot)
                    link.setAttribute('download', fileNameWithoutExtension)
                }
            }
            link.style.visibility = 'hidden'
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
        }
    }
}

export function byteToHumanFriendlyFormat(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes'

    const k = 1024
    const dm = decimals < 0 ? 0 : decimals
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']

    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
}

function fnBrowserDetect() {
    let userAgent = navigator.userAgent
    let browserName

    if (userAgent.match(/chrome|chromium|crios/i)) {
        browserName = 'chrome'
    } else if (userAgent.match(/firefox|fxios/i)) {
        browserName = 'firefox'
    } else if (userAgent.match(/safari/i)) {
        browserName = 'safari'
    } else if (userAgent.match(/opr\//i)) {
        browserName = 'opera'
    } else if (userAgent.match(/edg/i)) {
        browserName = 'edge'
    } else {
        browserName = 'No browser detection'
    }

    return browserName
}
