<template>
    <Dialog class="document-details-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="mainDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('documentExecution.documentDetails.title') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('closeMetaweb')" />
                </template>
            </Toolbar>
        </template>
        <div class="document-details-tab-container p-d-flex p-flex-column" :style="mainDescriptor.style.flexOne">
            <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
            <TabView v-if="!loading" class="document-details-tabview" :style="mainDescriptor.style.flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                    </template>
                    test1
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.drivers.title') }}</span>
                    </template>
                </TabPanel>
            </TabView>
        </div>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import mainDescriptor from './MetawebDescriptor.json'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
export default defineComponent({
    name: 'document-details',
    components: { TabView, TabPanel, Dialog },
    props: { visible: { type: Boolean } },
    emits: ['closeMetaweb'],
    data() {
        return {
            v$: useValidate() as any,
            mainDescriptor
        }
    },
    computed: {},
    created() {},
    methods: {}
})
</script>
<style lang="scss">
.right-border {
    border-right: 1px solid #ccc;
}
.document-details-tabview .p-tabview-panels {
    padding: 0 !important;
}
.document-details-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}
.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
.document-details-tab-container .p-tabview .p-tabview-panel,
.document-details-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.details-warning-color {
    color: red;
}
</style>
