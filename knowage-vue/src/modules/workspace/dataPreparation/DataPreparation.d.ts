export interface ITransformation {
	type: String
	config: { parameters?: Array<ITransformationParameter> }
}

export interface ITransformationParameter {
	name: String
	value?: String
}
