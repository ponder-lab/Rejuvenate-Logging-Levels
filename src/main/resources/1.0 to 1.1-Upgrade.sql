-- Update Data Converters
update DataConverterImplementation set dataConverter='edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.JSONTabularDataConverter' where dataConverter='edu.harvard.hms.dbmi.bd2k.irct.dataconverter.JSONTabularDataConverter';
update DataConverterImplementation set dataConverter='edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XMLTabularDataConverter' where dataConverter='edu.harvard.hms.dbmi.bd2k.irct.dataconverter.XMLTabularDataConverter';
update DataConverterImplementation set dataConverter='edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XSLXTabularDataConverter' where dataConverter='edu.harvard.hms.dbmi.bd2k.irct.dataconverter.XSLXTabularDataConverter';
update DataConverterImplementation set dataConverter='edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.CSVTabularDataConverter' where dataConverter='edu.harvard.hms.dbmi.bd2k.irct.dataconverter.CSVTabularDataConverter';


-- Drop Tables so they can be rebuilt
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE ClauseAbstract;
DROP TABLE Entity;
DROP TABLE Query_Resource;
DROP TABLE where_values;
SET FOREIGN_KEY_CHECKS = 1;