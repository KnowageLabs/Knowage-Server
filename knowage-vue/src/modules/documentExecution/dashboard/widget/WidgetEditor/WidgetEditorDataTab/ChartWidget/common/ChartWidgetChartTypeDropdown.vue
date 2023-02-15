<template>
    <div v-if="widgetModel">
        <div class="p-d-flex p-flex-row p-ai-center p-p-4">
            <Dropdown v-model="selectedType" class="kn-material-input kn-flex" :options="commonDescriptor.chartTypeOptions" option-disabled="disabled" option-value="value" @change="onChange">
                <template #value="slotProps">
                    <div class="p-d-flex p-flex-row p-ai-center">
                        <img class="chart-type-image p-mr-2" :src="getImageSource(slotProps.value)" />
                        <span>{{ slotProps.value }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div class="p-d-flex p-flex-row p-ai-center">
                        <img class="chart-type-image p-mr-2" :src="getImageSource(slotProps.option.value)" />
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'chart-widget-chart-type-dropdown',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    emits: ['selectedChartTypeChanged'],
    data() {
        return {
            commonDescriptor,
            selectedType: ''
        }
    },
    async created() {
        this.loadSelectedType()
    },
    methods: {
        loadSelectedType() {
            const chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (chartModel?.chart.type) {
                this.selectedType = chartModel.chart.type
            }
        },
        getImageSource(chartValue: string) {
            return `${import.meta.env.VITE_PUBLIC_PATH}images/dashboard/chartTypes/${chartValue}.png`
        },
        onChange() {
            this.$emit('selectedChartTypeChanged', this.selectedType)
        }
    }
})
</script>

<style lang="scss" scoped>
.chart-type-image {
    width: 20px;
    height: 20px;
}
</style>
