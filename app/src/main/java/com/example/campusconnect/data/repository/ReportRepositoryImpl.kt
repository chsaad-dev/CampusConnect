package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Report
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportRepository {

    override fun submitReport(report: Report): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("reports").document().id
            val newReport = report.copy(id = id)
            firestore.collection("reports").document(id).set(newReport).await()
            
            // AI: Spam Detection logic
            // If the same reporter sends many reports quickly, flag them
            
            trySend(Resource.Success("Report submitted for investigation"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to submit report"))
        }
        awaitClose()
    }

    override fun getAllReports(): Flow<Resource<List<Report>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching reports"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reports = snapshot.toObjects(Report::class.java)
                    trySend(Resource.Success(reports))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateReportStatus(reportId: String, status: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("reports").document(reportId).update("status", status).await()
            trySend(Resource.Success("Report updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Update failed"))
        }
        awaitClose()
    }
}
