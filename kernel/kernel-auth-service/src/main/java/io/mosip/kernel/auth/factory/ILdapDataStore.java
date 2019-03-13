/**
 * 
 */
package io.mosip.kernel.auth.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.auth.config.MosipEnvironment;
import io.mosip.kernel.auth.constant.AuthConstant;
import io.mosip.kernel.auth.entities.AuthZResponseDto;
import io.mosip.kernel.auth.entities.ClientSecret;
import io.mosip.kernel.auth.entities.LoginUser;
import io.mosip.kernel.auth.entities.MosipUserDto;
import io.mosip.kernel.auth.entities.MosipUserDtoToken;
import io.mosip.kernel.auth.entities.MosipUserListDto;
import io.mosip.kernel.auth.entities.RoleDto;
import io.mosip.kernel.auth.entities.RolesListDto;
import io.mosip.kernel.auth.entities.UserOtp;
import io.mosip.kernel.auth.entities.otp.OtpUser;
import io.mosip.kernel.auth.entities.otp.OtpValidateRequestDto;
import io.mosip.kernel.auth.jwtBuilder.TokenGenerator;
import io.mosip.kernel.auth.jwtBuilder.TokenValidator;
import io.mosip.kernel.auth.service.CustomTokenServices;

/**
 * @author Ramadurai Pandian
 *
 */
@Component
public class ILdapDataStore implements IDataStore {
	
	private DataBaseConfig dataBaseConfig;
	
	public ILdapDataStore()
	{
	}
	public ILdapDataStore(DataBaseConfig dataBaseConfig)
	{
		super();
		this.dataBaseConfig=dataBaseConfig;
	}

	@Autowired
    TokenGenerator tokenGenerator;
	
	 @Autowired
	 TokenValidator tokenValidator;
	 
	 @Autowired
	 MosipEnvironment environment;
	
	private LdapConnection createAnonymousConnection() throws Exception {
		LdapNetworkConnection network = new LdapNetworkConnection(dataBaseConfig.getUrl(),Integer.valueOf(dataBaseConfig.getPort()));
		LdapConnection connection = new LdapNetworkConnection(dataBaseConfig.getUrl(),Integer.valueOf(dataBaseConfig.getPort()));
		return connection;
	}
	
	@Override
	public MosipUserDto authenticateUser(LoginUser loginUser) throws Exception {
		MosipUserDto mosipUser = getLoginDetails(loginUser);
		return mosipUser;
	}


