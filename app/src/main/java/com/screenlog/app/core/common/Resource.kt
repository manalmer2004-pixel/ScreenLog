package com.screenlog.app.core.common

sealed class Resource<T>(open val data: T? = null, open val message: String? = null) {
    class Success<T>(override val data: T) : Resource<T>(data)
    class Error<T>(override val message: String, override val data: T? = null) : Resource<T>(data, message)
    class Loading<T>(override val data: T? = null) : Resource<T>(data)
}
