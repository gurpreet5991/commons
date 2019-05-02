-- create table section --------------------------------------------------------
-- schema 		: reg  - reg  schema
-- table 		: device_spec  - reg device_spec list
-- table alias  : dspec	

-- table section -------------------------------------------------------------------------------

	create table reg.device_spec ( 
	
		id character varying(36) not null ,
	
		name  character varying (64) not null ,
		brand character varying(32) not null ,			-- make
		model character varying(16) not null ,
		
		dtyp_code  character varying(36) not null ,  	-- master.device_type.code
		
		min_driver_ver character varying(16) not null ,
		descr character varying (256) ,
		
		lang_code  character varying(3) not null ,	-- master.language.code
	
		is_active 	boolean not null,
		cr_by 		character varying (256) not null,
		cr_dtimes 	timestamp  not null,
		upd_by  	character varying (256),
		upd_dtimes timestamp ,
		is_deleted 	boolean,
		del_dtimes	timestamp 
	)
;


-- keys section -------------------------------------------------------------------------------
alter table reg.device_spec add constraint pk_dspec_code primary key (id, lang_code)
 ;

-- indexes section -----------------------------------------------------------------------
-- create index idx_dspec_<col> on reg.device_spec (col)
-- ;
