<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('workspace.federationDefinition.associationsList') }}
                </template>
                <template #end>
                    <KnFabButton icon="fas fa-plus" @click="createAssociation"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div :style="workspaceFederationDatasetListDescriptor.styles.assContainer">
                <Listbox id="associations-list" :options="associations" :list-style="workspaceFederationDatasetListDescriptor.styles.maxHeight">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item p-d-flex p-flex-row">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.relationship }}</span>
                            </div>
                            <i class="fas fa-trash-alt delete-association-icon" @click.stop="deleteAssociationConfirm(slotProps.option)"></i>
                        </div>
                    </template>
                </Listbox>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Card from 'primevue/card'
    import KnFabButton from '@/components/UI/KnFabButton.vue'
    import Listbox from 'primevue/listbox'
    import workspaceFederationDatasetListDescriptor from './WorkspaceFederationDatasetListDescriptor.json'

    export default defineComponent({
        name: 'workspace-federation-definition-associations-list',
        components: { Card, KnFabButton, Listbox },
        props: { propAssociations: { type: Array } },
        emits: ['createAssociationClick'],
        data() {
            return {
                workspaceFederationDatasetListDescriptor,
                associations: [] as any[]
            }
        },
        watch: {
            propAssociations() {
                this.loadAssociations()
            }
        },
        created() {
            this.loadAssociations()
        },
        methods: {
            loadAssociations() {
                this.associations = this.propAssociations as any[]
            },
            createAssociation() {
                this.$emit('createAssociationClick')
            },
            deleteAssociationConfirm(association: any) {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteAssociation(association)
                })
            },
            deleteAssociation(association: any) {
                const index = this.associations.findIndex((el: any) => el.relationship === association.relationship)
                if (index !== -1) this.associations.splice(index, 1)
            }
        }
    })
</script>

<style lang="scss" scoped>
    #associations-list {
        border: none;
    }

    .delete-association-icon {
        margin-left: auto;
    }
</style>
