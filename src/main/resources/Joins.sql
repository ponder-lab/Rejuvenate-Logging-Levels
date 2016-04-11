-- Set the join variables
-- LEFT OUTER JOIN
set @LOJID = (select IF(max(id) is NULL,0, max(id)) from JOINTYPE) + 1;
set @LOJ_LEFTRESULTSET_ID = (select IF(max(id) is NULL,0, max(id)) from FIELD) + 1;
set @LOJ_LEFTCOLUMN_ID = @LOJ_LEFTRESULTSET_ID + 1;
set @LOJ_RIGHTRESULTSET_ID = @LOJ_LEFTRESULTSET_ID + 2;
set @LOJ_RIGHTCOLUMN_ID = @LOJ_LEFTRESULTSET_ID + 3;

-- RIGHT OUTER JOIN
set @ROJID = @LOJID + 2;
set @ROJ_LEFTRESULTSET_ID = @LOJ_LEFTRESULTSET_ID + 5;
set @ROJ_LEFTCOLUMN_ID = @ROJ_LEFTRESULTSET_ID + 1;
set @ROJ_RIGHTRESULTSET_ID = @ROJ_LEFTRESULTSET_ID + 2;
set @ROJ_RIGHTCOLUMN_ID = @ROJ_LEFTRESULTSET_ID + 3;

-- FULL OUTER JOIN
set @FOJID = @LOJID + 3;
set @FOJ_LEFTRESULTSET_ID = @LOJ_LEFTRESULTSET_ID + 10;
set @FOJ_LEFTCOLUMN_ID = @FOJ_LEFTRESULTSET_ID + 1;
set @FOJ_RIGHTRESULTSET_ID = @FOJ_LEFTRESULTSET_ID + 2;
set @FOJ_RIGHTCOLUMN_ID = @FOJ_LEFTRESULTSET_ID + 3;

-- INNER JOIN
set @IJID = @LOJID + 4;
set @IJ_LEFTRESULTSET_ID = @LOJ_LEFTRESULTSET_ID + 15;
set @IJ_LEFTCOLUMN_ID = @IJ_LEFTRESULTSET_ID + 1;
set @IJ_RIGHTRESULTSET_ID = @IJ_LEFTRESULTSET_ID + 2;
set @IJ_RIGHTCOLUMN_ID = @IJ_LEFTRESULTSET_ID + 3;

-- Insert into Join Type
insert into JoinType(id, action, description, displayName, name) values (@LOJID, 'edu.harvard.hms.dbmi.bd2k.irct.join.LeftOuterJoin', 'Performs a left outer join on two result sets', 'Left Outer Join', 'leftOuterJoin');
insert into JoinType(id, action, description, displayName, name) values (@ROJID, 'edu.harvard.hms.dbmi.bd2k.irct.join.RightOuterJoin', 'Performs a right outer join on two result sets', 'Right Outer Join', 'rightOuterJoin');
insert into JoinType(id, action, description, displayName, name) values (@FOJID, 'edu.harvard.hms.dbmi.bd2k.irct.join.FullOuterJoin', 'Performs a full outer join on two result sets', 'Full Outer Join', 'fullOuterJoin');
insert into JoinType(id, action, description, displayName, name) values (@IJID, 'edu.harvard.hms.dbmi.bd2k.irct.join.InnerJoin', 'Performs an inner join on two result sets', 'Inner Join', 'innerJoin');

-- Insert into fields
insert into FIELD(id, description, name, path, required) values (@LOJ_LEFTRESULTSET_ID, 'Result set on left side of join', 'Left Result Set', 'LeftResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@LOJ_LEFTCOLUMN_ID, 'Column for result set on left side of join', 'Left Result Set Column', 'LeftColumn', 1);
insert into FIELD(id, description, name, path, required) values (@LOJ_RIGHTRESULTSET_ID, 'Result set on right side of join', 'Right Result Set', 'RightResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@LOJ_RIGHTCOLUMN_ID, 'Column for result set on right side of join', 'Right Result Set Column', 'RightColumn', 1);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@LOJID, @LOJ_LEFTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@LOJID, @LOJ_LEFTCOLUMN_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@LOJID, @LOJ_RIGHTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@LOJID, @LOJ_RIGHTCOLUMN_ID);

insert into FIELD(id, description, name, path, required) values (@ROJ_LEFTRESULTSET_ID, 'Result set on left side of join', 'Left Result Set', 'LeftResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@ROJ_LEFTCOLUMN_ID, 'Column for result set on left side of join', 'Left Result Set Column', 'LeftColumn', 1);
insert into FIELD(id, description, name, path, required) values (@ROJ_RIGHTRESULTSET_ID, 'Result set on right side of join', 'Right Result Set', 'RightResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@ROJ_RIGHTCOLUMN_ID, 'Column for result set on right side of join', 'Right Result Set Column', 'RightColumn', 1);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@ROJID, @ROJ_LEFTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@ROJID, @ROJ_LEFTCOLUMN_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@ROJID, @ROJ_RIGHTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@ROJID, @ROJ_RIGHTCOLUMN_ID);
--
insert into FIELD(id, description, name, path, required) values (@FOJ_LEFTRESULTSET_ID, 'Result set on left side of join', 'Left Result Set', 'LeftResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@FOJ_LEFTCOLUMN_ID, 'Column for result set on left side of join', 'Left Result Set Column', 'LeftColumn', 1);
insert into FIELD(id, description, name, path, required) values (@FOJ_RIGHTRESULTSET_ID, 'Result set on right side of join', 'Right Result Set', 'RightResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@FOJ_RIGHTCOLUMN_ID, 'Column for result set on right side of join', 'Right Result Set Column', 'RightColumn', 1);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@FOJID, @FOJ_LEFTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@FOJID, @FOJ_LEFTCOLUMN_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@FOJID, @FOJ_RIGHTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@FOJID, @FOJ_RIGHTCOLUMN_ID);
--
insert into FIELD(id, description, name, path, required) values (@IJ_LEFTRESULTSET_ID, 'Result set on left side of join', 'Left Result Set', 'LeftResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@IJ_LEFTCOLUMN_ID, 'Column for result set on left side of join', 'Left Result Set Column', 'LeftColumn', 1);
insert into FIELD(id, description, name, path, required) values (@IJ_RIGHTRESULTSET_ID, 'Result set on right side of join', 'Right Result Set', 'RightResultSet', 1);
insert into FIELD(id, description, name, path, required) values (@IJ_RIGHTCOLUMN_ID, 'Column for result set on right side of join', 'Right Result Set Column', 'RightColumn', 1);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@IJID, @IJ_LEFTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@IJID, @IJ_LEFTCOLUMN_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@IJID, @IJ_RIGHTRESULTSET_ID);
insert into JOINTYPE_FIELD(JoinType_id, Fields_id) values (@IJID, @IJ_RIGHTCOLUMN_ID);

-- Insert into datatypes
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@LOJ_LEFTRESULTSET_ID ,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@LOJ_LEFTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@LOJ_RIGHTRESULTSET_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@LOJ_RIGHTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');

insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@ROJ_LEFTRESULTSET_ID ,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@ROJ_LEFTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@ROJ_RIGHTRESULTSET_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@ROJ_RIGHTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');

insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@FOJ_LEFTRESULTSET_ID ,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@FOJ_LEFTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@FOJ_RIGHTRESULTSET_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@FOJ_RIGHTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');

insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@IJ_LEFTRESULTSET_ID ,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@IJ_LEFTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@IJ_RIGHTRESULTSET_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into FIELD_DATATYPES(Field_Id, dataTypes) values (@IJ_RIGHTCOLUMN_ID,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');