library("jsonlite")
library("base64enc")

#' @post /img
function(dataset, dataset_name, script, output_variable){
  env <- new.env()
  script <- gsub(dataset_name,"df_",script)
  env$df_ <- as.data.frame(fromJSON(dataset))
  eval(parse(text=script), envir = env)
  enc_img <- base64encode(output_variable)
  paste0('<img src="data:image/;base64, ', enc_img, '" style="width:100%;height:100%;">')
}
