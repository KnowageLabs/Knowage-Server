export interface iMenuNode {
  adminsMenu: Boolean,
  clickable: Boolean,
  code: number | null,
  custIcon: string | null,
  depth: string | null,
  descr: string | null,
  externalApplicationUrl: string | null,
  functionality: any | null,
  groupingMenu: string | null,
  hasChildren: Boolean,
  hideSliders: Boolean,
  hideToolbar: Boolean,
  icon: iIcon,
  iconCls: string | null,
  iconPath: string | null,
  initialPath: any | null,
  level: number | null,
  linkType: string | null,
  lstChildren: [],
  menuId: number,
  name: string,
  document: string | null,
  objId: number | null,
  objParameters: string | null,
  parentId: number | null,
  prog: number,
  roles: iRole[],
  snapshotHistory: number | null,
  snapshotName: string | null,
  staticPage: string | null,
  subObjName: number | null,
  url: string | null,
  viewIcons: Boolean,
  menuNodeContent: any | null
}

export interface iIcon {
  id: number,
  category: string,
  className: string,
  fontFamily: string,
  fontWeight: number,
  label: string,
  unicode: string
  visible: Boolean
}

export interface iRole {
  id: number | null;
  name: string;
  value: string;
}

export interface Item {
  [key: string]: any;
}

export interface TreeItem {
  [key: string]: Item | TreeItem[] | any;
}

export interface Config {
  id: string;
  parentId: string;
  dataField: string | null;
  childrenField: string;
  throwIfOrphans: boolean;
  rootParentIds: { [rootParentId: string]: true };
  nestedIds: boolean;
}