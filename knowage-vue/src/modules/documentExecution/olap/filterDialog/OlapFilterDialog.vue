<template>
    <Dialog id="olap-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapFilterDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ propFilter?.filter.name }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        {{ selectedFilters }}

        <Message class="p-m-4" severity="info" :closable="false" :style="olapFilterDialogDescriptor.styles.message">
            <div v-if="treeLocked">
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partOne') }}</span>
                <b>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partTwo') }}</b>
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partThree') }}</span>
            </div>
            <span v-else>{{ $t('documentExecution.olap.filterDialog.infoMessage') }}</span>
        </Message>

        <OlapFilterTree :propFilter="propFilter" :id="id" :clearTrigger="clearTrigger" :treeLocked="treeLocked" @loading="loading = $event" @filtersChanged="onFiltersChange" @lockTree="treeLocked = true"></OlapFilterTree>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button v-show="selectedFilters.length > 0" class="kn-button kn-button--primary" @click="clear"> {{ $t('common.clear') }}</Button>
                <Button v-show="treeLocked" class="kn-button kn-button--primary" @click="treeLocked = false"> {{ $t('common.add') }}</Button>
                <Button class="kn-button kn-button--primary p-ml-auto" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="apply"> {{ $t('common.apply') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'
import OlapFilterTree from './OlapFilterTree.vue'

export default defineComponent({
    name: 'olap-filter-dialog',
    components: { Dialog, Message, OlapFilterTree },
    props: { visible: { type: Boolean }, olapVersionsProp: { type: Boolean, required: true }, propFilter: { type: Object }, id: { type: String } },
    emits: ['close', 'applyFilters'],
    data() {
        return {
            olapFilterDialogDescriptor,
            filter: null as any,
            selectedFilters: [] as string[],
            clearTrigger: false,
            treeLocked: false,
            loading: false
        }
    },
    watch: {
        propFilter() {
            if (this.visible) this.loadFilter()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter ? this.propFilter.filter : {}
            console.log('LOADED FILTER: ', this.filter)
        },
        clear() {
            this.selectedFilters = []
            this.treeLocked = false
            this.clearTrigger = !this.clearTrigger
        },
        closeDialog() {
            this.$emit('close')
        },
        apply() {
            // TODO: Hardcoded multi
            console.log(' AAAAAAAAAAAA - FILTER: ', this.propFilter)
            let payload = {}
            if (this.propFilter?.type === 'slicer') {
                payload = { hierarchy: this.propFilter?.filter.selectedHierarchyUniqueName, members: this.selectedFilters, multi: false, type: 'slicer' }
            } else {
                payload = { members: this.selectedFilters, type: 'visible', axis: this.propFilter?.filter.axis }
            }
            this.$emit('applyFilters', payload)
        },
        onFiltersChange(values: string[]) {
            this.selectedFilters = values
        }
    }
})
</script>

<style lang="scss">
#olap-filter-dialog .p-dialog-header,
#olap-filter-dialog .p-dialog-content {
    padding: 0;
}
#olap-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
