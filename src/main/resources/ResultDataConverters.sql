set @jsonDataConverterId = (select IF(id is NULL,0,max(id)) from DataConverterImplementation) + 1;

insert into DataConverterImplementation(id, dataConverter, format, resultDataType) values(@jsonDataConverterId, 'edu.harvard.hms.dbmi.bd2k.irct.dataconverter.JSONTabularDataConverter', 'JSON', 'TABULAR');
