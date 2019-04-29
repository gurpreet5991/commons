-- create table section -------------------------------------------------
-- schema 		: reg			- Registration Module
-- table 		: user_detail	- Registration application users
-- table alias  : usrdtl	

-- schemas section -------------------------------------------------

-- create schema if reg schema for Registration Module is not exists
create schema if not exists reg
;

-- table section -------------------------------------------------
create table reg.user_detail (
	
	id 			character varying (256) not null,
	
	uin		 	character varying (28),			-- UIN of indivisuals and referenced from idrepo database 
	
	name 		character varying (64) not null,
	
	email 		character varying (256),
	mobile 		character varying (16),
	
	status_code character varying(64) not null,			-- master.status_list.code
	
	lang_code 	character varying (3) not null,			-- master.language.code
		
	last_login_dtimes timestamp,
	last_login_method character varying (36),			-- master.login_method.code
	
	unsuccessful_login_count smallint,
	userlock_till_dtimes timestamp,

	is_active 	boolean not null,
	cr_by 		character varying (256) not null,
	cr_dtimes	timestamp not null,
	upd_by  	character varying (256),
	upd_dtimes  timestamp,
	is_deleted 	boolean,
	del_dtimes  timestamp
	
)
;

-- keys section -------------------------------------------------
 alter table reg.user_detail add constraint pk_usrdtl_id primary key (id)
 ;

 -- indexes section -------------------------------------------------
-- create index idx_usrdtl_uin_ref_id on reg.user_detail (uin_ref_id)
-- ;

-- comments section ------------------------------------------------- 
comment on table reg.user_detail is 'Application user details, List of user will have access to MOSIP applications based on assigned roles'
;

