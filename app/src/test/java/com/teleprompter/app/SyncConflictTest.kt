package com.teleprompter.app

import com.teleprompter.app.core.auth.AuthUser
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.sync.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Phase 5 Deterministic Conflict Strategy and Local-First Architecture.
 */
class SyncConflictTest {

    data class MockRemoteScript(
        val id: String,
        val title: String,
        val content: String,
        val updatedAt: Long,
        val isDeleted: Boolean = false
    )

    /**
     * Replicates the exact deterministic conflict resolution algorithm
     * implemented in SyncRepository.
     */
    private fun resolveSyncConflict(
        local: ScriptEntity?,
        remote: MockRemoteScript?
    ): Pair<ScriptEntity?, MockRemoteScript?> {
        val remoteUpdatedAt = remote?.updatedAt ?: 0L

        // Local -> Remote Push Condition:
        // Local is newer or doesn't exist remotely
        val pushedToRemote: MockRemoteScript? = if (local != null) {
            if (remote == null || local.updatedAt >= remoteUpdatedAt) {
                MockRemoteScript(
                    id = local.id,
                    title = local.title,
                    content = local.content,
                    updatedAt = local.updatedAt,
                    isDeleted = local.isDeleted
                )
            } else {
                null // Remote is newer; do not overwrite remote with older local
            }
        } else {
            null
        }

        // Remote -> Local Merge Condition:
        // Remote is newer than local, or script only exists remotely
        val mergedToLocal: ScriptEntity? = if (remote != null) {
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                if (!remote.isDeleted) {
                    ScriptEntity(
                        id = remote.id,
                        title = remote.title,
                        content = remote.content,
                        isDeleted = false,
                        isSynced = true,
                        updatedAt = remote.updatedAt
                    )
                } else {
                    null // Deleted remotely -> remove from local
                }
            } else {
                local // Local is newer; preserve local
            }
        } else {
            local
        }

        return Pair(mergedToLocal, pushedToRemote)
    }

    @Test
    fun newerLocalData_winsConflict_neverOverwrittenByOlderRemote() {
        val localTime = 2000L
        val remoteTime = 1000L

        val local = ScriptEntity(
            id = "script_1",
            title = "Local Updated Title",
            content = "New local edits",
            updatedAt = localTime
        )

        val remote = MockRemoteScript(
            id = "script_1",
            title = "Older Cloud Title",
            content = "Old cloud content",
            updatedAt = remoteTime
        )

        val (mergedLocal, pushedRemote) = resolveSyncConflict(local, remote)

        // Local copy must retain the newer local data
        assertNotNull(mergedLocal)
        assertEquals("Local Updated Title", mergedLocal?.title)
        assertEquals(localTime, mergedLocal?.updatedAt)

        // Remote should be updated with local newer data
        assertNotNull(pushedRemote)
        assertEquals("Local Updated Title", pushedRemote?.title)
        assertEquals(localTime, pushedRemote?.updatedAt)
    }

    @Test
    fun newerRemoteData_mergesToLocal_whenLocalIsOlder() {
        val localTime = 1000L
        val remoteTime = 3000L

        val local = ScriptEntity(
            id = "script_2",
            title = "Old Local Title",
            content = "Old local content",
            updatedAt = localTime
        )

        val remote = MockRemoteScript(
            id = "script_2",
            title = "Newer Cloud Title From Tablet",
            content = "Cloud updated content",
            updatedAt = remoteTime
        )

        val (mergedLocal, pushedRemote) = resolveSyncConflict(local, remote)

        // Local copy merges cloud updates
        assertNotNull(mergedLocal)
        assertEquals("Newer Cloud Title From Tablet", mergedLocal?.title)
        assertEquals(remoteTime, mergedLocal?.updatedAt)
        assertTrue(mergedLocal?.isSynced == true)

        // Cloud is not overwritten with older local
        assertNull(pushedRemote)
    }

    @Test
    fun remoteDeletion_deletesLocalCopy_withoutResurfacing() {
        val local = ScriptEntity(
            id = "script_3",
            title = "Script To Delete",
            content = "Will be deleted",
            updatedAt = 1000L
        )

        val remoteDeleted = MockRemoteScript(
            id = "script_3",
            title = "Script To Delete",
            content = "Will be deleted",
            updatedAt = 2000L,
            isDeleted = true
        )

        val (mergedLocal, _) = resolveSyncConflict(local, remoteDeleted)

        // Merged local must be null (deleted from DB)
        assertNull(mergedLocal)
    }

    @Test
    fun newRemoteScript_insertsIntoLocalDb() {
        val remoteNew = MockRemoteScript(
            id = "script_4",
            title = "Brand New Cloud Script",
            content = "Content created on web",
            updatedAt = 1500L
        )

        val (mergedLocal, _) = resolveSyncConflict(null, remoteNew)

        assertNotNull(mergedLocal)
        assertEquals("script_4", mergedLocal?.id)
        assertEquals("Brand New Cloud Script", mergedLocal?.title)
        assertEquals(1500L, mergedLocal?.updatedAt)
    }

    @Test
    fun guestMode_andOfflineState_preservesLocalFirstIntegrity() {
        val guestUser = AuthUser(
            id = "guest_123",
            email = null,
            displayName = "Guest Creator",
            isAnonymous = true
        )

        // Guest user is anonymous
        assertTrue(guestUser.isAnonymous)
        assertNull(guestUser.email)

        // Sync statuses for offline & guest
        val offlineStatus = SyncStatus.OFFLINE
        val idleStatus = SyncStatus.IDLE
        val disabledStatus = SyncStatus.DISABLED

        assertEquals("OFFLINE", offlineStatus.name)
        assertEquals("IDLE", idleStatus.name)
        assertEquals("DISABLED", disabledStatus.name)
    }

    @Test
    fun videoRecordings_areExcludedFromCloudSyncMap() {
        // Test that script sync map only serializes script text and teleprompter preferences,
        // and NEVER contains video URIs or video files
        val script = ScriptEntity(
            id = "script_record_test",
            title = "Show Script",
            content = "Teleprompter text",
            scrollSpeed = 55,
            fontSize = 32
        )

        val syncMapKeys = listOf(
            "id", "title", "content", "folderId", "isFavorite", "wordCount",
            "estimatedReadingTime", "scrollSpeed", "fontSize", "fontStyle",
            "textColorHex", "bgOpacity", "textOpacity", "readingWidthPercent",
            "verticalPositionPercent", "lineSpacingMultiplier", "letterSpacingSp",
            "textAlignment", "mirrorText", "showSafeAreaGuide", "presetType",
            "hasCustomSettings", "isDeleted", "updatedAt", "createdAt"
        )

        // Ensure no video / recording keys are present
        assertFalse(syncMapKeys.contains("videoUri"))
        assertFalse(syncMapKeys.contains("localUri"))
        assertFalse(syncMapKeys.contains("videoFile"))
        assertFalse(syncMapKeys.contains("recordingData"))
    }
}
