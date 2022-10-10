<template>
    <div class="outerIcon" :style="documentImageSource()"></div>

    <!-- http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/radio.svg -->
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'

export default defineComponent({
    name: 'table-widget-rows',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: false }, imgUrl: String },
    data() {
        return {}
    },
    created() {},
    unmounted() {},
    methods: {
        documentImageSource(): any {
            if (this.imgUrl) {
                return {
                    'background-image': `url(${this.imgUrl})`
                }
            }
        }
    }
})
</script>

<style lang="scss">
.outerIcon {
    width: 100px;
    height: 60px;
    border: 1px solid #ccc;
    cursor: pointer;
    margin-right: 8px;
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

.selTypesIcon {
    width: 100%;
    height: 100%;
    background-size: 50%;
    background-repeat: no-repeat;
    background-size: contain;
    background-position: center;
    background-color: #3b678c;
    // &.multiValueIcon {
    //     // @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg', 50%, center);
    //     background-image: url(http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg);
    // }
    // &.singleValueIcon {
    //     @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/radio.svg', 50%, center);
    // }
    // &.dropdownIcon {
    //     @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/dropdown.svg', 50%, center);
    // }
    // &.multiDropdownIcon {
    //     @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDropdown.svg', 50%, center);
    // }
    // &.singleValueTemporalIcon {
    //     @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/singleDate.svg', 50%, center);
    // }
    // &.multiValueTemporalIcon {
    //     @include custom-mask-image('http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDate.svg', 50%, center);
    // }
}
</style>
