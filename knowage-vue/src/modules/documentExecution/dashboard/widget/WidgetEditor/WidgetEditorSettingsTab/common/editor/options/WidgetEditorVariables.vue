<template>
    <div class="p-fluid p-formgrid p-grid p-m-4">
        <div class="p-field kn-flex">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="selectedVariable" :options="variables" optionLabel="name" @change="onVariableChange"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.variable') }}</label>
            </span>
        </div>
        <div v-if="selectedVariable && selectedVariable.pivotedValues" class="p-field kn-flex p-ml-3">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="variableKey" :options="selectedVariable.pivotedValues ? Object.keys(selectedVariable.pivotedValues) : []" @change="onVariableKeyChange"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.key') }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-active-selections',
    components: { Dropdown },
    props: { variables: { type: Array as PropType<IVariable[]>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedVariable: null as IVariable | null,
            variableKey: ''
        }
    },
    created() {},
    methods: {
        onVariableChange() {
            this.variableKey = ''
            if (!this.selectedVariable) return
            if (this.selectedVariable.type !== 'dataset' || this.selectedVariable.column) {
                const forInsert = `[kn-variable='${this.selectedVariable.name}']`
                this.$emit('insertChanged', forInsert)
            }
        },
        onVariableKeyChange() {
            if (!this.selectedVariable || !this.variableKey) return
            const forInsert = `[kn-variable='${this.selectedVariable.name}' key='${this.variableKey}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
