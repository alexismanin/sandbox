package fr.amanin.stackoverflow

import java.nio.ByteBuffer
import java.util.*
import kotlin.RuntimeException

/**
 * Response to https://stackoverflow.com/questions/63278362
 */
fun ByteBuffer.readUUIDs(nbUUIDs : Int = remaining()/16) : Sequence<UUID> {
    if (nbUUIDs <= 0) return emptySequence()
    val nbBytes = nbUUIDs * 16
    // Defensive copy -> resulting sequence becomes independant from receiver buffer
    val defCpy = ByteArray(nbBytes)
    // slice is required to left source buffer untouched
    slice().get(defCpy)
    val view = ByteBuffer.wrap(defCpy)
    return (1..nbUUIDs).asSequence()
        .map { UUID(view.long, view.long) }
}

fun List<UUID>?.write() : ByteBuffer? {
    if (this == null || isEmpty()) return null;
    val buffer = ByteBuffer.allocate(Math.multiplyExact(size, 16))
    forEach { uuid ->
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
    }
    buffer.rewind()
    return buffer
}

fun main() {
    val uuids = (0..3).asSequence().map { UUID.randomUUID() }.toList()
    val writtenUUIDs = uuids.write()
    val uuidSequence = writtenUUIDs ?.readUUIDs() ?: throw RuntimeException("Bug")

    // Note that now, we can do whatever we want with the buffer. The sequence is not impacted
    writtenUUIDs.getLong()
    writtenUUIDs.putLong(-1)

    val readBack = uuidSequence?.toList()
    assert(uuids == readBack) { throw RuntimeException("Bug") }
    println(uuids)
    println(readBack)
}