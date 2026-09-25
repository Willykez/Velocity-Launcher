package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppCategory
import com.example.model.IconShape
import com.example.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura Launcher", appName)
  }

  @Test
  fun `launcher settings default values are correct`() {
    val settings = LauncherSettings()
    assertEquals(5, settings.gridRows)
    assertEquals(4, settings.gridCols)
    assertEquals(5, settings.dockCount)
    assertEquals(IconShape.SQUIRCLE, settings.iconShape)
    assertNotNull(settings.iconShape.toComposeShape())
  }

  @Test
  fun `categories are well defined`() {
    val categories = AppCategory.values()
    assertEquals(8, categories.size)
  }
}
