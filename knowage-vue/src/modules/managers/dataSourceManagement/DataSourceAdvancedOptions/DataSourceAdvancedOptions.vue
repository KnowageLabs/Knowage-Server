<template>
    <Card :style="dataSourceDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start> {{ $t('managers.dataSourceManagement.form.advancedOptions.title') }} </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-m-3">
                <div class="p-formgrid p-grid">
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="maxTotal"
                                v-model.trim="v$.jdbcPoolConfiguration.maxTotal.$model"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxTotalInfo')"
                                class="kn-material-input"
                                type="number"
                                max-length="20"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.maxTotal.$invalid && v$.jdbcPoolConfiguration.maxTotal.$dirty
                                }"
                                :disabled="readOnly"
                                @blur="v$.jdbcPoolConfiguration.maxTotal.$touch()"
                                @input="onFieldChange('maxTotal', $event.target.value)"
                            />
                            <label for="maxTotal" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxTotal') }} * </label>
                        </span>
                        <KnValidationMessages :v-comp="v$.jdbcPoolConfiguration.maxTotal" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.maxTotal') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="maxWait"
                                v-model.trim="v$.jdbcPoolConfiguration.maxWait.$model"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxWaitInfo')"
                                class="kn-material-input"
                                type="number"
                                max-length="30"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.maxWait.$invalid && v$.jdbcPoolConfiguration.maxWait.$dirty
                                }"
                                :disabled="readOnly"
                                @blur="v$.jdbcPoolConfiguration.maxWait.$touch()"
                                @input="onFieldChange('maxWait', $event.target.value)"
                            />
                            <label for="maxWait" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxWait') }} * </label>
                        </span>
                        <KnValidationMessages :v-comp="v$.jdbcPoolConfiguration.maxWait" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.maxWait') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="maxIdle"
                                v-model.trim="jdbcPoolConfiguration.maxIdle"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxIdleInfo')"
                                class="kn-material-input"
                                type="number"
                                max-length="30"
                                :disabled="readOnly"
                                @input="onFieldChange('maxIdle', $event.target.value)"
                            />
                            <label for="maxWait" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxIdle') }} * </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="abandonedTimeout"
                                v-model.trim="v$.jdbcPoolConfiguration.abandonedTimeout.$model"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeoutInfo')"
                                class="kn-material-input"
                                type="number"
                                max-length="20"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.abandonedTimeout.$invalid && v$.jdbcPoolConfiguration.abandonedTimeout.$dirty
                                }"
                                :disabled="readOnly"
                                @blur="v$.jdbcPoolConfiguration.abandonedTimeout.$touch()"
                                @input="onFieldChange('abandonedTimeout', $event.target.value)"
                            />
                            <label for="abandonedTimeout" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeout') }} * </label>
                        </span>
                        <KnValidationMessages :v-comp="v$.jdbcPoolConfiguration.abandonedTimeout" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeout') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="timeBetweenEvictionRuns"
                                v-model.trim="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$model"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRunsInfo')"
                                class="kn-material-input"
                                type="number"
                                max-length="30"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$invalid && v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$dirty
                                }"
                                :disabled="readOnly"
                                @blur="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$touch()"
                                @input="onFieldChange('timeBetweenEvictionRuns', $event.target.value)"
                            />
                            <label for="timeBetweenEvictionRuns" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRuns') }} * </label>
                        </span>
                        <KnValidationMessages :v-comp="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRuns') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="minEvictableIdleTimeMillis"
                                v-model.trim="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$model"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillisInfo')"
                                class="kn-material-input"
                                :disabled="readOnly"
                                type="number"
                                max-length="30"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$invalid && v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$touch()"
                                @input="onFieldChange('minEvictableIdleTimeMillis', $event.target.value)"
                            />
                            <label for="minEvictableIdleTimeMillis" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillis') }} </label>
                        </span>
                        <KnValidationMessages :v-comp="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis" :additional-translate-params="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillis') }" />
                    </div>
                </div>
                <div class="p-formgrid p-grid">
                    <div class="p-field p-col-9" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText id="validationQuery" v-model.trim="jdbcPoolConfiguration.validationQuery" class="kn-material-input" :disabled="readOnly" type="text" max-length="200" @input="onFieldChange('validationQuery', $event.target.value)" />
                            <label for="validationQuery" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.validationQuery') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="validationQueryTimeout"
                                v-model.trim="jdbcPoolConfiguration.validationQueryTimeout"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.validationQueryTimeoutInfo')"
                                class="kn-material-input"
                                :disabled="readOnly"
                                type="number"
                                max-length="30"
                                @input="onFieldChange('validationQueryTimeout', $event.target.value)"
                            />
                            <label for="validationQueryTimeout" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.validationQueryTimeout') }} </label>
                        </span>
                    </div>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="removeAbandonedOnBorrow" v-model="jdbcPoolConfiguration.removeAbandonedOnBorrow" :disabled="readOnly" :binary="true" @change="onFieldChange('removeAbandonedOnBorrow', jdbcPoolConfiguration.removeAbandonedOnBorrow)" />
                    <label v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnBorrowInfo')" for="removeAbandonedOnBorrow">{{ $t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnBorrow') }} </label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="removeAbandonedOnMaintenance" v-model="jdbcPoolConfiguration.removeAbandonedOnMaintenance" :disabled="readOnly" :binary="true" @change="onFieldChange('removeAbandonedOnMaintenance', jdbcPoolConfiguration.removeAbandonedOnMaintenance)" />
                    <label v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnMaintenanceInfo')" for="removeAbandonedOnMaintenance">{{ $t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnMaintenance') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="logAbandoned" v-model="jdbcPoolConfiguration.logAbandoned" :disabled="readOnly" :binary="true" @change="onFieldChange('logAbandoned', jdbcPoolConfiguration.logAbandoned)" />
                    <label v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.logAbandonedInfo')" for="logAbandoned">{{ $t('managers.dataSourceManagement.form.advancedOptions.logAbandoned') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="testOnReturn" v-model="jdbcPoolConfiguration.testOnReturn" :disabled="readOnly" :binary="true" @change="onFieldChange('testOnReturn', jdbcPoolConfiguration.testOnReturn)" />
                    <label v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.testOnReturnInfo')" for="testOnReturn">{{ $t('managers.dataSourceManagement.form.advancedOptions.testOnReturn') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="testWhileIdle" v-model="jdbcPoolConfiguration.testWhileIdle" :disabled="readOnly" :binary="true" @change="onFieldChange('testWhileIdle', jdbcPoolConfiguration.testWhileIdle)" />
                    <label v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.testWhileIdleInfo')" for="testWhileIdle">{{ $t('managers.dataSourceManagement.form.advancedOptions.testWhileIdle') }} </label>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { createValidations } from '@/helpers/commons/validationHelper'
    import useValidate from '@vuelidate/core'
    import dataSourceDetailValidationDescriptor from '../DataSourceTabView/DataSourceDetailValidationDescriptor.json'
    import dataSourceDescriptor from '../../dataSourceManagement/DataSourceDescriptor.json'
    import Card from 'primevue/card'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
    import Tooltip from 'primevue/tooltip'
    import Checkbox from 'primevue/checkbox'

    export default defineComponent({
        name: 'detail-tab',
        components: {
            Card,
            KnValidationMessages,
            Checkbox
        },
        directives: {
            tooltip: Tooltip
        },
        props: {
            advancedOptions: {
                type: Object,
                requried: false
            },
            isReadOnly: Boolean
        },
        emits: ['fieldChanged'],
        data() {
            return {
                v$: useValidate() as any,
                dataSourceDetailValidationDescriptor,
                dataSourceDescriptor,
                jdbcPoolConfiguration: {} as any,
                readOnly: false
            }
        },
        validations() {
            return {
                jdbcPoolConfiguration: createValidations('jdbcPoolConfiguration', dataSourceDetailValidationDescriptor.validations.jdbcPoolConfiguration)
            }
        },
        watch: {
            advancedOptions() {
                this.v$.$reset()
                this.jdbcPoolConfiguration = { ...this.advancedOptions } as any
            },
            isReadOnly() {
                this.readOnly = this.isReadOnly
            }
        },
        async created() {
            if (this.advancedOptions) {
                this.jdbcPoolConfiguration = { ...this.advancedOptions } as any
            }
            this.readOnly = this.isReadOnly
        },
        methods: {
            onFieldChange(fieldName: string, value: any) {
                this.$emit('fieldChanged', { fieldName, value })
            }
        }
    })
</script>
