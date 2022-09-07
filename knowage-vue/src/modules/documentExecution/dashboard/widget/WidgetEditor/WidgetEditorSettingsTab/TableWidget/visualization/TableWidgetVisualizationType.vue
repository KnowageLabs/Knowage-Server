<template>
    <div>
        {{ visualizationTypes }}
        <div v-for="(visualizationType, index) in visualizationTypes" :key="index" class="p-d-flex p-flex-column p-mt-2">
            <div class="p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <TableWidgetVisualizationTypeMultiselect :value="(visualizationType.target as string[])" :availableTargetOptions="availableColumnOptions" :widgetColumnsAliasMap="widgetColumnsAliasMap" optionLabel="alias" optionValue="id" @change="onColumnsSelected($event, visualizationType)">
                    </TableWidgetVisualizationTypeMultiselect>
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
            <div class="p-d-flex p-flex-row p-ai-center kn-flex p-mt-1">
                <div class="p-d-flex p-flex-column kn-flex p-mr-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.prefix') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="visualizationType.prefix" @change="visualizationTypeChanged" />
                </div>
                <div class="p-d-flex p-flex-column kn-flex p-mr-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.suffix') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="visualizationType.suffix" @change="visualizationTypeChanged" />
                </div>
                <div v-if="optionsContainMeasureColumn(visualizationType)" class="p-d-flex p-flex-column p-mx-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.precision') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.precision" @change="visualizationTypeChanged" />
                </div>
                <div v-if="optionsContainMeasureColumn(visualizationType)" class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.visualizationType.pinned') }}</label>
                    <Dropdown class="kn-material-input" v-model="visualizationType.pinned" :options="descriptor.pinnedOptions" optionValue="value" @change="visualizationTypeChanged">
                        <template #value="slotProps">
                            <div>
                                <span>{{ getTranslatedLabel(slotProps.value, descriptor.pinnedOptions, $t) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>
            </div>
            <div class="p-d-flex p-flex-row p-ai-center kn-flex p-mt-1">
                <div class="p-d-flex p-flex-column p-mx-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.min') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.min" @change="visualizationTypeChanged" />
                </div>
                <div class="p-d-flex p-flex-column p-mx-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.max') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.max" @change="visualizationTypeChanged" />
                </div>

                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.visualizationType.alignment') }}</label>
                    <Dropdown class="kn-material-input" v-model="visualizationType.alignment" :options="descriptor.alignmentOptions" optionValue="value" @change="visualizationTypeChanged">
                        <template #value="slotProps">
                            <div>
                                <span>{{ getTranslatedLabel(slotProps.value, descriptor.alignmentOptions, $t) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>
            </div>

            <hr />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetVisualizationType, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputNumber from 'primevue/inputnumber'
import TableWidgetVisualizationTypeMultiselect from './TableWidgetVisualizationTypeMultiselect.vue'

export default defineComponent({
    name: 'table-widget-visualization-type',
    components: { Dropdown, InputSwitch, InputNumber, TableWidgetVisualizationTypeMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            visualizationTypes: [] as ITableWidgetVisualizationType[],
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            widgetColumnsTypeMap: {} as any,
            getTranslatedLabel
        }
    },
    created() {
        this.setEventListeners()
        this.loadColumnOptions()
        this.loadVisualizationTypes()
        this.loadWidgetColumnMaps()
    },
    methods: {
        setEventListeners() {
            emitter.on('columnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadColumnOptions() {
            this.availableColumnOptions = [...this.widgetModel.columns]
        },
        loadWidgetColumnMaps() {
            this.widgetModel.columns.forEach((column: IWidgetColumn) => {
                if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
                if (column.id && column.fieldType) this.widgetColumnsTypeMap[column.id] = column.fieldType
            })
        },
        visualizationTypeChanged() {
            emitter.emit('visualizationTypeChanged', this.visualizationTypes)
        },
        loadVisualizationTypes() {
            console.log(' ----- loadVisualizationTypes - model: ', this.widgetModel)
            if (this.widgetModel.settings?.visualization?.types) this.visualizationTypes = [...this.widgetModel.settings.visualization.types]
            this.removeColumnsFromAvailableOptions()
        },
        removeColumnsFromAvailableOptions() {
            for (let i = 0; i < this.widgetModel.settings.visualization.types.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.visualization.types[i].target.length; j++) {
                    this.removeColumnFromAvailableOptions({ id: this.widgetModel.settings.visualization.types[i].target[j], alias: this.widgetModel.settings.visualization.types[i].target[j] })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        onColumnsSelected(event: any, visualizationType: ITableWidgetVisualizationType) {
            const intersection = visualizationType.target.filter((el: string) => !event.value.includes(el))
            visualizationType.target = event.value
            intersection.length > 0 ? this.onColumnsRemovedFromMultiselect(intersection) : this.onColumnsAddedFromMultiselect(visualizationType)
            this.visualizationTypeChanged()
        },
        onColumnsRemovedFromMultiselect(intersection: string[]) {
            intersection.forEach((el: string) =>
                this.availableColumnOptions.push({
                    id: el,
                    alias: this.widgetColumnsAliasMap[el]
                })
            )
        },
        onColumnsAddedFromMultiselect(visualizationType: ITableWidgetVisualizationType) {
            visualizationType.target.forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        onColumnsSelectedChange() {},
        getVisualizationTypeOptions(visualizationType: ITableWidgetVisualizationType) {
            console.log('TEST: ', this.optionsContainMeasureColumn(visualizationType))
            return this.optionsContainMeasureColumn(visualizationType) ? descriptor.visualizationTypes : descriptor.visualizationTypes.slice(0, 3)
        },
        optionsContainMeasureColumn(visualizationType: ITableWidgetVisualizationType) {
            console.log('visualizationType: ', visualizationType)
            let found = false
            for (let i = 0; i < visualizationType.target.length; i++) {
                if (this.widgetColumnsTypeMap[visualizationType.target[i]] === 'MEASURE') {
                    found = true
                    break
                }
            }
            if (!found && (visualizationType.type === 'Bar' || visualizationType.type === 'Sparkline')) visualizationType.type = 'Text'
            return found
        },
        onTypeChanged(visualizationType: ITableWidgetVisualizationType) {
            console.log('onTypeChanged visualizationType: ', visualizationType)
        },
        addVisualizationType() {},
        removeVisualizationType(index: number) {},
        onColumnRemoved(column: IWidgetColumn) {
            console.log('ON COLUMN REMOVED: ', column)
        }
    }
})
</script>
