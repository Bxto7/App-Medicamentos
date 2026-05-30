package com.medicamentos.app.medremind.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestiona el estado Premium de la aplicación.
 * El código de activación se valida localmente y se persiste en
 * EncryptedSharedPreferences con clave en Android Keystore.
 *
 * Código de activación válido: DIABETRACK2026
 */
class PremiumManager(context: Context) {

    companion object {
        private const val PREF_FILE = "premium_prefs"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_ACTIVATED_CODE = "activated_code"

        // Códigos de activación válidos
        private val VALID_CODES = setOf("DIABETRACK2026", "DIABETRACK2026BETA")
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREF_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPremium(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)

    /**
     * Activa Premium con el código proporcionado.
     * @return true si el código es válido y Premium fue activado.
     */
    fun activatePremium(code: String): Boolean {
        val normalized = code.trim().uppercase()
        if (normalized in VALID_CODES) {
            prefs.edit()
                .putBoolean(KEY_IS_PREMIUM, true)
                .putString(KEY_ACTIVATED_CODE, normalized)
                .apply()
            return true
        }
        return false
    }

    fun deactivatePremium() {
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, false)
            .remove(KEY_ACTIVATED_CODE)
            .apply()
    }

    fun getActivatedCode(): String? = prefs.getString(KEY_ACTIVATED_CODE, null)
}
