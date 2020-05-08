source("constants.R")

pr <- plumber::plumb("webservice.R")
pr$run(port = r_webservice_port, host = knowage_address, swagger = FALSE)
