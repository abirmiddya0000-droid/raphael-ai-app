package com.example.security

import com.example.data.PreferencesManager
import com.example.data.dao.AuditLogDao
import com.example.data.entities.AuditLogEntity

class OwnerAuthManager(
    private val preferencesManager: PreferencesManager,
    private val auditLogDao: AuditLogDao
) {
    val ownerName: String
        get() = preferencesManager.ownerName

    fun verifyPin(pin: String): Boolean {
        val matches = (pin == preferencesManager.ownerPin)
        return matches
    }

    suspend fun logSensitiveAction(action: String, details: String, authorized: Boolean) {
        auditLogDao.insertLog(
            AuditLogEntity(
                action = action,
                details = details,
                authorizedByOwner = authorized
            )
        )
    }

    fun updatePin(oldPin: String, newPin: String): Boolean {
        if (verifyPin(oldPin)) {
            preferencesManager.ownerPin = newPin
            return true
        }
        return false
    }
}
