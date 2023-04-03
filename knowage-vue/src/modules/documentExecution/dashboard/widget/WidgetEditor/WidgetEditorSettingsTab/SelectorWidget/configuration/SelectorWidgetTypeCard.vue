<template>
    <div v-if="model && model.type == 'selector'" v-tooltip.top="selectorType.label" class="outerIcon" :class="{ selected: model.settings.configuration.selectorType.modality == selectorType.value, disabled: cardDisabled }" @click="changeSelectorModality(selectorType.value)">
        <div class="innerIcon" :style="documentImageSource()"></div>
    </div>
    <div v-if="model && model.type == 'selection'" v-tooltip.top="selectorType.label" class="outerIcon" :class="{ selected: model.settings.configuration.type == selectorType.value }" @click="changeSelectionType(selectorType.value)">
        <div class="innerIcon" :style="documentImageSource()"></div>
    </div>
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
                    'mask-image': `url(${this.selectorType.imageUrl})`,
                    '-webkit-mask-image': `url(${this.selectorType.imageUrl})`
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
    .innerIcon {
        mask-size: 50%;
        mask-repeat: no-repeat;
        mask-position: center center;
        -webkit-mask-size: 50%;
        -webkit-mask-repeat: no-repeat;
        -webkit-mask-position: center center;
        background-repeat: no-repeat;
        background-size: 50%;
        background-position: center;
        background-color: var(--kn-color-primary);
        width: 115px;
        height: 60px;
    }

    border: 1px solid var(--kn-color-borders);
    cursor: pointer;
    margin: 5px;
    &.selected {
        border: 1px solid var(--kn-color-secondary);
        background-color: var(--kn-color-primary);
        .innerIcon {
            background-color: white;
        }
    }
    &:hover {
        background-color: var(--kn-color-secondary);
        .innerIcon {
            background-color: white;
        }
    }
    &.disabled {
        cursor: not-allowed;
        pointer-events: none !important;
    }
}
</style>
