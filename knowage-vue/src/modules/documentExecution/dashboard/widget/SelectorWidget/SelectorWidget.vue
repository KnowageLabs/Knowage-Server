<template>
    <div class="selector-widget">
        <div v-if="widgetType === 'singleValue'" :class="getLayoutStyle()">
            <div class="multi-select p-m-1" :style="getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <RadioButton :inputId="`radio-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValues" />
                <label :for="`radio-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>

        <div v-if="widgetType === 'multiValue'" :class="getLayoutStyle()">
            <div class="multi-select p-m-1" :style="getGridWidth()" v-for="(value, index) of dataToShow?.rows" :key="index">
                <Checkbox :inputId="`multi-${index}`" class="p-mr-2" :name="value.column_1" :value="value.column_1" v-model="selectedValues" />
                <label :for="`multi-${index}`" class="multi-select-label">{{ value.column_1 }}</label>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Checkbox, RadioButton },
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
            selectedValues: [] as any
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
                    case 'Vertical':
                        return 'vertical-layout'
                    case 'Horizontal':
                        return 'horizontal-layout'
                    case 'Grid':
                        return 'grid-layout'
                    default:
                        break
                }
            }
        },
        getGridWidth() {
            let gridWidth = this.propWidget.settings.configuration.selectorType.columnSize
            if (gridWidth != '') return { width: gridWidth }
            else return {}
        }
    }
})
</script>

<style lang="scss">
.selector-widget {
    overflow-y: auto;
    .multi-select {
        display: flex;
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
</style>
