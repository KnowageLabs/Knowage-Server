<template>
    <div>
        <Dialog :header="$t('managers.glossary.glossaryDefinition.details')" :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <div v-if="contentInfo && contentInfo.CONTENT_ID">
                <ul>
                    <li>
                        <span>{{ $t('common.name') }}:</span>
                        <p>{{ contentInfo.CONTENT_NM }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.code') }}:</span>
                        <p>{{ contentInfo.CONTENT_CD }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.description') }}:</span>
                        <p>{{ contentInfo.CONTENT_DS }}</p>
                    </li>
                </ul>
            </div>
            <div v-else-if="contentInfo.WORD_ID">
                <ul>
                    <li>
                        <span>{{ $tc('managers.glossary.common.word', 1) }}:</span>
                        <p>{{ contentInfo.WORD }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.status') }}:</span>
                        <p v-if="contentInfo.STATE_NM">{{ $t(glossaryDefinitionDescriptor.translation[contentInfo.STATE_NM]) }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.category') }}:</span>
                        <p v-if="contentInfo.CATEGORY_NM">{{ $t(glossaryDefinitionDescriptor.translation[contentInfo.CATEGORY_NM]) }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.description') }}:</span>
                        <p>{{ contentInfo.DESCR }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.formula') }}:</span>
                        <p>{{ contentInfo.FORMULA }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.link') }}:</span>
                        <ul>
                            <li v-for="(link, index) in contentInfo.LINK" :key="index">
                                <span>
                                    <p>{{ link.WORD }}</p>
                                    <a v-if="index != contentInfo.LINK.length - 1">-</a>
                                </span>
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.attributes') }}:</span>
                        <ul>
                            <li v-for="(attribute, index) in contentInfo.SBI_GL_WORD_ATTR" :key="index">
                                <p>{{ attribute.ATTRIBUTE_NM }}:</p>
                                <p></p>
                                <ul>
                                    <li>{{ attribute.VALUE }}</li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <template #footer>
                <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import glossaryDefinitionDescriptor from '../GlossaryDefinitionDescriptor.json'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'

export default defineComponent({
    name: 'glossary-definition-info-dialog',
    components: { Dialog },
    emits: ['close'],
    props: {
        visible: { type: Boolean },
        contentInfo: { type: Object }
    },
    data() {
        return {
            glossaryDefinitionDescriptor,
            glossaryDefinitionDialogDescriptor
        }
    }
})
</script>

<style lang="scss" scoped>
ul {
    list-style: none;
}

span {
    font-weight: 600;
    text-transform: capitalize;
}

p {
    margin: 1rem 0 1rem 1.5rem;
}
</style>
