## kernel-templatemanager-velocity

 
[Background & Design](../../docs/design/kernel/kernel-templatemanager.md)
 

[API Documentation]
 
 ```
 mvn javadoc:javadoc

 ```
 
 
 **Maven Dependency**
 
 ```
 <dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-templatemanager-velocity</artifactId>
			<version>${project.version}</version>
 </dependency>
 ```
 
 
 3- Usage Sample
 
 
 Usage1: TemplateManager with default configuration and merge template (as InputStream)
 
 ```
//Autowire TemplateManagerBuilder
@Autowired
private TemplateManagerBuilder templateManagerBuilder;

//Traditional approach
//Call build() to create TemplateManager with default configuration
TemplateManager templateManager = templateManagerBuilder.build();

or

//By using spring java configuration 
@Bean
public TemplateManager templateManager(TemplateManagerBuilder templateManagerBuilder){
	//Call build() to create TemplateManager with default configuration
	return templateManagerBuilder.build();
}

//Create Map for templateValues and put all values
Map<String,Object> templateValues=new HashMap<>();
templateValues.put("name","nick");
..

//Call merge() method by passing template as InputStream and value as Map
InputStream resultedTemplate = templateManager.merge(templateInputStream, templateValues);


 EXAMPLE:
 InputStream is = this.getClass().getClassLoader().getResourceAsStream("template.vm");
 InputStream data = templateManager.merge(is, valueMap);
 
 ```

 Usage2: TemplateManager with custom configuration and merge template from classPath 
 
 ```
//Autowire TemplateManagerBuilder
@Autowired
private TemplateManagerBuilder templateManagerBuilder;

//Traditional approach
//Call build() to create TemplateManager with default configuration
TemplateManager templateManager = templateManagerBuilder.enableCache(false).resourceLoader("classPath").build();


or

//By using spring java configuration 
@Bean
public TemplateManager templateManager(TemplateManagerBuilder templateManagerBuilder){
	//Call build() to create TemplateManager with custom configuration configuration
	return templateManagerBuilder.enableCache(false).resourceLoader("classPath").build();
}


//Create Map for template values and put all values
Map<String,Object> templateValues=new HashMap<>();
templateValues.put("name","nick");
..

//For writing processed template
Writer writer=new StringWriter();

//TemplatePath , relative path from classPath
String templatePath="templates/email_template.vm";

//Call merge() method by passing templatePath ,writer and template value
InputStream resultedTemplate = templateManager.merge(templatePath,writer, templateValues);
 
 ```

 Usage3: TemplateManager with custom configuration and merge template from classPath and with different encoding type
 
 ```
//Autowire TemplateManagerBuilder
@Autowired
private TemplateManagerBuilder templateManagerBuilder;

//Traditional approach
//Call build() to create TemplateManager with default configuration
TemplateManager templateManager = templateManagerBuilder.enableCache(false).resourceLoader("classPath").build();


or

//By using spring java configuration 
@Bean
public TemplateManager templateManager(TemplateManagerBuilder templateManagerBuilder){
	//Call build() to create TemplateManager with custom configuration configuration
	return templateManagerBuilder.enableCache(false).resourceLoader("classPath").build();
}


//Create Map for template values and put all values
Map<String,Object> templateValues=new HashMap<>();
templateValues.put("name","nick");
..

//Encoding type
String encodingType=CharSetEnconding.UTF_16.name();

//For writing processed template
Writer writer=new StringWriter();


//TemplatePath , relative path from classPath
String templatePath="templates/email_template.vm";

//Call merge() method by passing templatePath and template value,writer and encoding type
InputStream resultedTemplate = templateManager.merge(templatePath,writer, templateValues,encodingType);
 
 ```




