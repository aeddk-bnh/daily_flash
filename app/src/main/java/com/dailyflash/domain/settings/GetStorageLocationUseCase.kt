package com.dailyflash.domain.settings

import com.dailyflash.core.storage.IStorageManager

class GetStorageLocationUseCase(
    private val storageManager: IStorageManager
) {
    operator fun invoke(): String {
        return storageManager.getStorageLocationDescription()
    }
}
