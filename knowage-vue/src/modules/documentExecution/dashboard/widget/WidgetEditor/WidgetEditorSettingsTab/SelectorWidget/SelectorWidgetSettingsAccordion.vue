<template>
    <div v-show="widgetModel">
        <Accordion class="selectorAccordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index" :disabled="accordion.type === 'LabelStyle' && labelStyleAccordionDisabled">
                <template #header>
                    <SelectorWidgetSettingsAccordionHeader :widgetModel="widgetModel" :title="accordion.title" :type="accordion.type"></SelectorWidgetSettingsAccordionHeader>
                </template>
                <SelectorWidgetType v-if="accordion.type === 'SelectorType'" :widgetModel="widgetModel"></SelectorWidgetType>
                <SelectorWidgetDefaultValues v-else-if="accordion.type === 'DefaultValues'" :widgetModel="widgetModel"></SelectorWidgetDefaultValues>
                <SelectorWidgetValuesManagement v-else-if="accordion.type === 'ValuesManagement'" :widgetModel="widgetModel"></SelectorWidgetValuesManagement>
                <WidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <SelectorWidgetLabelStyle v-else-if="accordion.type === 'LabelStyle'" :widgetModel="widgetModel"></SelectorWidgetLabelStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './SelectorWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import SelectorWidgetType from './configuration/SelectorWidgetType.vue'
import SelectorWidgetDefaultValues from './configuration/SelectorWidgetDefaultValues.vue'
import SelectorWidgetValuesManagement from './configuration/SelectorWidgetValuesManagement.vue'
import WidgetExport from '../common/configuration/WidgetExport.vue'
import WidgetTitleStyle from '../common/style/WidgetTitleStyle.vue'
import SelectorWidgetLabelStyle from './style/SelectorWidgetLabelStyle.vue'
import WidgetBackgroundColorStyle from '../common/style/WidgetBackgroundColorStyle.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../common/responsive/WidgetResponsive.vue'
import SelectorWidgetSettingsAccordionHeader from './SelectorWidgetSettingsAccordionHeader.vue'

export default defineComponent({
    name: 'selector-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        SelectorWidgetType,
        SelectorWidgetDefaultValues,
        SelectorWidgetValuesManagement,
        WidgetExport,
        WidgetTitleStyle,
        SelectorWidgetLabelStyle,
        WidgetBackgroundColorStyle,
        WidgetPaddingStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        SelectorWidgetSettingsAccordionHeader
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]> }
    },
    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
        }
    },
    computed: {
        labelStyleAccordionDisabled(): boolean {
            return !this.widgetModel || this.widgetModel.settings?.isDateType
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
.selectorAccordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
    .p-accordion-content {
        display: flex;
    }
}
</style>
