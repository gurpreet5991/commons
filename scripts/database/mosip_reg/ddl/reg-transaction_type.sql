-- create table section --------------------------------------------------------
-- schema 		: reg	- reg schema
-- table 		: transaction_type	- reg Transaction Type
-- table alias  : trntyp	

-- table section ---------------------------------------------------------------
create table reg.transaction_type (

	code character varying(36) not null,   
	descr character varying(256) not null,

	lang_code character varying(3) not null, 	-- master.language.code
	
	is_active 	boolean not null,
	cr_by 		character varying (256) not null,
	cr_dtimes 	timestamp not null,
	upd_by  	character varying (256),
	upd_dtimes 	timestamp,
	is_deleted 	boolean,
	del_dtimes 	timestamp
)
;

-- keys section ------------------------------------------------------------------
alter table reg.transaction_type add constraint pk_trntyp_code primary key (code, lang_code)
 ;
-- 

-- indexes section --------------------------------------------------------------
-- create index idx_trntyp_<colX> on reg.transaction_type (colX )
-- ;
