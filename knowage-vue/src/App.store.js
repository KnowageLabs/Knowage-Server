import { defineStore } from 'pinia'

const store = defineStore('store', {
    state() {
        return {
            configurations: {},
            user: {},
            error: {},
            info: {},
            warning: {},
            downloads: { count: { total: 0, alreadyDownloaded: 0 } },
            locale: 'en_US',
            news: { count: { total: 0, unRead: 0 } },
            loading: 0,
            homePage: {},
            internationalization: [],
            isEnterprise: false,
            licenses: {
                hosts: [],
                licenses: {},
                cpuNumber: -1
            },
            documentExecution: {},
            theme: {},
            defaultTheme: {}
        }
    },
    actions: {
        initializeUser(user) {
            this.setUser(user)
            this.setEnterprise(user.enterprise)
        },

        updateLicense(el) {
            let licenses = this.state.licenses

            let hostNameLicenses = licenses.licenses[el.hostName]

            let existingLicense = hostNameLicenses.filter((x) => x.product === el.license.product)

            if (existingLicense.length == 1) {
                hostNameLicenses.splice(existingLicense, 1)
            }

            hostNameLicenses.push(el.license)

            this.setLicenses(licenses)
        },
        setConfigurations(configs) {
            this.configurations = configs
        },
        getUser() {
            return this.user
        },
        setUser(user) {
            this.user = user
        },
        setError(error) {
            this.error = error
        },
        setInfo(info) {
            this.info = info
        },
        setLoading(loading) {
            if (loading) this.loading++
            else this.loading--

            if (this.loading < 0) this.loading = 0
        },
        setWarning(warning) {
            this.warning = warning
        },
        setLocale(locale) {
            this.locale = locale
        },
        setDownloads(hasDownload) {
            this.downloads = hasDownload
        },
        updateAlreadyDownloadedFiles() {
            if (this.downloads.count.total > this.downloads.count.alreadyDownloaded) this.downloads.count.alreadyDownloaded++
        },
        setNews(hasNews) {
            this.news = hasNews
        },
        setHomePage(homePage) {
            this.homePage = homePage
        },
        setInternationalization(internationalization) {
            this.internationalization = internationalization
        },
        setLicenses(licenses) {
            this.licenses = licenses
        },
        setEnterprise(enterprise) {
            this.isEnterprise = enterprise
        },
        setDocumentExecutionEmbed() {
            this.documentExecution.embed = true
        },
        setTheme(theme) {
            this.theme = theme
        },
        setDefaultTheme(defaultTheme) {
            this.defaultTheme = defaultTheme
        }
    }
})

export default store
