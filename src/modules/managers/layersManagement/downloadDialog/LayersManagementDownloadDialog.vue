<template>
    <Dialog id="qbe-join-definition-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('managers.layersManagement.downloadDialogTitle') }}
                </template>
            </Toolbar>
        </template>

        <div v-if="layer" class="p-m-5">
            <div v-for="(mode, index) in modes" :key="index" class="p-field-radiobutton p-d-flex p-flex-row">
                <RadioButton v-model="downloadMode" name="downloadMode" :value="mode" />
                <label>{{ $t(`managers.layersManagement.downloadTypes.${mode}`) }}</label>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="download"> {{ $t('common.download') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iLayer } from '../LayersManagement.d'
import { AxiosResponse } from 'axios'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import Dialog from 'primevue/dialog'
import layersManagementDownloadDialogDescriptor from './LayersManagementDownloadDialogDescriptor.json'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'qbe-join-definition-dialog',
    components: { Dialog, RadioButton },
    props: { visible: { type: Boolean }, layer: { type: Object as PropType<iLayer>, required: true } },
    emits: ['close'],
    data() {
        return {
            layersManagementDownloadDialogDescriptor,
            downloadMode: 'geojson'
        }
    },
    computed: {
        modes(): string[] {
            return this.layer.type === 'WFS' ? this.layersManagementDownloadDialogDescriptor.downloadModesWFS : this.layersManagementDownloadDialogDescriptor.downloadModes
        }
    },
    created() {},
    methods: {
        closeDialog() {
            this.$emit('close')
        },
        async download() {
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `layers/getDownload?id=${this.layer.layerId}%2CtypeWFS=${this.downloadMode}`, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    if (this.downloadMode === 'geojson') {
                        downloadDirect(JSON.stringify(response.data), this.layer.name, 'application/json')
                    } else {
                        window.open(response.data.url)
                    }
                    this.$emit('close')
                })
                .catch(() => {})
        }
    }
})
</script>

<style lang="scss">
#qbe-join-definition-dialog .p-dialog-header,
#qbe-join-definition-dialog .p-dialog-content {
    padding: 0;
}
#qbe-join-definition-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.qbe-advanced-filter-button {
    max-width: 150px;
}
</style>
