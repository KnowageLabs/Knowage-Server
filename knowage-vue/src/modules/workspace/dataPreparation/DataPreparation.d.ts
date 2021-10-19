export interface ITransformation {
	type: String
	config: ITransformationConfig
	description: String
}
export interface ITransformationConfig {
	parameters: Array<Array<ITransformationParameter>>
	conditions?: Array
	logicOperators?: Array
}

export interface ITransformationParameter {
	name: String
	value: string
	selectedItems: Array<IDataPreparationColumn>
	type: string
}

export interface IDataPreparationColumn {
	header: string
	type: string
	disabled: Boolean
}

export interface IDataPreparationDataset {
	name: String
	label: String
	description: String
	visibility: String
	refreshRate: {}
}
