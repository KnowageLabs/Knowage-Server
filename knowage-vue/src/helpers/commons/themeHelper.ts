export default {
    setTheme(variables) {
        for (let key in variables) {
            document.documentElement.style.setProperty(key, variables[key])
        }
    }
}
