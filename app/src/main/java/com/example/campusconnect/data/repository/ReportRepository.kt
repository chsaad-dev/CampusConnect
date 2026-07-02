package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Report
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun submitReport(report: Report): Flow<Resource<String>>
    fun getAllReports(): Flow<Resource<List<Report>>>
    fun updateReportStatus(reportId: String, status: String): Flow<Resource<String>>
}
