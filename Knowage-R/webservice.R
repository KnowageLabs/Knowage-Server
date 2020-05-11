library("jsonlite")
library("base64enc")
source("utils.R")

#' @post /img
function(dataset, dataset_name=NULL, script, drivers, output_variable){
  env <- new.env()
  analytical_drivers <- fromJSON(drivers)
  env$drivers_ <- analytical_drivers
  script <- resolve_drivers(script, analytical_drivers)
  if (!is.null(dataset_name)) {
    script <- gsub(dataset_name,"df_",script)
    env$df_ <- as.data.frame(fromJSON(dataset))
  }
  eval(parse(text=script), envir = env)
  enc_img <- base64encode(output_variable)
  if (file.exists(output_variable))
    file.remove(output_variable)
  enc_img
}

#' @post /html
function(dataset, dataset_name=NULL, script, drivers, output_variable){
  env <- new.env()
  analytical_drivers <- fromJSON(drivers)
  env$drivers_ <- analytical_drivers
  script <- resolve_drivers(script, analytical_drivers)
  if (!is.null(dataset_name)) {
    script <- gsub(dataset_name,"df_",script)
    env$df_ <- as.data.frame(fromJSON(dataset))
  }
  script <- gsub(output_variable,"env$output_variable_",script)
  eval(parse(text=script), envir = env)
  html <- env$output_variable_
  html
}

#' @post /dataset
function(script, df_name, parameters){
  env <- new.env()
  token <- decode_jwt_token(script)
  if (!is_dataset_request_authorized(token))
    stop("Unauthorized")
  decoded_script <- get_script_from_token(token)
  env$parameters_ <- build_parameters(parameters)
  decoded_script <- resolve_parameters(decoded_script, env$parameters_)
  decoded_script <- gsub(df_name, "df_", decoded_script)
  env$df_ <- data.frame()
  eval(parse(text=decoded_script), envir = env)
  env$df_
}

#' @get /libraries
#' @get /dataset/libraries
function(){
  lib <- get_libraries()
  lib
}
