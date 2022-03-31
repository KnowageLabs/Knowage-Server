<template>
    <Dialog id="hierarchy-management-hierarchy-master-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="hierarchyManagementHierarchyMasterDialogDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.createHierarchyMaster') }}
                </template>
            </Toolbar>
        </template>

        <div>
            <HierarchyManagementHierarchyMasterForm :nodeGeneralFields="nodeGeneralFields"></HierarchyManagementHierarchyMasterForm>
            <HierarchyManagementHierarchyMasterSelectList :dimensionMetadata="dimensionMetadata" @recursiveChanged="onRecursiveChanged" @levelsChanged="onLevelsChanged"></HierarchyManagementHierarchyMasterSelectList>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="close">{{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="save">{{ $t('common.save') }}</Button>
        </template>

        <KnOverlaySpinnerPanel :visibility="loading"></KnOverlaySpinnerPanel>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNodeMetadata, iNodeMetadataField, iDimensionMetadata, iDimension, iDimensionFilter } from '../../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Dialog from 'primevue/dialog'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import hierarchyManagementHierarchyMasterDialogDescriptor from './HierarchyManagementMasterDescriptor.json'
import HierarchyManagementHierarchyMasterForm from './HierarchyManagementHierarchyMasterForm.vue'
import HierarchyManagementHierarchyMasterSelectList from './HierarchyManagementHierarchyMasterSelectList.vue'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-dialog',
    components: { Dialog, HierarchyManagementHierarchyMasterForm, HierarchyManagementHierarchyMasterSelectList, KnOverlaySpinnerPanel },
    props: {
        visible: { type: Boolean },
        nodeMetadata: { type: Object as PropType<iNodeMetadata | null> },
        dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> },
        validityDate: { type: Date },
        selectedDimension: { type: Object as PropType<iDimension | null> },
        dimensionFilters: { type: Array as PropType<iDimensionFilter[]> }
    },
    emits: ['close', 'masterHierarchyCreated'],
    data() {
        return {
            hierarchyManagementHierarchyMasterDialogDescriptor,
            nodeGeneralFields: [] as iNodeMetadataField[],
            recursive: null as { NM: string; CD: string; NM_PARENT: string; CD_PARENT: string } | null,
            levels: [] as { CD: string; NM: string }[],
            loading: false
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return this.requiredFieldMissing()
        }
    },
    watch: {
        visible(value: boolean) {
            if (value) this.loadNodeData()
        },
        nodeMetadata() {
            this.loadNodeData()
        }
    },
    async created() {
        this.loadNodeData()
    },
    methods: {
        loadNodeData() {
            this.nodeGeneralFields = this.nodeMetadata
                ? this.nodeMetadata.GENERAL_FIELDS.map((field: iNodeMetadataField) => {
                      return { ...field, value: '' }
                  })
                : []
        },
        requiredFieldMissing() {
            let requiredMissing = false

            for (let i = 0; i < this.nodeGeneralFields.length; i++) {
                if (this.nodeGeneralFields[i].VISIBLE && !this.nodeGeneralFields[i].value) {
                    requiredMissing = true
                    break
                }
            }

            return requiredMissing
        },
        onLevelsChanged(selectedLevels: any[]) {
            this.levels = selectedLevels?.map((level: any) => {
                return { CD: level.code?.ID, NM: level.name?.ID }
            })
        },
        onRecursiveChanged(payload: any) {
            if (!payload) {
                this.recursive = null
                return
            }

            if (payload.levels) this.onLevelsChanged(payload.levels)

            this.recursive = { NM: payload.recursive.name?.ID, CD: payload.recursive.code?.ID, NM_PARENT: payload.recursiveParentName?.ID, CD_PARENT: payload.recursiveParentDescription?.ID }
        },
        async save() {
            console.log('LEVELS: ', this.levels)
            const postData = {
                dimension: this.selectedDimension?.DIMENSION_NM,
                validityDate: moment(this.validityDate).format('YYYY-MM-DD'),
                optionalFilters: this.dimensionFilters?.filter((filter: iDimensionFilter) => filter.VALUE && filter.VALUE !== ''),
                levels: this.levels,
                recursive: this.recursive
            }
            for (let i = 0; i < this.nodeGeneralFields.length; i++) {
                if (this.nodeGeneralFields[i].VISIBLE) {
                    postData[this.nodeGeneralFields[i].ID] = this.nodeGeneralFields[i].TYPE === 'NUMBER' && this.nodeGeneralFields[i].value ? parseInt(this.nodeGeneralFields[i].value as string) : this.nodeGeneralFields[i].value
                }
            }

            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchiesMaster/createHierarchyMaster`, postData)
                .then((response: AxiosResponse<any>) => {
                    if (response.data?.response === 'ok') {
                        this.$emit('masterHierarchyCreated')
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    }
                })
                .catch(() => {})
            this.loading = false
        },
        close() {
            this.nodeGeneralFields = []
            this.levels = []
            this.recursive = null
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#hierarchy-management-hierarchy-master-dialog .p-dialog-header,
#hierarchy-management-hierarchy-master-dialog .p-dialog-content {
    padding: 0;
}
#hierarchy-management-hierarchy-master-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
