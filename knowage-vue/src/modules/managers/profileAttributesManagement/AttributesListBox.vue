<template>
    <Listbox
        v-if="!load"
        class="kn-list--column"
        :options="attributes"
        :filter="true"
        :filter-placeholder="$t('common.search')"
        option-label="userId"
        filter-match-mode="contains"
        :filter-fields="profileAttributesManagementDescriptor.globalFilterFields"
        :empty-filter-message="$t('managers.widgetGallery.noResults')"
        data-test="attributes-list"
        @change="onAttributeSelect"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.attributeName }}</span>
                    <span class="kn-list-item-text-secondary">{{ slotProps.option.attributeDescription }}</span>
                </div>
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" :data-test="'delete-button'" @click="deleteAttribute(slotProps.option.attributeId)" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import ProfileAttributesManagementDescriptor from './ProfileAttributesManagementDescriptor.json'
import { iAttribute } from './ProfileAttributesManagement'

export default defineComponent({
    name: 'profile-attributes-list-box',
    components: {
        Listbox
    },
    props: {
        attributes: Object,
        loading: Boolean
    },
    emits: ['selectedAttribute', 'deleteAttribute'],
    data() {
        return {
            load: false as boolean,
            listAttributes: [] as iAttribute[],
            selectedAttribute: null as iAttribute | null,
            profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor
        }
    },
    watch: {
        attributes: {
            handler: function(attr) {
                this.listAttributes = attr
            }
        },
        loading: {
            handler: function(l) {
                this.load = l
            }
        }
    },
    methods: {
        deleteAttribute(attributeId: number) {
            this.$emit('deleteAttribute', attributeId)
        },
        onAttributeSelect(event: any) {
            this.$emit('selectedAttribute', event.value)
        }
    }
})
</script>
