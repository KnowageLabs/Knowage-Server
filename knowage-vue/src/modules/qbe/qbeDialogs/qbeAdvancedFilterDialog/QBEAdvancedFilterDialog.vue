<template>
    <Dialog id="qbe-advanced-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEAdvancedFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('qbe.advancedFilters.advancedFilterVisualisation') }}
                </template>
            </Toolbar>
        </template>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iFilter } from '../../QBE'
import Dialog from 'primevue/dialog'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-advanced-filter-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, query: { type: Object as PropType<iQuery> } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            expression: null as any,
            filters: [] as iFilter[]
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
            console.log('QBEAdvancedFItlerDialog - loadData() - Loaded expression: ', this.expression)
            console.log('QBEAdvancedFItlerDialog - loadData() - Loaded filters: ', this.filters)
        },
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
</style>
