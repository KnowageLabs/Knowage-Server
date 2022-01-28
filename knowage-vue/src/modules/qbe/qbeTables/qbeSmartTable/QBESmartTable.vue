<template>
    <DataTable class="qbe-smart-table" v-if="previewData != null" :value="previewData.rows" :scrollable="true" scrollHeight="flex" :loading="loading" scrollDirection="both" :resizableColumns="true" :rowHover="true" columnResizeMode="expand" stripedRows showGridlines>
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column v-for="(col, index) of columns" :field="col.field" :key="index" style="flex-grow:1; flex-basis:200px">
            <template #header>
                <div class="customHeader">
                    <div class="qbeCustomTopColor" :style="`background-color: ${col.props.color}`" title="Inventory fact"></div>
                    <div class="qbeHeaderContainer">
                        <i class="fas fa-sort p-ml-2" />
                        <span class="p-mx-2 kn-truncated">{{ col.header }}</span>
                        <i class="fas fa-cog p-ml-auto" />
                        <i class="fas fa-filter p-mx-2" />
                        <i class="fas fa-times p-mr-2" @click="$emit('removeFieldFromQuery', index)" />
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>
    <span v-else>nothing to preview</span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
// import Menu from 'primevue/menu'
// import Checkbox from 'primevue/checkbox'
import QBESimpleTableDescriptor from './QBESmartTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { previewData: { type: Object, required: true }, query: { type: Object, required: true } },
    components: { Column, DataTable },
    emits: ['removeFieldFromQuery'],
    data() {
        return {
            QBESimpleTableDescriptor,
            columns: [] as any,
            rows: [] as any[]
        }
    },
    computed: {},
    watch: {
        previewData() {
            this.previewData != null ? this.setData() : ''
        }
    },
    created() {
        this.previewData != null ? this.setData() : ''
    },
    methods: {
        setData() {
            this.rows = this.previewData.rows
            this.columns = this.setPreviewColumns(this.previewData)
        },
        // TODO: LOADOVATI KOLONE NE IZ RESPONSE-a NEGO IZ SELECTED QUERY FIELD ARRAY-a
        setPreviewColumns(data: any) {
            console.log('selQuery inside smat view', this.query)
            let columns = [] as any
            data.metaData.fields.forEach((field) => {
                this.query.fields.find((queryField) => {
                    if (field.header === queryField.field) {
                        console.log('QUERY FIELD: ------', queryField.field)
                        columns.push({ header: field.header, field: field.name, type: field.type, props: { color: queryField.color, entity: queryField.entity, format: queryField.format, id: queryField.id } })
                    }
                })
            })
            console.log('ALL COLUMNS ', columns)
            return columns
        }
    }
})
</script>
<style lang="scss">
.qbe-smart-table {
    th {
        padding: 0 !important;
        border-bottom: 1px solid #a9a9a9 !important;
        .p-column-header-content {
            flex: 1;
        }
    }
    td {
        height: 20px;
    }
    .customHeader {
        width: 100%;
        flex-direction: column;
        display: flex;
        justify-content: flex-start;
        align-items: center;
        .qbeCustomTopColor {
            width: 100%;
            height: 5px;
        }
        .qbeHeaderContainer {
            width: 100%;
            display: flex;
            justify-content: flex-start;
            align-items: baseline;
            color: #707171;
        }
        i {
            transition: color 0.3s ease-out;
            line-height: 24px;
            cursor: pointer;
            margin: 0;
            &:hover {
                color: #bbd6ed;
            }
        }
    }
}
</style>
