package se.marcuslonnberg.stark.utils

import org.apache.commons.codec.binary.Base64
import spray.http.{HttpHeader, HttpResponse}

object Implicits {

  implicit class Base64String(val str: String) extends AnyVal {
    def fromBase64 = new Base64().decode(str)
  }

  implicit class Base64ByteArray(val arr: Array[Byte]) extends AnyVal {
    def toBase64String = new Base64().encodeAsString(arr)
  }

  implicit class RichHttpResponse(val response: HttpResponse) extends AnyVal {
    def +(header: HttpHeader) = response.copy(headers = header :: response.headers)
  }

}
