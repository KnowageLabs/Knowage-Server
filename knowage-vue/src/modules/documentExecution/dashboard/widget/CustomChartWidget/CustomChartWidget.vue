<template>
    <iframe name="iframe" id="iframe" width="100%" height="100%" src="about:blank"></iframe>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver, IDataset, ISelection, IVariable } from '../../Dashboard'
import { mapActions } from 'pinia'
import store from '../../Dashboard.store'
import appStore from '../../../../../App.store'
import { IWidget } from '../../Dashboard'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { CustomChartDatastore } from '../WidgetEditor/WidgetEditorSettingsTab/CustomChartWidget/datastore/CustomChartWidgetDatastore'

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
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string,
            webComponentJs: '' as string,
            drivers: [] as IDashboardDriver[],
            datastore: new CustomChartDatastore(null),
            userScriptsURLs: [] as string[],
            iframeDocument: null as any,
            loadedScriptsCount: 0
        }
    },
    watch: {
        widgetData() {
            this.loadDataToShow()
        },
        propActiveSelections() {
            this.loadActiveSelections()
        }
    },
    mounted() {
        this.setEventListeners()
        this.loadDrivers()
        this.loadActiveSelections()
        this.loadDataToShow()
    },
    unmounted() {
        this.removeEventListeners()
        this.userScriptsURLs = []
        this.iframeDocument = null
        this.loadedScriptsCount = 0
    },
    methods: {
        ...mapActions(store, ['getInternationalization', 'setSelections', 'getAllDatasets', 'getDashboardDrivers']),
        ...mapActions(appStore, ['setError']),
        setEventListeners() {
            window.addEventListener('message', this.iframeEventsListener)
        },
        removeEventListeners() {
            window.removeEventListener('message', this.iframeEventsListener)
        },
        iframeEventsListener(event: any) {
            console.log('EVENT FROM IFRAME: ', event)
            if (event.data.type === 'error') this.setError({ title: this.$t('common.error.generic'), msg: event.data.error?.message ?? '' })
        },
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId) // TODO
        },
        async loadDataToShow() {
            console.log('dataToShow ------------', this.widgetData)
            this.dataToShow = this.widgetData
            await this.loadHTML()
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections // TODO
        },
        async loadHTML() {
            if (!this.propWidget.settings || !this.propWidget.settings.editor) return

            this.htmlContent = this.propWidget.settings.editor.html
            this.webComponentCss = this.propWidget.settings.editor.css
            this.webComponentJs = this.propWidget.settings.editor.js

            this.renderCustomWidget()
        },
        onSelect(event: any) {
            // TODO
            if (this.editorMode || !event.detail) return
            const value = event.detail.selectionValue
            const selectionColumnName = event.detail.selectionColumn
            updateStoreSelections(this.createNewSelection([value], selectionColumnName), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
        },
        getDatasetLabel(datasetId: number) {
            const datasets = this.getAllDatasets()
            const index = datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? datasets[index].label : ''
        },
        createNewSelection(value: (string | number)[], columnName: string) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: columnName, value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        renderCustomWidget() {
            console.log('>>>>>>> HTML CONTENT: ', this.htmlContent)
            this.loadedScriptsCount = 0
            const iframe = document.getElementById('iframe') as any
            this.iframeDocument = iframe.contentWindow.document
            this.iframeDocument.body.innerHTML = `<html>
                <head></head>
                <body>
                    <div id="containerElement">
                    </div>
                </body>
            </html>`

            const containerElement = this.iframeDocument.getElementById('containerElement')
            console.log('--------- containerElement: ', containerElement)
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
            console.log('>>>>>>> tempEl: ', tempEl)
            this.getUserImportScripts(tempEl)
        },
        getUserImportScripts(componentWrapperElement: any) {
            // TODO - remove hardcoded imports
            this.userScriptsURLs = ['https://code.highcharts.com/highcharts.js', 'https://code.highcharts.com/modules/drilldown.js']
            const userImports = componentWrapperElement.getElementsByTagName('kn-import') ?? []
            for (let i = 0; i < userImports.length; i++) {
                console.log('--------- userImport src: ', userImports.item(i).attributes.src.textContent)
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
            console.log('LOADING USER SCRIPT!!!!!!!!!!!!!!!!!: ')
            const userScript = document.createElement('script')
            userScript.text = 'try {' + this.webComponentJs + `} catch (error) {window.parent.postMessage({type: 'error', error: error}, '*')}`
            setTimeout(() => this.iframeDocument.body.appendChild(userScript), 10000)
        },
        loadUserImportScripts() {
            console.log('THIS LOADED SCRIPT COUNT: ', this.loadedScriptsCount)
            if (this.loadedScriptsCount === this.userScriptsURLs.length) this.createScriptTagFromUsersJSScript()
            else this.loadUserImportScript(this.userScriptsURLs[this.loadedScriptsCount])
        },
        loadUserImportScript(scriptURL: string) {
            const userImportScript = document.createElement('script')
            userImportScript.setAttribute('src', scriptURL)
            userImportScript.addEventListener('load', () => {
                this.loadedScriptsCount++
                this.loadUserImportScripts()
            })

            this.iframeDocument.body.appendChild(userImportScript)
        }
    }
})
</script>

<style lang="scss" scoped>
#iframe {
    border: 0;
}
</style>
