<template>
    <Card>
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-d-flex p-jc-between">
                    <div class="p-field">
                        <span class="p-float-label">
                            <InputNumber id="noOfEvents" v-model="alert.eventBeforeTriggerAction" showButtons @input="valueChanged('eventBeforeTriggerAction', $event.value)" />
                            <label for="noOfEvents" class="kn-material-input-label">{{ $t('kpi.alert.noOfEvents') }}</label>
                        </span>
                    </div>
                    <div class="p-field-checkbox">
                        <Checkbox id="execution" v-model="alert.singleExecution" :binary="true" @click="valueChanged('eventBeforeTriggerAction', $event)" />
                        <label for="execution">{{ $t('kpi.alert.execution') }}</label>
                    </div>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iAlert } from '../Alert'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
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
            alert: {} as iAlert
        }
    },
    methods: {
        valueChanged(fieldName: string, value: any) {
            this.$emit('valueChanged', { fieldName, value })
        }
    }
})
</script>
