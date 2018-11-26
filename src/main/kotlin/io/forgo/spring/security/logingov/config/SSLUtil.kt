package io.forgo.spring.security.logingov.config

import org.springframework.boot.autoconfigure.web.ServerProperties
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.xml.bind.DatatypeConverter

class SSLUtil(serverProperties: ServerProperties) {

    private val keyStore: String = serverProperties.ssl.keyStore
    private val keyStorePassword: String = serverProperties.ssl.keyStorePassword
    private val keyAlias: String = serverProperties.ssl.keyAlias
    //private val keyPassword: String = serverProperties.ssl.keyPassword
    private val keyStoreType: String = serverProperties.ssl.keyStoreType

    private val jksBuilder: KeyStore.Builder = KeyStore.Builder.newInstance(
            keyStoreType,
            null,
            File(keyStore),
            KeyStore.PasswordProtection(keyStorePassword.toCharArray())
    )
    private val jks: KeyStore = jksBuilder.keyStore
    private val protectionParameter: KeyStore.ProtectionParameter = jksBuilder.getProtectionParameter(keyAlias)
    private val privateKeyEntry: KeyStore.PrivateKeyEntry = jks.getEntry(keyAlias, protectionParameter) as KeyStore.PrivateKeyEntry
    private val privateKey: PrivateKey = privateKeyEntry.privateKey
    private val certificate: X509Certificate = privateKeyEntry.certificate as X509Certificate

    private fun printBase64(byteArray: ByteArray): String {
        certificate
        return DatatypeConverter.printBase64Binary(byteArray)
    }

    fun rsaPrivateKey(): RSAPrivateKey {
        return privateKey as RSAPrivateKey
    }

    fun rsaPublicKey(): RSAPublicKey {
        return certificate.publicKey as RSAPublicKey
    }

    fun base64PrivateKey(): String {
        return printBase64(privateKey.encoded)
    }

    fun base64PublicKey(): String {
        return printBase64(certificate.encoded)
    }
}