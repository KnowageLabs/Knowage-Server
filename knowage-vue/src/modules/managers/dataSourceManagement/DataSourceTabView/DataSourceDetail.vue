<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ datasource.label }}</template>
        <template #right>
            <Button icon="pi pi-info" class="p-button-text p-button-rounded p-button-plain" :disabled="readOnly" @click="testDataSource" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="readOnly" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <Card :style="dataSourceDescriptor.card.style">
        <template #content>
            {{ datasource }}

            <form class="p-fluid p-m-5">
                <div class="p-fluid p-formgrid p-grid">
                    <!-- LABEL disabled if readOnly or editing and existing datasource-->
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
                        <KnValidationMessages
                            class="p-mt-1"
                            :vComp="v$.datasource.label"
                            :additionalTranslateParams="{
                                fieldName: $t('common.name')
                            }"
                        />
                    </div>

                    <!-- DIALECT disabled if readOnly -->
                    <!-- need to show whole object on selection -->
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
                        <KnValidationMessages
                            class="p-mt-1"
                            :vComp="v$.datasource.dialectName"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.dataSourceManagement.form.dialect')
                            }"
                        />
                    </div>
                </div>

                <!-- DESCRIPTION disabled if readOnly-->
                <div class="p-field" :style="dataSourceDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="descr"
                            class="kn-material-input"
                            type="text"
                            maxLength="160"
                            v-model.trim="v$.datasource.descr.$model"
                            :class="{
                                'p-invalid': v$.datasource.descr.$invalid && v$.datasource.descr.$dirty
                            }"
                            @blur="v$.datasource.descr.$touch()"
                            @input="onFieldChange"
                            :disabled="readOnly"
                        />
                        <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.datasource.descr"
                        :additionalTranslateParams="{
                            fieldName: $t('common.description')
                        }"
                    />
                </div>

                <!-- MULTISCHEMA disabled if readOnly, hidden if type is not JNDI -->
                <div v-if="jdbcOrJndi.type == 'JNDI'">
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <Checkbox id="multiSchema" v-model="datasource.multiSchema" :binary="true" :disabled="readOnly" />
                            <label for="multiSchema" class="kn-material-input-label" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.multischema') }} </label>
                        </span>
                    </div>

                    <!-- SCHEMA ATTRIBUTE disabled if readOnly -->
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
                                @blur="v$.datasource.schemaAttribute.$touch()"
                                @input="onFieldChange"
                                :disabled="readOnly"
                            />
                            <label for="schemaAttribute" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.schemaAttribute') }} </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.datasource.schemaAttribute"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.dataSourceManagement.form.schemaAttribute')
                            }"
                        />
                    </div>
                </div>

                <label class="kn-material-input-label">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                <div class="p-field p-formgroup-inline p-mb-3 p-mt-2" :style="dataSourceDescriptor.pField.style">
                    <div class="p-field-radiobutton">
                        <RadioButton id="readOnly" :value="true" v-model="datasource.readOnly" :disabled="datasource.writeDefault || readOnly" />
                        <label for="readOnly">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                    </div>
                    <!-- READ AND WRITE BUTTON disabled if !selectedDatabase.cacheSupported || if readOnly -->
                    <div class="p-field-radiobutton">
                        <RadioButton id="readAndWrite" :value="false" v-model="datasource.readOnly" :disabled="readOnly || !selectedDatabase.cacheSupported" />
                        <label for="readAndWrite">{{ $t('managers.dataSourceManagement.form.readAndWrite') }}</label>
                    </div>
                    <!-- WRITE DEFAULT/USE AS CACHE  readOnly || !selectedDatabase.cacheSupported || selectedDataSource.readOnly == 1) || !isSuperAdminFunction() -->
                    <span class="p-float-label" v-if="!datasource.readOnly">
                        <Checkbox id="writeDefault" v-model="datasource.writeDefault" :binary="true" :disabled="readOnly || !selectedDatabase.cacheSupported || datasource.readOnly || !currentUser.isSuperadmin" />
                        <label for="writeDefault" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.writeDefault') }} </label>
                    </span>
                </div>

                <!-- TYPE CONTAINER-->
                <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                <div class="p-field p-formgroup-inline p-mt-2" :style="dataSourceDescriptor.pField.style">
                    <!-- JDBC disabled if readOnly -->
                    <div class="p-field-radiobutton">
                        <RadioButton id="JDBC" :value="'JDBC'" v-model="jdbcOrJndi.type" :disabled="readOnly" />
                        <label for="JDBC">JDBC</label>
                    </div>
                    <!-- JNDI disabled if readOnly && user is NOT SUPERADMIN-->
                    <div class="p-field-radiobutton">
                        <RadioButton id="readAndWrite" :value="'JNDI'" v-model="jdbcOrJndi.type" :disabled="readOnly && !currentUser.isSuperadmin" />
                        <label for="JNDI">JNDI</label>
                    </div>
                </div>

                <!-- JNDI OPTIONS disabled if readOnly || type isnt JNDI-->
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
                            v-tooltip.top="$t('managers.dataSourceManagement.form.jndiInfo')"
                            :disabled="readOnly"
                        />
                        <label for="jndi" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.jndi') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.datasource.jndi"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.dataSourceManagement.form.jndi')
                        }"
                    />
                </div>
                <!-- JDBC options disabled if readOnly || type isnt JDBC -->
                <!-- USER & PASSWORD CONTAINER -->
                <div class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                        <span class="p-float-label">
                            <InputText
                                id="user"
                                class="kn-material-input"
                                type="text"
                                maxLength="50"
                                v-model.trim="v$.datasource.user.$model"
                                :class="{ 'p-invalid': v$.datasource.user.$invalid && v$.datasource.user.$dirty }"
                                @blur="v$.datasource.user.$touch()"
                                @input="onFieldChange"
                                :disabled="readOnly"
                            />
                            <label for="user" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.user') }}</label>
                        </span>
                        <KnValidationMessages :vComp="v$.datasource.user" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.user') }" />
                    </div>

                    <!-- PASSWORD, nesto je cudno uradjeno u source kodu, treba da se razjasni -->
                    <div class="p-field p-col-12 p-md-6" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                        <span class="p-float-label">
                            <InputText
                                id="pwd"
                                class="kn-material-input"
                                type="password"
                                maxLength="50"
                                v-model.trim="v$.datasource.pwd.$model"
                                :class="{ 'p-invalid': v$.datasource.pwd.$invalid && v$.datasource.pwd.$dirty }"
                                @blur="v$.datasource.pwd.$touch()"
                                @input="onFieldChange"
                                :disabled="readOnly"
                            />
                            <label for="pwd" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.pwd') }}</label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.datasource.pwd"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.dataSourceManagement.form.pwd')
                            }"
                        />
                    </div>
                </div>

                <!-- URL disabled if readOnly -->
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
                    <KnValidationMessages
                        :vComp="v$.datasource.urlConnection"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.dataSourceManagement.form.urlConnection')
                        }"
                    />
                </div>

                <!-- DRIVER disabled if readOnly -->
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
                    <KnValidationMessages
                        :vComp="v$.datasource.driver"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.dataSourceManagement.form.driver')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>

    <div v-if="jdbcOrJndi.type == 'JDBC'">
        <DataSourceAdvancedOptions :advancedOptions="jdbcPoolConfiguration" :isReadOnly="readOnly" @fieldChanged="onAdvancedOptionsChange" />
    </div>
