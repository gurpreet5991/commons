## kernel-keymanager-softhsm

[Background & Design](../../docs/design/kernel/kernel-keymanager-softhsm.md)

[Api Documentation]


```
mvn javadoc:javadoc
```

**Application Properties**

```
	mosip.kernel.keymanager.softhsm.config-path=/etc/softhsm2-demo.conf
	#mosip.kernel.keymanager.softhsm.config-path=D\:\\SoftHSM2\\etc\\softhsm2-demo.conf
	mosip.kernel.keymanager.softhsm.keystore-type PKCS11
	mosip.kernel.keymanager.softhsm.keystore-pass=pwd
	mosip.kernel.keymanager.softhsm.certificate.common-name=www.mosip.io
	mosip.kernel.keymanager.softhsm.certificate.organizational-unit=MOSIP
	mosip.kernel.keymanager.softhsm.certificate.organization=IITB
	mosip.kernel.keymanager.softhsm.certificate.country=IN

```


To use this api, add this to dependency list:

```
		<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-keymanager-softhsm</artifactId>
			<version>${project.version}</version>
		</dependency>
```


**Exceptions to be handled while using this functionality:**

1. KeystoreProcessingException
2. NoSuchSecurityProviderException


**Usage Sample**
  
Usage1: Get All Alias
 
 ```
		@Autowired
		private KeyStore keystoreImpl;

		List<String> allAlias = keystoreImpl.getAllAlias();

		allAlias.forEach(alias -> {
			Key key = keystoreImpl.getKey(alias);
			System.out.println(alias + "," + key);
			keystoreImpl.deleteKey(alias);
		});

 
 ```
 
 Output: allAlias
 
 ```
test-alias-private,SunPKCS11-SoftHSM2 RSA private key, 2048 bits (id 2, token object, not sensitive, unextractable)
test-alias-secret,SunPKCS11-SoftHSM2 AES secret key, 32 bits (id 4, token object, not sensitive, unextractable)
 ```
 
 Usage2: Secret Key
 
 ```
 
 		@Autowired
		private KeyStore keystoreImpl;
		
 		KeyGenerator keyGenerator = null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		SecureRandom secureRandom = new SecureRandom();
		int keyBitSize = 256;
		keyGenerator.init(keyBitSize, secureRandom);
		SecretKey secretKey = keyGenerator.generateKey();

		keystoreImpl.storeSymmetricKey(secretKey, "test-alias-secret");

		SecretKey fetchedSecretKey = keystoreImpl.getSymmetricKey("test-alias-secret");
		
```

Output: SecretKey

```
SunPKCS11-SoftHSM2 AES secret key, 32 bits (id 6, token object, not sensitive, unextractable)
```

Usage:3 KeyPair 

```
		@Autowired
		private KeyStore keystoreImpl;
		
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		keyStoreImpl.storeAsymmetricKey(keyPair, "test-alias-private", LocalDateTime.now(),
				LocalDateTime.now().plusDays(365));

		PrivateKey privateKey = keystoreImpl.getPrivateKey("test-alias-private");


		PublicKey publicKey = keystoreImpl.getPublicKey("test-alias-private");
		
		
```

Output: privateKey , publicKey

```
SunPKCS11-SoftHSM2 RSA private key, 2048 bits (id 7, token object, not sensitive, unextractable)

Sun RSA public key, 2048 bits
  modulus: 30328748957969945174954745208064345862312969927254598238526101762391296493510742256233065491262447187409078364000282905404149813261041457625810095572898553468954461087700148296418925749778448475363419327262968745137244118884489221475416539456905265179962075689677948902259773446393861226550815844977347279173492442418988010503374643453726724268482134502743060873752304598505726082877096316359566366812195445612134930294321549768069928020266719012437919578249686342320718712856084420897976841767236464163300792423929086540883232763064844858799603503457594656470904561008040334197773602257581934425199267901046399134783
  public exponent: 65537
  
```

Usage 4 : Certificate

```
@Autowired
private KeyStore keystoreImpl;

X509Certificate certificate = (X509Certificate) keystoreImpl.getCertificate("test-alias-private");



```

Output: certificate

