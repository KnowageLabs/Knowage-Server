export interface iFolderTemplate {
	root?: iFolderTemplate
	key: String
	label: String
	icon?: String
	level: Number
	modelFolder: boolean
	relativePath: String
	children?: Array<iFolderTemplate>
}

export interface IFileTemplate {
	name: String
	size: String
	lastModified: Number
}

export interface iModelMetadataTemplate {
	name: String
	version: String
	typeOfAnalytics: String
	openSource: Boolean
	description: String
	accuracyAndPerformance: String
	usageOfTheModel: String
	formatOfData: String
	image: string | ArrayBuffer
}
