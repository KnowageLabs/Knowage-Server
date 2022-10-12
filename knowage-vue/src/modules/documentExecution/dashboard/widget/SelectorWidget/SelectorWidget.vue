<template>
    <div class="selector-widget">
        <div v-if="widgetType === 'singleValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <RadioButton :inputId="`radio-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValue" />
                <label :for="`radio-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <div v-if="widgetType === 'multiValue'" :class="getLayoutStyle()">
            <div class="multi-select p-p-1" :style="getLabelStyle() + getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <Checkbox :inputId="`multi-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValues" />
                <label :for="`multi-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <span v-if="widgetType === 'dropdown'" class="p-float-label p-m-2">
            <Dropdown class="kn-width-full" panelClass="selectorCustomDropdownPanel" v-model="selectedValue" :options="dataToShow?.rows" optionLabel="column_1" optionValue="column_1" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" />
        </span>

        <span v-if="widgetType === 'multiDropdown'" class="p-float-label p-m-2">
            <MultiSelect class="kn-width-full" panelClass="selectorCustomDropdownPanel" v-model="selectedValues" :options="dataToShow?.rows" optionLabel="column_1" optionValue="column_1" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" :filter="true" />
        </span>

        <span v-if="widgetType === 'date'" class="p-float-label p-m-2">
            <Calendar id="startDate" class="kn-material-input kn-width-full" v-model="selectedDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :showIcon="true" />
            <label for="startDate" class="kn-material-input-label"> {{ selectedDate }} </label>
        </span>

        <div v-if="widgetType === 'dateRange'" :class="getLayoutStyle()">
            <span class="p-float-label p-m-2" :style="getGridWidth()">
                <Calendar id="startDate" class="kn-width-full" v-model="startDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" :showIcon="true" />
            </span>
            <span class="p-float-label p-m-2" :style="getGridWidth()">
                <Calendar id="startDate" class="kn-width-full" v-model="endDate" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :style="getLabelStyle()" :inputStyle="getLabelStyle()" :panelStyle="getLabelStyle()" :showIcon="true" />
            </span>
        </div>

        <!-- TODO: Ask if they want date range selection using PV component or no
        <span v-if="widgetType === 'dateRange'" class="p-float-label p-m-2">
            <Calendar id="startDate" class="kn-material-input kn-width-full" selectionMode="range" v-model="selectedDateRange" :minDate="getDateRange('startDate')" :maxDate="getDateRange('endDate')" :showIcon="true" @change="logRange" />
            <label for="startDate" class="kn-material-input-label">{{ selectedDateRange }}</label>
        </span> -->
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import Calendar from 'primevue/calendar'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Checkbox, RadioButton, Dropdown, MultiSelect, Calendar },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true }
    },
    emits: ['close'],
    computed: {
        widgetType(): boolean {
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
            endDate: null as any
        }
    },
    setup() {},
    created() {},
    updated() {},
    methods: {
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
