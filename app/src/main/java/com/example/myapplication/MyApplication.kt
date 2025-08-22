package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.appModule // Importe seu módulo Koin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicia o Koin
        startKoin {
            // Log Koin no Logcat (útil para depuração)
            androidLogger()
            // Declara o contexto do Android para o Koin
            androidContext(this@MyApplication)
            // Carrega os seus módulos de dependência
            modules(appModule)
        }
    }
}