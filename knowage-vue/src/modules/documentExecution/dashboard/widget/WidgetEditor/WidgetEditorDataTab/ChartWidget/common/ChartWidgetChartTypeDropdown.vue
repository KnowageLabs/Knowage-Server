<template>
    <div v-if="widgetModel">
        <div class="p-d-flex p-flex-row p-ai-center">
            <Dropdown class="kn-material-input kn-flex" v-model="selectedType" :options="commonDescriptor.chartTypeOptions" optionValue="value" @change="onChange">
                <template #value="slotProps">
                    <div>
                        <span>{{ slotProps.value }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <!-- <div :style="{ 'background-image': 'url(./images/dashboard/chartTypes/' + slotProps.option.value + '.png' + ')' }"></div> -->
                        <div :style="getImageSource(slotProps.option.data)"></div>
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
            console.log('............. this model: ', this.widgetModel)
            const chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            if (chartModel?.chart.type) {
                this.selectedType = chartModel.chart.type
            }
        },
        getImageSource(chartValue: string) {
            // return { 'background-image': 'url(../../../images/dashboard/chartTypes/' + slotProps.option.value + '.png' + ')' }
            // return { 'background-image': `url(${require('@/assets/images/dashboard/chartTypes/' + chartValue)})` }
            //  return { 'background-image': `url(..../images/functionCatalog/' + ${chartValue} + '.png' + ')` }
            return ''
        },
        onChange() {
            this.$emit('selectedChartTypeChanged', this.selectedType)
        }
    }
})
</script>
