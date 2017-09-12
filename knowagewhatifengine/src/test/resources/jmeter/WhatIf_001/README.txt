Test di carico del motore WhatIf del 18 giugno 2014

- impostazioni del server: -Xms512m -Xmx1024m -XX:MaxPermSize=256m

- script di test: lo script di test è WhatIf_001, dentro il file Test-WhatIf-001.jmx: 
prevede l'esecuzione in modalità standalone del template tpl.xml con schema FoodMartMySQL.xml
La navigazione consiste nel caricamento della query iniziale, la modifica di alcune celle, il drill down e drill up.

- il test è stato effettuato dalle 17:30 alle 18:30 eseguendo lo script a ripetizione per 100 volte

- risultati (vedi WhatIf_001.PNG): la PermGen è rimasta pressoché costante. La Olg Gen ha avuto un picco verso le 17:45,
dopodiché il GC deve averla ripulita. Al termine delle 100 esecuzioni l'occupazione della Olg Gen superava i 200 MB,
valore sceso a 121 MB dopo la DOPPIA invocazione del GC.