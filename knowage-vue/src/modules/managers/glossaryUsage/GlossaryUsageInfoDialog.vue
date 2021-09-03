<template>
    <div>
        <Dialog :style="glossaryUsageDescriptor.dialog.style" :header="$t('managers.glossary.glossaryUsage.details')" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
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
                        <span>{{ $t('managers.glossary.common.word') }}:</span>
                        <p>{{ contentInfo.WORD }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.status') }}:</span>
                        <p v-if="contentInfo.STATE_NM">{{ $t(glossaryUsageDescriptor.translation[contentInfo.STATE_NM]) }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.common.category') }}:</span>
                        <p v-if="contentInfo.CATEGORY_NM">{{ $t(glossaryUsageDescriptor.translation[contentInfo.CATEGORY_NM]) }}</p>
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
            <div v-else-if="contentInfo.type && contentInfo.type === 'document'">
                <ul>
                    <li>
                        <span>{{ $t('common.label') }}:</span>
                        <p>{{ contentInfo.data.label }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.name') }}:</span>
                        <p>{{ contentInfo.data.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.hierarchyScope') }}:</span>
                        <p>{{ contentInfo.data.functionalities[0] }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.profiledVisibility') }}:</span>
                        <ul class="p-mt-3">
                            <li class="inline-list-item" v-for="(link, index) in contentInfo.data.access" :key="index">{{ link }}</li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div v-else-if="contentInfo.type && contentInfo.type === 'dataset'">
                <ul>
                    <li>
                        <span>{{ $t('common.label') }}:</span>
                        <p>{{ contentInfo.data.DataSet.label }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.name') }}:</span>
                        <p>{{ contentInfo.data.DataSet.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.type') }}:</span>
                        <p>{{ contentInfo.data.DataSet.type }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.dataSource') }}:</span>
                        <p>{{ contentInfo.data.DataSet.configuration.dataSource }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.associatedWord') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.Word" :key="index" :class="{ 'selected-word': wordIsSelected(column) }">
                                {{ column.WORD }}
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.column') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.SbiGlDataSetWlist" :key="index">
                                {{ column.alias }}
                                <ul>
                                    <li v-for="(word, index) in column.word" :key="index" :class="{ 'selected-word': wordIsSelected(word) }">
                                        {{ word.WORD }}
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div v-else-if="contentInfo.type && contentInfo.type === 'businessClass'">
                <ul>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.metaModel') }}:</span>
                        <p>{{ contentInfo.data.metaBc.sbiMetaModel.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.businessClass') }}:</span>
                        <p>{{ contentInfo.data.metaBc.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.associatedWord') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.words" :key="index" :class="{ 'selected-word': wordIsSelected(column) }">
                                {{ column.WORD }}
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.column') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.sbiGlBnessClsWlist" :key="index">
                                {{ column.name }}
                                <ul>
                                    <li v-for="(word, index) in column.word" :key="index" :class="{ 'selected-word': wordIsSelected(word) }">
                                        {{ word.WORD }}
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div v-else-if="contentInfo.type && contentInfo.type === 'table'">
                <ul>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.metaSource') }}:</span>
                        <p>{{ contentInfo.data.metaSource.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('common.label') }}:</span>
                        <p>{{ contentInfo.data.metaTable.name }}</p>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.associatedWord') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.words" :key="index" :class="{ 'selected-word': wordIsSelected(column) }">
                                {{ column.WORD }}
                            </li>
                        </ul>
                    </li>
                    <li>
                        <span>{{ $t('managers.glossary.glossaryUsage.column') }}:</span>
                        <ul class="p-my-3">
                            <li v-for="(column, index) in contentInfo.data.sbiGlTableWlist" :key="index">
                                {{ column.name }}
                                <ul>
                                    <li v-for="(word, index) in column.word" :key="index" :class="{ 'selected-word': wordIsSelected(word) }">
                                        {{ word.WORD }}
                                    </li>
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
import glossaryUsageDescriptor from './GlossaryUsageDescriptor.json'

export default defineComponent({
    name: 'glossary-usage-info-dialog',
    components: { Dialog },
    emits: ['close'],
    props: {
        visible: { type: Boolean },
        contentInfo: { type: Object },
        selectedWords: { type: Array, required: true }
    },
    data() {
        return {
            glossaryUsageDescriptor
        }
    },
    methods: {
        wordIsSelected(word: any) {
            return this.selectedWords.findIndex((el: any) => word.WORD_ID === el.WORD_ID) > -1
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

.inline-list-item {
    display: inline-block;
    margin-right: 1rem;
}

.selected-word {
    color: red;
}
</style>
