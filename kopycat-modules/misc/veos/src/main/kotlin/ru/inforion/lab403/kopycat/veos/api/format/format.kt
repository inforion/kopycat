/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.veos.api.format

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.misc.*


inline val ctrlVT get() = 0xB.asChar
inline val ctrlFF get() = 0xC.asChar

fun isdigit(sym: Char) = sym in "0123456789"
fun isspace(sym: Char) = sym in "\t\n$ctrlVT$ctrlFF\r "
fun isodigit(sym: Char) = sym in "01234567"
fun isxdigit(sym: Char) = sym in "0123456789abcdefABCDEF"

const val SIGN	    = 1		    /* unsigned/signed, must be 1 */
const val LEFT	    = 2		    /* left justified */
const val PLUS	    = 4		    /* show plus */
const val SPACE	    = 8		    /* space if plus */
const val ZEROPAD	= 16		/* pad with zero, must be 16 == '0' - ' ' */
const val SMALL	    = 32		/* use lowercase in hex (must be 32 == 0x20) */
const val SPECIAL	= 64		/* prefix hex with "0x", octal with "0" */


fun skipAtoi(str: ICharArrayPointer): Int {
    var i = 0

    do {
        i = i * 10 + str.get.toInt() - '0'.toInt()
        str.next()
    } while(isdigit(str.get))
    return i
}

fun moveRight(buf: ICharArrayPointer, len: Int, spaces: Int) {
    if (!buf.hasRemaining)
        return
    val size = buf.remaining
    if (size <= spaces) {
        buf.write(" " * spaces)
        return
    }
    if (len > 0) {
        val fixedLen = if (len > size - spaces) size - spaces else len
        (buf + spaces).write(buf.copy.read(len))
    }
    buf.write(" " * spaces)

}

/*
 * Handle field width padding for a string.
 * @buf: current buffer position
 * @n: length of string
 * @end: end of output buffer
 * @spec: for field width and flags
 * Returns: new buffer position after padding.
 */
fun widenString(buf: ICharArrayPointer, n: Int, spec: PrintfSpec) {

    if (n >= spec.fieldWidth)
        return
    /* we want to pad the sucker */
    var spaces = spec.fieldWidth - n
    if (LEFT !in spec.flags) {
        moveRight(buf - n, n, spaces);
        buf += spaces
        return
    }
    while (spaces-- != 0) {
        if (buf.hasRemaining)
            buf.write(' ')
    }
}

/* Handle string from a well known address. */
fun stringNocheck(buf: ICharArrayPointer, inS: String, spec: PrintfSpec) {
    var len = 0
    var lim = spec.precision
    val s = inS.toCharArray().charArrayPointer

    while (lim-- != 0) {
        val c = s.get
        if (c.asUInt == 0)
            break
        s.next()
        if (buf.hasRemaining)
            buf.write(c)
        ++len
    }
    widenString(buf, len, spec)
}

/* Be careful: error messages must fit into the given buffer. */
fun errorString(buf: ICharArrayPointer, s: String, spec: PrintfSpec) {
    /*
     * Hard limit to avoid a completely insane messages. It actually
     * works pretty well because most error messages are in
     * the many pointer format modifiers.
     */
    if (spec.precision == -1)
        spec.precision = 2 * 4//os.sys.sizeOf.pointer

    stringNocheck(buf, s, spec)
}

/*
 * Do not call any complex external code here. Nested printk()/vsprintf()
 * might cause infinite loops. Failures might break printk() and would
 * be hard to debug.
 */
fun checkPointerMsg(ptr: String?): String? {
    if (ptr == null)
        return "(null)"

//    if ((unsigned long)ptr < PAGE_SIZE || IS_ERR_VALUE(ptr))
//        return "(efault)";

    return null
}

fun checkPointer(buf: ICharArrayPointer, ptr: String?, spec: PrintfSpec): Boolean {

    val errMsg = checkPointerMsg(ptr)
    if (errMsg != null) {
        errorString(buf, errMsg, spec)
        return true
    }

    return false
}

fun string(buf: ICharArrayPointer, s: String?, spec: PrintfSpec) {
    if (!checkPointer(buf, s, spec))
        stringNocheck(buf, s!!, spec)
}

