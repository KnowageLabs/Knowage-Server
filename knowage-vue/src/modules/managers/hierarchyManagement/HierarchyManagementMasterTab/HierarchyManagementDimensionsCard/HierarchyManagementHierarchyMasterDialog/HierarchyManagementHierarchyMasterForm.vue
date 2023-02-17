<template>
    <form class="marginated-form p-fluid p-formgrid p-grid kn-flex p-m-2">
        <template v-for="(field, index) in fields" :key="index">
            <div v-if="field.VISIBLE" class="p-field p-col-6">
                <span class="p-float-label">
                    <InputText
                        v-model.trim="field.value"
                        class="kn-material-input"
                        :type="field.TYPE === 'Number' ? 'number' : 'text'"
                        :class="{
                            'p-invalid': !field.value
                        }"
                        :disabled="!field.EDITABLE"
                    />
                    <label class="kn-material-input-label"> {{ field.NAME + ' *' }}</label>
                </span>
            </div>
        </template>
    </form>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNodeMetadataField } from '../../../HierarchyManagement'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-form',
    components: {},
    props: { nodeGeneralFields: { type: Array as PropType<iNodeMetadataField[]> } },
    data() {
        return {
            fields: [] as iNodeMetadataField[]
        }
    },
    watch: {
        nodeGeneralFields() {
            this.loadFields()
        }
    },
    created() {
        this.loadFields()
    },
    methods: {
        loadFields() {
            this.fields = this.nodeGeneralFields as iNodeMetadataField[]
        }
    }
})
</script>
