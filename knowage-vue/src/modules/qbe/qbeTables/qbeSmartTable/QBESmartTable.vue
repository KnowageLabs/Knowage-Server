<template>
    <DataTable
        class="qbe-smart-table"
        v-if="previewData != null"
        :value="previewData.rows"
        :scrollable="true"
        scrollHeight="flex"
        :loading="loading"
        scrollDirection="both"
        :resizableColumns="true"
        :reorderableColumns="true"
        :rowHover="true"
        columnResizeMode="expand"
        stripedRows
        showGridlines
        @column-reorder="$emit('reordered', $event)"
        @drop="onDrop($event)"
        @dragover.prevent
        @dragenter.prevent
    >
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column v-for="(col, index) of query.fields" :hidden="!col.visible" :field="`column_${index + 1}`" :key="index" style="flex-grow:1; flex-basis:200px">
            <template #header>
                <div class="customHeader">
                    <div class="qbeCustomTopColor" :style="`background-color: ${col.color}`" :title="col.entity"></div>
                    <div class="qbeHeaderContainer">
                        <i class="fas fa-sort p-ml-2" @click="changeOrder(col)" />
                        <span class="p-mx-2 kn-truncated">{{ col.alias }}</span>
                        <i class="fas fa-cog p-ml-auto" @click="showMenu($event, col)" />
                        <i class="fas fa-filter p-mx-2" />
                        <i class="fas fa-times p-mr-2" @click="$emit('removeFieldFromQuery', index)" />
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>
    <span v-else>{{ $t('common.info.noDataFound') }}</span>

    <Dialog v-if="aliasDialogVisible" class="alias-dialog" :visible="aliasDialogVisible" :modal="trrue" :closable="false" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #left>
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
import QBESimpleTableDescriptor from './QBESmartTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { previewData: { type: Object, required: true }, query: { type: Object, required: true } },
    components: { Column, DataTable, Menu, Dialog },
    emits: ['removeFieldFromQuery', 'orderChanged', 'fieldHidden', 'fieldGrouped', 'fieldAggregated', 'aliasChanged', 'entityDropped', 'reordered'],
    data() {
        return {
            QBESimpleTableDescriptor,
            aliasDialogVisible: false,
            alias: '',
            menuButtons: [] as any,
            selectedField: {} as any
        }
    },
    computed: {},
    watch: {
        previewData() {}
    },
    created() {},
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
            console.log(field)
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
.alias-dialog .p-dialog-header,
.alias-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
</style>
