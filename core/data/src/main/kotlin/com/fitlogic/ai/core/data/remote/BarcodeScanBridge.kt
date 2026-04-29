package com.fitlogic.ai.core.data.remote

import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import com.google.mlkit.vision.barcode.common.Barcode
import javax.inject.Inject
import javax.inject.Singleton

data class BarcodePayload(
    val rawValue: String,
    val format: Int,
)

interface BarcodeScanDataSource {
    fun toPayload(barcode: Barcode): Result<BarcodePayload>
}

@Singleton
class MlKitBarcodeScanDataSource
    @Inject
    constructor() : BarcodeScanDataSource {
        override fun toPayload(barcode: Barcode): Result<BarcodePayload> =
            runCatching {
                val rawValue = barcode.rawValue?.trim().orEmpty()
                check(rawValue.isNotBlank()) { "Barkod okunamadi." }
                BarcodePayload(
                    rawValue = rawValue,
                    format = barcode.format,
                )
            }
    }

interface NutritionBarcodeGateway {
    suspend fun scan(barcode: Barcode): Result<FoodCatalogItem?>
}

@Singleton
class NutritionBarcodeGatewayImpl
    @Inject
    constructor(
        private val barcodeScanDataSource: BarcodeScanDataSource,
        private val nutritionRepository: NutritionRepository,
    ) : NutritionBarcodeGateway {
        override suspend fun scan(barcode: Barcode): Result<FoodCatalogItem?> =
            runCatching {
                val payload = barcodeScanDataSource.toPayload(barcode).getOrThrow()
                nutritionRepository.scanBarcode(payload.rawValue).getOrThrow()
            }
    }
