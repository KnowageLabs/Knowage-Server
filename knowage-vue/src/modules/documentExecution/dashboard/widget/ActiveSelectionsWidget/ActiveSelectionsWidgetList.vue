<template>
    <ul class="active-selections-list">
        <li v-for="(selection, index) of activeSelections" :key="index" :style="getRowStyle(index)">
            <p v-if="showDataset">{{ selection.datasetLabel }}</p>
            <p v-if="showColumn">{{ selection.columnName }}</p>
            <p>{{ selection.value?.toString() }}</p>
            <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" v-tooltip.left="$t('common.delete')" />
        </li>
    </ul>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ISelection, IWidget } from '../../Dashboard'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: {},
    props: {
        showDataset: Boolean,
        showColumn: Boolean,
        activeSelections: { type: Array as PropType<ISelection[]>, required: true },
        propWidget: { type: Object as PropType<IWidget>, required: true }
    },
    emits: ['selectionDeleted'],
    computed: {},
    data() {
        return {}
    },
    setup() {},
    created() {},
    updated() {},
    methods: {
        getRowStyle(rowIndex) {
            var rowStyles = this.propWidget.settings.style.rows

            if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
                if (rowStyles.alternatedRows.oddBackgroundColor && rowIndex % 2 === 0) {
                    return { background: rowStyles.alternatedRows.oddBackgroundColor, height: `${rowStyles.height}px` }
                } else return { background: rowStyles.alternatedRows.evenBackgroundColor, height: `${rowStyles.height}px` }
            }
        }
    }
})
</script>
<style lang="scss" scoped>
.active-selections-list {
    flex: 1;
    padding: 0;
    margin: 0;
    list-style-type: none;
    li {
        list-style: none;
        justify-content: flex-start;
        align-items: center;
        align-content: center;
        max-width: 100%;
        display: flex;
        flex-direction: row;
        position: relative;
        padding: 0 16px;
        flex: 1 1 auto;
        padding-right: 0;
        border-top: 0;
        p {
            list-style: none;
            color: rgba(0, 0, 0, 0.87);
            margin: 0;
            max-width: 100%;
            flex: 1;
            box-sizing: border-box;
            min-width: 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-size: 0.7rem;
        }
    }
}
</style>
