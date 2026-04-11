package com.example.appcarsilauth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape

@Composable
fun StatCard(
    label: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    iconBgColor: Color = CarsilColors.PrimaryLight,
    iconTintColor: Color = CarsilColors.Primary,
    trend: String? = null,
    isTrendUp: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(240.dp)
            .padding(4.dp),
        shape = CarsilShapes.Small,
        color = CarsilColors.Surface,
        border = border(1.dp, CarsilColors.Stroke)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarsilColors.TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBgColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CarsilColors.TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                
                if (trend != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .background(
                                if (isTrendUp) CarsilColors.SuccessLight else CarsilColors.DangerLight,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isTrendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isTrendUp) CarsilColors.Success else CarsilColors.Danger,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = trend,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subLabel,
                fontSize = 11.sp,
                color = CarsilColors.TextMuted
            )
        }
    }
}



@Composable
fun CarsilBarChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = CarsilColors.Primary,
    maxRange: Float? = null,
    showYAxis: Boolean = true,
    unit: String = "S/"
) {
    val maxValue = maxRange ?: (data.maxOrNull()?.takeIf { it > 0f } ?: 100f)
    var selectedIndex by remember { mutableStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    
    // Animation state
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }

    val animatedProgress = data.map { value ->
        animateFloatAsState(
            targetValue = if (animationStarted) value / maxValue else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "barAnimation"
        )
    }
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val canvasWidth = size.width
                        val paddingLeft = if (showYAxis) 40.dp.toPx() else 0f
                        val chartWidth = canvasWidth - paddingLeft
                        val barCount = data.size
                        if (barCount == 0) return@detectTapGestures
                        
                        val barWidth = (chartWidth / (barCount * 1.5f))
                        val spacing = (chartWidth - (barCount * barWidth)) / (barCount + 1)
                        
                        var foundIndex = -1
                        for (i in 0 until barCount) {
                            val left = paddingLeft + spacing + (i * (barWidth + spacing))
                            if (offset.x >= left && offset.x <= left + barWidth) {
                                foundIndex = i
                                break
                            }
                        }
                        selectedIndex = if (selectedIndex == foundIndex) -1 else foundIndex
                    }
                }
        ) {
            val canvasHeight = size.height
            val canvasWidth = size.width
            val paddingLeft = if (showYAxis) 40.dp.toPx() else 0f
            val paddingBottom = 20.dp.toPx()
            val chartHeight = canvasHeight - paddingBottom
            val chartWidth = canvasWidth - paddingLeft
            
            val barCount = data.size
            if (barCount == 0) return@Canvas
            
            val barWidth = (chartWidth / (barCount * 1.25f))
            val spacing = (chartWidth - (barCount * barWidth)) / (barCount + 1)

            // Y Axis Labels
            if (showYAxis) {
                val steps = 4
                for (i in 0..steps) {
                    val yLabel = (maxValue * i / steps).toInt()
                    val yPos = chartHeight - (chartHeight * i / steps)
                    
                    // Grid lines
                    if (i > 0) {
                        drawLine(
                            color = CarsilColors.Stroke.copy(alpha = 0.5f),
                            start = Offset(paddingLeft, yPos),
                            end = Offset(canvasWidth, yPos),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    drawText(
                        textMeasurer = textMeasurer,
                        text = if (yLabel >= 1000) "${yLabel/1000}k" else "$yLabel",
                        style = androidx.compose.ui.text.TextStyle(
                            color = CarsilColors.TextMuted,
                            fontSize = 10.sp
                        ),
                        topLeft = Offset(0f, yPos - 14.sp.toPx()/2)
                    )
                }
            }
            
            data.forEachIndexed { index, value ->
                val barProgress = animatedProgress[index].value
                val barHeight = barProgress * chartHeight
                val left = paddingLeft + spacing + (index * (barWidth + spacing))
                val top = chartHeight - barHeight
                
                val isSelected = index == selectedIndex
                
                // Draw bar
                drawRoundRect(
                    color = if (isSelected) barColor else barColor.copy(alpha = 0.7f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    alpha = if (selectedIndex != -1 && !isSelected) 0.3f else 1f
                )

                // Label on X-Axis (Intelligent Skipping)
                val labelSkip = when {
                    barCount > 20 -> 5
                    barCount > 10 -> 3
                    else -> 1
                }
                
                if (index % labelSkip == 0 || index == barCount - 1) {
                    val label = labels.getOrNull(index) ?: ""
                    val tl = textMeasurer.measure(label, androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = CarsilColors.TextMuted))
                    drawText(
                        tl,
                        topLeft = Offset(left + (barWidth/2) - (tl.size.width/2), chartHeight + 4.dp.toPx())
                    )
                }

                if (isSelected) {
                    val tooltipText = when {
                        unit == "Profs." -> "${value.toInt()} Profs."
                        unit == "S/" -> "S/ ${value.toInt()}"
                        unit.isNotEmpty() && unit.first().isLetter() -> "${value.toInt()} $unit"
                        else -> "$unit ${value.toInt()}"
                    }
                    val textLayout = textMeasurer.measure(
                        text = tooltipText,
                        style = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    val tw = textLayout.size.width + 12.dp.toPx()
                    val th = textLayout.size.height + 8.dp.toPx()
                    val tx = (left + barWidth/2 - tw/2).coerceIn(0f, canvasWidth - tw)
                    val ty = top - th - 8.dp.toPx()

                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(tx, ty),
                        size = Size(tw, th),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawText(textLayout, topLeft = Offset(tx + (tw - textLayout.size.width)/2, ty + (th - textLayout.size.height)/2))
                }
            }
            
            // Baseline
            drawLine(
                color = CarsilColors.Stroke,
                start = Offset(paddingLeft, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun CarsilDoughnutChart(
    data: List<Pair<String, Int>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    centerLabel: String = "Total",
    centerValue: String = ""
) {
    val total = data.sumOf { it.second }.toFloat()
    var selectedIndex by remember { mutableStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    var animationStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { animationStarted = true }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1200),
        label = "doughnutAnim"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val distance = (offset - center).getDistance()
                            val innerRadius = (size.width / 2f) * 0.7f
                            val outerRadius = size.width / 2f
                            
                            if (distance in innerRadius..outerRadius) {
                                val angle = Math.toDegrees(Math.atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())).let {
                                    if (it < 0) it + 360 else it
                                }.toFloat()
                                
                                var currentAngle = 0f
                                var found = -1
                                data.forEachIndexed { index, pair ->
                                    val sweep = (pair.second / total) * 360f
                                    if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                        found = index
                                    }
                                    currentAngle += sweep
                                }
                                selectedIndex = if (selectedIndex == found) -1 else found
                            } else {
                                selectedIndex = -1
                            }
                        }
                    }
            ) {
                val size = size.minDimension
                val strokeWidth = size * 0.15f
                val radius = (size - strokeWidth) / 2
                
                var startAngle = 0f
                data.forEachIndexed { index, pair ->
                    val sweepAngle = (pair.second / total) * 360f * animatedProgress
                    val isSelected = index == selectedIndex
                    
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (isSelected) strokeWidth * 1.2f else strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Butt
                        ),
                        alpha = if (selectedIndex != -1 && !isSelected) 0.3f else 1f
                    )
                    startAngle += (pair.second / total) * 360f
                }
            }
            
            // Central Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (selectedIndex != -1) data[selectedIndex].first else centerValue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CarsilColors.TextPrimary
                )
                Text(
                    text = if (selectedIndex != -1) "${data[selectedIndex].second}" else centerLabel,
                    fontSize = 12.sp,
                    color = CarsilColors.TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            data.forEachIndexed { index, pair ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(colors[index % colors.size], androidx.compose.foundation.shape.CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text("${pair.first}: ${pair.second}", fontSize = 11.sp, color = CarsilColors.TextSecondary)
                }
            }
        }
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
