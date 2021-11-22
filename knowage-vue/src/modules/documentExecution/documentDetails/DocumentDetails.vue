<template>
    <Dialog class="document-details-dialog p-fluid kn-dialog--toolbar--primary" :contentStyle="mainDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="9999" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('documentExecution.documentDetails.title') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('closeDetails')" />
                </template>
            </Toolbar>
        </template>
        <div class="document-details-tab-container p-d-flex p-flex-column" :style="mainDescriptor.style.flexOne">
            <TabView class="document-details-tabview" :style="mainDescriptor.style.flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                    </template>
                    <InformationsTab :selectedDocument="selectedDocument" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.drivers.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.outputParams.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.dataLineage.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.history.title') }}</span>
                    </template>
                </TabPanel>
            </TabView>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import { AxiosResponse } from 'axios'
import mainDescriptor from './DocumentDetailsDescriptor.json'
import InformationsTab from './tabs/DocumentDetailsInformations.vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'document-details',
    components: { InformationsTab, TabView, TabPanel, Dialog },
    props: { selectedDocument: { type: Object }, visible: { type: Boolean, required: false } },
    emits: ['closeDetails'],
    data() {
        return {
            mainDescriptor,
            document: {} as any
        }
    },
    watch: {},
    created() {},
    //analyticalDrivers: http://localhost:8080/knowage/restful-services/2.0/datasources
    //datasources: http://localhost:8080/knowage/restful-services/2.0/analyticalDrivers
    //document: http://localhost:8080/knowage/restful-services/2.0/documents/${id}
    //drivers: http://localhost:8080/knowage/restful-services/2.0/documentdetails/${id}/drivers
    //engines: http://localhost:8080/knowage/restful-services/2.0/engines

    //folderId: ??
    //resourcePath: ??
    //states: ??
    //template: ??
    //types: ??
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

.document-details-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.document-details-tab-container .p-tabview .p-tabview-panel,
.document-details-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
