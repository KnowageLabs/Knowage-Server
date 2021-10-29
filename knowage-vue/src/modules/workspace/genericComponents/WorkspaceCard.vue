<template>
    <div class="card-container p-col-12 p-md-6 p-lg-4 p-xl-3" :style="cardDescriptor.style.cardContainer">
        <div v-if="document[documentFields.image]" class="card-image" :style="[documentImageSource, cardDescriptor.style.cardImage]" />
        <!-- TODO: insert the default image source -->
        <div v-else class="card-image default" :style="[documentImageSource, cardDescriptor.style.cardImage]" />
        <!--  -->
        <span class="details-container" :style="cardDescriptor.style.detailsContainer">
            <div class="type-container" :style="cardDescriptor.style.typeContainer">
                <p class="p-mb-1">{{ document[documentFields.type] }}</p>
            </div>
            <div class="name-container" :style="cardDescriptor.style.nameContainer">
                <h4 class="p-m-0" v-tooltip="document[documentFields.label]">
                    <b>{{ document[documentFields.label] }}</b>
                </h4>
                <p class="p-m-0">{{ document[documentFields.name] }}</p>
            </div>
            <div class="button-container" :style="cardDescriptor.style.buttonContainer">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button class="p-mx-1" v-if="button.visible" :icon="button.icon" :class="button.class" :style="cardDescriptor.style.icon" @click="button.command" />
                </span>
            </div>
        </span>
    </div>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import cardDescriptor from './WorkspaceCardDescriptor.json'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    name: 'workspace-sidebar',
    components: { Menu },
    emits: [
        'executeRecent',
        'executeDocumentFromOrganizer',
        'moveDocumentToFolder',
        'deleteDocumentFromOrganizer',
        'executeAnalysisDocument',
        'editAnalysisDocument',
        'shareAnalysisDocument',
        'cloneAnalysisDocument',
        'deleteAnalysisDocument',
        'uploadAnalysisPreviewFile',
        'openDatasetInQBE',
        'editDataset',
        'deleteDataset',
        'openSidebar'
    ],
    props: { visible: Boolean, viewType: String, document: Object as any },
    computed: {
        isOwner(): any {
            return (this.$store.state as any).user.fullName === this.document.creationUser
        },
        documentImageSource(): any {
            if (this.document[this.documentFields.image]) {
                return {
                    //2nd image is the fallback in case there is an error iwth source image --- url(imgSource)url(fallbackImg)
                    'background-image': `url(${process.env.VUE_APP_HOST_URL}${descriptor.imgPath}${this.document[this.documentFields.image]}),url(https://www.hebergementwebs.com/image/72/722fd28e2eabfe2f8a1e5b2c32d553f8.jpg/error-0x80070005-the-best-approaches.jpg)`
                }
            }
            return {
                //default image if none is uploaded for the selected document
                'background-image': `url(${process.env.VUE_APP_HOST_URL}${descriptor.imgPath}82300081364511eca64e159ee59cd4dc.jpg)`
            }
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return cardDescriptor.defaultViewFields
                case 'repository':
                    return cardDescriptor.defaultViewFields
                case 'analysis':
                    return cardDescriptor.analysisViewFields
                case 'businessModel':
                    return cardDescriptor.businessModelViewFields
                case 'federationDataset':
                    return cardDescriptor.federationDatasetViewFields
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [
                        { icon: 'fas fa-info-circle', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeRecent') }
                    ]
                case 'repository':
                    return [
                        { icon: 'fas fa-trash', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('deleteDocumentFromOrganizer') },
                        { icon: 'fas fa-share', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('moveDocumentToFolder') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeDocumentFromOrganizer') }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', visible: this.isOwner, command: this.emitEvent('editAnalysisDocument') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeAnalysisDocument') }
                    ]
                case 'businessModel':
                    return [{ icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('openDatasetInQBE') }]
                case 'federationDataset':
                    return [
                        { icon: 'fas fa-trash-alt', class: 'p-button-text p-button-rounded p-button-plain', visible: (this.$store.state as any).user.isSuperadmin || (this.$store.state as any).user.userId === this.document.owner, command: this.emitEvent('deleteDataset') },
                        { icon: 'pi pi-pencil', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('editDataset') },
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('openDatasetInQBE') }
                    ]
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        menuButtons(): any {
            return [
                {
                    key: '0',
                    label: this.$t('workspace.myAnalysis.menuItems.share'),
                    icon: 'fas fa-share',
                    command: () => {
                        this.emitEvent('shareAnalysisDocument')
                    }
                },
                {
                    key: '1',
                    label: this.$t('workspace.myAnalysis.menuItems.clone'),
                    icon: 'fas fa-clone',
                    command: () => {
                        this.emitEvent('cloneAnalysisDocument')
                    }
                },
                {
                    key: '2',
                    label: this.$t('workspace.myAnalysis.menuItems.delete'),
                    icon: 'fas fa-trash',
                    command: () => {
                        this.emitEvent('deleteAnalysisDocument')
                    }
                },
                {
                    key: '3',
                    label: this.$t('workspace.myAnalysis.menuItems.upload'),
                    icon: 'fas fa-share-alt',
                    command: () => {
                        this.emitEvent('uploadAnalysisPreviewFile')
                    }
                }
            ]
        }
    },

    data() {
        return {
            cardDescriptor,
            sidebarVisible: false
        }
    },
    created() {},
    watch: {
        visible() {}
    },
    methods: {
        returnDefaultImage() {
            return process.env.VUE_APP_HOST_URL + descriptor.imgPath + `82300081364511eca64e159ee59cd4dc.jpg`
        },
        showMenu(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        emitEvent(event) {
            return () => this.$emit(event, this.document)
        }
    }
})
</script>
<style lang="scss" scoped>
@media screen and (max-width: 576px) {
    .card-image {
        display: none;
    }
    .details-container {
        border-radius: 10px;
    }
}
</style>
