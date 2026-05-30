package com.medicamentos.app.medremind.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.medicamentos.app.medremind.domain.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_ROL = stringPreferencesKey("rol")
    private val KEY_NOMBRE = stringPreferencesKey("nombre")

    suspend fun saveSession(userId: String, rol: Rol, nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_ROL] = rol.name
            prefs[KEY_NOMBRE] = nombre
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun updateNombre(nombre: String) {
        context.dataStore.edit { prefs -> prefs[KEY_NOMBRE] = nombre }
    }

    fun getUserId(): Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }

    fun getRol(): Flow<Rol?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROL]?.let { runCatching { Rol.valueOf(it) }.getOrNull() }
    }

    fun getNombre(): Flow<String?> = context.dataStore.data.map { it[KEY_NOMBRE] }

    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data.map { it[KEY_USER_ID] != null }
}
