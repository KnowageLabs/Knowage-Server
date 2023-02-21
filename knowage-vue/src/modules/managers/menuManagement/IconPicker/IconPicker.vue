<template>
    <Dialog v-model:visible="modalShown" :header="$t('managers.menuManagement.chooseIcon')" :style="{ width: '50vw' }" :modal="true" :closable="false">
        <KnImageToBase64IconPicker @selectedImageBase64="onBase64ImageSelection" @wrongInput="toggleDisableChooseButton"></KnImageToBase64IconPicker>

        <div id="iconPicker">
            <div class="p-mt-2 p-field">
                <div class="p-inputgroup">
                    <span class="p-float-label">
                        <InputText id="searchIcon" type="text" class="p-inputtext p-component kn-material-input" @keyup="filterIcons($event)" />
                        <label for="searchIcon">{{ $t('common.search') }}</label>
                    </span>
                </div>
            </div>

            <div class="p-mt-4">
                <div class="iconPicker__icons">
                    <p>fontawesome</p>
                    <a v-for="icon in icons" :key="icon.value" href="#" :class="`item ${selected === icon.name ? 'selected' : ''}`" @click.stop.prevent="getIcon(icon)">
                        <i :class="'fas fa-' + icon.name"></i>
                    </a>
                </div>
            </div>
        </div>
        <template #footer>
            <Button label="Cancel" icon="pi pi-times" class="p-button-text" @click="closeModal" />
            <Button label="Choose" icon="pi pi-check" :disabled="disableChoosen" autofocus @click="chooseIcon" />
        </template>
    </Dialog>
</template>

<script>
import KnImageToBase64IconPicker from '@/components/UI/KnImageToBase64IconPicker.vue'
import Dialog from 'primevue/dialog'
import icons from './icons'
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'icon-picker',
    components: { Dialog, KnImageToBase64IconPicker },
    props: ['showModal'],
    emits: ['chooseIcon', 'closeFontAwesomeModal'],
    data() {
        return {
            modalShown: false,
            disableChoosen: false,
            selected: '',
            chosenIcon: {},
            icons
        }
    },
    watch: {
        showModal: {
            handler: function(show) {
                this.modalShown = show
            }
        }
    },
    methods: {
        getIcon(icon) {
            this.selected = icon.name
            this.chosenIcon = icon
            this.disableChoosen = false
        },
        chooseIcon() {
            if (this.chosenIcon) {
                this.$emit('chooseIcon', this.chosenIcon)
            }
        },
        closeModal() {
            this.$emit('closeFontAwesomeModal')
        },
        filterIcons(event) {
            const search = event.target.value.trim()
            let filter = []
            if (search.length > 3) {
                filter = icons.filter((item) => {
                    const regex = new RegExp(search, 'gi')
                    return item.name.match(regex)
                })
            } else if (search.length === 0) {
                this.icons = icons
            }
            if (filter.length > 0) {
                this.icons = filter
            }
        },
        toggleDisableChooseButton(value) {
            this.disableChoosen = value
        },
        onBase64ImageSelection(image) {
            this.choosenIcon = image
            this.$emit('chooseIcon', image)
        }
    }
})
</script>

<style>
#iconPicker {
    position: relative;
    max-width: 100%;
}
.iconPicker__header {
    padding: 1em;
    border-radius: 8px 8px 0 0;
    border: 1px solid #ccc;
}
.iconPicker__header input {
    width: 100%;
    padding: 1em;
}
.iconPicker__body {
    position: relative;
    max-height: 250px;
    overflow: auto;
    padding: 1em 0 1em 1em;
    border-radius: 0 0 8px 8px;
    border: 1px solid #ccc;
}
.iconPicker__icons {
    display: table;
}
.iconPicker__icons .item {
    float: left;
    width: 40px;
    height: 40px;
    padding: 12px;
    margin: 0 12px 12px 0;
    text-align: center;
    border-radius: 3px;
    font-size: 14px;
    box-shadow: 0 0 0 1px #ddd;
    color: inherit;
}
.iconPicker__icons .item.selected {
    background: #ccc;
}
.iconPicker__icons .item i {
    box-sizing: content-box;
}
</style>
