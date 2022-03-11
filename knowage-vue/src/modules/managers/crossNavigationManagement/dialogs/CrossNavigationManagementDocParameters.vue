<template>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('managers.crossNavigationManagement.availableIO') }}
            </template>
        </Toolbar>
        <div class="p-inputgroup p-mt-3" v-if="navigation.fromPars">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model.trim="fixedValue" maxlength="100" />
                <label class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.fixedValue') }} </label>
            </span>
            <Button :label="$t('common.add')" @click="addFixedValue" class="kn-button p-button-text" />
        </div>
        <Listbox :options="navigation.fromPars" v-if="navigation.fromPars">
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
        <Listbox :options="navigation.toPars" v-if="navigation.toPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex p-p-3 card" v-if="slotProps.option.links && slotProps.option.links.length > 0">
                    <div>{{ slotProps.option.links[0].name }} <i class="fa fa-link"> </i> {{ slotProps.option.name }}</div>
                    <i class="fa fa-times-circle p-mr-2 p-ml-auto" @click="removeLink(slotProps.option.id)" data-test="remove"> </i>
                </div>
                <div
                    class="p-d-flex p-p-3 card"
                    :class="{ dropzone: dropzoneActive[slotProps.option.id] }"
                    @drop="link($event, slotProps.option)"
                    @dragenter.prevent
                    @dragleave.prevent="setDropzoneClass(false, slotProps.option.id)"
                    @dragover.prevent="setDropzoneClass(true, slotProps.option.id)"
                    v-else
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
    import dialogDescriptor from './CrossNavigationManagementDialogDescriptor.json'
    export default defineComponent({
        name: 'cross-navigation-detail',
        components: { Listbox },
        props: {
            selectedNavigation: {
                type: Object
            }
        },
        data() {
            return {
                navigation: {} as any,
                dialogDescriptor,
                fixedValue: '',
                dropzoneActive: [] as boolean[]
            }
        },
        created() {
            if (this.selectedNavigation) {
                this.navigation = this.selectedNavigation
                this.dropzoneActive = []
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
                    this.$store.commit('setInfo', {
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
