<template>
    <Dialog id="olap-save-new-version-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapSaveNewVersionDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.saveVersion.title') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <form v-if="!loading" class="p-fluid p-formgrid p-grid p-m-1">
            <InlineMessage v-if="!loading" class="p-m-1 kn-width-full" severity="info" closable="false"> {{ $t('documentExecution.olap.saveVersion.infoMessage') }}</InlineMessage>

            <div class="p-field p-float-label p-col-12 p-mt-2">
                <InputText class="kn-material-input" v-model.trim="version.name" />
                <label class="kn-material-input-label"> {{ $t('documentExecution.olap.saveVersion.versionName') }}</label>
            </div>

            <div class="p-field p-col-12 p-mt-2">
                <span class="p-float-label">
                    <Textarea class="kn-material-input" v-model.trim="version.descr" rows="3" :autoResize="true" />
                    <label class="kn-material-input-label"> {{ $t('documentExecution.olap.saveVersion.versionDescription') }}</label>
                </span>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="close"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import InlineMessage from 'primevue/inlinemessage'
import olapSaveNewVersionDialogDescriptor from './OlapSaveNewVersionDialogDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'olap-save-new-version-dialog',
    components: { Dialog, InlineMessage, Textarea },
    props: { visible: { type: Boolean }, id: { type: String } },
    emits: ['close', 'save'],
    computed: {},
    data() {
        return {
            olapSaveNewVersionDialogDescriptor,
            version: { name: '', descr: '' } as { name: string; descr: string },
            loading: false
        }
    },
    watch: {},
    created() {},
    methods: {
        close() {
            this.$emit('close')
            this.version = { name: '', descr: '' }
        },
        async save() {
            this.loading = true
            await this.$http
                .post(
                    process.env.VUE_APP_OLAP_PATH + `1.0/model/saveAs?SBI_EXECUTION_ID=${this.id}`,
                    { name: this.version.name ?? 'sbiNoDescription', descr: this.version.descr ?? 'sbiNoDescription' },
                    { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8', 'X-Disable-Errors': 'true' } }
                )
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.close()
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error?.localizedMessage
                    })
                )
            this.loading = false
        }
    }
})
</script>

<style lang="scss">
#olap-save-new-version-dialog .p-dialog-header,
#olap-save-new-version-dialog .p-dialog-content {
    padding: 0;
}
#olap-save-new-version-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
