<template>
    <div>
        <Dialog :header="$t('workspace.myAnalysis.editAnalysis')" :style="workspaceAnalysisViewEditDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <div v-if="analysis">
                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.label') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="analysis.label"
                            :class="{
                                'p-invalid': labelDirty && (!analysis.label || analysis.label.length === 0)
                            }"
                            :maxLength="workspaceAnalysisViewEditDialogDescriptor.labelMaxLength"
                            @input="labelDirty = true"
                            @blur="labelDirty = true"
                        />
                    </span>

                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="labelDirty && (!analysis.label || analysis.label.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('common.label') }) }}
                            </div>
                        </div>
                        <p class="input-help p-m-0">{{ labelHelp }}</p>
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.name') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="analysis.name"
                            :class="{
                                'p-invalid': nameDirty && (!analysis.name || analysis.name.length === 0)
                            }"
                            :maxLength="workspaceAnalysisViewEditDialogDescriptor.nameMaxLength"
                            @input="nameDirty = true"
                            @blur="nameDirty = true"
                        />
                    </span>

                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="nameDirty && (!analysis.name || analysis.name.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                            </div>
                        </div>
                        <p class="input-help p-m-0">{{ nameHelp }}</p>
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="analysis.description" :maxLength="workspaceAnalysisViewEditDialogDescriptor.descriptionMaxLength" />
                    </span>

                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div></div>
                        <p class="input-help p-m-0">{{ descriptionHelp }}</p>
                    </div>
                </div>
            </div>

            <template #footer>
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
                <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="saveAnalysis"> {{ $t('common.save') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import workspaceAnalysisViewEditDialogDescriptor from './WorkspaceAnalysisViewEditDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-analysis-edit-dialog',
    components: { Dialog },
    emits: ['close', 'save'],
    props: { visible: { type: Boolean }, propAnalysis: { type: Object } },
    data() {
        return {
            workspaceAnalysisViewEditDialogDescriptor,
            analysis: null as any,
            labelDirty: false
        }
    },
    computed: {
        labelHelp(): string {
            return (this.analysis.label?.length ?? '0') + ' / ' + workspaceAnalysisViewEditDialogDescriptor.labelMaxLength
        },
        nameHelp(): string {
            return (this.analysis.name?.length ?? '0') + ' / ' + workspaceAnalysisViewEditDialogDescriptor.nameMaxLength
        },
        descriptionHelp(): string {
            return (this.analysis.description?.length ?? '0') + ' / ' + workspaceAnalysisViewEditDialogDescriptor.descriptionMaxLength
        },
        saveButtonDisabled(): boolean {
            return this.analysis.label.length === 0 || this.analysis.name.length === 0
        }
    },
    watch: {
        propAnalysis() {
            this.loadAnalysis()
        }
    },
    created() {
        this.loadAnalysis()
    },
    methods: {
        loadAnalysis() {
            this.analysis = this.propAnalysis ? { ...this.propAnalysis } : {}
        },
        closeDialog() {
            this.analysis = { ...this.propAnalysis }
            this.$emit('close')
        },
        saveAnalysis() {
            this.$emit('save', this.analysis)
        }
    }
})
</script>

<style lang="scss" scoped>
.input-help {
    font-size: smaller;
}
</style>
