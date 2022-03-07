export function parseDateTemp(date) {
    let result = ''
    if (date === 'd/m/Y') {
        result = 'dd/MM/yyyy'
    }
    if (date === 'm/d/Y') {
        result = 'MM/dd/yyyy'
    }
    return result
}

export function formatDate(date, format) {
    const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

    format = format + ''
    let result = ''
    let i_format = 0
    let c = ''
    let token = ''
    let y = (date.getYear() + '') as any
    const M = date.getMonth() + 1
    const d = date.getDate()
    const E = date.getDay()
    const H = date.getHours()
    const m = date.getMinutes()
    const s = date.getSeconds()

    const value = new Object()
    if (y.length < 4) {
        y = '' + (y - 0 + 1900)
    }
    value['y'] = '' + y
    value['yyyy'] = y
    value['yy'] = y.substring(2, 4)
    value['M'] = M
    value['MM'] = (M)
    value['MMM'] = MONTH_NAMES[M - 1]
    value['NNN'] = MONTH_NAMES[M + 11]
    value['d'] = d
    value['dd'] = (d)
    value['E'] = DAY_NAMES[E + 7]
    value['EE'] = DAY_NAMES[E]
    value['H'] = H
    value['HH'] = LZ(H)
    if (H == 0) {
        value['h'] = 12
    } else if (H > 12) {
        value['h'] = H - 12
    } else {
        value['h'] = H
    }
    value['hh'] = LZ(value['h'])
    if (H > 11) {
        value['K'] = H - 12
    } else {
        value['K'] = H
    }
    value['k'] = H + 1
    value['KK'] = LZ(value['K'])
    value['kk'] = LZ(value['k'])
    if (H > 11) {
        value['a'] = 'PM'
    } else {
        value['a'] = 'AM'
    }
    value['m'] = m
    value['mm'] = LZ(m)
    value['s'] = s
    value['ss'] = LZ(s)
    while (i_format < format.length) {
        c = format.charAt(i_format)
        token = ''
        while (format.charAt(i_format) == c && i_format < format.length) {
            token += format.charAt(i_format++)
        }
        if (value[token] != null) {
            result = result + value[token]
        } else {
            result = result + token
        }
    }
    return result
}

export function LZ(x) {
    return (x < 0 || x > 9 ? '' : '0') + x
}