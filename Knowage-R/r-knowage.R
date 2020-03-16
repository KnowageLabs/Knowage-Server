pr <- plumber::plumb("D:/Knowage/Knowage-Server/Knowage-R/webservice.R")
print("HERE ")
print(getwd())
pr$run(port = 5000, host = "0.0.0.0", swagger = FALSE)
