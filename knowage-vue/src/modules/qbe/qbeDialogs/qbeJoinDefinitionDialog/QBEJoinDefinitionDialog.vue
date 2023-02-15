<template>
    <Dialog id="qbe-join-definition-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEJoinDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('qbe.joinDefinitions.dialogTitle') }}
                </template>
            </Toolbar>
        </template>

        <div>
            <div class="p-d-flex p-ai-center p-m-4">
                <i class="pi pi-search kn-cursor-pointer" />
                <InputText v-model="searchWord" class="kn-material-input p-ml-2" :placeholder="$t('common.search')" @input="searchItems" />
            </div>

            <div class="p-m-4">
                <div v-for="(relation, index) in filteredRelations" :key="relation.id" class="p-d-flex p-flex-row p-ai-center p-m-3">
                    <label class="p-text-bold">{{ index + 1 }}</label>
                    <Dropdown v-model="relation.joinType" class="kn-material-input p-mx-4" :options="QBEJoinDefinitionDialogDescriptor.joinTypes" />
                    <label v-tooltip.top="relation.attributes.sourceEntity + ' --- ' + relation.attributes.entity" class="p-text-bold kn-truncated">{{ relation.attributes.sourceEntity + ' --- ' + relation.attributes.entity }}</label>
                    <div class="p-d-flex p-flex-row p-ml-auto">
                        <i v-show="index !== 0 && searchWord.trim() === ''" class="fa fa-arrow-up kn-cursor-pointer" @click="moveUp(index)"></i>
                        <i v-show="index !== filteredRelations.length - 1 && searchWord.trim() === ''" class="fa fa-arrow-down kn-cursor-pointer p-ml-2" @click="moveDown(index)"></i>
                        <Checkbox v-model="relation.isConsidered" class="p-ml-3" :binary="true"></Checkbox>
                    </div>
                </div>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iField } from '../../QBE'
import { AxiosResponse } from 'axios'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import QBEJoinDefinitionDialogDescriptor from './QBEJoinDefinitionDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-join-definition-dialog',
    components: { Checkbox, Dialog, Dropdown },
    props: { visible: { type: Boolean }, qbe: { type: Object }, selectedQuery: { type: Object as PropType<iQuery> }, propEntities: { type: Array }, id: { type: String } },
    emits: ['close'],
    data() {
        return {
            QBEJoinDefinitionDialogDescriptor,
            entityNames: [] as string[],
            usedEntities: [] as any[],
            relations: [] as any[],
            filteredRelations: [] as any[],
            searchWord: '',
            query: {} as iQuery,
            entities: [] as any[]
        }
    },
    watch: {
        async visible(isVisible: boolean) {
            if (isVisible) {
                this.loadQuery()
                await this.loadEntityNames()
                this.loadData()
            }
        },
        selectedQuery() {
            this.loadQuery()
        },
        propEntities() {
            this.loadEntities()
            this.loadData()
        }
    },
    async created() {
        this.loadQuery()
        await this.loadEntityNames()
        this.loadEntities()
        this.loadData()
    },
    methods: {
        loadData() {
            if (this.qbe && this.query && this.entities) {
                this.usedEntities = []
                this.entities?.forEach((entity: any) => {
                    if (this.entityNames.includes(entity.id)) {
                        this.usedEntities.push(entity)
                    }
                })

                this.getRelations()
            }
        },
        loadQuery() {
            this.query = this.selectedQuery as iQuery
        },
        loadEntities() {
            this.entities = this.propEntities as any
        },
        getRelations() {
            this.relations = []
            this.usedEntities?.forEach((usedEntity: any) => {
                usedEntity.relation?.forEach((relation: any) => {
                    const index = this.usedEntities.findIndex((targetUsedEntity: any) => targetUsedEntity.id === relation.targetEntity)
                    if (index !== -1) this.relations.push({ ...relation })
                })
            })
            this.filteredRelations = [...this.relations]
        },
        async loadEntityNames() {
            if (this.id && this.query) {
                const postData = { catalogue: this.qbe?.qbeJSONQuery.catalogue.queries, meta: this.formatQbeMeta(), pars: this.qbe?.pars, qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
                await this.$http
                    .post(`/knowageqbeengine/restful-services/qbequery/queryEntities/?SBI_EXECUTION_ID=${this.id}&currentQueryId=${this.query?.id}`, postData)
                    .then((response: AxiosResponse<any>) => (this.entityNames = response.data))
                    .catch(() => {})
            }
        },
        formatQbeMeta() {
            const meta = [] as any[]
            this.qbe?.qbeJSONQuery.catalogue.queries?.forEach((query: iQuery) => {
                query.fields?.forEach((field: iField) => {
                    meta.push({ dataType: field.dataType, displayedName: field.alias, fieldType: field.fieldType.toUpperCase(), format: field.format, name: field.alias, type: field.type })
                })
            })
            return meta
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredRelations = [...this.relations]
                } else {
                    this.filteredRelations = this.relations.filter((relation: any) => {
                        return relation.attributes.entity.toLowerCase().includes(this.searchWord.trim().toLocaleLowerCase()) || relation.attributes.sourceEntity.toLowerCase().includes(this.searchWord.trim().toLocaleLowerCase())
                    })
                }
            }, 250)
        },
        moveDown(index: number) {
            const temp = this.relations[index]
            this.relations[index] = this.relations[index + 1]
            this.relations[index + 1] = temp
            this.filteredRelations = [...this.relations]
        },
        moveUp(index: number) {
            const temp = this.relations[index]
            this.relations[index] = this.relations[index - 1]
            this.relations[index - 1] = temp
            this.filteredRelations = [...this.relations]
        },
        closeDialog() {
            this.$emit('close')
            this.relations = []
            this.filteredRelations = []
        },
        save() {
            this.query.graph = [...this.relations]

            this.relations.forEach((relation: any) => {
                this.entities?.forEach((entity: any) => {
                    for (let i = 0; i < entity.relation.length; i++) {
                        const tempRelation = entity.relation[i]
                        if (tempRelation.id === relation.id) {
                            entity.relation[i] = { ...relation }
                        }
                    }
                })
            })

            this.relations = []
            this.filteredRelations = []
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#qbe-join-definition-dialog .p-dialog-header,
#qbe-join-definition-dialog .p-dialog-content {
    padding: 0;
}
#qbe-join-definition-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.qbe-advanced-filter-button {
    max-width: 150px;
}
</style>
