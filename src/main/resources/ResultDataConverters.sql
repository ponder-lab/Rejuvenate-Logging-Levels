set @jsonDataConverterId = (select IF(id is NULL,0,max(id)) from DataConverterImplementation) + 1;
set @xmlDataConverterId = @jsonDataConverterId + 1;
set @xlsxDataConverterId = @xmlDataConverterId + 1;
set @csvDataConverterId = @xlsxDataConverterId + 1;

insert into DataConverterImplementation(id, dataConverter, format, resultDataType) values(@jsonDataConverterId, 'edu.harvard.hms.dbmi.bd2k.irct.dataconverter.JSONTabularDataConverter', 'JSON', 'TABULAR');
insert into DataConverterImplementation(id, dataConverter, format, resultDataType) values(@xmlDataConverterId, 'edu.harvard.hms.dbmi.bd2k.irct.dataconverter.XMLTabularDataConverter', 'XML', 'TABULAR');
insert into DataConverterImplementation(id, dataConverter, format, resultDataType) values(@xlsxDataConverterId, 'edu.harvard.hms.dbmi.bd2k.irct.dataconverter.XSLXTabularDataConverter', 'XLSX', 'TABULAR');
insert into DataConverterImplementation(id, dataConverter, format, resultDataType) values(@csvDataConverterId, 'edu.harvard.hms.dbmi.bd2k.irct.dataconverter.CSVTabularDataConverter', 'CSV', 'TABULAR');