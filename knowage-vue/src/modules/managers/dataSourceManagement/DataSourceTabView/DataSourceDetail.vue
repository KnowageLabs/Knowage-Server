<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start>{{ datasource.label }}</template>
        <template #end>
            <Button class="p-button-text p-button-rounded p-button-plain p-jc-center" :disabled="readOnly" @click="testDataSource">{{ $t('common.test') }}</Button>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="readOnly || buttonDisabled" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <div class="kn-overflow-y">
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Message severity="info" v-if="showOwnerMessage">{{ ownerMessage }}</Message>
        <Card :style="dataSourceDescriptor.card.style">
            <template #content>
                <form class="p-fluid p-m-5">
                    <div class="p-fluid p-formgrid p-grid">
                        <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <InputText
                                    id="label"
                                    class="kn-material-input"
                                    type="text"
                                    maxLength="100"
                                    v-model.trim="v$.datasource.label.$model"
                                    :class="{
                                        'p-invalid': v$.datasource.label.$invalid && v$.datasource.label.$dirty
                                    }"
                                    @blur="v$.datasource.label.$touch()"
                                    @input="onFieldChange"
                                    :disabled="readOnly || disableLabelField"
                                />
                                <label for="label" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                            </span>
                            <KnValidationMessages class="p-mt-1" :vComp="v$.datasource.label" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                        </div>
                        <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <Dropdown
                                    id="dialectName"
                                    class="kn-material-input"
                                    :options="availableDatabases"
                                    optionLabel="databaseDialect.name"
                                    optionValue="databaseDialect.value"
                                    v-model="v$.datasource.dialectName.$model"
                                    :class="{
                                        'p-invalid': v$.datasource.dialectName.$invalid && v$.datasource.dialectName.$dirty
                                    }"
                                    @before-show="v$.datasource.dialectName.$touch()"
                                    @change="selectDatabase($event.value)"
                                    :disabled="readOnly"
                                />
                                <label for="dialectName" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.dialect') }} * </label>
                            </span>
                            <KnValidationMessages class="p-mt-1" :vComp="v$.datasource.dialectName" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.dialect') }" />
                        </div>
                    </div>

                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText id="descr" class="kn-material-input" type="text" maxLength="160" v-model.trim="datasource.descr" @input="onFieldChange" :disabled="readOnly" />
                            <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                        </span>
                    </div>

                    <template v-if="isVisible()">
                        <div v-if="jdbcOrJndi.type == 'JNDI'">
                            <div class="p-field" :style="dataSourceDescriptor.pField.style">
                                <span class="p-float-label">
                                    <Checkbox id="multiSchema" v-model="datasource.multiSchema" :binary="true" :disabled="readOnly" />
                                    <label for="multiSchema" class="kn-material-input-label" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.multischema') }} </label>
                                </span>
                            </div>
                            <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="datasource.multiSchema">
                                <span class="p-float-label">
                                    <InputText
                                        id="schemaAttribute"
                                        class="kn-material-input"
                                        type="text"
                                        maxLength="45"
                                        v-model.trim="v$.datasource.schemaAttribute.$model"
                                        :class="{
                                            'p-invalid': v$.datasource.schemaAttribute.$invalid && v$.datasource.schemaAttribute.$dirty
                                        }"
                                        @input="onFieldChange"
                                        :disabled="readOnly"
                                    />
                                    <label for="schemaAttribute" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.schemaAttribute') }} </label>
                                </span>
                                <KnValidationMessages :vComp="v$.datasource.schemaAttribute" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.schemaAttribute') }" />
                            </div>
                        </div>

                        <label class="kn-material-input-label">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                        <div class="p-field p-formgroup-inline p-mb-3 p-mt-2" :style="dataSourceDescriptor.pField.style">
                            <div class="p-field-radiobutton">
                                <RadioButton id="readOnly" :value="true" v-model="datasource.readOnly" :disabled="datasource.writeDefault || readOnly || datasource.useForDataprep" />
                                <label for="readOnly">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                            </div>
                            <div class="p-field-radiobutton">
                                <RadioButton id="readAndWrite" :value="false" v-model="datasource.readOnly" :disabled="readOnly || !selectedDatabase.cacheSupported" />
                                <label for="readAndWrite">{{ $t('managers.dataSourceManagement.form.readAndWrite') }}</label>
                            </div>
                            <div class="p-field-checkbox" :style="dataSourceDescriptor.pField.style" v-if="currentUser.isSuperadmin">
                                <Checkbox id="writeDefault" v-model="datasource.writeDefault" :binary="true" :disabled="readOnly || !selectedDatabase.cacheSupported || datasource.readOnly || !currentUser.isSuperadmin" />
                                <label for="writeDefault" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.writeDefault') }} </label>
                            </div>
                            <div class="p-field-checkbox" v-if="currentUser.isSuperadmin">
                                <Checkbox id="useForDataprep" v-model="datasource.useForDataprep" :binary="true" :disabled="readOnly || !selectedDatabase.cacheSupported || datasource.readOnly || !currentUser.isSuperadmin" />
                                <label for="useForDataprep"> {{ $t('managers.dataSourceManagement.form.useForDataprep') }} </label>
                            </div>
                        </div>

                        <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                        <div class="p-field p-formgroup-inline p-mt-2" :style="dataSourceDescriptor.pField.style">
                            <div class="p-field-radiobutton">
                                <RadioButton id="JDBC" :value="'JDBC'" v-model="jdbcOrJndi.type" @change="clearType" :disabled="readOnly" />
                                <label for="JDBC">JDBC</label>
                            </div>
                            <div class="p-field-radiobutton">
                                <RadioButton id="readAndWrite" :value="'JNDI'" v-model="jdbcOrJndi.type" @change="clearType" :disabled="readOnly || !currentUser.isSuperadmin" />
                                <label for="JNDI">JNDI</label>
                            </div>
                        </div>

                        <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JNDI'">
                            <span class="p-float-label">
                                <InputText
                                    id="jndi"
                                    class="kn-material-input"
                                    type="text"
                                    maxLength="160"
                                    v-model.trim="v$.datasource.jndi.$model"
                                    :class="{
                                        'p-invalid': v$.datasource.jndi.$invalid && v$.datasource.jndi.$dirty
                                    }"
                                    @blur="v$.datasource.jndi.$touch()"
                                    @input="onFieldChange"
                                    :disabled="readOnly"
                                />
                                <label for="jndi" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.jndi') }} * </label>
                                <small id="jndi-help">{{ $t('managers.dataSourceManagement.form.jndiInfo') }}</small>
                            </span>
                            <KnValidationMessages :vComp="v$.datasource.jndi" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.jndi') }" />
                        </div>
                        <div class="p-fluid p-formgrid p-grid">
                            <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                                <span class="p-float-label">
                                    <InputText id="user" class="kn-material-input" type="text" maxLength="50" v-model.trim="datasource.user" @input="onFieldChange" :disabled="readOnly" />
                                    <label for="user" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.user') }}</label>
                                </span>
                            </div>
                            <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                                <span class="p-float-label">
                                    <InputText id="pwd" class="kn-material-input" type="password" maxLength="50" v-model.trim="datasource.pwd" @input="onFieldChange" :disabled="readOnly" />
                                    <label for="pwd" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.pwd') }}</label>
                                </span>
                            </div>
                        </div>

                        <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                            <span class="p-float-label">
                                <InputText
                                    id="urlConnection"
                                    class="kn-material-input"
                                    type="text"
                                    maxLength="500"
                                    v-model.trim="v$.datasource.urlConnection.$model"
                                    :class="{
                                        'p-invalid': v$.datasource.urlConnection.$invalid && v$.datasource.urlConnection.$dirty
                                    }"
                                    @blur="v$.datasource.urlConnection.$touch()"
                                    @input="onFieldChange"
                                    :disabled="readOnly"
                                />
                                <label for="urlConnection" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.urlConnection') }} * </label>
                            </span>
                            <KnValidationMessages :vComp="v$.datasource.urlConnection" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.urlConnection') }" />
                        </div>

                        <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                            <span class="p-float-label">
                                <InputText
                                    id="driver"
                                    class="kn-material-input"
                                    type="text"
                                    maxLength="50"
                                    v-model.trim="v$.datasource.driver.$model"
                                    :class="{
                                        'p-invalid': v$.datasource.driver.$invalid && v$.datasource.driver.$dirty
                                    }"
                                    @blur="v$.datasource.driver.$touch()"
                                    @input="onFieldChange"
                                    :disabled="readOnly"
                                />
                                <label for="driver" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.driver') }} * </label>
                            </span>
                            <KnValidationMessages :vComp="v$.datasource.driver" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.driver') }" />
                        </div>
                    </template>
                </form>
            </template>
        </Card>

        <template v-if="isVisible()">
            <div v-if="jdbcOrJndi.type == 'JDBC'">
                <DataSourceAdvancedOptions :advancedOptions="jdbcPoolConfiguration" :isReadOnly="readOnly" @fieldChanged="onAdvancedOptionsChange" />
            </div>
        </template>
    </div>
