<template>
    <div id="associator-container" class="p-grid p-m-2">
        <div id="source-list-container" class="p-col p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('metaweb.businessModel.sourceAttr') }}
                </template>
            </Toolbar>
            <div class="kn-relative kn-flex">
                <Listbox class="associator-list kn-list data-condition-list kn-absolute kn-height-full kn-width-full" :options="sourceModel">
                    <template #empty>{{ $t('metaweb.businessModel.sourceHint') }} </template>
                    <template #option="slotProps">
                        <div :id="'source-' + slotProps.index" :ref="'source-' + slotProps.index" class="kn-list-item kn-draggable" draggable="true" @dragstart="onDragStart($event, 'source-' + slotProps.index)" @dragend="removeDragClass('source-' + slotProps.index)">
                            <i class="pi pi-bars"></i>
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
        <div id="target-list-container" class="p-col p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('metaweb.businessModel.targetAttr') }}
                </template>
            </Toolbar>
            <div class="kn-relative kn-flex">
                <Listbox class="associator-list kn-list data-condition-list kn-absolute kn-height-full kn-width-full" :options="targetModel">
                    <template #empty>{{ $t('metaweb.businessModel.targetHint') }} </template>
                    <template #option="slotProps">
                        <div
                            class="associator-target-list-item"
                            :id="'target-' + slotProps.index"
                            :ref="'target-' + slotProps.index"
                            @drop="onDrop($event, 'target-' + slotProps.index, slotProps.option)"
                            @dragover.prevent
                            @dragenter.prevent="setDropzoneClass(true, 'target-' + slotProps.index)"
                            @dragleave.prevent="setDropzoneClass(false, 'target-' + slotProps.index)"
                        >
                            <div class="associator-block-hover p-d-flex p-flex-row p-ai-center kn-width-full">
                                <span class="kn-truncated kn-flex-05">
                                    {{ slotProps.option.name }}
                                </span>
                                <i class="fas fa-link kn-flex-05" v-if="slotProps.option[associatedItem] && slotProps.option[associatedItem].length > 0" />
                                <div class="p-d-flex p-flex-column kn-flex" v-bind:class="{ 'p-mb-1': slotProps.option[associatedItem].length > 1 }" v-if="slotProps.option[associatedItem] && slotProps.option[associatedItem].length > 0">
                                    <span class="p-d-flex p-flex-row p-ai-center" v-for="(link, index) in slotProps.option[associatedItem]" v-bind:key="index">
                                        <span class="kn-truncated">
                                            {{ link.name }}
                                        </span>
                                        <Button v-if="slotProps.option[associatedItem].length > 1" icon="fas fa-times" class="associator-enable-hover p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option, link)" />
                                    </span>
                                </div>
                                <Button icon="far fa-trash-alt kn-flex-0" class="associator-enable-hover p-button-text p-button-rounded p-button-plain" v-if="slotProps.option[associatedItem] && slotProps.option[associatedItem].length > 0" @click.stop="deleteRelationship(slotProps.option)" />
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import bussinessModelDescriptor from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/MetawebBusinessModelDescriptor.json'
    import Listbox from 'primevue/listbox'

    export default defineComponent({
        components: { Listbox },
        props: { sourceArray: { type: Array, required: true }, targetArray: { type: Array, required: true }, useMultipleTablesFromSameSource: Boolean },
        emits: ['dropEnd', 'relationshipDeleted'],
        data() {
            return {
                bussinessModelDescriptor,
                sourceModel: [] as any,
                targetModel: [] as any,
                associatedItem: '',
                expandSummary: true
            }
        },
        created() {
            this.setAssociatedItem()
            this.targetModel = this.targetArray
            this.sourceModel = this.sourceArray
        },
        watch: {
            targetArray() {
                this.targetModel = this.targetArray
            },
            sourceArray() {
                this.sourceModel = this.sourceArray
            }
        },
        methods: {
            setAssociatedItem() {
                if (!this.associatedItem) {
                    this.associatedItem = 'links'
                }
            },
            onDragStart(event, elementId) {
                event.dataTransfer.setData('text', elementId.split('-')[1])
                event.dataTransfer.dropEffect = 'move'
                event.dataTransfer.effectAllowed = 'move'
                // @ts-ignore
                this.$refs[`${elementId}`].classList.add('associator-dragging')
            },
            removeDragClass(elementId) {
                // @ts-ignore
                this.$refs[`${elementId}`].classList.remove('associator-dragging')
            },
            setDropzoneClass(addClass, elementId) {
                // @ts-ignore
                addClass ? this.$refs[`${elementId}`].classList.add('associator-hover') : this.$refs[`${elementId}`].classList.remove('associator-hover')
            },
            setDropErrorClass(elementId) {
                // @ts-ignore
                this.$refs[`${elementId}`].classList.add('associator-drop-error')
                setTimeout(() => {
                    // @ts-ignore
                    this.$refs[`${elementId}`].classList.remove('associator-drop-error')
                }, 500)
            },
            beforeDrop(source, target) {
                if (target.links) {
                    for (var i = 0; i < target.links.length; i++) {
                        if (source.tableName === target.links[i].tableName) {
                            return false
                        }
                    }
                }
                return true
            },
            onDrop(event, elementId, targetElement) {
                // @ts-ignore
                this.$refs[`${elementId}`].classList.remove('associator-hover')
                var data = event.dataTransfer.getData('text')
                var executeDrop = true
                this.useMultipleTablesFromSameSource ? (executeDrop = this.beforeDrop(this.sourceModel[data], targetElement)) : ''
                if (executeDrop != false) {
                    if (targetElement[this.associatedItem] == undefined) {
                        targetElement[this.associatedItem] = []
                    }
                    if (targetElement[this.associatedItem].indexOf(this.sourceModel[data]) != -1) {
                        this.setDropErrorClass(elementId)
                    } else {
                        targetElement[this.associatedItem].push(this.sourceModel[data])
                        this.$emit('dropEnd', event, this.sourceModel[data], targetElement)
                    }
                } else {
                    this.setDropErrorClass(elementId)
                }
            },
            deleteRelationship(item, rel?) {
                rel == undefined ? (item[this.associatedItem] = []) : item[this.associatedItem].splice(rel, 1)
                this.$emit('relationshipDeleted')
            }
        }
    })
</script>
<style lang="scss">
    .associator-dragging {
        background-color: #bbd6ed;
        border: 1px dashed;
    }
    .associator-hover {
        background-color: rgba(128, 128, 128, 0.32);
        border: 1px dashed;
    }
    .associator-drop-error {
        background-color: rgba(255, 0, 0, 0.29);
        border: 1px dashed red;
    }
    .associator-block-hover {
        pointer-events: none !important;
    }
    .associator-enable-hover {
        pointer-events: auto !important;
    }
    .associator-target-list-item {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        align-items: center;
        padding: 0.75rem 0.75rem;
        border-bottom: 1px solid #f2f2f2;
    }
    .associator-list .p-listbox-list-wrapper {
        height: 100%;
    }
</style>
