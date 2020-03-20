library("jsonlite")
library("base64enc")

#' @post /img
function(dataset, dataset_name, script, output_variable){
  env <- new.env()
  script <- gsub(dataset_name,"df_",script)
  env$df_ <- as.data.frame(fromJSON(dataset))
  eval(parse(text=script), envir = env)
  enc_img <- base64encode(output_variable)
  if (file.exists(output_variable))
    unlink(output_variable)
    file.remove(output_variable)
  enc_img
}
