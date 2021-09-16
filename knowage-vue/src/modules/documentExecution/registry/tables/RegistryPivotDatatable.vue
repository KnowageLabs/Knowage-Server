<template>
    <KnPivotTable :columns="filteredColumns" :rows="rows" @rowChanged="onRowChanged"></KnPivotTable>
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
            filteredColumns: [] as any
        }
    },
    created() {
        this.getFilteredColumns()
    },
    methods: {
        getFilteredColumns() {
            this.filteredColumns = this.columns.filter((el: any) => el.isVisible === true)
            console.log('FILTERED COLUMNS: ', this.filteredColumns)
        },
        onRowChanged(row: any) {
            console.log('Changed Row: ', row)
        }
    }
})
</script>
