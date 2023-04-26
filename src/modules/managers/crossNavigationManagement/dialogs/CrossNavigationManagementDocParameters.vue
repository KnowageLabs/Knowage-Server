<template>
    <Message class="p-col-12 p-mb-3">{{ $t('managers.crossNavigationManagement.hintDrag') }}</Message>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('managers.crossNavigationManagement.availableIO') }}
            </template>
        </Toolbar>
        <div v-if="navigation.fromPars" class="p-inputgroup p-mt-3">
            <span class="p-float-label">
                <InputText v-model.trim="fixedValue" class="kn-material-input" type="text" maxlength="100" />
                <label class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.fixedValue') }} </label>
            </span>
            <Button :label="$t('common.add')" class="kn-button p-button-text" @click="addFixedValue" />
        </div>
        <Listbox v-if="navigation.fromPars" :options="navigation.fromPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex card p-p-3 kn-draggable" draggable="true" @dragstart="onDragStart($event, slotProps.option)">
                    <i class="pi pi-bars p-mr-2"> </i>
                    <div>{{ slotProps.option.name }}</div>
                    <div class="p-ml-auto">{{ $t(dialogDescriptor.parType[slotProps.option.type].label) }}</div>
                </div>
            </template>
        </Listbox>
    </div>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('managers.crossNavigationManagement.availableInput') }}
            </template>
        </Toolbar>
        <Listbox v-if="navigation.toPars" :options="navigation.toPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div v-if="slotProps.option.links && slotProps.option.links.length > 0" class="p-d-flex p-p-3 card">
                    <div>{{ slotProps.option.links[0].name }} <i class="fa fa-link"> </i> {{ slotProps.option.name }}</div>
                    <i class="fa fa-times-circle p-mr-2 p-ml-auto" data-test="remove" @click="removeLink(slotProps.option.id)"> </i>
                </div>
                <div
                    v-else
                    class="p-d-flex p-p-3 card"
                    :class="{ dropzone: dropzoneActive[slotProps.option.id] }"
                    @drop="link($event, slotProps.option)"
                    @dragenter.prevent
                    @dragleave.prevent="setDropzoneClass(false, slotProps.option.id)"
                    @dragover.prevent="setDropzoneClass(true, slotProps.option.id)"
                >
                    <div>{{ slotProps.option.name }}</div>
                    <div class="p-ml-auto">{{ $t(dialogDescriptor.parType[slotProps.option.type].label) }}</div>
                </div>
            </template>
        </Listbox>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import dialogDescriptor from './CrossNavigationManagementDialogDescriptor.json'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'cross-navigation-detail',
    components: { Listbox, Message },
    props: {
        selectedNavigation: {
            type: Object
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            navigation: {} as any,
            dialogDescriptor,
            fixedValue: '',
            dropzoneActive: [] as boolean[]
        }
    },
    watch: {
        selectedNavigation() {
            if (this.selectedNavigation) {
                this.navigation = this.selectedNavigation
                this.dropzoneActive = []
            }
        }
    },
    created() {
        if (this.selectedNavigation) {
            this.navigation = this.selectedNavigation
            this.dropzoneActive = []
        }
    },
    methods: {
        addFixedValue() {
            if (this.fixedValue != '') {
                if (!this.navigation.fromPars) this.navigation.fromPars = []
                this.navigation.fromPars.push({ id: this.navigation.simpleNavigation.fromDocId, name: this.fixedValue, type: 2, fixedValue: this.fixedValue })
                this.fixedValue = ''
            }
        },
        onDragStart(event: any, param: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(param))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        link(event: any, item: any) {
            const param = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (param.type === 2 || param.parType === item.parType) {
                item.links = [param]
                this.$emit('touched')
            } else {
                this.store.setInfo({
                    title: this.$t('managers.crossNavigationManagement.incompatibleTypes'),
                    msg: this.$t('managers.crossNavigationManagement.incompatibleTypesMessage', { originParam: param.name, targetParam: item.name })
                })
            }
        },
        removeLink(id) {
            this.navigation.toPars.forEach((param) => {
                if (param.id === id) {
                    param.links = []
                    this.$emit('touched')
                    this.setDropzoneClass(false, id)
                }
            })
        },
        setDropzoneClass(value: boolean, paramId: any) {
            if (paramId) {
                this.dropzoneActive[paramId] = value
            }
        }
    }
})
</script>
<style lang="scss" scoped>
::v-deep(.p-listbox) {
    .p-listbox-item {
        padding: 0;
    }
}
.dropzone {
    background-color: #c2c2c2;
    color: white;
    border: 1px dashed;
}
</style>
