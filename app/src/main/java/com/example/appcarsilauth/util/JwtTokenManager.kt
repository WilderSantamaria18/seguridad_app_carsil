package com.example.appcarsilauth.util

import android.util.Base64
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

object JwtTokenManager {
    private const val SECRET_KEY = "CARS1L_S3CUR1TY_P0L1CY_K3Y_M1L1T4RY_GR4D3_2026"
    
    fun generateToken(email: String, roleId: Int, idUsuario: Int): String {
        val iat = System.currentTimeMillis()
        val exp = iat + (2 * 60 * 1000) // 2 Minutos para pruebas (Antes 15)
        
        val headerJson = JSONObject()
        headerJson.put("alg", "HS256")
        headerJson.put("typ", "JWT")
        val headerBase64 = encodeBase64Url(headerJson.toString())

        val payloadJson = JSONObject()
        payloadJson.put("sub", idUsuario)
        payloadJson.put("email", email)
        payloadJson.put("roleId", roleId)
        payloadJson.put("iat", iat)
        payloadJson.put("exp", exp)
        val payloadBase64 = encodeBase64Url(payloadJson.toString())

        val signatureData = "$headerBase64.$payloadBase64"
        val signature = calculateHmacSha256(signatureData, SECRET_KEY)
        val signatureBase64 = encodeBase64UrlBytes(signature)

        return "$headerBase64.$payloadBase64.$signatureBase64"
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            
            val signatureData = "${parts[0]}.${parts[1]}"
            val expectedSignature = encodeBase64UrlBytes(calculateHmacSha256(signatureData, SECRET_KEY))
            
            if (parts[2] != expectedSignature) return false

            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payloadJson)
            val exp = json.getLong("exp")
            
            System.currentTimeMillis() < exp
        } catch (e: Exception) {
            false
        }
    }
    
    fun getRemainingTimeMs(token: String): Long {
        return try {
            val parts = token.split(".")
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payloadJson)
            val exp = json.getLong("exp")
            (exp - System.currentTimeMillis()).coerceAtLeast(0)
        } catch (e: Exception) {
            0L
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
