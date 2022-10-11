<template>
    <div class="outerIcon" :class="{ selected: widgetModel.settings.configuration.selectorType.modality == selectorType.value, disabled: selectorType.value == 'date' || selectorType.value == 'dateRange' }" :style="documentImageSource()" @click="changeModality(selectorType.value)"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'

export default defineComponent({
    name: 'table-widget-rows',
    components: {},
    props: { widgetModel: { type: Object as PropType<any>, required: true }, selectorType: { type: Object as any, true: false } },
    data() {
        return {}
    },
    created() {},
    unmounted() {},
    methods: {
        documentImageSource(): any {
            if (this.selectorType) {
                return {
                    'background-image': `url(${this.selectorType.imageUrl})`
                }
            }
        },
        changeModality(event) {
            this.widgetModel.settings.configuration.selectorType.modality = event
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
