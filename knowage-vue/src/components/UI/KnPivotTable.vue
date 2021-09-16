<template>
    <table class="pivot-table">
        <thead>
            <th v-for="(column, index) of columns" :key="index">
                {{ column.field }}
            </th>
        </thead>
        <tr v-for="(row, index) of mappedRows" :key="index">
            <template v-for="(column, i) of columns" :key="i">
                <td v-if="row[column.field].rowSpan > 0" :rowspan="row[column.field].rowSpan">
                    <!-- <span>{{ row[column.field].data }} -- {{ row[column.field].rowSpan }}</span> -->
                    <span>{{ row[column.field].data }} </span>
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
        // this.loading = false
    },
    watch: {
        rows() {
            this.mapRows()
            this.checkForRowSpan(0, this.mappedRows.length - 1, this.mappedRows, this.columns, 0)
        }
    },
    data() {
        return {
            mappedRows: [] as any
            // loading: true
        }
    },
    computed: {},
    methods: {
        mapRows() {
            this.mappedRows = this.rows.map((row) => {
                let newRow = {}
                this.columns.forEach((column) => {
                    newRow[column.field] = { data: row[column.field], rowSpan: 1 }
                })
                return newRow
            })
            console.log('MAPPED ROWS: ', this.mappedRows)
            console.log('COLUMNS: ', this.columns)
        },
        checkForRowSpan(fromIndex, toIndex, rows, columns, columnIndex) {
            const column = columns[columnIndex]
            // console.log(fromIndex, toIndex, column)
            console.log('LINE 61 fromIndex: ', fromIndex)
            console.log('LINE 61 toIndex: ', toIndex)
            console.log('LINE 61 column: ', column)
            let groupCount = 1
            let startIndex = fromIndex
            for (let i = fromIndex + 1; i <= toIndex; i++) {
                // console.log('i', i)
                // console.log(rows[i - 1][column.field].data, '===', rows[i][column.field].data)
                console.log('LINE 69 i: ', i)
                console.log('LINE 70 === ', rows[i - 1][column.field].data, '===', rows[i][column.field].data)
                if (rows[i - 1][column.field].data === rows[i][column.field].data) {
                    rows[i][column.field].rowSpan = 0
                    groupCount++
                }
                if (rows[i - 1][column.field].data !== rows[i][column.field].data || i === toIndex) {
                    // console.log('groupCount', column.field, rows[startIndex][column.field].data, groupCount)
                    console.log('LINE 77 groupCount', column.field)
                    console.log('LINE 78 groupCount', rows[startIndex][column.field].data)
                    console.log('LINE 79 groupCount', groupCount)
                    rows[startIndex][column.field].rowSpan = groupCount
                    if (i - 1 > startIndex) {
                        console.log('LINE 82: ', i - 1 > startIndex)
                        this.checkForRowSpan(startIndex, i === toIndex ? i : i - 1, rows, columns, columnIndex + 1)
                    }
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
