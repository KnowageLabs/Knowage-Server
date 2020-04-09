library("jsonlite")
library("base64enc")
source("D:/Knowage/Knowage-Server/Knowage-R/utils.R")

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
  if (!is.null(dataset_name)) {
    script <- gsub(dataset_name,"df_",script)
    env$df_ <- as.data.frame(fromJSON(dataset))
  }
  eval(parse(text=script), envir = env)
  html  <- read.file(output_variable)
  if (file.exists(output_variable))
    file.remove(output_variable)
  html
}

#' @post /dataset
function(script, df_name, parameters){
  decoded_script <- decode_jwt_token(script)
  "prova"
}

#' @get /libraries
#' @get /dataset/libraries
function(){
  lib <- get_libraries()
  lib
}
