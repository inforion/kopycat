package ru.inforion.lab403.kopycat.cores.ppc.instructions

import ru.inforion.lab403.common.extensions.*


/**
 * Sign extension to 32-bit integer
 * @param sbit sign bit
 * @return extended integer
 * */
infix fun Long.ssext(sbit: Int) = signext(this, sbit + 1).toLong()
infix fun Long.usext(sbit: Int) = signext(this, sbit + 1).toULong()

//Now it works and works faster
infix fun Long.rotl32(amount: Int): Long = ((this shl amount) or (this shr (32 - amount))).mask(31)

fun Long.replace(indx: Int, value: Long): Long = (this and (1L.shl(indx).inv()) or (value shl indx))
fun Long.replace(indx: Int, value: Boolean): Long = this.replace(indx, value.toLong())
fun Long.replace(indx: IntRange, value: Long): Long = (this and (bitMask(indx).inv()) or (value shl indx.last))


