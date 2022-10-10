<template>
    <div v-if="visualizationTypeModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-px-2 p-pb-4">
            <InputSwitch v-model="visualizationTypeModel.enabled" @change="visualizationTypeChanged"></InputSwitch>
            <label class="kn-material-input-label p-ml-3">{{ $t('common.enable') }}</label>
        </div>
        <div v-for="(visualizationType, index) in visualizationTypeModel.types" :key="index" class="dynamic-form-item p-grid p-col-12 p-ai-center p-pt-4">
            <div class="p-col-12 p-grid p-ai-center">
                <div class="p-col-12 p-md-6 p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label"> {{ $t('common.columns') }}</label>
                    <Dropdown v-if="index === 0" class="kn-material-input" v-model="visualizationType.target" :options="descriptor.allColumnOption" optionValue="value" optionLabel="label" :disabled="true"> </Dropdown>
                    <WidgetEditorColumnsMultiselect
                        v-else
                        :value="(visualizationType.target as string[])"
                        :availableTargetOptions="availableColumnOptions"
                        :widgetColumnsAliasMap="widgetColumnsAliasMap"
                        optionLabel="alias"
                        optionValue="id"
                        :disabled="visualizationTypeDisabled"
                        @change="onColumnsSelected($event, visualizationType)"
                    >
                    </WidgetEditorColumnsMultiselect>
                </div>
                <div class="p-col-11 p-md-5 p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.type') }}</label>
                    <Dropdown class="kn-material-input" v-model="visualizationType.type" :options="getVisualizationTypeOptions(visualizationType)" optionValue="value" :disabled="visualizationTypeDisabled" @change="visualizationTypeChanged">
                        <template #value="slotProps">
                            <div>
                                <span>{{ getTranslatedLabel(slotProps.value, getVisualizationTypeOptions(visualizationType), $t) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>
                <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                    <i :class="[index === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', visualizationTypeDisabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="index === 0 ? addVisualizationType() : removeVisualizationType(index)"></i>
                </div>
            </div>
            <div class="p-col-12 p-grid p-ai-center p-mt-1">
                <div class="p-col-6 p-md-3 p-d-flex p-flex-column p-pr-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.prefix') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="visualizationType.prefix" :disabled="visualizationTypeDisabled" @change="visualizationTypeChanged" />
                </div>
                <div class="p-col-6 p-md-3 p-d-flex p-flex-column kn-flex p-pr-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.suffix') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="visualizationType.suffix" :disabled="visualizationTypeDisabled" @change="visualizationTypeChanged" />
                </div>
                <div v-if="(optionsContainMeasureColumn(visualizationType) && visualizationType.type === 'Text') || visualizationType.type === 'Text & Icon'" class="p-col-6 p-md-3 p-d-flex p-flex-column p-px-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.precision') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.precision" :disabled="visualizationTypeDisabled" @blur="visualizationTypeChanged" />
                </div>
                <div class="p-col-6 p-md-3 p-d-flex p-flex-column kn-flex p-p-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.visualizationType.pinned') }}</label>
                    <Dropdown class="kn-material-input" v-model="visualizationType.pinned" :options="descriptor.pinnedOptions" optionValue="value" :disabled="visualizationTypeDisabled" @change="visualizationTypeChanged">
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
            <div v-if="optionsContainMeasureColumn(visualizationType) && (visualizationType.type === 'Bar' || visualizationType.type === 'Sparkline')" class="p-col-12 p-grid p-ai-center p-pt-1">
                <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-px-2">
                    <label class="kn-material-input-label">{{ $t('common.min') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.min" :disabled="visualizationTypeDisabled" @blur="visualizationTypeChanged" />
                </div>
                <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column p-px-2">
                    <label class="kn-material-input-label">{{ $t('common.max') }}</label>
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="visualizationType.max" :disabled="visualizationTypeDisabled" @blur="visualizationTypeChanged" />
                </div>

                <div class="p-col-6 p-md-6 p-lg-3 p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.visualizationType.alignment') }}</label>
                    <Dropdown class="kn-material-input" v-model="visualizationType.alignment" :options="descriptor.alignmentOptions" optionValue="value" :disabled="visualizationTypeDisabled" @change="visualizationTypeChanged">
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
                <div class="p-col-6 p-md-6 p-lg-3 style-toolbar-container">
                    <WidgetEditorStyleToolbar
                        :options="descriptor.styleToolbarVisualizationTypeOptions"
                        :propModel="{
                            color: visualizationType.color,
                            'background-color': visualizationType['background-color']
                        }"
                        :disabled="visualizationTypeDisabled"
                        @change="onStyleToolbarChange($event, visualizationType)"
                    ></WidgetEditorStyleToolbar>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetVisualizationType, IWidgetColumn, IWidgetStyleToolbarModel, ITableWidgetVisualizationTypes } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColumnsMultiselect from '../../common/WidgetEditorColumnsMultiselect.vue'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-visualization-type',
    components: {
        Dropdown,
        InputNumber,
        InputSwitch,
        WidgetEditorColumnsMultiselect,
        WidgetEditorStyleToolbar
    },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            visualizationTypeModel: null as ITableWidgetVisualizationTypes | null,
            availableColumnOptions: [] as (IWidgetColumn | { id: string; alias: string })[],
            widgetColumnsAliasMap: {} as any,
            widgetColumnsTypeMap: {} as any,
            getTranslatedLabel
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
            emitter.on('columnRemovedFromVisibilityTypes', this.onColumnRemovedFromVisibilityTypes)
            emitter.on('columnAliasRenamed', this.onColumnAliasRenamed)
            emitter.on('columnAdded', this.onColumnAdded)
        },
        removeEventListeners() {
            emitter.off('columnRemovedFromVisibilityTypes', this.onColumnRemovedFromVisibilityTypes)
            emitter.off('columnAliasRenamed', this.onColumnAliasRenamed)
            emitter.off('columnAdded', this.onColumnAdded)
        },
        onColumnRemovedFromVisibilityTypes() {
            this.onColumnRemoved()
        },
        onColumnAliasRenamed(column: any) {
            this.updateColumnAliases(column)
        },
        onColumnAdded(column: any) {
            this.addColumnAsOption(column)
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
            emitter.emit('visualizationTypeChanged', this.visualizationTypeModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        loadVisualizationTypes() {
            if (this.widgetModel.settings?.visualization?.visualizationTypes) this.visualizationTypeModel = this.widgetModel.settings.visualization.visualizationTypes
            this.removeColumnsFromAvailableOptions()
        },
        removeColumnsFromAvailableOptions() {
            for (let i = 0; i < this.widgetModel.settings.visualization.visualizationTypes.types.length; i++) {
                for (let j = 0; j < this.widgetModel.settings.visualization.visualizationTypes.types[i].target.length; j++) {
                    this.removeColumnFromAvailableOptions({
                        id: this.widgetModel.settings.visualization.visualizationTypes.types[i].target[j],
                        alias: this.widgetModel.settings.visualization.visualizationTypes.types[i].target[j]
                    })
                }
            }
        },
        removeColumnFromAvailableOptions(tempColumn: IWidgetColumn | { id: string; alias: string }) {
            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === tempColumn.id)
            if (index !== -1) this.availableColumnOptions.splice(index, 1)
        },
        onColumnsSelected(event: any, visualizationType: ITableWidgetVisualizationType) {
            const intersection = (visualizationType.target as string[]).filter((el: string) => !event.value.includes(el))
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
            ;(visualizationType.target as string[]).forEach((target: string) => {
                const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === target)
                if (index !== -1) this.availableColumnOptions.splice(index, 1)
            })
        },
        getVisualizationTypeOptions(visualizationType: ITableWidgetVisualizationType) {
            return this.optionsContainMeasureColumn(visualizationType) ? descriptor.visualizationTypes : descriptor.visualizationTypes.slice(0, 3)
        },
        optionsContainMeasureColumn(visualizationType: ITableWidgetVisualizationType) {
            let found = false
            for (let i = 0; i < visualizationType.target.length; i++) {
                if (this.widgetColumnsTypeMap[visualizationType.target[i]] === 'MEASURE') {
                    found = true
                    break
                }
            }
            if (!found && (visualizationType.type === 'Bar' || visualizationType.type === 'Sparkline')) this.resetMeasureProperties(visualizationType)
            return found
        },
        resetMeasureProperties(visualizationType: ITableWidgetVisualizationType) {
            visualizationType.type = 'Text'
            const fields = ['min', 'max', 'alignment']
            fields.forEach((field: string) => delete visualizationType[field])
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel, visualizationType: ITableWidgetVisualizationType) {
            visualizationType.color = model.color
            visualizationType['background-color'] = model['background-color'] ?? ''
            this.visualizationTypeChanged()
        },
        addVisualizationType() {
            if (this.visualizationTypeModel && !this.visualizationTypeDisabled)
                this.visualizationTypeModel.types.push({
                    target: [],
                    type: '',
                    prefix: '',
                    suffix: '',
                    pinned: ''
                })
        },
        removeVisualizationType(index: number) {
            if (!this.visualizationTypeModel || this.visualizationTypeDisabled) return
            ;(this.visualizationTypeModel.types[index].target as string[]).forEach((target: string) =>
                this.availableColumnOptions.push({
                    id: target,
                    alias: this.widgetColumnsAliasMap[target]
                })
            )
            this.visualizationTypeModel.types.splice(index, 1)
            this.visualizationTypeChanged()
        },
        onColumnRemoved() {
            this.loadColumnOptions()
            this.loadVisualizationTypes()
        },
        updateColumnAliases(column: IWidgetColumn) {
            if (column.id && this.widgetColumnsAliasMap[column.id]) this.widgetColumnsAliasMap[column.id] = column.alias

            const index = this.availableColumnOptions.findIndex((targetOption: IWidgetColumn | { id: string; alias: string }) => targetOption.id === column.id)
            if (index !== -1) this.availableColumnOptions[index].alias = column.alias
            this.visualizationTypeChanged()
        },
        addColumnAsOption(column: IWidgetColumn) {
            this.availableColumnOptions.push(column)
            if (column.id) this.widgetColumnsAliasMap[column.id] = column.alias
        }
    }
})
</script>

<style lang="scss" scoped>
.visualization-type-container {
    border-bottom: 1px solid #c2c2c2;
}

.visualization-type-containerr:last-child {
    border-bottom: none;
}
.style-toolbar-container {
    max-width: 120px;
}
</style>
