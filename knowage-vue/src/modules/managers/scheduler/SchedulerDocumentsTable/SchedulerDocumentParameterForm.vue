<template>
    <div class="parameter-card">
        <div v-if="parameter && !loading">
            <h2>{{ parameter.name }}</h2>
            <div class="p-grid p-ai-center">
                <div class="p-m-0  p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.parameterValueType') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.type" :options="parameter.temporal ? triggerStrategies : triggerStrategies.slice(0, 2)" optionLabel="label" optionValue="value" @change="onParameterTypeChange" />
                    </span>
                </div>
                <div v-if="parameter.type === 'fixed'" class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span v-if="parameterValues.manualInput">
                        <label class="kn-material-input-label">{{ $t('common.values') }}</label>
                        <InputText class="kn-material-input" v-model="parameter.value" />
                    </span>
                    <span v-else>
                        <label class="kn-material-input-label">{{ $t('common.values') }}</label>
                        <MultiSelect class="kn-material-input" v-model="parameter.selectedValues" :options="parameterValues.values" @change="formatSelectedValues" />
                    </span>
                </div>
                <div class="p-m-0 p-col-12 p-md-12 p-lg-4 p-xl-4" v-else-if="parameter.type === 'loadAtRuntime'">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.role') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.value" :options="rolesOptions" optionLabel="role" optionValue="userAndRole" />
                    </span>
                </div>
                <div class="p-m-0  p-col-12 p-md-12 p-lg-4 p-xl-4" v-else-if="parameter.type === 'formula'">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.selectFormula') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.value" :options="formulaOptions" optionLabel="description" optionValue="name" />
                    </span>
                </div>
                <div class="p-m-0  p-col-12 p-md-12 p-lg-4 p-xl-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.iterations') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.iterative" :options="triggerIterations" optionLabel="label" optionValue="value" />
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
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.documentLabel}/parameters/${this.parameter?.id}/values?role=${this.parameter?.role}`)
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
