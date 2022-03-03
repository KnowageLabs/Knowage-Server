<template>
    <DataTable class="p-datatable-sm kn-table p-m-2 kn-height-full" :value="rows" editMode="cell" responsiveLayout="stack" breakpoint="960px" :scrollable="true" @rowReorder="onRowReorder" @drop="onDrop($event)" @dragover.prevent @dragenter.prevent @cell-edit-complete="onCellEditComplete">
        <Column :rowReorder="true" :headerStyle="QBESimpleTableDescriptor.headerStyle" />
        <Column v-for="column in QBESimpleTableDescriptor.columns" :key="column.header" :field="column.field" :style="column.style">
            <template #header>
                <span v-tooltip.top="getHeaderTooltip(column)">{{ $t(column.header) }}</span>
            </template>
            <template #editor="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputText v-if="column.field === 'alias'" class="kn-material-input p-inputtext-sm qbe-simple-table-input" v-model="slotProps.data[slotProps.column.props.field]"></InputText>

                    <Checkbox v-else-if="column.field === 'group'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="onGroupingChanged(slotProps.data)"></Checkbox>
                    <Dropdown v-else-if="column.field === 'order'" v-model="slotProps.data[slotProps.column.props.field]" :options="QBESimpleTableDescriptor.orderingOptions" />
                    <Dropdown v-else-if="column.field === 'funct'" class="qbe-simple-table-dropdown" v-model="slotProps.data[slotProps.column.props.field]" :options="getAttributeOptions(slotProps.data)" :disabled="slotProps.data['group']" />
                    <Checkbox v-else-if="column.field === 'visible'" class="p-ml-3" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="$emit('columnVisibilityChanged')"></Checkbox>
                    <Checkbox v-else-if="column.field === 'inUse'" class="p-ml-2" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <span v-else v-tooltip.top="slotProps.data[slotProps.column.props.field]" class="kn-truncated">{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <i v-if="['alias', 'order', 'funct'].includes(column.field)" class="pi pi-pencil p-ml-2" />
                </div>
            </template>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center">
                    <Checkbox v-if="column.field === 'group'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="onGroupingChanged(slotProps.data)"></Checkbox>
                    <Dropdown v-else-if="column.field === 'funct'" class="qbe-simple-table-dropdown" v-model="slotProps.data[slotProps.column.props.field]" :options="getAttributeOptions(slotProps.data)" :disabled="slotProps.data['group']" />
                    <Checkbox v-else-if="column.field === 'visible'" class="p-ml-3" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="$emit('columnVisibilityChanged')"></Checkbox>
                    <Checkbox v-else-if="column.field === 'inUse'" class="p-ml-2" v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
                    <span v-else v-tooltip.top="slotProps.data[slotProps.column.props.field]" class="kn-truncated">{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <i v-if="['alias', 'order', 'funct'].includes(column.field)" class="pi pi-pencil p-ml-2" />
                </div>
            </template>
        </Column>
        <Column>
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row p-jc-end">
                    <div class="p-d-flex p-flex-row">
                        <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="toggle($event, slotProps.data, slotProps.index)" data-test="menu-toggle" />
                        <Menu ref="menu" :model="menuItems" :popup="true" />
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iField } from '../../QBE'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import QBESimpleTableDescriptor from './QBESimpleTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { query: { type: Object as PropType<iQuery> } },
    components: { Checkbox, Column, DataTable, Dropdown, Menu },
    emits: ['columnVisibilityChanged', 'openFilterDialog', 'openHavingDialog', 'entityDropped', 'groupingChanged'],
    data() {
        return {
            QBESimpleTableDescriptor,
            selectedQuery: {} as iQuery,
            rows: [] as iField[],
            menuItems: [] as any[]
        }
    },
    computed: {
        queryFields(): iField[] {
            return this.query ? this.query.fields : []
        }
    },
    watch: {
        queryFields() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (!this.query) return

            this.selectedQuery = this.query
            this.rows = this.selectedQuery.fields as iField[]
        },
        getAttributeOptions(row: iField) {
            return row.fieldType === 'attribute' ? this.QBESimpleTableDescriptor.attributeAggregationOptions : this.QBESimpleTableDescriptor.aggregationOptions
        },
        getHeaderTooltip(column: { field: string; header: string; style: string }) {
            switch (column.field) {
                case 'funct':
                    return this.$t('qbe.simpleTable.aggregation')
                case 'visible':
                    return this.$t('qbe.simpleTable.showField')
                default:
                    return this.$t(column.header)
            }
        },
        toggle(event: any, field: iField, index: number) {
            this.createMenuItems(field, index)
            const menu = this.$refs.menu as any
            menu?.toggle(event)
        },
        createMenuItems(field: iField, index: number) {
            this.menuItems = []
            this.menuItems.push({ icon: 'pi pi-filter', label: this.$t('common.filters'), command: () => this.openFiltersDialog(field) })
            if ((field.funct && field.funct !== 'NONE') || (field.type === 'inline.calculated.field' && field.fieldType === 'measure')) {
                this.menuItems.push({ icon: 'pi pi-filter', label: this.$t('qbe.simpleTable.havings'), command: () => this.openHavingsDialog(field) })
            }
            this.menuItems.push({ icon: 'pi pi-trash', label: this.$t('common.delete'), command: () => this.deleteColumn(index) })
        },
        onRowReorder(event: any) {
            this.rows = event.value
            this.selectedQuery.fields = this.rows
        },
        openFiltersDialog(field: iField) {
            this.$emit('openFilterDialog', field)
        },
        openHavingsDialog(field: iField) {
            this.$emit('openHavingDialog', { field: field, query: this.selectedQuery })
        },
        onDrop(event) {
            const data = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.$emit('entityDropped', data)
        },
        onGroupingChanged(field: iField) {
            field['funct'] = 'NONE'
            this.$emit('groupingChanged', field)
        },
        deleteColumn(index: number) {
            this.rows.splice(index, 1)
        },
        onCellEditComplete(event: any) {
            this.rows[event.index] = event.newData
        }
    }
})
</script>

<style lang="scss">
.qbe-simple-table-input {
    max-width: 100px;
}

.qbe-simple-table-dropdown {
    max-width: 100px;
}
</style>
