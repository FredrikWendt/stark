package se.marcuslonnberg.stark

import java.io._
import java.security._
import java.security.cert.{Certificate, CertificateFactory}
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import org.apache.commons.ssl.PKCS8Key
import spray.io.{SSLContextProvider, ServerSSLEngineProvider}

trait SSLSupport {
  val algorithm = "SunX509"
  val protocol = "TLS"

  implicit def sslEngineProvider(implicit context: SSLContextProvider): ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array(
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
        "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA"))

      engine.setEnabledProtocols(Array("TLSv1.2", "TLSv1.1", "TLSv1"))
      engine
    }
  }

  def createSSLContext(certificateFilename: String, privateKeyFilename: String): SSLContext = {
    val keyStore = KeyStore.getInstance("JKS")
    val entryAlias = "entry"
    val cert = readCert(certificateFilename)
    val privateKey = readPrivateKey(privateKeyFilename)
    val emptyPassword = "".toCharArray

    keyStore.load(null, emptyPassword)

    keyStore.setEntry(entryAlias, new KeyStore.PrivateKeyEntry(privateKey, cert), new KeyStore.PasswordProtection(emptyPassword))

    val keyManagerFactory = KeyManagerFactory.getInstance(algorithm)
    keyManagerFactory.init(keyStore, emptyPassword)

    val trustManagerFactory = TrustManagerFactory.getInstance(algorithm)
    trustManagerFactory.init(keyStore)

    val context = SSLContext.getInstance(protocol)
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  def readCert(certificateFilename: String): Array[Certificate] = {
    val certificateStream = new FileInputStream(certificateFilename)
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certs = certificateFactory.generateCertificates(certificateStream)

    var chain = new Array[Certificate](certs.size())
    chain = certs.toArray(chain)
    certificateStream.close()
    chain
  }

  def readPrivateKey(privateKeyFilename: String): PrivateKey = {
    val fileStream = new FileInputStream(privateKeyFilename)
    val key = new PKCS8Key(fileStream, Array.empty[Char]) // Empty password
    val encodedKey = key.getDecryptedBytes
    val rsaKeyFactory = KeyFactory.getInstance("RSA")
    rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey))
  }
}
