<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ datasource.label }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <Card :style="dataSourceDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <!-- LABEL -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            v-model.trim="v$.datasource.label.$model"
                            :class="{
                                'p-invalid': v$.datasource.label.$invalid && v$.datasource.label.$dirty
                            }"
                            maxLength="100"
                            @blur="v$.datasource.label.$touch()"
                            @input="onFieldChange('label', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.datasource.label"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>

                <!-- DESCRIPTION -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="descr"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            v-model.trim="v$.datasource.descr.$model"
                            :class="{
                                'p-invalid': v$.datasource.descr.$invalid && v$.datasource.descr.$dirty
                            }"
                            maxLength="160"
                            @blur="v$.datasource.descr.$touch()"
                            @input="onFieldChange('descr', $event.target.value)"
                            data-test="name-input"
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

                <!-- DIALECT -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style">
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
                            @change="onFieldChange('dialectName', $event.value)"
                        />
                        <label for="dialectName" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.dialect') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.datasource.dialectName"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.dataSourceManagement.form.dialect')
                        }"
                    />
                </div>

                <div v-if="jdbcOrJndi.type == 'JNDI'">
                    <!-- MULTISCHEMA -->
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <Checkbox id="multiSchema" v-model="datasource.multiSchema" :binary="true" />
                            <label for="multiSchema" class="kn-material-input-label" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.multischema') }} </label>
                        </span>
                    </div>

                    <!-- SCHEMA ATTRIBUTE -->
                    <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="datasource.multiSchema">
                        <span class="p-float-label">
                            <InputText
                                id="schemaAttribute"
                                class="kn-material-input"
                                type="text"
                                :disabled="disableField"
                                v-model.trim="v$.datasource.schemaAttribute.$model"
                                :class="{
                                    'p-invalid': v$.datasource.schemaAttribute.$invalid && v$.datasource.schemaAttribute.$dirty
                                }"
                                maxLength="45"
                                @blur="v$.datasource.schemaAttribute.$touch()"
                                @input="onFieldChange('schemaAttribute', $event.target.value)"
                                data-test="name-input"
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

                <div class="p-field" :style="dataSourceDescriptor.pField.style">
                    <!-- READ ONLY -->
                    <label class="kn-material-input-label">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                    <div class="p-field-radiobutton">
                        <RadioButton id="readOnly" :value="true" v-model="datasource.readOnly" :disabled="datasource.writeDefault" />
                        <label for="readOnly">{{ $t('managers.dataSourceManagement.form.readOnly') }}</label>
                    </div>
                    <div class="p-field-radiobutton">
                        <RadioButton id="readAndWrite" :value="false" v-model="datasource.readOnly" />
                        <label for="readAndWrite">{{ $t('managers.dataSourceManagement.form.readAndWrite') }}</label>
                    </div>

                    <!-- WRITE DEFAULT -->
                    <span class="p-float-label" v-if="!datasource.readOnly">
                        <Checkbox id="writeDefault" v-model="datasource.writeDefault" :binary="true" :disabled="datasource.readOnly" />
                        <label for="writeDefault" :style="dataSourceDescriptor.checkboxLabel.style"> {{ $t('managers.dataSourceManagement.form.writeDefault') }} </label>
                    </span>
                </div>

                <!-- TYPE -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style">
                    <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                    <div class="p-field-radiobutton">
                        <RadioButton id="JDBC" :value="'JDBC'" v-model="jdbcOrJndi.type" />
                        <label for="JDBC">JDBC</label>
                    </div>
                    <div class="p-field-radiobutton">
                        <RadioButton id="readAndWrite" :value="'JNDI'" v-model="jdbcOrJndi.type" />
                        <label for="JNDI">JNDI</label>
                    </div>
                </div>

                <!-- JNDI -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JNDI'">
                    <span class="p-float-label">
                        <InputText
                            id="jndi"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.datasource.jndi.$model"
                            :class="{
                                'p-invalid': v$.datasource.jndi.$invalid && v$.datasource.jndi.$dirty
                            }"
                            maxLength="160"
                            @blur="v$.datasource.jndi.$touch()"
                            @input="onFieldChange('jndi', $event.target.value)"
                            data-test="name-input"
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

                <!-- JDBC -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                    <!-- URL -->
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="urlConnection"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.datasource.urlConnection.$model"
                                :class="{
                                    'p-invalid': v$.datasource.urlConnection.$invalid && v$.datasource.urlConnection.$dirty
                                }"
                                maxLength="500"
                                @blur="v$.datasource.urlConnection.$touch()"
                                @input="onFieldChange('urlConnection', $event.target.value)"
                                data-test="name-input"
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
                </div>

                <!-- USER -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="user"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.datasource.user.$model"
                                :class="{
                                    'p-invalid': v$.datasource.user.$invalid && v$.datasource.user.$dirty
                                }"
                                maxLength="50"
                                @blur="v$.datasource.user.$touch()"
                                @input="onFieldChange('user', $event.target.value)"
                                data-test="name-input"
                            />
                            <label for="user" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.user') }}</label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.datasource.user"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.dataSourceManagement.form.user')
                            }"
                        />
                    </div>
                </div>

                <!-- PASSWORD -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="pwd"
                                class="kn-material-input"
                                type="password"
                                placeholder="Only If Changed"
                                v-model.trim="v$.datasource.pwd.$model"
                                :class="{
                                    'p-invalid': v$.datasource.pwd.$invalid && v$.datasource.pwd.$dirty
                                }"
                                maxLength="50"
                                @blur="v$.datasource.pwd.$touch()"
                                @input="onFieldChange('pwd', $event.target.value)"
                                data-test="name-input"
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

                <!-- DRIVER -->
                <div class="p-field" :style="dataSourceDescriptor.pField.style" v-if="jdbcOrJndi.type == 'JDBC'">
                    <div class="p-field" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="driver"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.datasource.driver.$model"
                                :class="{
                                    'p-invalid': v$.datasource.driver.$invalid && v$.datasource.driver.$dirty
                                }"
                                maxLength="50"
                                @blur="v$.datasource.driver.$touch()"
                                @input="onFieldChange('driver', $event.target.value)"
                                data-test="name-input"
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
                </div>

                {{ datasource }}
            </form>
        </template>
    </Card>

    <div v-if="jdbcOrJndi.type == 'JDBC'">
        <DataSourceAdvancedOptions :advancedOptions="jdbcPoolConfiguration" @fieldChanged="onAdvancedOptionsChange" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Card from 'primevue/card'
