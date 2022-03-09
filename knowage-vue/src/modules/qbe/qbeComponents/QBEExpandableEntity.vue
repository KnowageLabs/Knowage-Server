<template>
    <div class="expandable-entities" v-for="(entity, index) in entities" :key="index">
        <h4 class="entity-item-container" :style="{ 'border-left': `10px solid ${entity.color}` }" draggable="true" @dragstart="onDragStart($event, entity)" :data-test="'entity-container-' + entity.id">
            <i :class="getIconCls(entity.attributes.iconCls)" class="p-mx-2" v-tooltip.top="$t(`qbe.entities.types.${entity.attributes.iconCls}`)" />
            <span class="kn-flex" @click="expandEntity(entity)" :data-test="'expand-' + entity.id">{{ entity.text }}</span>
            <Button icon="fas fa-info" class="p-button-text p-button-rounded p-button-plain " v-tooltip.top="$t('qbe.entities.relations')" @click="$emit('showRelationDialog', entity)" />
            <Button v-if="entity.expanded" icon="pi pi-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = false" />
            <Button v-else icon="pi pi-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = true" />
        </h4>
        <ul v-show="entity.expanded">
            <li :style="{ 'border-left': `5px solid ${child.color}` }" v-for="(child, index) in entity.children" :key="index" draggable="true" @dragstart="onDragStart($event, child)">
                <i :class="getIconCls(child.attributes.iconCls)" class="p-mx-2" v-tooltip.top="$t(`qbe.entities.types.${child.attributes.iconCls}`)" />
                <span @click="$emit('entityChildClicked', child)" :data-test="'entity-' + entity.id">{{ child.text }}</span>
                <Button icon="fas fa-filter" :class="{ 'qbe-active-filter-icon': fieldHasFilters(child) }" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click="openFiltersDialog(child)" :data-test="'child-' + child.id" />
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery } from '../QBE'

export default defineComponent({
    name: 'expandable-entity',
    components: {},
    props: { availableEntities: { type: Array }, query: { type: Object as PropType<iQuery>, required: true } },
    emits: ['close', 'showRelationDialog', 'openFilterDialog', 'entityChildClicked'],
    data() {
        return {
            entities: [] as any,
            colors: ['#D7263D', '#F46036', '#2E294E', '#1B998B', '#C5D86D', '#3F51B5', '#8BC34A', '#009688', '#F44336']
        }
    },
    watch: {
        availableEntities() {
            this.entities = this.availableEntities
            this.setupEntities()
        }
    },
    created() {
        this.entities = this.availableEntities
        this.setupEntities()
    },
    methods: {
        expandEntity(entity) {
            entity.expanded = !entity.expanded
        },
        setupEntities() {
            let usedColorIndex = 0
            this.entities?.forEach((entity) => {
                if (!this.colors[usedColorIndex]) usedColorIndex = 0
                var color = this.colors[usedColorIndex]
                usedColorIndex++
                entity.color = color
                if (entity.children) {
                    entity.children.forEach((child) => {
                        child.color = color
                    })
                }
            })
        },

        getIconCls(iconCls) {
            switch (iconCls) {
                case 'measure':
                    return 'fas fa-ruler'
                case 'cube':
                    return 'fas fa-cube'
                case 'calculation':
                    return 'fas fa-calculator'
                case 'dimension':
                    return 'fas fa-ruler-horizontal'
                case 'geographic dimension':
                    return 'fas fa-map-marked-alt'
                case 'attribute':
                    return 'fas fa-font'
                case 'generic':
                    return 'fas fa-layer-group'
                case 'geographic_dimension':
                    return 'fas fa-globe'
                default:
                    return 'fas fa-cube'
            }
        },
        onDragStart(event, entity) {
            event.dataTransfer.setData('text', JSON.stringify(entity))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        openFiltersDialog(field: any) {
            this.$emit('openFilterDialog', field)
        },
        fieldHasFilters(field: any) {
            for (let i = 0; i < this.query.filters?.length; i++) {
                const tempFilter = this.query.filters[i]
                if (tempFilter.leftOperandValue === field.id) {
                    return true
                }
            }
            return false
        }
    }
})
</script>
<style lang="scss">
.expandable-entities {
    ul {
        background-color: #eceff1;
        margin: 0;
        list-style: none;
        padding-left: 0;
        cursor: pointer;
        li {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: flex-start;
            padding: 4px 0px 4px 20px;
            font-size: 0.8rem;
            border-bottom: 1px solid #cccccc;
            height: 24px;
            cursor: grab;
            button {
                margin: 0;
                padding: 0;
                width: 32px;
            }
            &:hover {
                background-color: darken(#eceff1, 10%);
            }
            i {
                cursor: help;
            }
            span {
                padding-left: 5px;
            }
        }
    }
    h4 {
        display: flex;
        background-color: #fff;
        flex-direction: row;
        align-items: center;
        justify-content: flex-start;
        height: 24px;
        line-height: 24px;
        margin: 0;
        padding: 4px 8px 4px 8px;
        font-size: 0.8rem;
        border-bottom: 1px solid #ccc;
        outline: none;
        cursor: grab;
        &:hover {
            background-color: darken(#ffffff, 15%);
        }
        button {
            cursor: pointer;
        }
        i {
            cursor: help;
        }
    }
}

.qbe-active-filter-icon {
    color: red !important;
}
</style>
