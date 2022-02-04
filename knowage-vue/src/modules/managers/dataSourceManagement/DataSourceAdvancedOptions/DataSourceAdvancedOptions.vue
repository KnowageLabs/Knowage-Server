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
                                class="kn-material-input"
                                type="number"
                                maxLength="20"
                                v-model.trim="v$.jdbcPoolConfiguration.maxTotal.$model"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.maxTotal.$invalid && v$.jdbcPoolConfiguration.maxTotal.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.maxTotal.$touch()"
                                @input="onFieldChange('maxTotal', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxTotalInfo')"
                                :disabled="readOnly"
                            />
                            <label for="maxTotal" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxTotal') }} * </label>
                        </span>
                        <KnValidationMessages :vComp="v$.jdbcPoolConfiguration.maxTotal" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.maxTotal') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="maxWait"
                                class="kn-material-input"
                                type="number"
                                maxLength="30"
                                v-model.trim="v$.jdbcPoolConfiguration.maxWait.$model"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.maxWait.$invalid && v$.jdbcPoolConfiguration.maxWait.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.maxWait.$touch()"
                                @input="onFieldChange('maxWait', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxWaitInfo')"
                                :disabled="readOnly"
                            />
                            <label for="maxWait" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxWait') }} * </label>
                        </span>
                        <KnValidationMessages :vComp="v$.jdbcPoolConfiguration.maxWait" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.maxWait') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="maxIdle"
                                class="kn-material-input"
                                type="number"
                                maxLength="30"
                                v-model.trim="jdbcPoolConfiguration.maxIdle"
                                @input="onFieldChange('maxIdle', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.maxIdleInfo')"
                                :disabled="readOnly"
                            />
                            <label for="maxWait" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.maxIdle') }} * </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="abandonedTimeout"
                                class="kn-material-input"
                                type="number"
                                maxLength="20"
                                v-model.trim="v$.jdbcPoolConfiguration.abandonedTimeout.$model"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.abandonedTimeout.$invalid && v$.jdbcPoolConfiguration.abandonedTimeout.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.abandonedTimeout.$touch()"
                                @input="onFieldChange('abandonedTimeout', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeoutInfo')"
                                :disabled="readOnly"
                            />
                            <label for="abandonedTimeout" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeout') }} * </label>
                        </span>
                        <KnValidationMessages :vComp="v$.jdbcPoolConfiguration.abandonedTimeout" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.abandonedTimeout') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="timeBetweenEvictionRuns"
                                class="kn-material-input"
                                type="number"
                                maxLength="30"
                                v-model.trim="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$model"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$invalid && v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns.$touch()"
                                @input="onFieldChange('timeBetweenEvictionRuns', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRunsInfo')"
                                :disabled="readOnly"
                            />
                            <label for="timeBetweenEvictionRuns" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRuns') }} * </label>
                        </span>
                        <KnValidationMessages :vComp="v$.jdbcPoolConfiguration.timeBetweenEvictionRuns" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.timeBetweenEvictionRuns') }" />
                    </div>
                    <div class="p-field p-col-12 p-md-6 p-lg-4 p-xl-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="minEvictableIdleTimeMillis"
                                class="kn-material-input"
                                :disabled="readOnly"
                                type="number"
                                maxLength="30"
                                v-model.trim="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$model"
                                :class="{
                                    'p-invalid': v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$invalid && v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$dirty
                                }"
                                @blur="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis.$touch()"
                                @input="onFieldChange('minEvictableIdleTimeMillis', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillisInfo')"
                            />
                            <label for="minEvictableIdleTimeMillis" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillis') }} </label>
                        </span>
                        <KnValidationMessages :vComp="v$.jdbcPoolConfiguration.minEvictableIdleTimeMillis" :additionalTranslateParams="{ fieldName: $t('managers.dataSourceManagement.form.advancedOptions.minEvictableIdleTimeMillis') }" />
                    </div>
                </div>
                <div class="p-formgrid p-grid">
                    <div class="p-field p-col-9" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText id="validationQuery" class="kn-material-input" :disabled="readOnly" type="text" v-model.trim="jdbcPoolConfiguration.validationQuery" maxLength="200" @input="onFieldChange('validationQuery', $event.target.value)" />
                            <label for="validationQuery" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.validationQuery') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-3" :style="dataSourceDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="validationQueryTimeout"
                                class="kn-material-input"
                                :disabled="readOnly"
                                type="number"
                                maxLength="30"
                                v-model.trim="jdbcPoolConfiguration.validationQueryTimeout"
                                @input="onFieldChange('validationQueryTimeout', $event.target.value)"
                                v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.validationQueryTimeoutInfo')"
                            />
                            <label for="validationQueryTimeout" class="kn-material-input-label"> {{ $t('managers.dataSourceManagement.form.advancedOptions.validationQueryTimeout') }} </label>
                        </span>
                    </div>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="removeAbandonedOnBorrow" v-model="jdbcPoolConfiguration.removeAbandonedOnBorrow" :disabled="readOnly" :binary="true" @change="onFieldChange('removeAbandonedOnBorrow', jdbcPoolConfiguration.removeAbandonedOnBorrow)" />
                    <label for="removeAbandonedOnBorrow" v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnBorrowInfo')">{{ $t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnBorrow') }} </label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="removeAbandonedOnMaintenance" v-model="jdbcPoolConfiguration.removeAbandonedOnMaintenance" :disabled="readOnly" :binary="true" @change="onFieldChange('removeAbandonedOnMaintenance', jdbcPoolConfiguration.removeAbandonedOnMaintenance)" />
                    <label for="removeAbandonedOnMaintenance" v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnMaintenanceInfo')">{{ $t('managers.dataSourceManagement.form.advancedOptions.removeAbandonedOnMaintenance') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="logAbandoned" v-model="jdbcPoolConfiguration.logAbandoned" :disabled="readOnly" :binary="true" @change="onFieldChange('logAbandoned', jdbcPoolConfiguration.logAbandoned)" />
                    <label for="logAbandoned" v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.logAbandonedInfo')">{{ $t('managers.dataSourceManagement.form.advancedOptions.logAbandoned') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="testOnReturn" v-model="jdbcPoolConfiguration.testOnReturn" :disabled="readOnly" :binary="true" @change="onFieldChange('testOnReturn', jdbcPoolConfiguration.testOnReturn)" />
                    <label for="testOnReturn" v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.testOnReturnInfo')">{{ $t('managers.dataSourceManagement.form.advancedOptions.testOnReturn') }}</label>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="testWhileIdle" v-model="jdbcPoolConfiguration.testWhileIdle" :disabled="readOnly" :binary="true" @change="onFieldChange('testWhileIdle', jdbcPoolConfiguration.testWhileIdle)" />
                    <label for="testWhileIdle" v-tooltip.top="$t('managers.dataSourceManagement.form.advancedOptions.testWhileIdleInfo')">{{ $t('managers.dataSourceManagement.form.advancedOptions.testWhileIdle') }} </label>
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
    props: {
        advancedOptions: {
            type: Object,
            requried: false
        },
        isReadOnly: Boolean
    },
    directives: {
        tooltip: Tooltip
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
    async created() {
        if (this.advancedOptions) {
            this.jdbcPoolConfiguration = { ...this.advancedOptions } as any
        }
        this.readOnly = this.isReadOnly
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
    methods: {
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        }
    }
})
</script>
