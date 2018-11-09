-- create table section -------------------------------------------------
-- schema 		: master	    - Master reference Module
-- table 		: app_login_method	- List of application and their user login methods
-- table alias  : applogm	

-- schemas section -------------------------------------------------

-- create schema if master schema for Master reference Module is not exists
create schema if not exists master
;
  
-- table section -------------------------------------------------
create table master.app_login_method (
	
	app_id 			  character varying (36) not null,  -- master.app_detail.id
	
	login_method_code character varying (36) not null,  -- master.login_method.code
	
	method_seq 		smallint,
	
	lang_code 		character varying (3) not null,		-- master.language.code 
	
	is_active 		boolean not null,
	cr_by 			character varying (32) not null,
	cr_dtimes		timestamp not null,
	upd_by  		character varying (32),
	upd_dtimes 		timestamp,
	is_deleted 		boolean,
	del_dtimes 		timestamp

)
;

-- keys section -------------------------------------------------
 alter table master.app_login_method add constraint pk_applogm_app_id primary key (app_id, login_method_code, lang_code)
 ;


-- indexes section -------------------------------------------------
-- create index idx_applogm_<colX> on master.app_login_method (colX )
-- ;

-- comments section ------------------------------------------------- 
comment on table master.app_login_method is 'Table to store all MOSIP Application and their user login methods'
;