/*
 * Helper function to decode printf style format.
 * Each call decode a token from the format and return the
 * number of characters read (or likely the delta where it wants
 * to go on the next call).
 * The decoded token is returned through the parameters
 *
 * 'h', 'l', or 'L' for integer fields
 * 'z' support added 23/7/1999 S.H.
 * 'z' changed to 'Z' --davidm 1/25/99
 * 'Z' changed to 'z' --adobriyan 2017-01-25
 * 't' added for ptrdiff_t
 *
 * @fmt: the format string
 * @type of the token returned
 * @flags: various flags such as +, -, # tokens..
 * @field_width: overwritten width
 * @base: base of the number (octal, hex, ...)
 * @precision: precision of a number
 * @qualifier: qualifier of a number (long, size_t, ...)
 */
fun formatDecode(fmt: CharArrayPointer, spec: PrintfSpec): Int
{
    val start = fmt.offset

    /* we finished early by reading the field width */
    val skipToQuantifier = spec.type == FormatType.PRECISION
    if (spec.type == FormatType.WIDTH) {
        if (spec.fieldWidth < 0) {
            spec.fieldWidth = -spec.fieldWidth
            spec.flags += LEFT
        }
        spec.type = FormatType.NONE
    }

    /* we finished early by reading the precision */
    else if (skipToQuantifier) {
        if (spec.precision < 0)
            spec.precision = 0

        spec.type = FormatType.NONE
    }

    /* By default */
    else {
        spec.type = FormatType.NONE

        while (fmt.hasRemaining) {
            if (fmt.get == '%')
                break
            fmt.next()
        }

        /* Return the current non-format string */
        if (fmt.offset != start || !fmt.hasRemaining)
            return fmt.offset - start

        /* Process flags */
        spec.flags.clear()

        while (true) {  /* this also skips first '%' */
            var found = true

            fmt.next()

            when (fmt.get) {
                '-' -> spec.flags += LEFT
                '+' -> spec.flags += PLUS
                ' ' -> spec.flags += SPACE
                '#' -> spec.flags += SPECIAL
                '0' -> spec.flags += ZEROPAD
                else -> found = false
            }

            if (!found)
                break
        }

        /* get field width */
        spec.fieldWidth = -1

        if (isdigit(fmt.get))
            spec.fieldWidth = skipAtoi(fmt)
        else if (fmt.get == '*') {
            /* it's the next argument */
            spec.type = FormatType.WIDTH
            fmt.next()
            return fmt.offset - start
        }

    };

    if (!skipToQuantifier) { //    precision:

        /* get the precision */
        spec.precision = -1
        if (fmt.get == '.') {
            fmt.next()
            if (isdigit(fmt.get)) {
                spec.precision = skipAtoi(fmt)
                if (spec.precision < 0)
                    spec.precision = 0
            } else if (fmt.get == '*') {
                /* it's the next argument */
                spec.type = FormatType.PRECISION
                fmt.next()
                return fmt.offset - start
            }
        }
    }

    // qualifier:
    /* get the conversion qualifier */
    var qualifier = 0.asChar
    if (fmt.get == 'h' || fmt.get.toLowerCase() == 'l' || fmt.get == 'z' || fmt.get == 't') {
        qualifier = fmt.get
        fmt.next()
        if (qualifier == fmt.get) {
            if (qualifier == 'l') {
                qualifier = 'L'
                fmt.next()
            } else if (qualifier == 'h') {
                qualifier = 'H'
                fmt.next()
            }
        }
    }

    /* default base */
    spec.base = 10
    when (fmt.get) {
        'c' -> {
            spec.type = FormatType.CHAR
            fmt.next()
            return fmt.offset - start
        }
        's' -> {
            spec.type = FormatType.STR
            fmt.next()
            return fmt.offset - start
        }
        'p' -> {
            spec.type = FormatType.PTR
            fmt.next()
            return fmt.offset - start
        }

        '%' -> {
            spec.type = FormatType.PERCENT_CHAR
            fmt.next()
            return fmt.offset - start
        }
        /* integer number formats - set up the flags and "break" */
        'o' -> spec.base = 8

        'x' -> {
            spec.flags += SMALL
            spec.base = 16
        }

        'X' -> spec.base = 16

        'd', 'i' -> spec.flags += SIGN
        'u' -> {}

//        'n',
        /*
         * Since %n poses a greater security risk than
         * utility, treat it as any other invalid or
         * unsupported format specifier.
         */
        else -> {
            println("Please remove unsupported ${fmt.get} in format string\n")
            spec.type = FormatType.INVALID
            return fmt.offset - start
        }
    }

    if (qualifier == 'L')
        spec.type = FormatType.LONG_LONG
    else if (qualifier == 'l') {
        spec.type = if (SIGN in spec.flags)
            FormatType.LONG
        else
            FormatType.ULONG
    } else if (qualifier == 'z') {
        spec.type = FormatType.SIZE_T
    } else if (qualifier == 't') {
        spec.type = FormatType.PTRDIFF
    } else if (qualifier == 'H') {
        spec.type = if (SIGN in spec.flags)
            FormatType.BYTE
        else
            FormatType.UBYTE
    } else if (qualifier == 'h') {
        spec.type = if (SIGN in spec.flags)
            FormatType.SHORT
        else
            FormatType.USHORT
    } else {
        spec.type = if (SIGN in spec.flags)
            FormatType.INT
        else
            FormatType.UINT
    }
    fmt.next()
    return fmt.offset - start
}

