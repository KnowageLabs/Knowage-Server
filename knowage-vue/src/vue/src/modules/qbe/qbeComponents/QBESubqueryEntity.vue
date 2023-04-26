<template>
    <div v-for="(entity, index) in entities" :key="index" class="expandable-entities">
        <h4 draggable="true" @dragstart="onDragStart($event, entity)">
            <i v-tooltip.top="$t(`qbe.entities.types.cube`)" class="fas fa-cube p-mx-2" />
            <span class="kn-flex" @click="expandEntity(entity)">{{ entity.name }}</span>
            <Button icon="fas fa-edit" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click="$emit('editSubquery', entity)" />
            <Button icon="fas fa-trash" class="p-button-text p-button-rounded p-button-plain" @click="$emit('deleteSubquery', index, entity)" />
            <Button v-if="entity.expanded" icon="pi pi-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = false" />
            <Button v-else icon="pi pi-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = true" />
        </h4>
        <ul v-show="entity.expanded">
            <li v-for="(child, index) in entity.fields" :key="index" draggable="true" @dragstart="onDragStart($event, child)">
                <i v-tooltip.top="$t(`qbe.entities.types.${child.iconCls}`)" :class="getIconCls(child.iconCls)" class="p-mx-2" />
                <span>{{ child.alias }}</span>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'expandable-entity',
    components: {},
    props: { availableEntities: { type: Array } },
    emits: ['close'],
    data() {
        return {
            entities: [] as any,
            colors: ['#D7263D', '#F46036', '#2E294E', '#1B998B', '#C5D86D', '#3F51B5', '#8BC34A', '#009688', '#F44336']
        }
    },
    watch: {
        availableEntities() {
            this.entities = this.availableEntities
        }
    },
    created() {
        this.entities = this.availableEntities
    },
    methods: {
        expandEntity(entity) {
            entity.expanded = !entity.expanded
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
                default:
                    return 'fas fa-cube'
            }
        },
        onDragStart(event, entity) {
            event.dataTransfer.setData('text', JSON.stringify(entity))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
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
            border-left: 5px solid #000;
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
        border-left: 10px solid #000;
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
</style>
