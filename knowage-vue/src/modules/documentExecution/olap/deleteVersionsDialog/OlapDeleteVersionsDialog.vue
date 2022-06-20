<template>
    <Dialog id="olap-delete-versions-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapDeleteVersionsDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.deleteVersion.title') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <DataTable v-if="!loading" class="p-datatable-sm kn-table p-m-4" v-model:selection="selectedVersions" :value="versions" dataKey="id">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column selectionMode="multiple" :headerStyle="olapDeleteVersionsDialogDescriptor.selectColumnStyle"></Column>
            <Column v-for="column in olapDeleteVersionsDialogDescriptor.columns" :key="column.header" :header="$t(column.header)" :field="column.field" :style="column.style" :sortable="true"> </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="close"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" @click="deleteVersions"> {{ $t('common.delete') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlap } from '../Olap'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import olapDeleteVersionsDialogDescriptor from './OlapDeleteVersionsDialogDescriptor.json'

export default defineComponent({
    name: 'olap-delete-versions-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, id: { type: String }, propOlapVersions: { type: Array as PropType<{ id: number; name: string; description: string }[]> }, olap: { type: Object as PropType<iOlap> } },
    emits: ['close'],
    computed: {},
    data() {
        return {
            olapDeleteVersionsDialogDescriptor,
            versions: [] as { id: number; name: string; description: string }[],
            selectedVersions: [] as { id: number; name: string; description: string }[],
            loading: false
        }
    },
    watch: {
        propOlapVersions() {
            this.loadVersions()
        }
    },
    created() {
        this.loadVersions()
    },
    methods: {
        loadVersions() {
            this.versions = this.propOlapVersions ? [...this.propOlapVersions] : []
        },
        close() {
            this.$emit('close')
            this.selectedVersions = []
        },
        async deleteVersions() {
            this.loading = true

            if (this.checkIfActiveVersionIsSelected()) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.deleteVersion.errorMessage') })
                this.loading = false
                return
            }

            const versionsToDelete = this.selectedVersions?.map((version: { id: number; name: string; description: string }) => version.id).join(',')
            if (!versionsToDelete || versionsToDelete.length === 0) return
            await this.$http
                .post(import.meta.env.VUE_APP_OLAP_PATH + `1.0/version/delete/${versionsToDelete}?SBI_EXECUTION_ID=${this.id}`, {}, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8', 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.close()
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error?.localizedMessage
                    })
                )
            this.loading = false
        },
        checkIfActiveVersionIsSelected() {
            if (!this.olap) return

            let selected = false
            let activeVersions = []
            for (let i = 0; i < this.olap.filters.length; i++) {
                if (this.olap.filters[i].name === 'Version') {
                    activeVersions = this.olap.filters[i].hierarchies[0].slicers
                    break
                }
            }

            for (let i = 0; i < this.selectedVersions.length; i++) {
                const index = activeVersions.findIndex((version: any) => version.name === this.selectedVersions[i].name)
                if (index !== -1) {
                    selected = true
                    break
                }
            }
            return selected
        }
    }
})
</script>

<style lang="scss">
#olap-delete-versions-dialog .p-dialog-header,
#olap-delete-versions-dialog .p-dialog-content {
    padding: 0;
}
#olap-delete-versions-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
