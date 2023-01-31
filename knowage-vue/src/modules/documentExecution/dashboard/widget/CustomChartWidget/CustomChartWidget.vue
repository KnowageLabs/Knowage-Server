<template>
    <iframe v-if="!loading" :id="'iframe-' + id" :name="'iframe-' + id" class="custom-chart-widget-iframe" width="100%" height="100%" src="about:blank"></iframe>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IVariable } from '../../Dashboard'
import { mapActions } from 'pinia'
import { IWidget } from '../../Dashboard'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { CustomChartDatastore } from '../WidgetEditor/WidgetEditorSettingsTab/CustomChartWidget/datastore/CustomChartWidgetDatastore'
import store from '../../Dashboard.store'
import appStore from '../../../../../App.store'

import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'custom-chart-widget',
    emits: ['interaction', 'pageChanged', 'launchSelection', 'sortingChanged'],
    components: {},
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        widgetData: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            id: cryptoRandomString({ length: 16, type: 'base64' }),
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string,
            webComponentJs: '' as string,
            datastore: new CustomChartDatastore(null),
            userScriptsURLs: [] as string[],
            iframeDocument: null as any,
            loadedScriptsCount: 0,
            loading: false
        }
    },
    watch: {
        widgetData() {
            this.loadDataToShow()
        },
        propActiveSelections() {
            this.loadActiveSelections()
        },
        editorMode() {
            this.renderCustomWidget()
        }
    },
    mounted() {
        this.setEventListeners()
        this.loadActiveSelections()
        this.loadDataToShow()
        this.loadProfileAttributesToDatastore()
        this.loadVariablesToDatastore()
    },
    unmounted() {
        this.removeEventListeners()
        this.userScriptsURLs = []
        this.iframeDocument = null
        this.loadedScriptsCount = 0
    },
    methods: {
        ...mapActions(store, ['getInternationalization', 'setSelections', 'getAllDatasets', 'getDashboardDrivers', 'getProfileAttributes']),
        ...mapActions(appStore, ['setError', 'setLoading']),
        setEventListeners() {
            window.addEventListener('message', this.iframeEventsListener)
        },
        removeEventListeners() {
            window.removeEventListener('message', this.iframeEventsListener)
        },
        iframeEventsListener(event: any) {
            console.log('EVENT FROM IFRAME: ', event)
            if (event.data.type === 'error') this.setError({ title: this.$t('common.error.generic'), msg: event.data.error?.message ?? '' })
            else if (event.data.type === 'clickManager') this.onClickManager(event.data.payload.columnName, event.data.payload.columnValue)
        },
        loadProfileAttributesToDatastore() {
            const profileAttributes = this.getProfileAttributes()
            this.datastore.setProfileAttributes(profileAttributes)
        },
        loadVariablesToDatastore() {
            this.datastore.setVariables(this.variables)
        },
        async loadDataToShow() {
            this.loading = true
            this.dataToShow = this.widgetData
            this.datastore.setData(this.widgetData)
            await this.loadHTML()
            this.loading = false
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        async loadHTML() {
            this.loading = true
            if (!this.propWidget.settings || !this.propWidget.settings.editor) return

            this.htmlContent = this.propWidget.settings.editor.html
            this.webComponentCss = this.propWidget.settings.editor.css
            this.webComponentJs = this.propWidget.settings.editor.js
            this.loading = false

            this.renderCustomWidget()
        },
        renderCustomWidget() {
            this.loadedScriptsCount = 0
            const iframe = document.getElementById('iframe-' + this.id) as any
            this.iframeDocument = iframe.contentWindow.document
            this.iframeDocument.body.innerHTML = `<html>
                <head></head>
                <body>
                    <div id="containerElement">
                    </div>
                </body>
            </html>`

            const containerElement = this.iframeDocument.getElementById('containerElement')
            this.createWrapperDiv(containerElement)
            this.insertUsersHtmlContent()
            this.insertUsersCssContent()
            this.setDatastoreObjectInFrame(iframe)
            this.loadUserImportScripts()
        },
        createWrapperDiv(containerElement: Element) {
            const style = document.createElement('style')
            style.classList.add('style-wrapper')

            const wrapper = document.createElement('div')
            wrapper.classList.add('component-wrapper')
            wrapper.style.position = 'relative'
            wrapper.style.overflow = 'auto'
            wrapper.style.height = '100%'

            wrapper.textContent = ''
            containerElement.appendChild(style)
            containerElement.appendChild(wrapper)
        },
        insertUsersHtmlContent() {
            const tempEl = this.iframeDocument.querySelector('.component-wrapper')
            if (tempEl) tempEl.innerHTML = this.htmlContent
            this.getUserImportScripts(tempEl)
        },
        getUserImportScripts(componentWrapperElement: any) {
            // TODO - remove hardcoded imports
            this.userScriptsURLs = ['https://code.highcharts.com/highcharts.js', 'https://code.highcharts.com/modules/drilldown.js', 'https://code.highcharts.com/highcharts.js']
            const userImports = componentWrapperElement.getElementsByTagName('kn-import') ?? []
            for (let i = 0; i < userImports.length; i++) {
                if (userImports.item(i)?.attributes?.src?.textContent) {
                    const textContent = userImports.item(i).attributes.src.textContent
                    const url = textContent.startsWith('http') ? textContent : import.meta.env.VITE_HOST_URL + '/' + userImports.item(i).attributes.src.textContent
                    this.userScriptsURLs.push(url)
                }
            }
        },
        insertUsersCssContent() {
            const tempEl = this.iframeDocument.querySelector('.style-wrapper')
            if (tempEl) tempEl.innerHTML = this.webComponentCss
        },
        setDatastoreObjectInFrame(iframe: any) {
            iframe.contentWindow.datastore = this.datastore
        },
        createScriptTagFromUsersJSScript() {
            const userScript = document.createElement('script')
            // userScript.text = "try { window.parent.postMessage({ type: 'loading', loading: true}, '*'); " + this.webComponentJs + ` window.parent.postMessage({ type: 'loading', loading: false}, '*'); } catch (error) {window.parent.postMessage({type: 'error', error: error}, '*')}`
            userScript.text = 'try {' + this.webComponentJs + `} catch (error) {window.parent.postMessage({type: 'error', error: error}, '*')}`

            setTimeout(() => {
                this.iframeDocument?.body?.appendChild(userScript)
                console.log('----- LOADING: ', false)
                this.setLoading(false)
            }, 1000)
            this.setLoading(false)
        },
        loadUserImportScripts() {
            console.log('----- LOADING: ', true)
            this.setLoading(true)
            if (this.loadedScriptsCount === this.userScriptsURLs.length) this.createScriptTagFromUsersJSScript()
            else this.loadUserImportScript(this.userScriptsURLs[this.loadedScriptsCount])
        },
        loadUserImportScript(scriptURL: string) {
            if (this.isUserScriptAlreadLoaded(scriptURL)) {
                this.loadedScriptsCount++
                this.loadUserImportScripts()
                return
            }

            const userImportScript = document.createElement('script')
            userImportScript.setAttribute('src', scriptURL)
            userImportScript.async = false
            userImportScript.addEventListener('load', () => {
                this.loadedScriptsCount++
                this.loadUserImportScripts()
            })
            userImportScript.addEventListener('error', () => {
                console.log('----- LOADING: ', false)
                this.setLoading(false)
            })
            this.iframeDocument.body.appendChild(userImportScript)
        },
        isUserScriptAlreadLoaded(scriptURL: string) {
            let loaded = false
            const loadedScriptElements = this.iframeDocument.getElementsByTagName('script')
            const test = deepcopy(loadedScriptElements)
            for (let i = 0; i < loadedScriptElements.length; i++) {
                if (loadedScriptElements.item(i).attributes?.src?.textContent == scriptURL) {
                    loaded = true
                    break
                }
            }
            return loaded
        },
        onClickManager(columnName: string, columnValue: string | number) {
            if (this.editorMode || !columnName) return
            updateStoreSelections(this.createNewSelection([columnValue], columnName), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
        },
        createNewSelection(value: (string | number)[], columnName: string) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        getDatasetLabel(datasetId: number) {
            const datasets = this.getAllDatasets()
            const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? datasets[index].label : ''
        }
    }
})
</script>

<style lang="scss" scoped>
.custom-chart-widget-iframe {
    border: 0;
}
</style>
