# Log4Shell

Log4Shell è una vulnerabilità del software di logging Apache Log4j che permette all'attaccante di eseguire codice arbitrario (RCE: Remote Code Execution) sul server vulnerabile.
Il vettore di attacco di questa vulnerabilità è un payload malevolo (stringa), che quando viene loggata permette all'utente di eseguire codice arbitrario sulla macchina.



8080 -> Java vulnerabile 
1380 -> PoC [python] localhost:1380
8000 -> webport server http di base usato dal protocollo LDAP
9001 -> nc
