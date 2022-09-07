<template>
    <div>
        {{ visualizationTypes }}
        <div v-for="(visualizationType, index) in visualizationTypes" :key="index" class="p-d-flex p-flex-row p-ai-center">
            <div class="p-d-flex p-flex-column p-m-3">
                <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                <MultiSelect v-model="visualizationType.target" :options="widgetModel.columns" optionLabel="alias" optionValue="id" @change="onColumnsSelectedChange"> </MultiSelect>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="visualizationType.type" :options="getVisualizationTypeOptions(visualizationType)" optionValue="value" @change="onTypeChanged(visualizationType)">
                    <template #value="slotProps">
                        <div>
                            <span>{{ slotProps.value }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>

            <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisualizationType() : removeVisualizationType(index)"></i>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetVisualizationType, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visualization-type',
    components: { Dropdown, InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            visualizationTypes: [] as ITableWidgetVisualizationType[],
            columnOptions: [] as IWidgetColumn[]
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadVisualizationTypes()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemoved', (column) => this.onColumnRemoved(column))
        },
        visualizationTypeChanged() {
            emitter.emit('visualizationTypeChanged', this.visualizationTypes)
        },
        loadVisualizationTypes() {
            console.log(' ----- loadVisualizationTypes - model: ', this.widgetModel)
            if (this.widgetModel.settings?.visualization?.types) this.visualizationTypes = [...this.widgetModel.settings.visualization.types]
        },
        loadColumnOptions() {
            this.columnOptions = this.widgetModel.columns
        },
        onColumnsSelectedChange() {},
        getVisualizationTypeOptions(visualizationType: ITableWidgetVisualizationType) {
            return this.optionsContainMeasureColumn(visualizationType) ? descriptor.visualizationTypes : descriptor.visualizationTypes.slice(0)
        },
        optionsContainMeasureColumn(visualizationType: ITableWidgetVisualizationType) {
            let found = true

            return found
        },
        onTypeChanged(visualizationType: ITableWidgetVisualizationType) {},
        addVisualizationType() {},
        removeVisualizationType(index: number) {},
        onColumnRemoved(column: IWidgetColumn) {
            console.log('ON COLUMN REMOVED: ', column)
        }
    }
})
</script>
