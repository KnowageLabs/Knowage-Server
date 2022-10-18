<template>
    <div class="selector-widget">
        <div v-if="widgetType === 'singleValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <RadioButton :inputId="`radio-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValue" @change="singleValueSelectionChanged" />
                <label :for="`radio-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <div v-if="widgetType === 'multiValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <Checkbox :inputId="`multi-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValues" @change="multiValueSelectionChanged" />
                <label :for="`multi-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <span v-if="widgetType === 'dropdown'" class="p-float-label p-m-2">
            <Dropdown
                class="kn-width-full"
                panelClass="selectorCustomDropdownPanel"
                v-model="selectedValue"
                :options="dataToShow?.rows"
                optionLabel="column_1"
                optionValue="column_1"
                :style="getLabelStyle()"
                :inputStyle="getLabelStyle()"
                :panelStyle="getLabelStyle()"
                @change="singleValueSelectionChanged"
            />
        </span>

        <span v-if="widgetType === 'multiDropdown'" class="p-float-label p-m-2">
            <MultiSelect
                class="kn-width-full"
                panelClass="selectorCustomDropdownPanel"
                v-model="selectedValues"
                :options="dataToShow?.rows"
                optionLabel="column_1"
                optionValue="column_1"
                :style="getLabelStyle()"
                :inputStyle="getLabelStyle()"
                :panelStyle="getLabelStyle()"
                :filter="true"
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
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Calendar from 'primevue/calendar'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'
import store from '../../Dashboard.store'
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Checkbox, RadioButton, Dropdown, MultiSelect, Calendar },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
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
        }
    },
    data() {
        return {
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
        }
    },
    setup() {},
    created() {
        this.loadActiveSelections()
    },
    updated() {},
    methods: {
        ...mapActions(store, ['setSelections']),
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
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
        logRange(event) {
            console.log('range', event)
        },
        singleValueSelectionChanged() {
            if (this.editorMode) return
            updateStoreSelections(this.createNewSelection([this.selectedValue]), this.activeSelections, this.dashboardId, this.setSelections)
        },
        multiValueSelectionChanged() {
            if (this.editorMode) return
            const tempSelection = this.createNewSelection(this.selectedValues)
            this.updateActiveSelectionsWithMultivalueSelection(tempSelection)
        },
        dateSelectionChanged() {
            if (this.editorMode) return
            updateStoreSelections(this.createNewSelection([this.selectedDate]), this.activeSelections, this.dashboardId, this.setSelections)
        },
        dateRangeSelectionChanged() {
            if (this.editorMode) return
            const tempSelection = this.createNewSelection([this.startDate, this.endDate])
            this.updateActiveSelectionsWithMultivalueSelection(tempSelection)
        },
        updateActiveSelectionsWithMultivalueSelection(tempSelection: ISelection) {
            const index = this.activeSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === tempSelection.datasetId && activeSelection.columnName === tempSelection.columnName)
            index !== -1 ? (this.activeSelections[index] = tempSelection) : this.activeSelections.push(tempSelection)
        },
        createNewSelection(value: (string | number)[]) {
            return { datasetId: this.propWidget.dataset as number, datasetLabel: this.getDatasetLabel(this.propWidget.dataset as number), columnName: this.propWidget.columns[0]?.columnName ?? '', value: value, aggregated: false, timestamp: new Date().getTime() }
        },
        getDatasetLabel(datasetId: number) {
            const index = this.datasets.findIndex((dataset: IDataset) => dataset.id.dsId == datasetId)
            return index !== -1 ? this.datasets[index].label : ''
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
