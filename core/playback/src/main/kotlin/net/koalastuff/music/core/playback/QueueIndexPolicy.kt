package net.koalastuff.music.core.playback

internal object QueueIndexPolicy {
    fun insertionIndexAfterCurrent(itemCount: Int, currentIndex: Int): Int =
        (currentIndex + 1).coerceIn(0, itemCount.coerceAtLeast(0))

    fun validIndex(itemCount: Int, index: Int): Boolean = index in 0 until itemCount

    fun move(itemCount: Int, fromIndex: Int, toIndex: Int): Pair<Int, Int>? = if (
        validIndex(itemCount, fromIndex) &&
        validIndex(itemCount, toIndex) &&
        fromIndex != toIndex
    ) {
        fromIndex to toIndex
    } else {
        null
    }
}
