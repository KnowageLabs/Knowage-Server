import { createStore } from 'vuex'
import overlayStore from './overlay/Overlay.store'

// Create a new store instance.
const store = createStore({
    modules: {
        overlayStore
    },
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
            defaultTheme: {},
            dataPreparation: {
                loadedAvros: [],
                loadingAvros: [],
                avroDatasets: []
            }
        }
    },
    actions: {
        initializeUser(context, user) {
            context.commit('setUser', user)
            context.commit('setEnterprise', user.enterprise)
        },

        updateLicense(context, el) {
            let licenses = context.state.licenses

            let hostNameLicenses = licenses.licenses[el.hostName]

            let existingLicense = hostNameLicenses.filter((x) => x.product === el.license.product)

            if (existingLicense.length == 1) {
                hostNameLicenses.splice(existingLicense, 1)
            }

            hostNameLicenses.push(el.license)

            context.commit('setLicenses', licenses)
        }
    },
    mutations: {
        setConfigurations(state, configs) {
            state.configurations = configs
        },
        setUser(state, user) {
            state.user = user
        },
        setError(state, error) {
            state.error = error
        },
        setInfo(state, info) {
            state.info = info
        },
        setLoading(state, loading) {
            if (loading) state.loading++
            else state.loading--

            if (state.loading < 0) state.loading = 0
        },
        setWarning(state, warning) {
            state.warning = warning
        },
        setLocale(state, locale) {
            state.locale = locale
        },
        setDownloads(state, hasDownload) {
            state.downloads = hasDownload
        },
        updateAlreadyDownloadedFiles(state) {
            if (state.downloads.count.total > state.downloads.count.alreadyDownloaded) state.downloads.count.alreadyDownloaded++
        },
        setNews(state, hasNews) {
            state.news = hasNews
        },
        setHomePage(state, homePage) {
            state.homePage = homePage
        },
        setInternationalization(state, internationalization) {
            state.internationalization = internationalization
        },
        setLicenses(state, licenses) {
            state.licenses = licenses
        },
        setEnterprise(state, enterprise) {
            state.isEnterprise = enterprise
        },
        setDocumentExecutionEmbed(state) {
            state.documentExecution.embed = true
        },
        setTheme(state, theme) {
            state.theme = theme
        },
        setDefaultTheme(state, defaultTheme) {
            state.defaultTheme = defaultTheme
        },
        addToLoadedAvros(state, dsId) {
            let stringId = dsId.toString()
            let idx = state.dataPreparation.loadedAvros.indexOf(stringId)
            if (idx == -1) state.dataPreparation.loadedAvros.push(stringId)
        },
        addToAvroDatasets(state, dsId) {
            let stringId = dsId.toString()
            let idx = state.dataPreparation.avroDatasets.indexOf(stringId)
            if (idx == -1) state.dataPreparation.avroDatasets.push(stringId)
        },
        addToLoadingAvros(state, dsId) {
            let stringId = dsId.toString()
            let idx = state.dataPreparation.loadingAvros.indexOf(stringId)
            if (idx == -1) state.dataPreparation.loadingAvros.push(stringId)
        },
        removeFromLoadedAvros(state, dsId) {
            let stringId = dsId.toString()
            let idx = state.dataPreparation.loadedAvros.indexOf(stringId)
            if (idx >= 0) state.dataPreparation.loadedAvros.splice(idx, 1)
        },
        removeFromLoadingAvros(state, dsId) {
            let stringId = dsId.toString()
            let idx = state.dataPreparation.loadingAvros.indexOf(stringId)
            if (idx >= 0) state.dataPreparation.loadingAvros.splice(idx, 1)
        },
        setAvroDatasets(state, data) {
            state.dataPreparation.avroDatasets = data
        },
        setLoadedAvros(state, data) {
            state.dataPreparation.loadedAvros = data
        }
    },
    getters: {
        isAvroLoaded(state) {
            return (id) => state.dataPreparation.loadedAvros.indexOf(id.toString()) >= 0
        },
        isAvroLoading(state) {
            return (id) => state.dataPreparation.loadingAvros.indexOf(id.toString()) >= 0
        },
        isAvroReady(state) {
            return (dsId) => {
                if ((dsId && state.dataPreparation.avroDatasets.indexOf(dsId.toString()) >= 0) || (dsId && state.dataPreparation.avroDatasets.indexOf(dsId.toString())) >= 0) return true
                else return false
            }
        }
    }
})

export default store
