<template>
    <Card :style="alertDescriptor.styles.basicCard" class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputNumber id="noOfEvents" inputClass="kn-material-input" v-model="alert.eventBeforeTriggerAction" @input="valueChanged('eventBeforeTriggerAction', $event.value)" />
                        <label for="noOfEvents" class="kn-material-input-label">{{ $t('kpi.alert.noOfEvents') }}</label>
                    </span>
                </div>
                <div class="p-field-checkbox">
                    <Checkbox id="execution" v-model="alert.singleExecution" :binary="true" @click="valueChanged('singleExecution', $event)" />
                    <label for="execution">{{ $t('kpi.alert.execution') }}</label>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iAlert } from '../AlertDefinition'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
import alertDescriptor from '../AlertDefinitionDescriptor.json'
export default defineComponent({
    name: 'events-card',
    components: { InputNumber, Checkbox },
    props: {
        selectedAlert: {
            type: Object as PropType<iAlert>,
            required: false
        },
        vcomp: Object
    },
    emits: ['touched', 'valueChanged'],
    watch: {
        selectedAlert() {
            this.alert = { ...this.selectedAlert }
        }
    },
    data() {
        return {
            alert: {} as iAlert,
            alertDescriptor
        }
    },
    methods: {
        valueChanged(fieldName: string, value: any) {
            this.$emit('valueChanged', { fieldName, value })
        }
    }
})
</script>
