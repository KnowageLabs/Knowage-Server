<template>
    <Dialog id="qbe-advanced-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEAdvancedFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('qbe.advancedFilters.advancedFilterVisualisation') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-d-flex p-flex-row">
            <Button class="kn-button kn-button--primary qbe-advanced-filter-button p-m-2" :disabled="getSelectedCount() < 1" @click="group"> {{ $t('qbe.advancedFilters.group') }}</Button>
            <Button class="kn-button kn-button--primary qbe-advanced-filter-button p-m-2" :disabled="!isSingleGroupSelected()" @click="ungroup"> {{ $t('qbe.advancedFilters.ungroup') }}</Button>
        </div>

        <QBEOperator :propNode="root"></QBEOperator>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iFilter } from '../../QBE'
import { getFilterTree, setFilterTree } from './treeService'
import { getSelectedCount, isSingleGroupSelected } from './selectedOperandService'
import Dialog from 'primevue/dialog'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'
import QBEOperator from './QBEOperator.vue'

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
            root: {},
            getSelectedCount,
            isSingleGroupSelected
        }
    },
    watch: {
        query() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            if (this.query) {
                this.expression = { ...this.query.expression }
                this.filters = [...this.query.filters]
            }

            this.root = this.expression
            setFilterTree(this.expression)
            this.root = getFilterTree()
            console.log('LOADED FILTER TREE: ', getFilterTree())
            console.log('QBEAdvancedFItlerDialog - loadData() - Loaded expression: ', this.expression)
            console.log('QBEAdvancedFItlerDialog - loadData() - Loaded filters: ', this.filters)

            console.log('QBEAdvancedFItlerDialog - loadData() - getSelectedCount(): ', this.getSelectedCount())
        },
        group() {},
        ungroup() {},
        closeDialog() {
            this.$emit('close')
        },
        save() {}
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
