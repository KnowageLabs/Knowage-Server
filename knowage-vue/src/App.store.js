import { createStore } from 'vuex'

// Create a new store instance.
const store = createStore({
	state() {
		return {
			user: {},
			error: {},
			info: {},
			warning: {},
			downloads: { count: { total: 0, unRead: 0 } },
			locale: {},
			news: { count: { total: 0, unRead: 0 } },
			loading: false,
			homePage: {},
			internationalization: []
		}
	},
	mutations: {
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
			state.loading = loading
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
		}
	}
})

export default store
