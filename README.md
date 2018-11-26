First, you'll want to run:

`./gradlew jks`

The above command will run the [Gradle Keystore Plugin](https://plugins.gradle.org/plugin/io.forgo.keystoreplugin). And should generate the following directory and files:

```
.keystore/
    - debug.crt
    - debug.key
    - keystore.jks
    - keystore.pkcs12
```

You'll want to make sure you have some variables in sync with the [Login.gov Dashboard](https://dashboard.int.identitysandbox.gov/) before running the application.

In your test app you'll want to configure the following settings:

**Identity protocol**: "Openid connect"

**Issuer**: This value cannot be changed after initially creating the test app. It will need to match the following configuration

`spring.security.oauth2.client.registration.logingov.client-id`

**Public key**: Copy the contents of `.keystore/debug.crt` you generated from the `jks` task. Don't run the `jks` task again after setting this on the dashboard; otherwise, your development cert and the cert you publish with login.gov will not match up.

**Redirect URIs**:

```
https://localhost:8080/login/oauth2/code/logingov
https://localhost:8080/authorize/oauth2/code/logingov
https://localhost:8443/oauth2/authorization/logingov
```

**Attribute bundle**: If you are leveraging LOA1 context (only need a UUID and email), make sure you only check `email` from this list.

The value "logingov" as the `<registrationId>` of our Spring Security autoconfiguration is arbitrary. If you really need to change this, change it in the `application.yml` and also the constant `LOGIN_GOV_REGISTRATION_ID` in the `LoginGovConstants` file. Your `Redirect URIs` registered on your login.gov test app will also need to reflect this change, as those paths are generated from a template.

#### Run the app:

`./gradlew bootrun`

`https://localhost:8080`