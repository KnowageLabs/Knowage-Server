<template>
    <div>
        <li>
            <span>{{ $t('managers.glossary.glossaryUsage.associatedWord') }}:</span>
            <ul class="p-my-3">
                <li v-for="(column, index) in wordsList" :key="index" class="glossary-info-word" :class="{ 'selected-word': wordIsSelected(column) }">
                    <span>{{ column.WORD }}</span>
                </li>
            </ul>
        </li>
        <li v-if="columnField">
            <span>{{ $t('managers.glossary.glossaryUsage.column') }}:</span>
            <ul class="p-my-3">
                <li v-for="(column, index) in columnList" :key="index">
                    {{ column[columnField] }}
                    <ul>
                        <li v-for="(word, index) in column.word" :key="index" class="glossary-info-word" :class="{ 'selected-word': wordIsSelected(word) }">
                            <span>{{ word.WORD }}</span>
                        </li>
                    </ul>
                </li>
            </ul>
        </li>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'glossary-usage-info-dialog-words-columns-list',
    props: {
        selectedWords: { type: Array, required: true },
        wordsList: { type: Array },
        columnList: { type: Array },
        columnField: { type: String }
    },
    data() {
        return {}
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

.glossary-info-word {
    background-color: var(--kn-color-warning);
    padding: 0 8px;
}
.selected-word {
    color: red;
}
</style>
