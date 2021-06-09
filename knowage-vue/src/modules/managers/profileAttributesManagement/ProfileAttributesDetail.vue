<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #left>
            {{ attribute.attributeName }}
        </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="save" :disabled="formValid" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeForm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />

    <div class="p-grid p-m-0 p-fluid">
        <div class="p-col-12">
            <Card>
                <template #content>
                    <form ref="profile-attributes-form">
                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="attributeName" type="text" v-model.trim="v$.attribute.attributeName.$model" @change="onDataChange(v$.attribute.attributeName)" class="p-inputtext p-component kn-material-input" />
                                    <label for="attributeName">{{ $t('managers.profileAttributesManagement.form.name') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.attribute.attributeName" :additionalTranslateParams="{ fieldName: $t('managers.profileAttributesManagement.form.name') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="attributeDescription" type="text" v-model.trim="v$.attribute.attributeDescription.$model" @blur="onDataChange(v$.attribute.attributeDescription)" class="p-inputtext p-component kn-material-input" />
                                    <label for="attributeDescription">{{ $t('managers.profileAttributesManagement.form.description') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.attribute.attributeDescription" :additionalTranslateParams="{ fieldName: $t('managers.profileAttributesManagement.form.description') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown id="dataType" v-model="v$.attribute.value.$model" :options="attributeTypeValues" optionLabel="name" optionValue="value" class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input" @change="onDataChange(v$.attribute.value)" />
                                    <label for="dataType">{{ $t('managers.profileAttributesManagement.form.dataType') }} *</label>
                                </span>
                            </div>
                        </div>

                        <div class="p-inputgroup p-col-6 p-sm-12 p-md-6">
                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <RadioButton id="disableLov" name="disableLov" v-model="disableLov" :value="false" @change="hideLovDropdown" />
                                <label for="disableLov">{{ $t('managers.profileAttributesManagement.form.manualInput') }}</label>
                            </div>

                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <RadioButton id="lov" name="enableLov" :value="false" v-model="enableLov" @change="showLovDropdown" />
                                <label for="enableLov">{{ $t('managers.profileAttributesManagement.form.lov') }}</label>
                            </div>
                        </div>

                        <div class="p-field" :hidden="LovSelectHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        v-model="v$.attribute.lovId.$model"
                                        :options="lovs"
                                        optionLabel="name"
                                        optionValue="id"
                                        @change="onLoveDropdownChange"
                                        :filter="true"
                                        placeholder="Select a Country"
                                        :showClear="true"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                    />
                                    <label for="attributeDescription">{{ $t('managers.profileAttributesManagement.form.lov') }} *</label>
                                </span>
                            </div>
                        </div>

                        <div class="p-inputgroup p-col-6 p-sm-12 p-md-6">
                            <div class="p-field-radiobutton p-col-6 p-sm-12 p-md-6">
                                <InputSwitch v-model="v$.attribute.multivalue.$model" :onInput="checkSyntax()" @change="onDataChange(v$.attribute.multivalue)" />
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
                                    <RadioButton id="simple" name="syntax" :value="false" v-model="v$.attribute.syntax.$model" @change="onDataChange(v$.attribute.syntax)" />
                                    <label class="p-m-2" for="simple">{{ $t('managers.profileAttributesManagement.form.syntax.simple') }}</label>
                                    <div class="p-mt-2" v-if="v$.attribute.syntax.$model === false">
                                        * Simple = ('Italy','USA','Serbia', ...)
                                    </div>
                                </div>
                                <div class="p-col">
                                    <RadioButton id="complex" name="syntax" :value="true" v-model="v$.attribute.syntax.$model" @change="onDataChange(v$.attribute.syntax)" />
                                    <label class="p-m-2" for="complex">{{ $t('managers.profileAttributesManagement.form.syntax.complex') }}</label>
                                    <div class="p-m-2" v-if="v$.attribute.syntax.$model === true">
                                        * Complex = {;{Italy;USA;Serbia; ...}}
                                    </div>
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
import axios, { AxiosResponse } from 'axios'
import { iAttribute, iLov } from './ProfileAttributesManagement'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import RadioButton from 'primevue/radiobutton'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import ProfileAttributesManagementDescriptor from './ProfileAttributesManagementDescriptor.json'
import profileAttributesManagementValidationDescriptor from './ProfileAttributesManagementValidationDescriptor.json'

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
    computed: {
        formValid(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        selectedAttribute: {
            handler: function(attribute) {
                if (attribute.attributeId === null) {
                    this.resetForm()
                    return
                }
                this.populateForm(attribute)
            }
        }
    },
    emits: ['refreshRecordSet', 'closesForm', 'dataChanged'],
    data() {
        return {
            v$: useValidate() as any,
            apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/',
            attribute: {} as iAttribute,
            lovs: {} as iLov,
            loading: false as Boolean,
            hideForm: false as Boolean,
            isDirty: false as Boolean,
            disableLov: true as Boolean,
            enableLov: true as Boolean,
            LovSelectHidden: true as Boolean,
            syntaxSelectHidden: true as Boolean,
            profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor,
            columns: ProfileAttributesManagementDescriptor.columns,
            attributeTypeValues: ProfileAttributesManagementDescriptor.attributeTypeValues
        }
    },
    validations() {
        return {
            attribute: createValidations('attribute', profileAttributesManagementValidationDescriptor.validations.attribute)
        }
    },
    async created() {
        await this.loadLovs()
    },
    methods: {
        async loadLovs() {
            this.loading = true
            await axios
                .get(this.apiUrl + 'lovs/get/all')
                .then((response) => {
                    this.lovs = response.data
                })
                .finally(() => (this.loading = false))
        },
        resetForm() {
            Object.keys(this.attribute).forEach((k) => delete this.attribute[k])
        },
        showForm() {
            this.hideLovDropdown()
            this.syntaxSelectHidden = true
            this.resetForm()
            this.hideForm = false
        },
        async save() {
            let response: AxiosResponse
            if (this.attribute.value === 'NUMBER') {
                this.attribute.value = 'NUM'
            }
            if (this.attribute.attributeId != null) {
                response = await axios.put(this.apiUrl + 'attributes/' + this.attribute.attributeId, this.attribute, ProfileAttributesManagementDescriptor.headers)
            } else {
                response = await axios.post(this.apiUrl + 'attributes/', this.attribute, ProfileAttributesManagementDescriptor.headers)
            }
            if (response.status == 200) {
                if (response.data.errors) {
                    console.log(response.data.errors)
                } else {
                    this.$store.commit('setInfo', {
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
            this.hideLovDropdown()
            this.hideForm = false

            this.attribute = { ...attribute }

            if (typeof attribute.value === 'object' && attribute.value !== null) {
                this.attribute.value = attribute.value['type'].toUpperCase()
            }

            this.showSyntaxButtons(attribute)

            if (attribute.lovId !== null) {
                this.disableLovs()
                this.showLovDropdown()
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
        hideLovDropdown() {
            this.attribute.lovId = null
            this.enableLovs()
            this.LovSelectHidden = true
        },
        showLovDropdown() {
            this.disableLovs()
            this.LovSelectHidden = false
            this.$emit('dataChanged')
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