	/* (non-Javadoc)
	 * @see io.mosip.kernel.auth.service.AuthNService#authenticateWithOtp(io.mosip.kernel.auth.entities.otp.OtpUser)
	 */
	@Override
	public MosipUserDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		MosipUserDto mosipUser = getOtpDetails(otpUser);
		return mosipUser;
	}

	private MosipUserDto getOtpDetails(OtpUser otpUser) throws Exception {
		LdapConnection connection = createAnonymousConnection();
		Dn userdn = createUserDn(otpUser.getUserId());
		if(!connection.exists(userdn))
		{
			if(otpUser.getOtpChannel().equals(AuthConstant.PHONE))
			{
				Entry userEntry = new DefaultEntry("uid=" + otpUser.getUserId() + ",ou=people,c=morocco",
						"objectClass: organizationalPerson", "objectClass: person", "objectClass: inetOrgPerson",
						"objectClass: top", "mobile", otpUser.getUserId(), "uid",
						otpUser.getUserId(),"sn",otpUser.getUserId(),"cn",otpUser.getUserId());
				connection.add(userEntry);

				Modification roleModification = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,
						"roleOccupant", String.valueOf(userdn));
				connection.modify("cn=individual,ou=roles,c=morocco", roleModification);
			}
			else
			{

				Entry userEntry = new DefaultEntry("uid=" + otpUser.getUserId() + ",ou=people,c=morocco",
						"objectClass: organizationalPerson", "objectClass: person", "objectClass: inetOrgPerson",
						"objectClass: top", "mail", otpUser.getUserId(), "uid",
						otpUser.getUserId());
				connection.add(userEntry);

				Modification roleModification = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,
						"roleOccupant", String.valueOf(userdn));
				connection.modify("cn=individual,ou=roles,c=morocco", roleModification);
			
			}
			
		}
		MosipUserDto mosipUserDto = lookupUserDetails(userdn, connection);
		return mosipUserDto;
	}
	/* (non-Javadoc)
	 * @see io.mosip.kernel.auth.service.AuthNService#authenticateUserWithOtp(io.mosip.kernel.auth.entities.UserOtp)
	 */
	@Override
	public MosipUserDto authenticateUserWithOtp(UserOtp userOtp) throws Exception {
		MosipUserDto mosipUserDto = getMosipUser(userOtp.getUserId());
		return mosipUserDto;
	}

	private MosipUserDto getMosipUser(String userId) throws Exception {
		LdapConnection connection = createAnonymousConnection();
		Dn userdn = createUserDn(userId);
		MosipUserDto mosipUserDto = lookupUserDetails(userdn, connection);
		return mosipUserDto;
	}
	/* (non-Javadoc)
	 * @see io.mosip.kernel.auth.service.AuthNService#authenticateWithSecretKey(io.mosip.kernel.auth.entities.ClientSecret)
	 */
	@Override
	public MosipUserDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		MosipUserDto mosipUser = getClientSecretDetails(clientSecret);
		return mosipUser;
	}

	private MosipUserDto getClientSecretDetails(ClientSecret clientSecret) throws Exception {
		LdapConnection connection = createAnonymousConnection();
		Dn userdn = createUserDn(clientSecret.getClientId());
		connection.bind(userdn, clientSecret.getSecretKey());

		if (connection.isAuthenticated()) {
			return lookupUserDetails(userdn, connection);
		}
		connection.unBind();
		connection.close();
		return null;
	}
	/* (non-Javadoc)
	 * @see io.mosip.kernel.auth.service.AuthZService#verifyOtp(io.mosip.kernel.auth.entities.otp.OtpValidateRequestDto, java.lang.String)
	 */
	
	public MosipUserDto getLoginDetails(LoginUser loginUser) throws Exception {
		LdapConnection connection = createAnonymousConnection();
		Dn userdn = createUserDn(loginUser.getUserName());
		connection.bind(userdn, loginUser.getPassword());

		if (connection.isAuthenticated()) {
			return lookupUserDetails(userdn, connection);
		}
		connection.unBind();
		connection.close();
		return null;
	}
	
	private MosipUserDto lookupUserDetails(Dn userdn, LdapConnection connection) throws Exception {
		try {
			// if lookup access is retricted only to admin then bind the
			// connection with
			// admin details
			// connection.bind(createAdminDn(),
			// mosipEnvironment.getLdapAdminPassword());

			Collection<String> roles = getUserRoles(userdn, connection);
			String rolesString = convertRolesToString(roles);
			MosipUserDto mosipUserDto = null;

			Entry userLookup = connection.lookup(userdn);
			if (userLookup != null) {
				mosipUserDto = new MosipUserDto();
				mosipUserDto.setUserId(userLookup.get("uid").get().toString());
				mosipUserDto.setMobile(userLookup.get("mobile")!=null?userLookup.get("mobile").get().toString():null);
				mosipUserDto.setMail(userLookup.get("mail")!=null?userLookup.get("mail").get().toString():null);
				mosipUserDto.setUserPassword(userLookup.get("userPassword")!=null?userLookup.get("userPassword").get().getBytes():null);
				// mosipUserDto.setLangCode(userLookup.get("preferredLanguage").get().toString());
				mosipUserDto.setName(userLookup.get("cn").get().toString());
				mosipUserDto.setRole(rolesString);
			}
			return mosipUserDto;
		} catch (Exception err) {
			throw new RuntimeException("unable to fetch user details", err);
		}
	}
	
	private Collection<String> getUserRoles(Dn userdn, LdapConnection connection) {
		try {
			Dn searchBase = new Dn("ou=roles,c=morocco");
			String searchFilter = "(&(objectClass=organizationalRole)(roleOccupant=" + userdn
					+ "))";

			EntryCursor rolesData = connection.search(searchBase, searchFilter, SearchScope.ONELEVEL);

			Set<String> roles = new HashSet<String>();
			for (Entry entry : rolesData) {
				roles.add(entry.get("cn").getString());
			}

			rolesData.close();
			return roles;
		} catch (Exception err) {
			throw new RuntimeException(err + "Unable to fetch user roles from LDAP");
		}
	}

	private String convertRolesToString(Collection<String> roles) throws Exception {
		StringBuilder rolesString = new StringBuilder();
		for (String role : roles) {
			rolesString.append(role);
			rolesString.append(",");
		}

		return rolesString.length() > 0 ? rolesString.substring(0, rolesString.length() - 1) : "";
	}
	
	private Dn createUserDn(String userName) throws Exception {
		return new Dn("uid=" + userName + ",ou=people,c=morocco");
	}

	public RolesListDto getAllRoles() {
		RolesListDto rolesListDto = new RolesListDto();

		try {
			LdapConnection connection = createAnonymousConnection();
			List<RoleDto> roleDtos = new ArrayList<>();
			Dn searchBase = new Dn(environment.getRolesSearchBase());
			String searchFilter = environment.getLdapRolesClass();

			EntryCursor rolesData = connection.search(searchBase, searchFilter, SearchScope.ONELEVEL);

			for (Entry entry : rolesData) {
				RoleDto roleDto = new RoleDto();
				roleDto.setRoleId(entry.get("cn").get().toString());
				roleDto.setRoleName(entry.get("cn").get().toString());
				roleDto.setRoleDescription(entry.get("description").get().toString());
				roleDtos.add(roleDto);
			}
			rolesListDto.setRoles(roleDtos);
			rolesData.close();
			connection.close();

			return rolesListDto;
		} catch (Exception e) {
			throw new RuntimeException(e + " Unable to fetch user roles from LDAP");
		}
	}

	public MosipUserListDto getListOfUsersDetails(List<String> users) throws Exception {
		try {
			MosipUserListDto userResponseDto = new MosipUserListDto();
			List<MosipUserDto> mosipUserDtos = new ArrayList<>();

			LdapConnection connection = createAnonymousConnection();

			for (String user : users) {
				Dn userdn = createUserDn(user);
				MosipUserDto data = lookupUserDetails(userdn, connection);
				if (data != null)
					mosipUserDtos.add(data);
			}

			connection.close();
			userResponseDto.setMosipUserDtoList(mosipUserDtos);
			return userResponseDto;
		} catch (Exception err) {
			throw new RuntimeException(err + " Unable to fetch user roles from LDAP");
		}
	}
}
