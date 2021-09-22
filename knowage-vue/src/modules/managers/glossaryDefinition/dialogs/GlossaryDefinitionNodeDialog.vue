<template>
    <Dialog :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            {{ content.CONTENT_ID ? $t('managers.glossary.glossaryDefinition.editNode') : $t('managers.glossary.glossaryDefinition.newNode') }}
        </template>
        <div class="p-field p-m-4">
            <span class="p-float-label">
                <InputText
                    id="contentName"
                    class="kn-material-input"
                    v-model.trim="content.CONTENT_NM"
                    max="100"
                    :class="{
                        'p-invalid': content.CONTENT_NM?.length === 0 && contentNameDirty
                    }"
                    @blur="contentNameDirty = true"
                />
                <label for="contentName" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
            </span>
            <div v-if="content.CONTENT_NM.length === 0 && contentNameDirty" class="p-error p-grid p-mt-2">
                {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
            </div>
        </div>

        <div class="p-field p-m-4">
            <span class="p-float-label">
                <InputText id="contentCode" class="kn-material-input" v-model.trim="content.CONTENT_CD" max="30" />
                <label for="contentCode" class="kn-material-input-label"> {{ $t('managers.glossary.common.code') }}</label>
            </span>
        </div>
        <div class="p-field p-m-4">
            <span class="p-float-label">
                <Textarea id="contentDescription" class="kn-material-input" v-model.trim="content.CONTENT_DS" :autoResize="true" maxLength="500" rows="1" />
                <label for="contentDescription" class="kn-material-input-label"> {{ $t('common.description') }}</label>
            </span>
            <div id="description-help">
                <small id="description-help">{{ descriptionHelp }}</small>
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button :label="$t('common.save')" @click="$emit('save', content)" class="kn-button kn-button--primary" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iContent } from '../GlossaryDefinition'
import Dialog from 'primevue/dialog'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'glossary-definition-node-dialog',
    components: { Dialog, Textarea },
    emits: ['close', 'save'],
    props: {
        visible: { type: Boolean },
        selectedContent: { type: Object }
    },
    watch: {
        selectedContent() {
            this.loadContent()
        }
    },
    data() {
        return {
            glossaryDefinitionDialogDescriptor,
            content: {} as iContent,
            contentNameDirty: false
        }
    },
    computed: {
        descriptionHelp(): string {
            return (this.content.CONTENT_DS?.length ?? '0') + ' / 500'
        },
        buttonDisabled(): boolean {
            return this.content.CONTENT_NM?.length === 0
        }
    },
    created() {
        this.loadContent()
    },
    methods: {
        loadContent() {
            this.contentNameDirty = false
            this.content = { ...this.selectedContent } as iContent
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
