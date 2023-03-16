<template>
    <Dialog :header="$t('managers.lovsManagement.test')" :visible="visible" :modal="true" class="testLovDialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-fluid p-m-4">
            <div>
                <span>
                    <label for="treeType" class="kn-material-input-label" aria-label="dropdown">{{ $t('managers.lovsManagement.testTreeType') }} * </label>
                    <Dropdown id="treeType" v-model="treeListTypeModel.LOVTYPE" class="kn-material-input" :options="lovsManagementTestDialogDescriptor.treeTypes" option-label="name" option-value="value" @change="resetValues" />
                </span>
            </div>
        </div>
        <LovsManagementSimpleDatatable v-if="treeListTypeModel.LOVTYPE === 'simple'" :table-data="model" :tree-list-type-model="treeListTypeModel" @modelChanged="onModelChange($event)"></LovsManagementSimpleDatatable>
        <LovsManagementTree v-else :list-data="model" :tree-model="treeModel" @modelChanged="onTreeModelChange($event)"></LovsManagementTree>
        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="buttonDisabled" @click="onSave"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../../LovsManagement'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import lovsManagementTestDialogDescriptor from './LovsManagementTestDialogDescriptor.json'
import LovsManagementSimpleDatatable from './LovsManagementSimpleDatatable/LovsManagementSimpleDatatable.vue'
import LovsManagementTree from './LovsManagementTree/LovsManagementTree.vue'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'lovs-management-test-dialog',
    components: { Dialog, Dropdown, LovsManagementSimpleDatatable, LovsManagementTree },
    props: {
        visible: { type: Boolean },
        testModel: {
            type: Object
        },
        testLovModel: {
            type: Array
        },
        selectedLov: {
            type: Object
        },
        testLovTreeModel: {
            type: Array
        }
    },
    emits: ['close', 'pageChanged', 'save'],
    data() {
        return {
            lovsManagementTestDialogDescriptor,
            lov: {} as iLov,
            treeListTypeModel: {} as any,
            model: {} as any,
            treeModel: {} as any
        }
    },
    computed: {
        buttonDisabled(): boolean {
            if (this.treeListTypeModel.LOVTYPE !== 'simple') {
                return this.treeModel.length === 0
            }
            return false
        }
    },
    watch: {
        visible() {
            this.loadLov()
            this.loadTypeModel()
            this.loadModel()
            this.loadTreeModel()
        },
        selectedLov() {
            this.loadLov()
        },
        testModel() {
            this.loadTypeModel()
        },
        testLovModel() {
            this.loadModel()
            this.loadTypeModel()
        },
        testLovTreeModel() {
            this.loadTreeModel()
        }
    },
    created() {
        this.loadLov()
        this.loadTypeModel()
        this.loadModel()
        this.loadTreeModel()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
        },
        loadTypeModel() {
            this.treeListTypeModel = deepcopy(this.testModel)
        },
        loadModel() {
            this.model = deepcopy(this.testLovModel)
        },
        loadTreeModel() {
            this.treeModel = deepcopy(this.testLovTreeModel)
        },
        resetValues() {
            this.treeModel = []
            if (this.treeListTypeModel.LOVTYPE === 'simple') {
                delete this.treeListTypeModel['VALUE-COLUMNS']
                delete this.treeListTypeModel['DESCRIPTION-COLUMNS']
                this.treeListTypeModel['VALUE-COLUMN'] = ''
                this.treeListTypeModel['DESCRIPTION-COLUMN'] = ''
            } else {
                delete this.treeListTypeModel['VALUE-COLUMN']
                delete this.treeListTypeModel['DESCRIPTION-COLUMN']
                this.treeListTypeModel['VALUE-COLUMNS'] = ''
                this.treeListTypeModel['DESCRIPTION-COLUMNS'] = ''
            }
            this.treeListTypeModel['VISIBLE-COLUMNS'] = ''
        },
        onSave() {
            this.$emit('save', { treeListTypeModel: this.treeListTypeModel, model: this.model, treeModel: this.treeModel })
        },
        onModelChange(event: any) {
            this.treeListTypeModel = event
        },
        onTreeModelChange(event: any) {
            this.treeModel = event
        }
    }
})
</script>

<style lang="scss">
.testLovDialog {
    width: 960px;
}
.full-screen-dialog.p-dialog {
    max-height: 100%;
}
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
}
</style>