import useValidate from '@vuelidate/core'
import dataSourceDescriptor from '../DataSourceDescriptor.json'
import dataSourceDetailValidationDescriptor from './DataSourceDetailValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dropdown from 'primevue/dropdown'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'
import DataSourceAdvancedOptions from '../DataSourceAdvancedOptions/DataSourceAdvancedOptions.vue'
// import Accordion from 'primevue/accordion'
// import AccordionTab from 'primevue/accordiontab'

export default defineComponent({
    components: {
        Card,
        KnValidationMessages,
        Dropdown,
        RadioButton,
        Checkbox,
        DataSourceAdvancedOptions
        // Accordion,
        // AccordionTab
    },
    props: {
        selectedDatasource: {
            type: Object,
            required: false
        },
        databases: Array
    },
    emits: ['touched', 'closed', 'inserted', 'fieldChanged'],
    data() {
        return {
            dataSourceDescriptor,
            loading: false,
            touched: false,
            operation: 'insert',
            v$: useValidate() as any,
            datasource: {} as any,
            availableDatabases: [] as any,
            jdbcOrJndi: {} as any,
            jdbcPoolConfiguration: {} as any
        }
    },
    validations() {
        return {
            datasource: createValidations('datasource', dataSourceDetailValidationDescriptor.validations.datasource)
        }
    },
    computed: {},
    mounted() {
        if (this.selectedDatasource) {
            this.datasource = { ...this.selectedDatasource } as any
            this.jdbcPoolConfiguration = this.datasource.jdbcPoolConfiguration as any
        }
        this.availableDatabases = this.databases
    },
    watch: {
        selectedDatasource() {
            this.datasource = { ...this.selectedDatasource } as any
            this.jdbcPoolConfiguration = this.datasource.jdbcPoolConfiguration as any
            this.connectionType()
        },
        databases() {
            this.availableDatabases = this.databases
        }
    },
    methods: {
        closeTemplate() {
            this.$router.push('/datasource')
            this.$emit('closed')
        },
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
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
        connectionType() {
            if (this.datasource.driver) {
                this.jdbcOrJndi.type = 'JDBC'
            }
            if (this.datasource.jndi != undefined && this.datasource.jndi != '') {
                this.jdbcOrJndi.type = 'JNDI'
            }
            console.log(this.jdbcOrJndi.type)
        },
        onAdvancedOptionsChange(event) {
            this.datasource.jdbcPoolConfiguration[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
