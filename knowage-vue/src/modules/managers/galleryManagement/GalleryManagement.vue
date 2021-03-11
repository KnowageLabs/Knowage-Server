<template>
  <div class="knPage">
    <div class="knPageContent p-grid p-m-0">
      <div class="knListColumn p-col-3 p-p-0">
        <Toolbar class="knToolbar">
          <template #left>
            {{$t('managers.gallery.title')}}
          </template>
        </Toolbar>
        <Listbox class="knList" v-model="selectedCars" :options="galleryTemplates" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="label" filterMatchMode="contains" >
          <template #option="slotProps">
            <router-link class="noDecoration" :to="slotProps.option.id" exact>
              <div class="knListItem">
                <Avatar :icon="iconTypesMap[slotProps.option.type]" shape="circle" size="medium"/>
                <div class="knListItemText">
                  <span>{{slotProps.option.label}}</span>
                  <span class="smallerLine">{{slotProps.option.author}}</span>
                </div>
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded" />
              </div>
            </router-link>
          </template>
        </Listbox>
      </div>
      <div class="p-col-9">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import Avatar from 'primevue/avatar'
  import Listbox from 'primevue/listbox'

  export default defineComponent({
    name: 'gallery-management',
    data (){
      return{
        galleryTemplates: [
          {id:'1234', label:'test1', type:'html', author:'author1', tags:[]},
          {id:'5678', label:'test2', type:'chart', author:'author2', tags:["tag1", "tag2"]}
        ],
        iconTypesMap: {
          "chart": "fab fa-js-square",
          "html": "fab fa-html5",
          "python": "fab fa-python"
        }
      }
    },
    components: {
      Avatar,
      Listbox
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

    .p-listbox-filter{
      border-radius: 0;
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
