<template>
    <table class="pivot-table" v-if="!loading">
        <thead>
            <th v-for="(column, index) of columns" :key="index">
                {{ column.name }}
            </th>
        </thead>
        <tr v-for="(row, index) of mappedRows" :key="index">
            <template v-for="(column, i) of columns" :key="i">
                <td v-if="row[column.name].rowSpan > 0" :rowspan="row[column.name].rowSpan">
                    <span>{{ row[column.name].data }} -- {{ row[column.name].rowSpan }}</span>
                </td>
            </template>
        </tr>
    </table>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-pivot-table',
    props: {
        columns: [] as any,
        rows: [] as any
    },
    created() {
        this.mapRows()
        this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 0)
        this.loading = false
    },
    watch: {
        rows() {
            this.mapRows()
            this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 0)
        }
    },
    data() {
        return {
            mappedRows: [] as any,
            loading: true
        }
    },
    computed: {},
    methods: {
        mapRows() {
            this.mappedRows = this.rows.map((row) => {
                let newRow = {}
                this.columns.forEach((column) => {
                    newRow[column.name] = { data: row[column.name], rowSpan: 1 }
                })
                return newRow
            })
        },
        checkForRowSpan(fromIndex, toIndex, rows, columns, columnIndex) {
            const column = columns[columnIndex]
            console.log(fromIndex, toIndex, column)
            let groupCount = 1
            let startIndex = fromIndex
            for (let i = fromIndex + 1; i <= toIndex; i++) {
                console.log('i', i)
                console.log(rows[i - 1][column.name].data, '===', rows[i][column.name].data)
                if (rows[i - 1][column.name].data === rows[i][column.name].data) {
                    rows[i][column.name].rowSpan = 0
                    groupCount++
                }
                if (rows[i - 1][column.name].data !== rows[i][column.name].data || i === toIndex) {
                    console.log('groupCount', column.name, rows[startIndex][column.name].data, groupCount)
                    rows[startIndex][column.name].rowSpan = groupCount
                    if (i - 1 > startIndex) this.checkForRowSpan(startIndex, i === toIndex ? i : i - 1, rows, columns, columnIndex + 1)
                    startIndex = i
                    groupCount = 1
                }
            }
        }
    }
})
</script>

<style scoped lang="scss">
.pivot-table table,
th,
td {
    border: 1px solid black;
}
</style>
