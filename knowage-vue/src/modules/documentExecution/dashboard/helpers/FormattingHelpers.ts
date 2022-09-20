export const hexToRgb = (hex: string) => {
    if (!hex.startsWith('#')) return 'rgb(0, 0, 0)'
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? "rgb(" + parseInt(result[1], 16) + ', ' + parseInt(result[2], 16) + ', ' + parseInt(result[3], 16) + ')' : '';
}

export const convertColorFromHSLtoRGB = (hslColor: string | null) => {
    if (!hslColor) return 'rgb(0, 0, 0)'
    const temp = hslColor
        ?.trim()
        ?.substring(4, hslColor.length - 1)
        ?.split(',')

    const h = temp[0] ? +temp[0] : 0
    let s = temp[1] ? +(temp[1].replace('%', '').trim()) : 0
    let l = temp[2] ? +(temp[2].replace('%', '').trim()) : 0


    s /= 100;
    l /= 100;
    const k = n => (n + h / 30) % 12;
    const a = s * Math.min(l, 1 - l);
    const f = (n: number) =>
        l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
    const tempResult = [Math.round(255 * f(0)), Math.round(255 * f(8)), Math.round(255 * f(4))]
    return 'rgb(' + tempResult[0] + ', ' + tempResult[1] + ', ' + tempResult[2] + ')'
}