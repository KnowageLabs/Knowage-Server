<template>
    <Dialog id="qbe-advanced-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEAdvancedFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('qbe.advancedFilters.advancedFilterVisualisation') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-d-flex p-flex-row">
            <Button class="kn-button kn-button--primary qbe-advanced-filter-button p-m-2" :disabled="selectedCount < 2" @click="group"> {{ $t('qbe.advancedFilters.group') }}</Button>
            <Button class="kn-button kn-button--primary qbe-advanced-filter-button p-m-2" :disabled="!singleGroupSelected" @click="ungroup"> {{ $t('qbe.advancedFilters.ungroup') }}</Button>
        </div>

        <QBEOperator v-if="expression" :propNode="root" @selectedChanged="onSelectedChanged" @treeUpdated="onTreeUpdated"></QBEOperator>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iFilter } from '../../QBE'
import * as treeService from './treeService'
import * as selectedOperandService from './selectedOperandService'
import * as advancedFilterservice from './advancedFilterService'
import Dialog from 'primevue/dialog'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'
import QBEOperator from './QBEOperator.vue'

import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'qbe-advanced-filter-dialog',
    components: { Dialog, QBEOperator },
    props: { visible: { type: Boolean }, query: { type: Object as PropType<iQuery> } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            expression: null as any,
            filters: [] as iFilter[],
            root: {} as any,
            selectedCount: 0,
            singleGroupSelected: false,
            getSelectedCount: selectedOperandService.getSelectedCount,
            isSingleGroupSelected: selectedOperandService.isSingleGroupSelected,
            sel: selectedOperandService.getSelected
        }
    },
    watch: {
        query: {
            handler() {
                this.loadData()
            },
            deep: true
        },
        visible(value: boolean) {
            if (value) this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (this.query) {
                this.expression = this.query.expression ? deepcopy(this.query?.expression) : {}
                this.filters = this.query.filters ? [...this.query.filters] : []
            }

            treeService.setFilterTree(deepcopy(this.expression))
            this.root = treeService.getFilterTree()
        },
        onSelectedChanged() {
            this.selectedCount = this.getSelectedCount()
            this.singleGroupSelected = this.isSingleGroupSelected()
        },
        group() {
            advancedFilterservice.group(treeService.getFilterTree(), selectedOperandService.getSelected())
            selectedOperandService.unSelectAll()
            this.root = treeService.getFilterTree()
        },
        ungroup() {
            advancedFilterservice.ungroup(treeService.getFilterTree(), selectedOperandService.getSelected()[0])
            selectedOperandService.unSelectAll()
            this.root = treeService.getFilterTree()
        },
        onTreeUpdated() {
            this.root = treeService.getFilterTree()
        },
        closeDialog() {
            selectedOperandService.unSelectAll()
            this.selectedCount = this.getSelectedCount()
            this.singleGroupSelected = this.isSingleGroupSelected()
            this.$emit('close')
        },
        save() {
            this.$emit('save', deepcopy(this.root))
            selectedOperandService.unSelectAll()
            this.selectedCount = this.getSelectedCount()
            this.singleGroupSelected = this.isSingleGroupSelected()
        }
    }
})
</script>

<style lang="scss">
#qbe-advanced-filter-dialog .p-dialog-header,
#qbe-advanced-filter-dialog .p-dialog-content {
    padding: 0;
}
#qbe-advanced-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.qbe-advanced-filter-button {
    max-width: 150px;
}
</style>
