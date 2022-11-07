<template>
    <div>CSS EDITOR</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-responsive',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>
