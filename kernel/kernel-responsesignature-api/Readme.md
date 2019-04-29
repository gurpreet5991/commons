## kernel-responsesignature-api
This api can be used to digitally sign the response of a microservice.


[Background & Design](../../docs/design/kernel/kernel-responsesignature.md)


**Api Documentation**

```
mvn javadoc:javadoc
```

**Maven dependency**

 ```
    <dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-responsesignature-api</artifactId>
			<version>${project.version}</version>
		</dependency>
 ```


 **Application Properties**
 
 ```
mosip.kernel.keygenerator.asymmetric-algorithm-name=RSA
mosip.kernel.keygenerator.asymmetric-algorithm-length=2048
mosip.kernel.keygenerator.symmetric-algorithm-name=AES
mosip.kernel.keygenerator.symmetric-algorithm-length=256
mosip.kernel.crypto.symmetric-algorithm-name=AES
mosip.kernel.crypto.asymmetric-algorithm-name=RSA

mosip.kernel.signature.signature-request-id=SIGNATURE.REQUEST
mosip.kernel.signature.signature-version-id=v1.0

mosip.signed.header=response-signature

mosip.sign.applicationid=KERNEL
mosip.sign.refid=KER

mosip.kernel.signature.cryptomanager-encrypt-url=https://host/v1/cryptomanager/private/encrypt
mosip.kernel.keymanager-service-publickey-url=https://host/v1/keymanager/publickey/{applicationId}
auth.server.validate.url=https://host/v1.0/authorize/validateToken
 ```
 
 **Sample Usage**
 
   1. *Signing the response*
   
  ```
   #Instance of signatureUtil
   @Autowired
   private SignatureUtil signingUtil;
   
   String responseBody = "\"response\": { \"roles\": [ { \"roleId\": \"REGISTRATION_ADMIN\", \"roleName\": \"REGISTRATION_ADMIN\", \"roleDescription\": \"Registration administrator\" }, { \"roleId\": \"TSP\", \"roleName\": \"TSP\", \"roleDescription\": \"Trusted Service Provider\" } ] }"
  
    #Pass the response body to sign the response with private key.
    SignatureResponse signatureResponse=signingUtil.signResponse( responseBody);
   ```
   
   **Sample response**
 
 ```
 {
  "id": "string",
  "version": "string",
  "responsetime": "2019-04-29T04:37:16.141Z",
  "metadata": null,
  "response": {
    "data": "SvYBeeZTl-ao4loe981MkTTBZ507Om7HaZAzxQ1Dj9M9KNuxgslYbQgFdcsaSnoCiUM5nZRDVl2-GgyUJdlqd9cb5kvnAZQjubV2ZYsZfqu2W8MJnsglXK1iUrD6jPf0KNCQ86UmlHOc9BIFi9u1Wh87b8kKmIdbkL8Jv4x2Yqqvufp5kkFja4udXcIVJhhSmsYS4Z0DtDv6p9eGZ18Gcrz-Nf9G9ZRcGpllOIvZfo7Jq4-MW94TJNBq0FA-H0qwdHFJIDJaCT5lN_dGzD4mFu-9CPL4xpeA76V1E7D_vT_v7UQcFgAu4ewdw4-Qew9guOSCUrrcJ-PF5sYxxyT9Fg"
    "responseTime":"2019:09:09:09.000Z
  },
  "errors": null
}
 ```
   2. *Validate with public key*
   
   ```
    #Instance of signatureUtil
   @Autowired
   private SignatureUtil signingUtil;
   
   boolean isVerfied = signingUtil.validateWithPublicKey(responseSignature,
   responseBody ,publicKey);
   
   ```
 3. *Validate*
 
 
   ```
   #Instance of signatureUtil
   @Autowired
   private SignatureUtil signingUtil;
   
   boolean isVerfied = signingUtil.validate(responseSignature, responseBody,
				responseTime);
  ```
  
  **Sample response**
 
 ```
 #if the signature is verified the response will be true
 boolean isVerified= true;
 ```
 
  
  
   
 
   