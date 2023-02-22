<template>
    <Dialog id="olap-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.alDialog.title') }}
                </template>
            </Toolbar>
        </template>

        <form class="p-fluid p-formgrid p-grid p-m-1">
            <InlineMessage class="p-m-1 p-col-12" severity="info" closable="false">{{ $t('documentExecution.olap.alDialog.infoMsg') }}</InlineMessage>
            <div class="p-field p-float-label p-col-12 p-mt-2">
                <Dropdown id="availableAlg" v-model="selectedAlgorithm" class="kn-material-input" :options="availableAlgorithms" option-label="name" />
                <label for="availableAlg" class="kn-material-input-label"> {{ $t('documentExecution.olap.alDialog.availableAlg') }} </label>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="!selectedAlgorithm" @click="changeAlgorithm"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import descriptor from './OlapAlgorithmDialog.json'
import Dropdown from 'primevue/dropdown'
import InlineMessage from 'primevue/inlinemessage'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'olap-algorithm',
    components: { Dialog, Dropdown, InlineMessage },
    props: { sbiExecutionId: { type: String } },
    emits: ['close'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            descriptor,
            selectedAlgorithm: {} as any,
            availableAlgorithms: [] as any
        }
    },
    created() {
        this.getAvailableAlgorithms()
    },
    methods: {
        async getAvailableAlgorithms() {
            await this.$http
                .get(import.meta.env.VITE_OLAP_PATH + `1.0/allocationalgorithm/?SBI_EXECUTION_ID=${this.sbiExecutionId}`, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    this.availableAlgorithms = response.data
                })
                .catch(() => {})
        },
        async changeAlgorithm() {
            await this.$http
                .post(import.meta.env.VITE_OLAP_PATH + `1.0/allocationalgorithm/${this.selectedAlgorithm.className}/?SBI_EXECUTION_ID=${this.sbiExecutionId}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then(async () => {
                    this.store.setInfo({ title: this.$t('common.toast.updateTitle'), msg: this.$t('common.toast.updateSuccess') })
                })
                .catch(() => {})
        }
    }
})
</script>

<style lang="scss">
#olap-wizard-dialog .p-dialog-header,
#olap-wizard-dialog .p-dialog-content {
    padding: 0;
}
#olap-wizard-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.wizard-overlay-spinner .p-progress-spinner-svg {
    width: 125px;
}
</style>
