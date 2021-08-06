<template>
    <Card>
        <template #content>
            <form class="p-fluid p-m-5" v-if="target">
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            v-bind="$attrs"
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            v-model="target.name"
                            @input="valueChanged('name', $event.target.value)"
                            :class="{
                                'p-invalid': vcomp.name.$invalid && vcomp.name.$dirty
                            }"
                            @blur="vcomp.name.$touch()"
                        />
                        <label for="name" class="kn-material-input-label">{{ $t('kpi.targetDefinition.name') }} * </label>
                    </span>
                    <KnValidationMessages :vComp="vcomp.name" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.name') }"></KnValidationMessages>
                </div>
                <div class="kn-flex">
                    <div class="p-d-flex p-jc-between">
                        <div>
                            <span class="p-float-label">
                                <Calendar
                                    id="startDate"
                                    class="kn-material-input"
                                    v-model="target.startValidity"
                                    @date-select="valueChanged('startValidity', $event)"
                                    :class="{
                                        'p-invalid': vcomp.startValidity.$invalid && vcomp.startValidity.$dirty
                                    }"
                                    :showIcon="true"
                                    :manualInput="false"
                                    @blur="vcomp.startValidity.$touch()"
                                />
                                <label for="startDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} * </label>
                            </span>
                            <KnValidationMessages :vComp="vcomp.startValidity" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.startDate') }"></KnValidationMessages>
                        </div>
                        <div class="p-d-flex">
                            <div>
                                <span class="p-float-label">
                                    <Calendar
                                        id="endDate"
                                        class="kn-material-input"
                                        v-model="target.endValidity"
                                        @date-select="valueChanged('endValidity', $event)"
                                        :class="{
                                            'p-invalid': vcomp.endValidity.$invalid && vcomp.endValidity.$dirty
                                        }"
                                        :showIcon="true"
                                        :manualInput="false"
                                        @blur="vcomp.endValidity.$touch()"
                                    />
                                    <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} * </label>
                                </span>
                                <KnValidationMessages :vComp="vcomp.endValidity" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.endDate') }" :specificTranslateKeys="{ is_after_date: 'kpi.targetDefinition.endDateBeforeStart' }"></KnValidationMessages>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Calendar from 'primevue/calendar'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import { iTargetDefinition } from './TargetDefinition'

export default defineComponent({
    name: 'target-definition-form',
    components: {
        Calendar,
        KnValidationMessages
    },
    props: {
        selectedTarget: {
            type: Object as PropType<iTargetDefinition>
        },
        vcomp: Object
    },
    emits: ['touched', 'valueChanged'],
    watch: {
        selectedTarget() {
            this.target = { ...this.selectedTarget }
        }
    },
    data() {
        return {
            target: {} as iTargetDefinition
        }
    },
    methods: {
        valueChanged(fieldName: string, value: any) {
            this.$emit('valueChanged', { fieldName, value })
        }
    }
})
</script>
