<template>
    <h1>{{ inputColumn }}</h1>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputColumn } from '../../FunctionsCatalog'

export default defineComponent({
    name: 'function-catalog-input-column',
    components: {},
    props: { column: { type: Object } },
    data() {
        return {
            inputColumn: {} as iInputColumn
        }
    },
    created() {
        this.loadColumn()
    },
    methods: {
        loadColumn() {
            this.inputColumn = this.column as iInputColumn
        }
    }
})
</script>
