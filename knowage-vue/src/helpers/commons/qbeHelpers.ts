import cryptoRandomString from 'crypto-random-string'

export function createNewField(editQueryObj, field) {
    var newField = {
        id: field.attributes.type === 'inLineCalculatedField' ? field.attributes.formState : field.id,
        alias: field.attributes.field,
        type: field.attributes.type === 'inLineCalculatedField' ? 'inline.calculated.field' : 'datamartField',
        fieldType: field.attributes.iconCls,
        entity: field.attributes.entity,
        field: field.attributes.field,
        funct: getFunct(field),
        color: field.color,
        group: getGroup(field),
        order: 'NONE',
        include: true,
        // eslint-disable-next-line no-prototype-builtins
        inUse: field.hasOwnProperty('inUse') ? field.inUse : true,
        visible: true,
        iconCls: field.iconCls,
        dataType: field.dataType,
        format: field.format,
        longDescription: field.attributes.longDescription,
        distinct: editQueryObj.distinct,
        leaf: field.leaf,
        originalId: field.id,
        isSpatial: field.isSpatial,
        uniqueID: cryptoRandomString({ length: 4, type: 'base64' })
    } as any

    // eslint-disable-next-line no-prototype-builtins
    if (!field.hasOwnProperty('id')) {
        newField.id = field.alias
        newField.alias = field.text
        newField.field = field.text
        newField.temporal = field.temporal
    }

    return newField
}

export function creatNewMetadataFromField(newField) {
    var newMetadata = {
        uniqueID: newField.uniqueID,
        column: newField.alias,
        fieldAlias: newField.field,
        Type: newField.dataType,
        fieldType: newField.iconCls.toUpperCase(),
        decrypt: false,
        personal: false,
        subjectid: false
    } as any

    return newMetadata
}

export function getFunct(field) {
    if (isColumnType(field, 'measure') && field.aggtype) {
        return field.aggtype
    } else if (isColumnType(field, 'measure')) {
        return 'SUM'
    }
    return 'NONE'
}
export function getGroup(field) {
    return isColumnType(field, 'attribute') && !isDataType(field, 'com.vividsolutions.jts.geom.Geometry')
}
export function isDataType(field, dataType) {
    return field.dataType == dataType
}
export function isColumnType(field, columnType) {
    return field.iconCls == columnType || isCalculatedFieldColumnType(field, columnType)
}
export function isCalculatedFieldColumnType(inLineCalculatedField, columnType) {
    return isInLineCalculatedField(inLineCalculatedField) && inLineCalculatedField.attributes.formState.nature === columnType
}
export function isInLineCalculatedField(field) {
    return field.attributes.type === 'inLineCalculatedField'
}