```
[
[
  Version: V3
  Subject: CN=www.mosip.io, OU=MOSIP, O=IITB, C=IN
  Signature Algorithm: SHA1withRSA, OID = 1.2.840.113549.1.1.5

  Key:  Sun RSA public key, 2048 bits
  modulus: 16265726321697190074417570435820273840526375073035324517336363746765717615231463034967523596048334125232762399152129449559887648172484985573599028996853878084271063611042433468250637295902848860022208245186117552741112522087084290954015399873585231466909793093414927350186559745777351425653013789176663345326343349978119814401307260122185081738557665997537450087518000196841656438659836315332485387833632324608293715178218792148295966729133268565081260221357611865111342824069787439471367315492975709574099349381748011137719162784189473363140108977750461142038262582094549226950119548612995912086261424715267362148473
  public exponent: 65537
  Validity: [From: Mon Nov 26 19:19:31 IST 2018,
               To: Tue Nov 27 01:24:31 IST 2018]
  Issuer: CN=www.mosip.io, OU=MOSIP, O=IITB, C=IN
  SerialNumber: [    cb0f0ba3 47dbbc2a]

]
  Algorithm: [SHA1withRSA]
  Signature:
0000: 4C 02 4F 6B 5F FE BC 86   08 1F B4 3D A3 5E 22 F9  L.Ok_......=.^".
0010: F3 8B EE B8 DE 03 EE 97   E2 6C 04 2E BF 5E EE 81  .........l...^..
0020: E2 B2 2A A8 71 93 B3 B1   41 C4 6A 6B 71 AD 30 A3  ..*.q...A.jkq.0.
0030: D5 1A 5F E6 4D DC F8 63   E5 53 B6 20 25 E2 6D 7A  .._.M..c.S. %.mz
0040: 0E CE 40 4A C6 16 EF 75   E7 1B 8C AB CD CF C7 D7  ..@J...u........
0050: B7 B3 D4 F8 23 B6 69 3B   7E 12 45 7F AA 2D 26 E2  ....#.i;..E..-&.
0060: E7 F8 46 5A 42 FF 8E 2E   D5 B6 A6 D4 34 02 C8 6A  ..FZB.......4..j
0070: 46 53 93 04 A4 BC 8D 47   74 1C 10 81 21 74 8F 83  FS.....Gt...!t..
0080: 8B 84 20 30 F0 A9 F7 5B   9C C2 3D EB D1 E4 1B 20  .. 0...[..=.... 
0090: 53 F9 71 17 1C 6A C0 18   A4 76 4F 8D D0 F2 00 55  S.q..j...vO....U
00A0: 76 1D 5C 07 6B 16 CC 6D   36 7B 2C 98 3B 5F A7 D3  v.\.k..m6.,.;_..
00B0: C3 20 42 CC D1 D0 EE B3   49 C8 A5 E0 1F 6B 68 82  . B.....I....kh.
00C0: 71 87 42 17 4E FE EF A4   39 B5 35 A3 E4 30 3D 02  q.B.N...9.5..0=.
00D0: 25 9D DE 7D 93 0D 79 60   6A D1 65 CF B5 C8 D8 00  %.....y`j.e.....
00E0: 05 E0 1D 79 41 F8 9B 8C   4E 42 ED A6 52 2B 96 D9  ...yA...NB..R+..
00F0: 4A 95 D8 F4 78 82 AB 8C   EC 7C 13 22 45 0B 7E 45  J...x......"E..E

]
```


## Setup steps:

### Linux

1. Follow docker installation steps from https://github.com/mosip/mosip/blob/master/kernel/kernel-keymanager-service/README.md

### Windows

1. Download softhsm portable zip archive from https://github.com/disig/SoftHSM2-for-Windows#download
2. Extract it to any location, e.g `D:\SoftHSM2`. SoftHSM2 searches for its configuration file in the following locations:
```
  1. Path specified by SOFTHSM2_CONF environment variable
  2. User specific path %HOMEDRIVE%%HOMEPATH%\softhsm2.conf
  3. File softhsm2.conf in the current working directory
```
3. Modify following in environment variables:
```
> set SOFTHSM2_CONF=D:\SoftHSM2\etc\softhsm2.conf
> set PATH=%PATH%;D:\SoftHSM2\lib\
```
4. Create another conf file at `D:\SoftHSM2\etc\softhsm2-application.conf` with below content
```
# Sun PKCS#11 provider configuration file for SoftHSMv2
name = SoftHSM2
library = D:\SoftHSM2\lib\softhsm2-x64.dll 
slotListIndex = 0
```
5. Install JCE With an Unlimited Strength Jurisdiction Policy as shown here:
https://dzone.com/articles/install-java-cryptography-extension-jce-unlimited
6. Go to `D:\SoftHSM2\bin` and run below command:
```
> softhsm2-util.exe --init-token --slot 0 --label "My token 1"
```
Check token is initialized in slot with below command:
```
> softhsm2-util.exe --show-slots
```
The output should be like below:
```
Slot 569035518
    Slot info:
        Description:      SoftHSM slot ID 0x21eacafe
        Manufacturer ID:  SoftHSM project
        Hardware version: 2.4
        Firmware version: 2.4
        Token present:    yes
    Token info:
        Manufacturer ID:  SoftHSM project
        Model:            SoftHSM v2
        Hardware version: 2.4
        Firmware version: 2.4
        Serial number:    b1ee933e21eacafe
        Initialized:      yes
        User PIN init.:   yes
        Label:            My token 1
Slot 1
    Slot info:
        Description:      SoftHSM slot ID 0x1
        Manufacturer ID:  SoftHSM project
        Hardware version: 2.4
        Firmware version: 2.4
        Token present:    yes
    Token info:
        Manufacturer ID:  SoftHSM project
        Model:            SoftHSM v2
        Hardware version: 2.4
        Firmware version: 2.4
        Serial number:
        Initialized:      no
        User PIN init.:   no
        Label:
```
5. Put the newly conf filepath `D:\SoftHSM2\etc\softhsm2-application.conf` in `mosip.kernel.keymanager.softhsm.config-path` property. Softhsm is ready to be used. 




