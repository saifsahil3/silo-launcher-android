package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.ui.LauncherViewModel
import com.example.util.toImageBitmapSafe
import java.util.Locale

@Composable
fun AllAppsScreen(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    searchQuery: String,
    selectedCategory: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories = remember {
        listOf("All", "Productivity", "Communication", "Tools", "Media", "Navigation", "System")
    }

    val filteredApps = remember(allApps, searchQuery, selectedCategory) {
        allApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" ||
                    app.category.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Search & Header Title Bar
        Text(
            text = "ALL APPLICATIONS (${filteredApps.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search installed apps...", color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFFCE93D8)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1B1B22),
                unfocusedContainerColor = Color(0xFF16161D),
                focusedBorderColor = Color(0xFFCE93D8),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("all_apps_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory.equals(category, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedCategory(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFCE93D8),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1B1B22),
                        labelColor = Color.White.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apps Grid Layout
        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No apps found matching '$searchQuery'",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(filteredApps, key = { it.packageName }) { appInfo ->
                    AllAppGridItem(
                        appInfo = appInfo,
                        onLaunch = { viewModel.launchApp(context, appInfo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AllAppGridItem(
    appInfo: AppInfo,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val imageBitmap = remember(appInfo) {
        appInfo.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appInfo.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLaunch() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("app_grid_item_${appInfo.packageName}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B1B22))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = appInfo.label,
                    modifier = Modifier.size(38.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = appInfo.label,
                    tint = Color(0xFFCE93D8),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = appInfo.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
