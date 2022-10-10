<template>
    <div class="p-grid p-m-2">
        <TypeCard :imgUrl="'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg'" />
        <TypeCard :imgUrl="'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg'" />
        <TypeCard :imgUrl="'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg'" />
        <TypeCard :imgUrl="'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg'" />
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
        return {}
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
