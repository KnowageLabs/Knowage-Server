<template>
    <div class="p-grid p-m-2">
        <TypeCard v-for="(type, index) of selectorTypes" :key="index" :selectorType="type" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import TypeCard from './SelectorWidgetTypeCard.vue'

export default defineComponent({
    name: 'table-widget-rows',
    components: { TypeCard },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            selectorTypes: [
                { label: 'singleValue', value: 'singleValue', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/radio.svg' },
                { label: 'multiValue', value: 'multiValue', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg' },
                { label: 'dropdown', value: 'dropdown', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/dropdown.svg' },
                { label: 'multiDropdown', value: 'multiDropdown', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDropdown.svg' },
                { label: 'date', value: 'date', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/singleDate.svg' },
                { label: 'dateRange', value: 'dateRange', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDate.svg' }
            ]
        }
    },
    created() {},
    unmounted() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
#index-column-switch {
    border-bottom: 1px solid #c2c2c2;
}
</style>
