package com.patil.coconut_pfa.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.patil.coconut_pfa.MainActivity
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.domain.CycleSummaryCalculator
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.util.MoneyFormatter
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class CoconutWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userSettingsDataStore = UserSettingsDataStore(context)
        val settings = userSettingsDataStore.settingsFlow.first()
        val showAmount = settings.showAmountOnWidget
        val startDay = settings.salaryCycleStartDay

        val database = CoconutDatabase.getInstance(context)
        val txns = database.txnDao().getAll()
        val period = SalaryCycleCalculator.calculateCycle(startDay, LocalDate.now())
        val totals = CycleSummaryCalculator.calculateCycleTotals(period.startDate, period.endDate, txns)
        val spentPaise = totals.spentPaise

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "add_transaction")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addAction = actionStartActivity(intent)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.Start
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.End
                    ) {
                        Text(
                            text = "Fortis",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        // + Add Button
                        Row(
                            modifier = GlanceModifier
                                .background(GlanceTheme.colors.primary)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable(addAction),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "+ Add",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (showAmount) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        val formattedSpent = MoneyFormatter.format(spentPaise, isPrivacyEnabled = false, showSymbol = true)
                        Text(
                            text = "Spent this cycle $formattedSpent",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        suspend fun updateAll(context: Context) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(CoconutWidget::class.java)
                for (glanceId in glanceIds) {
                    CoconutWidget().update(context, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
