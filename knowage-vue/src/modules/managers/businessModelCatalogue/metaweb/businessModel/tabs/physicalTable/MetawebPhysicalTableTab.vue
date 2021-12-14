<template>
    <h1>IT WORKS</h1>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'

export default defineComponent({
    name: 'metaweb-physical-table-tab',
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, observer: { type: Object } },
    components: {},
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
