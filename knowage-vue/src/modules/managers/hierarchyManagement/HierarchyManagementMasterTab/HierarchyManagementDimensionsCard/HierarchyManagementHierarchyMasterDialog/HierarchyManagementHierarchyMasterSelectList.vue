<template>
    <div class="p-grid p-ai-center">
        <div class="p-col-5">
            {{ 'TODO' }}
            {{ selectedSourceFields }}
            <span v-if="errorMessageVisible" class="p-error">{{ $t('managers.hierarchyManagement.createHierarchyMasterErrorMessage') }}</span>
            <Listbox class="kn-list hierarchy-management-list" v-model="selectedSourceFields" :options="dimensionSourceFields" optionLabel="NAME" :multiple="true" @change="onSelectedField">
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
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button" icon="pi pi-angle-double-right" :disabled="selectedSourceFields.length === 0" @click="moveToTheRight" />
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button p-mt-2" icon="pi pi-angle-double-left" :disabled="selectedDestinationFields.length === 0" @click="moveToTheLeft" />
            </div>
        </div>
        <div class="p-col-5">
            {{ 'TODO' }}
            {{ selectedDestinationFields }}
            <Listbox class="kn-list hierarchy-management-list" v-model="selectedDestinationFields" :options="dimensionDestinationFields" optionLabel="NAME" :multiple="true" @change="onSelectedField">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" @click="selectField(slotProps.option)">
                        <div class="p-d-flex p-flex-row p-jc-start p-ai-center">
                            <span
                                ><b>{{ $t('managers.hierarchyManagement.lev') + ' ' + slotProps.option.code.level + ' ' }}</b
                                >{{ slotProps.option.code.NAME + ', ' + slotProps.option.name.NAME }}</span
                            >
                            <Button v-if="slotProps.index === dimensionDestinationFields.length - 1" icon="fa fa-plus" class="p-button-link p-button-sm p-p-0" @click.stop="moveToRecursive(slotProps.option, slotProps.index)" />
                            <Button v-if="slotProps.index !== 0" icon="fa fa-arrow-up" class="p-button-link p-button-sm p-p-0" @click.stop="move(slotProps.option, slotProps.index, 'up')" />
                            <Button v-if="slotProps.index !== dimensionDestinationFields.length - 1" icon="fa fa-arrow-down" class="p-button-link p-button-sm p-p-0" @click.stop="move(slotProps.option, slotProps.index, 'down')" />
                        </div>
                    </div>
                </template>
            </Listbox>
            <div class="recursive-container">
                <div class="p-d-flex p-flex-rowbp-ai-center">
                    <div>
                        <span>{{ $t('managers.hierarchyManagement.recursive') + ': ' }}</span
                        ><span v-if="recursive">{{ recursive.code.NAME + ' ' + recursive.name.NAME }}</span>
                    </div>
                    <Button v-show="recursive" icon="pi pi-trash" class="p-button-link p-ml-auto" @click="removeRecursive" />
                </div>
                <div class="p-d-flex p-flex-row">
                    <div class="kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown
                                class="kn-material-input"
                                v-model="recursiveParentName"
                                :options="parentDimensionSourceFields"
                                optionLabel="NAME"
                                :disabled="!recursive"
                                @change="$emit('recursiveChanged', { recursive: recursive, recursiveParentName: recursiveParentName, recursiveParentDescription: recursiveParentDescription })"
                            >
                            </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('common.name') }} </label>
                        </span>
                    </div>
                    <div class="kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown
                                class="kn-material-input"
                                v-model="recursiveParentDescription"
                                :options="parentDimensionSourceFields"
                                optionLabel="NAME"
                                :disabled="!recursive"
                                @change="$emit('recursiveChanged', { recursive: recursive, recursiveParentName: recursiveParentName, recursiveParentDescription: recursiveParentDescription })"
                            >
                            </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('common.description') }} </label>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionField, iDimensionMetadata } from '../../../HierarchyManagement'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-select-list',
    components: { Dropdown, Listbox },
    props: { dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> } },
    emits: ['recursiveChanged', 'levelsChanged'],
    data() {
        return {
            dimensionSourceFields: [] as iDimensionField[],
            selectedSourceFields: [] as iDimensionField[],
            dimensionDestinationFields: [] as any[],
            selectedDestinationFields: [] as any[],
            errorMessageVisible: false,
            recursive: null as any,
            parentDimensionSourceFields: [] as iDimensionField[],
            recursiveParentName: null as any,
            recursiveParentDescription: null as any
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
            this.dimensionSourceFields = []
            this.parentDimensionSourceFields = []
            this.dimensionMetadata?.DIM_FIELDS.forEach((filter: iDimensionField) => (filter.PARENT ? this.parentDimensionSourceFields.push(filter) : this.dimensionSourceFields.push(filter)))
        },
        selectField(field: iDimensionField) {
            console.log('SELECTED FIELD: ', field)
        },
        onSelectedField() {
            if (this.selectedSourceFields.length === 3) {
                this.selectedSourceFields.splice(2, 1)
                this.errorMessageVisible = true
            } else {
                this.errorMessageVisible = false
            }
        },
        moveToTheRight() {
            if (this.selectedSourceFields.length === 1 || this.selectedSourceFields.length === 2) {
                const newLevel = { code: this.selectedSourceFields[0], name: this.selectedSourceFields[1] ?? this.selectedSourceFields[0], hasCopy: this.selectedSourceFields.length === 1, isLast: true } as any
                newLevel.code.level = this.dimensionDestinationFields.length + 1
                newLevel.name.level = this.dimensionDestinationFields.length + 1
                if (this.dimensionDestinationFields.length > 0) this.dimensionDestinationFields[this.dimensionDestinationFields.length - 1].isLast = false
                for (let i = 0; i < this.selectedSourceFields.length; i++) {
                    const index = this.dimensionSourceFields.findIndex((el: any) => el.ID === this.selectedSourceFields[i].ID)
                    if (index !== -1) this.dimensionSourceFields.splice(index, 1)
                }
                this.selectedSourceFields = []
                this.dimensionDestinationFields.push(newLevel)
            }
            this.$emit('levelsChanged', this.dimensionDestinationFields)
        },

        moveToTheLeft() {
            if (this.selectedDestinationFields.length === 0) return
            for (let i = 0; i < this.selectedDestinationFields.length; i++) {
                delete this.selectedDestinationFields[i].code.level
                delete this.selectedDestinationFields[i].name.maxLevel
                this.dimensionSourceFields.push(this.selectedDestinationFields[i].code)
                if (!this.selectedDestinationFields[i].hasCopy) {
                    this.dimensionSourceFields.push(this.selectedDestinationFields[i].name)
                }

                const index = this.dimensionDestinationFields.findIndex((el: any) => el.code.ID === this.selectedDestinationFields[i].code.ID)
                if (index !== -1) this.dimensionDestinationFields.splice(index, 1)
            }

            this.selectedDestinationFields = []

            for (let i = 0; i < this.dimensionDestinationFields.length; i++) {
                this.dimensionDestinationFields[i].code.level = i + 1
                this.dimensionDestinationFields[i].name.level = i + 1
            }
            this.$emit('levelsChanged', this.dimensionDestinationFields)
        },
        move(destinationField: any, index: number, direction: 'up' | 'down') {
            const tempIndex = direction === 'up' ? index - 1 : index + 1
            destinationField.code.level = tempIndex
            destinationField.name.level = tempIndex

            this.dimensionDestinationFields[index] = this.dimensionDestinationFields[tempIndex]
            this.dimensionDestinationFields[index].code.level = index
            this.dimensionDestinationFields[index].level = index
            this.dimensionDestinationFields[tempIndex] = destinationField
            this.$emit('levelsChanged', this.dimensionDestinationFields)
        },
        moveToRecursive(destinationField: any, index: number) {
            console.log('DEST FIELD: ', destinationField)
            destinationField.isLast = false
            this.dimensionDestinationFields.splice(index, 1)
            if (this.dimensionDestinationFields.length > 0) {
                this.dimensionDestinationFields[this.dimensionDestinationFields.length - 1].isLast = true
            }
            this.selectedDestinationFields = []
            this.recursive = destinationField
            this.$emit('recursiveChanged', { recursive: this.recursive, recursiveParentName: this.recursiveParentName, recursiveParentDescription: this.recursiveParentDescription })
        },
        removeRecursive() {
            this.selectedDestinationFields = [this.recursive]
            this.moveToTheLeft()
            this.recursive = false
            this.recursiveParentName = null
            this.recursiveParentDescription = null
            this.$emit('recursiveChanged', null)
        }
    }
})
</script>

<style lang="scss" scoped>
.hierarchy-management-list {
    border: 1px solid var(--kn-color-borders);
    border-top: none;
    max-height: 300px;
}

.hierarchy-management-master-selecet-list-button {
    width: 150px;
}

.recursive-container {
    border: 1px solid #c2c2c2;
}
</style>
