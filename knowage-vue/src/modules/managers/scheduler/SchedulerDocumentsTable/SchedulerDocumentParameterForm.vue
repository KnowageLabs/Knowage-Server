<template>
    <div class="parameter-card">
        <div v-if="parameter && !loading">
            <h2>{{ parameter.name }}</h2>
            <div class="p-grid p-ai-center">
                <div class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.parameterValueType') }}</label>
                        <Dropdown v-model="parameter.type" class="kn-material-input" :options="parameter.temporal ? triggerStrategies : triggerStrategies.slice(0, 2)" option-label="label" option-value="value" @change="onParameterTypeChange" />
                    </span>
                </div>
                <div v-if="parameter.type === 'fixed'" class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span v-if="parameterValues.manualInput">
                        <label class="kn-material-input-label">{{ $t('common.values') }}</label>
                        <InputText v-model="parameter.value" class="kn-material-input" />
                    </span>
                    <span v-else>
                        <label class="kn-material-input-label">{{ $t('common.values') }}</label>
                        <MultiSelect v-model="parameter.selectedValues" class="kn-material-input" :options="parameterValues.values" @change="formatSelectedValues" />
                    </span>
                </div>
                <div v-else-if="parameter.type === 'loadAtRuntime'" class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.role') }}</label>
                        <Dropdown v-model="parameter.value" class="kn-material-input" :options="rolesOptions" option-label="role" option-value="userAndRole" />
                    </span>
                </div>
                <div v-else-if="parameter.type === 'formula'" class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.selectFormula') }}</label>
                        <Dropdown v-model="parameter.value" class="kn-material-input" :options="formulaOptions" option-label="description" option-value="name" />
                    </span>
                </div>
                <div class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.iterations') }}</label>
                        <Dropdown v-model="parameter.iterative" class="kn-material-input" :options="triggerIterations" option-label="label" option-value="value" />
                    </span>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'scheduler-document-parameter-form',
    components: { Dropdown, MultiSelect },
    props: { propParameter: { type: Object }, roles: { type: Array }, formulas: { type: Array }, documentLabel: { type: String } },
    emits: ['loading'],
    data() {
        return {
            parameter: null as any,
            triggerStrategies: [
                { value: 'fixed', label: this.$t('managers.scheduler.fixedValuesStrategy') },
                { value: 'loadAtRuntime', label: this.$t('managers.scheduler.loadAtRuntimeStrategy') },
                { value: 'formula', label: this.$t('managers.scheduler.useFormulaStrategy') }
            ],
            triggerIterations: [
                { value: true, label: this.$t('managers.scheduler.iterateOnParameterValues') },
                { value: false, label: this.$t('managers.scheduler.doNotIterateOnParameterValues') }
            ],
            parameterValues: {} as any,
            rolesOptions: [] as any[],
            formulaOptions: [],
            loading: false
        }
    },
    watch: {
        async propParameter() {
            this.loadParameter()
            await this.formatParameter()
        },
        roles() {
            this.loadRoles()
        },
        formulas() {
            this.loadFormulas()
        }
    },
    async created() {
        this.loadRoles()
        this.loadParameter()
        await this.formatParameter()
        this.loadFormulas()
    },
    methods: {
        loadParameter() {
            this.parameter = this.propParameter as any
        },
        async formatParameter() {
            if (this.parameter.type === 'fixed') {
                await this.loadParameterValues()
                if (!this.parameterValues?.manualInput && this.parameter.value) {
                    this.parameter.manualInput = false
                    this.parameter.selectedValues = this.parameter.value.split(';').map((el: any) => el.trim())
                }
            } else if (this.parameter.type === 'loadAtRuntime' && this.parameter.value) {
                for (let i = 0; i < this.rolesOptions.length; i++) {
                    if (this.parameter.value.endsWith(this.rolesOptions[i].role)) {
                        this.parameter.value = this.rolesOptions[i].userAndRole
                        break
                    }
                }
            }
        },
        async loadParameterValues() {
            this.loading = true
            this.$emit('loading', true)
            await axios
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documents/${this.documentLabel}/parameters/${this.parameter?.id}/values?role=${this.parameter?.role}`)
                .then((response) => (this.parameterValues = response.data))
                .catch(() => {})
            this.$emit('loading', false)
            this.loading = false
        },
        loadRoles() {
            this.rolesOptions = this.roles as any
        },
        loadFormulas() {
            this.formulaOptions = this.formulas as any
        },
        formatSelectedValues() {
            this.parameter.value = ''
            for (let i = 0; i < this.parameter.selectedValues.length; i++) {
                this.parameter.value += this.parameter.selectedValues[i]
                this.parameter.value += i === this.parameter.selectedValues.length - 1 ? ' ' : '; '
            }
        },
        async onParameterTypeChange() {
            this.parameter.value = ''
            this.parameter.selectedValues = []
            if (this.parameter.type === 'fixed') {
                await this.loadParameterValues()
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.parameter-card {
    padding: 0;
}
</style>
