## kernel-auditmanager-service


[Background & Design](../../docs/design/kernel/kernel-auditmanager.md)

[Api Documentation](https://github.com/mosip/mosip/wiki/Kernel-APIs#audit-manager)

Default Port and Context Path

```
server.port=8081
server.servlet.path=/auditmanager

```

localhost:8081/auditmanager/swagger-ui.html


**Application Properties**

[kernel-auditmanager-service-dev.properties](../../config/kernel-auditmanager-service-dev.properties)

```
#logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG

javax.persistence.jdbc.driver=org.postgresql.Driver
javax.persistence.jdbc.url=jdbc:postgresql://localhost:8888/mosip_audit
javax.persistence.jdbc.user=dbuser
javax.persistence.jdbc.password=dbpwd

hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
hibernate.jdbc.lob.non_contextual_creation=true
hibernate.hbm2ddl.auto=none
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.connection.charSet=utf8
hibernate.cache.use_second_level_cache=false
hibernate.cache.use_query_cache=false
hibernate.cache.use_structured_entries=false
hibernate.generate_statistics=false
spring.datasource.initialization-mode=always

```


**The inputs which have to be provided are:**
1. Audit Event ID - Mandatory
2. Audit Event name - Mandatory
3. Audit Event Type - Mandatory
4. Action DateTimestamp - Mandatory
5. Host - Name - Mandatory
6. Host - IP - Mandatory
7. Application Id - Mandatory
8. Application Name - Mandatory
9. Session User Id - Mandatory
10. Session User Name - Mandatory
11. Module Name – Optional
12. Module Id - Optional
13. ID - Mandatory
14. ID Type - Mandatory
15. Logged Timestamp - Mandatory
16. Audit Log Description - Optional
17. cr_by, (Actor who has done the event) - Mandatory
18. cr_dtimes, (When this row is inserted into DB) - Mandatory


**The response will be true is audit request is successful, otherwise false** 

**Exceptions to be handled while using this functionality:**

1. AuditHandlerException ("KER-AUD-001", "Invalid Audit Request. Required parameters must be present")
2. InvalidFormatException ("KER-AUD-002", "Audit Request format is invalid");

**Usage Sample**
  
  *Request*
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{\r\n  \"eventId\": \"EventId12333\",\r\n  \"eventName\": \"Event Name1\",\r\n  \"eventType\": \"EventType3\",\r\n  \"actionTimeStamp\": \"2018-11-04T10:52:48.838Z\",\r\n  \"hostName\": \"Host Name6\",\r\n  \"hostIp\": \"10.89.213.89\",\r\n  \"applicationId\": \"ApplicationId89\",\r\n  \"applicationName\": \"Application Name22\",\r\n  \"sessionUserId\": \"SessionUserId22\",\r\n  \"sessionUserName\": \"Session UserName22\",\r\n  \"id\": \"id3333\",\r\n  \"idType\": \"idType333\",\r\n  \"createdBy\": \"user1\",\r\n  \"moduleName\": \"Module Name22\",\r\n  \"moduleId\": \"ModuleId22\",\r\n  \"description\": \"Description for event\"\r\n}");
Request request = new Request.Builder()
  .url("http://104.211.214.143:8081/auditmanager/audits")
  .post(body)
  .addHeader("Content-Type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
 HTTP Status: 200 OK
  
  ```
{
  "status": true
}
  ```
  
  


 *Invalid Audit Request*
 
 HTTP Status: 400 Bad Request

```
{
    "code": "KER-AUD-001",
    "message": "Invalid Audit Request. Required parameters must be present"
}
```
 

 *Invalid Audit Format*
 
  HTTP Status: 400 Bad Request

```
{
    "code": "KER-AUD-002",
    "message": "Audit Request format is invalid"
}
```

