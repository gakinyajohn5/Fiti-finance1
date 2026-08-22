package com.fitifinance.comrade.ui.insights
import androidx.compose.ui.text.drawText

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitifinance.comrade.viewmodel.SpendPoint
import com.fitifinance.comrade.viewmodel.SpendingChartViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

private enum class ChartTab { DAILY, WEEKLY }

@Composable
fun SpendingChartScreen(onBack: () -> Unit) {
    val vm: SpendingChartViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var tab by remember { mutableStateOf(ChartTab.DAILY) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Spending Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        TabRow(selectedTabIndex = tab.ordinal) {
            Tab(selected = tab == ChartTab.DAILY, onClick = { tab = ChartTab.DAILY }, text = { Text("Daily (7 days)") })
            Tab(selected = tab == ChartTab.WEEKLY, onClick = { tab = ChartTab.WEEKLY }, text = { Text("Weekly (8 weeks)") })
        }

        Spacer(Modifier.height(24.dp))

        val points = if (tab == ChartTab.DAILY) state.daily else state.weekly
        val maxValue = if (tab == ChartTab.DAILY) state.dailyMax else state.weeklyMax
        val total = points.sumOf { it.totalKes }

        Text(
            "KSh ${total.toInt()} total",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (tab == ChartTab.DAILY) "across the last 7 days" else "across the last 8 weeks",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            SpendBarChart(
                points = points,
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SpendBarChart(points: List<SpendPoint>, maxValue: Double, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

    Canvas(modifier = modifier) {
        val chartHeight = size.height - 24.dp.toPx() // leave room for x-axis labels
        val barCount = points.size.coerceAtLeast(1)
        val spacing = 8.dp.toPx()
        val barWidth = ((size.width - spacing * (barCount + 1)) / barCount).coerceAtLeast(4f)
        val safeMax = if (maxValue <= 0.0) 1.0 else maxValue

        // Horizontal gridlines
        repeat(4) { i ->
            val y = chartHeight * i / 4f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        points.forEachIndexed { index, point ->
            val barHeight = (point.totalKes / safeMax * chartHeight).toFloat().coerceAtLeast(2f)
            val x = spacing + index * (barWidth + spacing)
            val y = chartHeight - barHeight

            drawRoundRect(
                color = if (point.isCurrent) primary else primary.copy(alpha = 0.55f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            val labelResult = textMeasurer.measure(point.label, style = labelStyle)
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(
                    x + barWidth / 2f - labelResult.size.width / 2f,
                    chartHeight + 6.dp.toPx()
                )
            )
        }

        // Baseline
        drawLine(muted, Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 1.dp.toPx())
    }
}
