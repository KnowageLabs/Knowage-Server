<template>
    <div class="p-grid p-ai-center">
        <div class="p-col-5">
            {{ 'TODO' }}
            {{ selectedFields }}
            <span v-if="errorMessageVisible" class="p-error">{{ $t('managers.hierarchyManagement.createHierarchyMasterErrorMessage') }}</span>
            <Listbox class="kn-list hierarchy-management-list" v-model="selectedFields" :options="dimensionFields" optionLabel="NAME" :multiple="true" @change="onSelectedField">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" @click="selectField(slotProps.option)">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.NAME }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="p-col-2">
            <div class="p-d-flex p-flex-column">
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button" icon="pi pi-angle-double-right" :disabled="selectedFields.length === 0" @click="moveToTheRight" />
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button p-mt-2" icon="pi pi-angle-double-left" @click="moveToTheLeft" />
            </div>
        </div>
        <div class="p-col-5">
            <Listbox class="kn-list hierarchy-management-list" v-model="selectedFields" :options="dimensionExportFields" optionLabel="NAME" :multiple="true" @change="onSelectedField">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" @click="selectField(slotProps.option)">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.NAME }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionField, iDimensionMetadata } from '../../../HierarchyManagement'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-select-list',
    components: { Listbox },
    props: { dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> } },
    data() {
        return {
            dimensionFields: [] as iDimensionField[],
            selectedFields: [] as iDimensionField[],
            dimensionExportFields: [] as any[],
            selectedExportFields: [] as any[],
            errorMessageVisible: false
        }
    },
    watch: {
        dimensionMetadata() {
            this.loadDimensionData()
        }
    },
    created() {
        this.loadDimensionData()
    },
    methods: {
        loadDimensionData() {
            this.dimensionFields = this.dimensionMetadata?.DIM_FIELDS as iDimensionField[]
        },
        selectField(field: iDimensionField) {
            console.log('SELECTED FIELD: ', field)
        },
        onSelectedField() {
            if (this.selectedFields.length === 3) {
                this.selectedFields.splice(2, 1)
                this.errorMessageVisible = true
            } else {
                this.errorMessageVisible = false
            }
        },
        moveToTheRight() {
            if (this.selectedFields.length === 1 || this.selectedFields.length === 2) {
                const newLevel = {}

                this.selectedExportFields.push(newLevel)
            }
        },
        moveToTheLeft() {}
    }
})
</script>

<style lang="scss" scoped>
.hierarchy-management-list {
    border: 1px solid var(--kn-color-borders);
    border-top: none;
}

.hierarchy-management-master-selecet-list-button {
    width: 150px;
}
</style>
