<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            {{ attribute.attributeName }}
        </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="formValid" @click="save" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeForm" />
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />

    <div class="p-grid p-m-0 p-fluid">
        <div class="p-col-12">
            <Card>
                <template #content>
                    <form ref="profile-attributes-form">
                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="attributeName" v-model.trim="v$.attribute.attributeName.$model" max-length="100" type="text" class="p-inputtext p-component kn-material-input" @change="onDataChange(v$.attribute.attributeName)" />
                                    <label for="attributeName">{{ $t('managers.profileAttributesManagement.form.name') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.attribute.attributeName" :additional-translate-params="{ fieldName: $t('managers.profileAttributesManagement.form.name') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="attributeDescription" v-model.trim="v$.attribute.attributeDescription.$model" max-length="250" type="text" class="p-inputtext p-component kn-material-input" @blur="onDataChange(v$.attribute.attributeDescription)" />
                                    <label for="attributeDescription">{{ $t('managers.profileAttributesManagement.form.description') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.attribute.attributeDescription" :additional-translate-params="{ fieldName: $t('managers.profileAttributesManagement.form.description') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="dataType"
                                        v-model="v$.attribute.value.$model"
                                        :options="attributeTypeValues"
                                        option-label="name"
                                        option-value="value"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                        @before-show="setDirty(v$.attribute.value)"
                                        @change="onDataChange(v$.attribute.value)"
                                    />
                                    <label for="dataType">{{ $t('managers.profileAttributesManagement.form.dataType') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.attribute.value" :additional-translate-params="{ fieldName: $t('managers.profileAttributesManagement.form.dataType') }"></KnValidationMessages>
                        </div>

                        <div class="p-inputgroup p-col-6 p-sm-12 p-md-6">
                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <RadioButton id="disableLov" v-model="disableLov" name="disableLov" :value="false" @change="hideLovDropdown" />
                                <label for="disableLov">{{ $t('managers.profileAttributesManagement.form.manualInput') }}</label>
                            </div>

                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <RadioButton id="lov" v-model="enableLov" name="enableLov" :value="false" @change="showLovDropdown" />
                                <label for="enableLov">{{ $t('managers.profileAttributesManagement.form.lov') }}</label>
                            </div>
                        </div>

                        <div class="p-field" :hidden="LovSelectHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        v-model="v$.attribute.lovId.$model"
                                        :options="lovs"
                                        option-label="name"
                                        option-value="id"
                                        :filter="true"
                                        :placeholder="$t('managers.profileAttributesManagement.form.lovPlaceholder')"
                                        :show-clear="true"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                        @change="onLoveDropdownChange"
                                        @before-show="setDirty(v$.attribute.lovId)"
                                    />
                                    <label for="attributeDescription">{{ $t('managers.profileAttributesManagement.form.lov') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.attribute.lovId" :additional-translate-params="{ fieldName: $t('managers.profileAttributesManagement.form.lov') }"></KnValidationMessages>
                        </div>

                        <div class="p-inputgroup p-col-6 p-sm-12 p-md-6">
                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <InputSwitch v-model="v$.attribute.multivalue.$model" :on-input="checkSyntax()" @change="onDataChange(v$.attribute.multivalue)" />
                                <i class="p-ml-2 pi pi-bars"></i>
                                <label for="multiValue">{{ $t('managers.profileAttributesManagement.form.multiValue') }}</label>
                            </div>

                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <InputSwitch v-model="v$.attribute.allowUser.$model" @change="onDataChange(v$.attribute.allowUser)" />
                                <i class="p-ml-2 pi pi-eye"></i>
                                <label for="multiValue">{{ $t('managers.profileAttributesManagement.form.allowUser') }}</label>
                            </div>
                        </div>

                        <div class="p-col-6 p-sm-12 p-md-6" :hidden="syntaxSelectHidden">
                            <div class="p-grid p-ai-start vertical-container">
                                <div class="p-col">
                                    <RadioButton id="simple" v-model="v$.attribute.syntax.$model" name="syntax" :value="false" @change="onDataChange(v$.attribute.syntax)" />
                                    <label class="p-m-2" for="simple">{{ $t('managers.profileAttributesManagement.form.syntax.simple') }}</label>
                                    <div v-if="v$.attribute.syntax.$model === false" class="p-mt-2">* Simple = ('Italy','USA','Serbia', ...)</div>
                                </div>
                                <div class="p-col">
                                    <RadioButton id="complex" v-model="v$.attribute.syntax.$model" name="syntax" :value="true" @change="onDataChange(v$.attribute.syntax)" />
                                    <label class="p-m-2" for="complex">{{ $t('managers.profileAttributesManagement.form.syntax.complex') }}</label>
                                    <div v-if="v$.attribute.syntax.$model === true" class="p-m-2">* Complex = {;{Italy;USA;Serbia; ...}}</div>
                                </div>
                            </div>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iAttribute, iLov } from './ProfileAttributesManagement'
import useValidate from '@vuelidate/core'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import RadioButton from 'primevue/radiobutton'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import ProfileAttributesManagementDescriptor from './ProfileAttributesManagementDescriptor.json'
import profileAttributesManagementValidationDescriptor from './ProfileAttributesManagementValidationDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'profile-attributes-detail',
    components: {
        Dropdown,
        InputSwitch,
        RadioButton,
        KnValidationMessages
    },
    props: {
        selectedAttribute: {
            type: Object,
            required: true
        }
    },
    emits: ['refreshRecordSet', 'closesForm', 'dataChanged'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            v$: useValidate() as any,
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            attribute: {} as iAttribute,
            lovs: {} as iLov,
            loading: false as boolean,
            hideForm: false as boolean,
            isDirty: false as boolean,
            disableLov: true as boolean,
            enableLov: true as boolean,
            LovSelectHidden: true as boolean,
            syntaxSelectHidden: true as boolean,
            profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor,
            columns: ProfileAttributesManagementDescriptor.columns,
            attributeTypeValues: ProfileAttributesManagementDescriptor.attributeTypeValues
        }
    },
    computed: {
        formValid(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        selectedAttribute: {
            handler: function (attribute) {
                this.v$.$reset()
                this.loadAttribute(attribute)
            }
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            'custom-required': (value) => {
                return this.enableLov || value
            }
        }
        return {
            attribute: createValidations('attribute', profileAttributesManagementValidationDescriptor.validations.attribute, customValidators)
        }
    },
    async created() {
        await this.loadLovs()
        if (this.selectedAttribute) {
            this.loadAttribute(this.selectedAttribute)
        }
    },
    methods: {
        onLoveBlur() {
            this.v$.attribute.lovId.$touch()
        },
        async loadLovs() {
            this.loading = true
            await this.$http
                .get(this.apiUrl + 'lovs/get/all')
                .then((response: AxiosResponse<any>) => {
                    this.lovs = response.data
                })
                .finally(() => (this.loading = false))
        },
        resetForm() {
            Object.keys(this.attribute).forEach((k) => delete this.attribute[k])
        },
        showForm() {
            this.hideLovDropdown(false)
            this.syntaxSelectHidden = true
            this.resetForm()
            this.hideForm = false
        },
        async save() {
            let response: AxiosResponse<any>
            if (this.attribute.value === 'NUMBER') {
                this.attribute.value = 'NUM'
            }
            if (this.attribute.attributeId != null) {
                response = await this.$http.put(this.apiUrl + 'attributes/' + this.attribute.attributeId, this.attribute, ProfileAttributesManagementDescriptor.headers)
            } else {
                response = await this.$http.post(this.apiUrl + 'attributes/', this.attribute, ProfileAttributesManagementDescriptor.headers)
            }
            if (response.status == 200) {
                if (response.data.errors) {
                    console.log(response.data.errors)
                } else {
                    this.store.setInfo({
                        title: this.$t('managers.profileAttributesManagement.info.saveTitle'),
                        msg: this.$t('managers.profileAttributesManagement.info.saveMessage')
                    })
                }
            }

            this.$emit('refreshRecordSet')
            this.resetForm()
        },
        closeForm() {
            this.$emit('closesForm')
        },
        onAttributeSelect(event: any) {
            this.populateForm(event.data)
        },
        checkSyntax() {
            if (this.attribute.multivalue === true) {
                this.showSyntaxButtons(this.attribute)
            } else {
                this.attribute.syntax = null
                this.syntaxSelectHidden = true
            }
        },
        populateForm(attribute: iAttribute) {
            this.hideLovDropdown(false)
            this.hideForm = false

            this.attribute = { ...attribute }

            if (typeof attribute.value === 'object' && attribute.value !== null) {
                this.attribute.value = attribute.value['type'].toUpperCase()
            }

            this.showSyntaxButtons(attribute)

            if (attribute.lovId !== null) {
                this.disableLovs()
                this.showLovDropdown(false)
            } else {
                this.enableLovs()
            }
        },
        showSyntaxButtons(attribute: iAttribute) {
            if (attribute.lovId !== null && attribute.multivalue === true) {
                this.syntaxSelectHidden = false
            } else {
                this.syntaxSelectHidden = true
            }
        },
        hideLovDropdown(emitChange = true) {
            this.attribute.lovId = null
            this.enableLovs()
            this.LovSelectHidden = true
            if (emitChange) this.$emit('dataChanged')
        },
        showLovDropdown(emitChange = true) {
            this.disableLovs()
            this.LovSelectHidden = false
            if (emitChange) this.$emit('dataChanged')
        },
        disableLovs() {
            this.enableLov = false
            this.disableLov = true
        },
        enableLovs() {
            this.enableLov = true
            this.disableLov = false
        },
        onDataChange(v$Comp) {
            v$Comp.$touch()
            this.$emit('dataChanged')
        },
        onLoveDropdownChange() {
            this.checkSyntax()
            this.$emit('dataChanged')
        },
        loadAttribute(attribute) {
            if (attribute.attributeId === null) {
                this.resetForm()
                return
            }
            this.populateForm(attribute)
        },
        setDirty(v$Comp) {
            v$Comp.$touch()
            this.$emit('dataChanged')
        }
    }
})
</script>

<style lang="scss" scoped>
.table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    @media screen and (max-width: 960px) {
        align-items: start;
    }
}

.record-image {
    width: 50px;
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
}

.p-dialog .record-image {
    width: 50px;
    margin: 0 auto 2rem auto;
    display: block;
}

.confirmation-content {
    display: flex;
    align-items: center;
    justify-content: center;
}
@media screen and (max-width: 960px) {
    ::v-deep(.p-toolbar) {
        flex-wrap: wrap;

        .p-button {
            margin-bottom: 0.25rem;
        }
    }
}
</style>
