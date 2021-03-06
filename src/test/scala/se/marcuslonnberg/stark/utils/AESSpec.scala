package se.marcuslonnberg.stark.utils

import org.scalatest.{Matchers, FreeSpec}

class AESSpec extends FreeSpec with Matchers {
  "Encrypt and decrypt should " - {
    val message = "Secret message"
    val key = AES.generateKey(128)

    val encryptedMessage = AES.encrypt(message, key)
    val decryptedMessage = AES.decrypt(encryptedMessage, key)

    decryptedMessage shouldEqual message
  }
}
