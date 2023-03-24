<template>
    <div v-show="widgetModel">
        <Accordion v-model:activeIndex="activeIndex" class="widget-editor-accordion">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <VegaChartsSettingsAccordionHeader :widget-model="widgetModel" :title="accordion.title" :type="accordion.type"></VegaChartsSettingsAccordionHeader>
                </template>

                <VegaNoDataMessageConfiguration v-if="accordion.type === 'NoDataMessageConfiguration'" :widget-model="widgetModel"></VegaNoDataMessageConfiguration>
                <WidgetExport v-else-if="accordion.type === 'Export'" :widget-model="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widget-model="widgetModel" :toolbar-style-settings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <WidgetRowsStyle v-else-if="accordion.type === 'RowsStyle'" :widget-model="widgetModel"></WidgetRowsStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widget-model="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widget-model="widgetModel"></WidgetBordersStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widget-model="widgetModel"></WidgetPaddingStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widget-model="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widget-model="widgetModel"></WidgetResponsive>
                <WidgetSelection v-else-if="accordion.type === 'Selection'" :widget-model="widgetModel"></WidgetSelection>
                <WidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widget-model="widgetModel" :datasets="datasets" :selected-datasets="selectedDatasets" :dashboard-id="dashboardId"></WidgetCrossNavigation>
                <WidgetInteractionsLinks v-else-if="accordion.type === 'Link'" :widget-model="widgetModel" :datasets="datasets" :selected-datasets="selectedDatasets" :dashboard-id="dashboardId"></WidgetInteractionsLinks>
                <WidgetPreview v-else-if="accordion.type === 'Preview'" :widget-model="widgetModel" :datasets="datasets" :selected-datasets="selectedDatasets" :dashboard-id="dashboardId"></WidgetPreview>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './VegaChartsSettingsDescriptor.json'
import settingsTabDescriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import VegaNoDataMessageConfiguration from './configuration/VegaNoDataMessageConfiguration.vue'
import WidgetExport from '../../common/configuration/WidgetExport.vue'
import WidgetRowsStyle from '../../common/style/WidgetRowsStyle.vue'
import WidgetBordersStyle from '../../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../../common/responsive/WidgetResponsive.vue'
import WidgetSelection from '../../common/interactions/selection/WidgetSelection.vue'
import WidgetCrossNavigation from '../../common/interactions/crossNavigation/WidgetCrossNavigation.vue'
import WidgetInteractionsLinks from '../../common/interactions/link/WidgetInteractionsLinks.vue'
import WidgetPreview from '../../common/interactions/preview/WidgetPreview.vue'
import WidgetTitleStyle from '../../common/style/WidgetTitleStyle.vue'
import WidgetPaddingStyle from '../../common/style/WidgetPaddingStyle.vue'
import WidgetBackgroundColorStyle from '../../common/style/WidgetBackgroundColorStyle.vue'
import VegaChartsSettingsAccordionHeader from './VegaChartsSettingsAccordionHeader.vue'

export default defineComponent({
    name: 'chart-j-s-widget-configuration-container',
    components: {
        Accordion,
        AccordionTab,
        VegaNoDataMessageConfiguration,
        WidgetExport,
        WidgetTitleStyle,
        WidgetRowsStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        WidgetPaddingStyle,
        WidgetBackgroundColorStyle,
        WidgetCrossNavigation,
        WidgetInteractionsLinks,
        WidgetPreview,
        WidgetSelection,
        VegaChartsSettingsAccordionHeader
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
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
            this.setActiveAccordion()
        }
    },
    created() {
        this.setActiveAccordion()
    },
    methods: {
        setActiveAccordion() {
            if (this.settings?.length === 1) this.activeIndex = 0
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
