## kernel-idvalidator-mispid

[Background & Design](../../docs/design/kernel/kernel-idvalidator.md)
 

 
[API Documentation ]
 
 ```
 mvn javadoc:javadoc

 ```
 
**Properties to be added in Spring application environment using this component**

```
mosip.kernel.mispid.length=3
```

[application-dev.properties](../../config/application-dev.properties)

 
 **Maven Dependency**
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-mispid</artifactId>
			<version>${project.version}</version>
		</dependency>

 ```
 


**The response will be true is case if it pass the all validation condition otherwise it will throw respective error message**

 

**Usage Sample:**

Autowired interface IdValidator and call the method validateId(Id)

 Valid MISPID  Example:
 
 ```
	@Autowired
	private IdValidator<String> mispIdValidatorImpl;
	
	boolean isValid = mispIdValidatorImpl.validateId("100"); //return true
	
```




 






