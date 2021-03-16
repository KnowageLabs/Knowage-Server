<template>
  <div class="knPage">
    <div class="knPageContent p-grid p-m-0">
      <div class="knListColumn p-col-3 p-p-0">
        <Toolbar class="knToolbar">
          <template #left>
            {{$t('managers.widgetGallery.title')}}
          </template>
          <template #right>
            <FabButton icon="fas fa-plus" />
          </template>
        </Toolbar>
        <Listbox class="knList" :options="galleryTemplates" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="label" filterMatchMode="contains" :filterFields="['label','type','tags']" :emptyFilterMessage="$t('managers.widgetGallery.noResults')">
          <template #option="slotProps">
            <router-link class="kn-decoration-none" :to="{ name: 'gallerydetail', params: { id: slotProps.option.id }}" exact>
              <div class="knListItem">
                <Avatar :icon="iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" :style="iconTypesMap[slotProps.option.type].style" />
                <div class="knListItemText">
                  <span>{{slotProps.option.label}}</span>
                  <span class="smallerLine">{{slotProps.option.author}}</span>
                </div>
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain kn-gallery-slotProps.option.type" @click="deleteTemplate($event,slotProps.option.id)"/>
              </div>
            </router-link>
          </template>
        </Listbox>
      </div>
      <div class="p-col-9 p-p-0 p-m-0">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import Avatar from 'primevue/avatar'
  import FabButton from '@/components/UI/fabButton/FabButton.vue'
  import Listbox from 'primevue/listbox'

  export default defineComponent({
    name: 'gallery-management',
    components: {
      Avatar,
      FabButton,
      Listbox
    },
    data (){
      return{
        galleryTemplates: [],
        iconTypesMap: {
          "chart": {"className":"fab fa-js-square", "style":{"background-color":"#ffc107","color":"white"}},
          "html": {"className":"fab fa-html5", "style":{"background-color":"#c2185b","color":"white"}},
          "python": {"className":"fab fa-python", "style":{"background-color":"#4caf50","color":"white"}},
        }
      }
    },
    created() {
      this.loadAllTemplates()
    },
    methods:{
      loadAllTemplates() {
        this.axios.get(`/knowage-api/api/1.0/widgetgallery`)
          .then(response => this.galleryTemplates = response.data)
          .catch(error => console.error(error))
      },
      deleteTemplate(e,templateId){
        e.preventDefault()
        /*this.axios.delete(`/knowage/restful-services/3.0/gallery/${this.id}`)
            .then(response => this.template = response.data)
            .catch(error => console.error(error))*/
        alert('delete template: '+templateId)
      }
    }
  })
</script>

<style lang="scss" scoped>

  .knPage {
    display: flex;
    flex-direction: column;
    height: 100%;

    .knPageContent {
      flex: 1;
    }
  }
  .noDecoration{
    text-decoration: none;
    color: inherit;
  }

  .knListColumn {
    border-right: 1px solid #ccc;
  }

  .knList{
    border: none;
    border-radius: 0;
    &:deep() .p-listbox-item {
      padding: 0;
      a {
        display: block;
        padding: 0.75rem 0.75rem;
        &.router-link-active{
          background-color: $color-secondary;
        }
      }
    }
    &:deep() .p-listbox-filter-container{
      input.p-listbox-filter{
        border-radius: 0;
      }
    }
    
    .knListItem{
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      align-items: center;

      .knListItemText{
        display: flex;
        flex: 1;
        flex-direction: column;
        justify-content: center;
        align-items: flex-start;
        margin-left: .8rem;

        .smallerLine{
          color: rgb(148, 148, 148);
          font-size: .8rem;
        }
      }
    }
  }

  .knToolbar{
    background-color: #3B678C;
    border-radius: 0;
    color: white;
  }

</style>
