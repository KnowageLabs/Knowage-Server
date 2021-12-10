<template>
    <form ref="bvForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2">
        <div class="p-field p-col-12 p-md-6">
            <span class="p-float-label ">
                <Dropdown id="source" class="kn-material-input" v-model="sourceTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.sourceTable') }}</label>
            </span>
        </div>
        <div class="p-field p-col-12 p-md-6">
            <span class="p-float-label ">
                <Dropdown id="target" class="kn-material-input" v-model="targetTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.targetTable') }}</label>
            </span>
        </div>
    </form>
    <div id="attr-container" class="p-grid p-m-2">
        <div class="kn-remove-card-padding p-col">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('metaweb.businessModel.sourceAttr') }}
                </template>
            </Toolbar>
            <Listbox class="kn-list data-condition-list" :options="sourceTable.columns">
                <template #empty>{{ $t('metaweb.businessModel.sourceHint') }} </template>
                <template #option="slotProps">
                    <div class="kn-list-item kn-draggable" draggable="true" @dragstart="onDragStart($event, slotProps.option)">
                        <i class="pi pi-bars"></i>
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="kn-remove-card-padding p-col">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('metaweb.businessModel.targetAttr') }}
                </template>
            </Toolbar>
            <Listbox class="kn-list data-condition-list" :options="targetTable.columns">
                <template #empty>{{ $t('metaweb.businessModel.targetHint') }} </template>
                <template #option="slotProps">
                    <div class="kn-list-item" @drop="onDrop($event, slotProps.option)" @dragover.prevent>
                        <div class="kn-list-item-text">
                            <span class="kn-truncated">
                                {{ slotProps.option.name }}&emsp; <i v-if="slotProps.option.links" class="fas fa-link" />&emsp;
                                <span v-if="slotProps.option.links" class="kn-truncated">
                                    {{ slotProps.option.links[0].name }}
                                </span>
                            </span>
                        </div>
                        <Button v-if="slotProps.option.links" icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option)" />
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
    <div id="summary-container" class="p-m-3">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('metaweb.businessModel.summary') }}
            </template>
            <template #right>
                <Button v-if="!expandSummary" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = false" />
            </template>
        </Toolbar>
        <Listbox v-show="expandSummary" class="kn-list data-condition-list" :options="summary">
            <template #empty>{{ $t('metaweb.businessModel.summaryHint') }} </template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span class="p-d-flex p-flex-row" :style="bsDescriptor.style.summaryItemWidth">
                            <span v-if="slotProps.option.links" class="kn-truncated" :style="bsDescriptor.style.summaryItemFirst"> {{ slotProps.option.name }}&emsp; </span>
                            <i v-if="slotProps.option.links" class="fas fa-link" :style="bsDescriptor.style.summaryItemMiddle" />&emsp;
                            <span v-if="slotProps.option.links" class="kn-truncated" :style="bsDescriptor.style.summaryItemSecond"> {{ slotProps.option.links[0].name }} </span>
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option)" />
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import bsDescriptor from '../../MetawebBusinessModelDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'

export default defineComponent({
    components: { Dropdown, Listbox },
    props: { physicalModels: Array, summaryList: Array, bnssViewObject: Object },
    data() {
        return {
            bsDescriptor,
            tmpBnssView: {} as any,
            sourceTable: { columns: [] } as any,
            targetTable: { columns: [] } as any,
            summary: [] as any,
            expandSummary: true
        }
    },
    created() {
        this.tmpBnssView = this.bnssViewObject
        this.summary = this.summaryList
    },
    watch: {
        bnssViewObject() {
            this.tmpBnssView = this.bnssViewObject
            this.summary = this.summaryList
        }
    },
    methods: {
        onDragStart(event, sourceAttr) {
            event.dataTransfer.setData('text/plain', JSON.stringify(sourceAttr))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        beforeDrop(event, target) {
            var source = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (target.links) {
                for (var i = 0; i < target.links.length; i++) {
                    if (source.tableName === target.links[i].tableName) {
                        return false
                    }
                }
            }
            return true
        },
        onDrop(event, targetAttr) {
            var sourceAttr = JSON.parse(event.dataTransfer.getData('text/plain'))
            console.log('DROP SOURCE ATTR -------------------------', sourceAttr)
            console.log('DROP TARGET ATTR -------------------------', targetAttr)
            var executeDrop = true
            executeDrop = this.beforeDrop(event, targetAttr)

            if (executeDrop != false) {
                if (targetAttr.links == undefined) {
                    targetAttr.links = []
                }
                targetAttr.links.push(sourceAttr)
                targetAttr.test = 'linkedClass'
            }

            this.updateSummary()
        },
        deleteRelationship(item) {
            delete item.links
            this.updateSummary()
        },
        updateSummary() {
            this.summary = []
            for (var i = 0; i < this.tmpBnssView.physicalModels.length; i++) {
                for (var col = 0; col < this.tmpBnssView.physicalModels[i].columns.length; col++) {
                    // eslint-disable-next-line no-prototype-builtins
                    if (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        this.summary.push(this.tmpBnssView.physicalModels[i].columns[col])
                    }
                }
            }
        }
    }
})
</script>
