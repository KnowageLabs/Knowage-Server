<template>
    <DataTable
        v-if="previewData != null"
        class="qbe-smart-table"
        :first="first"
        :value="previewData.rows"
        :scrollable="true"
        scroll-height="flex"
        scroll-direction="both"
        :resizable-columns="true"
        :reorderable-columns="true"
        :row-hover="true"
        column-resize-mode="expand"
        :paginator="true"
        :lazy="true"
        :rows="25"
        :total-records="lazyParams.size"
        :current-page-report-template="
            $t('common.table.footer.paginated', {
                first: '{first}',
                last: '{last}',
                totalRecords: '{totalRecords}'
            })
        "
        striped-rows
        show-gridlines
        @page="onPage($event)"
        @column-reorder="$emit('reordered', $event)"
        @drop.stop="onDrop($event)"
        @dragover.prevent
        @dragenter.prevent
    >
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column v-for="(col, index) of filteredVisibleFields" :key="index" class="kn-truncated" :hidden="!col.visible" :field="`column_${index + 1}`" :style="qbeSimpleTableDescriptor.style.column">
            <template #header>
                <div class="customHeader">
                    <div class="qbeCustomTopColor" :style="`background-color: ${col.color}`" :title="col.entity"></div>
                    <div class="qbeHeaderContainer">
                        <i v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.sorting`)" class="fas fa-sort p-ml-2" :data-test="'change-order-' + col.alias" @click="changeOrder(col)" />
                        <span v-tooltip.bottom="col.alias" class="p-mx-2 kn-truncated">{{ col.alias }}</span>
                        <i v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.colset`)" class="fas fa-cog p-ml-auto" @click="showMenu($event, col)" />
                        <i v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.colfil`)" class="fas fa-filter p-mx-2" :class="{ 'qbe-active-filter-icon': fieldHasFilters(col) }" @click="openFiltersDialog(col)" />
                        <i v-tooltip.bottom="$t(`qbe.detailView.smartViewMenu.coldel`)" class="fas fa-times p-mr-2" :data-test="'delete-column-' + col.alias" @click="$emit('removeFieldFromQuery', col.uniqueID)" />
                    </div>
                </div>
            </template>
            <template #body="slotProps">
                <span v-if="typeof slotProps.data[`column_${index + 1}`] === 'number' && slotProps.data[`column_${index + 1}`]" v-tooltip="{ value: slotProps.data[`column_${index + 1}`] }"> {{ getFormattedNumber(col, slotProps.data[`column_${index + 1}`]) }}</span>
                <span v-else-if="previewData?.metaData?.fields[index + 1]?.type === 'date' && col.type != 'inline.calculated.field'">{{ getFormattedDate(slotProps.data[`column_${index + 1}`], previewData.metaData.fields[index + 1].metawebDateFormat, 'DD/MM/YYYY') }} </span>
                <span v-else-if="previewData?.metaData?.fields[index + 1]?.type === 'date' && col.type == 'inline.calculated.field'">{{ getFormattedDate(slotProps.data[`column_${index + 1}`], col.id.format, 'DD/MM/YYYY') }} </span>
                <span v-else-if="previewData?.metaData?.fields[index + 1]?.type === 'timestamp' && col.type != 'inline.calculated.field'">{{ getFormattedDate(slotProps.data[`column_${index + 1}`], previewData.metaData.fields[index + 1].metawebDateFormat, 'DD/MM/YYYY HH:mm:ss.SSS') }} </span>
                <span v-else-if="previewData?.metaData?.fields[index + 1]?.type === 'timestamp' && col.type == 'inline.calculated.field'">{{ getFormattedDate(slotProps.data[`column_${index + 1}`], col.id.format, 'DD/MM/YYYY HH:mm:ss.SSS') }} </span>
                <span v-else v-tooltip.bottom="slotProps.data[`column_${index + 1}`]">{{ slotProps.data[`column_${index + 1}`] }}</span>
            </template>
        </Column>
    </DataTable>

    <div v-else class="kn-height-full kn-width-full" @drop="onDrop($event)" @dragover.prevent @dragenter.prevent>{{ $t('common.info.noDataFound') }}</div>

    <Dialog v-if="aliasDialogVisible" class="qbe-smart-table-alias-dialog" :visible="aliasDialogVisible" :modal="true" :closable="false" :base-z-index="1" :auto-z-index="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    {{ $t('common.alias') }}
                </template>
            </Toolbar>
        </template>

        <span class="p-float-label p-m-4">
            <InputText id="alias" v-model="alias" class="kn-material-input" type="text" max-length="50" />
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
import { formatDate, getLocale } from '@/helpers/commons/localeHelper'
import { formatNumber } from '@/helpers/commons/qbeHelpers'

export default defineComponent({
    name: 'qbe-simple-table',
    components: { Column, DataTable, Menu, Dialog },
    props: { previewData: { type: Object, required: true }, query: { type: Object, required: true }, pagination: { type: Object } },
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
            const newArr = this.query.fields.filter((field) => field.visible === true && field.inUse === true)
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
            const visibleIcon = field.visible ? 'fas fa-check' : 'fas fa-times'
            const groupIcon = field.group ? 'fas fa-check' : 'fas fa-times'
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
            this.$emit('aliasChanged', this.selectedField)
        },
        onDrop(event) {
            const data = JSON.parse(event.dataTransfer.getData('text/plain'))
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
        },
        getFormattedNumber(column: any, number: number) {
            const configuration = formatNumber(column)
            let locale = getLocale()
            locale = locale ? locale.replaceAll('_', '-') : 'en-US'
            if (!configuration) return number
            const formattedNumber = Intl.NumberFormat(locale, { minimumFractionDigits: configuration.minFractionDigits, maximumFractionDigits: configuration.maxFractionDigits, useGrouping: configuration.useGrouping }).format(number)
            return configuration.currency + formattedNumber
        },
        getFormattedDate(date: any, output: any, input: any) {
            return formatDate(date, output, input)
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
