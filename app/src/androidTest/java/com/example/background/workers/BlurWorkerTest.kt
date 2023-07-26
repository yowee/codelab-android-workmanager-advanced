
package com.example.background.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import com.example.background.KEY_IMAGE_URI
import org.junit.Test

class BlurWorkerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var wmRule = WorkManagerTestRule()

    @Test
    fun testFailsIfNoInput() {
        // Do not define input data

        // Create request
        val request = OneTimeWorkRequestBuilder<BlurWorker>().build()

        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor.
        wmRule.workManager.enqueue(request).result.get()

        // Get WorkInfo
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()

        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    @Throws(Exception::class)
    fun testAppliesBlur() {
        // Define input data
        val inputDataUri = copyFileFromTestToTargetCtx(
                wmRule.testContext,
                wmRule.targetContext,
                "test_image.png")
        val inputData = workDataOf(KEY_IMAGE_URI to inputDataUri.toString())

        // Create request
        val request = OneTimeWorkRequestBuilder<BlurWorker>()
                .setInputData(inputData)
                .build()

        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor.
        wmRule.workManager.enqueue(request).result.get()

        // Get WorkInfo
        val workInfo = wmRule.workManager.getWorkInfoById(request.id).get()
        val outputUri = workInfo.outputData.getString(KEY_IMAGE_URI)

        // Assert
        assertThat(uriFileExists(wmRule.targetContext, outputUri), `is`(true))
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }
}
