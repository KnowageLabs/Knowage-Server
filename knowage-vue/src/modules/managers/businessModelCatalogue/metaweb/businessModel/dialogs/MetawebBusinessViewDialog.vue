<template>
    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="showBusinessViewDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.newView') }}
                </template>
            </Toolbar>
        </template>

        <StepOne v-if="wizardStep === 1" :physicalModels="physicalModels" :showBusinessViewDialog="showBusinessViewDialog" :bnssViewObject="tmpBnssView" />
        <StepTwo v-if="wizardStep === 2" :physicalModels="physicalModels" :bnssViewObject="tmpBnssView" :summaryList="summary" />

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="onCancel" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--secondary" :label="$t('common.back')" :disabled="buttonDisabled" @click="previousStep" />
            <Button v-if="wizardStep == 1" class="kn-button kn-button--primary" :label="$t('common.next')" :disabled="buttonDisabled" @click="nextStep" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import bsDescriptor from '../MetawebBusinessModelDescriptor.json'
import StepOne from './businessViewWizard/MetawebBusinessViewWizardStepOne.vue'
import StepTwo from './businessViewWizard/MetawebBusinessViewWizardStepTwo.vue'

export default defineComponent({
    components: { Dialog, StepOne, StepTwo },
    emits: ['closeDialog'],
    props: { physicalModels: Array, showBusinessViewDialog: Boolean },
    computed: {
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.tmpBnssView.physicalModels.length < 2) {
                return true
            } else return false
        }
    },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBnssView: { physicalModels: [], name: '', description: '' } as any,
            wizardStep: 1,
            summary: [] as any
        }
    },
    methods: {
        resetPhModel() {
            this.tmpBnssView.physicalModels = []
        },
        onCancel() {
            this.$emit('closeDialog')
            this.tmpBnssView = { physicalModels: [], name: '', description: '' } as any
        },
        nextStep() {
            this.wizardStep++
        },
        previousStep() {
            this.wizardStep--
        }
    }
})
</script>
<style lang="scss">
.data-condition-list {
    border: 1px solid $color-borders !important;
    border-top: none;
}
</style>
