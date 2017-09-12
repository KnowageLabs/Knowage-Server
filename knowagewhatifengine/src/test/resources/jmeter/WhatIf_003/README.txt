Test di carico del motore WhatIf del 20 giugno 2014, dalle 15:20 alle 17:30 circa

- impostazioni del server: -Xms512m -Xmx1024m -XX:MaxPermSize=256m

- script di test: gli script di test sono 2, biuser e bidemo, dentro il file Test-WhatIf-003.jmx: 
prevedono l'esecuzione di 2 documenti analitici dentro SpagoBI: entrambi gli utenti si autenticano in SpagoBI,
vanno nel document browser, cliccano sulla cartella e poi sul documento da eseguire, interagiscono con il 
documento OLAP facendo operazioni di drill, spostando gerarchie... ma NON cambiano alcun dato, e poi fanno logout.
I documenti eseguiti sono 2 (uno per ogni utente), che puntano a 2 cubi diversi (vedi i template 
TEST_WHAT_IF_001_template.xml e TEST_WHAT_IF_002_template.xml)
In quasi tutte richieste viene controllato che la risposta contenga i valori attesi.

- risultati (vedi bidemo.csv e biuser.csv): nessun errore, biuser è stato eseguito 99 volte, bidemo 172.
L'occupazione della PermGen è costante, però la Old Generation ha un trend verso l'alto preoccupante.
