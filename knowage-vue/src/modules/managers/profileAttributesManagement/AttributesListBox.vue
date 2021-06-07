<template>
    <Listbox
        v-if="!load"
        class="kn-list--column"
        :options="attributes"
        :filter="true"
        :filterPlaceholder="$t('common.search')"
        optionLabel="userId"
        filterMatchMode="contains"
        :filterFields="profileAttributesManagementDescriptor.globalFilterFields"
        :emptyFilterMessage="$t('managers.widgetGallery.noResults')"
        @change="onAttributeSelect"
        data-test="attributes-list"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.attributeName }}</span>
                    <span class="kn-list-item-text-secondary">{{ slotProps.option.attributeDescription }}</span>
                </div>
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteAttribute(slotProps.option.attributeId)" :data-test="'delete-button'" />
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
    emits: ['selectedAttribute', 'deleteAttribute'],
    props: {
        attributes: Object,
        loading: Boolean
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
    data() {
        return {
            load: false as Boolean,
            listAttributes: [] as iAttribute[],
            selectedAttribute: null as iAttribute | null,
            profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor
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
