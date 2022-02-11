<template>
    <Dialog id="query-help-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left> {{ $t('common.help') }} </template>
            </Toolbar>
        </template>

        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.paragraph1')" />
        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.paragraph2')" />
        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.list1')" />
        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.list2')" />
        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.list3')" />
        <div :style="queryDescriptor.style.dialog" v-html="$t('managers.datasetManagement.queryHelpDialog.list4')" />

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import queryDescriptor from './DatasetManagementQueryDataset.json'

export default defineComponent({
    components: { Dialog },
    props: {},
    emits: ['close'],
    data() {
        return {
            queryDescriptor
        }
    }
})
</script>
