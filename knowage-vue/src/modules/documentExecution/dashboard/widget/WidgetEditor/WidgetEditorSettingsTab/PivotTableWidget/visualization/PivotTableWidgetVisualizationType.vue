<template>
    <div v-if="visualizationTypeModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-for="(visualizationType, index) in visualizationTypeModel.types" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-col-12">
                {{ visualizationType }}
            </div>
            <div class="p-col-12 p-grid p-ai-center">
                <div class="p-col-12 p-md-7 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <Dropdown v-if="index === 0" v-model="visualizationType.target" class="kn-material-input" :options="descriptor.allColumnOption" option-value="value" option-label="label" :disabled="true"> </Dropdown>
                    <WidgetEditorColumnsMultiselect
                        v-else
                        :value="(visualizationType.target as string[])"
                        :available-target-options="availableColumnOptions"
                        :widget-columns-alias-map="widgetColumnsAliasMap"
                        option-label="alias"
                        option-value="id"
                        :disabled="visualizationTypeDisabled"
                        @change="onColumnsSelected($event, visualizationType)"
                    >
                    </WidgetEditorColumnsMultiselect>
                </div>
                <div class="p-col-12 p-md-2 p-d-flex p-flex-column">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.prefix') }}</label>
                    <InputText v-model="visualizationType.prefix" class="kn-material-input p-inputtext-sm" :disabled="visualizationTypeDisabled" />
                </div>
                <div class="p-col-11 p-md-2 p-d-flex p-flex-column">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.suffix') }}</label>
                    <InputText v-model="visualizationType.suffix" class="kn-material-input p-inputtext-sm" :disabled="visualizationTypeDisabled" />
                </div>
                <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', visualizationTypeDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisualizationType() : removeVisualizationType(index)"></i>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IPivotTableWidgetVisualizationType, IPivotTableWidgetVisualizationTypes } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import descriptor from '../PivotTableSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'

export default defineComponent({
    name: 'pivot-table-widget-visualization-type',
    components: { Dropdown, WidgetEditorColumnsMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            visualizationTypeModel: null as IPivotTableWidgetVisualizationTypes | null,
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any
        }
    },
    computed: {
        visualizationTypeDisabled() {
            return !this.visualizationTypeModel || !this.visualizationTypeModel.enabled
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadVisualizationTypes()
        this.loadWidgetColumnMaps()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemovedFromVisualizationTypes', this.onColumnRemoved)
            emitter.on('columnRemoved', this.loadColumnOptions)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromVisualizationTypes', this.onColumnRemoved)
            emitter.off('columnRemoved', this.loadColumnOptions)
        },
        loadVisualizationTypes() {
            if (this.widgetModel.settings?.visualization?.visualizationTypes) this.visualizationTypeModel = this.widgetModel.settings.visualization.visualizationTypes
            this.removeColumnsFromAvailableOptions()
        },
        loadColumnOptions() {
            this.availableColumnOptions = this.widgetModel.fields ? [...this.widgetModel.fields.data] : []
            this.removeColumnsFromAvailableOptions()
        },
        loadWidgetColumnMaps() {
            this.widgetModel.fields?.data?.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
            })
        },
        removeColumnsFromAvailableOptions() {
            for (let i = 0; i < this.widgetModel.settings.visualization.visualizationTypes.types.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.visualization.visualizationTypes.types[i].target.length; j++) {
                    this.removeColumnFromAvailableOptions({ id: this.widgetModel.settings.visualization.visualizationTypes.types[i].target[j], alias: this.widgetModel.settings.visualization.visualizationTypes.types[i].target[j] })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        onColumnsSelected(event: any, visualizationType: IPivotTableWidgetVisualizationType) {
            const intersection = (visualizationType.target as string[]).filter((el: string) => !event.value.includes(el))
            visualizationType.target = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(visualizationType)
        },
        onColumnsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) => this.availableColumnOptions.push({ id: el, alias: this.widgetColumnsAliasMap[el] }))
        },
        onColumnsAddedFromMultiselect(visualizationType: IPivotTableWidgetVisualizationType) {
            ;(visualizationType.target as string[]).forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        addVisualizationType() {
            if (this.visualizationTypeModel && !this.visualizationTypeDisabled) this.visualizationTypeModel.types.push({ target: [], prefix: '', suffix: '' })
        },
        removeVisualizationType(index: number) {
            if (!this.visualizationTypeModel || this.visualizationTypeDisabled) return
            ;(this.visualizationTypeModel.types[index].target as string[]).forEach((target: string) => this.availableColumnOptions.push({ id: target, alias: this.widgetColumnsAliasMap[target] }))
            this.visualizationTypeModel.types.splice(index, 1)
        },
        onColumnRemoved() {
            this.loadColumnOptions()
            this.loadVisualizationTypes()
        }
    }
})
</script>
