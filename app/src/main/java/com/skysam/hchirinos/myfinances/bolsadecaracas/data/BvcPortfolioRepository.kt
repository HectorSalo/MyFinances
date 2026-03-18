package com.skysam.hchirinos.myfinances.bolsadecaracas.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class BvcPortfolioConfig(
    val selectedSymbols: List<String> = emptyList(),
    val updatedAt: Timestamp? = null
)

class BvcPortfolioRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val docRef
        get() = firestore.collection(Constants.BD_BVC_PORTFOLIO)
            .document(Auth.uidCurrentUser())

    private val positionsRef
        get() = docRef.collection("positions")

    fun observePortfolioConfig(): Flow<Result<BvcPortfolioConfig?>> = callbackFlow {
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val selectedSymbols = (snapshot.get("selectedSymbols") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val updatedAt = snapshot.getTimestamp("updatedAt")
                trySend(Result.success(BvcPortfolioConfig(selectedSymbols, updatedAt)))
            } else {
                trySend(Result.success(null))
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun savePortfolioConfig(selectedSymbols: List<String>): Result<Unit> {
        val data = mapOf(
            "selectedSymbols" to selectedSymbols,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        return try {
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePositions(): Flow<Result<List<BvcPosition>>> = callbackFlow {
        val registration = positionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val positions = snapshot.documents.mapNotNull { doc ->
                    doc.toBvcPositionOrNull()
                }
                trySend(Result.success(positions))
            } else {
                trySend(Result.success(emptyList()))
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun savePosition(position: BvcPosition): Result<Unit> {
        return try {
            positionsRef.document(position.symbol)
                .set(position, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toBvcPositionOrNull(): BvcPosition? {
        val symbol = getString("symbol") ?: return null
        val quantity = getDouble("quantity") ?: 0.0
        val averagePrice = getDouble("averagePrice") ?: 0.0
        val updatedAt = getTimestamp("updatedAt")
        return BvcPosition(symbol, quantity, averagePrice, updatedAt)
    }
}
