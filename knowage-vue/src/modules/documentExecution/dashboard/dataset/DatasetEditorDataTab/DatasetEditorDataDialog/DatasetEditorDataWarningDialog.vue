<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    <span>{{ $t('common.toast.warning') }}</span>
                </template>
            </Toolbar>
        </template>

        <span class="p-mt-2">{{ $t('dashboard.datasetEditor.datasetDriversWarningMsg1') }}</span>
        <br />
        <span v-for="dataset of ignoredDatasets" :key="dataset">
            <b>{{ dataset }}, </b>
        </span>
        <span>{{ $t('dashboard.datasetEditor.datasetDriversWarningMsg2') }}</span>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog },
    props: { ignoredDatasets: Array, visible: Boolean }
})
</script>
