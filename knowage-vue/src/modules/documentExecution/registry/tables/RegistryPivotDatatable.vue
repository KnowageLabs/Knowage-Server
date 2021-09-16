<template>
    <h1>IT WORKS</h1>
    <KnPivotTable :columns="fitleredColumns" :rows="rows"></KnPivotTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnPivotTable from '@/components/UI/KnPivotTable.vue'

export default defineComponent({
    name: 'registry-pivot-datatable',
    components: { KnPivotTable },
    props: {
        columns: [] as any,
        rows: [] as any
    },
    data() {
        return {
            fitleredColumns: [] as any
        }
    },
    created() {
        this.getFilteredColumns()
    },
    methods: {
        getFilteredColumns() {
            this.fitleredColumns = this.columns.filter((el: any) => el.isVisible === true)
            console.log('FILTERED COLUMNS: ', this.fitleredColumns)
        }
    }
})
</script>
