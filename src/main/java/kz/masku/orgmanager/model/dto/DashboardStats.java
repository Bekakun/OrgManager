package kz.masku.orgmanager.model.dto;

/** Aggregated statistics shown on the dashboard page. */
public record DashboardStats(
        long totalUsers,
        long activeUsers,
        long totalDocuments,
        long pendingDocuments,
        long approvedDocuments,
        long rejectedDocuments
) {}
