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
            {{ $t('documentExecution.olap.filterDialog.infoMessage') }}
        </Message>

        <OlapFilterTree :propFilter="propFilter" :id="id" :clearTrigger="clearTrigger" @loading="loading = $event" @filtersChanged="onFiltersChange"></OlapFilterTree>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button v-show="selectedFilters.length > 0" class="kn-button kn-button--primary" @click="clear"> {{ $t('common.clear') }}</Button>
                <Button class="kn-button kn-button--primary p-ml-auto" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
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
    emits: ['close'],
    data() {
        return {
            olapFilterDialogDescriptor,
            selectedFilters: [] as string[],
            clearTrigger: false,
            loading: false
        }
    },
    watch: {},
    created() {},
    methods: {
        clear() {
            this.selectedFilters = []
            this.clearTrigger = !this.clearTrigger
        },
        closeDialog() {
            this.$emit('close')
        },
        save() {
            console.log('SAVE: ')
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
