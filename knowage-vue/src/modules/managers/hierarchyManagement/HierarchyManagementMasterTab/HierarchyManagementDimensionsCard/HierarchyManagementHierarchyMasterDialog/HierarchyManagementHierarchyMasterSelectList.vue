<template>
    <small v-if="errorMessageVisible" class="p-error p-ml-2" data-test="error-message">{{ $t('managers.hierarchyManagement.createHierarchyMasterErrorMessage') }}</small>
    <div class="p-d-flex kn-flex kn-overflow">
        <div id="list-container" class="p-d-flex p-ml-2" style="flex: 1 1 0;">
            <Listbox v-model="selectedSourceFields" class="kn-list kn-list-border-all kn-flex hierarchy-management-list" list-style="max-height:calc(100% - 62px)" :options="dimensionSourceFields" option-label="NAME" :multiple="true" :filter="true" @change="onSelectedField">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <span :data-test="'list-item-' + slotProps.option.NAME">{{ slotProps.option.NAME }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>

        <div id="button-container" class="p-as-center p-mx-2">
            <div class="p-d-flex p-flex-column">
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button" icon="pi pi-angle-double-right" :disabled="selectedSourceFields.length === 0" data-test="move-right-button" @click="moveToTheRight" />
                <Button class="kn-button kn-button--primary hierarchy-management-master-selecet-list-button p-mt-2" icon="pi pi-angle-double-left" :disabled="selectedDestinationFields.length === 0" @click="moveToTheLeft" />
            </div>
        </div>

        <div id="identifier-container" class="p-d-flex p-mr-2" style="flex: 1 1 0;">
            <Listbox v-model="selectedDestinationFields" class="kn-list kn-list-border-all kn-flex hierarchy-management-list" list-style="max-height:100%" :options="dimensionDestinationFields" option-label="NAME" :multiple="true" data-test="selected-destinations-list" @change="onSelectedField">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="p-d-flex p-flex-row kn-flex">
                            <span>
                                <b>{{ $t('managers.hierarchyManagement.lev') + ' ' + slotProps.option.code.level + ' ' }}</b>
                                {{ slotProps.option.code.NAME + ', ' + slotProps.option.name.NAME }}
                            </span>
                            <div class="p-ml-auto">
                                <Button
                                    v-if="slotProps.index === dimensionDestinationFields.length - 1"
                                    icon="fa fa-plus"
                                    class="p-button-text p-button-plain p-button-sm p-p-0"
                                    :data-test="'recursive-button-' + slotProps.option.code.NAME"
                                    @click.stop="moveToRecursive(slotProps.option, slotProps.index)"
                                />
                                <Button v-if="slotProps.index !== 0" icon="fa fa-arrow-up" class="p-button-text p-button-plain p-button-sm p-p-0" @click.stop="move(slotProps.option, slotProps.index, 'up')" />
                                <Button v-if="slotProps.index !== dimensionDestinationFields.length - 1" icon="fa fa-arrow-down" class="p-button-text p-button-plain p-button-sm p-p-0" @click.stop="move(slotProps.option, slotProps.index, 'down')" />
                            </div>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
    <div id="recursive-container" class="p-m-2">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
            <template #start>
                {{ $t('managers.hierarchyManagement.recursive') + ': ' }}
                <span v-if="recursive" class="p-ml-2">{{ recursive.code.NAME + ' ' + recursive.name.NAME }}</span>
            </template>
            <template #end>
                <Button v-show="recursive" icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" @click="removeRecursive" />
            </template>
        </Toolbar>
        <form class="marginated-form p-fluid p-formgrid p-grid">
            <div class="p-field p-col-6">
                <span class="p-float-label">
                    <Dropdown
                        v-model="recursiveParentName"
                        class="kn-material-input"
                        :options="parentDimensionSourceFields"
                        option-label="NAME"
                        :disabled="!recursive"
                        @change="$emit('recursiveChanged', { recursive: recursive, recursiveParentName: recursiveParentName, recursiveParentDescription: recursiveParentDescription })"
                    >
                    </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.name') }} </label>
                </span>
            </div>
            <div class="p-field p-col-6">
                <span class="p-float-label">
                    <Dropdown
                        v-model="recursiveParentDescription"
                        class="kn-material-input"
                        :options="parentDimensionSourceFields"
                        option-label="NAME"
                        :disabled="!recursive"
                        @change="$emit('recursiveChanged', { recursive: recursive, recursiveParentName: recursiveParentName, recursiveParentDescription: recursiveParentDescription })"
                    >
                    </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.description') }} </label>
                </span>
            </div>
        </form>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionField, iDimensionMetadata } from '../../../HierarchyManagement'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-select-list',
    components: {
        Dropdown,
        Listbox
    },
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

            destinationField.code.level = tempIndex + 1
            destinationField.name.level = tempIndex + 1

            this.dimensionDestinationFields[index] = this.dimensionDestinationFields[tempIndex]
            this.dimensionDestinationFields[index].code.level = index + 1
            this.dimensionDestinationFields[index].level = index + 1
            this.dimensionDestinationFields[tempIndex] = destinationField
            this.$emit('levelsChanged', this.dimensionDestinationFields)
        },
        moveToRecursive(destinationField: any, index: number) {
            if (this.recursive) {
                this.selectedDestinationFields = [this.recursive]
                this.moveToTheLeft()
            }
            destinationField.isLast = false
            this.dimensionDestinationFields.splice(index, 1)
            if (this.dimensionDestinationFields.length > 0) {
                this.dimensionDestinationFields[this.dimensionDestinationFields.length - 1].isLast = true
            }
            this.selectedDestinationFields = []
            this.recursive = destinationField
            this.$emit('recursiveChanged', { recursive: this.recursive, recursiveParentName: this.recursiveParentName, recursiveParentDescription: this.recursiveParentDescription, levels: this.dimensionDestinationFields })
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
