package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioStockUi

@Composable
fun PortfolioStockItem(
    stock: PortfolioStockUi,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle(stock.symbol) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = stock.isSelected,
            onCheckedChange = { onToggle(stock.symbol) }
        )
        Text(
            text = "${stock.symbol} - ${stock.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (stock.isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

