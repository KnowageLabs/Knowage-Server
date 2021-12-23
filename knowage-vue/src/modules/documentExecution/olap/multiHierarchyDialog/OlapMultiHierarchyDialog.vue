<template>
    <Dialog id="olap-sorting-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left> {{ member.caption }} </template>
            </Toolbar>
        </template>
        <span class="p-m-3">
            {{ $t('documentExecution.olap.multiHierarchy.detail1') }}
            <b>{{ member.hierarchies[member.selectedHierarchyPosition].caption }}.</b>
            {{ $t('documentExecution.olap.multiHierarchy.detail2') }}
        </span>
        <div class="p-col-12">
            <span class="p-float-label">
                <Dropdown id="hier" class="kn-material-input" v-model="selecetedMultiHierUN" :options="member.hierarchies" optionLabel="caption" optionValue="uniqueName" @change="setNewSessionRole" />
                <label for="hier" class="kn-material-input-label">{{ $t('documentExecution.olap.multiHierarchy.dropdownLabel') }} </label>
            </span>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOlapFilter } from '@/modules/documentExecution/olap/Olap'
import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import hierDialog from './OlapMultiHierarchyDialog.json'

export default defineComponent({
    components: { Dialog, Dropdown },
    props: { selectedFilter: { type: Object, required: true }, multiHierUN: { type: String, required: true } },
    emits: ['save'],
    data() {
        return {
            hierDialog,
            member: {} as iOlapFilter,
            selecetedMultiHierUN: '' as any
        }
    },
    watch: {
        selectedFilter() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.member = this.selectedFilter as iOlapFilter
            this.selecetedMultiHierUN = this.multiHierUN
        }
    }
})
</script>

<style lang="scss"></style>
