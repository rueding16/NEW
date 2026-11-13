package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.DarkAccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.RealtimeSyncState
import com.example.ui.viewmodel.RoleViewMode

@Composable
fun CrediPulseTopBar(
    user: UserEntity?,
    isDarkTheme: Boolean,
    unreadNotificationCount: Int,
    activeRoleMode: RoleViewMode,
    syncState: RealtimeSyncState? = null,
    onTriggerSyncPulse: (() -> Unit)? = null,
    onOpenCommunications: (() -> Unit)? = null,
    onOpenDocumentVault: (() -> Unit)? = null,
    onOpenArchitecture: (() -> Unit)? = null,
    onToggleTheme: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSwitchRoleMode: (RoleViewMode) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_top_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "CrediPulse Shield",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CrediPulse",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Role Badge
                            val role = user?.role ?: "GUEST"
                            val isUserAdmin = role == "ADMIN"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isUserAdmin) DarkAccentGold.copy(alpha = 0.2f)
                                        else PrimaryRed.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUserAdmin) "ADMIN" else "CUSTOMER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserAdmin) DarkAccentGold else PrimaryRed
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Live Online Real-time Sync Indicator
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .clickable { onTriggerSyncPulse?.invoke() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("online_realtime_badge"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (user != null) user.fullName else "Realtime Financial Platform",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Real-time Sync Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                // Action Icons (Theme, Sync, Vault, Comms, Arch, Notifications, Logout)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // BaaS Architecture Guide
                    if (onOpenArchitecture != null) {
                        IconButton(
                            onClick = onOpenArchitecture,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("baas_architecture_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "BaaS & Multi-Channel Architecture",
                                tint = DarkAccentGold
                            )
                        }
                    }

                    // Per-Account Document Vault Button
                    if (user != null && onOpenDocumentVault != null) {
                        IconButton(
                            onClick = onOpenDocumentVault,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("open_vault_topbar_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = "Per-Account Document Vault",
                                tint = PrimaryRed
                            )
                        }
                    }

                    // Multi-Channel Communications Gateway Button
                    if (user != null && onOpenCommunications != null) {
                        IconButton(
                            onClick = onOpenCommunications,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("open_comms_topbar_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Multi-Channel Gateway Logs",
                                tint = SuccessGreen
                            )
                        }
                    }

                    // Real-time Sync Pulse button
                    IconButton(
                        onClick = { onTriggerSyncPulse?.invoke() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("realtime_sync_pulse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Trigger Real-time Sync Pulse",
                            tint = SuccessGreen
                        )
                    }

                    // Theme Toggle Button
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = if (isDarkTheme) DarkAccentGold else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Notification Button with badge
                    if (user != null) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(
                                            containerColor = PrimaryRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Logout Button
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // If user is Admin, provide direct accessible tabs/switch between Admin Dashboard and Customer Preview Dashboard
            if (user?.role == "ADMIN") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("admin_role_switcher"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Admin Mode:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilterChip(
                        selected = activeRoleMode == RoleViewMode.ADMIN_PANEL,
                        onClick = { onSwitchRoleMode(RoleViewMode.ADMIN_PANEL) },
                        label = { Text("Admin Controls", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryRed,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("switch_to_admin_panel")
                    )

                    FilterChip(
                        selected = activeRoleMode == RoleViewMode.CUSTOMER_DASHBOARD,
                        onClick = { onSwitchRoleMode(RoleViewMode.CUSTOMER_DASHBOARD) },
                        label = { Text("Customer View", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkAccentGold,
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black
                        ),
                        modifier = Modifier.testTag("switch_to_customer_preview")
                    )
                }
            }
        }
    }
}
