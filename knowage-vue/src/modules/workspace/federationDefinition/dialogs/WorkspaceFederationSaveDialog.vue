<template>
    <div>
        <Dialog :header="$t('workspace.federationDefinition.savingFederation')" :style="workspaceFederationSaveDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <div v-if="dataset">
                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.label') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="dataset.label"
                            :class="{
                                'p-invalid': labelDirty && (!dataset.label || dataset.label.length === 0)
                            }"
                            @input="labelDirty = true"
                            @blur="labelDirty = true"
                        />
                    </span>

                    <div v-show="labelDirty && (!dataset.label || dataset.label.length === 0)" class="p-error p-my-2">
                        {{ $t('common.validation.required', { fieldName: $t('common.label') }) }}
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.name') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="dataset.name"
                            :class="{
                                'p-invalid': nameDirty && (!dataset.name || dataset.name.length === 0)
                            }"
                            @input="nameDirty = true"
                            @blur="nameDirty = true"
                        />
                    </span>

                    <div v-show="nameDirty && (!dataset.name || dataset.name.length === 0)" class="p-error p-my-2">
                        {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="dataset.description" />
                    </span>
                </div>
            </div>

            <template #footer>
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
                <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="saveFederation"> {{ $t('common.save') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import workspaceFederationSaveDialogDescriptor from './WorkspaceFederationSaveDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-federation-save-dialog',
    components: { Dialog },
    emits: ['close', 'save'],
    props: { visible: { type: Boolean }, federatedDataset: { type: Object } },
    data() {
        return {
            workspaceFederationSaveDialogDescriptor,
            dataset: null as any,
            labelDirty: false,
            nameDirty: false
        }
    },
    watch: {
        federatedDataset() {
            this.loadDataset()
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return this.dataset.label.length === 0 || this.dataset.name.length === 0
        }
    },
    created() {
        this.loadDataset()
    },
    methods: {
        loadDataset() {
            this.dataset = this.federatedDataset ? { ...this.federatedDataset } : {}
        },
        closeDialog() {
            this.dataset = { ...this.federatedDataset }
            this.labelDirty = false
            this.nameDirty = false
            this.$emit('close')
        },
        saveFederation() {
            this.$emit('save', this.dataset)
        }
    }
})
</script>
