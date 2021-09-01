<template>
    <Dialog :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            {{ title }}
        </template>
        <div class="p-field p-m-4">
            <span class="p-float-label">
                <InputText
                    id="glossaryName"
                    class="kn-material-input"
                    v-model.trim="glossary.GLOSSARY_NM"
                    max="100"
                    :class="{
                        'p-invalid': glossary.GLOSSARY_NM?.length === 0 && glossaryNameDirty
                    }"
                    @blur="glossaryNameDirty = true"
                />
                <label for="glossaryName" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
            </span>
            <div v-if="glossary.GLOSSARY_NM?.length === 0 && glossaryNameDirty" class="p-error p-grid p-mt-2">
                {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
            </div>
        </div>

        <div class="p-field p-m-4">
            <span class="p-float-label">
                <InputText id="glossaryCode" class="kn-material-input" v-model.trim="glossary.GLOSSARY_CD" max="30" />
                <label for="glossaryCode" class="kn-material-input-label"> {{ $t('managers.glossary.common.code') }}</label>
            </span>
        </div>
        <div class="p-field p-m-4">
            <span class="p-float-label">
                <Textarea id="glossaryDescription" class="kn-material-input" v-model.trim="glossary.GLOSSARY_DS" :autoResize="true" maxLength="500" rows="1" />
                <label for="glossaryDescription" class="kn-material-input-label"> {{ $t('common.description') }}</label>
            </span>
            <div id="description-help">
                <small id="description-help">{{ descriptionHelp }}</small>
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button :label="$t('common.save')" @click="$emit('save', glossary)" class="kn-button kn-button--primary" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iGlossary } from '../GlossaryDefinition'
import Dialog from 'primevue/dialog'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'glossary-definition-glossary-dialog',
    components: { Dialog, Textarea },
    emits: ['close', 'save'],
    props: {
        visible: { type: Boolean },
        selectedGlossary: { type: Object }
    },
    watch: {
        selectedGlossary() {
            this.loadGlossary()
        }
    },
    data() {
        return {
            glossaryDefinitionDialogDescriptor,
            glossary: {} as iGlossary,
            glossaryNameDirty: false
        }
    },
    computed: {
        title(): string {
            if (!this.glossary.SaveOrUpdate) {
                return this.$t('managers.glossary.glossaryDefinition.cloneTitle')
            } else {
                return this.glossary.SaveOrUpdate === 'Save' ? this.$t('managers.glossary.glossaryDefinition.saveTitle') : this.$t('managers.glossary.glossaryDefinition.updateTitle')
            }
        },
        descriptionHelp(): string {
            return (this.glossary.GLOSSARY_DS?.length ?? '0') + ' / 500'
        },
        buttonDisabled(): boolean {
            return this.glossary.GLOSSARY_NM?.length === 0
        }
    },
    created() {
        this.loadGlossary()
    },
    methods: {
        loadGlossary() {
            this.glossaryNameDirty = false
            this.glossary = { ...this.selectedGlossary } as iGlossary
        }
    }
})
</script>

<style lang="scss" scoped>
#description-help {
    display: flex;
    justify-content: flex-end;
}
</style>
