source("D:/Knowage/Knowage-Server/Knowage-R/constants.R")

pr <- plumber::plumb("D:/Knowage/Knowage-Server/Knowage-R/webservice.R")
pr$run(port = r_webservice_port, host = knowage_address, swagger = FALSE)
