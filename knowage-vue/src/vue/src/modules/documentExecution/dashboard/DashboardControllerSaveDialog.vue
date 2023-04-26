<template>
    <Dialog id="dashboard-save-dialog" :header="$t('dashboard.saveCockpit')" :style="descriptor.saveDialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <form class="p-fluid p-formgrid p-grid p-p-5">
            <div class="p-col-12 p-p-2">
                <Message class="p-m-2" severity="warn" :closable="false" :style="descriptor.saveHintMessageStyle">
                    {{ $t('dashboard.saveCockpitHint') }}
                </Message>
            </div>
            <div class="p-field p-col-12 p-mt-2 p-p-2">
                <span class="p-float-label">
                    <InputText id="label" v-model="v$.document.label.$model" class="kn-material-input" :max-length="descriptor.labelMaxLength" :class="{ 'p-invalid': v$.document.label.$invalid && v$.document.label.$dirty }" @blur="v$.document.label.$touch()" />
                    <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                </span>
                <KnValidationMessages class="p-mt-1" :v-comp="v$.document.label" :additional-translate-params="{ fieldName: $t('common.label') }" />
                <p class="input-help p-m-1">{{ labelHelp }}</p>
            </div>

            <div class="p-field p-col-12 p-mt-2 p-p-2">
                <span class="p-float-label">
                    <InputText id="name" v-model="v$.document.name.$model" class="kn-material-input" :max-length="descriptor.nameMaxLength" :class="{ 'p-invalid': v$.document.name.$invalid && v$.document.name.$dirty }" @blur="v$.document.name.$touch()" />
                    <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                </span>
                <KnValidationMessages class="p-mt-1" :v-comp="v$.document.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                <p class="input-help p-m-1">{{ nameHelp }}</p>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="saveAnalysis"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dialog from 'primevue/dialog'
import descriptor from './DashboardDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Message from 'primevue/message'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'dashboard-save-dialog',
    components: { Dialog, KnValidationMessages, Message },
    props: { visible: { type: Boolean } },
    emits: ['close', 'save'],
    data() {
        return {
            descriptor,
            document: { label: '', name: '' } as { label: string; name: string },
            nameDirty: false,
            labelDirty: false,
            v$: useValidate() as any
        }
    },
    computed: {
        labelHelp(): string {
            return (this.document?.label?.length ?? '0') + ' / ' + descriptor.labelMaxLength
        },
        nameHelp(): string {
            return (this.document?.name?.length ?? '0') + ' / ' + descriptor.nameMaxLength
        },
        saveButtonDisabled(): boolean {
            return !this.document || this.document.label.length === 0 || this.document.name.length === 0
        }
    },
    watch: {},
    validations() {
        const validationObject = { document: createValidations('document', descriptor.validations.document) }
        return validationObject
    },
    created() {},
    methods: {
        closeDialog() {
            this.$emit('close')
        },
        saveAnalysis() {
            this.$emit('save', this.document)
        }
    }
})
</script>

<style lang="scss" scoped>
#dashboard-save-dialog .p-dialog-header,
#dashboard-save-dialog .p-dialog-content {
    padding: 0;
}
#dashboard-save-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.input-help {
    font-size: smaller;
}
</style>
