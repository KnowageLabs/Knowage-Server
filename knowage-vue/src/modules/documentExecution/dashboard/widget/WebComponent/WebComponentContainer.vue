<template>
    <div>
        <widget-web-component ref="webComponent"></widget-web-component>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IVariable } from '../../Dashboard'
import './component/WidgetWebComponent'
import { mapActions } from 'pinia'
import store from '../../Dashboard.store'
import { IWidget } from '../../Dashboard'
import { parseHtml, parseText } from '../WidgetEditor/helpers/htmlParser/ParserHelper'

export default defineComponent({
    name: 'widget-component-container',
    emits: ['interaction', 'pageChanged', 'launchSelection', 'sortingChanged'],
    components: {},
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        widgetData: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        drivers: { type: Array, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        editorMode: { type: Boolean }
    },
    data() {
        return {
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string,
            textModel: '' as string
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
    created() {
        this.loadActiveSelections()
        this.loadDataToShow()
    },
    methods: {
        ...mapActions(store, ['getInternationalization']),
        async loadDataToShow() {
            this.dataToShow = this.widgetData
            await this.loadHTML()
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        async loadHTML() {
            if (this.propWidget.type !== 'html' && this.propWidget.type !== 'text') return
            let temp = {} as any
            if (this.propWidget.type === 'html') {
                temp = parseHtml(this.propWidget, this.drivers, this.variables, this.activeSelections, this.getInternationalization(), this.dataToShow)
                this.htmlContent = temp.html
                this.webComponentCss = temp.css
            } else {
                this.textModel = parseText(this.propWidget, this.drivers, this.variables, this.activeSelections, this.getInternationalization())
            }

            console.log('TEEEEEEEEEST: ', this.$refs)
            const webComponentRef = this.$refs.webComponent as any
            webComponentRef.htmlContent = this.htmlContent
            webComponentRef.webComponentCss = this.webComponentCss
            webComponentRef.addEventListener('selectEvent', this.onSelect)
        },
        onSelect(event: any) {
            console.log('>>>>>>>>>>>>>>>>>>>>> ON SELECT CAAAALED: ', event)
        }
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
