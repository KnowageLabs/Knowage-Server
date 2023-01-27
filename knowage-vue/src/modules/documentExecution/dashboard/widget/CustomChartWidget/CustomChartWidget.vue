<template>
    <!-- <custom-chart-widget-web-component class="kn-flex" ref="webComponent"></custom-chart-widget-web-component> -->
    <!-- <div id="containerElement"></div> -->
    <iframe name="iframe" id="iframe" width="100%" height="100%" src="about:blank"></iframe>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver, IDataset, ISelection, IVariable } from '../../Dashboard'
import './webComponent/CustomChartWidgetWebComponent'
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
            webComponentRef: {} as any,
            drivers: [] as IDashboardDriver[],
            datastore: new CustomChartDatastore(null),
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
        this.webComponentRef = this.$refs.webComponent as any
        this.loadDrivers()
        this.loadActiveSelections()
        this.loadDataToShow()
        //  console.log('DATASTORE ---------------', this.datastore)
    },
    unmounted() {
        this.removeEventListeners()
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

            // TODO - remove
            // if (!this.webComponentRef) return
            // this.webComponentRef.htmlContent = this.propWidget.type === 'text' ? '<div style="position: absolute;height: 100%;width: 100%;">' + this.htmlContent + '</div>' : this.htmlContent
            // this.webComponentRef.webComponentCss = this.webComponentCss
            // this.webComponentRef.webComponentJs = this.webComponentJs
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
        //#region ===================== IFRAME Logic ====================================================
        renderCustomWidget() {
            const iframe = document.getElementById('iframe') as any
            const iframeDocument = iframe.contentWindow.document
            iframeDocument.body.innerHTML = `<html>
                <head></head>
                <body>
                    <div id="containerElement">
                    </div>
                </body>
            </html>`

            const containerElement = iframeDocument.getElementById('containerElement')
            console.log('--------- containerElement: ', containerElement)
            this.createWrapperDiv(containerElement)
            this.insertUsersHtmlContent(iframeDocument)
            this.insertUsersCssContent(iframeDocument)
            iframe.contentWindow.datastore = this.datastore
            this.createScriptTagFromUsersJSScript(iframeDocument)

            const userImportScript = document.createElement('script')
            userImportScript.setAttribute('src', 'https://code.highcharts.com/highcharts.js')

            iframeDocument.body.appendChild(userImportScript)

            // bla

            // const userScript = document.createElement('script')
            // userScript.text = `console.log('------- WINDOW PARENT: ', window.parent)
            //     window.parent.postMessage('message', '*')
            // `
            // // userScript.setAttribute('src', iframeScript)
            //setTimeout(() => iframeDocument.body.appendChild(userScript), 10000)
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
        insertUsersHtmlContent(iframeDocument: any) {
            const tempEl = iframeDocument.querySelector('.component-wrapper')
            if (tempEl) tempEl.innerHTML = this.htmlContent
        },
        insertUsersCssContent(iframeDocument: any) {
            const tempEl = iframeDocument.querySelector('.style-wrapper')
            if (tempEl) tempEl.innerHTML = this.webComponentCss
        },
        createScriptTagFromUsersJSScript(iframeDocument: any) {
            const userScript = document.createElement('script')
            userScript.text = 'try {' + this.webComponentJs + `} catch (error) {      window.parent.postMessage({type: 'error', error: error}, '*')}`
            setTimeout(() => iframeDocument.body.appendChild(userScript), 10000)
        },
        createUserImportScripts(scriptURLs: string[]) {
            scriptURLs.forEach((scriptURL: string) => this.createUserImportScript(scriptURL))
        },
        createUserImportScript(scriptURL: string) {
            let loaded = false
            const userImportScript = document.createElement('script')
            userImportScript.setAttribute('src', scriptURL)
            userImportScript.addEventListener('load', () => {
                console.log('SCRIPT LOADED!')
                loaded = true
                this.loadedScriptsCount++
            })

            document.body.appendChild(userImportScript)
            // while (true) {
            //     if (loaded) break
            //     setTimeout(() => console.log('-------- not loaded YET'), 1000)
            // }
        }
        //#endregion
        //#region ===================== WEB COMOPONENT Logic ====================================================
        // renderCustomWidget() {
        //     this.loadedScriptsCount = 0
        //     const containerElement = document.getElementById('containerElement')
        //     console.log('----------- CONTAINER ELEMENT: ', containerElement)
        //     if (!containerElement) return

        //     this.createWrapperDiv(containerElement)
        //     this.insertUsersHtmlContent()
        //     this.insertUsersCssContent()
        //     this.createUserImportScripts(['https://code.highcharts.com/highcharts.js', 'https://code.highcharts.com/modules/drilldown.js'])
        //     console.log('-------------- loadedScriptsCount: ', this.loadedScriptsCount)
        //     this.createScriptTagFromUsersJSScript()
        // },
        // createWrapperDiv(containerElement: Element) {
        //     const style = document.createElement('style')
        //     style.classList.add('style-wrapper')

        //     const wrapper = document.createElement('div')
        //     wrapper.classList.add('component-wrapper')
        //     wrapper.style.position = 'relative'
        //     wrapper.style.overflow = 'auto'
        //     wrapper.style.height = '100%'

        //     wrapper.textContent = ''
        //     containerElement.appendChild(style)
        //     containerElement.appendChild(wrapper)
        // },
        // insertUsersHtmlContent() {
        //     const tempEl = document.querySelector('.component-wrapper')
        //     if (tempEl) tempEl.innerHTML = this.htmlContent
        // },
        // insertUsersCssContent() {
        //     const tempEl = document.querySelector('.style-wrapper')
        //     if (tempEl) tempEl.innerHTML = this.webComponentCss
        // },
        // createScriptTagFromUsersJSScript() {
        //     const userScript = document.createElement('script')
        //     userScript.text = 'try {' + this.webComponentJs + '} catch (error) {}'
        //     setTimeout(() => document.body.appendChild(userScript), 10000)
        // },
        // createUserImportScripts(scriptURLs: string[]) {
        //     scriptURLs.forEach((scriptURL: string) => this.createUserImportScript(scriptURL))
        // },
        // createUserImportScript(scriptURL: string) {
        //     let loaded = false
        //     const userImportScript = document.createElement('script')
        //     userImportScript.setAttribute('src', scriptURL)
        //     userImportScript.addEventListener('load', () => {
        //         console.log('SCRIPT LOADED!')
        //         loaded = true
        //         this.loadedScriptsCount++
        //     })

        //     document.body.appendChild(userImportScript)
        //     while (true) {
        //         if (loaded) break
        //         setTimeout(() => console.log('-------- not loaded YET'), 1000)
        //     }
        // }
        //#endregion
    }
})
</script>

<style lang="scss" scoped>
.widget-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
    flex: 1;
    .widget-container-renderer {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
}
</style>
