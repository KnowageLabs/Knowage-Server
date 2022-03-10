<template>
    <DataTable
        class="qbe-smart-table"
        v-if="previewData != null"
        :first="first"
        :value="previewData.rows"
        :scrollable="true"
        scrollHeight="flex"
        scrollDirection="both"
        :resizableColumns="true"
        :reorderableColumns="true"
        :rowHover="true"
        columnResizeMode="expand"
        :paginator="true"
        :lazy="true"
        :rows="25"
        :totalRecords="lazyParams.size"
        :currentPageReportTemplate="
            $t('common.table.footer.paginated', {
                first: '{first}',
                last: '{last}',
                totalRecords: '{totalRecords}'
            })
        "
        @page="onPage($event)"
        @column-reorder="$emit('reordered', $event)"
        @drop="onDrop($event)"
        @dragover.prevent
        @dragenter.prevent
        stripedRows
        showGridlines
    >
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column v-for="(col, index) of filteredVisibleFields" :hidden="!col.visible" :field="`column_${index + 1}`" :key="index" :style="qbeSimpleTableDescriptor.style.column">
            <template #header>
                <div class="customHeader">
                    <div class="qbeCustomTopColor" :style="`background-color: ${col.color}`" :title="col.entity"></div>
                    <div class="qbeHeaderContainer">
                        <i class="fas fa-sort p-ml-2" @click="changeOrder(col)" :data-test="'change-order-' + col.alias" v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.sorting`)" />
                        <span class="p-mx-2 kn-truncated" v-tooltip.bottom="col.alias">{{ col.alias }}</span>
                        <i class="fas fa-cog p-ml-auto" v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.colset`)" @click="showMenu($event, col)" />
                        <i class="fas fa-filter p-mx-2" :class="{ 'qbe-active-filter-icon': fieldHasFilters(col) }" v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.colfil`)" @click="openFiltersDialog(col)" />
                        <i class="fas fa-times p-mr-2" v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.coldel`)" @click="$emit('removeFieldFromQuery', index)" :data-test="'delete-column-' + col.alias" />
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>

    <div v-else class="kn-height-full kn-width-full" @drop="onDrop($event)" @dragover.prevent @dragenter.prevent>{{ $t('common.info.noDataFound') }}</div>

    <Dialog v-if="aliasDialogVisible" class="qbe-smart-table-alias-dialog" :visible="aliasDialogVisible" :modal="true" :closable="false" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    {{ $t('common.alias') }}
                </template>
            </Toolbar>
        </template>

        <span class="p-float-label p-m-4">
            <InputText id="alias" class="kn-material-input" type="text" maxLength="50" v-model="alias" />
            <label for="alias" class="kn-material-input-label"> {{ $t('common.alias') }} </label>
        </span>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="aliasDialogVisible = false"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="changeAlias"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>

    <Menu id="smartTableMenu" ref="smartTableMenu" :model="menuButtons" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Menu from 'primevue/contextmenu'
import Dialog from 'primevue/dialog'
import qbeSimpleTableDescriptor from './QBESmartTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { previewData: { type: Object, required: true }, query: { type: Object, required: true }, pagination: { type: Object } },
    components: { Column, DataTable, Menu, Dialog },
    emits: ['removeFieldFromQuery', 'orderChanged', 'fieldHidden', 'fieldGrouped', 'fieldAggregated', 'aliasChanged', 'entityDropped', 'reordered', 'pageChanged', 'openFilterDialog'],
    data() {
        return {
            qbeSimpleTableDescriptor,
            aliasDialogVisible: false,
            alias: '',
            menuButtons: [] as any,
            lazyParams: {} as any,
            selectedField: {} as any,
            first: 0
        }
    },
    computed: {
        filteredVisibleFields(): any {
            var newArr = this.query.fields.filter((field) => field.visible === true && field.inUse === true)
            return newArr
        }
    },
    watch: {
        previewData() {
            this.loadPagination()
        }
    },
    created() {
        this.loadPagination()
    },
    methods: {
        showMenu(event, col) {
            this.createMenuItems(col)
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.smartTableMenu.toggle(event)
        },
        createMenuItems(field) {
            this.menuButtons = []
            let visibleIcon = field.visible ? 'fas fa-check' : 'fas fa-times'
            let groupIcon = field.group ? 'fas fa-check' : 'fas fa-times'
            this.menuButtons.push(
                { key: '1', label: this.$t('qbe.detailView.smartViewMenu.showField'), icon: visibleIcon, command: () => this.hideField(field) },
                { key: '2', label: this.$t('qbe.detailView.smartViewMenu.group'), icon: groupIcon, visible: field.iconCls == 'attribute' || (field.iconCls == 'calculation' && field.attributes.formState.nature.toLowerCase() == 'attribute'), command: () => this.groupField(field) },
                {
                    key: '3',
                    label: this.$t('qbe.detailView.smartViewMenu.aggregation.title') + `: ${field.funct}`,
                    visible: field.iconCls == 'measure' || (field.iconCls == 'calculation' && field.attributes.formState.nature.toLowerCase() == 'measure'),
                    items: [
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.sum'), command: () => this.applyAggregation(field, 'SUM') },
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.min'), command: () => this.applyAggregation(field, 'MIN') },
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.max'), command: () => this.applyAggregation(field, 'MAX') },
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.avg'), command: () => this.applyAggregation(field, 'AVG') },
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.count'), command: () => this.applyAggregation(field, 'COUNT') },
                        { label: this.$t('qbe.detailView.smartViewMenu.aggregation.distinct'), command: () => this.applyAggregation(field, 'COUNT_DISTINCT') }
                    ]
                },
                { key: '4', label: this.$t('qbe.detailView.smartViewMenu.rename'), icon: 'fas fa-tag', command: () => this.showChangeAliasDialog(field) }
            )
        },
        changeOrder(field) {
            field.order === 'ASC' ? (field.order = 'DESC') : (field.order = 'ASC')
            this.$emit('orderChanged')
        },
        hideField(field) {
            field.visible = !field.visible
            this.$emit('fieldHidden')
        },
        groupField(field) {
            field.group = !field.group
            this.$emit('fieldGrouped')
        },
        applyAggregation(field, value) {
            field.funct = value
            this.$emit('fieldAggregated')
        },
        showChangeAliasDialog(field) {
            this.selectedField = field
            this.alias = JSON.parse(JSON.stringify(field.alias))
            this.aliasDialogVisible = true
        },
        changeAlias() {
            this.selectedField.alias = this.alias
            this.aliasDialogVisible = false
            this.$emit('aliasChanged')
        },
        onDrop(event) {
            var data = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.$emit('entityDropped', data)
        },
        loadPagination() {
            this.lazyParams = this.pagination as any
            this.first = this.pagination?.start
        },
        onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            this.$emit('pageChanged', this.lazyParams)
        },
        fieldHasFilters(field: any) {
            for (let i = 0; i < this.query.filters.length; i++) {
                const tempFilter = this.query.filters[i]
                if (tempFilter.leftOperandValue === field.id) {
                    return true
                }
            }

            return false
        },
        openFiltersDialog(field: any) {
            this.$emit('openFilterDialog', field)
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
            min-width: 0;
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
.qbe-smart-table-alias-dialog .p-dialog-header,
.qbe-smart-table-alias-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.qbe-active-filter-icon {
    color: red !important;
}
</style>
