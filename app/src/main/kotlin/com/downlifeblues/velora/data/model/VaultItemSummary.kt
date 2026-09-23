package com.downlifeblues.velora.data.model

/** A category-agnostic projection used by Home, Search, and the vault list — never carries secrets. */
data class VaultItemSummary(
    val id: String,
    val category: VaultCategory,
    val title: String,
    val subtitle: String,
    val iconUrl: String?,
    val isFavorite: Boolean,
    val updatedAt: Long,
)
