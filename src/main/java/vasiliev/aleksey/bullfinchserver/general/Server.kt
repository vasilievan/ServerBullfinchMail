package vasiliev.aleksey.bullfinchserver.general

import java.security.KeyPairGenerator
import java.security.SecureRandom
import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_FACTORY_ALGORITM
import vasiliev.aleksey.bullfinchserver.general.Constants.KEY_LENGTH

object Server {
    fun createKeyPairGenerator(secureRandom: SecureRandom): KeyPairGenerator {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_FACTORY_ALGORITM)
        keyPairGenerator.initialize(KEY_LENGTH, secureRandom)
        return keyPairGenerator
    }
}