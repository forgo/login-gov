### Instructions

**1. Run the [Gradle Keystore Plugin](https://plugins.gradle.org/plugin/io.forgo.keystoreplugin):**

`./gradlew jks`

Output:
```
.keystore/
    - debug.crt
    - debug.key
    - logingov_keystore.jks
    - keystore.pkcs12
```

**2. (OPTIONAL) Publish test app with Login.gov:**

Ensure your configuration is in sync with the [Login.gov Dashboard](https://dashboard.int.identitysandbox.gov/) before running the application.

Creating your test app, you'll want to configure the following:

- **Identity protocol**: "Openid connect"
- **Issuer**: "urn:gov:gsa:openidconnect.profiles:sp:sso:_\<your organization\>_:_\<your app name\>_"
- **Public key**: Copy the contents of `.keystore/debug.crt` from the `jks` task. Don't run the `jks` task again after setting this on the dashboard; otherwise, your development key (used to sign the _client\_assertion_ JWT in the token request) will not match up with the public cert published to login.gov .
- **Redirect URIs**:
  - `http://localhost:8080/authorize/oauth2/code/logingov`
  - `http://localhost:8080/login/oauth2/code/logingov`
- **Attribute bundle**: If you are leveraging LOA1 context (only need a UUID and email), make sure you only check `email` from this list.

**NOTE:** The value "logingov" as the `<registrationId>` of our Spring Security autoconfiguration is arbitrary. If you really need to change this, change it in the `application.yml` and also the constant `LOGIN_GOV_REGISTRATION_ID` in the `LoginGovConstants` file. Your `Redirect URIs` registered on your login.gov test app will also need to reflect this change, as those paths are generated from a template.

#### Run the app:

`./gradlew bootrun`

`https://localhost:8080`
