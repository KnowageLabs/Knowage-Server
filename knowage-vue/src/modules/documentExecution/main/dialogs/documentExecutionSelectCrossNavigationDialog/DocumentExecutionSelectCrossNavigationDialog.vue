<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionSelectCNDialogDsecriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('documentExecution.main.destinationSelectionTitle') }}
                </template>
            </Toolbar>
        </template>

        <Listbox id="cross-navigation-document-list" class="p-m-2" :options="crossNavigationDocuments" @change="navigationSelected($event.value)" data-test="kpi-list">
            <template #option="slotProps">
                <div class="kn-list-item" data-test="list-item">
                    <div class="kn-list-item-text">
                        <span>{{ slotProps.option.crossText }}</span>
                    </div>
                </div>
            </template>
        </Listbox>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button class="kn-button kn-button--primary p-ml-auto" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import documentExecutionSelectCNDialogDsecriptor from './DocumentExecutionSelectCNDialogDsecriptor.json'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'document-execution-select-cn-dialog',
    components: { Dialog, Listbox },
    props: { visible: { type: Boolean }, crossNavigationDocuments: { type: Array } },
    emits: ['close', 'selected'],
    data() {
        return {
            documentExecutionSelectCNDialogDsecriptor
        }
    },
    methods: {
        closeDialog() {
            this.$emit('close')
        },
        navigationSelected(navigation: any) {
            this.$emit('selected', navigation)
        }
    }
})
</script>

<style lang="scss" scoped>
#cross-navigation-document-list {
    border: none;
}
</style>
