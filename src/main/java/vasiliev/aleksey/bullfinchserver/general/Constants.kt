package vasiliev.aleksey.bullfinchserver.general

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object Constants {
    const val SECRET_KEY_FACTORY_ALGORITM = "PBKDF2WithHmacSHA1"
    const val MAIN_DIR = "/root/BullfinchMail"
    val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    const val KEY_LENGTH = 1024
    const val EXTENDED_KEY_LENGTH = 2048
    const val SALT_SIZE = 16
    const val MESSAGE_DIGEST_ALGORITM = "SHA-256"
    const val CIPHER_ALGORITM = "RSA/ECB/PKCS1Padding"
    const val KEY_FACTORY_ALGORITM = "RSA"
}