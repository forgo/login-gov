## Instructions

**1. Run the [Gradle Keystore Plugin](https://plugins.gradle.org/plugin/io.forgo.keystoreplugin):**

`./gradlew jks`

```
.keystore/
    - debug.crt
    - debug.key
    - logingov_keystore.jks
    - keystore.pkcs12
```

**2. Review configuration:**

Note the `ext` block of the `build.gradle` contains variables used to populate some configuration. This is accomplished via the `processResources` task which expands any project properties into the `application.yml`.

For the purposes of demonstation, you shouldn't need to change any values here (possibly `clientId`, see step #3). Once you better understand these variables, you may decide to change them for your use case:

- `clientId`: a unique identifier for your application, published to Login.gov
- `allowedOrigin`: specify the origin that you want to allow cross-domain requests from
- `loginSuccessRedirect`: specify the URL the user will be redirected to after a successful login
- `logoutSuccessRedirect`: specify the URL the user will be redirected to after a successful logout

- `keystoreOutputDir`: output directory of the keystore gradle plugin
- `keystoreFile`: filename of the JKS keystore generated by the keystore gradle plugin
- `keystoreAlias`: alias tied to the key of the generated keystore
- `keystorePassword`: the password used to generate the keystore



**3. Review Login.gov OpenID documentation:**

To better understand the specification which drove this project, refer to the [OpenID Developer Guide](https://developers.login.gov/oidc/) provided by Login.gov.



**4. (OPTIONAL) Publish test app on the [Login.gov Dashboard](https://dashboard.int.identitysandbox.gov/):**

As long as the test data originally published for this demo app remains in the Login.gov sandbox, you should not need to change these settings.

If you have developer access and wish to register your own test app, ensure your configuration is in sync before running the application. You'll want to configure the following on the dashboard:

- **Identity protocol**: "Openid connect"
- **Issuer**: "urn:gov:gsa:openidconnect.profiles:sp:sso:_\<your organization\>_:_\<your app name\>_"

  - The issuer should match the `clientId` gradle build variable, assigned here in your Spring config:
`spring.security.oauth2.client.registration.logingov.client-id: ${clientId}`


- **Public key**: Copy the contents of `.keystore/debug.crt` from the `jks` task. Don't run the `jks` task again after setting this on the dashboard; otherwise, your development key (used to sign the _client\_assertion_ JWT in the token request) will not match up with the public cert published to login.gov .
- **Redirect URIs**:
  - `http://localhost:8080/authorize/oauth2/code/logingov`
  - `http://localhost:8080/login/oauth2/code/logingov`
- **Attribute bundle**: If you are leveraging LOA1 context (only need a UUID and email), make sure you only check `email` from this list.

**NOTE:** The value "logingov" as the `<registrationId>` of our Spring Security autoconfiguration is arbitrary. If you really need to change this, change it in the `application.yml` and also the constant `LOGIN_GOV_REGISTRATION_ID` in the `LoginGovConstants` file. Your `Redirect URIs` registered on your login.gov test app will also need to reflect this change, as those paths are generated from a template.



**5. Run the application:**

`./gradlew bootrun`

`https://localhost:8080`
