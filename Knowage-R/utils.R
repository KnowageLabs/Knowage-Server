library(openssl)
library(jose)
source("D:/Knowage/Knowage-Server/Knowage-R/configs.R")

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

resolve_parameters <- function(script, parameters){
  parameters_df <- as.data.frame(parameters)
  print("BEFORE")
  for(i in 1:nrow(parameters_df)) {
    row <- parameters_df[i,]
    print("ROW")
    print(parameters_df)
  }
  print("AFTER")
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