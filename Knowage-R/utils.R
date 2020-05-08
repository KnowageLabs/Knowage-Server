library(openssl)
library(jose)
source("configs.R")

get_libraries <- function(){
  str(allPackages <- installed.packages(.Library, priority = "high"))
  lib_matrix <- allPackages[, c(1,3:5)]
  lib_info <- lib_matrix[,c(1,2)]
  lib_info
}

resolve_drivers <- function(script, drivers){
  for(name in names(drivers)) {
    value <- drivers[[name]]
    original <- paste0('\\$P\\{', name , '\\}')
    final <- paste0('drivers_[[\\"', name, '\\"]]')
    script <- gsub(original,final,script)
  }
  script
}

build_parameters <- function(parameters){
  to_return <- list()
  if(length(parameters) > 0) {
    parameters_df <- as.data.frame(parameters)
    for(i in 1:nrow(parameters_df)) {
      row <- parameters_df[i,]
      name <- row[["name"]]
      value <- row[["value"]]
      type <- row[["type"]]
      if(value == "") {
        value <- row[["defaultValue"]]
      }
      if(type == "Number") {
        value <- as.numeric(value)
      }
      to_return[[name]] <- value
    }
  }
  to_return
}

resolve_parameters <- function(script, parameters){
  for(name in names(parameters)) {
    value <- parameters[[name]]
    original <- paste0('\\$P\\{', name , '\\}')
    final <- paste0('parameters_[[\\"', name, '\\"]]')
    script <- gsub(original,final,script)
  }
  script
}

decode_jwt_token <- function(script){
  token <- jwt_decode_hmac(script, secret = hmac_key)
  token
}

get_script_from_token <- function(token){
  script <- token[["script"]]
  script
}

is_dataset_request_authorized <- function(token){
  expirationTime <- token[["exp"]]
  now <- as.numeric(as.POSIXct(Sys.time()))
  if (now > expirationTime)
    FALSE
  TRUE
}