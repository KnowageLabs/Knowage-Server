Test di carico del motore WhatIf del 19 giugno 2014

- impostazioni del server: -Xms512m -Xmx1024m -XX:MaxPermSize=256m

- script di test: lo script di test è WhatIf_002, dentro il file Test-WhatIf-001.jmx: 
prevede l'esecuzione in modalità standalone del template tpl.xml con schema FoodMartMySQL.xml
La navigazione consiste nel caricamento della query iniziale, la modifica di alcune celle, il drill down e drill up, 
il salvataggio come nuova versione e la cancellazione della versione appena creata.
In quasi tutte richieste viene controllato che la risposta contenga i valori attesi (esempio: se modificando una cella 
impostando 100 ci si attende che le celle figlie valgano 20, 50 e 30, allora si controlla che la risposta contenga 100, 
20, 50 e 30, in realtà però senza verificare che quei valori corrispondano esattamente alle celle modificate).

- il test è stato effettuato dalle 18:00 eseguendo lo script a ripetizione per 100 volte

- risultati (vedi WhatIf_002_2014_06_19.csv): nessun errore, le operazioni più critiche sono
1. creazione nuova versione: da un minimo di 64 ad un massimo di 84, la media è 71;
2. la cancellazione della versione: da un minimo di 12 ad un massimo di 15, la media è 13.
Il mattino seguente (dopo diverse ore che il test era concluso, il server era in buono stato di memoria)