fun setFieldWidth(spec: PrintfSpec, width: Int) {
    spec.fieldWidth = width
    // TODO: check MAX/MIN ranges
}

fun setPrecision(spec: PrintfSpec, prec: Int) {
    spec.precision = prec
    // TODO: check MAX/MIN ranges
}

fun putDec(buf: CharArrayPointer, n: Long): Int {
    val str = n.toString().reversed()
    buf.write(str)
    return str.length
}


fun number(buf: ICharArrayPointer, inNum: Long, spec: PrintfSpec) {
    val hexAscUpper = "0123456789ABCDEF"
    var num = inNum
    /* put_dec requires 2-byte alignment of the buffer. */
    val tmp = CharArray(24)
    val needPfx = ((SPECIAL in spec.flags) && spec.base != 10);
    val isZero = num == 0L
    var fieldWidth = spec.fieldWidth
    var precision = spec.precision

    /* locase = 0 or 0x20. ORing digits or letters with 'locase'
     * produces same digits or (maybe lowercased) letters */
    val locase = if (SMALL in spec.flags) 0x20 else 0
    if (LEFT in spec.flags)
        spec.flags.remove(ZEROPAD)
    var sign = 0.asChar
    if (SIGN in spec.flags) {
        if (num < 0L) {
            sign = '-'
            num = -num
            fieldWidth--
        } else if (PLUS in spec.flags) {
            sign = '+'
            fieldWidth--
        } else if (SPACE in spec.flags) {
            sign = ' '
            fieldWidth--
        }
    }
    if (needPfx) {
        if (spec.base == 16)
            fieldWidth -= 2
        else if (!isZero)
            fieldWidth--
    }

    /* generate full string in tmp[], in reverse order */
    var i = 0
    if (num in 0 until spec.base)
        tmp[i++] = (hexAscUpper[num.asInt].asUInt or locase).asChar
    else if (spec.base != 10) { /* 8 or 16 */
        val mask = spec.base - 1
        var shift = 3

        if (spec.base == 16)
            shift = 4
        do {
            tmp[i++] = (hexAscUpper[num.asInt and mask].asUInt or locase).asChar
            num = num ushr shift
        } while (num != 0L)
    } else { /* base 10 */
        i = putDec(tmp.charArrayPointer, num)
    }

    /* printing 100 using %2d gives "100", not "00" */
    if (i > precision)
        precision = i
    /* leading space padding */
    fieldWidth -= precision
    if (ZEROPAD !in spec.flags && LEFT !in spec.flags) {
        while (--fieldWidth >= 0) {
            if (buf.hasRemaining)
                buf.write(' ')
        }
    }
    /* sign */
    if (sign != 0.asChar) {
        if (buf.hasRemaining)
            buf.write(sign)
    }
    /* "0x" / "0" prefix */
    if (needPfx) {
        if (spec.base == 16 || !isZero) {
            if (buf.hasRemaining)
                buf.write('0')
        }
        if (spec.base == 16) {
            if (buf.hasRemaining)
                buf.write(('x'.asUInt or locase).asChar)
        }
    }
    /* zero or space padding */
    if (LEFT !in spec.flags) {
        val pad = if (ZEROPAD in spec.flags) 16 else 0
        val c = ' ' + pad

        while (--fieldWidth >= 0) {
            if (buf.hasRemaining)
                buf.write(c)
        }
    }
    /* hmm even more zero padding? */
    while (i <= --precision) {
        if (buf.hasRemaining)
            buf.write('0')
    }
    /* actual digits of result */
    while (--i >= 0) {
        if (buf.hasRemaining)
            buf.write(tmp[i])
    }
    /* trailing space padding */
    while (--fieldWidth >= 0) {
        if (buf.hasRemaining)
            buf.write(' ')
    }
}


