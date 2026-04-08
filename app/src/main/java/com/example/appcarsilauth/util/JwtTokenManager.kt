package com.example.appcarsilauth.util

import android.util.Base64
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

object JwtTokenManager {
    // Clave secreta fuerte simulada (en entorno real estaría en BuildConfig protegida)
    private const val SECRET_KEY = "CARS1L_S3CUR1TY_P0L1CY_K3Y_M1L1T4RY_GR4D3_2026"
    
    fun generateToken(email: String, roleId: Int, idUsuario: Int): String {
        // 1. Header
        val headerJson = JSONObject()
        headerJson.put("alg", "HS256")
        headerJson.put("typ", "JWT")
        val headerBase64 = encodeBase64Url(headerJson.toString())

        // 2. Payload
        val payloadJson = JSONObject()
        val issueTime = System.currentTimeMillis() / 1000
        val expTime = issueTime + (60 * 60 * 8) // 8 horas de sesión
        
        payloadJson.put("sub", idUsuario)
        payloadJson.put("email", email)
        payloadJson.put("roleId", roleId)
        payloadJson.put("iat", issueTime)
        payloadJson.put("exp", expTime)
        val payloadBase64 = encodeBase64Url(payloadJson.toString())

        // 3. Signature
        val signatureData = "$headerBase64.$payloadBase64"
        val signature = calculateHmacSha256(signatureData, SECRET_KEY)
        val signatureBase64 = encodeBase64UrlBytes(signature)

        return "$headerBase64.$payloadBase64.$signatureBase64"
    }

    fun isTokenValid(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            
            val signatureData = "${parts[0]}.${parts[1]}"
            val expectedSignature = encodeBase64UrlBytes(calculateHmacSha256(signatureData, SECRET_KEY))
            
            if (parts[2] != expectedSignature) return false
            
            // Verificar expiración
            val payloadString = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val payloadJson = JSONObject(payloadString)
            val exp = payloadJson.getLong("exp")
            val currentTime = System.currentTimeMillis() / 1000
            
            return currentTime < exp
        } catch (e: Exception) {
            return false
        }
    }

    private fun encodeBase64Url(data: String): String {
        return Base64.encodeToString(data.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun encodeBase64UrlBytes(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun calculateHmacSha256(data: String, key: String): ByteArray {
        val hmacSha256 = "HmacSHA256"
        val mac = Mac.getInstance(hmacSha256)
        val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), hmacSha256)
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }
}
