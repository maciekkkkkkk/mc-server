package mpeciakk.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ByteProcessor
import mpeciakk.math.Vector3i
import mpeciakk.nbt.NbtIo
import mpeciakk.nbt.Tag
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and

class PacketByteBuf : ByteBuf {
    private var buf: ByteBuf = Unpooled.buffer()

    constructor(buf: ByteBuf) {
        this.buf = buf
    }

    constructor()

    fun readVarInt(): Int {
        var value = 0
        var size = 0

        var data: Byte
        do {
            data = readByte()
            value = value or ((data and 127).toInt() shl size++ * 7)
            if (size > 5) {
                throw RuntimeException("VarInt too big")
            }
        } while ((data and 128.toByte()).toInt() != 0)

        return value
    }

    fun writeVarInt(data: Int) {
        var input = data
        while ((input and -128) != 0) {
            writeByte(input and 127 or 128)
            input = input ushr 7
        }

        writeByte(input)
    }

    fun readString(maxLength: Int): String {
        val i = readVarInt()

        return if (i > maxLength * 4) {
            throw DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")")
        } else if (i < 0) {
            throw DecoderException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8)
            this.readerIndex(this.readerIndex() + i)
            if (s.length > maxLength) {
                throw DecoderException("The received string length is longer than maximum allowed ($i > $maxLength)")
            } else {
                s
            }
        }
    }

    fun writeString(str: String): ByteBuf {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)

        return if (bytes.size > 32767) {
            throw EncoderException("String too big (was " + bytes.size + " bytes encoded, max " + 32767 + ")")
        } else {
            writeVarInt(bytes.size)
            writeBytes(bytes)
        }
    }

    fun writeUUID(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

    fun writeTag(tag: Tag) {
        NbtIo.write(tag, ByteBufOutputStream(this))
    }

    fun writeLongs(data: LongArray) {
        for (index in data.indices) {
            writeLong(data[index])
        }
    }

    fun writePosition(x: Int, y: Int, z: Int) {
        val position = x.toLong() and 0x3FFFFFF shl 38 or (z.toLong() and 0x3FFFFFF shl 12) or (y.toLong() and 0xFFF)

//        writeLong((x and 0x3FFFFFF shl 38 or (z and 0x3FFFFFF shl 12) or (y and 0xFFF)))
        writeLong(position)
    }

    fun writePosition(position: Vector3i) {
        writePosition(position.x, position.y, position.z)
    }

    fun readPosition(): Vector3i {
        val long = readLong()
        val x = long shr 38
        val y = long and 0xFFF
        val z = long shl 26 shr 38

        return Vector3i(x.toInt(), y.toInt(), z.toInt())
    }

    fun readItemData(): ItemData {
        val preset = readBoolean()

        if (preset) {
            val id = readVarInt()
            val count = readByte()

            // TODO: compound tag reading
//            val nbt = readTag()

            return ItemData(preset, id, count)
        }

        return ItemData(preset, null, null)
    }

    fun readAngle(): Float {
        return (buf.readByte() * 256 / 360).toFloat()
    }

    fun writeAngle(angle: Float) {
        buf.writeByte((angle * 256 / 360).toInt())
    }

    override fun readerIndex(): Int {
        return buf.readerIndex()
    }

    override fun readerIndex(readerIndex: Int): ByteBuf {
        return buf.readerIndex(readerIndex)
    }

    override fun setZero(index: Int, length: Int): ByteBuf {
        return buf.setZero(index, length)
    }

    override fun setShortLE(index: Int, value: Int): ByteBuf {
        return buf.setShortLE(index, value)
    }

    override fun getUnsignedInt(index: Int): Long {
        return buf.getUnsignedInt(index)
    }

    override fun readByte(): Byte {
        return buf.readByte()
    }

    override fun arrayOffset(): Int {
        return buf.arrayOffset()
    }

    override fun writeCharSequence(sequence: CharSequence?, charset: Charset?): Int {
        return buf.writeCharSequence(sequence, charset)
    }

    override fun getMedium(index: Int): Int {
        return buf.getMedium(index)
    }

    override fun asReadOnly(): ByteBuf {
        return buf.asReadOnly()
    }

    override fun setCharSequence(index: Int, sequence: CharSequence?, charset: Charset?): Int {
        return buf.setCharSequence(index, sequence, charset)
    }

    override fun nioBuffer(): ByteBuffer {
        return buf.nioBuffer()
    }

    override fun nioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.nioBuffer(index, length)
    }

    override fun getBoolean(index: Int): Boolean {
        return buf.getBoolean(index)
    }

    override fun writeByte(value: Int): ByteBuf {
        return buf.writeByte(value)
    }

    override fun capacity(): Int {
        return buf.capacity()
    }

    override fun capacity(newCapacity: Int): ByteBuf {
        return buf.capacity(newCapacity)
    }

    override fun writeShortLE(value: Int): ByteBuf {
        return buf.writeShortLE(value)
    }

    override fun isReadOnly(): Boolean {
        return buf.isReadOnly
    }

    override fun readShortLE(): Short {
        return buf.readShortLE()
    }

    override fun setMedium(index: Int, value: Int): ByteBuf {
        return buf.setMedium(index, value)
    }

    override fun setInt(index: Int, value: Int): ByteBuf {
        return buf.setInt(index, value)
    }

    override fun getUnsignedShortLE(index: Int): Int {
        return buf.getUnsignedShortLE(index)
    }

    override fun maxWritableBytes(): Int {
        return buf.maxWritableBytes()
    }

    override fun readDouble(): Double {
        return buf.readDouble()
    }

    override fun duplicate(): ByteBuf {
        return buf.duplicate()
    }

    override fun writeShort(value: Int): ByteBuf {
        return buf.writeShort(value)
    }

    override fun clear(): ByteBuf {
        return buf.clear()
    }

    override fun readUnsignedShort(): Int {
        return buf.readUnsignedShort()
    }

    override fun readUnsignedByte(): Short {
        return buf.readUnsignedByte()
    }

    override fun readIntLE(): Int {
        return buf.readIntLE()
    }

    override fun unwrap(): ByteBuf {
        return buf.unwrap()
    }

    override fun getShortLE(index: Int): Short {
        return buf.getShortLE(index)
    }

    override fun writeChar(value: Int): ByteBuf {
        return buf.writeChar(value)
    }

    override fun getCharSequence(index: Int, length: Int, charset: Charset?): CharSequence {
        return buf.getCharSequence(index, length, charset)
    }

    override fun getFloat(index: Int): Float {
        return buf.getFloat(index)
    }

    override fun writeBoolean(value: Boolean): ByteBuf {
        return buf.writeBoolean(value)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return buf.nioBuffers()
    }

    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> {
        return buf.nioBuffers(index, length)
    }

    override fun maxCapacity(): Int {
        return buf.maxCapacity()
    }

    override fun writeInt(value: Int): ByteBuf {
        return buf.writeInt(value)
    }

    override fun getUnsignedByte(index: Int): Short {
        return buf.getUnsignedByte(index)
    }

    override fun readSlice(length: Int): ByteBuf {
        return buf.readSlice(length)
    }

    override fun readBytes(length: Int): ByteBuf {
        return buf.readBytes(length)
    }

    override fun readBytes(dst: ByteBuf?): ByteBuf {
        return buf.readBytes(dst)
    }

    override fun readBytes(dst: ByteBuf?, length: Int): ByteBuf {
        return buf.readBytes(dst, length)
    }

    override fun readBytes(dst: ByteBuf?, dstIndex: Int, length: Int): ByteBuf {
        return buf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteArray?): ByteBuf {
        return buf.readBytes(dst)
    }

    override fun readBytes(dst: ByteArray?, dstIndex: Int, length: Int): ByteBuf {
        return buf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteBuffer?): ByteBuf {
        return buf.readBytes(dst)
    }

    override fun readBytes(out: OutputStream?, length: Int): ByteBuf {
        return buf.readBytes(out, length)
    }

    override fun readBytes(out: GatheringByteChannel?, length: Int): Int {
        return buf.readBytes(out, length)
    }

    override fun readBytes(out: FileChannel?, position: Long, length: Int): Int {
        return buf.readBytes(out, position, length)
    }

    override fun writeLong(value: Long): ByteBuf {
        return buf.writeLong(value)
    }

    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int {
        return buf.indexOf(fromIndex, toIndex, value)
    }

    override fun markWriterIndex(): ByteBuf {
        return buf.markWriterIndex()
    }

    override fun equals(other: Any?): Boolean {
        return buf.equals(other)
    }

    override fun readChar(): Char {
        return buf.readChar()
    }

    override fun compareTo(other: ByteBuf?): Int {
        return buf.compareTo(other)
    }

    override fun writeFloat(value: Float): ByteBuf {
        return buf.writeFloat(value)
    }

    override fun getUnsignedShort(index: Int): Int {
        return buf.getUnsignedShort(index)
    }

    override fun readCharSequence(length: Int, charset: Charset?): CharSequence {
        return buf.readCharSequence(length, charset)
    }

    override fun forEachByte(processor: ByteProcessor?): Int {
        return buf.forEachByte(processor)
    }

    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor?): Int {
        return buf.forEachByte(index, length, processor)
    }

    override fun isWritable(): Boolean {
        return buf.isWritable
    }

    override fun isWritable(size: Int): Boolean {
        return buf.isWritable(size)
    }

    override fun readableBytes(): Int {
        return buf.readableBytes()
    }

    override fun setShort(index: Int, value: Int): ByteBuf {
        return buf.setShort(index, value)
    }

    override fun writeZero(length: Int): ByteBuf {
        return buf.writeZero(length)
    }

    override fun refCnt(): Int {
        return buf.refCnt()
    }

    override fun writerIndex(): Int {
        return buf.writerIndex()
    }

    override fun writerIndex(writerIndex: Int): ByteBuf {
        return buf.writerIndex(writerIndex)
    }

    override fun skipBytes(length: Int): ByteBuf {
        return buf.skipBytes(length)
    }

    override fun bytesBefore(value: Byte): Int {
        return buf.bytesBefore(value)
    }

    override fun bytesBefore(length: Int, value: Byte): Int {
        return buf.bytesBefore(length, value)
    }

    override fun bytesBefore(index: Int, length: Int, value: Byte): Int {
        return buf.bytesBefore(index, length, value)
    }

    override fun getByte(index: Int): Byte {
        return buf.getByte(index)
    }

    override fun readUnsignedMedium(): Int {
        return buf.readUnsignedMedium()
    }

    override fun getMediumLE(index: Int): Int {
        return buf.getMediumLE(index)
    }

    override fun resetReaderIndex(): ByteBuf {
        return buf.resetReaderIndex()
    }

    override fun setBoolean(index: Int, value: Boolean): ByteBuf {
        return buf.setBoolean(index, value)
    }

    override fun setByte(index: Int, value: Int): ByteBuf {
        return buf.setByte(index, value)
    }

    override fun readRetainedSlice(length: Int): ByteBuf {
        return buf.readRetainedSlice(length)
    }

    override fun readLongLE(): Long {
        return buf.readLongLE()
    }

    override fun discardSomeReadBytes(): ByteBuf {
        return buf.discardSomeReadBytes()
    }

    override fun forEachByteDesc(processor: ByteProcessor?): Int {
        return buf.forEachByteDesc(processor)
    }

    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor?): Int {
        return buf.forEachByteDesc(index, length, processor)
    }

    override fun discardReadBytes(): ByteBuf {
        return buf.discardReadBytes()
    }

    override fun nioBufferCount(): Int {
        return buf.nioBufferCount()
    }

    override fun copy(): ByteBuf {
        return buf.copy()
    }

    override fun copy(index: Int, length: Int): ByteBuf {
        return buf.copy(index, length)
    }

    override fun getLong(index: Int): Long {
        return buf.getLong(index)
    }

    override fun setBytes(index: Int, src: ByteBuf?): ByteBuf {
        return buf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteBuf?, length: Int): ByteBuf {
        return buf.setBytes(index, src, length)
    }

    override fun setBytes(index: Int, src: ByteBuf?, srcIndex: Int, length: Int): ByteBuf {
        return buf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteArray?): ByteBuf {
        return buf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteArray?, srcIndex: Int, length: Int): ByteBuf {
        return buf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteBuffer?): ByteBuf {
        return buf.setBytes(index, src)
    }

    override fun setBytes(index: Int, `in`: InputStream?, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    override fun setBytes(index: Int, `in`: ScatteringByteChannel?, length: Int): Int {
        return buf.setBytes(index, `in`, length)
    }

    override fun setBytes(index: Int, `in`: FileChannel?, position: Long, length: Int): Int {
        return buf.setBytes(index, `in`, position, length)
    }

    override fun readUnsignedShortLE(): Int {
        return buf.readUnsignedShortLE()
    }

    override fun setLong(index: Int, value: Long): ByteBuf {
        return buf.setLong(index, value)
    }

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer {
        return buf.internalNioBuffer(index, length)
    }

    override fun resetWriterIndex(): ByteBuf {
        return buf.resetWriterIndex()
    }

    override fun readLong(): Long {
        return buf.readLong()
    }

    override fun retainedSlice(): ByteBuf {
        return buf.retainedSlice()
    }

    override fun retainedSlice(index: Int, length: Int): ByteBuf {
        return buf.retainedSlice(index, length)
    }

    override fun memoryAddress(): Long {
        return buf.memoryAddress()
    }

    override fun hashCode(): Int {
        return buf.hashCode()
    }

    override fun setFloat(index: Int, value: Float): ByteBuf {
        return buf.setFloat(index, value)
    }

    override fun toString(charset: Charset?): String {
        return buf.toString(charset)
    }

    override fun toString(index: Int, length: Int, charset: Charset?): String {
        return buf.toString(index, length, charset)
    }

    override fun toString(): String {
        return buf.toString()
    }

    override fun hasMemoryAddress(): Boolean {
        return buf.hasMemoryAddress()
    }

    override fun writeLongLE(value: Long): ByteBuf {
        return buf.writeLongLE(value)
    }

    override fun setMediumLE(index: Int, value: Int): ByteBuf {
        return buf.setMediumLE(index, value)
    }

    override fun order(): ByteOrder {
        return buf.order()
    }

    override fun order(endianness: ByteOrder?): ByteBuf {
        return buf.order(endianness)
    }

    override fun readUnsignedInt(): Long {
        return buf.readUnsignedInt()
    }

    override fun isDirect(): Boolean {
        return buf.isDirect
    }

    override fun readMedium(): Int {
        return buf.readMedium()
    }

    override fun getShort(index: Int): Short {
        return buf.getShort(index)
    }

    override fun setDouble(index: Int, value: Double): ByteBuf {
        return buf.setDouble(index, value)
    }

    override fun readShort(): Short {
        return buf.readShort()
    }

    override fun alloc(): ByteBufAllocator {
        return buf.alloc()
    }

    override fun getUnsignedMedium(index: Int): Int {
        return buf.getUnsignedMedium(index)
    }

    override fun writeBytes(src: ByteBuf?): ByteBuf {
        return buf.writeBytes(src)
    }

    override fun writeBytes(src: ByteBuf?, length: Int): ByteBuf {
        return buf.writeBytes(src, length)
    }

    override fun writeBytes(src: ByteBuf?, srcIndex: Int, length: Int): ByteBuf {
        return buf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteArray?): ByteBuf {
        return buf.writeBytes(src)
    }

    override fun writeBytes(src: ByteArray?, srcIndex: Int, length: Int): ByteBuf {
        return buf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteBuffer?): ByteBuf {
        return buf.writeBytes(src)
    }

    override fun writeBytes(`in`: InputStream?, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    override fun writeBytes(`in`: ScatteringByteChannel?, length: Int): Int {
        return buf.writeBytes(`in`, length)
    }

    override fun writeBytes(`in`: FileChannel?, position: Long, length: Int): Int {
        return buf.writeBytes(`in`, position, length)
    }

    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf {
        return buf.setIndex(readerIndex, writerIndex)
    }

    override fun array(): ByteArray {
        return buf.array()
    }

    override fun getUnsignedMediumLE(index: Int): Int {
        return buf.getUnsignedMediumLE(index)
    }

    override fun getIntLE(index: Int): Int {
        return buf.getIntLE(index)
    }

    override fun slice(): ByteBuf {
        return buf.slice()
    }

    override fun slice(index: Int, length: Int): ByteBuf {
        return buf.slice(index, length)
    }

    override fun getUnsignedIntLE(index: Int): Long {
        return buf.getUnsignedIntLE(index)
    }

    override fun readFloat(): Float {
        return buf.readFloat()
    }

    override fun getBytes(index: Int, dst: ByteBuf?): ByteBuf {
        return buf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteBuf?, length: Int): ByteBuf {
        return buf.getBytes(index, dst, length)
    }

    override fun getBytes(index: Int, dst: ByteBuf?, dstIndex: Int, length: Int): ByteBuf {
        return buf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteArray?): ByteBuf {
        return buf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteArray?, dstIndex: Int, length: Int): ByteBuf {
        return buf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteBuffer?): ByteBuf {
        return buf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, out: OutputStream?, length: Int): ByteBuf {
        return buf.getBytes(index, out, length)
    }

    override fun getBytes(index: Int, out: GatheringByteChannel?, length: Int): Int {
        return buf.getBytes(index, out, length)
    }

    override fun getBytes(index: Int, out: FileChannel?, position: Long, length: Int): Int {
        return buf.getBytes(index, out, position, length)
    }

    override fun markReaderIndex(): ByteBuf {
        return buf.markReaderIndex()
    }

    override fun getDouble(index: Int): Double {
        return buf.getDouble(index)
    }

    override fun readBoolean(): Boolean {
        return buf.readBoolean()
    }

    override fun writeIntLE(value: Int): ByteBuf {
        return buf.writeIntLE(value)
    }

    override fun readInt(): Int {
        return buf.readInt()
    }

    override fun getLongLE(index: Int): Long {
        return buf.getLongLE(index)
    }

    override fun writeMedium(value: Int): ByteBuf {
        return buf.writeMedium(value)
    }

    override fun touch(): ByteBuf {
        return buf.touch()
    }

    override fun touch(hint: Any?): ByteBuf {
        return buf.touch(hint)
    }

    override fun writeDouble(value: Double): ByteBuf {
        return buf.writeDouble(value)
    }

    override fun readUnsignedMediumLE(): Int {
        return buf.readUnsignedMediumLE()
    }

    override fun getInt(index: Int): Int {
        return buf.getInt(index)
    }

    override fun setIntLE(index: Int, value: Int): ByteBuf {
        return buf.setIntLE(index, value)
    }

    override fun setChar(index: Int, value: Int): ByteBuf {
        return buf.setChar(index, value)
    }

    override fun writeMediumLE(value: Int): ByteBuf {
        return buf.writeMediumLE(value)
    }

    override fun ensureWritable(minWritableBytes: Int): ByteBuf {
        return buf.ensureWritable(minWritableBytes)
    }

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int {
        return buf.ensureWritable(minWritableBytes, force)
    }

    override fun retain(increment: Int): ByteBuf {
        return buf.retain(increment)
    }

    override fun retain(): ByteBuf {
        return buf.retain()
    }

    override fun setLongLE(index: Int, value: Long): ByteBuf {
        return buf.setLongLE(index, value)
    }

    override fun readMediumLE(): Int {
        return buf.readMediumLE()
    }

    override fun readUnsignedIntLE(): Long {
        return buf.readUnsignedIntLE()
    }

    override fun hasArray(): Boolean {
        return buf.hasArray()
    }

    override fun isReadable(): Boolean {
        return buf.isReadable
    }

    override fun isReadable(size: Int): Boolean {
        return buf.isReadable(size)
    }

    override fun getChar(index: Int): Char {
        return buf.getChar(index)
    }

    override fun release(): Boolean {
        return buf.release()
    }

    override fun release(decrement: Int): Boolean {
        return buf.release(decrement)
    }

    override fun retainedDuplicate(): ByteBuf {
        return buf.retainedDuplicate()
    }

    override fun writableBytes(): Int {
        return buf.writableBytes()
    }
}