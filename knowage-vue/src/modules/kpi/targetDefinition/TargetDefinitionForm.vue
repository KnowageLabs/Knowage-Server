<template>
    <Card>
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            v-bind="$attrs"
                            id="name"
                            class="kn-material-input"
                            type="text"
                            maxLength="100"
                            :value="modelValue.name"
                            @input="$emit('update:modelValue', $event.target.value)"
                            :class="{
                                'p-invalid': vcomp.name.$invalid && vcomp.name.$dirty
                            }"
                            @change="setDirty"
                            @blur="vcomp.name.$touch()"
                        />
                        <label for="name" class="kn-material-input-label">Name * </label>
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
                                    :value="modelValue.startValidity"
                                    @input="$emit('update:modelValue', $event.target.value)"
                                    :class="{
                                        'p-invalid': vcomp.startValidity.$invalid && vcomp.startValidity.$dirty
                                    }"
                                    :showIcon="true"
                                    :manualInput="false"
                                    @date-select="setDirty"
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
                                        :value="modelValue.endValidity"
                                        @input="$emit('update:modelValue', $event.target.value)"
                                        :class="{
                                            'p-invalid': vcomp.endValidity.$invalid && vcomp.endValidity.$dirty
                                        }"
                                        :showIcon="true"
                                        :manualInput="false"
                                        @date-select="setDirty"
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
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
//import { iTargetDefinition } from './TargetDefinition'

export default defineComponent({
    name: 'target-definition-form',
    components: {
        Calendar,
        KnValidationMessages
    },
    props: {
        modelValue: {
            type: Object
        },
        vcomp: Object
    },
    data() {},
    methods: {
        setDirty(): void {
            this.$emit('touched')
        }
    }
})
</script>
