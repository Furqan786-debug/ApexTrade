package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditLogEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminAuditLogsScreen(
    viewModel: TradingViewModel,
    auditLogs: List<AuditLogEntity>,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var exportDialogContent by remember { mutableStateOf<String?>(null) }
    var filterAction by remember { mutableStateOf("ALL") }

    val filtered = remember(auditLogs, filterAction) {
        if (filterAction == "ALL") auditLogs
        else auditLogs.filter { it.action.contains(filterAction) }
    }

    if (exportDialogContent != null) {
        AlertDialog(
            onDismissRequest = { exportDialogContent = null },
            title = {
                Text("Centralized Database Backup / Export", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Encrypted JSON database snapshot of users, ledgers, trades, and audit trail:", color = Slate400, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianSurface,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = exportDialogContent!!,
                            color = Slate300,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportDialogContent!!))
                        exportDialogContent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { exportDialogContent = null }) {
                    Text("Close", color = Slate400)
                }
            },
            containerColor = ObsidianCard
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Immutable Audit Logs & Backups",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Permanent non-repudiation ledger for all administrative actions",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = {
                    viewModel.exportDatabaseBackup { json ->
                        exportDialogContent = json
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("export_database_btn")
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Backup", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "DEPOSIT", "WITHDRAWAL", "TRADE", "BALANCE").forEach { act ->
                val isSelected = filterAction == act
                FilterChip(
                    selected = isSelected,
                    onClick = { filterAction = act },
                    label = { Text(act) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TronGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(log.action, color = TronGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(log.actorRole)
                            }
                            Text(
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(log.timestamp)),
                                color = Slate500,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Actor: ${log.actorId} • Target Entity: ${log.entityType} [${log.entityId}]", color = Slate400, fontSize = 11.sp)
                        Text("Reason: ${log.reason}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Prev: ${log.previousValue}", color = Slate500, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("New: ${log.newValue}", color = TradeGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("Client: ${log.clientInfo}", color = Slate500, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