</template>

<script lang="ts">
/* eslint-disable no-prototype-builtins */
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import useValidate from '@vuelidate/core'
import dataSourceDescriptor from '../DataSourceDescriptor.json'
import dataSourceDetailValidationDescriptor from './DataSourceDetailValidationDescriptor.json'
import DataSourceAdvancedOptions from '../DataSourceAdvancedOptions/DataSourceAdvancedOptions.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dropdown from 'primevue/dropdown'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'
import Card from 'primevue/card'
import Tooltip from 'primevue/tooltip'
import Message from 'primevue/message'
import mainStore from '../../../../App.store'

export default defineComponent({
    emits: ['touched', 'closed', 'inserted'],

    directives: {
        tooltip: Tooltip
    },

    components: {
        Card,
        KnValidationMessages,
        Dropdown,
        RadioButton,
        Checkbox,
        Message,
        DataSourceAdvancedOptions
    },

    props: {
        selectedDatasource: {
            type: Object,
            required: false
        },
        user: {
            type: Object,
            required: false
        },
        databases: Array,
        id: String
    },

    setup() {
        const store = mainStore()
        return { store }
    },

    mounted() {
        this.currentUser = { ...this.user } as any
        this.availableDatabases = this.databases
        if (this.selectedDatasource) {
            this.loadExistingDataSourceValues()
        } else {
            this.createNewDataSourceValues()
        }
    },

    watch: {
        id() {
            if (this.id == undefined) {
                this.createNewDataSourceValues()
            } else {
                this.loadExistingDataSourceValues()
            }
            this.touched = false
        },
        databases() {
            this.availableDatabases = this.databases
            this.selectDatabase(this.datasource.dialectName)
            this.checkIfReadOnly()
        },
        user() {
            this.currentUser = { ...this.user } as any
        }
    },

    computed: {
        operation() {
            if (this.id) {
                return 'update'
            }
            return 'insert'
        },
        buttonDisabled(): any {
            if (this.v$.$invalid) {
                return true
            }
            return false
        }
    },

    data() {
        return {
            v$: useValidate() as any,
            dataSourceDescriptor,
            datasource: {} as any,
            availableDatabases: [] as any,
            selectedDatabase: {} as any,
            jdbcOrJndi: {} as any,
            jdbcPoolConfiguration: {} as any,
            currentUser: {} as any,
            ownerMessage: '',
            showOwnerMessage: false,
            loading: false,
            touched: false,
            readOnly: false,
            disableLabelField: false
        }
    },

    validations() {
        const jndiTypeRequired = (jndiType) => (value) => {
            return this.jdbcOrJndi.type != jndiType || value
        }
        const customValidators: ICustomValidatorMap = {
            'jndi-name-required': jndiTypeRequired('JNDI'),
            'jdbc-data-required': jndiTypeRequired('JDBC')
        }
        const validationObject = {
            datasource: createValidations('datasource', dataSourceDetailValidationDescriptor.validations.datasource, customValidators)
        }
        return validationObject
    },

    methods: {
        setConnetionType() {
            if (this.datasource.driver) {
                this.jdbcOrJndi.type = 'JDBC'
            }
            if (this.datasource.jndi != undefined && this.datasource.jndi != '') {
                this.jdbcOrJndi.type = 'JNDI'
            }
        },

        createNewDataSourceValues() {
            this.jdbcOrJndi.type = 'JDBC'
            this.jdbcPoolConfiguration = { ...dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration }
            this.datasource = { ...dataSourceDescriptor.newDataSourceValues }
            this.datasource.owner = this.user?.userId
            this.disableLabelField = false
            this.showOwnerMessage = false
            this.checkIfReadOnly()
        },

        loadExistingDataSourceValues() {
            this.datasource = { ...this.selectedDatasource } as any
            this.jdbcPoolConfiguration = { ...this.datasource.jdbcPoolConfiguration } as any
            this.disableLabelField = true
            this.setConnetionType()
            this.selectDatabase(this.datasource.dialectName)
            this.checkIfReadOnly()
        },

        convertToMili(dsToSave) {
            dsToSave.jdbcPoolConfiguration.maxWait *= 1000
            dsToSave.jdbcPoolConfiguration.timeBetweenEvictionRuns *= 1000
            dsToSave.jdbcPoolConfiguration.minEvictableIdleTimeMillis *= 1000
        },

        selectDatabase(selectedDatabaseDialect) {
            this.availableDatabases.forEach((database) => {
                if (database.databaseDialect.value == selectedDatabaseDialect) {
                    this.selectedDatabase = database
                }
            })
            if (!this.selectedDatabase.cacheSupported) {
                this.datasource.writeDefault = false
                this.datasource.readOnly = true
            }
        },

        clearType() {
            if (!this.datasource.hasOwnProperty('dsId')) {
                if (this.jdbcOrJndi.type == 'JDBC') {
                    this.datasource.jndi = ''
                    this.datasource.jdbcPoolConfiguration = { ...dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration }
                } else {
                    this.datasource.urlConnection = ''
                    this.datasource.user = ''
                    this.datasource.pwd = ''
                    this.datasource.driver = ''
                    delete this.datasource.jdbcPoolConfiguration
                }
            } else {
                if (this.jdbcOrJndi.type == 'JDBC') {
                    this.datasource.jndi = ''
                    if (!this.datasource.hasOwnProperty('jdbcPoolConfiguration')) {
                        this.jdbcPoolConfiguration = { ...dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration }
                        this.datasource.jdbcPoolConfiguration = { ...dataSourceDescriptor.newDataSourceValues.jdbcPoolConfiguration }
                    }
                }
            }
        },

        checkIfReadOnly() {
            if (this.selectedDatasource) {
                if (this.currentUser.isSuperadmin || (this.currentUser.userId == this.datasource.owner && (!this.datasource.hasOwnProperty('jndi') || this.datasource.jndi == ''))) {
                    this.showOwnerMessage = false
                    this.readOnly = false
                } else {
                    this.ownerMessage = this.$t('managers.dataSourceManagement.form.notOwner')
                    this.showOwnerMessage = true
                    this.readOnly = true
                }
            } else {
                this.readOnly = false
            }
        },

        async testDataSource() {
            var url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/test'
            var dsToTest = {} as any
            dsToTest = { ...this.datasource }
            dsToTest.type = this.jdbcOrJndi.type

            await this.$http.post(url, dsToTest).then((response: AxiosResponse<any>) => {
                if (response.data.error) {
                    this.store.setError({ title: this.$t('managers.dataSourceManagement.form.errorTitle'), msg: response.data.error })
                } else {
                    this.store.setInfo({ msg: this.$t('managers.dataSourceManagement.form.testOk') })
                }
            })
        },

        async createOrUpdate(url, dsToSave) {
            return this.operation === 'update' ? this.$http.put(url, dsToSave) : this.$http.post(url, dsToSave)
        },

        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/'
            let dsToSave = {} as any
            dsToSave = { ...this.datasource }

            if (dsToSave.hasOwnProperty('jdbcPoolConfiguration')) {
                this.convertToMili(dsToSave)
            }

            await this.createOrUpdate(url, dsToSave).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.store.setError({ title: 'Error', msg: response.data.error })
                } else {
                    this.store.setInfo({ title: 'Ok', msg: 'Saved OK' })
                }
            })
            this.$emit('inserted')
            this.touched = false
        },

        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        },
        onAdvancedOptionsChange(event) {
            this.datasource.jdbcPoolConfiguration[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },
        onFieldChange() {
            this.touched = true
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push({ name: 'datasource-hint' })
            this.$emit('closed')
        },
        isVisible() {
            return this.datasource.owner == this.user?.userId || this.user?.isSuperadmin
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
