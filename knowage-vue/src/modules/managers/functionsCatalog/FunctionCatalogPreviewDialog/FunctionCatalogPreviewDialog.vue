<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="functionCatalogPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.functionsCatalog.previewTitle') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        </template>

        <TabView>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.configurator') }}</span>
                </template>

                <FunctionCatalogConfiguratorTab :datasets="datasets" :propFunction="propFunction" @loading="setLoading"></FunctionCatalogConfiguratorTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.preview') }}</span>
                </template>
            </TabPanel>
        </TabView>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('managers.functionsCatalog.next') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import functionCatalogPreviewDialogDescriptor from './FunctionCatalogPreviewDialogDescriptor.json'
import FunctionCatalogConfiguratorTab from './tabs/FunctionCatalogConfiguratorTab/FunctionCatalogConfiguratorTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'function-catalog-preview-dialog',
    components: { Dialog, FunctionCatalogConfiguratorTab, TabView, TabPanel },
    props: { propFunction: { type: Object }, datasets: { type: Array }, pythonConfigurations: { type: Array } },
    data() {
        return { functionCatalogPreviewDialogDescriptor, loading: false }
    },
    created() {},
    methods: {
        setLoading(value: boolean) {
            this.loading = value
            // console.log('LOADING: ', this.loading)
        }
    }
})
</script>
