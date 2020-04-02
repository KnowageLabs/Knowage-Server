get_libraries <- function(){
  str(allPackages <- installed.packages(.Library, priority = "high"))
  lib_matrix <- allPackages[, c(1,3:5)]
  lib_info <- lib_matrix[,c(1,2)]
  lib_info
}

resolve_drivers <- function(script, drivers){
  script
}