<template>
    <h1>IT WORKS</h1>
</template>

<script lang="ts">
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'

export default defineComponent({
    name: 'pivot-table-conditional-style',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
