package com.colonydirect.dashboard

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface DailyMetricsSnapshotRepository : JpaRepository<DailyMetricsSnapshot, UUID> {
    fun findBySnapshotDate(snapshotDate: LocalDate): DailyMetricsSnapshot?
    fun findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate: LocalDate, endDate: LocalDate): List<DailyMetricsSnapshot>
}