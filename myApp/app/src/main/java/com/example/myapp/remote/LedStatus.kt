package com.example.myapp.remote


/**
 * LedStatus model
 */
class LedStatus {
    private var identifier: String? = null
    private var status: Boolean = false

    fun getIdentifier(): String? {
        return identifier
    }

    fun setIdentifier(identifier: String?): LedStatus {
        this.identifier = identifier
        return this
    }

    fun getStatus(): Boolean {
        return status
    }

    fun setStatus(status: Boolean): LedStatus {
        this.status = status
        return this
    }

    fun reverseStatus(): LedStatus {
        return setStatus(!status)
    }
}
