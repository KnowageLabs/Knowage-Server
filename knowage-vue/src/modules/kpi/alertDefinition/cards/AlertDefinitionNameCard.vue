<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            v-bind="$attrs"
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model="alert.name"
                            @input="valueChanged('name', $event.target.value)"
                            :class="{
                                'p-invalid': vcomp.name.$invalid && vcomp.name.$dirty
                            }"
                            @blur="vcomp.name.$touch()"
                        />
                        <label for="name" class="kn-material-input-label">{{ $t('kpi.alert.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="vcomp.name" :additionalTranslateParams="{ fieldName: $t('kpi.alert.name') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            v-bind="$attrs"
                            id="listener"
                            class="kn-material-input"
                            v-model="alert.alertListener"
                            :options="listeners"
                            optionLabel="name"
                            @change="valueChanged('alertListener', $event.value)"
                            :class="{
                                'p-invalid': vcomp.alertListener.$invalid && vcomp.alertListener.$dirty
                            }"
                            @blur="vcomp.alertListener.$touch()"
                        />
                        <label for="listener" class="kn-material-input-label"> {{ $t('kpi.alert.kpiListener') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages>
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
