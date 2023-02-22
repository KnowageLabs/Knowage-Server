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
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />
        <Message v-if="showOwnerMessage" severity="info">{{ ownerMessage }}</Message>
        <Card :style="dataSourceDescriptor.card.style">
            <template #content>
                <form class="p-fluid p-m-5">
                    <div class="p-fluid p-formgrid p-grid">
                        <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <InputText
                                    id="label"
                                    v-model.trim="v$.datasource.label.$model"
                                    class="kn-material-input"
                                    type="text"
                                    max-length="100"
                                    :class="{
                                        'p-invalid': v$.datasource.label.$invalid && v$.datasource.label.$dirty
                                    }"
                                    :disabled="readOnly || disableLabelField"
                                    @blur="v$.datasource.label.$touch()"
                                    @input="onFieldChange"
                                />
                                <label for="label" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                            </span>
                            <KnValidationMessages class="p-mt-1" :v-comp="v$.datasource.label" :additional-translate-params="{ fieldName: $t('common.name') }" />
                        </div>
                        <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <Dropdown
                                    id="dialectName"
                                    v-model="v$.datasource.dialectName.$model"
                                    class="kn-material-input"
                                    :options="availableDatabases"
                                    option-label="databaseDialect.name"
                                    option-value="databaseDialect.value"
                                    :class="{
                                        'p-invalid': v$.datasource.dialectName.$invalid && v$.datasource.dialectName.$dirty
                                    }"
                                    :disabled="readOnly"
                                    @before-show="v$.datasource.dialectName.$touch()"
                                    @change="selectDatabase($event.value)"
                                />
                                <label for="dialectName" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.dialect') }} * </label>
                            </span>
                            <KnValidationMessages class="p-mt-1" :v-comp="v$.datasource.dialectName" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.dialect') }" />
                        </div>
                    </div>

                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText id="descr" v-model.trim="datasource.descr" class="kn-material-input" type="text" max-length="160" :disabled="readOnly" @input="onFieldChange" />
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
                            <div v-if="datasource.multiSchema" class="p-field" :style="dataSourceDescriptor.pField.style">
                                <span class="p-float-label">
                                    <InputText
                                        id="schemaAttribute"
                                        v-model.trim="v$.datasource.schemaAttribute.$model"
                                        class="kn-material-input"
                                        type="text"
                                        max-length="45"
                                        :class="{
                                            'p-invalid': v$.datasource.schemaAttribute.$invalid && v$.datasource.schemaAttribute.$dirty
                                        }"
                                        :disabled="readOnly"
                                        @input="onFieldChange"
                                    />
                                    <label for="schemaAttribute" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.schemaAttribute') }} </label>
                                </span>
                                <KnValidationMessages :v-comp="v$.datasource.schemaAttribute" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.schemaAttribute') }" />
                            </div>
                        </div>

                        <label class="kn-material-input-label">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                        <div class="p-field p-formgroup-inline p-mb-3 p-mt-2" :style="dataSourceDescriptor.pField.style">
                            <div class="p-field-radiobutton">
                                <RadioButton id="readOnly" v-model="datasource.readOnly" :value="true" :disabled="datasource.writeDefault || readOnly || datasource.useForDataprep" />
                                <label for="readOnly">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                            </div>
                            <div class="p-field-radiobutton">
                                <RadioButton id="readAndWrite" v-model="datasource.readOnly" :value="false" :disabled="readOnly || !selectedDatabase.cacheSupported" />
                                <label for="readAndWrite">{{ $t('managers.dataSourceManagement.form.readAndWrite') }}</label>
                            </div>
                            <div v-if="currentUser.isSuperadmin" class="p-field-checkbox" :style="dataSourceDescriptor.pField.style">
                                <Checkbox id="writeDefault" v-model="datasource.writeDefault" :binary="true" :disabled="readOnly || !selectedDatabase.cacheSupported || datasource.readOnly || !currentUser.isSuperadmin" />
                                <label for="writeDefault" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.writeDefault') }} </label>
                            </div>
                            <div v-if="currentUser.isSuperadmin" class="p-field-checkbox">
                                <Checkbox id="useForDataprep" v-model="datasource.useForDataprep" :binary="true" :disabled="readOnly || !selectedDatabase.cacheSupported || datasource.readOnly || !currentUser.isSuperadmin" />
                                <label for="useForDataprep"> {{ $t('managers.dataSourceManagement.form.useForDataprep') }} </label>
                            </div>
                        </div>

                        <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                        <div class="p-field p-formgroup-inline p-mt-2" :style="dataSourceDescriptor.pField.style">
                            <div class="p-field-radiobutton">
                                <RadioButton id="JDBC" v-model="jdbcOrJndi.type" :value="'JDBC'" :disabled="readOnly" @change="clearType" />
                                <label for="JDBC">JDBC</label>
                            </div>
                            <div class="p-field-radiobutton">
                                <RadioButton id="readAndWrite" v-model="jdbcOrJndi.type" :value="'JNDI'" :disabled="readOnly || !currentUser.isSuperadmin" @change="clearType" />
                                <label for="JNDI">JNDI</label>
                            </div>
                        </div>

                        <div v-if="jdbcOrJndi.type == 'JNDI'" class="p-field" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <InputText
                                    id="jndi"
                                    v-model.trim="v$.datasource.jndi.$model"
                                    class="kn-material-input"
                                    type="text"
                                    max-length="160"
                                    :class="{
                                        'p-invalid': v$.datasource.jndi.$invalid && v$.datasource.jndi.$dirty
                                    }"
                                    :disabled="readOnly"
                                    @blur="v$.datasource.jndi.$touch()"
                                    @input="onFieldChange"
                                />
                                <label for="jndi" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.jndi') }} * </label>
                                <small id="jndi-help">{{ $t('managers.dataSourceManagement.form.jndiInfo') }}</small>
                            </span>
                            <KnValidationMessages :v-comp="v$.datasource.jndi" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.jndi') }" />
                        </div>
                        <div class="p-fluid p-formgrid p-grid">
                            <div v-if="jdbcOrJndi.type == 'JDBC'" class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                                <span class="p-float-label">
                                    <InputText id="user" v-model.trim="datasource.user" class="kn-material-input" type="text" max-length="50" :disabled="readOnly" @input="onFieldChange" />
                                    <label for="user" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.user') }}</label>
                                </span>
                            </div>
                            <div v-if="jdbcOrJndi.type == 'JDBC'" class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style">
                                <span class="p-float-label">
                                    <InputText id="pwd" v-model.trim="datasource.pwd" class="kn-material-input" type="password" max-length="50" :disabled="readOnly" @input="onFieldChange" />
                                    <label for="pwd" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.pwd') }}</label>
                                </span>
                            </div>
                        </div>

                        <div v-if="jdbcOrJndi.type == 'JDBC'" class="p-field" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <InputText
                                    id="urlConnection"
                                    v-model.trim="v$.datasource.urlConnection.$model"
                                    class="kn-material-input"
                                    type="text"
                                    max-length="500"
                                    :class="{
                                        'p-invalid': v$.datasource.urlConnection.$invalid && v$.datasource.urlConnection.$dirty
                                    }"
                                    :disabled="readOnly"
                                    @blur="v$.datasource.urlConnection.$touch()"
                                    @input="onFieldChange"
                                />
                                <label for="urlConnection" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.urlConnection') }} * </label>
                            </span>
                            <KnValidationMessages :v-comp="v$.datasource.urlConnection" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.urlConnection') }" />
                        </div>

                        <div v-if="jdbcOrJndi.type == 'JDBC'" class="p-field" :style="dataSourceDescriptor.pField.style">
                            <span class="p-float-label">
                                <InputText
                                    id="driver"
                                    v-model.trim="v$.datasource.driver.$model"
                                    class="kn-material-input"
                                    type="text"
                                    max-length="50"
                                    :class="{
                                        'p-invalid': v$.datasource.driver.$invalid && v$.datasource.driver.$dirty
                                    }"
                                    :disabled="readOnly"
                                    @blur="v$.datasource.driver.$touch()"
                                    @input="onFieldChange"
                                />
                                <label for="driver" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.driver') }} * </label>
                            </span>
                            <KnValidationMessages :v-comp="v$.datasource.driver" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.driver') }" />
                        </div>
                    </template>
                </form>
            </template>
        </Card>

        <template v-if="isVisible()">
            <div v-if="jdbcOrJndi.type == 'JDBC'">
                <DataSourceAdvancedOptions :advanced-options="jdbcPoolConfiguration" :is-read-only="readOnly" @fieldChanged="onAdvancedOptionsChange" />
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
    emits: ['touched', 'closed', 'inserted'],

    setup() {
        const store = mainStore()
        return { store }
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

    mounted() {
        this.currentUser = { ...this.user } as any
        this.availableDatabases = this.databases
        if (this.selectedDatasource) {
            this.loadExistingDataSourceValues()
        } else {
            this.createNewDataSourceValues()
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
            const url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/test'
            let dsToTest = {} as any
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
            const url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasources/'
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
