<template>
    <div v-if="inputVariable" class="p-d-flex p-flex-row p-ai-center" data-test="input-variables-container">
        <div class="variable-name-input kn-flex ">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model.trim="inputVariable.name" :disabled="readonly" data-test="variable-name-input" />
                <label class="kn-material-input-label"> {{ $t('managers.functionsCatalog.variableName') }} </label>
            </span>
        </div>
        <div class="p-field kn-flex p-m-2">
            <span>
                <label class="kn-material-input-label">{{ $t('managers.functionsCatalog.variableType') }}</label>
                <Dropdown class="kn-material-input" v-model="inputVariable.type" :options="functionsCatalogInputTabDescriptor.variableTypes" optionLabel="value" optionValue="value" :disabled="readonly" @change="onTypeChange" />
            </span>
        </div>
        <div class="p-field kn-flex">
            <label class="kn-material-input-label">{{ $t('managers.functionsCatalog.variableDefaultValue') }}</label>
            <InputText v-if="inputVariable.type !== 'DATE'" :type="inputVariable.type === 'NUMBER' ? 'number' : 'text'" class="kn-material-input" v-model.trim="inputVariable.value" :disabled="readonly" data-test="variable-defult-value-input" />
            <Calendar v-else v-model="inputVariable.value" :showButtonBar="true" :disabled="readonly"></Calendar>
        </div>
        <div class="p-field p-mt-5">
            <Button v-if="!readonly" icon="pi pi-trash" class="p-button-link" @click="deleteVariableConfirm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputVariable } from '../../FunctionsCatalog'
import { formatDate } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import functionsCatalogInputTabDescriptor from './FunctionsCatalogInputTabDescriptor.json'

export default defineComponent({
    name: 'function-catalog-input-column',
    components: { Calendar, Dropdown },
    props: { variable: { type: Object }, readonly: { type: Boolean } },
    emits: ['deleted'],
    data() {
        return {
            functionsCatalogInputTabDescriptor,
            inputVariable: {} as iInputVariable
        }
    },
    watch: {
        variable() {
            this.loadVariable()
        }
    },
    created() {
        this.loadVariable()
    },
    methods: {
        loadVariable() {
            this.inputVariable = this.variable as iInputVariable
            if (this.inputVariable.type === 'DATE') {
                this.inputVariable.value = this.getFormatedDate(this.inputVariable.value)
            }
        },
        onTypeChange() {
            this.inputVariable.value = ''
        },
        deleteVariableConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleted')
            })
        },
        getFormatedDate(date: any) {
            return formatDate(date, 'MM/DD/YYYY')
        }
    }
})
</script>

<style lang="scss" scoped>
.variable-name-input {
    margin-top: 1.2rem;
}
</style>
