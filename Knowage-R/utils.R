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