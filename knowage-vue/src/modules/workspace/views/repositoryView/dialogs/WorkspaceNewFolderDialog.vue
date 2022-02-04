<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceNewFolderDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.myRepository.newFolderTitle') }}
                </template>
            </Toolbar>
        </template>

        <form v-if="newFolder" class="p-fluid p-formgrid p-grid">
            <div class="p-field p-col-6  p-mt-5">
                <span class="p-float-label ">
                    <InputText
                        id="code"
                        class="kn-material-input"
                        type="text"
                        maxLength="25"
                        v-model.trim="v$.newFolder.code.$model"
                        :class="{
                            'p-invalid': v$.newFolder.code.$invalid && v$.newFolder.code.$dirty
                        }"
                        @blur="v$.newFolder.code.$touch()"
                    />
                    <label for="code" class="kn-material-input-label">{{ $t('managers.glossary.common.code') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.newFolder.code"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.glossary.common.code')
                    }"
                />
            </div>
            <div class="p-field p-col-6 p-mt-5">
                <span class="p-float-label ">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        type="text"
                        maxLength="25"
                        v-model.trim="v$.newFolder.name.$model"
                        :class="{
                            'p-invalid': v$.newFolder.name.$invalid && v$.newFolder.name.$dirty
                        }"
                        @blur="v$.newFolder.name.$touch()"
                    />
                    <label for="name" class="kn-material-input-label">{{ $t('importExport.catalogFunction.column.name') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.newFolder.name"
                    :additionalTranslateParams="{
                        fieldName: $t('importExport.catalogFunction.column.name')
                    }"
                />
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label p-mb-2">
                    <InputText id="descr" class="kn-material-input" type="text" maxLength="254" v-model.trim="newFolder.descr" />
                    <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                </span>
            </div>
        </form>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="createFolder" :disabled="buttonDisabled">{{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dialog from 'primevue/dialog'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import workspaceNewFolderDialogDescriptor from './WorkspaceNewFolderDialogDescriptor.json'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'workspace-repository-move-dialog',
    components: { Dialog, KnValidationMessages },
    props: { visible: { type: Boolean } },
    emits: ['close', 'create'],
    data() {
        return {
            v$: useValidate() as any,
            workspaceNewFolderDialogDescriptor,
            newFolder: {} as any
        }
    },
    validations() {
        return {
            newFolder: createValidations('newFolder', workspaceNewFolderDialogDescriptor.validations.newFolder)
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    created() {},
    methods: {
        closeDialog() {
            this.newFolder = {} as any
            this.v$.$reset()
            this.$emit('close')
        },
        createFolder() {
            this.$emit('create', this.newFolder)
            this.newFolder = {} as any
            this.v$.$reset()
        }
    }
})
</script>
