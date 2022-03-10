<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterSaveDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start> {{ $t('common.saveAs') + ' ... ' }} </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <div class="p-fluid p-formgrid p-grid p-m-4">
            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        :maxLength="knParameterSaveDialogDescriptor.nameMaxLength"
                        v-model.trim="v$.viewpoint.NAME.$model"
                        :class="{
                            'p-invalid': v$.viewpoint.NAME.$invalid && v$.viewpoint.NAME.$dirty
                        }"
                        @blur="v$.viewpoint.NAME.$touch()"
                    />
                    <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                </span>
                <div class="p-d-flex p-flex-row p-jc-around">
                    <KnValidationMessages
                        :vComp="v$.viewpoint.NAME"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                    <p class="input-help">{{ nameHelp }}</p>
                </div>
            </div>

            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <InputText
                        id="description"
                        class="kn-material-input"
                        :maxLength="knParameterSaveDialogDescriptor.descriptionMaxLength"
                        v-model.trim="v$.viewpoint.DESCRIPTION.$model"
                        :class="{
                            'p-invalid': v$.viewpoint.DESCRIPTION.$invalid && v$.viewpoint.DESCRIPTION.$dirty
                        }"
                        @blur="v$.viewpoint.DESCRIPTION.$touch()"
                    />
                    <label for="description" class="kn-material-input-label">{{ $t('common.description') }} * </label>
                </span>
                <div class="p-d-flex p-flex-row p-jc-around">
                    <KnValidationMessages
                        :vComp="v$.viewpoint.DESCRIPTION"
                        :additionalTranslateParams="{
                            fieldName: $t('common.description')
                        }"
                    />
                    <p class="input-help">{{ descriptionHelp }}</p>
                </div>
            </div>

            <div class="p-field p-col-12">
                <label for="scope" class="kn-material-input-label">{{ $t('common.scope') }} * </label>
                <Dropdown
                    id="scope"
                    class="kn-material-input"
                    v-model="v$.viewpoint.SCOPE.$model"
                    :class="{
                        'p-invalid': v$.viewpoint.SCOPE.$invalid && v$.viewpoint.SCOPE.$dirty
                    }"
                    :options="knParameterSaveDialogDescriptor.scopeOptions"
                    @blur="v$.viewpoint.SCOPE.$touch()"
                />
                <div class="p-d-flex p-flex-row">
                    <KnValidationMessages
                        :vComp="v$.viewpoint.SCOPE"
                        :additionalTranslateParams="{
                            fieldName: $t('common.scope')
                        }"
                    />
                </div>
            </div>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="saveViewpoint" :disabled="saveButtonDisabled"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { createValidations } from '@/helpers/commons/validationHelper'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import knParameterSaveDialogDescriptor from './KnParameterSaveDialogDescriptor.json'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
    import useValidate from '@vuelidate/core'

    export default defineComponent({
        name: 'kn-parameter-save-dialog',
        components: { Dialog, Dropdown, KnValidationMessages },
        props: { visible: { type: Boolean }, propLoading: { type: Boolean } },
        emits: ['close', 'saveViewpoint'],
        data() {
            return {
                knParameterSaveDialogDescriptor,
                viewpoint: {} as any,
                loading: false,
                v$: useValidate() as any
            }
        },
        validations() {
            return {
                viewpoint: createValidations('viewpoint', this.knParameterSaveDialogDescriptor.validations.viewpoint)
            }
        },
        computed: {
            nameHelp(): string {
                return (this.viewpoint.NAME?.length ?? '0') + ' / ' + this.knParameterSaveDialogDescriptor.nameMaxLength
            },
            descriptionHelp(): string {
                return (this.viewpoint.DESCRIPTION?.length ?? '0') + ' / ' + this.knParameterSaveDialogDescriptor.descriptionMaxLength
            },
            saveButtonDisabled(): any {
                return this.v$.$invalid
            }
        },
        watch: {
            visible() {
                this.v$.$reset()
                this.viewpoint = {}
            },
            propLoading() {
                this.setLoading()
            }
        },
        created() {
            this.setLoading()
        },
        methods: {
            setLoading() {
                this.loading = this.propLoading
            },
            closeDialog() {
                this.viewpoint = {}
                this.v$.$reset()
                this.$emit('close')
            },
            saveViewpoint() {
                this.$emit('saveViewpoint', this.viewpoint)
            }
        }
    })
</script>

<style lang="scss" scoped>
    .input-help {
        font-size: smaller;
        margin-top: auto;
        margin-left: auto;
    }
</style>
