<template>
    <div class="kn-remove-card-padding p-col">
        <Toolbar class="kn-toolbar kn-toolbar--default">
            <template #start>
                {{ $t('documentExecution.documentDetails.drivers.visibilityTitle') }}
            </template>
            <template #end>
                <Button :label="$t('managers.businessModelManager.addCondition')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="openVisibilityConditionDialog('newCondition')" />
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Listbox class="kn-list data-condition-list" :options="visusalDependencyObjects" @change="openVisibilityConditionDialog($event.value)">
            <template #empty>{{ $t('documentExecution.documentDetails.drivers.noVisCond') }}</template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span class="kn-truncated" v-tooltip.top="slotProps.option.viewLabel + ' ' + slotProps.option.parFatherUrlName + ' ' + slotProps.option.operation + slotProps.option.compareValue">
                            <b>{{ slotProps.option.viewLabel }}</b> {{ slotProps.option.parFatherUrlName }} {{ slotProps.option.operation }}{{ slotProps.option.compareValue }}
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteCondition(slotProps.option)" />
                </div>
            </template>
        </Listbox>
    </div>
    <Dialog class="remove-padding" :style="driversDescriptor.style.conditionDialog" :visible="showVisibilityConditionDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('documentExecution.documentDetails.drivers.visualizationTitle') }}
                </template>
            </Toolbar>
        </template>

        <InlineMessage severity="info" class="p-m-2 kn-width-full">{{ $t('documentExecution.documentDetails.drivers.visualizationHint') }}</InlineMessage>

        <form class="p-fluid p-formgrid p-grid p-m-2">
            <div class="p-field p-col-12 p-mt-2">
                <span class="p-float-label">
                    <InputText id="title" class="kn-material-input" v-model="v$.selectedCondition.viewLabel.$model" :class="{ 'p-invalid': v$.selectedCondition.viewLabel.$invalid && v$.selectedCondition.viewLabel.$dirty }" @blur="v$.selectedCondition.viewLabel.$touch()" />
                    <label for="title" class="kn-material-input-label"> {{ $t('common.title') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedCondition.viewLabel" :additionalTranslateParams="{ fieldName: $t('common.title') }" />
            </div>
            <div class="p-field p-col-12 p-md-4">
                <span class="p-float-label ">
                    <Dropdown
                        id="driver"
                        class="kn-material-input"
                        v-model="v$.selectedCondition.parFatherId.$model"
                        :options="availableDrivers"
                        optionLabel="label"
                        optionValue="id"
                        :class="{ 'p-invalid': v$.selectedCondition.parFatherId.$invalid && v$.selectedCondition.parFatherId.$dirty }"
                        @blur="v$.selectedCondition.parFatherId.$touch()"
                        @change="setParFatherUrlName"
                    />
                    <label for="driver" class="kn-material-input-label"> {{ $t('managers.businessModelManager.analyticalDriver') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedCondition.parFatherId" :additionalTranslateParams="{ fieldName: $t('managers.businessModelManager.analyticalDriver') }" />
            </div>
            <div class="p-field p-col-12 p-md-4">
                <span class="p-float-label">
                    <Dropdown
                        id="operator"
                        class="kn-material-input"
                        v-model="v$.selectedCondition.operation.$model"
                        :options="availableOperators"
                        :class="{ 'p-invalid': v$.selectedCondition.operation.$invalid && v$.selectedCondition.operation.$dirty }"
                        @blur="v$.selectedCondition.operation.$touch()"
                    />
                    <label for="operator" class="kn-material-input-label"> {{ $t('managers.businessModelManager.analyticalDriver') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedCondition.operation" :additionalTranslateParams="{ fieldName: $t('managers.businessModelManager.analyticalDriver') }" />
            </div>
            <div class="p-field p-col-12 p-md-4">
                <span class="p-float-label">
                    <InputText id="title" class="kn-material-input" v-model="v$.selectedCondition.compareValue.$model" :class="{ 'p-invalid': v$.selectedCondition.compareValue.$invalid && v$.selectedCondition.compareValue.$dirty }" @blur="v$.selectedCondition.compareValue.$touch()" />
                    <label for="title" class="kn-material-input-label"> {{ $t('common.value') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedCondition.compareValue" :additionalTranslateParams="{ fieldName: $t('common.value') }" />
            </div>
        </form>

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click=";(showVisibilityConditionDialog = false), (selectedCondition = {})" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="v$.$invalid" @click="saveCondition" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { iDriver, iDocument, iVisualDependency } from '@/modules/documentExecution/documentDetails/DocumentDetails'
    import { defineComponent, PropType } from 'vue'
    import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
    import { AxiosResponse } from 'axios'
    import useValidate from '@vuelidate/core'
    import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
    import driversDescriptor from './DocumentDetailsDriversDescriptor.json'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
    import Listbox from 'primevue/listbox'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import InlineMessage from 'primevue/inlinemessage'

    export default defineComponent({
        name: 'document-drivers',
        components: { Listbox, Dialog, Dropdown, KnValidationMessages, InlineMessage },
        props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, selectedDriver: { type: Object as PropType<iDriver>, required: true } },
        emits: ['driversChanged'],
        data() {
            return {
                v$: useValidate() as any,
                mainDescriptor,
                driversDescriptor,
                selectedCondition: {} as iVisualDependency,
                visusalDependencyObjects: [] as iVisualDependency[],
                availableOperators: driversDescriptor.availableOperators,
                showVisibilityConditionDialog: false,
                loading: false
            }
        },
        watch: {
            selectedDriver() {
                this.selectedDriver.id ? this.getVisualDependenciesByDriverId() : ''
            }
        },
        created() {
            this.selectedDriver.id ? this.getVisualDependenciesByDriverId() : ''
        },
        validations() {
            const visibilityValidator = (value) => {
                return Object.keys(this.selectedCondition).length === 0 || value
            }
            const customValidators: ICustomValidatorMap = { 'visibility-validator': visibilityValidator }
            const validationObject = { selectedCondition: createValidations('selectedCondition', driversDescriptor.validations.selectedCondition, customValidators) }
            return validationObject
        },
        methods: {
            async getVisualDependenciesByDriverId() {
                this.loading = true
                this.$http
                    .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/visualdependencies?driverId=${this.selectedDriver.id}`)
                    .then((response: AxiosResponse<any>) => {
                        this.visusalDependencyObjects = response.data
                    })
                    .finally(() => (this.loading = false))
            },
            openVisibilityConditionDialog(condition?) {
                condition != 'newCondition' ? (this.selectedCondition = { ...condition }) : (this.selectedCondition = { parId: this.selectedDriver.id, prog: this.visusalDependencyObjects.length + 1 } as iVisualDependency)
                this.showVisibilityConditionDialog = true
            },
            setParFatherUrlName(event) {
                this.availableDrivers.filter((driver) => {
                    driver.id === event.value ? (this.selectedCondition.parFatherUrlName = driver.parameterUrlName) : ''
                })
            },
            async saveCondition() {
                await this.saveRequest()
                    .then(() => {
                        this.$store.commit('setInfo', { title: this.$t('common.save'), msg: this.$t('documentExecution.documentDetails.drivers.conditionSavedMsg') })
                        this.showVisibilityConditionDialog = false
                        this.getVisualDependenciesByDriverId()
                    })
                    .catch((error) => {
                        this.$store.commit('setError', { title: this.$t('common.error.saving'), msg: error })
                    })
            },
            saveRequest() {
                if (!this.selectedCondition.id) {
                    return this.$http.post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/visualdependencies`, this.selectedCondition, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                } else {
                    return this.$http.put(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/visualdependencies`, this.selectedCondition, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                }
            },
            async deleteCondition(conditionToDelete) {
                await this.$http
                    .post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/visualdependencies/delete`, conditionToDelete, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                    .then(() => {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                        this.getVisualDependenciesByDriverId()
                    })
                    .catch((error) => {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error })
                    })
            }
        }
    })
</script>
<style lang="scss" scoped>
    .kn-remove-card-padding .data-condition-list {
        border: 1px solid var(--kn-color-borders);
        border-top: none;
    }
</style>
