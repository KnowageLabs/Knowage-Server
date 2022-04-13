<template>
    <Toolbar class="kn-toolbar kn-toolbar--default">
        <template #start>
            <Button v-if="!expandFilterCard" icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="expandFilterCard = true" />
            <Button v-else icon="fas fa-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="expandFilterCard = false" />
            <span>{{ $t('common.filters') }}</span>
        </template>
        <template #end>
            <Button v-if="expandFilterCard" icon="pi pi-check" class="p-button-text p-button-rounded p-button-plain" @click="applyFilters" />
            <Button v-if="expandFilterCard" icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="resetFilters" />
        </template>
    </Toolbar>
    <Card v-show="expandFilterCard">
        <template #content>
            <div class="p-grid p-fluid p-formgrid">
                <div class="p-field-checkbox p-col-12">
                    <Checkbox class="p-mr-2" v-model="showMissingElements" :binary="true" :disabled="!selectedHierarchy" />
                    <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.showMissingElements') }}</label>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <Calendar class="kn-material-input" v-model="afterDate" :manualInput="true" :showIcon="true" :disabled="!selectedHierarchy" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.afterDate') }}</label>
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iHierarchy } from '../../../HierarchyManagement'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Card from 'primevue/card'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-filter-card',
    components: { Calendar, Checkbox, Card },
    props: { selectedHierarchy: { type: Object as PropType<iHierarchy | null> } },
    emits: ['applyFilters'],
    data() {
        return {
            showMissingElements: false,
            expandFilterCard: false,
            afterDate: null as Date | null
        }
    },
    created() {},
    methods: {
        applyFilters() {
            this.$emit('applyFilters', { showMissingElements: this.showMissingElements, afterDate: this.afterDate })
        },
        resetFilters() {
            this.showMissingElements = false
            this.afterDate = null
            this.$emit('applyFilters', null)
        }
    }
})
</script>
