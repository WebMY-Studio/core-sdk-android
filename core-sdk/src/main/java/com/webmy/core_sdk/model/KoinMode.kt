package com.webmy.core_sdk.model

enum class KoinMode {

    /**
     * Use this parameter if you use another DI in your app
     * or you do not use DI at all
     */
    START,

    /**
     * Use this parameter if you use Koin as your DI library
     * and startKoin() is already called in your app
     */
    LOAD
}