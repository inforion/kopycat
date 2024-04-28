/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.veos.ports.posix


enum class PosixError(val id: ULong){
    ESUCCESS        ( 0u ), /* https://stackoverflow.com/questions/21025631/default-value-of-errno-variable */
    EPERM           ( 1u ), /* Operation not permitted */
    ENOENT          ( 2u ), /* No such file or directory */
    ESRCH           ( 3u ), /* No such process */
    EINTR           ( 4u ), /* Interrupted system call */
    EIO             ( 5u ), /* I/O error */
    ENXIO           ( 6u ), /* No such device or address */
    E2BIG           ( 7u ), /* Argument list too long */
    ENOEXEC         ( 8u ), /* Exec format error */
    EBADF           ( 9u ), /* Bad file number */
    ECHILD          (10u ), /* No child processes */
    EAGAIN          (11u ), /* Try again */
    EWOULDBLOCK     (11u ), /* Operation would block */
    ENOMEM          (12u ), /* Out of memory */
    EACCES          (13u ), /* Permission denied */
    EFAULT          (14u ), /* Bad address */
    ENOTBLK         (15u ), /* Block device required */
    EBUSY           (16u ), /* Device or resource busy */
    EEXIST          (17u ), /* File exists */
    EXDEV           (18u ), /* Cross-device link */
    ENODEV          (19u ), /* No such device */
    ENOTDIR         (20u ), /* Not a directory */
    EISDIR          (21u ), /* Is a directory */
    EINVAL          (22u ), /* Invalid argument */
    ENFILE          (23u ), /* File table overflow */
    EMFILE          (24u ), /* Too many open files */
    ENOTTY          (25u ), /* Not a typewriter */
    ETXTBSY         (26u ), /* Text file busy */
    EFBIG           (27u ), /* File too large */
    ENOSPC          (28u ), /* No space left on device */
    ESPIPE          (29u ), /* Illegal seek */
    EROFS           (30u ), /* Read-only file system */
    EMLINK          (31u ), /* Too many links */
    EPIPE           (32u ), /* Broken pipe */
    EDOM            (33u ), /* Math argument out of domain of func */
    ERANGE          (34u ), /* Math result not representable */
    EDEADLK         (35u ), /* Resource deadlock would occur */
    EDEADLOCK       (35u ), /* Resource deadlock would occur */
    ENAMETOOLONG    (36u ), /* File name too long */
    ENOLCK          (37u ), /* No record locks available */
    ENOSYS          (38u ), /* Function not implemented */
    ENOTEMPTY       (39u ), /* Directory not empty */
    ELOOP           (40u ), /* Too many symbolic links encountered */
    ENOMSG          (42u ), /* No message of desired type */
    EIDRM           (43u ), /* Identifier removed */
    ECHRNG          (44u ), /* Channel number out of range */
    EL2NSYNC        (45u ), /* Level 2 not synchronized */
    EL3HLT          (46u ), /* Level 3 halted */
    EL3RST          (47u ), /* Level 3 reset */
    ELNRNG          (48u ), /* Link number out of range */
    EUNATCH         (49u ), /* Protocol driver not attached */
    ENOCSI          (50u ), /* No CSI structure available */
    EL2HLT          (51u ), /* Level 2 halted */
    EBADE           (52u ), /* Invalid exchange */
    EBADR           (53u ), /* Invalid request descriptor */
    EXFULL          (54u ), /* Exchange full */
    ENOANO          (55u ), /* No anode */
    EBADRQC         (56u ), /* Invalid request code */
    EBADSLT         (57u ), /* Invalid slot */
    EBFONT          (59u ), /* Bad font file format */
    ENOSTR          (60u ), /* Device not a stream */
    ENODATA         (61u ), /* No data available */
    ETIME           (62u ), /* Timer expired */
    ENOSR           (63u ), /* Out of streams resources */
    ENONET          (64u ), /* Machine is not on the network */
    ENOPKG          (65u ), /* Package not installed */
    EREMOTE         (66u ), /* Object is remote */
    ENOLINK         (67u ), /* Link has been severed */
    EADV            (68u ), /* Advertise error */
    ESRMNT          (69u ), /* Srmount error */
    ECOMM           (70u ), /* Communication error on send */
    EPROTO          (71u ), /* Protocol error */
    EMULTIHOP       (72u ), /* Multihop attempted */
    EDOTDOT         (73u ), /* RFS specific error */
    EBADMSG         (74u ), /* Not a data message */
    EOVERFLOW       (75u ), /* Value too large for defined data type */
    ENOTUNIQ        (76u ), /* Name not unique on network */
    EBADFD          (77u ), /* File descriptor in bad state */
    EREMCHG         (78u ), /* Remote address changed */
    ELIBACC         (79u ), /* Can not access a needed shared library */
    ELIBBAD         (80u ), /* Accessing a corrupted shared library */
    ELIBSCN         (81u ), /* .lib section in a.out corrupted */
    ELIBMAX         (82u ), /* Attempting to link in too many shared libraries */
    ELIBEXEC        (83u ), /* Cannot exec a shared library directly */
    EILSEQ          (84u ), /* Illegal byte sequence */
    ERESTART        (85u ), /* Interrupted system call should be restarted */
    ESTRPIPE        (86u ), /* Streams pipe error */
    EUSERS          (87u ), /* Too many users */
    ENOTSOCK        (88u ), /* Socket operation on non-socket */
    EDESTADDRREQ    (89u ), /* Destination address required */
    EMSGSIZE        (90u ), /* Message too long */
    EPROTOTYPE      (91u ), /* Protocol wrong type for socket */
    ENOPROTOOPT     (92u ), /* Protocol not available */
    EPROTONOSUPPORT (93u ), /* Protocol not supported */
    ESOCKTNOSUPPORT (94u ), /* Socket type not supported */
    EOPNOTSUPP      (95u ), /* Operation not supported on transport endpoint */
    EPFNOSUPPORT    (96u ), /* Protocol family not supported */
    EAFNOSUPPORT    (97u ), /* Address family not supported by protocol */
    EADDRINUSE      (98u ), /* Address already in use */
    EADDRNOTAVAIL   (99u ), /* Cannot assign requested address */
    ENETDOWN        (100u), /* Network is down */
    ENETUNREACH     (101u), /* Network is unreachable */
    ENETRESET       (102u), /* Network dropped connection because of reset */
    ECONNABORTED    (103u), /* Software caused connection abort */
    ECONNRESET      (104u), /* Connection reset by peer */
    ENOBUFS         (105u), /* No buffer space available */
    EISCONN         (106u), /* Transport endpoint is already connected */
    ENOTCONN        (107u), /* Transport endpoint is not connected */
    ESHUTDOWN       (108u), /* Cannot send after transport endpoint shutdown */
    ETOOMANYREFS    (109u), /* Too many references: cannot splice */
    ETIMEDOUT       (110u), /* Connection timed out */
    ECONNREFUSED    (111u), /* Connection refused */
    EHOSTDOWN       (112u), /* Host is down */
    EHOSTUNREACH    (113u), /* No route to host */
    EALREADY        (114u), /* Operation already in progress */
    EINPROGRESS     (115u), /* Operation now in progress */
    ESTALE          (116u), /* Stale NFS file handle */
    EUCLEAN         (117u), /* Structure needs cleaning */
    ENOTNAM         (118u), /* Not a XENIX named type file */
    ENAVAIL         (119u), /* No XENIX semaphores available */
    EISNAM          (120u), /* Is a named type file */
    EREMOTEIO       (121u), /* Remote I/O error */
    EDQUOT          (122u), /* Quota exceeded */
    ENOMEDIUM       (123u), /* No medium found */
    EMEDIUMTYPE     (124u), /* Wrong medium type */
    ECANCELED       (125u), /* Operation Canceled */
    ENOKEY          (126u), /* Required key not available */
    EKEYEXPIRED     (127u), /* Key has expired */
    EKEYREVOKED     (128u), /* Key has been revoked */
    EKEYREJECTED    (129u), /* Key was rejected by service */
    EOWNERDEAD      (130u), /* Owner died */
    ENOTRECOVERABLE (131u) /* State not recoverable */
}
