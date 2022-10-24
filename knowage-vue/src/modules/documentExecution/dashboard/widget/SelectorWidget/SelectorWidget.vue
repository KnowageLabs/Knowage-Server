<template>
    <div v-if="options" class="selector-widget">
        {{ showMode }}
        {{ selectedDate }}
        <div v-if="widgetType === 'singleValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of showMode === 'hideDisabled' ?  options.rows.filter((row: any) => !row.disabled) : options.rows" :key="index">
                <RadioButton :inputId="`radio-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValue" :disabled="showMode === 'showDisabled' && value.disabled" @change="singleValueSelectionChanged" />
                <label :for="`radio-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <div v-if="widgetType === 'multiValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of showMode === 'hideDisabled' ?  options.rows.filter((row: any) => !row.disabled) : options.rows" :key="index">
                <Checkbox :inputId="`multi-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValues" :disabled="showMode === 'showDisabled' && value.disabled" @change="multiValueSelectionChanged" />
                <label :for="`multi-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <span v-if="widgetType === 'dropdown'" class="p-float-label p-m-2">
            <Dropdown
                class="kn-width-full"
                panelClass="selectorCustomDropdownPanel"
                v-model="selectedValue"
                :options="showMode === 'hideDisabled' ?  options.rows.filter((row: any) => !row.disabled) : options.rows"
                optionLabel="column_1"
                optionValue="column_1"
                :style="getLabelStyle()"
                :inputStyle="getLabelStyle()"
                :panelStyle="getLabelStyle()"
                :optionDisabled="showMode === 'showDisabled' ? 'disabled' : ''"
                @change="singleValueSelectionChanged"
            />
        </span>

        <span v-if="widgetType === 'multiDropdown'" class="p-float-label p-m-2">
            <MultiSelect
                class="kn-width-full"
                panelClass="selectorCustomDropdownPanel"
                v-model="selectedValues"
                :options="showMode === 'hideDisabled' ?  options.rows.filter((row: any) => !row.disabled) : options.rows"
                optionLabel="column_1"
                optionValue="column_1"
                :style="getLabelStyle()"
                :inputStyle="getLabelStyle()"
                :panelStyle="getLabelStyle()"
                :filter="true"
                :optionDisabled="showMode === 'showDisabled' ? 'disabled' : ''"
                @change="multiValueSelectionChanged"
            />
        </span>

        <span v-if="widgetType === 'date'" class="p-float-label p-m-2">
            <Calendar class="kn-material-input kn-width-full" v-model="selectedDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :showIcon="true" @date-select="dateSelectionChanged" />
            <label class="kn-material-input-label">
                {{ selectedDate }}
            </label>
        </span>

        <div v-if="widgetType === 'dateRange'" :class="getLayoutStyle()">
            <span class="p-float-label p-m-2" :style="getGridWidth()">
                <Calendar class="kn-width-full" v-model="startDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" :showIcon="true" @dateSelected="dateRangeSelectionChanged" />
            </span>
            <span class="p-float-label p-m-2" :style="getGridWidth()">
                <Calendar class="kn-width-full" v-model="endDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" :showIcon="true" @dateSelected="dateRangeSelectionChanged" />
            </span>
        </div>

        <!-- TODO: Ask if they want date range selection using PV component or no
        <span v-if="widgetType === 'dateRange'" class="p-float-label p-m-2">
            <Calendar  class="kn-material-input kn-width-full" selectionMode="range" v-model="selectedDateRange" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :showIcon="true" @change="logRange" />
            <label class="kn-material-input-label">{{ selectedDateRange }}</label>
        </span> -->
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IWidget } from '../../Dashboard'
import { mapActions } from 'pinia'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { emitter } from '../../DashboardHelpers'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Calendar from 'primevue/calendar'
import store from '../../Dashboard.store'
import deepcopy from 'deepcopy'
import moment from 'moment'
import dashboardDescriptor from '../../DashboardDescriptor.json'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Checkbox, RadioButton, Dropdown, MultiSelect, Calendar },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
        widgetInitialData: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        selectionIsLocked: { type: Boolean, required: true },
        editorMode: { type: Boolean }
    },
    emits: ['close'],
    computed: {
        widgetType(): string {
            return this.propWidget.settings.configuration.selectorType.modality || null
        },
        showMode(): string {
            if (this.propWidget.settings.configuration.valuesManagement.hideDisabled) return 'hideDisabled'
            else if (this.propWidget.settings.configuration.valuesManagement.enableAll) return 'enableAll'
            else return 'showDisabled'
        }
    },
    data() {
        return {
            dashboardDescriptor,
            initialOptions: { rows: [] } as any,
            options: { rows: [] } as any,
            selectedValue: null as any,
            selectedValues: [] as any,
            selectedDate: null as any,
            selectedDateRange: null as any,
            startDate: null as any,
            endDate: null as any,
            activeSelections: [] as ISelection[]
        }
    },
    watch: {
        propActiveSelections() {
            this.loadActiveSelections()
        },
        dataToShow() {
            this.loadOptions()
            const hasActiveSelectionValue = this.loadActiveSelectionValue()
            if (this.dataToShow?.initialCall && !hasActiveSelectionValue) this.updateSelectedValue()
        },
        widgetInitialData() {
            this.loadInitialValues()
        },
        widgetType() {
            this.updateDefaultValues()
        }
    },
    created() {
        this.setEventListeners()
        this.loadActiveSelections()
        this.loadInitialValues()
        this.loadActiveSelectionValue()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(store, ['setSelections']),
        setEventListeners() {
            emitter.on('defaultValuesChanged', this.onDefaultValuesChanged)
            emitter.on('valuesManagementChanged', this.onDefaultValuesChanged)
            emitter.on('widgetUnlocked', this.removeDeafultValues)
            emitter.on('selectionsDeleted', this.onSelectionsDeleted)
        },
        removeEventListeners() {
            emitter.off('defaultValuesChanged', this.onDefaultValuesChanged)
            emitter.off('valuesManagementChanged', this.onDefaultValuesChanged)
            emitter.off('widgetUnlocked', this.removeDeafultValues)
            emitter.off('selectionsDeleted', this.onSelectionsDeleted)
        },
        loadInitialValues() {
            this.initialOptions = deepcopy(this.widgetInitialData)
            this.loadOptions()
            this.updateSelectedValue()
        },
        loadOptions() {
            this.loadAvailableOptions(this.dataToShow)
        },
        loadAvailableOptions(dataToShow: any) {
            this.options = { rows: [] }
            if (!dataToShow || !dataToShow.rows) return
            this.initialOptions?.rows?.forEach((initialOption: any) => {
                const index = dataToShow.rows.findIndex((row: any) => row.column_1 === initialOption.column_1)
                this.options.rows.push({ ...initialOption, disabled: index === -1 })
            })
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        loadActiveSelectionValue() {
            if (this.editorMode) return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.propWidget.dataset && selection.columnName === this.propWidget.columns[0]?.columnName)
            if (index !== -1) {
                const selection = this.activeSelections[index]
                switch (this.widgetType) {
                    case 'singleValue':
                    case 'dropdown':
                        this.selectedValue = selection.value[0]
                        break
                    case 'multiValue':
                    case 'multiDropdown':
                        this.selectedValues = selection.value
                        break
                    case 'date':
                        console.log('>>>>>>>>>>>>> TEEEEEEEEEEEEEEEEEEEEEEEST 1: ', selection.value[0])
                        this.selectedDate = selection.value[0] ? moment(selection.value[0], dashboardDescriptor.selectionsDateFormat).toDate() : null
                        break
                    case 'dateRange':
                        this.startDate = new Date(selection.value[0])
                        this.endDate = new Date(selection.value[1])
                }
                return true
            } else return false
        },
        onDefaultValuesChanged(widgetId: any) {
            if (this.propWidget.id !== widgetId || !this.editorMode) return
            this.updateDefaultValues()
        },
        updateDefaultValues() {
            if (!this.propWidget.settings.configuration.defaultValues.enabled) {
                this.removeDeafultValues()
            } else {
                this.updateSelectedValue()
            }
        },
        removeDeafultValues() {
            this.selectedValue = null
            this.selectedValues = []
            this.selectedDate = null
            this.startDate = null
            this.endDate = null
        },
        updateSelectedValue() {
            const defaultMode = this.propWidget.settings.configuration.defaultValues.valueType
            switch (this.widgetType) {
                case 'singleValue':
                case 'dropdown':
                    this.selectDefaultValue(defaultMode, false)
                    break
                case 'multiValue':
                case 'multiDropdown':
                    this.selectDefaultValue(defaultMode, true)
                    break
                case 'date':
                    this.updateDateSelectedValue(false)
                    break
                case 'dateRange':
                    this.updateDateSelectedValue(true)
            }
        },
        selectDefaultValue(defaultMode: string, multivalue: boolean) {
            if (!this.options || !this.options.rows || !defaultMode) {
                this.removeDeafultValues()
                return
            }
            switch (defaultMode) {
                case 'FIRST':
                    const firstValue = this.findFirstAvailableValue()
                    if (multivalue) {
                        this.selectedValues = firstValue !== null ? [firstValue.column_1] : []
                    } else {
                        this.selectedValue = firstValue !== null ? firstValue.column_1 : null
                    }
                    break
                case 'LAST':
                    const lastValue = this.findLastAvailableValue()
                    if (multivalue) {
                        this.selectedValues = lastValue !== null ? [lastValue.column_1] : []
                    } else {
                        this.selectedValue = lastValue !== null ? lastValue.column_1 : null
                    }
                    break
                case 'STATIC':
                    this.setDefaultStaticValue(multivalue)
                    break
                default:
                    this.removeDeafultValues()
            }
            this.updateSelectionsAfterDefaultValuesAreSet(multivalue)
        },
        updateSelectionsAfterDefaultValuesAreSet(multivalue: boolean) {
            if (multivalue && this.selectedValues.length > 0) this.multiValueSelectionChanged()
            else if (!multivalue && this.selectedValue) this.singleValueSelectionChanged()
        },
        findFirstAvailableValue() {
            if (this.showMode === 'enableAll') return this.options.rows[0]
            const index = this.options.rows.findIndex((row: any) => !row.disabled)
            return index !== -1 ? this.options.rows[index] : null
        },
        findLastAvailableValue() {
            if (this.showMode === 'enableAll') return this.options.rows[this.options.rows.length - 1]
            const index = this.options.rows.findLastIndex((row: any) => !row.disabled)
            return index !== -1 ? this.options.rows[index] : null
        },
        setDefaultStaticValue(multivalue: boolean) {
            const staticValue = this.propWidget.settings.configuration.defaultValues?.value ?? ''
            if (!staticValue || !this.options.rows) {
                this.selectedValue = null
                this.selectedValues = []
                return
            }
            const index = this.options.rows.findIndex((option: any) => staticValue.trim() === option.column_1.trim())
            if (index !== -1) {
                if (multivalue) {
                    this.selectedValues = [this.options.rows[index].column_1]
                } else {
                    this.selectedValue = this.options.rows[index].column_1
                }
            } else {
                this.selectedValue = null
                this.selectedValues = []
            }
        },
        updateDateSelectedValue(multivalue: boolean) {
            const minDate = this.propWidget.settings.configuration.defaultValues?.startDate ?? null
            const maxDate = this.propWidget.settings.configuration.defaultValues?.endDate ?? null
            const datesProperties = multivalue ? ['startDate', 'endDate'] : ['selectedDate']
            datesProperties.forEach((property: string) => {
                if (this[property]?.getTime() < minDate?.getTime() || this[property]?.getTime() > maxDate?.getTime()) {
                    this[property] = null
                }
            })
        },
        getLayoutStyle() {
            let selectorType = this.propWidget.settings.configuration.selectorType
            if (selectorType.alignment) {
                switch (selectorType.alignment) {
                    case 'vertical':
                        return 'vertical-layout'
                    case 'horizontal':
                        return 'horizontal-layout'
                    case 'grid':
                        return 'grid-layout'
                    default:
                        break
                }
            }
        },
        getGridWidth() {
            let gridWidth = this.propWidget.settings.configuration.selectorType.columnSize
            if (gridWidth != '') return `width: ${gridWidth}`
            else return ''
        },
        getDateRange(rangeValue: string) {
            let dateRange = this.propWidget.settings.configuration.defaultValues
            if (dateRange.enabled && dateRange[rangeValue]) return dateRange[rangeValue]
            else return undefined
        },
        getLabelStyle() {
            return getWidgetStyleByType(this.propWidget, 'label')
        },
        getBackgroundColor() {
            return getWidgetStyleByType(this.propWidget, 'background')
        },
        singleValueSelectionChanged() {
            if (this.editorMode) return
            updateStoreSelections(this.createNewSelection([this.selectedValue]), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
        },
        multiValueSelectionChanged() {
            if (this.editorMode) return
            const tempSelection = this.createNewSelection(this.selectedValues)

            this.updateActiveSelectionsWithMultivalueSelection(tempSelection)
        },
        dateSelectionChanged() {
            if (this.editorMode) return
            console.log('>>>>>>>>>>> SELECTED DATE: ', this.selectedDate)
            //  console.log('>>>>>>>>>>> SELECTED DATE FORMATTED moment: ', moment(this.selectedDate).format(dashboardDescriptor.selectionsDateFormat))
            updateStoreSelections(this.createNewSelection([moment(deepcopy(this.selectedDate)).format(dashboardDescriptor.selectionsDateFormat)]), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
        },
        dateRangeSelectionChanged() {
            if (this.editorMode) return
            const tempSelection = this.createNewSelection([moment(this.selectedDate).format(dashboardDescriptor.selectionsDateFormat), moment(this.endDate).format(dashboardDescriptor.selectionsDateFormat)])
            this.updateActiveSelectionsWithMultivalueSelection(tempSelection)
        },
        updateActiveSelectionsWithMultivalueSelection(tempSelection: ISelection) {
            const index = this.activeSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === tempSelection.datasetId && activeSelection.columnName === tempSelection.columnName)
            if (index !== -1) {
                this.activeSelections[index] = tempSelection
            } else {
                this.activeSelections.push(tempSelection)
            }
        },
        createNewSelection(value: (string | number)[]) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: this.propWidget.columns[0]?.columnName ?? '', value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        getDatasetLabel(datasetId: number) {
            const index = this.datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? this.datasets[index].label : ''
        },
        onSelectionsDeleted(selections: any) {
            const index = selections.findIndex((selection: ISelection) => selection.datasetId === this.propWidget.dataset && selection.columnName === this.propWidget.columns[0]?.columnName)
            if (index !== -1) this.removeDeafultValues()
        }
    }
})
</script>

<style lang="scss">
.selector-widget {
    overflow-y: auto;
    .multi-select {
        display: flex;
        align-items: center;
        .multi-select-label {
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
        }
    }
    .vertical-layout {
        display: flex;
        flex-direction: column;
    }
    .horizontal-layout {
        display: flex;
    }
    .grid-layout {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        overflow-y: auto;
    }
}
.selectorCustomDropdownPanel {
    color: unset;
}
</style>
<style lang="scss" scoped>
::-webkit-scrollbar {
    width: 5px;
    height: 5px;
}
::-webkit-scrollbar-track {
    background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
    background: #888;
}
::-webkit-scrollbar-thumb:hover {
    background: #555;
}
.testClass {
    background-color: red;
    color: blue;
}
</style>
