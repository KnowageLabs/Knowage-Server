<template>
    <div v-show="widgetModel">
        <Accordion class="selectorAccordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                </template>
                <SelectorWidgetType v-if="accordion.type === 'SelectorType'" :widgetModel="widgetModel"></SelectorWidgetType>
                <SelectorWidgetDefaultValues v-else-if="accordion.type === 'DefaultValues'" :widgetModel="widgetModel"></SelectorWidgetDefaultValues>
                <SelectorWidgetValuesManagement v-else-if="accordion.type === 'ValuesManagement'" :widgetModel="widgetModel"></SelectorWidgetValuesManagement>
                <WidgetHeaders v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetHeaders>
                <SelectorWidgetLabelStyle v-else-if="accordion.type === 'LabelStyle'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></SelectorWidgetLabelStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
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
import WidgetHeaders from '../common/style/WidgetHeaders.vue'
import SelectorWidgetLabelStyle from './style/SelectorWidgetLabelStyle.vue'
import WidgetBackgroundColorStyle from '../common/style/WidgetBackgroundColorStyle.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'

export default defineComponent({
    name: 'selector-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        SelectorWidgetType,
        SelectorWidgetDefaultValues,
        SelectorWidgetValuesManagement,
        WidgetHeaders,
        SelectorWidgetLabelStyle,
        WidgetBackgroundColorStyle,
        WidgetPaddingStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        variables: { type: Array as PropType<IVariable[]> }
    },
    watch: {
        settings() {
            this.activeIndex = -1
        }
    },
    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
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

.dynamic-form-item {
    border-bottom: 1px solid #c2c2c2;
}

.dynamic-form-item:last-child {
    border-bottom: none;
}
</style>
