/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/
package org.gjt.jclasslib.structures.elementvalues

import org.gjt.jclasslib.structures.InvalidByteCodeException

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * Describes an  ArrayElementValue attribute structure.

 * @author [Vitor Carreira](mailto:vitor.carreira@gmail.com)
 */
class ArrayElementValue : ElementValue(ElementValueType.ARRAY) {
    /**
     * Element values associations of this entry.
     */
    var elementValueEntries: Array<ElementValue> = emptyArray()

    override val specificLength: Int
        get() = 2 + elementValueEntries.sumBy { it.length }

    @Throws(InvalidByteCodeException::class, IOException::class)
    override fun read(input: DataInput) {
        val elementValueEntriesLength = input.readUnsignedShort()
        elementValueEntries = Array(elementValueEntriesLength) {
            ElementValue.create(input, classFile)
        }

        if (isDebug) debug("read")
    }

    @Throws(InvalidByteCodeException::class, IOException::class)
    override fun write(output: DataOutput) {
        super.write(output)
        output.writeShort(elementValueEntries.size)
        elementValueEntries.forEach { it.write(output) }

        if (isDebug) debug("wrote")
    }

    override fun debug(message: String) {
        super.debug("$message ArrayElementValue with ${elementValueEntries.size} entries")
    }

    override val entryName: String
        get() = "ArrayElement"

}