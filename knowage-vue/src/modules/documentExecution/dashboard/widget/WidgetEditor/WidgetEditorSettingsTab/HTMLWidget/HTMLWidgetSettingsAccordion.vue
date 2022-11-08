<template>
    <div v-show="widgetModel">
        <Accordion class="selectorAccordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                </template>
                <WidgetExport v-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
                <WidgetHtmlEditor v-else-if="accordion.type === 'HTML'" :activeIndex="activeIndex" :widgetModel="widgetModel" :drivers="drivers"></WidgetHtmlEditor>
                <WidgetCssEditor v-else-if="accordion.type === 'CSS'" :widgetModel="widgetModel"></WidgetCssEditor>
                <WidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets"></WidgetCrossNavigation>
                <WidgetInteractionsLinks v-else-if="accordion.type === 'Link'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers"></WidgetInteractionsLinks>
                <WidgetPreview v-else-if="accordion.type === 'Preview'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :dashboardId="dashboardId"></WidgetPreview>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './HTMLWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import WidgetExport from '../common/configuration/WidgetExport.vue'
import WidgetTitleStyle from '../common/style/WidgetTitleStyle.vue'
import WidgetBackgroundColorStyle from '../common/style/WidgetBackgroundColorStyle.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../common/responsive/WidgetResponsive.vue'
import WidgetHtmlEditor from './editor/WidgetHtmlEditor.vue'
import WidgetCssEditor from './editor/WidgetCssEditor.vue'
import WidgetCrossNavigation from '../common/interactions/crossNavigation/WidgetCrossNavigation.vue'
import WidgetInteractionsLinks from '../common/interactions/link/WidgetInteractionsLinks.vue'
import WidgetPreview from '../common/interactions/preview/WidgetPreview.vue'

export default defineComponent({
    name: 'html-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        WidgetExport,
        WidgetTitleStyle,
        WidgetBackgroundColorStyle,
        WidgetPaddingStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        WidgetHtmlEditor,
        WidgetCssEditor,
        WidgetCrossNavigation,
        WidgetInteractionsLinks,
        WidgetPreview
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array, required: true },
        variables: { type: Array as PropType<IVariable[]> },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
        }
    },
    watch: {
        settings() {
            this.activeIndex = -1
        }
    },
    created() {},
    methods: {}
})
</script>

<style lang="scss">
.selectorAccordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
    .p-accordion-content {
        display: flex;
    }
}
</style>
