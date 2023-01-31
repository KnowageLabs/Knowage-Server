<template>
    <div v-show="widgetModel">
        <Accordion class="widget-editor-accordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <CustomChartWidgetSettingsAccordionHeader :widgetModel="widgetModel" :title="accordion.title" :type="accordion.type"></CustomChartWidgetSettingsAccordionHeader>
                </template>
                <CustomChartHTMLEditor v-if="accordion.type === 'HTML'" :activeIndex="activeIndex" :widgetModel="widgetModel"></CustomChartHTMLEditor>
                <CustomChartCSSEditor v-if="accordion.type === 'CSS'" :activeIndex="activeIndex" :widgetModel="widgetModel"></CustomChartCSSEditor>
                <CustomChartJSEditor v-if="accordion.type === 'JS'" :activeIndex="activeIndex" :widgetModel="widgetModel"></CustomChartJSEditor>
                <WidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
                <WidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :dashboardId="dashboardId"></WidgetCrossNavigation>
                <WidgetPreview v-else-if="accordion.type === 'Preview'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :dashboardId="dashboardId"></WidgetPreview>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './CustomChartWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import WidgetExport from '../common/configuration/WidgetExport.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../common/responsive/WidgetResponsive.vue'
import WidgetCrossNavigation from '../common/interactions/crossNavigation/WidgetCrossNavigation.vue'
import WidgetPreview from '../common/interactions/preview/WidgetPreview.vue'
import WidgetTitleStyle from '../common/style/WidgetTitleStyle.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'
import WidgetBackgroundColorStyle from '../common/style/WidgetBackgroundColorStyle.vue'
import CustomChartWidgetSettingsAccordionHeader from './CustomChartWidgetSettingsAccordionHeader.vue'
import CustomChartHTMLEditor from './editor/CustomChartHtmlEditor.vue'
import CustomChartCSSEditor from './editor/CustomChartCSSEditor.vue'
import CustomChartJSEditor from './editor/CustomChartJSEditor.vue'

export default defineComponent({
    name: 'custom-chart-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        WidgetExport,
        WidgetTitleStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        WidgetPaddingStyle,
        WidgetBackgroundColorStyle,
        WidgetCrossNavigation,
        WidgetPreview,
        CustomChartWidgetSettingsAccordionHeader,
        CustomChartHTMLEditor,
        CustomChartCSSEditor,
        CustomChartJSEditor
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    watch: {
        settings() {
            this.activeIndex = -1
            this.setActiveAccordion()
        }
    },
    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
        }
    },
    created() {
        this.setActiveAccordion()
    },
    methods: {
        setActiveAccordion() {
            if (!this.settings) return
            if (this.settings.length === 1) this.activeIndex = 0
            else if (this.activeIndex === -1 && this.settings.length === 3 && this.settings[1].type === 'HTML') this.activeIndex = 1
        }
    }
})
</script>

<style lang="scss">
.widget-editor-accordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
}

.p-accordion-content {
    padding: 0 !important;
}

.dynamic-form-item {
    border-bottom: 1px solid #c2c2c2;
}

.dynamic-form-item:last-child {
    border-bottom: none;
}
</style>
