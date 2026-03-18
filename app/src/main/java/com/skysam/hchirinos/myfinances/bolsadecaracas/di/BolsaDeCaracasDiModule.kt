package com.skysam.hchirinos.myfinances.bolsadecaracas.di

import com.skysam.hchirinos.myfinances.bolsadecaracas.data.BolsaDeCaracasRepositoryImpl
import com.skysam.hchirinos.myfinances.bolsadecaracas.domain.BolsaDeCaracasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BolsaDeCaracasDiModule {

    @Binds
    abstract fun bindBolsaDeCaracasRepository(
        impl: BolsaDeCaracasRepositoryImpl
    ): BolsaDeCaracasRepository
}

