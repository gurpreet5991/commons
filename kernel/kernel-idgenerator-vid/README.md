## Module kernel-idgenerator-vid

[API Documentation](doc/index.html)

```
 mvn javadoc:javadoc

 ```
**Maven Dependency**

```
	<dependencies>
		<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-vid</artifactId>
			<version>${project.version}</version>
	</dependency>

```



** Properties to be added in parent Spring Application environment **

[application-dev.properties](../../config/application-dev.properties)


** Database Properties **

Schema : ids

Table : vid


**Description**

1.**ADMIN** _can only configure the length_ 

2.Logic behind generating vid
  1. _The  Id should not be generated sequentially._
  2. _cannot not have repeated numbers,cannot contain any repeating numbers for configured number of digit or more than configured number of digits in property file._
  3. _cannot have repeated block of numbers for configured number of digits in property file._ 
  4. _cannot contain any sequential number for configured number of digits or more than configured number of  digits in property file and cannot contain alphanumeric values._
  5. _The last digit of the generated id should be reserved for checksum_  
  6. _The number should not contain '0' or '1' as the first digit._
  
  
**Sample Usage**
  
    
      //Autowire the interface class vidGenerator
	  @Autowired
	  private VidGenerator<String> vidGeneratorImpl;
	
     //Call generateId from autowired vidGenerator instance to generateId.
     
     
	  String generatedVid = vidGeneratorImpl.generateId());
	  
	  
	 
**Sample VID**
	  
_Generated vid_: 5916983045841801  
  