</template>

<script lang="ts">
/* eslint-disable no-prototype-builtins */
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'
import dataSourceDescriptor from '../DataSourceDescriptor.json'
import dataSourceDetailValidationDescriptor from './DataSourceDetailValidationDescriptor.json'
import DataSourceAdvancedOptions from '../DataSourceAdvancedOptions/DataSourceAdvancedOptions.vue'
import Dropdown from 'primevue/dropdown'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'
import Card from 'primevue/card'
import Tooltip from 'primevue/tooltip'

export default defineComponent({
    components: {
        Card,
        KnValidationMessages,
        Dropdown,
        RadioButton,
        Checkbox,
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
    directives: {
        tooltip: Tooltip
    },
    emits: ['touched', 'closed', 'inserted'],
    computed: {
        operation() {
            if (this.id) {
                return 'update'
            }
            return 'insert'
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
            loading: false,
            touched: false,
            readOnly: false,
            disableLabelField: false
        }
    },
    validations() {
        return {
            datasource: createValidations('datasource', dataSourceDetailValidationDescriptor.validations.datasource)
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
    watch: {
        id() {
            if (this.id == undefined) {
                this.createNewDataSourceValues()
            } else {
                this.loadExistingDataSourceValues()
            }
        },
        databases() {
            this.availableDatabases = this.databases
            this.selectDatabase(this.datasource.dialectName)
            this.isReadOnly()
        },
        user() {
            this.currentUser = { ...this.user } as any
        }
    },
    methods: {
        connectionType() {
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
            this.disableLabelField = false
        },

        loadExistingDataSourceValues() {
            console.log('loadExistingDataSourceValues() {')
            this.datasource = { ...this.selectedDatasource } as any
            this.jdbcPoolConfiguration = { ...this.datasource.jdbcPoolConfiguration } as any
            this.disableLabelField = true
            this.connectionType()
            this.selectDatabase(this.datasource.dialectName)
            this.isReadOnly()
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

        isReadOnly() {
            if (this.selectedDatasource) {
                if (this.currentUser.isSuperadmin || (this.currentUser.userId == this.datasource.owner && (!this.datasource.hasOwnProperty('jndi') || this.datasource.jndi == ''))) {
                    this.$store.commit('setInfo', {
                        title: this.$t('YOU ARE THE OWNER or SUPERADMIN')
                    })
                    this.readOnly = false
                } else {
                    this.$store.commit('setInfo', {
                        title: this.$t('Information'),
                        msg: this.$t('managers.dataSourceManagement.form.notOwner')
                    })
                    this.readOnly = true
                }
            } else {
                this.readOnly = false
            }
        },

        async testDataSource() {
            // if (this.v$.$invalid) {
            //     console.log('validError', this.v$)
            //     return
            // }
            var url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'datasourcestest/2.0/test/'
            var dsToTest = {} as any
            dsToTest = { ...this.datasource }
            dsToTest.type = this.jdbcOrJndi.type
            if (dsToTest.hasOwnProperty('jdbcPoolConfiguration')) {
                this.convertToMili(dsToTest)
            }

            await axios.post(url, dsToTest).then((response) => {
                if (response.data.error) {
                    this.$store.commit('setError', { title: this.$t('managers.dataSourceManagement.form.errorTitle'), msg: response.data.error })
                    console.log(response.data)
                } else {
                    this.$store.commit('setInfo', { msg: this.$t('managers.dataSourceManagement.form.testOk') })
                    console.log(response.data)
                }
            })
        },

        async createOrUpdate(url, dsToSave) {
            return this.operation === 'update' ? axios.put(url, dsToSave) : axios.post(url, dsToSave)
        },

        async handleSubmit() {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasources/'
            let dsToSave = {} as any
            dsToSave = { ...this.datasource }
            // delete dsToSave.type
            if (dsToSave.hasOwnProperty('jdbcPoolConfiguration')) {
                this.convertToMili(dsToSave)
            }

            await this.createOrUpdate(url, dsToSave).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'Error', msg: response.data.error })
                } else {
                    this.$store.commit('setInfo', { title: 'Ok', msg: 'Saved OK' })
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
            console.log('CHANGED')
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/datasource')
            this.$emit('closed')
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