/**
 * vsnprintf - Format a string and place it in a buffer
 * @buf: The buffer to place the result into
 * @size: The size of the buffer, including the trailing null space
 * @fmt: The format string to use
 * @args: Arguments for the format string
 *
 * This function generally follows C99 vsnprintf, but has some
 * extensions and a few limitations:
 *
 *  - ``%n`` is unsupported
 *  - ``%p*`` is handled by pointer()
 *
 * See pointer() or Documentation/core-api/printk-formats.rst for more
 * extensive description.
 *
 * **Please update the documentation in both places when making changes**
 *
 * The return value is the number of characters which would
 * be generated for the given input, excluding the trailing
 * '\0', as per ISO C99. If you want to have the exact
 * number of characters written into @buf as return value
 * (not including the trailing '\0'), use vscnprintf(). If the
 * return is greater than or equal to @size, the resulting
 * string is truncated.
 *
 * If you're not already dealing with a va_list consider using snprintf().
 */
fun vsprintfMain(os: VEOS<*>, buf: ICharArrayPointer, fmtString: String, args: Iterator<Long>): Int {
    val spec = PrintfSpec()

    /* Reject out-of-range values early.  Large positive sizes are
       used for unknown buffer sizes. */
//    if (WARN_ON_ONCE(size > INT_MAX))
//        return 0;

    val str = buf//.charArrayPointer

    /* Make sure end is always >= buf */
//    if (end < buf) {
//        end = ((void *)-1);
//        size = end - buf;
//    }
    val fmt = fmtString.toCharArray().charArrayPointer

    fun vaArg() = args.next()

    loop@ while (fmt.get != 0.asChar) {
        val ptr = formatDecode(fmt, spec)

        when (spec.type) {
            FormatType.NONE -> str.write(fmt.readLast(ptr))

            FormatType.WIDTH -> setFieldWidth(spec, vaArg().asInt)

            FormatType.PRECISION -> setPrecision(spec, vaArg().asInt)

            FormatType.CHAR -> {
                if (LEFT !in spec.flags) {
                    while (--spec.fieldWidth > 0) {
                        if (str.hasRemaining)
                            str.write(' ')
                    }
                }
                val c = vaArg().asByte.asChar // cut off to 1 byte
                if (str.hasRemaining)
                    str.write(c)
                while (--spec.fieldWidth > 0) {
                    if (str.hasRemaining)
                        str.write(' ')
                }
            }

            FormatType.STR -> {
                val address = vaArg()
                val data = if (address == 0L) null else os.sys.readAsciiString(address)
                string(str, data, spec)
            }

            FormatType.PTR -> {
                TODO("Not implemented")
                //            str = pointer(
                //                fmt, str, end, va_arg(args, void *),
                //                spec
                //            );
                //            while (isalnum(*fmt))
                //                fmt++;
                //            break;
            }

            FormatType.PERCENT_CHAR -> {
                if (str.hasRemaining)
                    str.write('%')
            }

            FormatType.INVALID -> {
                /*
                * Presumably the arguments passed gcc's type
                * checking, but there is no safe or sane way
                * for us to continue parsing the format and
                * fetching from the va_list; the remaining
                * specifiers and arguments would be out of
                * sync.
                */
                break@loop
            }

            else -> {
                val num = when (spec.type) {
                    FormatType.LONG_LONG -> vaArg()
                    FormatType.ULONG -> vaArg()
                    FormatType.LONG -> vaArg()
                    FormatType.SIZE_T -> if (SIGN in spec.flags) vaArg() else vaArg()
                    FormatType.PTRDIFF -> TODO("Unknown type")
                    FormatType.UBYTE -> vaArg().asByte.asULong
                    FormatType.BYTE -> vaArg().asByte.asLong
                    FormatType.USHORT -> vaArg().asShort.asULong
                    FormatType.SHORT -> vaArg().asShort.asLong
                    FormatType.INT -> vaArg().asInt.asLong
                    FormatType.UINT -> vaArg().asInt.asULong
                    else -> throw IllegalStateException("Unknown format type ${spec.type}")
                }
                number(str, num, spec)
            }

        }
    }

//    out:
    if (buf.isNotEmpty()) {
        if (str.hasRemaining)
            str.write(0.asChar)
        else {
            str.prev()
            str.write(0.asChar)
        }
    }
    return str.offset - 1
}

