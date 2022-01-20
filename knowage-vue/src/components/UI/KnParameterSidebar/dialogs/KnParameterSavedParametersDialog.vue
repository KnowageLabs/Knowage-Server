<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterSavedParametersDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left> {{ $t('documentExecution.main.savedParameters') }} </template>
                <template #right>
                    <Button id="saved-parameters-dialog-close-button" class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
                </template>
            </Toolbar>
        </template>

        <DataTable :value="viewpoints" class="p-datatable-sm kn-table" :paginator="viewpoints.length > 20" dataKey="id" v-model:filters="filters" :globalFilterFields="knParameterSavedParametersDialogDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="600px">
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <template #empty>
                <Message class="p-m-2" severity="info" :closable="false" :style="knParameterSavedParametersDialogDescriptor.styles.message">
                    {{ $t('common.info.noDataFound') }}
                </Message>
            </template>

            <Column class="kn-truncated" v-for="col of knParameterSavedParametersDialogDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
            <Column :style="knParameterSavedParametersDialogDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <div class="p-d-flex p-flex-row">
                        <Button icon="fas fa-file-signature" class="p-button-link p-mr-2" v-tooltip.left="$t('documentExecution.main.fillForm')" @click.stop="$emit('fillForm', slotProps.data)" />
                        <Button icon="fa fa-play-circle" class="p-button-link p-mr-2" v-tooltip.left="$t('common.execute')" @click.stop="$emit('executeViewpoint', slotProps.data)" />
                        <Button icon="fas fa-trash-alt" class="p-button-link" v-tooltip.left="$t('common.delete')" @click.stop="deleteViewpointConfirm(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import knParameterSavedParametersDialogDescriptor from './KnParameterSavedParametersDialogDescriptor.json'

export default defineComponent({
    name: 'kn-parameter-saved-parameters-dialog',
    components: { Column, DataTable, Dialog, Message },
    props: { visible: { type: Boolean }, propViewpoints: { type: Array } },
    emits: ['close', 'fillForm', 'executeViewpoint', 'deleteViewpoint'],
    data() {
        return {
            knParameterSavedParametersDialogDescriptor,
            viewpoints: [] as any[],
            filters: { global: [filterDefault] } as Object
        }
    },
    computed: {},
    watch: {
        propViewpoints() {
            this.loadViewpoints()
        }
    },
    created() {
        this.loadViewpoints()
    },
    methods: {
        loadViewpoints() {
            this.viewpoints = this.propViewpoints as any[]
        },
        closeDialog() {
            this.$emit('close')
        },
        deleteViewpointConfirm(viewpoint: any) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteViewpoint', viewpoint)
            })
        }
    }
})
</script>

<style lang="scss" scoped>
#saved-parameters-dialog-close-button {
    font-size: 0.75rem;
}
</style>
