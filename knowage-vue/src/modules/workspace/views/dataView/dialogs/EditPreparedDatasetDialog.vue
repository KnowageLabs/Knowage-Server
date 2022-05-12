<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.myData.editPreparedDataset') }}
                </template>
            </Toolbar>
        </template>
        <div class="p-grid p-m-0">
            <div class="p-col-12 ">
                <div class="p-d-flex">
                    <span class="p-float-label kn-flex p-mr-2">
                        <InputText
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.newDataset.name.$model"
                            :class="{
                                'p-invalid': v$.newDataset.name.$invalid
                            }"
                            maxLength="100"
                        />
                        <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.name') }}</label>
                        <KnValidationMessages
                            :vComp="v$.newDataset.name"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.configurationManagement.headers.name')
                            }"
                        ></KnValidationMessages>
                    </span>
                </div>
                <span class="p-float-label">
                    <Textarea
                        class="kn-material-input p-mb-1"
                        type="text"
                        v-model.trim="v$.newDataset.description.$model"
                        :class="{
                            'p-invalid': v$.newDataset.description.$invalid
                        }"
                        rows="3"
                        maxLength="10000"
                    />
                    <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.description') }}</label>
                    <KnValidationMessages
                        :vComp="v$.newDataset.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.configurationManagement.headers.description')
                        }"
                    ></KnValidationMessages>
                </span>
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="cancel" />
            <Button class="kn-button kn-button--primary" v-t="'common.apply'" @click="apply" :disabled="saveButtonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { createValidations } from '@/helpers/commons/validationHelper'
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import useValidate from '@vuelidate/core'
import Textarea from 'primevue/textarea'
import EditPreparedDatasetDialogValidationDescriptor from './EditPreparedDatasetDialogValidationDescriptor.json'

export default defineComponent({
    name: 'edit-prepared-dataset-dialog',
    components: { Dialog, Textarea },
    props: {
        visible: Boolean,
        dataset: Object
    },
    emits: ['save', 'cancel'],
    data() {
        return {
            newDataset: {} as any,
            v$: useValidate() as any,
            validationDescriptor: EditPreparedDatasetDialogValidationDescriptor
        }
    },
    validations() {
        return {
            newDataset: createValidations('newDataset', this.validationDescriptor.validations.configuration)
        }
    },
    updated() {
        if (this.dataset) {
            this.newDataset['label'] = this.dataset.label
            this.newDataset['name'] = this.dataset.name
            this.newDataset['description'] = this.dataset.description
        }
    },
    methods: {
        apply(): void {
            this.$emit('save', this.newDataset)
            this.clearForm()
        },
        cancel(): void {
            this.$emit('cancel')
            this.clearForm()
        },
        clearForm(): void {
            this.newDataset = {}
        }
    },
    computed: {
        saveButtonDisabled(): any {
            return this.v$.$invalid
        }
    }
})
</script>

<style lang="scss">
.dataPreparationSaveDialog {
    min-width: 600px !important;
    width: 600px !important;
    max-width: 600px !important;
}
</style>
