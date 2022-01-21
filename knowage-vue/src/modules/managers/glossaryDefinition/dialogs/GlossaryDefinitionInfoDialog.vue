<template>
    <div>
        <Dialog
            :header="$t('managers.glossary.glossaryDefinition.details')"
            :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints"
            :style="glossaryDefinitionDialogDescriptor.dialog.style"
            :visible="visible"
            :modal="true"
            class="glossaryDefinitionDetail p-fluid kn-dialog--toolbar--primary"
            :closable="false"
        >
            <div v-if="contentInfo && contentInfo.CONTENT_ID">
                <ul>
                    <li>
                        <span>{{ $t('common.name') }}:</span>
                        <span>{{ contentInfo.CONTENT_NM }}</span>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.code') }}:</span>
                        <span>{{ contentInfo.CONTENT_CD }}</span>
                    </li>
                    <li>
                        <span>{{ $t('common.description') }}:</span>
                        <span>{{ contentInfo.CONTENT_DS }}</span>
                    </li>
                </ul>
            </div>
            <div v-else-if="contentInfo.WORD_ID">
                <ul>
                    <li>
                        <span>{{ $tc('managers.glossary.common.word', 1) }}:</span>
                        <span>{{ contentInfo.WORD }}</span>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.status') }}:</span>
                        <span v-if="contentInfo.STATE_NM">{{ $t(glossaryDefinitionDescriptor.translation[contentInfo.STATE_NM]) }}</span>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.category') }}:</span>
                        <span v-if="contentInfo.CATEGORY_NM">{{ $t(glossaryDefinitionDescriptor.translation[contentInfo.CATEGORY_NM]) }}</span>
                    </li>
                    <li>
                        <span>{{ $t('common.description') }}:</span>
                        <span>{{ contentInfo.DESCR }}</span>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.formula') }}:</span>
                        <span>{{ contentInfo.FORMULA }}</span>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.link') }}:</span>
                        <ul>
                            <li v-for="(link, index) in contentInfo.LINK" :key="index">
                                <span>
                                    <span>{{ link.WORD }}</span>
                                    <a v-if="index != contentInfo.LINK.length - 1">-</a>
                                </span>
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.attributes') }}:</span>
                        <ul>
                            <li v-for="(attribute, index) in contentInfo.SBI_GL_WORD_ATTR" :key="index">
                                <span>{{ attribute.ATTRIBUTE_NM }}:</span>
                                <span></span>
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

<style lang="scss">
.glossaryDefinitionDetail {
    ul {
        list-style: none;
        padding: 0;
        li {
            display: inline-flex;
            justify-content: flex-start;
            height: 40px;
            &:nth-child(even) {
                background-color: $list-item-alternated-background-color;
            }
            span {
                height: 40px;
                display: flex;
                justify-content: flex-start;
                align-items: center;
                &:first-child {
                    font-weight: 600;
                    padding-left: 10px;
                    text-transform: capitalize;
                    width: 150px;
                    align-items: center;
                }
                &:nth-child(2) {
                    flex: 1;
                }
            }
        }
    }

    p {
        margin: 1rem 0 1rem 1.5rem;
    }
}
</style>