fun simpleStrtoul(ptr: ICharArrayConstPointer, fieldWidth: Int, inBase: Int): Long? {
    var base = inBase
    when (base) {
        0 -> {
            if (ptr.get == '0') {
                ptr.next()
                if (ptr.get.toLowerCase() == 'x') {
                    ptr.next()
                    base = 16
                }
                else
                    base = 8
            }
            else
                base = 10
        }
        8, 10, 16 -> {}
        else -> throw IllegalArgumentException("Unknown radix $base")
    }
    var str = ptr.readUntil {
        when (base) {
            8 -> !isodigit(it)
            10 -> !isdigit(it)
            16 -> !isxdigit(it)
            else -> throw IllegalArgumentException("Unknown radix $base")
        }
    }
    if (str.isEmpty())
        return null
    if (fieldWidth > 0 && str.length > fieldWidth)
        str = str.substring(0 until fieldWidth)
    return str.toLong(base)
}

fun simpleStrtoull(ptr: ICharArrayConstPointer, fieldWidth: Int, inBase: Int) = simpleStrtoul(ptr, fieldWidth, inBase)

fun simpleStrtol(ptr: ICharArrayConstPointer, fieldWidth: Int, inBase: Int) =
        if (ptr.get == '-') {
            ptr.next()
            val result = simpleStrtoul(ptr, fieldWidth, inBase)
            if (result != null) -result else null
        }
        else
            simpleStrtoul(ptr, fieldWidth, inBase)

fun simpleStrtoll(ptr: ICharArrayConstPointer, fieldWidth: Int, inBase: Int) = simpleStrtol(ptr, fieldWidth, inBase)

fun skipSpaces(str: ICharArrayConstPointer) {
    while (isspace(str.get))
        str.next()
}

/**
 * vsscanf - Unformat a buffer into a list of arguments
 * @buf:	input buffer
 * @fmt:	format of buffer
 * @args:	arguments
 */
