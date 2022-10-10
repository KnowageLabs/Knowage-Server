<template>
    <div class="outerIcon" :style="documentImageSource()"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'table-widget-rows',
    components: {},
    props: { selectorType: { type: Object as any, true: false } },
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
        background-color: #a9c3db;
    }
    &:hover {
        background-color: darken(#a9c3db, 15%);
    }
    &:hover,
    &.selected {
        .selTypesIcon {
            background-color: white;
        }
    }
    background-repeat: no-repeat;
    background-size: contain;
    background-position: center;
}
</style>
