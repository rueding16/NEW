package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.GoogleSignInHelper
import com.example.data.model.LoanApplicationEntity
import com.example.data.model.RepaymentScheduleEntity
import com.example.ui.components.CrediPulseTopBar
import com.example.ui.components.DisbursementWithdrawDialog
import com.example.ui.components.LoanApplicationDialog
import com.example.ui.components.NotificationDialog
import com.example.ui.components.PaymentGatewayDialog
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.theme.CrediPulseTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.CrediPulseViewModel
import com.example.ui.viewmodel.RoleViewMode
import com.example.util.configureAppCheck
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CrediPulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureAppCheck(this, intent)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            CrediPulseTheme(darkTheme = isDarkTheme) {
                MainContent(viewModel = viewModel, isDarkTheme = isDarkTheme)
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: CrediPulseViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeViewMode by viewModel.activeViewMode.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    val userLoans by viewModel.userLoans.collectAsStateWithLifecycle()
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val userSchedules by viewModel.userSchedules.collectAsStateWithLifecycle()
    val adminNotifs by viewModel.adminNotifications.collectAsStateWithLifecycle()
    val customerNotifs by viewModel.customerNotifications.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val lastTransaction by viewModel.lastTransaction.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    // Dialog state
    var showApplyLoanDialog by remember { mutableStateOf(false) }
    var loanToWithdraw by remember { mutableStateOf<LoanApplicationEntity?>(null) }
    var scheduleToPay by remember { mutableStateOf<RepaymentScheduleEntity?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val notifications = if (currentUser?.role == "ADMIN") adminNotifs else customerNotifs
    val unreadNotifsCount = notifications.count { !it.isRead }

    // If Admin is in Customer Preview, back button returns to Admin Panel
    if (currentUser?.role == "ADMIN" && activeViewMode == RoleViewMode.CUSTOMER_DASHBOARD) {
        BackHandler {
            viewModel.switchViewMode(RoleViewMode.ADMIN_PANEL)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentUser != null) {
                CrediPulseTopBar(
                    user = currentUser,
                    isDarkTheme = isDarkTheme,
                    unreadNotificationCount = unreadNotifsCount,
                    activeRoleMode = activeViewMode,
                    syncState = syncState,
                    onTriggerSyncPulse = { viewModel.triggerManualSyncPulse() },
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onOpenNotifications = { showNotificationDialog = true },
                    onSwitchRoleMode = { viewModel.switchViewMode(it) },
                    onLogout = { viewModel.logout() },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main View Routing
            if (currentUser == null) {
                AuthScreen(
                    onLogin = { u, p ->
                        viewModel.login(u, p) {}
                    },
                    onRegister = { u, p, name, email, phone, role, idType, idNum, emp, comp, inc, proof ->
                        viewModel.register(u, p, name, email, phone, role, idType, idNum, emp, comp, inc, proof) {}
                    },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onGoogleSignIn = {
                        coroutineScope.launch {
                            val res = GoogleSignInHelper.signInWithGoogle(context)
                            res.onSuccess { firebaseUser ->
                                viewModel.handleGoogleSignIn(firebaseUser) {}
                            }.onFailure { err ->
                                viewModel.showFeedback(err.message ?: "Google Sign-In was cancelled or failed", isError = true)
                            }
                        }
                    }
                )
            } else if (currentUser?.role == "ADMIN" && activeViewMode == RoleViewMode.ADMIN_PANEL) {
                AdminDashboardScreen(
                    adminUser = currentUser!!,
                    allLoans = allLoans,
                    allCustomers = allCustomers,
                    allTransactions = allTransactions,
                    allAuditLogs = allAuditLogs,
                    syncState = syncState,
                    onSimulateIncomingLoan = { viewModel.simulateRealtimeIncomingLoan() },
                    onTriggerSyncPulse = { viewModel.triggerManualSyncPulse() },
                    onApproveLoan = { id, remarks -> viewModel.approveLoan(id, remarks) },
                    onRejectLoan = { id, remarks -> viewModel.rejectLoan(id, remarks) },
                    onUpdateKycStatus = { id, status, reason -> viewModel.updateKycStatus(id, status, reason) }
                )
            } else {
                // Customer Dashboard (Used by Customers, and can also be previewed by Admin)
                val assessment = viewModel.evaluateRealtimeRisk(
                    amount = userLoans.firstOrNull()?.principalAmount ?: 25000.0,
                    tenor = userLoans.firstOrNull()?.tenorMonths ?: 6,
                    user = currentUser,
                    loans = userLoans,
                    schedules = userSchedules
                )

                CustomerDashboardScreen(
                    user = currentUser!!,
                    loans = userLoans,
                    schedules = userSchedules,
                    transactions = userTransactions,
                    creditAssessment = assessment,
                    syncState = syncState,
                    onTriggerSyncPulse = { viewModel.triggerManualSyncPulse() },
                    onApplyLoan = { showApplyLoanDialog = true },
                    onWithdrawBalance = { loan -> loanToWithdraw = loan },
                    onPaySchedule = { schedule -> scheduleToPay = schedule }
                )
            }

            // Top Feedback Banner Toast
            AnimatedVisibility(
                visible = feedback != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                feedback?.let { fb ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (fb.isError) Color(0xFFE11D48) else SuccessGreen
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("ui_feedback_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (fb.isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = fb.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            )
                            IconButton(
                                onClick = { viewModel.clearFeedback() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showApplyLoanDialog) {
        LoanApplicationDialog(
            initialMaxLimit = 75000.0,
            onCalculateRisk = { amt, tenor -> viewModel.evaluateRealtimeRisk(amt, tenor) },
            onSubmit = { type, amt, tenor, purpose ->
                viewModel.applyForLoan(type, amt, tenor, purpose) {
                    showApplyLoanDialog = false
                }
            },
            onDismiss = { showApplyLoanDialog = false }
        )
    }

    loanToWithdraw?.let { loan ->
        DisbursementWithdrawDialog(
            loan = loan,
            defaultRecipientName = currentUser?.fullName ?: "",
            onConfirmDisbursement = { amt, channel, destType, targetAcc, targetName ->
                viewModel.withdrawBalance(
                    loanId = loan.id,
                    amount = amt,
                    channel = channel,
                    destinationType = destType,
                    targetAccount = targetAcc,
                    targetAccountName = targetName
                ) {
                    loanToWithdraw = null
                }
            },
            onDismiss = { loanToWithdraw = null }
        )
    }

    scheduleToPay?.let { schedule ->
        PaymentGatewayDialog(
            schedule = schedule,
            defaultCustomerName = currentUser?.fullName ?: "",
            onConfirmPayment = { schedId, method, channel, acc, amt ->
                viewModel.processRepayment(
                    scheduleId = schedId,
                    method = method,
                    channelName = channel,
                    accountIdentifier = acc,
                    amountToPay = amt
                ) {
                    scheduleToPay = null
                }
            },
            onDismiss = { scheduleToPay = null }
        )
    }

    lastTransaction?.let { tx ->
        TransactionReceiptDialog(
            transaction = tx,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onMarkRead = { viewModel.markNotificationRead(it) },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismiss = { showNotificationDialog = false }
        )
    }
}
