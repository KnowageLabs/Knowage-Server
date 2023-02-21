<template>
    <Dialog id="metaweb-attribute-detail-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="metawebAttributeDetailDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('metaweb.businessModel.attributesDetail.title') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />

        <div v-if="attribute" ref="attribute-form" class="p-mt-4 p-mx-4 kn-flex-0">
            <div class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-md-12">
                    <span class="p-float-label">
                        <InputText id="name" v-model.trim="attribute.name" class="kn-material-input" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }}</label>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-12">
                    <span class="p-float-label">
                        <InputText id="name" v-model.trim="attribute.description" class="kn-material-input" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-12">
                    <span class="p-float-label">
                        <Dropdown v-model="columnType" class="kn-material-input" :options="metawebAttributeDetailDialogDescriptor.typeOptions" @change="onTypeChange()" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }}</label>
                    </span>
                </div>
            </div>

            <label class="kn-material-input-label">{{ $t('metaweb.businessModel.attributesDetail.structural') }}</label>

            <div class="p-fluid p-formgrid p-grid p-mt-4">
                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.aggtype'].value" class="kn-material-input" :options="properties['structural.aggtype'].propertyType.admissibleValues" @change="updateAttribute('structural.aggtyp')" />
                        <label class="kn-material-input-label"> {{ properties['structural.aggtype'].propertyType.name }}</label>
                        <small>{{ properties['structural.aggtype'].propertyType.description }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.datatype'].value" class="kn-material-input" :options="properties['structural.datatype'].propertyType.admissibleValues" @change="updateAttribute('structural.datatype')" />
                        <label class="kn-material-input-label"> {{ properties['structural.datatype'].propertyType.name }}</label>
                        <small>{{ properties['structural.datatype'].propertyType.description }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <MultiSelect v-model="properties['behavioural.notEnabledRoles'].value" class="kn-material-input" :options="roleOptions" option-label="name" option-value="name" :filter="true" @change="updateAttribute('behavioural.notEnabledRoles')" />
                        <label class="kn-material-input-label"> {{ properties['behavioural.notEnabledRoles'].propertyType.name }}</label>
                        <small>{{ properties['behavioural.notEnabledRoles'].propertyType.description }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.attribute'].value" class="kn-material-input" :options="profileAttributes" @change="updateAttribute('structural.attribute')" />
                        <label class="kn-material-input-label"> {{ properties['structural.attribute'].propertyType.name }}</label>
                        <small>{{ properties['structural.attribute'].propertyType.description }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <InputText v-model.trim="properties['physical.physicaltable'].value" class="kn-material-input" :disabled="true" />
                        <label class="kn-material-input-label"> {{ properties['physical.physicaltable'].propertyType.name }}</label>
                        <small>{{ properties['physical.physicaltable'].propertyType.description }}</small>
                    </span>
                </div>

                <div v-if="attribute.physicalColumn" class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <InputText id="physicalColumn" v-model.trim="attribute.physicalColumn.name" class="kn-material-input" :disabled="true" />
                        <label for="physicalColumn" class="kn-material-input-label">{{ $t('metaweb.businessModel.physicalColumn') }}</label>
                        <small>{{ $t('metaweb.businessModel.physicalColumnHint') }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.filtercondition'].value" class="kn-material-input" :options="properties['structural.filtercondition'].propertyType.admissibleValues" @change="updateAttribute('structural.filtercondition')" />
                        <label class="kn-material-input-label"> {{ properties['structural.filtercondition'].propertyType.name }}</label>
                        <small>{{ properties['structural.filtercondition'].propertyType.description }}</small>
                    </span>
                </div>

                <div v-if="properties['structural.datatype'].value === 'DATE' || properties['structural.datatype'].value === 'TIMESTAMP' || properties['structural.datatype'].value === 'TIME'" class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.dateformat'].value" class="kn-material-input" :options="properties['structural.dateformat'].propertyType.admissibleValues" @change="updateAttribute('structural.dateformat')">
                            <template #option="slotProps">
                                <span>{{ getFormattedDate(new Date(), slotProps.option) }}</span>
                            </template>
                            <template #value="slotProps">
                                <span>{{ getFormattedDate(new Date(), slotProps.value) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ properties['structural.dateformat'].propertyType.name }}</label>
                        <small>{{ properties['structural.datatype'].value === 'TIME' ? properties['structural.format'].propertyType.description : properties['structural.dateformat'].propertyType.description }}</small>
                    </span>
                </div>

                <div v-if="['INTEGER', 'DOUBLE', 'DECIMAL', 'BIGINT', 'FLOAT'].indexOf(properties['structural.datatype'].value) !== -1" class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <Dropdown v-model="properties['structural.format'].value" class="kn-material-input" :options="properties['structural.format'].propertyType.admissibleValues" @change="updateAttribute('structural.format')" />
                        <label class="kn-material-input-label"> {{ properties['structural.format'].propertyType.name }}</label>
                        <small>{{ properties['structural.format'].propertyType.description }}</small>
                    </span>
                </div>

                <div class="p-field p-col-12 p-md-6 p-mt-2">
                    <span class="p-float-label">
                        <InputText v-model.trim="properties['structural.customFunction'].value" class="kn-material-input" @change="updateAttribute('structural.customFunction')" />
                        <label class="kn-material-input-label"> {{ properties['structural.customFunction'].propertyType.name }}</label>
                        <small>{{ properties['structural.customFunction'].propertyType.description }}</small>
                    </span>
                </div>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="saveAttribute"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModelColumn } from '../../../../../Metaweb'
import { formatDate } from '@/helpers/commons/localeHelper'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import metawebAttributeDetailDialogDescriptor from './MetawebAttributeDetailDialogDescriptor.json'
import mainStore from '../../../../../../../../../App.store'

export default defineComponent({
    name: 'metaweb-attribute-detail-dialog',
    components: { Dialog, Dropdown, MultiSelect },
    props: { visible: { type: Boolean }, selectedAttribute: { type: Object as PropType<iBusinessModelColumn> }, roles: { type: Array } },
    emits: ['close', 'save'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            metawebAttributeDetailDialogDescriptor,
            attribute: null as iBusinessModelColumn | null,
            properties: {} as any,
            roleOptions: [] as any[],
            columnType: '' as string,
            loading: false
        }
    },
    computed: {
        profileAttributes(): any[] {
            return (this.store.$state as any).user.attributes ? Object.keys((this.store.$state as any).user.attributes) : []
        }
    },
    watch: {
        selectedAttribute() {
            this.loadAttribute()
        },
        roles() {
            this.loadRoleOptions()
        },
        visible() {
            this.loadAttribute()
            this.loadRoleOptions()
        }
    },
    created() {
        this.loadAttribute()
        this.loadRoleOptions()
    },
    methods: {
        loadAttribute() {
            if (this.selectedAttribute) {
                this.attribute = { ...this.selectedAttribute, physicalColumn: { ...this.selectedAttribute.physicalColumn }, properties: this.getDeepCopyProperties(this.selectedAttribute.properties) } as iBusinessModelColumn
            }

            this.getAttributeType()
            this.loadAttributeProperties()
        },
        loadRoleOptions() {
            this.roleOptions = this.roles as any[]
        },
        getAttributeType() {
            if (this.attribute) {
                for (let i = 0; i < this.attribute.properties.length; i++) {
                    const tempProperty = this.attribute.properties[i]
                    const key = Object.keys(tempProperty)[0]
                    if (key === 'structural.columntype') {
                        this.columnType = tempProperty[key].value
                    }
                }
            }
        },
        getDeepCopyProperties(properties: any[]) {
            const newProperties = [] as any[]
            properties.forEach((property: any) => {
                const key = Object.keys(property)[0]
                const tempProperty = {}
                tempProperty[key] = { ...property[key] }
                newProperties.push(tempProperty)
            })

            return newProperties
        },
        loadAttributeProperties() {
            if (this.attribute) {
                this.attribute.properties?.forEach((property: any) => {
                    const key = Object.keys(property)[0]
                    this.properties[key] = property[key]
                })
            }

            if (this.properties['behavioural.notEnabledRoles']?.value && typeof this.properties['behavioural.notEnabledRoles'].value === 'string') {
                this.properties['behavioural.notEnabledRoles'].value = this.properties['behavioural.notEnabledRoles'].value?.split(';')
            }
        },
        onTypeChange() {
            if (this.attribute) {
                for (let i = 0; i < this.attribute.properties.length; i++) {
                    const tempProperty = this.attribute.properties[i]
                    const key = Object.keys(tempProperty)[0]
                    if (key === 'structural.columntype') {
                        tempProperty[key].value = this.columnType
                    }
                }
            }
        },
        updateAttribute(propertyKey: string) {
            if (this.attribute) {
                for (let i = 0; i < this.attribute.properties.length; i++) {
                    const property = this.attribute.properties[i]
                    const key = Object.keys(property)[0]
                    if (key === propertyKey) {
                        property[key].value = this.properties[key].value
                        break
                    }
                }
            }
        },
        closeDialog() {
            this.$emit('close')
        },
        saveAttribute() {
            if (this.attribute) {
                for (let i = 0; i < this.attribute.properties.length; i++) {
                    const property = this.attribute.properties[i]
                    const key = Object.keys(property)[0]
                    if (key === 'behavioural.notEnabledRoles' && Array.isArray(property[key].value)) {
                        property[key].value = property[key].value?.join(';')
                        break
                    }
                }
            }

            this.$emit('save', this.attribute)
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        }
    }
})
</script>

<style lang="scss">
#metaweb-attribute-detail-dialog .p-dialog-header,
#metaweb-attribute-detail-dialog .p-dialog-content {
    padding: 0;
}

#metaweb-attribute-detail-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
