<template>
    <Dialog id="olap-custom-view-save-dialog" class="p-fluid kn-dialog--toolbar--secondary" :style="olapCustomViewSaveDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.savingCustomizedView') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <div class="p-formgrid p-grid p-m-4">
            <div class="p-fluid p-col-12 p-md-12 p-mt-2">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        v-model.trim="view.name"
                        :class="{
                            'p-invalid': !view.name && viewNameTouched
                        }"
                        @blur="viewNameTouched = true"
                    />
                    <label for="name" class="kn-material-input-label">{{ $t('common.name') }} *</label>
                </span>
                <div v-if="!view.name && viewNameTouched" class="p-error">
                    <small class="p-col-12">
                        {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                    </small>
                </div>
            </div>

            <div class="p-field p-col-12 p-md-12 p-mt-2">
                <span class="p-float-label">
                    <InputText id="description" class="kn-material-input" v-model.trim="view.description" />
                    <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                </span>
            </div>

            <div class="p-field p-col-12 p-md-12 p-mt-2">
                <span class="p-float-label">
                    <Dropdown class="kn-material-input" v-model="view.scope" :options="olapCustomViewSaveDialogDescriptor.scopeOptions"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.scope') }}</label>
                </span>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="!view.name" @click="saveCustomizedView"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import olapCustomViewSaveDialogDescriptor from './OlapCustomViewSaveDialogDescriptor.json'

    export default defineComponent({
        name: 'olap-custom-view-save-dialog',
        components: { Dialog, Dropdown },
        props: { sbiExecutionId: { type: String } },
        data() {
            return {
                olapCustomViewSaveDialogDescriptor,
                view: { name: '', description: '', scope: 'public' },
                viewNameTouched: false,
                loading: false
            }
        },
        created() {},
        methods: {
            closeDialog() {
                this.$emit('close')
                this.viewNameTouched = false
                this.view = { name: '', description: '', scope: 'public' }
            },
            async saveCustomizedView() {
                this.loading = true
                await this.$http
                    .post(process.env.VUE_APP_OLAP_PATH + `1.0/subobject?SBI_EXECUTION_ID=${this.sbiExecutionId}`, this.view)
                    .then(() => {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                        this.closeDialog()
                    })
                    .catch(() => {})
                this.loading = false
            }
        }
    })
</script>

<style lang="scss">
    #olap-custom-view-save-dialog .p-dialog-header,
    #olap-custom-view-save-dialog .p-dialog-content {
        padding: 0;
    }
    #olap-custom-view-save-dialog .p-dialog-content {
        display: flex;
        flex-direction: column;
        flex: 1;
    }
</style>
