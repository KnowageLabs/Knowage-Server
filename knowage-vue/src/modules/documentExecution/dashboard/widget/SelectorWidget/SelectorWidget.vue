<template>
    <div>
        {{ propWidget }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: {},
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true }
    },
    emits: ['close'],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    updated() {},
    methods: {}
})
</script>

<style lang="scss"></style>
