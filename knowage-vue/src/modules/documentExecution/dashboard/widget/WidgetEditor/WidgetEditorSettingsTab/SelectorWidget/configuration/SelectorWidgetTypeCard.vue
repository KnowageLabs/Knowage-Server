<template>
    <div v-if="model && model.type == 'selector'" class="outerIcon" :class="{ selected: model.settings.configuration.selectorType.modality == selectorType.value, disabled: cardDisabled }" :style="documentImageSource()" @click="changeSelectorModality(selectorType.value)" />
    <div v-if="model && model.type == 'selection'" class="outerIcon" :class="{ selected: model.settings.configuration.type == selectorType.value }" :style="documentImageSource()" @click="changeSelectionType(selectorType.value)" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'

export default defineComponent({
    name: 'table-widget-rows',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectorType: { type: Object as any, true: false } },
    data() {
        return {
            model: {} as IWidget
        }
    },
    computed: {
        cardDisabled(): boolean {
            if (this.selectorType.value == 'date' || this.selectorType.value == 'dateRange') {
                if (this.model.columns[0]?.type.toLowerCase().includes('date') || this.model.columns[0]?.type.toLowerCase().includes('timestamp')) return false
                else return true
            } else if (this.model.columns[0]?.type.toLowerCase().includes('date') || this.model.columns[0]?.type.toLowerCase().includes('timestamp')) return true
            return false
        }
    },
    watch: {
        widgetModel() {
            this.loadModel()
        }
    },
    created() {
        this.loadModel()
    },
    unmounted() {},
    methods: {
        loadModel() {
            this.model = this.widgetModel
        },
        documentImageSource(): any {
            if (this.selectorType) {
                return {
                    'background-image': `url(${this.selectorType.imageUrl})`
                }
            }
        },
        changeSelectorModality(event) {
            this.model.settings.configuration.selectorType.modality = event
        },
        changeSelectionType(event) {
            this.model.settings.configuration.type = event
        }
    }
})
</script>

<style lang="scss">
.outerIcon {
    width: 115px;
    height: 60px;
    border: 1px solid #ccc;
    cursor: pointer;
    margin: 5px;
    &.selected {
        border: 2px solid;
        background-color: #a9c3db;
        border-color: #43749e;
    }
    &:hover {
        background-color: darken(#a9c3db, 15%);
    }
    &.disabled {
        background-color: darken(#ccc, 10%);
        cursor: not-allowed;
        pointer-events: none !important;
    }
    background-repeat: no-repeat;
    background-size: contain;
    background-position: center;
}
</style>