fun vsscanfMain(os: VEOS<*>, str: ICharArrayConstPointer, fmtString: String, args: Iterator<Long>): Int {
    var num = 0
    fun vaArg() = args.next()

    val fmt = fmtString.toCharArray().charArrayPointer

    loop@ while (fmt.get != 0.asChar) {
        /* skip any white space in format */
        /* white space in format matchs any amount of
         * white space, including none, in the input.
         */
        if (isspace(fmt.get)) {
            skipSpaces(fmt)
            skipSpaces(str)
        }

        /* anything that is not a conversion must match exactly */
        if (fmt.get != '%' && fmt.isNotNull) {
            val cond = fmt.get != str.get
            fmt.next()
            str.next()
            if (cond)
                break
            continue
        }

        if (fmt.isNull)
            break
        fmt.next()

        /* skip this conversion.
         * advance both strings to next white space
         */
        if (fmt.get == '*') {
            if (str.isNull)
                break
            while (!isspace(fmt.get) && fmt.get != '%' && fmt.isNotNull) {
                /* '%*[' not yet supported, invalid format */
                if (fmt.get == '[')
                    return num
                fmt.next()
            }
            while (!isspace(str.get) && str.isNotNull)
                str.next()
            continue
        }

        /* get field width */
        var fieldWidth = -1
        if (isdigit(fmt.get)) {
            fieldWidth = skipAtoi(fmt)
            if (fieldWidth <= 0)
                break
        }

        /* get conversion qualifier */
        var qualifier = 0.asChar
        if (fmt.get == 'h' || fmt.get.toLowerCase() == 'l' || fmt.get == 'z') {
            qualifier = fmt.get
            fmt.next()
            if (qualifier == fmt.get) {
                if (qualifier == 'h') {
                    qualifier = 'H'
                    fmt.next()
                } else if (qualifier == 'l') {
                    qualifier = 'L';
                    fmt.next()
                }
            }
        }

        if (fmt.isNull)
            break

        if (fmt.get == 'n') {
            /* return number of characters read so far */
            os.abi.writeInt(vaArg(), str.offset.asULong)
            fmt.next()
            continue
        }

        if (str.isNull)
            break

        var base = 10
        var isSign = false

        val c = fmt.get
        fmt.next()
        when (c) {
            'c' -> {
                var s = vaArg()
                if (fieldWidth == -1)
                    fieldWidth = 1
                do {
                    os.abi.writeChar(s++, str.get.asULong)
                    str.next()
                } while (--fieldWidth > 0 && str.isNotNull)
                num++
                continue@loop
            }

            's' -> {
                var s = vaArg()
                if (fieldWidth == -1)
                    fieldWidth = 0xFFFF // SHRT_MAX
                /* first, skip leading white space in buffer */
                skipSpaces(str)

                /* now copy until next white space */
                while (str.isNotNull && !isspace(str.get) && fieldWidth-- != 0) {
                    os.abi.writeChar(s++, str.get.asULong)
                    str.next()
                }
                os.abi.writeChar(s, 0L)
                num++
                continue@loop
            }
            /*
         * Warning: This implementation of the '[' conversion specifier
         * deviates from its glibc counterpart in the following ways:
         * (1) It does NOT support ranges i.e. '-' is NOT a special
         *     character
         * (2) It cannot match the closing bracket ']' itself
         * (3) A field width is required
         * (4) '%*[' (discard matching input) is currently not supported
         *
         * Example usage:
         * ret = sscanf("00:0a:95","%2[^:]:%2[^:]:%2[^:]",
         *		buf1, buf2, buf3);
         * if (ret < 3)
         *    // etc..
         */
            '[' -> {
                TODO("Not implemented")
//            var s = va_arg()
//			DECLARE_BITMAP(set, 256) = {0};
//			unsigned int len = 0;
//			bool negate = (*fmt == '^');
//
//			/* field width is required */
//			if (field_width == -1)
//				return num;
//
//			if (negate)
//				++fmt;
//
//			for ( ; *fmt && *fmt != ']'; ++fmt, ++len)
//				set_bit((u8)*fmt, set);
//
//			/* no ']' or no character set found */
//			if (!*fmt || !len)
//				return num;
//			++fmt;
//
//			if (negate) {
//				bitmap_complement(set, set, 256);
//				/* exclude null '\0' byte */
//				clear_bit(0, set);
//			}
//
//			/* match must be non-empty */
//			if (!test_bit((u8)*str, set))
//				return num;
//
//			while (test_bit((u8)*str, set) && field_width--)
//				*s++ = *str++;
//			*s = '\0';
//			++num;
//		    continue@loop
            }
            'o' -> base = 8
            'x', 'X' -> base = 16
            'i' -> {
                base = 0
                isSign = true
            }
            'd' -> isSign = true
            'u' -> {}
            '%' -> {
                if (str.get != '%')
                    return num
                str.next()
                continue@loop
            }
            /* invalid format; stop here */
            else -> return num
        }

        /* have some sort of integer conversion.
         * first, skip white space in buffer.
         */
        skipSpaces(str)

        // Note: We use more direct methods to trim the value to the size of the field
        //  and parse it into a number. In view of this, the following paragraph of the code,
        //  as well as the functions being called, have serious discrepancies with the
        //  original code from the Linux kernel.
        //  - shiftdj
        val value = if (isSign) {
            if (qualifier != 'L')
                simpleStrtol(str, fieldWidth, base)
            else
                simpleStrtoll(str, fieldWidth, base)
        }
        else if (qualifier != 'L')
            simpleStrtoul(str, fieldWidth, base)
        else
            simpleStrtoull(str, fieldWidth, base)

        if (value == null)
            break

        when (qualifier) {
            'H' -> os.abi.writeChar(vaArg(), value) /* that's 'hh' in format */
            'h' -> os.abi.writeShort(vaArg(), value)
            'l' -> os.abi.writeInt(vaArg(), value)
            'L' -> os.abi.writeLongLong(vaArg(), value)
            'z' -> os.abi.writeLongLong(vaArg(), value)
            else -> os.abi.writeInt(vaArg(), value)
        }
        num++

        if (str.isNull)
            break
    }

    return num
}

val CharArray.charArrayPointer get() = CharArrayPointer(this)
