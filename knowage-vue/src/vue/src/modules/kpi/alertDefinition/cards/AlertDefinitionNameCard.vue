<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            v-bind="$attrs"
                            id="name"
                            v-model="alert.name"
                            class="kn-material-input"
                            type="text"
                            :class="{
                                'p-invalid': vcomp.name.$invalid && vcomp.name.$dirty
                            }"
                            @input="valueChanged('name', $event.target.value)"
                            @blur="vcomp.name.$touch()"
                        />
                        <label for="name" class="kn-material-input-label">{{ $t('kpi.alert.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="vcomp.name" :additional-translate-params="{ fieldName: $t('kpi.alert.name') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            v-bind="$attrs"
                            id="listener"
                            v-model="alert.alertListener"
                            class="kn-material-input"
                            :options="listeners"
                            option-label="name"
                            :class="{
                                'p-invalid': vcomp.alertListener.$invalid && vcomp.alertListener.$dirty
                            }"
                            @change="valueChanged('alertListener', $event.value)"
                            @blur="vcomp.alertListener.$touch()"
                        />
                        <label for="listener" class="kn-material-input-label"> {{ $t('kpi.alert.kpiListener') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="vcomp.alertListener" :additional-translate-params="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iAlert, iListener } from '../AlertDefinition'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import alertDescriptor from '../AlertDefinitionDescriptor.json'
export default defineComponent({
    name: 'name-card',
    components: { Dropdown, KnValidationMessages },
    props: {
        selectedAlert: {
            type: Object as PropType<iAlert>,
            required: false
        },
        listeners: {
            type: Array as PropType<iListener[]>,
            required: false
        },
        vcomp: Object
    },
    emits: ['touched', 'valueChanged'],
    data() {
        return {
            alert: {} as iAlert,
            alertDescriptor
        }
    },
    watch: {
        selectedAlert() {
            this.alert = { ...this.selectedAlert }
        }
    },
    methods: {
        valueChanged(fieldName: string, value: any) {
            this.$emit('valueChanged', { fieldName, value })
        }
    }
})
</script>
